package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "friend_request")
public class FriendRequest {
	@Id
	@Column(name = "friend_request_id", nullable = false)
	private Integer id;
	@Column(name = "friend_request_content", length = 250)
	private String friendRequestContent;
	@ColumnDefault("getdate()")
	@Column(name = "friend_request_date", nullable = false)
	private Instant friendRequestDate;
	@ColumnDefault("'sended'")
	@Column(name = "friend_request_status", nullable = false, length = 10)
	private String friendRequestStatus;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "send_account_id", nullable = false)
	private Account sendAccount;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "receive_account_id", nullable = false)
	private Account receiveAccount;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFriendRequestContent() {
		return friendRequestContent;
	}

	public void setFriendRequestContent(String friendRequestContent) {
		this.friendRequestContent = friendRequestContent;
	}

	public Instant getFriendRequestDate() {
		return friendRequestDate;
	}

	public void setFriendRequestDate(Instant friendRequestDate) {
		this.friendRequestDate = friendRequestDate;
	}

	public String getFriendRequestStatus() {
		return friendRequestStatus;
	}

	public void setFriendRequestStatus(String friendRequestStatus) {
		this.friendRequestStatus = friendRequestStatus;
	}

	public Account getSendAccount() {
		return sendAccount;
	}

	public void setSendAccount(Account sendAccount) {
		this.sendAccount = sendAccount;
	}

	public Account getReceiveAccount() {
		return receiveAccount;
	}

	public void setReceiveAccount(Account receiveAccount) {
		this.receiveAccount = receiveAccount;
	}

}
