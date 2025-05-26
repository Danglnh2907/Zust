package util.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.ServletContext;
// Sử dụng Log4j2 API trực tiếp
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MailService {
    // Sử dụng Log4j2 API
    private static final Logger logger = LogManager.getLogger(MailService.class);
    private static final AtomicInteger logSessionIdCounter = new AtomicInteger(0); // Để tạo ID cho mỗi phiên gửi mail lớn

    private static final int DEFAULT_BATCH_SIZE = 20;
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
    private final Cache<String, byte[]> imageResourceCache;
    private final long maxAttachmentSize;
    private final long maxImageSize;
    private final String mailServiceInstanceId; // ID cho instance này của MailService

    public MailService(ServletContext context) {
        this.mailServiceInstanceId = "MailServ-" + UUID.randomUUID().toString().substring(0, 8);
        long initStartTime = System.nanoTime();
        logger.info("[{}] Initializing MailService...", mailServiceInstanceId);

        Dotenv dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
        this.username = dotenv.get("SMTP_USERNAME", "uiniacad.dev@gmail.com");
        this.password = dotenv.get("SMTP_PASSWORD", "uxgo qecc roxv okxh"); // Gmail App Password
        String smtpHost = dotenv.get("SMTP_HOST", "smtp.gmail.com");
        int smtpPort = Integer.parseInt(dotenv.get("SMTP_PORT", "587"));
        this.baseUrl = dotenv.get("APP_BASE_URL", "http://localhost:8080/YourApp"); // Cập nhật nếu cần
        this.maxAttachmentSize = Long.parseLong(dotenv.get("MAX_ATTACHMENT_SIZE_BYTES", String.valueOf(DEFAULT_MAX_ATTACHMENT_SIZE)));
        this.maxImageSize = Long.parseLong(dotenv.get("MAX_IMAGE_SIZE_BYTES", String.valueOf(DEFAULT_MAX_IMAGE_SIZE)));

        int threadCount = Integer.parseInt(dotenv.get("EMAIL_THREAD_COUNT", String.valueOf(Math.max(2, Runtime.getRuntime().availableProcessors() / 2) + 1)));
        long cacheSizeBytes = Long.parseLong(dotenv.get("IMAGE_CACHE_SIZE_BYTES", String.valueOf(20 * 1024 * 1024)));
        long cacheDurationHours = Long.parseLong(dotenv.get("IMAGE_CACHE_DURATION_HOURS", "6"));
        long rateLimitPerMinute = Long.parseLong(dotenv.get("EMAIL_RATE_LIMIT_PER_MINUTE", "30"));

        logger.debug("[{}] SMTP User: {}", mailServiceInstanceId, username != null ? username.substring(0, Math.min(3, username.length())) + "***" : "NOT SET");
        logger.debug("[{}] SMTP Host: {}:{}", mailServiceInstanceId, smtpHost, smtpPort);
        logger.debug("[{}] Base URL: {}", mailServiceInstanceId, baseUrl);
        logger.debug("[{}] Max Attachment Size: {} bytes", mailServiceInstanceId, maxAttachmentSize);
        logger.debug("[{}] Max Image Size: {} bytes", mailServiceInstanceId, maxImageSize);
        logger.debug("[{}] Email Thread Count: {}", mailServiceInstanceId, threadCount);
        logger.debug("[{}] Image Cache Size: {} bytes, Duration: {} hours", mailServiceInstanceId, cacheSizeBytes, cacheDurationHours);
        logger.debug("[{}] Email Rate Limit: {} per minute", mailServiceInstanceId, rateLimitPerMinute);


        if (username == null || password == null) {
            logger.error("[{}] CRITICAL: Missing SMTP_USERNAME or SMTP_PASSWORD in .env file. MailService cannot function.", mailServiceInstanceId);
            throw new IllegalStateException("Missing SMTP_USERNAME or SMTP_PASSWORD in .env file");
        }

        this.fileService = (context != null) ? new FileService(context) : new FileService();
        String fileServiceRootPath = "N/A";
        try {
            fileServiceRootPath = this.fileService.getAbsolutePath(null, null);
        } catch (Exception e) {
            logger.warn("[{}] Could not determine FileService root path for logging: {}", mailServiceInstanceId, e.getMessage());
        }
        logger.info("[{}] FileService integration: Storage root determined as: {}", mailServiceInstanceId, fileServiceRootPath);

        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host", smtpHost);
        smtpProps.put("mail.smtp.port", String.valueOf(smtpPort));
        smtpProps.put("mail.smtp.connectiontimeout", "25000"); // 25s
        smtpProps.put("mail.smtp.timeout", "25000");         // 25s
        smtpProps.put("mail.smtp.writetimeout", "25000");    // 25s
        // smtpProps.put("mail.debug", dotenv.get("MAIL_DEBUG", "false")); // Bật nếu cần debug sâu hơn về JavaMail

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        this.session = Session.getInstance(smtpProps, auth);
        logger.debug("[{}] JavaMail Session configured for {}:{}", mailServiceInstanceId, smtpHost, smtpPort);


        this.imageResourceCache = Caffeine.newBuilder()
                .maximumWeight(cacheSizeBytes)
                .weigher((String key, byte[] value) -> value.length)
                .expireAfterWrite(Duration.ofHours(cacheDurationHours))
                .recordStats() // Bật thống kê cache
                .build();
        logger.info("[{}] Image resource cache initialized. Max weight: {} bytes, Expires after: {} hours.", mailServiceInstanceId, cacheSizeBytes, cacheDurationHours);


        Bandwidth limit = Bandwidth.classic(rateLimitPerMinute, Refill.greedy(rateLimitPerMinute, Duration.ofMinutes(1)));
        this.rateLimiter = Bucket.builder().addLimit(limit).build();
        logger.info("[{}] Rate limiter initialized: {} tokens/minute.", mailServiceInstanceId, rateLimitPerMinute);


        ThreadFactory namedThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String prefix = mailServiceInstanceId + "-Worker-";
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, prefix + threadNumber.getAndIncrement());
                t.setDaemon(false);
                logger.trace("[{}] Created new worker thread: {}", mailServiceInstanceId, t.getName());
                return t;
            }
        };
        this.executor = Executors.newFixedThreadPool(threadCount, namedThreadFactory);
        logger.info("[{}] Mail sending executor initialized with {} threads.", mailServiceInstanceId, threadCount);


        FileTemplateResolver resolver = new FileTemplateResolver();
        String templatesDir = fileService.getAbsolutePath("", FileService.FileType.TEMPLATE);
        logger.debug("[{}] Thymeleaf FileTemplateResolver determined template directory: '{}'", mailServiceInstanceId, templatesDir);
        resolver.setPrefix(templatesDir + (templatesDir.endsWith(File.separator) ? "" : File.separator));
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        boolean thymeleafCacheEnabled = !"true".equalsIgnoreCase(dotenv.get("THYMELEAF_DISABLE_CACHE_DEV", "false"));
        resolver.setCacheable(thymeleafCacheEnabled);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
        logger.info("[{}] Thymeleaf configured. Template prefix: '{}', Caching enabled: {}", mailServiceInstanceId, resolver.getPrefix(), thymeleafCacheEnabled);
        logger.info("[{}] MailService initialized successfully in {} ms.", mailServiceInstanceId, (System.nanoTime() - initStartTime) / 1_000_000);
    }

    public Map<String, String> sendPersonalizedWithAttachments(String templateName, String subject,
                                                               Map<String, Map<String, Object>> variablesPerEmail,
                                                               Map<String, String> inlineImageMap, List<String> attachmentFilePaths,
                                                               int batchSize) throws IOException {
        String operationId = "SendOp-" + logSessionIdCounter.incrementAndGet();
        long opStartTime = System.nanoTime();
        logger.info("[{}] [{}] Initiating sendPersonalizedWithAttachments. Template: '{}', Subject: '{}', Recipients: {}, Images: {}, Attachments: {}, BatchSize: {}",
                mailServiceInstanceId, operationId, templateName, subject, variablesPerEmail.size(),
                inlineImageMap != null ? inlineImageMap.size() : 0,
                attachmentFilePaths != null ? attachmentFilePaths.size() : 0,
                batchSize);

        logger.debug("[{}] [{}] Validating template: '{}'", mailServiceInstanceId, operationId, templateName);
        validateTemplateExists(templateName, operationId); // Truyền operationId

        logger.debug("[{}] [{}] Loading attachments from paths: {}", mailServiceInstanceId, operationId, attachmentFilePaths);
        List<Attachment> attachments = loadAttachments(attachmentFilePaths, operationId); // Truyền operationId
        logger.debug("[{}] [{}] {} attachments loaded.", mailServiceInstanceId, operationId, attachments.size());

        Map<String, String> failed = sendPersonalized(templateName, subject, variablesPerEmail, inlineImageMap, attachments, batchSize, operationId);
        logger.info("[{}] [{}] sendPersonalizedWithAttachments completed in {} ms. Failed count: {}",
                mailServiceInstanceId, operationId, (System.nanoTime() - opStartTime) / 1_000_000, failed.size());
        return failed;
    }

    // Thêm operationId để theo dõi
    public Map<String, String> sendPersonalized(String templateName, String subject,
                                                Map<String, Map<String, Object>> variablesPerEmail,
                                                Map<String, String> inlineImageMap, List<Attachment> attachments,
                                                int batchSize, String operationId) {
        long personalizeStartTime = System.nanoTime();
        if (variablesPerEmail == null || variablesPerEmail.isEmpty()) {
            logger.warn("[{}] [{}] No variables provided for personalized email sending. Template: {}", mailServiceInstanceId, operationId, templateName);
            return Collections.emptyMap();
        }

        Map<String, String> failedRecipients = new ConcurrentHashMap<>();
        int effectiveBatchSize = (batchSize <= 0) ? DEFAULT_BATCH_SIZE : batchSize;
        List<List<String>> emailBatches = partition(new ArrayList<>(variablesPerEmail.keySet()), effectiveBatchSize);

        logger.info("[{}] [{}] Starting sendPersonalized for template '{}'. Total recipients: {}. Batches: {}. Batch size: {}",
                mailServiceInstanceId, operationId, templateName, variablesPerEmail.size(), emailBatches.size(), effectiveBatchSize);

        AtomicInteger batchCounter = new AtomicInteger(1);
        for (List<String> emailBatch : emailBatches) {
            int currentBatchNum = batchCounter.getAndIncrement();
            logger.info("[{}] [{}] Processing Batch {}/{} for template '{}', emails in batch: {}",
                    mailServiceInstanceId, operationId, currentBatchNum, emailBatches.size(), templateName, emailBatch.size());
            sendBatch(emailBatch, templateName, subject, variablesPerEmail, inlineImageMap, attachments, failedRecipients, operationId, currentBatchNum);
        }
        logger.info("[{}] [{}] Finished sending all batches for template '{}'. Total failed count: {}. Time: {} ms",
                mailServiceInstanceId, operationId, templateName, failedRecipients.size(), (System.nanoTime() - personalizeStartTime) / 1_000_000);
        return failedRecipients;
    }

    // Thêm operationId và batchNum để theo dõi
    private void sendBatch(List<String> emailBatch, String templateName, String subject,
                           Map<String, Map<String, Object>> allVariables, Map<String, String> inlineImageMap,
                           List<Attachment> attachments, Map<String, String> failedRecipientsMap,
                           String operationId, int batchNum) {
        long batchStartTime = System.nanoTime();
        String batchLogId = operationId + "-Batch-" + batchNum;
        logger.info("[{}] [{}] Starting processing. Recipients in batch: {}", mailServiceInstanceId, batchLogId, emailBatch.size());

        int maxRetries = 2;
        long singleEmailSendTimeoutMillis = 45 * 1000;
        long rateLimitAcquireTimeoutMillis = 15 * 1000;

        try (Transport transport = session.getTransport("smtp")) {
            logger.debug("[{}] [{}] Attempting initial connection of shared transport...", mailServiceInstanceId, batchLogId);
            connectTransport(transport, batchLogId);
            logger.info("[{}] [{}] Shared transport connected.", mailServiceInstanceId, batchLogId);

            List<CompletableFuture<Void>> emailTasks = new ArrayList<>();
            AtomicInteger emailInBatchCounter = new AtomicInteger(1);

            for (String recipientEmail : emailBatch) {
                final String emailLogId = batchLogId + "-Email-" + emailInBatchCounter.getAndIncrement() + "-" + maskEmail(recipientEmail);

                CompletableFuture<Void> emailTask = CompletableFuture.runAsync(() -> {
                    logger.debug("[{}] [{}] Starting processing.", mailServiceInstanceId, emailLogId);
                    int attempts = 0;
                    boolean sentSuccessfully = false;
                    long emailTaskStartTime = System.nanoTime();

                    while (attempts < maxRetries && !sentSuccessfully &&
                            (System.nanoTime() - emailTaskStartTime) < singleEmailSendTimeoutMillis) {
                        long attemptStartTime = System.nanoTime();
                        try {
                            logger.debug("[{}] [{}] Attempt {}: Acquiring rate limit token (timeout {}ms)...", mailServiceInstanceId, emailLogId, attempts + 1, rateLimitAcquireTimeoutMillis);
                            if (!rateLimiter.tryConsume(1)) {
                                logger.warn("[{}] [{}] Attempt {}: Could not acquire rate limit token within timeout. Will retry if attempts/time allow. Time for this try: {} ms",
                                        mailServiceInstanceId, emailLogId, attempts + 1, (System.nanoTime() - attemptStartTime) / 1_000_000);
                                attempts++;
                                if (attempts < maxRetries && (System.nanoTime() - emailTaskStartTime) < singleEmailSendTimeoutMillis) {
                                    Thread.sleep(Math.min(1000L + (attempts * 500L), 5000L) ); // Chờ ngắn hơn
                                }
                                continue;
                            }
                            logger.debug("[{}] [{}] Attempt {}: Rate limit token acquired in {} ms.", mailServiceInstanceId, emailLogId, attempts + 1, (System.nanoTime() - attemptStartTime) / 1_000_000);

                            synchronized (transport) {
                                if (!transport.isConnected()) {
                                    logger.warn("[{}] [{}] Attempt {}: Shared transport DISCONNECTED. Reconnecting...", mailServiceInstanceId, emailLogId, attempts + 1);
                                    connectTransport(transport, emailLogId + "-ReconnectAttempt-" + attempts);
                                    logger.info("[{}] [{}] Attempt {}: Shared transport RECONNECTED.", mailServiceInstanceId, emailLogId, attempts + 1);
                                } else {
                                    logger.trace("[{}] [{}] Attempt {}: Shared transport is connected.", mailServiceInstanceId, emailLogId, attempts + 1);
                                }
                            }

                            Map<String, Object> emailSpecificVars = allVariables.getOrDefault(recipientEmail, Collections.emptyMap());
                            logger.debug("[{}] [{}] Attempt {}: Creating MimeMessage for {}...", mailServiceInstanceId, emailLogId, attempts + 1, recipientEmail);
                            MimeMessage mimeMessage = createMessage(templateName, subject, recipientEmail, emailSpecificVars, inlineImageMap, attachments, emailLogId);
                            logger.debug("[{}] [{}] Attempt {}: MimeMessage created. Sending to {}...", mailServiceInstanceId, emailLogId, attempts + 1, recipientEmail);

                            long sendTime = System.nanoTime();
                            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                            logger.info("[{}] [{}] Email sent successfully to {} (Attempt {}, Send time: {} ms, Total email time: {} ms)",
                                    mailServiceInstanceId, emailLogId, recipientEmail, attempts + 1,
                                    (System.nanoTime() - sendTime) / 1_000_000,
                                    (System.nanoTime() - emailTaskStartTime) / 1_000_000);
                            sentSuccessfully = true;

                        } catch (MessagingException ex) {
                            attempts++;
                            logger.error("[{}] [{}] Attempt {} to send to {} FAILED with MessagingException (Duration: {} ms): {}",
                                    mailServiceInstanceId, emailLogId, attempts, recipientEmail, (System.nanoTime() - attemptStartTime) / 1_000_000, ex.getMessage(), ex);
                            if (!isTransientError(ex) || attempts >= maxRetries || (System.nanoTime() - emailTaskStartTime) >= singleEmailSendTimeoutMillis) {
                                String failureReason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                                if (attempts >= maxRetries) failureReason += " (Max retries reached)";
                                if ((System.nanoTime() - emailTaskStartTime) >= singleEmailSendTimeoutMillis) failureReason += " (Email processing timeout)";
                                failedRecipientsMap.put(recipientEmail, failureReason);
                                logger.warn("[{}] [{}] Email to {} permanently failed. Reason: {}", mailServiceInstanceId, emailLogId, recipientEmail, failureReason);
                                break;
                            }
                            try {
                                long backoff = Math.min(1000L * (1L << (attempts -1) ), 10000L); // 1s, 2s, 4s, max 10s
                                logger.debug("[{}] [{}] Attempt {}: Transient error. Waiting {}ms before retry for {}.", mailServiceInstanceId, emailLogId, attempts, backoff, recipientEmail);
                                Thread.sleep(backoff);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                failedRecipientsMap.put(recipientEmail, "Send interrupted during retry wait: " + ie.getMessage());
                                logger.error("[{}] [{}] Interrupted during retry wait for {}: {}", mailServiceInstanceId, emailLogId, recipientEmail, ie.getMessage());
                                break;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.error("[{}] [{}] Thread interrupted while processing email for {}: {}", mailServiceInstanceId, emailLogId, recipientEmail, e.getMessage());
                            failedRecipientsMap.put(recipientEmail, "Process interrupted for email");
                            break;
                        } catch (Exception e) {
                            attempts++;
                            logger.error("[{}] [{}] UNEXPECTED error sending email to {} (Attempt {}, Duration: {} ms): {}",
                                    mailServiceInstanceId, emailLogId, recipientEmail, attempts, (System.nanoTime() - attemptStartTime) / 1_000_000, e.getMessage(), e);
                            failedRecipientsMap.put(recipientEmail, "Unexpected error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                            break;
                        }
                    }

                    if (!sentSuccessfully && !failedRecipientsMap.containsKey(recipientEmail)) {
                        String finalReason = (System.nanoTime() - emailTaskStartTime) >= singleEmailSendTimeoutMillis ? " (Overall email timeout)" : " (Ran out of retries)";
                        logger.error("[{}] [{}] Email to {} was not sent after {} attempts{}. Total time: {} ms. Marking as failed.",
                                mailServiceInstanceId, emailLogId, recipientEmail, attempts, finalReason, (System.nanoTime() - emailTaskStartTime) / 1_000_000);
                        failedRecipientsMap.put(recipientEmail, "Failed after " + attempts + " attempts" + finalReason);
                    }
                    logger.debug("[{}] [{}] Finished processing. Success: {}. Total time: {} ms",
                            mailServiceInstanceId, emailLogId, sentSuccessfully, (System.nanoTime() - emailTaskStartTime) / 1_000_000);
                }, executor);
                emailTasks.add(emailTask);
            }

            CompletableFuture<Void> allOfBatch = CompletableFuture.allOf(emailTasks.toArray(new CompletableFuture[0]));
            try {
                long batchProcessingTimeoutMillis = (long)emailBatch.size() * singleEmailSendTimeoutMillis + (60 * 1000); // Thêm 60s overhead cho batch
                logger.debug("[{}] [{}] Waiting for {} email tasks to complete with batch timeout of {}ms...", mailServiceInstanceId, batchLogId, emailBatch.size(), batchProcessingTimeoutMillis);
                allOfBatch.get(batchProcessingTimeoutMillis, TimeUnit.MILLISECONDS);
                logger.info("[{}] [{}] All {} email tasks in batch completed processing (either success or final failure). Batch duration: {} ms",
                        mailServiceInstanceId, batchLogId, emailBatch.size(), (System.nanoTime() - batchStartTime) / 1_000_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("[{}] [{}] Batch processing was interrupted: {}", mailServiceInstanceId, batchLogId, e.getMessage(), e);
                emailBatch.forEach(email -> failedRecipientsMap.putIfAbsent(email, "Batch processing interrupted by system"));
                cancelTasks(emailTasks, batchLogId, "BatchInterrupted");
            } catch (ExecutionException e) {
                logger.error("[{}] [{}] A critical exception occurred during batch completion: {}", mailServiceInstanceId, batchLogId, e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e.getCause());
                emailBatch.forEach(email -> failedRecipientsMap.putIfAbsent(email, "Batch execution error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage())));
            } catch (TimeoutException e) {
                logger.error("[{}] [{}] Overall batch processing TIMED OUT after {} ms. Cancelling unfinished tasks.", mailServiceInstanceId, batchLogId, (System.nanoTime() - batchStartTime) / 1_000_000);
                cancelTasksAndUpdateFailures(emailTasks, emailBatch, failedRecipientsMap, batchLogId, "BatchOverallTimeout");
            } finally {
                logger.debug("[{}] [{}] Ensuring all tasks in batch are finalized.", mailServiceInstanceId, batchLogId);
                finalizeTasks(emailTasks, batchLogId);
            }
        } catch (MessagingException ex) {
            logger.error("[{}] [{}] CRITICAL: Failed to establish initial SMTP transport for batch: {}. All emails in this batch will fail.", mailServiceInstanceId, batchLogId, ex.getMessage(), ex);
            emailBatch.forEach(email -> failedRecipientsMap.putIfAbsent(email, "SMTP transport layer error for batch: " + ex.getMessage()));
        } catch (Exception e) {
            logger.error("[{}] [{}] CRITICAL: Unexpected error during batch setup or transport handling: {}. All emails in this batch may fail.", mailServiceInstanceId, batchLogId, e.getMessage(), e);
            emailBatch.forEach(email -> failedRecipientsMap.putIfAbsent(email, "Unexpected batch error: " + e.getMessage()));
        }
        logger.info("[{}] [{}] Batch processing completed. Failed in this batch: {}. Batch duration: {} ms",
                mailServiceInstanceId, batchLogId,
                emailBatch.stream().filter(failedRecipientsMap::containsKey).count(),
                (System.nanoTime() - batchStartTime) / 1_000_000);
    }

    private void cancelTasks(List<CompletableFuture<Void>> tasks, String logContext, String reason) {
        logger.warn("[{}] [{}] Attempting to cancel {} tasks due to: {}", mailServiceInstanceId, logContext, tasks.size(), reason);
        tasks.forEach(task -> {
            if (!task.isDone()) {
                boolean cancelled = task.cancel(true); // true to interrupt the running thread
                logger.trace("[{}] [{}] Task cancelled: {}. StatusIsDone: {}, StatusIsCancelled: {}",
                        mailServiceInstanceId, logContext, cancelled, task.isDone(), task.isCancelled());
            }
        });
    }

    private void cancelTasksAndUpdateFailures(List<CompletableFuture<Void>> tasks, List<String> emailBatch,
                                              Map<String, String> failedRecipientsMap, String logContext, String reason) {
        logger.warn("[{}] [{}] Cancelling tasks and updating failures for {} emails due to: {}", mailServiceInstanceId, logContext, tasks.size(), reason);
        for (int i = 0; i < tasks.size(); i++) {
            CompletableFuture<Void> task = tasks.get(i);
            if (!task.isDone()) {
                task.cancel(true);
                String recipient = (i < emailBatch.size()) ? emailBatch.get(i) : "UnknownRecipient-" + i;
                failedRecipientsMap.putIfAbsent(recipient, "Email task cancelled due to " + reason);
                logger.warn("[{}] [{}] Email to {} marked as failed (task cancelled). Reason: {}", mailServiceInstanceId, logContext, maskEmail(recipient), reason);
            }
        }
    }

    private void finalizeTasks(List<CompletableFuture<Void>> tasks, String logContext) {
        tasks.forEach(task -> {
            if (!task.isDone()) {
                try {
                    logger.trace("[{}] [{}] Finalizing task, waiting 1s for it to complete or be cancelled...", mailServiceInstanceId, logContext);
                    task.get(1, TimeUnit.SECONDS); // Give it a very short time to complete if it was close
                } catch (Exception e) {
                    logger.trace("[{}] [{}] Exception while finalizing task (likely already cancelled or timed out): {}", mailServiceInstanceId, logContext, e.getMessage());
                    if (!task.isDone()) task.cancel(true); // Force cancel again if still not done
                }
            }
        });
    }


    private void connectTransport(Transport transport, String logContext) throws MessagingException {
        long connStartTime = System.nanoTime();
        if (!transport.isConnected()) {
            String host = session.getProperty("mail.smtp.host");
            String port = session.getProperty("mail.smtp.port");
            logger.debug("[{}] [{}] Attempting to connect transport to SMTP server: {}:{}", mailServiceInstanceId, logContext, host, port);
            try {
                transport.connect();
                logger.info("[{}] [{}] Transport connected successfully to {}:{} in {} ms", mailServiceInstanceId, logContext, host, port, (System.nanoTime() - connStartTime) / 1_000_000);
            } catch (AuthenticationFailedException e) {
                logger.error("[{}] [{}] SMTP Authentication Failed for user '{}' on {}:{}. Check credentials. Duration: {} ms", mailServiceInstanceId, logContext, username, host, port, (System.nanoTime() - connStartTime) / 1_000_000, e);
                throw e;
            } catch (MessagingException e) {
                logger.error("[{}] [{}] Failed to connect SMTP transport to {}:{}. Duration: {} ms. Error: {}", mailServiceInstanceId, logContext, host, port, (System.nanoTime() - connStartTime) / 1_000_000, e.getMessage(), e);
                throw e;
            }
        } else {
            logger.trace("[{}] [{}] Transport already connected.", mailServiceInstanceId, logContext);
        }
    }

    private MimeMessage createMessage(String templateName, String subject, String recipientEmail,
                                      Map<String, Object> emailVariables, Map<String, String> inlineImageMap,
                                      List<Attachment> attachments, String emailLogId) throws MessagingException {
        long createMsgStartTime = System.nanoTime();
        logger.debug("[{}] [{}] Creating MimeMessage for recipient: {}, Subject: '{}', Template: '{}'", mailServiceInstanceId, emailLogId, recipientEmail, subject, templateName);

        InternetAddress toAddress = new InternetAddress(recipientEmail);
        toAddress.validate();

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipient(Message.RecipientType.TO, toAddress);
            mimeMessage.setSubject(subject, "UTF-8");
            mimeMessage.setSentDate(new Date());
        } catch (AddressException e) {
            logger.error("[{}] [{}] Invalid email address format for recipient '{}' or sender '{}'", mailServiceInstanceId, emailLogId, recipientEmail, username, e);
            throw e;
        }

        MimeMultipart multipartContainer = new MimeMultipart("related");
        MimeBodyPart htmlContentPart = new MimeBodyPart();
        logger.debug("[{}] [{}] Rendering HTML template '{}'...", mailServiceInstanceId, emailLogId, templateName);
        long renderStartTime = System.nanoTime();
        String renderedHtml = renderTemplate(templateName, emailVariables);
        logger.debug("[{}] [{}] HTML template '{}' rendered in {} ms.", mailServiceInstanceId, emailLogId, templateName, (System.nanoTime() - renderStartTime) / 1_000_000);
        htmlContentPart.setContent(renderedHtml, "text/html; charset=UTF-8");
        multipartContainer.addBodyPart(htmlContentPart);
        logger.debug("[{}] [{}] HTML part added.", mailServiceInstanceId, emailLogId);

        if (inlineImageMap != null && !inlineImageMap.isEmpty()) {
            logger.debug("[{}] [{}] Processing {} inline image(s)...", mailServiceInstanceId, emailLogId, inlineImageMap.size());
            for (Map.Entry<String, String> entry : inlineImageMap.entrySet()) {
                String imageKeyInMap = entry.getKey();
                String contentIdValue = entry.getValue();
                String imageFileNameForFileService = Paths.get(imageKeyInMap).getFileName().toString();
                logger.debug("[{}] [{}] Adding inline image: FileKey='{}', FileName='{}', ContentID='{}'", mailServiceInstanceId, emailLogId, imageKeyInMap, imageFileNameForFileService, contentIdValue);

                long imgLoadStartTime = System.nanoTime();
                // Sử dụng imageResourceCache.get() để tải từ cache hoặc từ FileService nếu chưa có
                byte[] imageData;
                try {
                    imageData = imageResourceCache.get(imageFileNameForFileService, k -> {
                        logger.trace("[{}] [{}] Cache miss for image '{}'. Loading from FileService...", mailServiceInstanceId, emailLogId, k);
	                    Optional<byte[]> dataOpt = fileService.getFileAsBytes(k, FileService.FileType.IMAGE);
	                    if (dataOpt.isEmpty()) {
	                        logger.error("[{}] [{}] CRITICAL: Image '{}' not found by FileService for caching.", mailServiceInstanceId, emailLogId, k);
	                        throw new RuntimeException(new IOException("Image not found by FileService: " + k));
	                    }
	                    byte[] data = dataOpt.get();
	                    if (data.length > this.maxImageSize) {
	                        logger.error("[{}] [{}] Image '{}' ({} bytes) exceeds maxImageSize ({} bytes) during cache load.", mailServiceInstanceId, emailLogId, k, data.length, this.maxImageSize);
	                        throw new RuntimeException(new IOException("Image size exceeds limit: " + k));
	                    }
	                    logger.debug("[{}] [{}] Image '{}' loaded from FileService for cache. Size: {} bytes", mailServiceInstanceId, emailLogId, k, data.length);
	                    return data;
                    });
                } catch (Exception e) { // Bắt RuntimeException từ mappingFunction
                    logger.error("[{}] [{}] Failed to load/cache inline image '{}': {}", mailServiceInstanceId, emailLogId, imageFileNameForFileService, e.getMessage(), e.getCause() != null ? e.getCause() : e);
                    throw new MessagingException("Failed to load/cache inline image: " + imageFileNameForFileService, e.getCause() != null ? (IOException)e.getCause() : new IOException(e.getMessage()));
                }


                logger.debug("[{}] [{}] Image '{}' ({} bytes) obtained (from cache or disk) in {} ms.", mailServiceInstanceId, emailLogId, imageFileNameForFileService, imageData.length, (System.nanoTime() - imgLoadStartTime) / 1_000_000);


                MimeBodyPart imagePart = new MimeBodyPart();
                String mimeType = detectMime(imageFileNameForFileService, emailLogId);
                imagePart.setDataHandler(new DataHandler(new ByteArrayDataSource(imageData, mimeType)));
                imagePart.setHeader("Content-ID", "<" + contentIdValue + ">");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                multipartContainer.addBodyPart(imagePart);
                logger.trace("[{}] [{}] Inline image '{}' (MIME: {}) added.", mailServiceInstanceId, emailLogId, imageFileNameForFileService, mimeType);
            }
        } else {
            logger.trace("[{}] [{}] No inline images to process.", mailServiceInstanceId, emailLogId);
        }

        if (attachments != null && !attachments.isEmpty()) {
            logger.debug("[{}] [{}] Adding {} attachment(s)...", mailServiceInstanceId, emailLogId, attachments.size());
            for (Attachment attachment : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment.content, attachment.mimeType)));
                try {
                    attachmentPart.setFileName(MimeUtility.encodeText(attachment.fileName, "UTF-8", null));
                } catch (java.io.UnsupportedEncodingException e) {
                    logger.warn("[{}] [{}] Could not UTF-8 encode attachment filename '{}'. Using raw name.", mailServiceInstanceId, emailLogId, attachment.fileName, e);
                    attachmentPart.setFileName(attachment.fileName);
                }
                multipartContainer.addBodyPart(attachmentPart);
                logger.trace("[{}] [{}] Attachment '{}' (MIME: {}, Size: {} bytes) added.", mailServiceInstanceId, emailLogId, attachment.fileName, attachment.mimeType, attachment.content.length);
            }
        } else {
            logger.trace("[{}] [{}] No attachments to process.", mailServiceInstanceId, emailLogId);
        }

        mimeMessage.setContent(multipartContainer);
        logger.info("[{}] [{}] MimeMessage created successfully for {}. Total creation time: {} ms.", mailServiceInstanceId, emailLogId, recipientEmail, (System.nanoTime() - createMsgStartTime) / 1_000_000);
        return mimeMessage;
    }

    private void validateTemplateExists(String templateName, String operationId) throws IOException {
        long validateStartTime = System.nanoTime();
        String templateFileName = templateName + ".html";
        logger.debug("[{}] [{}] Validating existence of template file: '{}' using FileType: TEMPLATE", mailServiceInstanceId, operationId, templateFileName);
        Optional<String> absolutePathOpt = fileService.findAbsolutePath(templateFileName, FileService.FileType.TEMPLATE);

        if (absolutePathOpt.isEmpty()) {
            String expectedPathAttempt = fileService.getAbsolutePath(templateFileName, FileService.FileType.TEMPLATE);
            logger.error("[{}] [{}] Email template '{}' NOT FOUND. FileService looked at approx: {}", mailServiceInstanceId, operationId, templateFileName, expectedPathAttempt);
            throw new IOException("Email template '" + templateFileName + "' not found. Expected at: " + expectedPathAttempt);
        }
        logger.info("[{}] [{}] Email template '{}' confirmed to exist at: {}. Validation time: {} ms",
                mailServiceInstanceId, operationId, templateFileName, absolutePathOpt.get(), (System.nanoTime() - validateStartTime) / 1_000_000);
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        // Không cần log ID ở đây vì nó được gọi từ createMessage đã có log ID
        long renderStartTime = System.nanoTime();
        Context context = new Context();
        context.setVariable("baseUrl", this.baseUrl);
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        String html = templateEngine.process(templateName, context);
        logger.trace("Template '{}' rendered. Length: {}. Time: {} ms", templateName, html.length(), (System.nanoTime() - renderStartTime) / 1_000_000);
        return html;
    }

    // Phương thức này giờ không được dùng trực tiếp trong luồng gửi email chính
    // nếu createMessage dùng cache.get()
    @SuppressWarnings("unused")
    private byte[] loadImageResourceForCache(String imageFileName, String operationId) throws IOException {
        long loadStartTime = System.nanoTime();
        logger.debug("[{}] [{}] Attempting to load image resource for cache: '{}'", mailServiceInstanceId, operationId, imageFileName);
        // Thử lấy từ cache trước, nhưng hàm này thường được gọi bởi cache.get() nên không cần check cache ở đây nữa.

        logger.debug("[{}] [{}] Loading image '{}' from FileService (FileType: IMAGE) for cache.", mailServiceInstanceId, operationId, imageFileName);
        Optional<byte[]> imageBytesOpt = fileService.getFileAsBytes(imageFileName, FileService.FileType.IMAGE);
        if (imageBytesOpt.isEmpty()) {
            String expectedPath = fileService.getAbsolutePath(imageFileName, FileService.FileType.IMAGE);
            logger.error("[{}] [{}] Image resource not found via FileService: '{}'. Expected at: {}", mailServiceInstanceId, operationId, imageFileName, expectedPath);
            throw new IOException("Image resource not found via FileService: " + imageFileName + ". Expected at: " + expectedPath);
        }
        byte[] imageData = imageBytesOpt.get();
        logger.debug("[{}] [{}] Image '{}' loaded from FileService. Size: {} bytes. Time: {} ms",
                mailServiceInstanceId, operationId, imageFileName, imageData.length, (System.nanoTime() - loadStartTime) / 1_000_000);

        if (imageData.length > this.maxImageSize) {
            logger.error("[{}] [{}] Image resource '{}' size ({} bytes) exceeds limit of {} bytes.",
                    mailServiceInstanceId, operationId, imageFileName, imageData.length, this.maxImageSize);
            throw new IOException("Image resource '" + imageFileName + "' size (" + imageData.length
                    + " bytes) exceeds limit of " + this.maxImageSize + " bytes.");
        }
        // Việc put vào cache sẽ do Caffeine tự làm nếu hàm này là mappingFunction
        logger.info("[{}] [{}] Image '{}' prepared for cache. Size: {} bytes", mailServiceInstanceId, operationId, imageFileName, imageData.length);
        return imageData;
    }

    private List<Attachment> loadAttachments(List<String> attachmentSourcePaths, String operationId) throws IOException {
        long loadAllAttachStartTime = System.nanoTime();
        if (attachmentSourcePaths == null || attachmentSourcePaths.isEmpty()) {
            logger.debug("[{}] [{}] No attachment paths provided, returning empty list.", mailServiceInstanceId, operationId);
            return Collections.emptyList();
        }
        List<Attachment> loadedAttachments = new ArrayList<>();
        logger.info("[{}] [{}] Starting to load {} attachment(s). Paths: {}", mailServiceInstanceId, operationId, attachmentSourcePaths.size(), attachmentSourcePaths);

        for (String sourcePath : attachmentSourcePaths) {
            if (sourcePath == null || sourcePath.isBlank()){
                logger.warn("[{}] [{}] Null or blank attachment source path encountered, skipping.", mailServiceInstanceId, operationId);
                continue;
            }
            String attachmentFileName = Paths.get(sourcePath).getFileName().toString();
            FileService.FileType attachmentType = determineAttachmentFileType(attachmentFileName);
            logger.debug("[{}] [{}] Processing attachment source: '{}', resolved FileName: '{}', determined FileType: {}",
                    mailServiceInstanceId, operationId, sourcePath, attachmentFileName, attachmentType);

            long singleAttachLoadStartTime = System.nanoTime();
            Optional<byte[]> contentOpt = fileService.getFileAsBytes(attachmentFileName, attachmentType);

            if (contentOpt.isEmpty()) {
                String expectedPath = fileService.getAbsolutePath(attachmentFileName, attachmentType);
                logger.error("[{}] [{}] Attachment file not found: '{}' (from source '{}'). Expected by FileService at: {}",
                        mailServiceInstanceId, operationId, attachmentFileName, sourcePath, expectedPath);
                throw new IOException("Attachment file not found: " + attachmentFileName + " (from source: " + sourcePath + "). Expected at: " + expectedPath);
            }
            byte[] attachmentContent = contentOpt.get();
            logger.debug("[{}] [{}] Attachment content loaded for '{}'. Size: {} bytes. Time: {} ms",
                    mailServiceInstanceId, operationId, attachmentFileName, attachmentContent.length, (System.nanoTime() - singleAttachLoadStartTime) / 1_000_000);

            if (attachmentContent.length > this.maxAttachmentSize) {
                logger.error("[{}] [{}] Attachment '{}' size ({} bytes) exceeds limit of {} bytes.",
                        mailServiceInstanceId, operationId, attachmentFileName, attachmentContent.length, this.maxAttachmentSize);
                throw new IOException("Attachment '" + attachmentFileName + "' size (" + attachmentContent.length
                        + " bytes) exceeds limit of " + this.maxAttachmentSize + " bytes.");
            }
            String mimeType = detectMime(attachmentFileName, operationId + "-AttachMime-" + attachmentFileName);
            loadedAttachments.add(new Attachment(attachmentFileName, attachmentContent, mimeType));
            logger.info("[{}] [{}] Successfully loaded attachment: '{}'. Size: {} bytes, MIME: {}",
                    mailServiceInstanceId, operationId, attachmentFileName, attachmentContent.length, mimeType);
        }
        logger.info("[{}] [{}] All {} attachments loaded in {} ms.",
                mailServiceInstanceId, operationId, loadedAttachments.size(), (System.nanoTime() - loadAllAttachStartTime) / 1_000_000);
        return loadedAttachments;
    }

    private List<List<String>> partition(List<String> list, int size) {
        if (list == null || list.isEmpty() || size <= 0) {
            return Collections.emptyList();
        }
        List<List<String>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return parts;
    }

    private FileService.FileType determineAttachmentFileType(String pathOrFileName) {
        // Logic này không đổi, không cần log nhiều
        String lowerCasePath = pathOrFileName.toLowerCase();
        if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg") ||
                lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".gif") ||
                lowerCasePath.endsWith(".bmp")) {
            return FileService.FileType.IMAGE;
        }
        // HTML file dùng làm attachment là OTHER, không phải TEMPLATE
        return FileService.FileType.OTHER;
    }

    private String detectMime(String pathOrFileName, String logContext) {
        long mimeStartTime = System.nanoTime();
        // Logic này không đổi, chỉ thêm logContext
        String lowerCasePath = pathOrFileName.toLowerCase();
        String mimeType = "application/octet-stream"; // Default

        if (lowerCasePath.endsWith(".pdf")) mimeType = "application/pdf";
        else if (lowerCasePath.endsWith(".docx")) mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else if (lowerCasePath.endsWith(".xlsx")) mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        else if (lowerCasePath.endsWith(".zip")) mimeType = "application/zip";
        else if (lowerCasePath.endsWith(".txt")) mimeType = "text/plain; charset=utf-8";
        else if (lowerCasePath.endsWith(".png")) mimeType = "image/png";
        else if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg")) mimeType = "image/jpeg";
        else if (lowerCasePath.endsWith(".gif")) mimeType = "image/gif";
        else if (lowerCasePath.endsWith(".bmp")) mimeType = "image/bmp";
        else if (lowerCasePath.endsWith(".html") || lowerCasePath.endsWith(".htm")) mimeType = "text/html; charset=utf-8";
        else {
            try {
                String probedMime = Files.probeContentType(Paths.get(pathOrFileName));
                if (probedMime != null) {
                    mimeType = probedMime;
                    logger.trace("[{}] [{}] Probed MIME type for '{}': {}", mailServiceInstanceId, logContext, pathOrFileName, probedMime);
                } else {
                    logger.warn("[{}] [{}] Files.probeContentType returned null for '{}'. Using default.", mailServiceInstanceId, logContext, pathOrFileName);
                }
            } catch (IOException e) {
                logger.warn("[{}] [{}] Could not probe MIME type for filename '{}': {}. Using default.", mailServiceInstanceId, logContext, pathOrFileName, e.getMessage());
            }
        }
        logger.debug("[{}] [{}] Detected MIME type for '{}' as '{}'. Detection time: {} ns", mailServiceInstanceId, logContext, pathOrFileName, mimeType, (System.nanoTime() - mimeStartTime));
        return mimeType;
    }
    private boolean isTransientError(MessagingException ex) {
        // Logic này không đổi, không cần log nhiều vì nó được gọi trong vòng lặp retry đã có log
        if (ex == null) return false;
        String message = (ex.getMessage() != null) ? ex.getMessage().toLowerCase() : "";
        Throwable cause = ex.getCause();
        String causeMessage = (cause != null && cause.getMessage() != null) ? cause.getMessage().toLowerCase() : "";

        if (message.contains("timeout") || message.contains("connection reset") ||
                message.contains("connection refused") || message.contains("service not available") ||
                message.contains("try again later") || message.contains("resources temporarily unavailable") ||
                message.contains("not connected") || message.contains("could not connect to smtp host") ||
                message.contains("connection timed out")) {
            return true;
        }
        if (causeMessage.contains("timeout") || causeMessage.contains("connection reset") || causeMessage.contains("connection refused") || causeMessage.contains("connection timed out")) {
            return true;
        }

        if (message.matches(".*\\b(421|450|451|452)\\b.*")) {
            return true;
        }
        // Cẩn thận với 5xx, chúng thường là permanent. Chỉ coi là transient nếu có dấu hiệu rõ ràng.
        if (message.matches(".*\\b(550|554)\\b.*") && (message.contains("unusual rate") || message.contains("try again") || message.contains("resources unavailable"))) {
            logger.warn("SMTP error code 550/554 encountered, but message indicates it might be transient: {}", message);
            return true;
        }

        if (ex instanceof SendFailedException) {
            Exception next = ((SendFailedException) ex).getNextException();
            return next instanceof java.net.SocketTimeoutException || next instanceof java.net.ConnectException || next instanceof java.io.EOFException;
        }
        return cause instanceof java.net.SocketTimeoutException || cause instanceof java.net.ConnectException || cause instanceof java.io.EOFException;
    }

    private static String maskEmail(String email) {
        if (email == null || email.isEmpty()) return "empty_email_masked";
        int atIndex = email.indexOf('@');
        if (atIndex < 1) return email; // Not a valid structure to mask
        if (atIndex <= 2) return email.charAt(0) + String.join("", Collections.nCopies(atIndex-1, "*")) + "****" + email.substring(atIndex);
        return email.substring(0, 2) + String.join("", Collections.nCopies(atIndex-2, "*")) + "****" + email.substring(atIndex);
    }


    public void shutdown() {
        long shutdownStartTime = System.nanoTime();
        logger.info("[{}] Attempting to shut down MailService executor...", mailServiceInstanceId);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) { // Tăng thời gian chờ một chút
                logger.warn("[{}] Executor did not terminate in 15 seconds, forcing shutdown...", mailServiceInstanceId);
                List<Runnable> droppedTasks = executor.shutdownNow();
                logger.warn("[{}] {} tasks were dropped due to forced shutdown.", mailServiceInstanceId, droppedTasks.size());
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    logger.error("[{}] Executor did not terminate even after forced shutdown.", mailServiceInstanceId);
                } else {
                    logger.info("[{}] Executor terminated after forced shutdown.", mailServiceInstanceId);
                }
            } else {
                logger.info("[{}] Executor terminated gracefully.", mailServiceInstanceId);
            }
        } catch (InterruptedException e) {
            logger.warn("[{}] Shutdown interrupted, forcing executor shutdown now.", mailServiceInstanceId, e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("[{}] MailService shutdown process complete in {} ms.", mailServiceInstanceId, (System.nanoTime() - shutdownStartTime) / 1_000_000);
        // Log thống kê cache khi shutdown
        if (imageResourceCache != null && imageResourceCache.stats() != null) {
            logger.info("[{}] Image Cache Stats on Shutdown: {}", mailServiceInstanceId, imageResourceCache.stats().toString());
        }
    }

    public Map<String, String> buildImageMap(List<String> imageFileNamesToMap, List<String> contentIds) throws IOException {
        // Thêm operationId cho hàm này nếu nó được gọi từ nhiều nơi
        String operationId = "BuildImgMap-" + logSessionIdCounter.incrementAndGet();
        long buildMapStartTime = System.nanoTime();

        if (imageFileNamesToMap == null || contentIds == null || imageFileNamesToMap.size() != contentIds.size()) {
            logger.error("[{}] [{}] Invalid arguments: imageFileNamesToMap (size {}) or contentIds (size {}) are null or sizes differ.",
                    mailServiceInstanceId, operationId,
                    imageFileNamesToMap != null ? imageFileNamesToMap.size() : "null",
                    contentIds != null ? contentIds.size() : "null");
            throw new IllegalArgumentException("Image file names and Content-IDs lists must be non-null and of the same size.");
        }
        if (imageFileNamesToMap.isEmpty()) {
            logger.debug("[{}] [{}] No images provided to build map, returning empty map.", mailServiceInstanceId, operationId);
            return Collections.emptyMap();
        }
        logger.info("[{}] [{}] Building image map for {} image(s).", mailServiceInstanceId, operationId, imageFileNamesToMap.size());

        Map<String, String> imageMap = new HashMap<>();
        for (int i = 0; i < imageFileNamesToMap.size(); i++) {
            String imageFileNameInput = imageFileNamesToMap.get(i);
            String contentId = contentIds.get(i);

            if (imageFileNameInput == null || imageFileNameInput.isBlank() || contentId == null || contentId.isBlank()) {
                logger.error("[{}] [{}] Invalid image file name or Content-ID at index {}. FileName: '{}', ContentID: '{}'",
                        mailServiceInstanceId, operationId, i, imageFileNameInput, contentId);
                throw new IllegalArgumentException("Image file name and Content-ID cannot be null or blank at index " + i +
                        ". Provided FileName: '" + imageFileNameInput + "', ContentID: '" + contentId + "'");
            }
            String imageFileName = Paths.get(imageFileNameInput).getFileName().toString(); // Đảm bảo chỉ lấy tên file

            logger.debug("[{}] [{}] Checking image file: '{}' (from input '{}') with FileType: IMAGE. Mapping to Content-ID: '{}'",
                    mailServiceInstanceId, operationId, imageFileName, imageFileNameInput, contentId);

            Optional<String> absolutePathOpt = fileService.findAbsolutePath(imageFileName, FileService.FileType.IMAGE);
            if (absolutePathOpt.isPresent()) {
                imageMap.put(imageFileName, contentId); // Key là tên file đã được chuẩn hóa (chỉ tên file)
                logger.info("[{}] [{}] Image '{}' found at '{}' and mapped to Content-ID '{}'",
                        mailServiceInstanceId, operationId, imageFileName, absolutePathOpt.get(), contentId);
            } else {
                String expectedLocation = fileService.getAbsolutePath(imageFileName, FileService.FileType.IMAGE);
                logger.error("[{}] [{}] Image for map NOT FOUND: '{}'. FileService expected it at approx: {}",
                        mailServiceInstanceId, operationId, imageFileName, expectedLocation);
                throw new IOException("Image for map not found: " + imageFileName + ". FileService expected it at: " + expectedLocation);
            }
        }
        logger.info("[{}] [{}] Successfully built image map with {} entries in {} ms. Map: {}",
                mailServiceInstanceId, operationId, imageMap.size(), (System.nanoTime() - buildMapStartTime) / 1_000_000, imageMap);
        return imageMap;
    }

    public static class Attachment {
        // ... (Giữ nguyên Attachment class) ...
        public final String fileName;
        public final byte[] content;
        public final String mimeType;

        public Attachment(String fileName, byte[] content, String mimeType) {
            if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("Attachment file name cannot be blank.");
            if (content == null) throw new IllegalArgumentException("Attachment content cannot be null.");
            if (mimeType == null || mimeType.isBlank()) throw new IllegalArgumentException("Attachment MIME type cannot be blank.");

            this.fileName = fileName;
            this.content = content;
            this.mimeType = mimeType;
        }
    }

    public static void main(String[] args) {
        long overallStartTime = System.nanoTime();
        String mainOpId = "MainTest-" + logSessionIdCounter.incrementAndGet();
        logger.info("[{}] [{}] Starting MailService standalone test...", "MailServ-Main", mainOpId);

        ServletContext context = null;
        MailService mailService = null;
        try {
            mailService = new MailService(context); // MailService constructor đã có logging

            String templateName = "welcome_test"; // Đảm bảo file này tồn tại trong [FILE_STORAGE_PATH]/templates/
            mailService.validateTemplateExists(templateName, mainOpId + "-PreValidate");

            // Các file ảnh này phải tồn tại trong [FILE_STORAGE_PATH]/img/
            List<String> imageFileNames = List.of("logo_test.png", "banner_test.jpg");

            // Kiểm tra sự tồn tại của từng ảnh trước khi build map để debug dễ hơn
            for (String imgName : imageFileNames) {
                if (mailService.fileService.findAbsolutePath(imgName, FileService.FileType.IMAGE).isEmpty()) {
                    String expected = mailService.fileService.getAbsolutePath(imgName, FileService.FileType.IMAGE);
                    logger.error("[{}] [{}] PRE-CHECK FAILED: Image '{}' not found at expected location: {}", "MailServ-Main", mainOpId, imgName, expected);
                    throw new FileNotFoundException("Pre-check failed: Image " + imgName + " not found at " + expected);
                }
                logger.info("[{}] [{}] PRE-CHECK PASSED: Image '{}' found.", "MailServ-Main", mainOpId, imgName);
            }


            List<String> imageContentIds = List.of("companyLogoCID", "mainBannerCID");
            Map<String, String> inlineImageMap = mailService.buildImageMap(imageFileNames, imageContentIds);


            // Các file đính kèm này phải tồn tại trong [FILE_STORAGE_PATH]/files/ (hoặc img/ nếu là ảnh)
            List<String> attachmentFileNames = List.of(
                    // "report_test.pdf" // Bỏ comment và đảm bảo file này tồn tại nếu muốn test attachment
            );
            if (!attachmentFileNames.isEmpty()) {
                for (String attName : attachmentFileNames) {
                    FileService.FileType attType = mailService.determineAttachmentFileType(attName);
                    if (mailService.fileService.findAbsolutePath(attName, attType).isEmpty()) {
                        String expected = mailService.fileService.getAbsolutePath(attName, attType);
                        logger.error("[{}] [{}] PRE-CHECK FAILED: Attachment '{}' (type {}) not found at expected location: {}", "MailServ-Main", mainOpId, attName, attType, expected);
                        throw new FileNotFoundException("Pre-check failed: Attachment " + attName + " not found at " + expected);
                    }
                    logger.info("[{}] [{}] PRE-CHECK PASSED: Attachment '{}' (type {}) found.", "MailServ-Main", mainOpId, attName, attType);
                }
            }


            Map<String, Map<String, Object>> variables = new HashMap<>();
            // !!! THAY THẾ BẰNG ĐỊA CHỈ EMAIL THẬT CỦA BẠN ĐỂ NHẬN MAIL TEST !!!
            variables.put("khai1234sd@gmail.com", Map.of(
                    "name", "Khai The Tester",
                    "token", "test_token_khai_789",
                    "verificationLink", mailService.baseUrl + "/verify?token=test_token_khai_789",
                    "companyLogoCID", "companyLogoCID", // Truyền CID vào template vars nếu template dùng ${companyLogoCID}
                    "mainBannerCID", "mainBannerCID",   // Tương tự
                    "items", List.of("Advanced Java", "System Design Principles")
            ));
            variables.put("khainhce182286@fpt.edu.vn", Map.of(
                    "name", "FPT Student Khai",
                    "token", "fpt_token_000",
                    "verificationLink", mailService.baseUrl + "/verify?token=fpt_token_000",
                    "companyLogoCID", "companyLogoCID",
                    "mainBannerCID", "mainBannerCID",
                    "items", List.of("Data Structures", "Algorithms")
            ));

            logger.info("[{}] [{}] Attempting to send test emails. Inline images: {}. Attachments: {}",
                    "MailServ-Main", mainOpId, inlineImageMap, attachmentFileNames.isEmpty() ? "None" : attachmentFileNames);

            Map<String, String> failedRecipients = mailService.sendPersonalizedWithAttachments(
                    templateName,
                    "[TEST] MailService: Welcome to Our Platform!",
                    variables,
                    inlineImageMap,
                    attachmentFileNames,
                    2 // Batch size
            );

            if (failedRecipients.isEmpty()) {
                System.out.println("All test emails were dispatched successfully. Please check your inbox (and spam folder).");
                logger.info("[{}] [{}] All test emails dispatched successfully.", "MailServ-Main", mainOpId);
            } else {
                System.out.println("Some test emails failed to send:");
                failedRecipients.forEach((email, reason) -> {
                    String masked = MailService.maskEmail(email);
                    System.out.println(masked + ": " + reason);
                    logger.warn("[{}] [{}] Email to {} failed: {}", "MailServ-Main", mainOpId, masked, reason);
                });
            }

        } catch (Exception e) {
            System.err.println("ERROR during MailService standalone test run: " + e.getMessage());
            logger.error("[{}] [{}] Main method execution error: ", "MailServ-Main", mainOpId, e); // Log full stack trace
        } finally {
            if (mailService != null) {
                mailService.shutdown();
            }
            logger.info("[{}] [{}] MailService standalone test completed in {} ms.",
                    "MailServ-Main", mainOpId, (System.nanoTime() - overallStartTime) / 1_000_000);
        }
    }
}
