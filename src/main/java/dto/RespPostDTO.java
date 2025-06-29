package dto;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DTO class for carrying data from database to View: Post
 */
public class RespPostDTO {
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

    private final Logger logger = Logger.getLogger(RespPostDTO.class.getName());
    private final String[] templates;

    public RespPostDTO() {
        this.hashtags = new ArrayList<>();
        this.images = new ArrayList<>();

        templates = new String[5];
        try {
            // Get the ClassLoader to read from resources
            ClassLoader classLoader = getClass().getClassLoader();

            // Read templates from resources directory
            templates[0] = new String(classLoader.getResourceAsStream("templates/post.html").readAllBytes());
            templates[1] = new String(classLoader.getResourceAsStream("templates/post_header.html").readAllBytes());
            templates[2] = new String(classLoader.getResourceAsStream("templates/post_content.html").readAllBytes());
            templates[3] = new String(classLoader.getResourceAsStream("templates/post_image_carousel.html").readAllBytes());
            templates[4] = new String(classLoader.getResourceAsStream("templates/post_action.html").readAllBytes());
        } catch (IOException | NullPointerException e) {
            logger.warning("Failed to read templates from resources: " + e.getMessage());
        }
    }

    // Getters and Setters
    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
    }

    @Override
    public String toString() {
        /*
         * templates[0]: base template
         * templates[1]: post header template
         * templates[2]: post content template
         * templates[3]: images' carousel template
         * templates[4]: action template
         */

        //Get header
        Duration timeDiff = Duration.between(getLastModified(), LocalDateTime.now());
        String lastTimeUpdate;
        if (timeDiff.toSeconds() < 60) {
            lastTimeUpdate = "Just now";
        } else if (timeDiff.toMinutes() < 60) {
            lastTimeUpdate = timeDiff.toMinutes() + " minutes";
        } else if (timeDiff.toHours() < 24) {
            lastTimeUpdate = timeDiff.toHours() + " hours";
        } else {
            lastTimeUpdate = timeDiff.toDays() + " days";
        }
        String header = String.format(templates[1], getAvatar(), getUsername(), lastTimeUpdate, getPostId(), getPostId(), getPostId());

        //Get post content
        String content = String.format(templates[2], getPostContent());

        //Get image carousel
        String carousel = "";
        if (!getImages().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String image : getImages()) {
                sb.append(String.format("<img class=\"carousel-slide\" src=\"%s\">",
                        "static/images/" + image));
            }
            carousel = String.format(templates[3], sb);
        }

        //Get likes, comment and repost count
        String action = String.format(templates[4], getLikeCount(), getCommentCount(), getRepostCount());

        return String.format(templates[0], getPostId(), header, content, carousel, action);
    }
}
