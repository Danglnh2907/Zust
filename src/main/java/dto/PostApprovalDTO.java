package dto;

import model.Account;
import model.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostApprovalDTO {
    private Post post;
    private Account account;
    private List<String> errors;

    public PostApprovalDTO(Post post, Account account) {
        this.post = post;
        this.account = account;
        this.errors = new ArrayList<>();
    }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public List<String> getErrors() { return errors; }
    public void addError(String error) { this.errors.add(error); }
}