package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RepostId implements Serializable {
	private static final long serialVersionUID = 1008967023865528508L;
	@Column(name = "account_id", nullable = false)
	private Integer accountId;
	@Column(name = "post_id", nullable = false)
	private Integer postId;

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		RepostId entity = (RepostId) o;
		return Objects.equals(this.accountId, entity.accountId) &&
				Objects.equals(this.postId, entity.postId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, postId);
	}

}
