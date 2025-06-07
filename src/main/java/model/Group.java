package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "\"group\"")
public class Group {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "group_id", nullable = false)
	private Integer id;
	@Column(name = "group_name", length = 100)
	private String groupName;
	@Lob
	@Column(name = "group_cover_image")
	private String groupCoverImage;
	@Lob
	@Column(name = "group_description")
	private String groupDescription;
	@ColumnDefault("getdate()")
	@Column(name = "group_create_date", nullable = false)
	private Instant groupCreateDate;
	@ColumnDefault("'active'")
	@Column(name = "group_status", nullable = false, length = 10)
	private String groupStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupCoverImage() {
		return groupCoverImage;
	}

	public void setGroupCoverImage(String groupCoverImage) {
		this.groupCoverImage = groupCoverImage;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public Instant getGroupCreateDate() {
		return groupCreateDate;
	}

	public void setGroupCreateDate(Instant groupCreateDate) {
		this.groupCreateDate = groupCreateDate;
	}

	public String getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(String groupStatus) {
		this.groupStatus = groupStatus;
	}

}
