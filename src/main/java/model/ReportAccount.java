package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "report_account")
public class ReportAccount {
	@Id
	@Column(name = "report_id", nullable = false)
	private Integer id;
	@Column(name = "report_content", nullable = false, length = 250)
	private String reportContent;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "report_account_id", nullable = false)
	private Account reportAccount;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reported_account_id", nullable = false)
	private Account reportedAccount;
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

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

	public Account getReportAccount() {
		return reportAccount;
	}

	public void setReportAccount(Account reportAccount) {
		this.reportAccount = reportAccount;
	}

	public Account getReportedAccount() {
		return reportedAccount;
	}

	public void setReportedAccount(Account reportedAccount) {
		this.reportedAccount = reportedAccount;
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
