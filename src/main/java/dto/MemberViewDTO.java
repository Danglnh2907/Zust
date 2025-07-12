package dto;

import model.Account;
import model.Participate;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class to transfer member data from the participate table.
 */
public class MemberViewDTO {
    private Participate participate;
    private Account account;
    private List<String> errors;

    public MemberViewDTO() {
        this.errors = new ArrayList<>();
    }

    public MemberViewDTO(Participate participate, Account account) {
        this();
        this.participate = participate;
        this.account = account;
    }

    public Participate getParticipate() {
        return participate;
    }

    public void setParticipate(Participate participate) {
        this.participate = participate;
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