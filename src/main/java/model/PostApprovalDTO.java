package model;

import java.util.ArrayList;
import java.util.List;

public class PostApprovalDTO {
    private Post post;
    private Account account;
    private List<String> errors;
    private List<String> hashtags;
    private List<String> images;

    public PostApprovalDTO() {
        this.errors = new ArrayList<>();
        this.hashtags = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public PostApprovalDTO(Post post, Account account) {
        this();
        this.post = post;
        this.account = account;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}