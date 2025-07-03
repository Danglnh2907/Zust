package dto;

import model.Account;
import model.FeedbackGroup;

import java.util.ArrayList;
import java.util.List;

public class FeedbackGroupDTO {
    private FeedbackGroup feedback;
    private Account account;
    private List<String> errors;

    public FeedbackGroupDTO() {
        this.errors = new ArrayList<>();
    }

    public FeedbackGroupDTO(FeedbackGroup feedback, Account account) {
        this();
        this.feedback = feedback;
        this.account = account;
    }

    public FeedbackGroup getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackGroup feedback) {
        this.feedback = feedback;
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