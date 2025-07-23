package model;

import java.util.ArrayList;
import java.util.List;

public class FeedbackGroupDTO {
    private String content;
    private int requesterID;
    private String username;
    private String avatar;

    public FeedbackGroupDTO() {
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRequesterID() {
        return requesterID;
    }

    public void setRequesterID(int requesterID) {
        this.requesterID = requesterID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}