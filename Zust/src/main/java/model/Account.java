package model;

public class Account {
	private int accountId;
	private String username;
	private String accountStatus;
	private String accountEmail;
	private String accountFullname;
	// Getters and Setters
	public int getAccountId() { return accountId; }
	public void setAccountId(int accountId) { this.accountId = accountId; }
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getAccountStatus() { return accountStatus; }
	public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
	public String getAccountEmail() { return accountEmail; }
	public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }
	public String getAccountFullname() { return accountFullname; }
	public void setAccountFullname(String accountFullname) { this.accountFullname = accountFullname; }
}
