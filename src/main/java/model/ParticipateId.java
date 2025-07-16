package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ParticipateId implements Serializable {
	private static final long serialVersionUID = -8532366068669335140L;
	@Column(name = "account_id", nullable = false)
	private Integer accountId;
	@Column(name = "group_id", nullable = false)
	private Integer groupId;

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		ParticipateId entity = (ParticipateId) o;
		return Objects.equals(this.accountId, entity.accountId) &&
				Objects.equals(this.groupId, entity.groupId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, groupId);
	}

}