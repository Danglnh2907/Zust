package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {
	@Id
	@Column(name = "notification_id", nullable = false)
	private Integer id;
	@Column(name = "notification_title", nullable = false, length = 250)
	private String notificationTitle;
	@Lob
	@Column(name = "notification_content", nullable = false)
	private String notificationContent;
	@ColumnDefault("getdate()")
	@Column(name = "notification_create_date", nullable = false)
	private Instant notificationCreateDate;
	@Column(name = "notification_last_update")
	private Instant notificationLastUpdate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNotificationTitle() {
		return notificationTitle;
	}

	public void setNotificationTitle(String notificationTitle) {
		this.notificationTitle = notificationTitle;
	}

	public String getNotificationContent() {
		return notificationContent;
	}

	public void setNotificationContent(String notificationContent) {
		this.notificationContent = notificationContent;
	}

	public Instant getNotificationCreateDate() {
		return notificationCreateDate;
	}

	public void setNotificationCreateDate(Instant notificationCreateDate) {
		this.notificationCreateDate = notificationCreateDate;
	}

	public Instant getNotificationLastUpdate() {
		return notificationLastUpdate;
	}

	public void setNotificationLastUpdate(Instant notificationLastUpdate) {
		this.notificationLastUpdate = notificationLastUpdate;
	}

}
