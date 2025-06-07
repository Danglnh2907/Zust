package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TagHashtagId implements Serializable {
	private static final long serialVersionUID = -3343098169071633743L;
	@Column(name = "hashtag_index", nullable = false)
	private Integer hashtagIndex;
	@Column(name = "post_id", nullable = false)
	private Integer postId;
	@Column(name = "hashtag_id", nullable = false)
	private Integer hashtagId;

	public Integer getHashtagIndex() {
		return hashtagIndex;
	}

	public void setHashtagIndex(Integer hashtagIndex) {
		this.hashtagIndex = hashtagIndex;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getHashtagId() {
		return hashtagId;
	}

	public void setHashtagId(Integer hashtagId) {
		this.hashtagId = hashtagId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		TagHashtagId entity = (TagHashtagId) o;
		return Objects.equals(this.hashtagId, entity.hashtagId) &&
				Objects.equals(this.hashtagIndex, entity.hashtagIndex) &&
				Objects.equals(this.postId, entity.postId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hashtagId, hashtagIndex, postId);
	}

}
