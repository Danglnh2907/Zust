package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "post")
public class Post {
	@Id
	@Column(name = "post_id", nullable = false)
	private Integer id;
	@Lob
	@Column(name = "post_content")
	private String postContent;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ColumnDefault("getdate()")
	@Column(name = "post_create_date", nullable = false)
	private Instant postCreateDate;
	@Column(name = "post_last_update")
	private Instant postLastUpdate;
	@ColumnDefault("'public'")
	@Column(name = "post_privacy", nullable = false, length = 10)
	private String postPrivacy;
	@ColumnDefault("'published'")
	@Column(name = "post_status", nullable = false, length = 10)
	private String postStatus;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPostContent() {
		return postContent;
	}

	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Instant getPostCreateDate() {
		return postCreateDate;
	}

	public void setPostCreateDate(Instant postCreateDate) {
		this.postCreateDate = postCreateDate;
	}

	public Instant getPostLastUpdate() {
		return postLastUpdate;
	}

	public void setPostLastUpdate(Instant postLastUpdate) {
		this.postLastUpdate = postLastUpdate;
	}

	public String getPostPrivacy() {
		return postPrivacy;
	}

	public void setPostPrivacy(String postPrivacy) {
		this.postPrivacy = postPrivacy;
	}

	public String getPostStatus() {
		return postStatus;
	}

	public void setPostStatus(String postStatus) {
		this.postStatus = postStatus;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
