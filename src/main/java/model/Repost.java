package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "repost")
public class Repost {
	@EmbeddedId
	private RepostId id;
	@MapsId("accountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@MapsId("postId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
	@ColumnDefault("getdate()")
	@Column(name = "repost_create_date", nullable = false)
	private Instant repostCreateDate;

	public RepostId getId() {
		return id;
	}

	public void setId(RepostId id) {
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

	public Instant getRepostCreateDate() {
		return repostCreateDate;
	}

	public void setRepostCreateDate(Instant repostCreateDate) {
		this.repostCreateDate = repostCreateDate;
	}

}
