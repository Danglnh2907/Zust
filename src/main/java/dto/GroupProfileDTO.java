// Khai báo package DTO (Data Transfer Object)
package dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Lớp DTO dùng để chứa thông tin của một Group khi truyền qua lại giữa Controller, DAO và View
public class GroupProfileDTO {
    // ==== Các thuộc tính (fields) ====

    // ID của nhóm
    private int groupId;

    // Tên nhóm
    private String groupName;

    // Mô tả nhóm
    private String description;

    // Đường dẫn avatar (ảnh đại diện) của nhóm
    private String avatarPath;

    // Trạng thái nhóm: "public" hoặc "private"
    private String status;

    // ID của tài khoản tạo nhóm (account_id)
    private int createdBy;

    // Ngày tạo nhóm
    private LocalDateTime groupCreateDate;

    // Ngày cập nhật cuối cùng
    private LocalDateTime lastUpdated;

    // Danh sách lỗi (dùng để lưu các lỗi hợp lệ hóa dữ liệu trong quá trình nhập)
    private List<String> errors;

    // ==== Constructor ====

    // Hàm khởi tạo: khởi tạo danh sách lỗi rỗng
    public GroupProfileDTO() {
        this.errors = new ArrayList<>();
    }

    // ==== Getter và Setter ====

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getStatus() {
        return status;
    }

    // Set trạng thái nhóm: nếu là "public" thì set là public, còn lại thì là private
    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getGroupCreateDate() {
        return groupCreateDate;
    }

    public void setGroupCreateDate(LocalDateTime groupCreateDate) {
        this.groupCreateDate = groupCreateDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Trả về danh sách lỗi hiện tại
    public List<String> getErrors() {
        return errors;
    }

    // Thêm lỗi vào danh sách lỗi
    public void addError(String error) {
        this.errors.add(error);
    }
}