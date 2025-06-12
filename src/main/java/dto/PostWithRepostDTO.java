package dto;

import model.Account;
import model.Post;

public class PostWithRepostDTO {
    private Post post;
    private int repostCount;
    private boolean isReposted;

    public PostWithRepostDTO(Post post, int repostCount, boolean isReposted) {
        this.post = post;
        this.repostCount = repostCount;
        this.isReposted = isReposted;
    }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public int getRepostCount() { return repostCount; }
    public void setRepostCount(int repostCount) { this.repostCount = repostCount; }
    public boolean isReposted() { return isReposted; }
    public void setReposted(boolean isReposted) { this.isReposted = isReposted; }
}