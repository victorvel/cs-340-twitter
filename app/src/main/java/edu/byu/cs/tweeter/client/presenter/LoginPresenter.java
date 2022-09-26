package edu.byu.cs.tweeter.client.presenter;

import android.text.Editable;

import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.User;

public class LoginPresenter implements UserService.GetUserObserver {

    private View view;
    private UserService userService;

    public LoginPresenter(View view) {
        this.view = view;
        userService = new UserService();
    }

    public interface View {
        void displayMessage(String message);
        void userLoggedIn(User user, String name);
        void userRegistered(User user, String name);
        void displayLoginMessage(String message);
    }

    public void userLogin(String username, String password) {
        view.displayLoginMessage("Logging In...");
        userService.userLogin(username, password, this);
    }

    public void validateLogin(Editable username, Editable password) {
        if (username.charAt(0) != '@') {
            throw new IllegalArgumentException("Alias must begin with @.");
        }
        if (username.length() < 2) {
            throw new IllegalArgumentException("Alias must contain 1 or more characters after the @.");
        }
        if (password.length() == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
    }

    @Override
    public void displayErrorMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void userLoggedIn(User user, String name) {
        view.userLoggedIn(user, name);
    }

    @Override
    public void userRegistered(User user, String name) {
        view.userRegistered(user, name);
    }
}
