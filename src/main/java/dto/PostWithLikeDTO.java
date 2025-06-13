package dto;

import model.Account;
import model.Post;

public class PostWithLikeDTO {
    private Post post;
    private int likeCount;
    private boolean isLiked;

    public PostWithLikeDTO(Post post, int likeCount, boolean isLiked) {
        this.post = post;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }
}