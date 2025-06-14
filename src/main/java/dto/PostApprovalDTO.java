package dto; // Gói chứa lớp Data Transfer Object (DTO) dùng để truyền dữ liệu giữa các tầng

import model.Account; // Import lớp Account từ package model
import model.Post;    // Import lớp Post từ package model

import java.util.ArrayList; // Import ArrayList để khởi tạo danh sách lỗi
import java.util.List;      // Import interface List để quản lý danh sách lỗi

// Lớp PostApprovalDTO dùng để gom dữ liệu của một bài post và thông tin tài khoản người đăng
public class PostApprovalDTO {
    private Post post;                // Bài viết cần phê duyệt
//    private Account account;          // Tài khoản người đăng bài
    private List<String> errors;      // Danh sách lỗi (nếu có), phục vụ kiểm tra và hiển thị

    // Constructor: tạo mới một đối tượng DTO với bài viết
    public PostApprovalDTO(Post post) {
        this.post = post;             // Gán bài viết vào thuộc tính post
//        this.account = account;       // ❌ Lỗi: account không được truyền vào, luôn null
        this.errors = new ArrayList<>(); // Khởi tạo danh sách lỗi rỗng
    }

    // Getter: trả về đối tượng bài viết
    public Post getPost() {
        return post;
    }

    // Setter: thiết lập lại bài viết
    public void setPost(Post post) {
        this.post = post;
    }

    // Getter: trả về đối tượng tài khoản
//    public Account getAccount() {
//        return account;
//    }
//
//    // Setter: thiết lập lại tài khoản
//    public void setAccount(Account account) {
//        this.account = account;
//    }

    // Getter: trả về danh sách lỗi
    public List<String> getErrors() {
        return errors;
    }

    // Phương thức thêm lỗi vào danh sách
    public void addError(String error) {
        this.errors.add(error); // Thêm lỗi mới vào list
    }
}
