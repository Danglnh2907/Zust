package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "participate")
public class Participate {
	@EmbeddedId
	private ParticipateId id;
	@MapsId("accountId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@MapsId("groupId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;
	@ColumnDefault("getdate()")
	@Column(name = "participate_start_date", nullable = false)
	private Instant participateStartDate;

	public ParticipateId getId() {
		return id;
	}

	public void setId(ParticipateId id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Instant getParticipateStartDate() {
		return participateStartDate;
	}

	public void setParticipateStartDate(Instant participateStartDate) {
		this.participateStartDate = participateStartDate;
	}

}
