package dto;

import java.time.LocalDateTime;

public class MemberDTO {
    private int id;
    private String name;
    private String avatar;
    private InteractStatus interactStatus;
    private LocalDateTime date;

    public enum InteractStatus {
        NORMAL, BLOCK, FRIEND, SELF
    }

    // Constructor
    public MemberDTO() {
    }

    public MemberDTO(int id, String name, String avatar, InteractStatus interactStatus, LocalDateTime date) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.interactStatus = interactStatus;
        this.date = date;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public InteractStatus getInteractStatus() {
        return interactStatus;
    }

    public void setInteractStatus(InteractStatus interactStatus) {
        this.interactStatus = interactStatus;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
