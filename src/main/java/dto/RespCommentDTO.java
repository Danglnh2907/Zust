package dto;

import java.time.LocalDateTime;

public class RespCommentDTO {
    private int commentId;
    private String commentContent;
    private String commentImage;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private Integer replyCommentId;
    private int likeCount;
    private int accountId;

    public RespCommentDTO() {
        this.commentId = 0;
        this.commentContent = "";
        this.commentImage = null;
        this.username = "";
        this.createdAt = null;
        this.lastModified = null;
        this.replyCommentId = null;
        this.likeCount = 0;
        this.accountId = 0;
    }

    public RespCommentDTO(int commentId, String commentContent, String commentImage, String username,
                          LocalDateTime createdAt, LocalDateTime lastModified, Integer replyCommentId,
                          int likeCount, int accountId) {
        this.commentId = commentId;
        this.commentContent = commentContent;
        this.commentImage = commentImage;
        this.username = username;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.replyCommentId = replyCommentId;
        this.likeCount = likeCount;
        this.accountId = accountId;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getCommentImage() {
        return commentImage;
    }

    public void setCommentImage(String commentImage) {
        this.commentImage = commentImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Integer getReplyCommentId() {
        return replyCommentId;
    }

    public void setReplyCommentId(Integer replyCommentId) {
        this.replyCommentId = replyCommentId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}