package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "report_post")
public class ReportPost {
	@Id
	@Column(name = "report_id", nullable = false)
	private Integer id;
	@Column(name = "report_content", nullable = false, length = 250)
	private String reportContent;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
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

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
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
