package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "comment")
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id", nullable = false)
	private Integer id;
	@Lob
	@Column(name = "comment_content", nullable = false)
	private String commentContent;
	@Lob
	@Column(name = "comment_image")
	private String commentImage;
	@ColumnDefault("0")
	@Column(name = "comment_status", nullable = false)
	private Boolean commentStatus = false;
	@ColumnDefault("getdate()")
	@Column(name = "comment_create_date", nullable = false)
	private Instant commentCreateDate;
	@Column(name = "comment_last_update")
	private Instant commentLastUpdate;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reply_comment_id")
	private Comment replyComment;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public String getCommentImage() {
		return commentImage;
	}

	public void setCommentImage(String commentImage) {
		this.commentImage = commentImage;
	}

	public Boolean getCommentStatus() {
		return commentStatus;
	}

	public void setCommentStatus(Boolean commentStatus) {
		this.commentStatus = commentStatus;
	}

	public Instant getCommentCreateDate() {
		return commentCreateDate;
	}

	public void setCommentCreateDate(Instant commentCreateDate) {
		this.commentCreateDate = commentCreateDate;
	}

	public Instant getCommentLastUpdate() {
		return commentLastUpdate;
	}

	public void setCommentLastUpdate(Instant commentLastUpdate) {
		this.commentLastUpdate = commentLastUpdate;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public Comment getReplyComment() {
		return replyComment;
	}

	public void setReplyComment(Comment replyComment) {
		this.replyComment = replyComment;
	}

}
