package edu.byu.cs.tweeter.client.presenter;

import android.view.View;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class FeedPresenter {

    private static final int PAGE_SIZE = 10;

    private boolean hasMorePages;
    private boolean isLoading = false;
    private View view;
    private StatusService statusService;
    private UserService userService;
    private Status lastStatus;

    public FeedPresenter(View view) {
        this.view = view;
        statusService = new StatusService();
        userService = new UserService();
    }

    public interface View {
        void setLoadingFooter(boolean value);
        void addStatuses(List<Status> statuses);
        void displayMessage(String message);
        void startUserActivity(User user);
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
        statusService.loadMoreItems(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastStatus, new GetStatusObserver());
    }

    public void userInformation(String user) {
        userService.getUserInformation(Cache.getInstance().getCurrUserAuthToken(), user, new GetUserObserver());
    }

    private class GetStatusObserver implements StatusService.GetStatusObserver {

        @Override
        public void addStatuses(List<Status> statuses, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingFooter(false);
            lastStatus = (statuses.size() > 0) ? statuses.get(statuses.size() - 1) : null;
            view.addStatuses(statuses);
            FeedPresenter.this.hasMorePages = hasMorePages;
        }

        @Override
        public void displayErrorMessage(String message) {
            view.displayMessage(message);
        }

        @Override
        public void displayMessage(String message) {
            view.displayMessage(message);
        }

        @Override
        public void postStatus(String message) {

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
