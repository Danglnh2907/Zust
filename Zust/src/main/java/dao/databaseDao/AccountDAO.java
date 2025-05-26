package dao.databaseDao;

import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.database.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AccountDAO extends DBContext{

	private static final Logger logger = LoggerFactory.getLogger(AccountDAO.class);

	public Optional<Account> findByEmail(String email) {
		String sql = "SELECT account_id, username, account_fullname, account_email, account_status FROM account WHERE account_email = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Account account = new Account();
					account.setAccountId(rs.getInt("account_id"));
					account.setUsername(rs.getString("username"));
					account.setAccountFullname(rs.getString("account_fullname"));
					account.setAccountEmail(rs.getString("account_email"));
					account.setAccountStatus(rs.getString("account_status"));
					return Optional.of(account);
				}
			}
		} catch (SQLException e) {
			logger.error("Error finding account by email {}: {}", email, e.getMessage());
		}
		return Optional.empty();
	}

	public boolean updateUserStatus(String email, String status) {
		String sql = "UPDATE account SET account_status = ? WHERE account_email = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, status);
			ps.setString(2, email);
			int rows = ps.executeUpdate();
			logger.info("Updated status to {} for email {}. Rows affected: {}", status, email, rows);
			return rows > 0;
		} catch (SQLException e) {
			logger.error("Error updating status for email {}: {}", email, e.getMessage());
			return false;
		}
	}

	public boolean createOrUpdateTestAccount(String username, String password, String fullname, String email, String status, String role) {
		Optional<Account> existing = findByEmail(email);
		if (existing.isPresent() && status.equalsIgnoreCase(existing.get().getAccountStatus())) {
			logger.info("Account {} exists with status {}.", email, status);
			return true;
		}
		if (existing.isPresent()) {
			return updateUserStatus(email, status);
		}

		String sql = "INSERT INTO account (username, password, account_fullname, account_email, account_status, account_role, account_dob) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setString(2, password); // Mật khẩu nên được hash trong thực tế
			ps.setString(3, fullname);
			ps.setString(4, email);
			ps.setString(5, status);
			ps.setString(6, role);
			int rows = ps.executeUpdate();
			logger.info("Created test account {} with status {}.", email, status);
			return rows > 0;
		} catch (SQLException e) {
			logger.error("Error creating test account {}: {}", email, e.getMessage());
			return false;
		}
	}

	public boolean deleteTestAccount(String email) {
		String sql = "DELETE FROM account WHERE account_email = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, email);
			int rows = ps.executeUpdate();
			logger.info("Deleted test account {}. Rows affected: {}.", email, rows);
			return true;
		} catch (SQLException e) {
			logger.error("Error deleting test account {}: {}", email, e.getMessage());
			return false;
		}
	}
}
