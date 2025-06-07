package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "token")
public class Token {
	@Id
	@Column(name = "token_id", nullable = false)
	private Integer id;
	@Column(name = "token_content", nullable = false, length = 512)
	private String tokenContent;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;
	@ColumnDefault("getdate()")
	@Column(name = "created_at")
	private Instant createdAt;
	@Column(name = "updated_at")
	private Instant updatedAt;
	@ColumnDefault("0")
	@Column(name = "token_status")
	private Boolean tokenStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTokenContent() {
		return tokenContent;
	}

	public void setTokenContent(String tokenContent) {
		this.tokenContent = tokenContent;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getTokenStatus() {
		return tokenStatus;
	}

	public void setTokenStatus(Boolean tokenStatus) {
		this.tokenStatus = tokenStatus;
	}

}
