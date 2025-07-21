package model;

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
    private int accountID;
    private String username;
    private String avatar;
    private LocalDateTime lastModified;
    private List<String> hashtags;
    private List<String> images;
    private int likeCount;
    private int commentCount;
    private int repostCount;
    private boolean liked;
    private boolean ownPost;
    private RespPostDTO repost;

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

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
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

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isOwnPost() {
        return ownPost;
    }

    public void setOwnPost(boolean ownPost) {
        this.ownPost = ownPost;
    }

    public RespPostDTO getRepost() {
        return repost;
    }

    public void setRepost(RespPostDTO repost) {
        this.repost = repost;
    }

    private String getLastTimeUpdate(Duration timeDiff) {
        if (timeDiff.toSeconds() < 60) return "Just now";
        if (timeDiff.toMinutes() < 60) return timeDiff.toMinutes() + " minutes";
        if (timeDiff.toHours() < 24) return timeDiff.toHours() + " hours";
        return timeDiff.toDays() + " days";
    }

    private String getAction() {
        /*
         * If the requester is the owner of the post and this is a repost, then we only have delete
         * If the requester is the owner of the post and this isn't a repost, then we have edit and delete
         * Else, we only let the report option
         */
        String template = """
                <div class="post-options">
                    <button class="options-btn" aria-label="More options">...</button>
                    <div class="options-menu">
                        %s
                    </div>
                </div>""";
        if (ownPost && repost != null) {
            String delete = String.format("<a href=\"#\" class=\"delete\" data-post-id=\"%s\">Delete</a>", postId);
            return String.format(template, delete);
        }

        if (ownPost) {
            String edit = String.format("<a href=\"#\" class=\"edit\" data-post-id=\"%s\">Edit</a>", postId);
            String delete = String.format("<a href=\"#\" class=\"delete\" data-post-id=\"%s\">Delete</a>", postId);
            return String.format(template, edit + delete);
        }

        String report = String.format("<a href=\"/zust/report?type=post&id=%s\" class=\"report\">Report</a>", postId);
        return String.format(template, report);
    }

    private String getImageCarousel(List<String> images) {
        if (images.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String image : images) {
            sb.append(String.format("<img class=\"carousel-slide\" src=\"%s\">",
                    "static/images/" + image));
        }
        return String.format(templates[3], sb);
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
        String header = String.format(templates[1],
                getAvatar(), getAccountID(), getUsername(), getLastTimeUpdate(timeDiff), getAction());


        //Get image carousel
        String carousel = getImageCarousel(getImages());

        //Get the repost (if exist)
        String repostTemplate = "";
        if (repost != null) {
            //Repost only have header (without the 3-dot button), content, carousel
            String repostHeader = String.format(templates[1], repost.getAvatar(), repost.getAccountID(), repost.getUsername(),
                    getLastTimeUpdate(Duration.between(repost.getLastModified(), LocalDateTime.now())), "");
            String repostContent = String.format(templates[2], repost.getPostContent());
            String repostImage = getImageCarousel(repost.getImages());
            repostTemplate = String.format(templates[0], repost.getPostId(), repostHeader, repostContent, repostImage, "");
        }

        //Get post content
        String content = String.format(templates[2], getPostContent() + repostTemplate);

        //Get likes, comment and repost count
        String action = String.format(templates[4], liked ? "liked" : "", getLikeCount(), getCommentCount(), getRepostCount());

        return String.format(templates[0], getPostId(), header, content, carousel, action);
    }

}
