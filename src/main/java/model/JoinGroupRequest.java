package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "join_group_request")
public class JoinGroupRequest {
	@Id
	@Column(name = "join_group_request_id", nullable = false)
	private Integer id;
	@Column(name = "join_group_request_content", length = 250)
	private String joinGroupRequestContent;
	@ColumnDefault("getdate()")
	@Column(name = "join_group_request_date", nullable = false)
	private Instant joinGroupRequestDate;
	@ColumnDefault("'sended'")
	@Column(name = "join_group_request_status", nullable = false, length = 10)
	private String joinGroupRequestStatus;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getJoinGroupRequestContent() {
		return joinGroupRequestContent;
	}

	public void setJoinGroupRequestContent(String joinGroupRequestContent) {
		this.joinGroupRequestContent = joinGroupRequestContent;
	}

	public Instant getJoinGroupRequestDate() {
		return joinGroupRequestDate;
	}

	public void setJoinGroupRequestDate(Instant joinGroupRequestDate) {
		this.joinGroupRequestDate = joinGroupRequestDate;
	}

	public String getJoinGroupRequestStatus() {
		return joinGroupRequestStatus;
	}

	public void setJoinGroupRequestStatus(String joinGroupRequestStatus) {
		this.joinGroupRequestStatus = joinGroupRequestStatus;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
