package dto;

import model.Account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResGroupDTO {
    private int id;
    private String name;
    private String description;
    private String image;
    private LocalDateTime createDate;
    private List<Account> managers;
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
        this.managers = new ArrayList<Account>();
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

    public List<Account> getManagers() {
        return managers;
    }
    public void setManagers(List<Account> managers) {
        this.managers = managers;
    }
    public void addManager(Account manager) {
        this.managers.add(manager);
    }
}
