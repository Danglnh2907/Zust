package model;

public class ReqGroupDTO {
    private String groupName;
    private String groupDescription;
    private String coverImage;
    private int managerId;

    public ReqGroupDTO(String groupName, String groupDescription, String coverImage, int managerId) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.coverImage = coverImage;
        this.managerId = managerId;
    }

    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getGroupDescription() {
        return groupDescription;
    }
    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }
    public String getCoverImage() {
        return coverImage;
    }
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    public int getManagerId() {
        return managerId;
    }
    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }
}
