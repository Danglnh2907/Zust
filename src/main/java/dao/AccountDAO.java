package dao;

import dto.ResGroupDTO;
import model.Account;
import util.database.DBContext;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AccountDAO extends DBContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public List<Account> getActiveAccounts() {
        List<Account> accounts = new ArrayList<Account>();
        String sql = "SELECT * FROM account\n" +
                "WHERE account_status = 'active' AND account_role = 'user'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setFullname(rs.getString("fullname"));
                account.setEmail(rs.getString("email"));
                account.setPhone(rs.getString("phone"));
                account.setGender(rs.getBoolean("gender"));
                account.setDob(rs.getDate("dob") != null
                        ? rs.getDate("dob").toLocalDate() : null);
                account.setAvatar(rs.getString("avatar"));
                account.setBio(rs.getString("bio"));
                account.setCredit(rs.getInt("credit"));
                account.setAccountStatus(rs.getString("account_status"));
                account.setAccountRole(rs.getString("account_role"));
                accounts.add(account);
            }
            return accounts;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    public Account getAccountById(int id) {
        Account account = null;
        try {
            String sql = "SELECT * FROM account WHERE account_id = ?";
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account = new Account();
                account.setId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setFullname(rs.getString("fullname"));
                account.setEmail(rs.getString("email"));
                account.setPhone(rs.getString("phone"));
                account.setGender(rs.getBoolean("gender"));
                account.setDob(rs.getDate("dob") != null
                        ? rs.getDate("dob").toLocalDate() : null);
                account.setAvatar(rs.getString("avatar"));
                account.setBio(rs.getString("bio"));
                account.setCredit(rs.getInt("credit"));
                account.setAccountStatus(rs.getString("account_status"));
                account.setAccountRole(rs.getString("account_role"));
            }
        } catch (SQLException e) {
            logger.warning("Failed to get account by ID: " + e.getMessage());
            return null;
        }
        return account;
    }

    public boolean updateAccount(Account account) {
        try {
            String sql = """
                    UPDATE account
                    SET username = ?, password = ?, fullname = ?, phone = ?, gender = ?, dob = ?, avatar = ?, bio = ?, credit = ?, account_status = ?, account_role = ?
                    WHERE account_id = ?""";
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.setString(3, account.getFullname());
            stmt.setString(4, account.getPhone());
            stmt.setBoolean(5, account.getGender());
            stmt.setDate(6, Date.valueOf(account.getDob()));
            stmt.setString(7, account.getAvatar());
            stmt.setString(8, account.getBio());
            stmt.setInt(9, account.getCredit());
            stmt.setString(10, account.getAccountStatus());
            stmt.setString(11, account.getAccountRole());
            stmt.setInt(12, account.getId());
            return stmt.executeUpdate() >= 1;
        } catch (SQLException e) {
            logger.warning("Failed to update user: " + e.getMessage());
        }
        return false;
    }

    public boolean banAccount(int id) {
        logger.info("Attempting to ban account with ID: " + id);

        String sql = "UPDATE account SET account_status = 'banned' WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warning("No account found with ID: " + id);
                return false;
            }
            logger.info("Successfully banned account with ID: " + id);
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to ban account with ID: " + id + " - Error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        AccountDAO dao = new AccountDAO();
//        System.out.println(dao.getActiveAccounts());
//        System.out.println(dao.getActiveAccountsManagers(31));
//        Account account = dao.getAccountById(1);
//        System.out.println(account.getAvatar());
//        System.out.println(account.getUsername());
//
//        account.setPassword("123456");
//        account.setBio("Update bio");
//        dao.updateAccount(account);
    }
}
