package edu.byu.cs.tweeter.client.presenter;

import android.content.Intent;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.client.view.main.following.FollowingFragment;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter {

    private static final int PAGE_SIZE = 10;

    private View view;
    private FollowService followService;
    private UserService userService;
    private User lastFollowee;
    private boolean hasMorePages;
    private boolean isLoading = false;

    public interface View {
        void displayMessage(String message);
        void setLoadingFooter(boolean value);
        void addFollowees(List<User> followees);
        void startUserActivity(User user);
    }

    public FollowingPresenter(View view) {
        this.view = view;
        followService = new FollowService();
        userService = new UserService();
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void loadMoreItems(User user) {
        isLoading = true;
        view.setLoadingFooter(true);
        followService.loadMoreItems(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastFollowee, new GetFollowingObserver());
    }

    public void userInformation(String user) {
        followService.getUserInformation(Cache.getInstance().getCurrUserAuthToken(), user, new GetFollowingObserver());
    }

    private class GetFollowingObserver implements FollowService.GetFollowingObserver {

        @Override
        public void addFolowees(List<User> followees, boolean hasMorePages) {
            lastFollowee = (followees.size() > 0) ? followees.get(followees.size() - 1) : null;
            view.addFollowees(followees);
            view.setLoadingFooter(false);
            FollowingPresenter.this.hasMorePages = hasMorePages;
            isLoading = false;
        }

        @Override
        public void displayErrorMessage(String message) {
            view.displayMessage(message);
            view.setLoadingFooter(false);
            isLoading = false;
        }

        @Override
        public void startUserActivity(User user) {
            view.startUserActivity(user);
        }

        @Override
        public void displayMessage(String message) {
            view.displayMessage(message);
        }
    }
}
