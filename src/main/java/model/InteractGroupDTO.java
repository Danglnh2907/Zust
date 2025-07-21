package model;

import java.time.LocalDateTime;

public class InteractGroupDTO {
    private int id;
    private String name;
    private String coverImage;
    private String description;
    private int createrId;
    private LocalDateTime createDate;
    private String status;
    private int memberCount;
    private int postCount;
    private InteractStatus interactStatus;

    public enum InteractStatus {
        UNJOINED, JOINED, MANAGER, LEADER, SENT
    }

    // Constructor
    public InteractGroupDTO() {
    }

    public InteractGroupDTO(int id, String name, String coverImage, String description, int createrId,
                    LocalDateTime createDate, String status, int memberCount, int postCount,
                    InteractStatus interactStatus) {
        this.id = id;
        this.name = name;
        this.coverImage = coverImage;
        this.description = description;
        this.createrId = createrId;
        this.createDate = createDate;
        this.status = status;
        this.memberCount = memberCount;
        this.postCount = postCount;
        this.interactStatus = interactStatus;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreaterId() {
        return createrId;
    }

    public void setCreaterId(int createrId) {
        this.createrId = createrId;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public InteractStatus getInteractStatus() {
        return interactStatus;
    }

    public void setInteractStatus(InteractStatus interactStatus) {
        this.interactStatus = interactStatus;
    }
}
