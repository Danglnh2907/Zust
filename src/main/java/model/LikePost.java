package model;

import jakarta.persistence.*;

@Entity
@Table(name = "like_post")
public class LikePost {
	@EmbeddedId
	private LikePostId id;
	@MapsId("accountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@MapsId("postId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	public LikePostId getId() {
		return id;
	}

	public void setId(LikePostId id) {
		this.id = id;
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

}
