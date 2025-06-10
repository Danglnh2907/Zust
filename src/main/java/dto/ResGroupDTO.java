package dto;

import java.time.LocalDateTime;

public class ResGroupDTO {
    private int id;
    private String name;
    private String description;
    private String image;
    private LocalDateTime createDate;
    private String status;
    private int numberParticipants;
    private int numberPosts;

    public ResGroupDTO() {}

    public ResGroupDTO(int id, String name, String description, String image, LocalDateTime createDate, String status, int numberParticipants, int numberPosts) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.createDate = createDate;
        this.status = status;
        this.numberParticipants = numberParticipants;
        this.numberPosts = numberPosts;
    }

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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
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
    public int getNumberParticipants() {
        return numberParticipants;
    }
    public void setNumberParticipants(int numberParticipants) {
        this.numberParticipants = numberParticipants;
    }
    public int getNumberPosts() {
        return numberPosts;
    }
    public void setNumberPosts(int numberPosts) {
        this.numberPosts = numberPosts;
    }
}
