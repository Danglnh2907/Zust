package dto.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class TokenDto {
	private String token;
	private Date createdAt;
	private Date expiredAt;

	public TokenDto() {
		// Constructor mặc định
	}

	public TokenDto(String token, Date createdAt, Date expiredAt) {
		this.token = token;
		this.createdAt = createdAt;
		this.expiredAt = expiredAt;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(Date expiredAt) {
		this.expiredAt = expiredAt;
	}

	public boolean isExpired() {
		if (this.expiredAt == null) {
			return false; // Coi như không bao giờ hết hạn nếu không có thời gian hết hạn
		}
		return new Date().after(this.expiredAt);
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		return "TokenDto{" +
				"token='" + token + '\'' +
				", createdAt=" + (createdAt != null ? sdf.format(createdAt) : "null") +
				", expiredAt=" + (expiredAt != null ? sdf.format(expiredAt) : "null") +
				", isExpired=" + isExpired() +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TokenDto tokenDto = (TokenDto) o;
		return Objects.equals(token, tokenDto.token) &&
				Objects.equals(createdAt, tokenDto.createdAt) &&
				Objects.equals(expiredAt, tokenDto.expiredAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(token, createdAt, expiredAt);
	}
}
