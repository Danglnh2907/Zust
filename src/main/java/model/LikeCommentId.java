package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LikeCommentId implements Serializable {
	private static final long serialVersionUID = 4880645421463507884L;
	@Column(name = "account_id", nullable = false)
	private Integer accountId;
	@Column(name = "comment_id", nullable = false)
	private Integer commentId;

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		LikeCommentId entity = (LikeCommentId) o;
		return Objects.equals(this.accountId, entity.accountId) &&
				Objects.equals(this.commentId, entity.commentId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, commentId);
	}

}
