package util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;

public class DBContext {
	private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());
	private static final Properties properties = new Properties();
	protected Connection connection;

	static {
		try (InputStream input = DBContext.class.getResourceAsStream("/database.properties")) {
			if (input == null) {
				LOGGER.severe("database.properties file not found in the classpath.");
			} else {
				properties.load(input);
				String driver = properties.getProperty("DB_DRIVER");
				if (driver == null || driver.isEmpty()) {
					LOGGER.severe("DB_DRIVER property is missing in database.properties.");
				} else {
					Class.forName(driver);
					LOGGER.info("Database driver loaded successfully");
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.severe("Failed to load database configuration: " + e.getMessage());
		}
	}

	public DBContext() {
		String url = properties.getProperty("DB_URL");
		String user = properties.getProperty("DB_USERNAME");
		String password = properties.getProperty("DB_PASSWORD");

		if (url == null || user == null || password == null) {
			LOGGER.severe("Database credentials are missing or incomplete in database.properties.");
			return;
		}

		try {
			connection = DriverManager.getConnection(url, user, password);
			LOGGER.info("Database connection established successfully.");
		} catch (SQLException e) {
			LOGGER.severe("Failed to establish database connection: " + e.getMessage());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
				LOGGER.info("Database connection closed.");
			} catch (SQLException e) {
				LOGGER.severe("Failed to close database connection: " + e.getMessage());
			}
		}
	}
}
