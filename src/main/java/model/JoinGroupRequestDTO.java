package model;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupRequestDTO {
    private JoinGroupRequest request;
    private Account account;
    private List<String> errors;

    public JoinGroupRequestDTO() {
        this.errors = new ArrayList<>();
    }

    public JoinGroupRequestDTO(JoinGroupRequest request, Account account) {
        this();
        this.request = request;
        this.account = account;
    }

    public JoinGroupRequest getRequest() {
        return request;
    }

    public void setRequest(JoinGroupRequest request) {
        this.request = request;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}