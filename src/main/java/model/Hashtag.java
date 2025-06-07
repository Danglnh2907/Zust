package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hashtag")
public class Hashtag {
	@Id
	@Column(name = "hashtag_id", nullable = false)
	private Integer id;
	@Column(name = "hashtag_name", nullable = false, length = 50)
	private String hashtagName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHashtagName() {
		return hashtagName;
	}

	public void setHashtagName(String hashtagName) {
		this.hashtagName = hashtagName;
	}

}
