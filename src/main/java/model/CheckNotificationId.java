package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CheckNotificationId implements Serializable {
	private static final long serialVersionUID = 812479740262390095L;
	@Column(name = "account_id", nullable = false)
	private Integer accountId;
	@Column(name = "notification_id", nullable = false)
	private Integer notificationId;

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		CheckNotificationId entity = (CheckNotificationId) o;
		return Objects.equals(this.accountId, entity.accountId) &&
				Objects.equals(this.notificationId, entity.notificationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, notificationId);
	}

}
