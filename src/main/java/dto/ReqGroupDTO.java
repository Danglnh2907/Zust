package dto;

public class ReqGroupDTO {
    private String groupName;
    private String groupDescription;
    private String coverImage;

    public ReqGroupDTO(String groupName, String groupDescription, String coverImage) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.coverImage = coverImage;
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

}
