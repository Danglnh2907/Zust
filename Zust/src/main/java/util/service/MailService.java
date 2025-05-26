package util.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import util.service.FileService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final long DEFAULT_MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long DEFAULT_MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    private final String username;
    private final String password;
    private final String baseUrl;
    private final Session session;
    private final FileService fileService;
    private final TemplateEngine templateEngine;
    private final Bucket rateLimiter;
    private final ExecutorService executor;
    private final Cache<String, byte[]> resourceCache;
    private final long maxAttachmentSize;
    private final long maxImageSize;

    public MailService(ServletContext context) {
        Dotenv dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
        this.username = dotenv.get("SMTP_USERNAME", "uiniacad.dev@gmail.com");
        this.password = dotenv.get("SMTP_PASSWORD", "uxgo qecc roxv okxh");
        String smtpHost = dotenv.get("SMTP_HOST", "smtp.gmail.com");
        int smtpPort = Integer.parseInt(dotenv.get("SMTP_PORT", "587"));
        this.baseUrl = dotenv.get("APP_BASE_URL", "http://localhost:8080");
        this.maxAttachmentSize = Long.parseLong(dotenv.get("MAX_ATTACHMENT_SIZE", String.valueOf(DEFAULT_MAX_ATTACHMENT_SIZE)));
        this.maxImageSize = Long.parseLong(dotenv.get("MAX_IMAGE_SIZE", String.valueOf(DEFAULT_MAX_IMAGE_SIZE)));
        int threadCount = Integer.parseInt(dotenv.get("EMAIL_THREAD_COUNT",
                String.valueOf(Math.max(4, Runtime.getRuntime().availableProcessors()))));
        long cacheSizeBytes = Long.parseLong(dotenv.get("CACHE_SIZE_BYTES", String.valueOf(50 * 1024 * 1024)));
        long cacheDurationHours = Long.parseLong(dotenv.get("CACHE_DURATION_HOURS", "2"));
        long rateLimit = Long.parseLong(dotenv.get("EMAIL_RATE_LIMIT", "100")); // Giảm xuống 100 để an toàn với Gmail

        if (username == null || password == null) {
            throw new IllegalStateException("Missing SMTP credentials in env");
        }

        this.fileService = context != null ? new FileService(context) : new FileService(dotenv.get("FILE_STORAGE_PATH"));

        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host", smtpHost);
        smtpProps.put("mail.smtp.port", String.valueOf(smtpPort));
        smtpProps.put("mail.smtp.connectiontimeout", "10000"); // 10s timeout
        smtpProps.put("mail.smtp.timeout", "10000");
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        this.session = Session.getInstance(smtpProps, auth);

        this.resourceCache = Caffeine.newBuilder()
                .maximumWeight(cacheSizeBytes)
                .weigher((String key, byte[] value) -> value.length)
                .expireAfterWrite(Duration.ofHours(cacheDurationHours))
                .build();

        Bandwidth limit = Bandwidth.classic(rateLimit, Refill.greedy(rateLimit, Duration.ofMinutes(1)));
        this.rateLimiter = Bucket.builder().addLimit(limit).build();

        this.executor = Executors.newFixedThreadPool(threadCount);

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(fileService.getAbsolutePath("", FileService.FileType.TEMPLATE) + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    public Map<String, String> sendPersonalizedWithAttachments(String templateName, String subject,
                                                               Map<String, Map<String, Object>> variables,
                                                               Map<String, String> imageMap, List<String> attachmentPaths,
                                                               int batchSize) throws IOException {
        validateTemplateExists(templateName);
        List<Attachment> attachments = loadAttachments(attachmentPaths);
        return sendPersonalized(templateName, subject, variables, imageMap, attachments, batchSize);
    }

    public Map<String, String> sendPersonalized(String templateName, String subject,
                                                Map<String, Map<String, Object>> variables,
                                                Map<String, String> imageMap, List<Attachment> attachments,
                                                int batchSize) {
        if (variables == null || variables.isEmpty()) {
            throw new IllegalArgumentException("Variables cannot be empty");
        }

        Map<String, String> failed = new ConcurrentHashMap<>();
        batchSize = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;
        List<List<String>> batches = partition(new ArrayList<>(variables.keySet()), batchSize);

        for (List<String> batch : batches) {
            sendBatch(batch, templateName, subject, variables, imageMap, attachments, failed);
        }
        return failed;
    }

    private void sendBatch(List<String> batch, String templateName, String subject,
                           Map<String, Map<String, Object>> variables, Map<String, String> imageMap,
                           List<Attachment> attachments, Map<String, String> failed) {
        int maxRetries = 3;
        Map<String, Integer> retryCounts = new ConcurrentHashMap<>();

        try (Transport transport = session.getTransport("smtp")) {
            connectTransport(transport); // Kết nối ban đầu
            List<CompletableFuture<Void>> tasks = new ArrayList<>();
            for (String email : batch) {
                CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                    int retries = retryCounts.computeIfAbsent(email, k -> 0);
                    if (retries >= maxRetries) {
                        failed.put(email, "Max retries reached");
                        logger.error("Failed to send to {}: Max retries reached", maskEmail(email));
                        return;
                    }
                    try {
                        rateLimiter.asBlocking().consume(1);
                        // Kiểm tra và tái kết nối nếu cần
                        if (!transport.isConnected()) {
                            logger.warn("Transport disconnected, reconnecting for {}", maskEmail(email));
                            connectTransport(transport);
                        }
                        MimeMessage msg = createMessage(templateName, subject, email,
                                variables.getOrDefault(email, Collections.emptyMap()), imageMap, attachments);
                        transport.sendMessage(msg, msg.getAllRecipients());
                        logger.info("Email sent to {}", maskEmail(email));
                    } catch (MessagingException ex) {
                        if (isTransientError(ex)) {
                            retryCounts.compute(email, (k, v) -> (v == null ? 0 : v) + 1);
                            throw new RuntimeException("Retry needed for " + maskEmail(email), ex);
                        } else {
                            logger.error("Failed to send to {}: {}", maskEmail(email), ex.getMessage());
                            failed.put(email, ex.getMessage());
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
                tasks.add(task);
            }
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.error("Batch processing error: {}", ex.getMessage(), ex);
                        }
                    });
        } catch (MessagingException ex) {
            logger.error("Transport error: {}", ex.getMessage(), ex);
            batch.forEach(email -> failed.put(email, "SMTP transport error: " + ex.getMessage()));
        }
    }

    private void connectTransport(Transport transport) throws MessagingException {
        try {
            if (!transport.isConnected()) {
                transport.connect();
                logger.info("SMTP transport connected");
            }
        } catch (MessagingException ex) {
            logger.error("Failed to connect SMTP transport: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private MimeMessage createMessage(String templateName, String subject, String recipient,
                                      Map<String, Object> vars, Map<String, String> imageMap,
                                      List<Attachment> attachments) throws MessagingException {
        InternetAddress addr = new InternetAddress(recipient);
        addr.validate();

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        msg.setRecipient(Message.RecipientType.TO, addr);
        msg.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart("related");
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(renderTemplate(templateName, vars), "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        if (imageMap != null) {
            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                MimeBodyPart imgPart = new MimeBodyPart();
                String fileName = Paths.get(entry.getKey()).getFileName().toString();
                try (InputStream in = fileService.getFileInputStream(fileName, FileService.FileType.IMAGE)) {
                    imgPart.setDataHandler(new DataHandler(new ByteArrayDataSource(in, detectMime(entry.getKey()))));
                    imgPart.setHeader("Content-ID", "<" + entry.getValue() + ">");
                    imgPart.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(imgPart);
                } catch (IOException ex) {
                    throw new MessagingException("Failed to load image: " + fileName, ex);
                }
            }
        }

        if (attachments != null) {
            for (Attachment att : attachments) {
                MimeBodyPart attPart = new MimeBodyPart();
                attPart.setDataHandler(new DataHandler(new ByteArrayDataSource(att.content, att.mimeType)));
                attPart.setFileName(att.fileName);
                multipart.addBodyPart(attPart);
            }
        }

        msg.setContent(multipart);
        return msg;
    }

    private void validateTemplateExists(String templateName) throws IOException {
        String path = fileService.getAbsolutePath(templateName + ".html", FileService.FileType.TEMPLATE);
        if (!new File(path).exists()) {
            throw new IOException("Template not found: " + path);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> vars) {
        Context ctx = new Context();
        ctx.setVariable("baseUrl", baseUrl);
        vars.forEach(ctx::setVariable);
        return templateEngine.process(templateName, ctx);
    }

    private byte[] loadResource(String relativePath) throws IOException {
        byte[] cached = resourceCache.getIfPresent(relativePath);
        if (cached != null) {
            return cached;
        }

        String fileName = Paths.get(relativePath).getFileName().toString();
        try (InputStream in = fileService.getFileInputStream(fileName, FileService.FileType.IMAGE)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalSize = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                totalSize += bytesRead;
                if (totalSize > maxImageSize) {
                    throw new IOException("Image size exceeds limit: " + fileName);
                }
                bos.write(buffer, 0, bytesRead);
            }
            byte[] data = bos.toByteArray();
            resourceCache.put(relativePath, data);
            return data;
        }
    }

    private List<Attachment> loadAttachments(List<String> paths) throws IOException {
        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }
        List<Attachment> list = new ArrayList<>();
        for (String path : paths) {
            String name = Paths.get(path).getFileName().toString();
            try (InputStream in = fileService.getFileInputStream(name, detectType(path))) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalSize = 0;
                while ((bytesRead = in.read(buffer)) != -1) {
                    totalSize += bytesRead;
                    if (totalSize > maxAttachmentSize) {
                        throw new IOException("Attachment size exceeds limit: " + name);
                    }
                    bos.write(buffer, 0, bytesRead);
                }
                byte[] content = bos.toByteArray();
                list.add(new Attachment(name, content, detectMime(path)));
            }
        }
        return list;
    }

    private List<List<String>> partition(List<String> list, int size) {
        List<List<String>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return parts;
    }

    private FileService.FileType detectType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".pdf")) return FileService.FileType.PDF;
        if (lower.endsWith(".docx")) return FileService.FileType.WORD;
        if (lower.endsWith(".xlsx")) return FileService.FileType.EXCEL;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) {
            return FileService.FileType.IMAGE;
        }
        if (lower.endsWith(".html")) return FileService.FileType.TEMPLATE;
        throw new IllegalArgumentException("Unknown file type: " + path);
    }

    private String detectMime(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".html")) return "text/html";
        return "application/octet-stream";
    }

    private boolean isTransientError(MessagingException ex) {
        String message = ex.getMessage().toLowerCase();
        return message.contains("timeout") || message.contains("connection") || message.contains("not connected");
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 3) return email;
        return email.substring(0, 3) + "****" + email.substring(atIndex);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public Map<String, String> buildImageMap(List<String> imagePaths, List<String> contentIds) throws IOException {
        if (imagePaths.size() != contentIds.size()) {
            throw new IllegalArgumentException("Image paths and Content-IDs must match in size");
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < imagePaths.size(); i++) {
            String img = imagePaths.get(i);
            String fileName = Paths.get(img).getFileName().toString();
            try (InputStream in = fileService.getFileInputStream(fileName, FileService.FileType.IMAGE)) {
                map.put(img, contentIds.get(i));
            }
        }
        return map;
    }

    public static class Attachment {
        public final String fileName;
        public final byte[] content;
        public final String mimeType;

        public Attachment(String fileName, byte[] content, String mimeType) {
            this.fileName = fileName;
            this.content = content;
            this.mimeType = mimeType;
        }
    }
    public static void main(String[] args) {
        ServletContext context = null; // Replace with actual ServletContext in a web application
        MailService mailService = null;
        try {
            // Khởi tạo MailService
            mailService = new MailService(context);
            logger.info("MailService initialized successfully");

            // Tạo dữ liệu mẫu cho email cá nhân hóa
            Map<String, Map<String, Object>> variables = new HashMap<>();
            variables.put("khai1234sd@gmail.com", Map.of(
                    "name", "Khai",
                    "token", "xyz123",
                    "verificationLink", mailService.baseUrl + "/verify?token=xyz123",
                    "items", List.of("Course A", "Course B", "Course C")
            ));
            variables.put("khainhce182286@fpt.edu.vn", Map.of(
                    "name", "Nguyen Hoang Khai",
                    "token", "xyz124",
                    "verificationLink", mailService.baseUrl + "/verify?token=xyz124",
                    "items", List.of("Course X", "Course Y", "Course Z")
            ));

            // Định nghĩa hình ảnh inline (đường dẫn tương đối từ FILE_STORAGE_PATH)
            List<String> imagePaths = List.of(
                    "img/z6551691367925_531d0046b054fb91725c5e3741c9f3bf.jpg",
                    "img/z6551691370813_6bd95414b6ae097d27db7ed09f9d682b.jpg"
            );
            List<String> imageContentIds = List.of(
                    "logoIcon",
                    "bannerIcon"
            );
            Map<String, String> imageMap = mailService.buildImageMap(imagePaths, imageContentIds);

            // Định nghĩa file đính kèm (đường dẫn tương đối từ FILE_STORAGE_PATH)
            List<String> attachmentPaths = List.of(
                    "xlsx/StudentTemplate(1).xlsx"
            );

            Map<String, String> failedRecipients = mailService.sendPersonalizedWithAttachments(
                    "welcome",                     // Tên template HTML (welcome.html)
                    "Welcome to UniAcad!",        // Tiêu đề email
                    variables,                    // Biến cá nhân hóa
                    imageMap,                     // Hình ảnh inline
                    attachmentPaths,              // File đính kèm
                    10
            );

            // In kết quả
            if (failedRecipients.isEmpty()) {
                System.out.println("All emails sent successfully!");
            } else {
                System.out.println("Some emails failed:");
                failedRecipients.forEach((email, reason) ->
                        System.out.println(maskEmail(email) + ": " + reason));
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error sending emails: " + e.getMessage());
            logger.error("Main method error: {}", e.getMessage(), e);
            e.printStackTrace();
        } finally {
            // Đóng MailService an toàn
            if (mailService != null) {
                mailService.shutdown();
                logger.info("MailService shut down");
            }
        }
    }
}
