package model;

import jakarta.persistence.*;

@Entity
@Table(name = "tag_hashtag")
public class TagHashtag {
	@EmbeddedId
	private TagHashtagId id;
	@MapsId("postId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
	@MapsId("hashtagId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hashtag_id", nullable = false)
	private Hashtag hashtag;

	public TagHashtagId getId() {
		return id;
	}

	public void setId(TagHashtagId id) {
		this.id = id;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public Hashtag getHashtag() {
		return hashtag;
	}

	public void setHashtag(Hashtag hashtag) {
		this.hashtag = hashtag;
	}

}
