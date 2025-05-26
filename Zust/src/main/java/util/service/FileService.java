package util.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletContext; // Giữ lại nếu bạn có lúc dùng ServletContext
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO; // Cần cho validateImageContent
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    // Giới hạn kích thước này CHỈ áp dụng khi đọc toàn bộ file vào byte[] (ví dụ khi validate content)
    // Nó KHÔNG áp dụng cho việc stream file lớn qua saveFile(InputStream,...)
    private static final long DEFAULT_MAX_FILE_SIZE_FOR_VALIDATION = 5 * 1024 * 1024; // 5MB cho validate content
    private static final String DEFAULT_STORAGE_PATH = "./app_file_storage"; // Đường dẫn mặc định tương đối
    private static final String DEFAULT_ENV_FILENAME = "save.env"; // Tên file .env mặc định FileService sẽ tìm
    private static final String EXE_EXTENSION = ".exe";

    private final Path storageRoot;
    private final long maxFileSizeForValidation;


    public enum FileType {
        IMAGE("img"),
        VIDEO("video"), // Giữ lại phòng khi cần
        OTHER("files"), // Cho attachments như PDF, DOCX, XLSX
        TEMPLATE("templates");

        private final String folder;

        FileType(String folder) {
            this.folder = folder;
        }

        public String getFolder() {
            return folder;
        }
    }

    /**
     * Constructor mặc định.
     * Sử dụng file .env có tên được định nghĩa bởi DEFAULT_ENV_FILENAME để tải cấu hình.
     */
    public FileService() {
        this((String) null, null); // Gọi constructor chính với ServletContext là null
    }

    /**
     * Constructor với ServletContext (nếu chạy trong môi trường web).
     * @param context ServletContext để suy ra đường dẫn lưu trữ.
     */
    public FileService(ServletContext context) {
        this(null, context); // Gọi constructor chính
    }

    /**
     * Constructor cho phép chỉ định đường dẫn tuyệt đối đến thư mục lưu trữ
     * hoặc tên file .env cụ thể chứa cấu hình FILE_STORAGE_PATH.
     * @param pathOrSpecificEnvFile Đường dẫn thư mục hoặc tên file .env.
     */
    public FileService(String pathOrSpecificEnvFile) {
        this(pathOrSpecificEnvFile, null); // Gọi constructor chính với ServletContext là null
    }

    /**
     * Constructor chính, xử lý logic khởi tạo.
     * Ưu tiên:
     * 1. pathOrSpecificEnvFile nếu là đường dẫn trực tiếp.
     * 2. ServletContext để suy ra đường dẫn (nếu context khác null).
     * 3. pathOrSpecificEnvFile nếu là tên file .env.
     * 4. DEFAULT_ENV_FILENAME để lấy FILE_STORAGE_PATH.
     * 5. DEFAULT_STORAGE_PATH nếu tất cả các cách trên không thành công.
     */
    private FileService(String pathOrSpecificEnvFile, ServletContext context) {
        String envFileToLoadConfigFrom = (pathOrSpecificEnvFile != null && pathOrSpecificEnvFile.toLowerCase().endsWith(".env"))
                ? pathOrSpecificEnvFile
                : DEFAULT_ENV_FILENAME;

        Dotenv dotenv = Dotenv.configure().filename(envFileToLoadConfigFrom).ignoreIfMissing().load();
        this.maxFileSizeForValidation = Long.parseLong(dotenv.get("FILE_MAX_VALIDATE_SIZE_BYTES", String.valueOf(DEFAULT_MAX_FILE_SIZE_FOR_VALIDATION)));

        Path resolvedStoragePath = null;

        if (pathOrSpecificEnvFile != null && !pathOrSpecificEnvFile.isBlank() && !pathOrSpecificEnvFile.toLowerCase().endsWith(".env")) {
            // 1. Ưu tiên đường dẫn trực tiếp
            resolvedStoragePath = Paths.get(pathOrSpecificEnvFile);
            logger.debug("Attempting to use direct path for storage: {}", resolvedStoragePath);
        } else if (context != null) {
            // 2. Ưu tiên ServletContext nếu có
            String realPath = context.getRealPath("/");
            if (realPath != null) {
                Path parent = Paths.get(realPath).getParent(); // Thường là thư mục webapps hoặc tương đương
                if (parent != null) {
                    resolvedStoragePath = parent.resolve("app_storage_data"); // Đặt tên thư mục cụ thể
                    logger.debug("Attempting to use ServletContext-resolved path: {}", resolvedStoragePath);
                }
            }
        }

        if (resolvedStoragePath == null) {
            // 3 & 4. Sử dụng file .env (envFileToLoadConfigFrom) để lấy FILE_STORAGE_PATH
            String pathFromEnv = dotenv.get("FILE_STORAGE_PATH");
            if (pathFromEnv != null && !pathFromEnv.isBlank()) {
                resolvedStoragePath = Paths.get(pathFromEnv);
                logger.debug("Using FILE_STORAGE_PATH from '{}': {}", envFileToLoadConfigFrom, resolvedStoragePath);
            } else {
                // 5. Dùng default path
                resolvedStoragePath = Paths.get(DEFAULT_STORAGE_PATH);
                logger.warn("FILE_STORAGE_PATH not found or empty in '{}'. Using default storage path: {}", envFileToLoadConfigFrom, resolvedStoragePath);
            }
        }

        this.storageRoot = resolvedStoragePath.toAbsolutePath();
        try {
            Files.createDirectories(this.storageRoot);
            // Tạo các thư mục con mặc định nếu chưa có
            for (FileType ft : FileType.values()) {
                Files.createDirectories(this.storageRoot.resolve(ft.getFolder()));
            }
        } catch (IOException e) {
            logger.error("CRITICAL: Could not create storage root directory or subdirectories at: {}. Please check permissions and path.", this.storageRoot, e);
            throw new RuntimeException("Failed to initialize FileService storage root at " + this.storageRoot, e);
        }
        logger.info("FileService initialized. Storage root is set to: {}", this.storageRoot);
    }


    public boolean saveFile(String fileName, byte[] content, FileType type, boolean overwrite) {
        try {
            validateFileIsSafe(fileName, type);
            validateFileSizeForByteArray(content); // Chỉ validate size nếu là byte array

            Path target = resolveTargetPath(fileName, type, overwrite); // resolveTargetPath sẽ tạo thư mục category nếu chưa có
            Files.write(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); // Đảm bảo ghi đè nếu overwrite
            logger.info("File saved (from byte[]): {} ({} bytes)", target, content.length);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Failed to save file '{}' (from byte[]), type '{}': {}", fileName, type, e.getMessage(), e);
            return false;
        }
    }

    public boolean saveFile(String fileName, InputStream inputStream, FileType type, boolean overwrite) {
        Path target = null;
        try {
            validateFileIsSafe(fileName, type);
            target = resolveTargetPath(fileName, type, overwrite); // resolveTargetPath sẽ tạo thư mục category nếu chưa có

            try (OutputStream outputStream = Files.newOutputStream(target,
                    StandardOpenOption.CREATE, // Luôn tạo mới nếu chưa có
                    overwrite ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.WRITE // TRUNCATE nếu overwrite, nếu không thì chỉ WRITE (sẽ lỗi nếu CREATE_NEW và file tồn tại)
                    // resolveTargetPath đã xử lý logic tên file nếu !overwrite và file tồn tại
            )) {
                long bytesCopied = inputStream.transferTo(outputStream); // Java 9+
                logger.info("File saved (from InputStream): {}. Size: {} bytes", target, bytesCopied);
            }
            return true;
        } catch (FileAlreadyExistsException e) {
            logger.warn("Attempted to save file '{}' (type '{}') but it already exists and overwrite is false. Path: {}", fileName, type, e.getFile());
            return false;
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Failed to save file '{}' (from InputStream), type '{}': {}. Path attempted: {}", fileName, type, e.getMessage(), target, e);
            if (target != null) {
                try { Files.deleteIfExists(target); } catch (IOException ignored) {
                    logger.warn("Could not delete partially written file at {}", target);
                }
            }
            return false;
        }
    }

    public InputStream getFileInputStream(String fileName, FileType type) throws IOException {
        Path path = getPath(fileName, type); // getPath sẽ sanitize fileName
        logger.debug("[getFileInputStream] Attempting to get InputStream for: '{}' in type '{}'. Resolved path: {}", fileName, type, path);
        if (!Files.exists(path) || Files.isDirectory(path)) {
            logger.warn("[getFileInputStream] File not found or is a directory at: {}", path);
            throw new FileNotFoundException("File not found or is a directory: " + path);
        }
        return Files.newInputStream(path);
    }

    public Optional<byte[]> getFileAsBytes(String fileName, FileType type) {
        Path path = null;
        try {
            path = getPath(fileName, type); // getPath sẽ sanitize fileName
            logger.debug("[getFileAsBytes] Attempting to read file as bytes: '{}' in type '{}'. Resolved path: {}", fileName, type, path);
            if (!Files.exists(path) || Files.isDirectory(path)) {
                logger.warn("[getFileAsBytes] File not found or is a directory at: {}", path);
                return Optional.empty();
            }
            byte[] content = Files.readAllBytes(path);
            // validateFileSizeForByteArray(content); // Kiểm tra lại kích thước ở đây là tùy chọn
            logger.debug("[getFileAsBytes] Successfully read file {} as bytes ({} bytes)", path, content.length);
            return Optional.of(content);
        } catch (FileNotFoundException e) { // Có thể không cần vì Files.exists đã check
            logger.warn("[getFileAsBytes] File not found for '{}' in type '{}' (path: {}): {}", fileName, type, path, e.getMessage());
            return Optional.empty();
        } catch (IOException | IllegalArgumentException e) {
            logger.error("[getFileAsBytes] Error reading file '{}' in type '{}' as bytes (path: {}): {}", fileName, type, path, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<String> getFileAsText(String fileName, FileType type) {
        logger.debug("[getFileAsText] Attempting to read file as text: '{}' in type '{}'", fileName, type);
        return getFileAsBytes(fileName, type)
                .map(bytes -> {
                    String text = new String(bytes, StandardCharsets.UTF_8);
                    logger.debug("[getFileAsText] Successfully read file for '{}', type '{}' as text.", fileName, type);
                    return text;
                });
    }

    public boolean deleteFile(String fileName, FileType type) {
        Path path = getPath(fileName, type);
        logger.debug("[deleteFile] Attempting to delete file: '{}' in type '{}'. Resolved path: {}", fileName, type, path);
        try {
            if (Files.exists(path) && !Files.isDirectory(path)) {
                Files.delete(path);
                logger.info("[deleteFile] Deleted file: {}", path);
                return true;
            }
            logger.warn("[deleteFile] File not found for deletion or is a directory: {}", path);
            return false;
        } catch (IOException e) {
            logger.error("[deleteFile] Error deleting file '{}': {}", path, e.getMessage(), e);
            return false;
        }
    }

    public String getAbsolutePath(String fileName, FileType type) {
        // Xử lý trường hợp fileName rỗng hoặc null khi type được cung cấp (để lấy đường dẫn thư mục category)
        if (type != null && (fileName == null || fileName.isEmpty())) {
            Path categoryPath = this.storageRoot.resolve(type.getFolder());
            // Đảm bảo thư mục tồn tại khi lấy đường dẫn đến nó
            try {
                Files.createDirectories(categoryPath);
            } catch (IOException e) {
                logger.error("Could not create directory for FileType '{}' at {}. Error: {}", type, categoryPath, e.getMessage());
                // Có thể ném một RuntimeException ở đây nếu thư mục không thể tạo
                throw new RuntimeException("Failed to ensure directory for FileType " + type, e);
            }
            return categoryPath.toAbsolutePath().toString();
        }
        // Xử lý trường hợp lấy storageRoot (khi cả fileName và type đều không có)
        if ((fileName == null || fileName.isEmpty()) && type == null) {
            return this.storageRoot.toAbsolutePath().toString();
        }
        // Trường hợp thông thường: lấy đường dẫn file cụ thể
        return getPath(fileName, type).toAbsolutePath().toString();
    }

    public Optional<String> findAbsolutePath(String fileName, FileType type) {
        Path path = getPath(fileName, type);
        logger.trace("[findAbsolutePath] Checking existence for: '{}' in type '{}'. Resolved path: {}", fileName, type, path);
        return (Files.exists(path) && !Files.isDirectory(path)) ? Optional.of(path.toAbsolutePath().toString()) : Optional.empty();
    }

    private Path getPath(String fileName, FileType type) {
        if (type == null) {
            // Điều này có thể xảy ra nếu getAbsolutePath("", null) được gọi,
            // nhưng logic của getAbsolutePath đã xử lý trường hợp đó để trả về storageRoot.
            // Nếu đến đây mà type vẫn null thì là lỗi logic.
            throw new IllegalArgumentException("FileType cannot be null when resolving a specific file path component.");
        }

        String sanitizedFileName = (fileName != null) ? sanitizeFileName(fileName) : "";

        Path categoryDir = storageRoot.resolve(type.getFolder());
        if (sanitizedFileName.isEmpty()) {
            return categoryDir; // Trả về đường dẫn đến thư mục category nếu không có tên file
        }
        return categoryDir.resolve(sanitizedFileName);
    }

    private Path resolveTargetPath(String fileNameToSave, FileType type, boolean overwrite) throws IOException {
        Path baseDir = storageRoot.resolve(type.getFolder());
        Files.createDirectories(baseDir); // Đảm bảo thư mục category tồn tại

        String sanitizedOriginalName = sanitizeFileName(fileNameToSave);
        Path targetPath = baseDir.resolve(sanitizedOriginalName);

        if (overwrite || !Files.exists(targetPath)) {
            return targetPath;
        }

        // Nếu !overwrite và file đã tồn tại, tạo tên mới
        String namePart = sanitizedOriginalName.contains(".") ? sanitizedOriginalName.substring(0, sanitizedOriginalName.lastIndexOf('.')) : sanitizedOriginalName;
        String extensionPart = sanitizedOriginalName.contains(".") ? sanitizedOriginalName.substring(sanitizedOriginalName.lastIndexOf('.')) : "";
        int counter = 1;
        Path candidatePath;
        String newName;
        do {
            newName = String.format("%s(%d)%s", namePart, counter++, extensionPart);
            candidatePath = baseDir.resolve(newName); // Tên mới đã được tạo từ tên đã sanitize, không cần sanitize lại
        } while (Files.exists(candidatePath));

        logger.info("File '{}' already exists. Saving as '{}' in type '{}' directory.", sanitizedOriginalName, newName, type);
        return candidatePath;
    }

    private void validateFileIsSafe(String fileName, FileType type) throws IllegalArgumentException {
        if (fileName == null || fileName.isBlank()) { // Chỉ kiểm tra null/blank ở đây
            throw new IllegalArgumentException("File name for saving cannot be null or blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("FileType for saving cannot be null.");
        }

        String sanitizedName = sanitizeFileName(fileName); // Dùng tên đã sanitize để validate
        if (sanitizedName.contains("..")) { // Double check, dù sanitizeFileName nên đã loại bỏ
            throw new IllegalArgumentException("Invalid characters (directory traversal attempt) in file name: " + fileName);
        }
        if (sanitizedName.matches(".*[<>:\"/\\\\|?*\\x00-\\x1F].*")) { // Check lại các ký tự nguy hiểm
            throw new IllegalArgumentException("Invalid characters in file name after sanitization: " + sanitizedName + " (from original: " + fileName + ")");
        }
        if (sanitizedName.toLowerCase().endsWith(EXE_EXTENSION)) {
            throw new IllegalArgumentException("Executable files (.exe) are not allowed: " + fileName);
        }
    }

    private String sanitizeFileName(String fileNameInput) {
        if (fileNameInput == null || fileNameInput.isBlank()) {
            return ""; // Trả về rỗng nếu input là null hoặc rỗng
        }
        // 1. Loại bỏ ".." để chống directory traversal
        String sanitized = fileNameInput.replace("..", "");

        // 2. Thay thế các ký tự không hợp lệ bằng "_"
        // Danh sách ký tự không hợp lệ phổ biến trên Windows và Unix-like systems
        // Bao gồm cả ký tự control (0-31), < > : " / \ | ? *
        sanitized = sanitized.replaceAll("[\\x00-\\x1F<>:\"/\\\\|?*]", "_");

        // 3. Loại bỏ khoảng trắng thừa ở đầu và cuối
        sanitized = sanitized.trim();

        // 4. Giới hạn độ dài tên file (ví dụ 200 ký tự)
        int maxNameLength = 200;
        if (sanitized.length() > maxNameLength) {
            // Lấy phần mở rộng (nếu có)
            String ext = "";
            int dotIndex = sanitized.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < sanitized.length() - 1) { // Có dấu chấm và có ký tự sau nó
                ext = sanitized.substring(dotIndex);
                sanitized = sanitized.substring(0, dotIndex);
            }
            // Cắt ngắn phần tên, đảm bảo đủ chỗ cho phần mở rộng và dấu chấm
            int availableLengthForName = maxNameLength - ext.length();
            if (sanitized.length() > availableLengthForName) {
                sanitized = sanitized.substring(0, availableLengthForName);
            }
            sanitized = sanitized + ext; // Gắn lại phần mở rộng
            logger.trace("Sanitized file name '{}' (from '{}') was truncated due to max length ({})", sanitized, fileNameInput, maxNameLength);
        }

        // 5. Nếu sau khi sanitize, tên file trở nên rỗng (ví dụ: tên gốc là toàn ký tự không hợp lệ)
        // thì tạo một tên ngẫu nhiên để tránh lỗi.
        if (sanitized.isEmpty() || "_".equals(sanitized) || ".".equals(sanitized)) {
            String originalExt = "";
            int dotIdx = fileNameInput.lastIndexOf('.');
            if (dotIdx > 0 && dotIdx < fileNameInput.length() - 1) {
                originalExt = fileNameInput.substring(dotIdx); // Giữ lại đuôi file gốc nếu có
            }
            sanitized = "sanitized_file_" + UUID.randomUUID().toString().substring(0, 8) + originalExt;
            logger.warn("Original file name '{}' became invalid after sanitization. Renamed to '{}'", fileNameInput, sanitized);
        }
        return sanitized;
    }

    private void validateFileSizeForByteArray(byte[] content) throws IllegalArgumentException {
        if (content.length > this.maxFileSizeForValidation) {
            throw new IllegalArgumentException("File content size (" + content.length + " bytes) exceeds validation limit of " + this.maxFileSizeForValidation + " bytes");
        }
    }

    // Validate nội dung file (chỉ nên gọi nếu bạn đã có content là byte[] và file không quá lớn)
    @SuppressWarnings("unused") // Đánh dấu là có thể không dùng đến để tránh warning
    private void validateFileContent(String fileName, byte[] content, FileType type) throws IOException {
        if (fileName == null || content == null || type == null) return; // Bỏ qua nếu thiếu thông tin

        validateFileSizeForByteArray(content); // Kiểm tra kích thước trước

        switch (type) {
            case IMAGE:
                validateImageContentInternal(fileName, content);
                break;
            case TEMPLATE:
                validateTemplateContentInternal(fileName, content);
                break;
            case VIDEO: // Bỏ qua validate video content phức tạp
            case OTHER: // Không có validate cụ thể cho OTHER
                break;
        }
    }

    private void validateImageContentInternal(String fileName, byte[] content) throws IOException {
        String lowerFileName = fileName.toLowerCase();
        // Chỉ validate các định dạng phổ biến
        if (lowerFileName.endsWith(".png") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".gif") || lowerFileName.endsWith(".bmp")) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(content)) {
                if (ImageIO.read(bis) == null) {
                    throw new IOException("Invalid or unsupported image file format (ImageIO.read failed): " + fileName);
                }
            } catch (Exception e) { // ImageIO.read có thể ném nhiều loại Exception
                throw new IOException("Error validating image content for " + fileName + ": " + e.getMessage(), e);
            }
        } else {
            logger.trace("Skipping image content validation for unsupported extension: {}", fileName);
        }
    }

    private void validateTemplateContentInternal(String fileName, byte[] content) throws IOException {
        if (!fileName.toLowerCase().endsWith(".html")) {
            throw new IOException("Template file must have .html extension: " + fileName);
        }
        if (content.length == 0) {
            throw new IOException("Template file is empty: " + fileName);
        }
        String html = new String(content, StandardCharsets.UTF_8).trim().toLowerCase();
        if (!html.startsWith("<html") && !html.startsWith("<!doctype html")) { // Chấp nhận cả doctype
            throw new IOException("Invalid HTML content (missing <html> or <!doctype html> tag) in template file: " + fileName);
        }
    }
}
