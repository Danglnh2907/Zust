package dto;

import model.Account;
import model.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DTO (Data Transfer Object) dùng để gom dữ liệu của một bài post và thông tin tài khoản người đăng bài.
 * Thường dùng để truyền dữ liệu từ tầng DAO lên controller/view một cách thuận tiện.
 */
public class PostApprovalDTO {
    private Post post;                // Thông tin bài đăng
    private Account account;          // Thông tin tài khoản của người đăng
    private List<String> errors;      // Danh sách lỗi (nếu có), dùng để hiển thị lên giao diện

    // Constructor: khởi tạo đối tượng với bài post và tài khoản tương ứng
    public PostApprovalDTO(Post post, Account account) {
        this.post = post;
        this.account = account;
        this.errors = new ArrayList<>(); // Khởi tạo danh sách lỗi rỗng
    }

    // Getter & Setter cho bài post
    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    // Getter & Setter cho tài khoản
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    // Getter danh sách lỗi
    public List<String> getErrors() {
        return errors;
    }

    // Thêm lỗi vào danh sách lỗi
    public void addError(String error) {
        this.errors.add(error);
    }
}
