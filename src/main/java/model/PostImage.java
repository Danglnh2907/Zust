package model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_image")
public class PostImage {
	@Id
	@Column(name = "post_image_id", nullable = false)
	private Integer id;
	@Lob
	@Column(name = "post_image", nullable = false)
	private String postImage;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPostImage() {
		return postImage;
	}

	public void setPostImage(String postImage) {
		this.postImage = postImage;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

}
