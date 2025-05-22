package util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * The DBContext class is responsible for managing database connections.
 * It loads configuration from an external file and provides method to get a new connection.
 */
public class DBContext {
	protected Connection connection;
	private static final String NAME_CLASS = "DBContext";
	private static final Properties properties = new Properties();

	static {
		try {
			properties.load(DBContext.class.getResourceAsStream("/database.properties"));
			Class.forName(properties.getProperty("DB_DRIVER"));
			Logger.getLogger(NAME_CLASS).info("Database driver loaded successfully.");
		} catch (Exception e) {
			Logger.getLogger(NAME_CLASS).severe("Failed to load database configuration: " + e.getMessage());
		}
	}

	public DBContext() {
		String url = properties.getProperty("DB_URL");
		String user = properties.getProperty("DB_USER");
		String password = properties.getProperty("DB_PASSWORD");
		try {
			connection = DriverManager.getConnection(url, user, password);
			Logger.getLogger(NAME_CLASS).info("Database connection established successfully.");
		} catch (SQLException e) {
			Logger.getLogger(NAME_CLASS).severe("Failed to establish database connection: " + e.getMessage());
		}
	}
}
