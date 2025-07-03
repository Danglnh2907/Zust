package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "feedback_group")
public class FeedbackGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_group_id", nullable = false)
	private Integer id;
	@Lob
	@Column(name = "feedback_group_content", nullable = false)
	private String feedbackGroupContent;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;
	@ColumnDefault("getdate()")
	@Column(name = "report_create_date", nullable = false)
	private Instant reportCreateDate;
	@Column(name = "report_status")
	private String  reportStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFeedbackGroupContent() {
		return feedbackGroupContent;
	}

	public void setFeedbackGroupContent(String feedbackGroupContent) {
		this.feedbackGroupContent = feedbackGroupContent;
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

	public Instant getReportCreateDate() {
		return reportCreateDate;
	}

	public void setReportCreateDate(Instant reportCreateDate) {
		this.reportCreateDate = reportCreateDate;
	}

//	public Boolean getReportStatus() {
//		return reportStatus;
//	}
//
//	public void setReportStatus(Boolean reportStatus) {
//		this.reportStatus = reportStatus;
//	}

	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}


}
