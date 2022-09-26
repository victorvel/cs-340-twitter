package edu.byu.cs.tweeter.client.presenter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.view.login.RegisterFragment;
import edu.byu.cs.tweeter.model.domain.User;

public class RegisterPresenter implements UserService.GetUserObserver {

    private View view;
    private UserService userService;

    public interface View {
        void displayMessage(String message);
        void userRegistered(User registeredUser, String name);
        void displayRegisteredMessage(String message);
    }

    public RegisterPresenter(View view) {
        this.view = view;
        userService = new UserService();
    }

    public void registerUser(String firstName, String lastName, String alias, String password, Bitmap image) {
        view.displayRegisteredMessage("Registering...");

        // Convert image to byte array.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] imageBytes = bos.toByteArray();

        // Intentionally, Use the java Base64 encoder so it is compatible with M4.
        String imageBytesBase64 = Base64.getEncoder().encodeToString(imageBytes);

        userService.registerUser(firstName, lastName, alias, password, imageBytesBase64, this);
    }

    public void validateRegistration(Editable firstName, Editable lastName, Editable alias, Editable password, Drawable imageToUpload) {
        if (firstName.length() == 0) {
            throw new IllegalArgumentException("First Name cannot be empty.");
        }
        if (lastName.length() == 0) {
            throw new IllegalArgumentException("Last Name cannot be empty.");
        }
        if (alias.length() == 0) {
            throw new IllegalArgumentException("Alias cannot be empty.");
        }
        if (alias.charAt(0) != '@') {
            throw new IllegalArgumentException("Alias must begin with @.");
        }
        if (alias.length() < 2) {
            throw new IllegalArgumentException("Alias must contain 1 or more characters after the @.");
        }
        if (password.length() == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (imageToUpload == null) {
            throw new IllegalArgumentException("Profile image must be uploaded.");
        }
    }

    @Override
    public void displayErrorMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void userLoggedIn(User user, String name) {

    }

    @Override
    public void userRegistered(User user, String name) {
        view.userRegistered(user, name);
    }
}
