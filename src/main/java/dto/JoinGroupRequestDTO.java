package dto;

import model.Group;
import model.JoinGroupRequest;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupRequestDTO {
    private JoinGroupRequest request;
//    private Account account;
    private Group group;
    private List<String> errors;

    public JoinGroupRequestDTO(JoinGroupRequest request, Group group) {
        this.request = request;
//        this.account = account;
        this.group = group;
        this.errors = new ArrayList<>();
    }

    public JoinGroupRequest getRequest() { return request; }
    public void setRequest(JoinGroupRequest request) { this.request = request; }
//    public Account getAccount() { return account; }
//    public void setAccount(Account account) { this.account = account; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public List<String> getErrors() { return errors; }
    public void addError(String error) { this.errors.add(error); }
}