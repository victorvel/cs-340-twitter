package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.backgroundTask.PostStatusTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter {

    private static final String LOG_TAG = "MainActivity";

    private View view;
    private FollowService followService;
    private UserService userService;
    private StatusService statusService;

    public MainPresenter(View view) {
        this.view = view;
        followService = new FollowService();
        userService = new UserService();
        statusService = new StatusService();
    }

    public interface View {
        void displayMessage(String message);
        void isFollower(boolean isFollower);
        void updateFollowingAndFollowers(boolean updateFollowButton);
        void enableFollowButton(boolean setEnabled);
        void userLoggedOut();
        void displayLogOutToast(String message);
        void displayPostingToast(String message);
        void postStatus(String message);
        void setFollowerCount(int count);
        void setFolloweeCount(int count);
    }

    public void isFollower(User selectedUser) {
        followService.isFollower(Cache.getInstance().getCurrUserAuthToken(), Cache.getInstance().getCurrUser(), selectedUser, new GetFollowingObserver());
    }

    public void unFollow(boolean unFollow, User selectedUser) {
        view.enableFollowButton(false);
        if (unFollow) {
            followService.unFollow(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new GetFollowingObserver());
            view.displayMessage("Removing " + selectedUser.getName() + "...");
        }
        else {
            followService.follow(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new GetFollowingObserver());
            view.displayMessage("Adding " + selectedUser.getName() + "...");
        }
        view.enableFollowButton(true);
    }

    public void userLogOut() {
        view.displayLogOutToast("Logging Out...");
        userService.userLogOut(new GetUserObserver());
    }

    public void postStatus(String post) {
        view.displayPostingToast("Posting Status...");

        try {
            statusService.postStatus(post, Cache.getInstance().getCurrUserAuthToken(), Cache.getInstance().getCurrUser(), getFormattedDateTime(), parseURLs(post), parseMentions(post), new GetStatusObserver());
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            view.displayMessage("Failed to post the status because of exception: " + ex.getMessage()); // might be missing 'this' in the context
        }
    }

    public void updateSelectedUserFollowingAndFollowers(User selectedUser) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        followService.getFollowersCount(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new GetFollowingObserver(), executor);
        followService.getFollowingCount(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new GetFollowingObserver(), executor);
    }

    public String getFormattedDateTime() throws ParseException {
        SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat statusFormat = new SimpleDateFormat("MMM d yyyy h:mm aaa");

        return statusFormat.format(userFormat.parse(LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8)));
    }

    public List<String> parseURLs(String post) {
        List<String> containedUrls = new ArrayList<>();
        for (String word : post.split("\\s")) {
            if (word.startsWith("http://") || word.startsWith("https://")) {

                int index = findUrlEndIndex(word);

                word = word.substring(0, index);

                containedUrls.add(word);
            }
        }

        return containedUrls;
    }

    public List<String> parseMentions(String post) {
        List<String> containedMentions = new ArrayList<>();

        for (String word : post.split("\\s")) {
            if (word.startsWith("@")) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                word = "@".concat(word);

                containedMentions.add(word);
            }
        }

        return containedMentions;
    }

    public int findUrlEndIndex(String word) {
        if (word.contains(".com")) {
            int index = word.indexOf(".com");
            index += 4;
            return index;
        } else if (word.contains(".org")) {
            int index = word.indexOf(".org");
            index += 4;
            return index;
        } else if (word.contains(".edu")) {
            int index = word.indexOf(".edu");
            index += 4;
            return index;
        } else if (word.contains(".net")) {
            int index = word.indexOf(".net");
            index += 4;
            return index;
        } else if (word.contains(".mil")) {
            int index = word.indexOf(".mil");
            index += 4;
            return index;
        } else {
            return word.length();
        }
    }

    private class GetFollowingObserver implements FollowService.GetFollowingObserver {

        @Override
        public void addFolowees(List<User> followees, boolean hasMorePages) {

        }

        @Override
        public void displayErrorMessage(String message) {
            view.displayMessage(message);
        }

        @Override
        public void displayMessage(String message) {

        }

        @Override
        public void isFollower(boolean isFollower) {
            view.isFollower(isFollower);
        }

        @Override
        public void updateFollowingAndFollowers(boolean updateFollowButton) {
            view.updateFollowingAndFollowers(updateFollowButton);
        }

        @Override
        public void setFollowerCount(int count) {
            view.setFollowerCount(count);
        }

        @Override
        public void setFolloweeCount(int count) {
            view.setFolloweeCount(count);
        }
    }

    private class GetUserObserver implements UserService.GetUserObserver {

        @Override
        public void displayErrorMessage(String message) {

        }

        @Override
        public void userLoggedIn(User user, String name) {

        }

        @Override
        public void userRegistered(User user, String name) {

        }

        @Override
        public void userLoggedOut() {
            view.userLoggedOut();
        }

        @Override
        public void startUserActivity(User user) {

        }

        @Override
        public void displayMessage(String message) {

        }
    }

    private class GetStatusObserver implements StatusService.GetStatusObserver {

        @Override
        public void addStatuses(List<Status> statuses, boolean hasMorePages) {

        }

        @Override
        public void displayErrorMessage(String message) {

        }

        @Override
        public void displayMessage(String message) {

        }

        @Override
        public void postStatus(String message) {
            view.postStatus(message);
        }
    }
}
