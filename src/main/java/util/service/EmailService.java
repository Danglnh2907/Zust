package util.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EmailService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

	private static final String SMTP_USERNAME_ENV = "SMTP_USERNAME";
	private static final String SMTP_PASSWORD_ENV = "SMTP_PASSWORD";
	private static final String SMTP_HOST_ENV = "SMTP_HOST";
	private static final String SMTP_PORT_ENV = "SMTP_PORT";
	private static final String APP_BASE_URL_ENV = "APP_BASE_URL";

	private final FileService fileService;
	private final TemplateEngine templateEngine;
	private final Session mailSession;
	private final String appBaseUrl;
	private final String senderEmail;

	public EmailService(FileService fileService) {
		this.fileService = fileService;
		this.templateEngine = initializeTemplateEngine();
		this.mailSession = initializeMailSession();
		this.appBaseUrl = loadAppBaseUrl();
		this.senderEmail = initializeSenderEmail();
		LOGGER.info("EmailService initialized with sender: {}", senderEmail);
	}

	private TemplateEngine initializeTemplateEngine() {
		TemplateEngine engine = new TemplateEngine();
		FileTemplateResolver resolver = new FileTemplateResolver();
		resolver.setPrefix(fileService.getLocationPath() + "/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCacheable(false);
		engine.setTemplateResolver(resolver);
		return engine;
	}

	private String initializeSenderEmail() {
		Dotenv dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
		String username = dotenv.get(SMTP_USERNAME_ENV);
		if (username == null) {
			username = System.getenv(SMTP_USERNAME_ENV);
		}
		if (username == null || username.isBlank()) {
			LOGGER.error("SMTP_USERNAME is not configured.");
			throw new IllegalStateException("SMTP_USERNAME is missing. Please configure it in save.env.");
		}
		return username;
	}

	private Session initializeMailSession() {
		Dotenv dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();

		String username = dotenv.get(SMTP_USERNAME_ENV);
		String password = dotenv.get(SMTP_PASSWORD_ENV);
		String host = dotenv.get(SMTP_HOST_ENV);
		String port = dotenv.get(SMTP_PORT_ENV);

		if (username == null) username = System.getenv(SMTP_USERNAME_ENV);
		if (password == null) password = System.getenv(SMTP_PASSWORD_ENV);
		if (host == null) host = System.getenv(SMTP_HOST_ENV);
		if (port == null) port = System.getenv(SMTP_PORT_ENV);

		host = (host != null && !host.isBlank()) ? host : "smtp.gmail.com";
		port = (port != null && !port.isBlank()) ? port : "587";

		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			LOGGER.error("SMTP_USERNAME or SMTP_PASSWORD not configured.");
			throw new IllegalStateException("SMTP credentials (username/password) are missing. Please configure them in save.env.");
		}

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.user", username);
		props.put("mail.debug", "false");

		final String finalUsername = username;
		final String finalPassword = password;

		return Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(finalUsername, finalPassword);
			}
		});
	}

	private String loadAppBaseUrl() {
		Dotenv dotenv = Dotenv.configure().filename("save.env").ignoreIfMissing().load();
		String baseUrl = dotenv.get(APP_BASE_URL_ENV);

		if (baseUrl == null) {
			baseUrl = System.getenv(APP_BASE_URL_ENV);
		}

		if (baseUrl == null || baseUrl.isBlank()) {
			LOGGER.warn("APP_BASE_URL environment variable is not set.");
			return "";
		}
		return baseUrl;
	}

	public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables, List<String> imageNames)
			throws IOException, MessagingException {

		if (to == null || to.isBlank()) {
			throw new IllegalArgumentException("Recipient email address cannot be null or empty.");
		}
		if (subject == null || subject.isBlank()) {
			throw new IllegalArgumentException("Email subject cannot be null or empty.");
		}
		if (templateName == null || templateName.isBlank()) {
			throw new IllegalArgumentException("Email template name cannot be null or empty.");
		}

		String actualTemplateName = ensureHtmlExtension(templateName);
		String templateFilePath = fileService.getLocationPath() + "/templates/" + actualTemplateName;
		if (!Files.exists(Paths.get(templateFilePath))) {
			LOGGER.error("Template file not found at expected path: {}", templateFilePath);
			throw new IOException("Email template not found: " + actualTemplateName);
		}

		Context context = new Context();
		context.setVariable("appBaseUrl", appBaseUrl);
		if (variables != null) {
			variables.forEach(context::setVariable);
		}

		String htmlContent;
		try {
			htmlContent = templateEngine.process(actualTemplateName, context);
		} catch (Exception e) {
			LOGGER.error("Failed to process email template '{}': {}", actualTemplateName, e.getMessage(), e);
			throw new IOException("Error processing email template: " + actualTemplateName, e);
		}

		MimeMessage message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(senderEmail));
		InternetAddress[] recipients;
		try {
			String trimmedTo = to.trim();
			if (trimmedTo.isEmpty()) {
				LOGGER.error("Recipient email address is empty after trimming: '{}'", to);
				throw new IllegalArgumentException("Recipient email address cannot be empty after trimming.");
			}
			recipients = InternetAddress.parse(trimmedTo);
		} catch (jakarta.mail.internet.AddressException e) {
			LOGGER.error("Invalid recipient email address format for '{}': {}", to, e.getMessage(), e);
			throw new IllegalArgumentException("Invalid recipient email address format: " + to, e);
		}
		message.setRecipients(Message.RecipientType.TO, recipients);
		message.setSubject(subject);

		MimeMultipart multipart = new MimeMultipart("related");

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
		multipart.addBodyPart(htmlPart);

		if (imageNames != null && !imageNames.isEmpty()) {
			for (String imageName : imageNames) {
				if (imageName == null || imageName.isBlank()) {
					continue;
				}
				String imagePath = fileService.getImagePath(imageName);
				Path imageFile = Paths.get(imagePath);

				if (!Files.exists(imageFile) || !Files.isReadable(imageFile)) {
					LOGGER.warn("Image file not found or not readable: {}. Skipping attachment.", imagePath);
					continue;
				}

				MimeBodyPart imagePart = new MimeBodyPart();
				FileDataSource source = new FileDataSource(imageFile.toFile());
				imagePart.setDataHandler(new DataHandler(source));
				imagePart.setHeader("Content-ID", "<" + imageName + ">");
				imagePart.setDisposition(MimeBodyPart.INLINE);
				multipart.addBodyPart(imagePart);
			}
		}

		message.setContent(multipart);

		try {
			Transport.send(message);
			LOGGER.info("Email sent successfully to: '{}' with subject: '{}'", to, subject);
		} catch (MessagingException e) {
			LOGGER.error("Failed to send email to '{}' with subject '{}': {}", to, subject, e.getMessage(), e);
			throw e;
		}
	}

	private String ensureHtmlExtension(String templateName) {
		return templateName.toLowerCase().endsWith(".html") ? templateName : templateName + ".html";
	}
}
