package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "account_log")
public class AccountLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_log_id", nullable = false)
	private Integer id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ColumnDefault("getdate()")
	@Column(name = "account_log_create_date", nullable = false)
	private Instant accountLogCreateDate;
	@Lob
	@Column(name = "account_log_content", nullable = false)
	private String accountLogContent;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Instant getAccountLogCreateDate() {
		return accountLogCreateDate;
	}

	public void setAccountLogCreateDate(Instant accountLogCreateDate) {
		this.accountLogCreateDate = accountLogCreateDate;
	}

	public String getAccountLogContent() {
		return accountLogContent;
	}

	public void setAccountLogContent(String accountLogContent) {
		this.accountLogContent = accountLogContent;
	}

}
