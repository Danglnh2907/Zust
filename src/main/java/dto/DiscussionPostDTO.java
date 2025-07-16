package dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DiscussionPostDTO {
    private int postId;
    private String postContent;
    private String username;
    private String avatar;
    private LocalDateTime lastModified;
    private List<String> hashtags;
    private List<String> images;
    private int likeCount;
    private int commentCount;
    private int repostCount;

    public DiscussionPostDTO() {
        this.hashtags = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    // Getters and Setters
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public int getRepostCount() { return repostCount; }
    public void setRepostCount(int repostCount) { this.repostCount = repostCount; }

}