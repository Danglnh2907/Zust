package dto;
import model.Account;

import java.util.ArrayList;
import java.util.List;

public class ReqGroupDTO {
    private String groupName;
    private String groupDescription;
    private String coverImage;
    private List<Integer> managers;

    public ReqGroupDTO(String groupName, String groupDescription, String coverImage) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.coverImage = coverImage;
        this.managers = new ArrayList<Integer>();
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

    public List<Integer> getManagers() {
        return managers;
    }

    public void setManagers(List<Integer> managers) {
        this.managers = managers;
    }

    public void addManager(Integer manager) {
        this.managers.add(manager);
    }
}
