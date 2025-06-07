package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "report_comment")
public class ReportComment {
	@Id
	@Column(name = "report_id", nullable = false)
	private Integer id;
	@Column(name = "report_feedback", nullable = false, length = 250)
	private String reportFeedback;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;
	@ColumnDefault("getdate()")
	@Column(name = "report_create_date", nullable = false)
	private Instant reportCreateDate;
	@Column(name = "report_status")
	private Boolean reportStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getReportFeedback() {
		return reportFeedback;
	}

	public void setReportFeedback(String reportFeedback) {
		this.reportFeedback = reportFeedback;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public Instant getReportCreateDate() {
		return reportCreateDate;
	}

	public void setReportCreateDate(Instant reportCreateDate) {
		this.reportCreateDate = reportCreateDate;
	}

	public Boolean getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(Boolean reportStatus) {
		this.reportStatus = reportStatus;
	}

}
