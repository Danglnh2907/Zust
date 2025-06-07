package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "interact")
public class Interact {
	@EmbeddedId
	private InteractId id;
	@MapsId("actorAccountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "actor_account_id", nullable = false)
	private Account actorAccount;
	@MapsId("targetAccountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "target_account_id", nullable = false)
	private Account targetAccount;
	@Column(name = "interact_status", length = 10)
	private String interactStatus;
	@ColumnDefault("getdate()")
	@Column(name = "interact_start_date", nullable = false)
	private Instant interactStartDate;

	public InteractId getId() {
		return id;
	}

	public void setId(InteractId id) {
		this.id = id;
	}

	public Account getActorAccount() {
		return actorAccount;
	}

	public void setActorAccount(Account actorAccount) {
		this.actorAccount = actorAccount;
	}

	public Account getTargetAccount() {
		return targetAccount;
	}

	public void setTargetAccount(Account targetAccount) {
		this.targetAccount = targetAccount;
	}

	public String getInteractStatus() {
		return interactStatus;
	}

	public void setInteractStatus(String interactStatus) {
		this.interactStatus = interactStatus;
	}

	public Instant getInteractStartDate() {
		return interactStartDate;
	}

	public void setInteractStartDate(Instant interactStartDate) {
		this.interactStartDate = interactStartDate;
	}

}
