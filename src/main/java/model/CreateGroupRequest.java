package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "create_group_request")
public class CreateGroupRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "create_group_request_id", nullable = false)
	private Integer id;
	@Lob
	@Column(name = "create_group_request_content")
	private String createGroupRequestContent;
	@ColumnDefault("getdate()")
	@Column(name = "group_request_date", nullable = false)
	private Instant groupRequestDate;
	@ColumnDefault("'sended'")
	@Column(name = "group_request_status", nullable = false, length = 10)
	private String groupRequestStatus;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCreateGroupRequestContent() {
		return createGroupRequestContent;
	}

	public void setCreateGroupRequestContent(String createGroupRequestContent) {
		this.createGroupRequestContent = createGroupRequestContent;
	}

	public Instant getGroupRequestDate() {
		return groupRequestDate;
	}

	public void setGroupRequestDate(Instant groupRequestDate) {
		this.groupRequestDate = groupRequestDate;
	}

	public String getGroupRequestStatus() {
		return groupRequestStatus;
	}

	public void setGroupRequestStatus(String groupRequestStatus) {
		this.groupRequestStatus = groupRequestStatus;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
