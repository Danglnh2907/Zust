package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "view_post")
public class ViewPost {
	@EmbeddedId
	private ViewPostId id;
	@MapsId("accountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@MapsId("postId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
	@ColumnDefault("getdate()")
	@Column(name = "view_date")
	private Instant viewDate;

	public ViewPostId getId() {
		return id;
	}

	public void setId(ViewPostId id) {
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

	public Instant getViewDate() {
		return viewDate;
	}

	public void setViewDate(Instant viewDate) {
		this.viewDate = viewDate;
	}

}
