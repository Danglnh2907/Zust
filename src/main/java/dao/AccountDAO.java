package dao;

import dto.ResGroupDTO;
import model.Account;
import util.database.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                account.setFullname(rs.getString("fullname"));
                account.setAvatar(rs.getString("avatar"));
                accounts.add(account);
            }
            return accounts;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }



    public static void main(String[] args) {
        AccountDAO dao = new AccountDAO();
//        System.out.println(dao.getActiveAccounts());
//        System.out.println(dao.getActiveAccountsManagers(31));
    }
}
