package dto;

import java.time.LocalDateTime;

public class ReqCommentDTO {
    private int accountID;
    private int postID;
    private String commentContent;
    private String commentImage;
    private Integer replyCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    public ReqCommentDTO() {
        this.accountID = -1;
        this.postID = -1;
        this.commentContent = "";
        this.commentImage = null;
        this.replyCommentId = null;
        this.createdAt = null;
        this.lastModified = null;
    }

    public ReqCommentDTO(int accountID, int postID, String commentContent, String commentImage, Integer replyCommentId, LocalDateTime createdAt, LocalDateTime lastModified) {
        this.accountID = accountID;
        this.postID = postID;
        this.setCommentContent(commentContent); // Use setter for validation
        this.commentImage = commentImage;
        this.replyCommentId = replyCommentId;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = (commentContent == null || commentContent.trim().isEmpty()) ? "" : commentContent.trim();
    }

    public String getCommentImage() {
        return commentImage;
    }

    public void setCommentImage(String commentImage) {
        this.commentImage = commentImage;
    }

    public Integer getReplyCommentId() {
        return replyCommentId;
    }

    public void setReplyCommentId(Integer replyCommentId) {
        this.replyCommentId = replyCommentId;
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
}