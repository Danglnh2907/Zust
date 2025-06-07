package model;

import jakarta.persistence.*;

@Entity
@Table(name = "like_comment")
public class LikeComment {
	@EmbeddedId
	private LikeCommentId id;
	@MapsId("accountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@MapsId("commentId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;

	public LikeCommentId getId() {
		return id;
	}

	public void setId(LikeCommentId id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

}
