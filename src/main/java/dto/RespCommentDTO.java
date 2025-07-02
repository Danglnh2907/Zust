package dto;

import java.time.LocalDateTime;
import java.util.logging.Logger;

public class RespCommentDTO {
    private int id;
    private String content;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int totalLikes;
    private int accountID;
    private String username;
    private String avatar;
    private int postID;
    private int replyID;
    private boolean liked;

    private String template;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public RespCommentDTO() {
        this.id = -1;
        this.content = "";
        this.image = "";
        this.createdAt = null;
        this.updatedAt = null;
        this.totalLikes = 0;
        this.accountID = -1;
        this.username = "";
        this.avatar = "";
        this.postID = -1;
        this.replyID = -1;
        this.liked = false;

        try {
            template = new String(this.getClass().getClassLoader()
                    .getResourceAsStream("templates/comment_item.html").readAllBytes());
        } catch (Exception e) {
            logger.severe("Failed to load template\nError: " + e.getMessage());
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public int getReplyID() {
        return replyID;
    }

    public void setReplyID(int replyID) {
        this.replyID = replyID;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    @Override
    public String toString() {
        String imageURL = String.format("<img src=\"/zust/static/images/%s\" alt=\"Comment image\">", image);

        //The template UI have no display for last_update yet
        return String.format(template,
                replyID == -1 ? "" : "is-reply",
                id,
                avatar,
                username,
                username.toLowerCase().replaceAll(" ", ""),
                content,
                image == null || image.isEmpty() ? "" : imageURL,
                liked ? "liked" : "",
                totalLikes,
                id);
    }
}
