package util.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private static final long MAX_FILE_SIZE = 10485760; // 10MB
    private static final String DEFAULT_STORAGE_PATH = "D:\\UniAcad\\UniAcad\\src\\main\\resources";

    private final Path storageRoot;

    public enum FileType {
        EXCEL("xlsx"),
        WORD("docx"),
        PDF("pdf"),
        IMAGE("img"),
        TEMPLATE("templates");

        private final String folder;

        FileType(String folder) {
            this.folder = folder;
        }

        public String getFolder() {
            return folder;
        }
    }

    public FileService(ServletContext context) {
        if (context != null) {
            String contextPath = context.getRealPath("/");
            if (contextPath != null) {
                this.storageRoot = Paths.get(contextPath).getParent().resolve("storage");
                logger.info("FileService initialized with ServletContext: {}", storageRoot);
                return;
            }
        }
        String pathFromEnv = loadFromEnv("save.env");
        if (pathFromEnv != null && !pathFromEnv.isEmpty()) {
            this.storageRoot = Paths.get(pathFromEnv);
        } else {
            this.storageRoot = Paths.get(DEFAULT_STORAGE_PATH);
        }
        logger.info("FileService initialized with fallback path: {}", storageRoot);
    }

    public FileService(String pathInput) {
        final var storageRoot1 = Paths.get(DEFAULT_STORAGE_PATH);
        if (pathInput == null || pathInput.trim().isEmpty()) {
            String pathFromEnv = loadFromEnv("save.env");
            if (pathFromEnv != null && !pathFromEnv.isEmpty()) {
                this.storageRoot = Paths.get(pathFromEnv);
            } else {
                this.storageRoot = storageRoot1;
            }
        } else if (pathInput.toLowerCase().endsWith(".env")) {
            String pathFromEnv = loadFromEnv(pathInput);
            if (pathFromEnv != null && !pathFromEnv.isEmpty()) {
                this.storageRoot = Paths.get(pathFromEnv);
            } else {
                this.storageRoot = storageRoot1;
            }
        } else {
            this.storageRoot = Paths.get(pathInput);
        }
        logger.info("FileService initialized with path: {}", storageRoot);
    }

    private String loadFromEnv(String envFile) {
        try {
            Dotenv dotenv = Dotenv.configure().filename(envFile).ignoreIfMissing().load();
            String path = dotenv.get("FILE_STORAGE_PATH");
            if (path != null && !path.trim().isEmpty()) {
                return path;
            } else {
                logger.warn("FILE_STORAGE_PATH missing in env file: {}", envFile);
            }
        } catch (Exception e) {
            logger.warn("Could not load env file {}, continuing with default.", envFile);
        }
        return null;
    }

    public boolean saveFile(String fileName, byte[] content, FileType type, boolean overwrite) {
        try {
            validateFileName(fileName);
            validateFileSize(content);
            validateFileContent(fileName, content, type);

            Path target = resolveTargetPath(fileName, type, overwrite);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            logger.info("File saved: {}", target);
            return true;
        } catch (Exception e) {
            logger.error("Failed to save file {}: {}", fileName, e.getMessage());
            return false;
        }
    }

    public boolean saveFile(String fileName, InputStream inputStream, FileType type, boolean overwrite) {
        try {
            validateFileName(fileName);
            byte[] content = inputStream.readAllBytes();
            validateFileSize(content);
            validateFileContent(fileName, content, type);

            Path target = resolveTargetPath(fileName, type, overwrite);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            logger.info("File saved from InputStream: {}", target);
            return true;
        } catch (Exception e) {
            logger.error("Failed to save file from InputStream {}: {}", fileName, e.getMessage());
            return false;
        }
    }

    public InputStream getFileInputStream(String fileName, FileType type) throws IOException {
        Path path = getPath(fileName, type);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + path);
        }
        return Files.newInputStream(path);
    }

    public boolean deleteFile(String fileName, FileType type) {
        try {
            Path path = getPath(fileName, type);
            if (Files.exists(path)) {
                Files.delete(path);
                logger.info("Deleted file: {}", path);
                return true;
            } else {
                logger.warn("File not found for deletion: {}", path);
                return false;
            }
        } catch (IOException e) {
            logger.error("Error deleting file {}: {}", fileName, e.getMessage());
            return false;
        }
    }

    public String getAbsolutePath(String fileName, FileType type) {
        return getPath(fileName, type).toAbsolutePath().toString();
    }

    public Optional<String> findAbsolutePath(String fileName, FileType type) {
        Path path = getPath(fileName, type);
        return Files.exists(path) ? Optional.of(path.toAbsolutePath().toString()) : Optional.empty();
    }

    private Path getPath(String fileName, FileType type) {
        return storageRoot.resolve(type.getFolder()).resolve(fileName);
    }

    private Path resolveTargetPath(String fileName, FileType type, boolean overwrite) {
        Path base = getPath(fileName, type);

        if (overwrite || !Files.exists(base)) {
            return base;
        }

        String name = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
        int counter = 1;

        while (Files.exists(base)) {
            String newName = String.format("%s(%d).%s", name, counter++, ext);
            base = getPath(newName, type);
        }
        return base;
    }

    private void validateFileName(String fileName) {
        if (fileName.contains("..") || fileName.matches(".*[\\/\\\\%:|?*].*")) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
    }

    private void validateFileSize(byte[] content) {
        if (content.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds allowed maximum");
        }
    }

    private void validateFileContent(String fileName, byte[] content, FileType type) throws IOException {
        String lowerCaseName = fileName.toLowerCase();
        if (type == FileType.PDF) {
            if (content.length < 4 || !new String(content, 0, 4, StandardCharsets.UTF_8).startsWith("%PDF")) {
                throw new IOException("Invalid PDF file: " + fileName);
            }
        } else if (type == FileType.WORD || type == FileType.EXCEL) {
            if (content.length < 2 || content[0] != 0x50 || content[1] != 0x4B) {
                throw new IOException("Invalid Office file: " + fileName);
            }
        } else if (type == FileType.IMAGE) {
            if (lowerCaseName.endsWith(".gif")) {
                if (content.length < 6 || (!new String(content, 0, 6, StandardCharsets.US_ASCII).equals("GIF87a") &&
                        !new String(content, 0, 6, StandardCharsets.US_ASCII).equals("GIF89a"))) {
                    throw new IOException("Invalid GIF file: " + fileName);
                }
            } else if (lowerCaseName.matches(".*\\.(png|jpg|jpeg)$")) {
                try {
                    ImageIO.read(new ByteArrayInputStream(content));
                } catch (Exception e) {
                    throw new IOException("Invalid image file: " + fileName, e);
                }
            } else {
                throw new IOException("Unsupported image format: " + fileName);
            }
        } else if (type == FileType.TEMPLATE && !lowerCaseName.endsWith(".html")) {
            logger.error("Template should be HTML {}", fileName);
            throw new IOException("Template should be HTML: " + fileName);
        }
    }
}
