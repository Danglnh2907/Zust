package util.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

public class FileService {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
	private static final String VIDEO_PATH = "/videos";
	private static final String IMAGE_PATH = "/images";
	private static final String OTHER_PATH = "/others";
	private static final String TEMPLATE_PATH = "/templates";
	private static final List<String> FORBIDDEN_EXTENSIONS = List.of(".exe", ".bat", ".js", ".dll");
	private static final List<String> IMAGE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");
	private static final List<String> VIDEO_EXTENSIONS = List.of(".mp4", ".avi", ".mkv", ".mov");
	private static final String HTML_EXTENSION = ".html";
	private static final String DEFAULT_PATH = "files/app_stores";
	private static final String FILE_STORAGE_PATH_ENV = "FILE_STORAGE_PATH";
	private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[\\p{Cntrl}\\\\/:*?\"<>|]");
	private static final Pattern DANGEROUS_HTML_PATTERN = Pattern.compile("(?i)<\\s*script\\b[^>]*>");

	private final String locationPath;

	public FileService(ServletContext context) {
		String path = extractContextPath(context);
		this.locationPath = initializePath(path);
		LOGGER.info("FileService initialized with path: {}", locationPath);
	}

	public FileService() {
		this.locationPath = initializePath(null);
		LOGGER.info("FileService initialized with default or env path: {}", locationPath);
	}

	private String extractContextPath(ServletContext context) {
		if (context == null) {
			return null;
		}
		Object ctxPath = context.getAttribute("contentPath");
		return (ctxPath instanceof String && !((String) ctxPath).isBlank()) ? (String) ctxPath : null;
	}

	private String initializePath(String contextPath) {
		if (contextPath != null && isValidPath(contextPath)) {
			return normalizePath(contextPath);
		}

		String envPath = loadPathFromEnv();
		if (envPath != null && isValidPath(envPath)) {
			return normalizePath(envPath);
		}

		if (isValidPath(DEFAULT_PATH)) {
			return normalizePath(DEFAULT_PATH);
		}

		String tempDir = System.getProperty("java.io.tmpdir") + "/app_stores";
		LOGGER.warn("Falling back to system temp directory: {}", tempDir);
		return normalizePath(tempDir);
	}

	public String loadPathFromEnv() {
		try {
			Dotenv dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
			String envPath = dotenv.get(FILE_STORAGE_PATH_ENV);
			return (envPath != null && !envPath.isBlank()) ? envPath : null;
		} catch (Exception e) {
			LOGGER.warn("Failed to load environment variable {} from save.env: {}", FILE_STORAGE_PATH_ENV, e.getMessage());
			return null;
		}
	}

	private boolean isValidPath(String path) {
		if (path == null || path.isBlank()) {
			return false;
		}
		try {
			Path filePath = Paths.get(path);
			if (!Files.exists(filePath)) {
				Files.createDirectories(filePath);
			}
			return Files.isDirectory(filePath) && Files.isWritable(filePath);
		} catch (IOException e) {
			LOGGER.error("Invalid path {}: {}", path, e.getMessage());
			return false;
		}
	}

	private String normalizePath(String path) {
		return Paths.get(path).normalize().toString().replace("\\", "/");
	}

	public String getLocationPath() {
		return locationPath;
	}

	public String getImagePath(String imageName) {
		validateFileName(imageName);
		return normalizePath(locationPath + IMAGE_PATH + "/" + imageName);
	}

	public String getVideoPath(String videoName) {
		validateFileName(videoName);
		return normalizePath(locationPath + VIDEO_PATH + "/" + videoName);
	}

	public String saveFile(String fileName, InputStream fileContent) throws IOException {
		validateFileName(fileName);
		String subDir = determineSubDirectory(fileName);
		Path filePath = prepareAndSaveFile(fileName, fileContent, subDir);
		LOGGER.info("File saved successfully: {}", filePath);
		return normalizePath(filePath.toString());
	}

	public String saveTemplate(String templateName, String htmlContent) throws IOException {
		validateTemplateName(templateName);
		validateHtmlContent(htmlContent);
		String fileName = ensureHtmlExtension(templateName);
		Path filePath = prepareAndSaveFile(fileName, htmlContent, TEMPLATE_PATH);
		LOGGER.info("Template saved successfully: {}", filePath);
		return normalizePath(filePath.toString());
	}

	private Path prepareAndSaveFile(String fileName, Object content, String subDir) throws IOException {
		Path targetDir = Paths.get(locationPath + subDir);
		if (!Files.exists(targetDir)) {
			Files.createDirectories(targetDir);
		}

		Path filePath = Paths.get(locationPath + subDir, fileName).normalize();
		if (!filePath.startsWith(Paths.get(locationPath))) {
			throw new SecurityException("Invalid path: Attempted path traversal");
		}

		try {
			if (content instanceof InputStream input) {
				try (input) {
					Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
				}
			} else if (content instanceof String stringContent) {
				Files.writeString(filePath, stringContent, StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} else {
				throw new IllegalArgumentException("Unsupported content type");
			}
			return filePath;
		} catch (IOException e) {
			LOGGER.error("Failed to save file {}: {}", filePath, e.getMessage());
			throw e;
		}
	}

	private String determineSubDirectory(String fileName) {
		String lowerCaseFileName = fileName.toLowerCase();
		if (IMAGE_EXTENSIONS.stream().anyMatch(lowerCaseFileName::endsWith)) {
			return IMAGE_PATH;
		} else if (VIDEO_EXTENSIONS.stream().anyMatch(lowerCaseFileName::endsWith)) {
			return VIDEO_PATH;
		} else {
			return OTHER_PATH;
		}
	}

	private void validateResourceName(String name, String resourceType) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException(String.format("%s name cannot be null or empty", resourceType));
		}
		if (INVALID_FILENAME_PATTERN.matcher(name).find()) {
			throw new IllegalArgumentException(String.format("%s name contains invalid characters", resourceType));
		}
		String lowerCaseName = name.toLowerCase();
		for (String ext : FORBIDDEN_EXTENSIONS) {
			if (lowerCaseName.endsWith(ext)) {
				throw new IllegalArgumentException(String.format("%s extension %s is not allowed", resourceType, ext));
			}
		}
	}

	private void validateFileName(String fileName) {
		validateResourceName(fileName, "File");
	}

	private void validateTemplateName(String templateName) {
		validateResourceName(templateName, "Template");
	}

	private void validateHtmlContent(String htmlContent) {
		if (htmlContent == null || htmlContent.isBlank()) {
			throw new IllegalArgumentException("HTML content cannot be null or empty");
		}
		String trimmedContent = htmlContent.trim().toLowerCase();
		if (!trimmedContent.contains("<html") || !trimmedContent.contains("<body")) {
			throw new IllegalArgumentException("Invalid HTML content: Missing required <html> or <body> tags");
		}
		if (DANGEROUS_HTML_PATTERN.matcher(htmlContent).find()) {
			throw new IllegalArgumentException("Invalid HTML content: Contains potentially dangerous <script> tags");
		}
	}

	private String ensureHtmlExtension(String templateName) {
		return templateName.toLowerCase().endsWith(HTML_EXTENSION) ? templateName : templateName + HTML_EXTENSION;
	}
}
