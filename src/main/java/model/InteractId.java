package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InteractId implements Serializable {
	private static final long serialVersionUID = -8581101629838154737L;
	@Column(name = "actor_account_id", nullable = false)
	private Integer actorAccountId;
	@Column(name = "target_account_id", nullable = false)
	private Integer targetAccountId;

	public Integer getActorAccountId() {
		return actorAccountId;
	}

	public void setActorAccountId(Integer actorAccountId) {
		this.actorAccountId = actorAccountId;
	}

	public Integer getTargetAccountId() {
		return targetAccountId;
	}

	public void setTargetAccountId(Integer targetAccountId) {
		this.targetAccountId = targetAccountId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		InteractId entity = (InteractId) o;
		return Objects.equals(this.targetAccountId, entity.targetAccountId) &&
				Objects.equals(this.actorAccountId, entity.actorAccountId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetAccountId, actorAccountId);
	}

}
