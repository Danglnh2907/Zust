package model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "account")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id", nullable = false)
	private Integer id;
	@Column(name = "username", nullable = false, length = 250)
	private String username;
	@Column(name = "password", nullable = false, length = 250)
	private String password;
	@Column(name = "fullname", nullable = false, length = 50)
	private String fullname;
	@Column(name = "email", nullable = false, length = 50)
	private String email;
	@Column(name = "phone", length = 15)
	private String phone;
	@Column(name = "gender")
	private Boolean gender;
	@Column(name = "dob")
	private LocalDate dob;
	@Lob
	@Column(name = "avatar")
	private String avatar;
	@Column(name = "bio", length = 250)
	private String bio;
	@ColumnDefault("100")
	@Column(name = "credit", nullable = false)
	private Integer credit;
	@ColumnDefault("'active'")
	@Column(name = "account_status", nullable = false, length = 10)
	private String accountStatus;
	@ColumnDefault("'user'")
	@Column(name = "account_role", nullable = false, length = 10)
	private String accountRole;

	private String googleId;

	public String getGoogleId() {
		return googleId;
	}

	public void setGoogleId(String googleId) {
		this.googleId = googleId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Boolean getGender() {
		return gender;
	}

	public void setGender(Boolean gender) {
		this.gender = gender;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public Integer getCredit() {
		return credit;
	}

	public void setCredit(Integer credit) {
		this.credit = credit;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getAccountRole() {
		return accountRole;
	}

	public void setAccountRole(String accountRole) {
		this.accountRole = accountRole;
	}

}
