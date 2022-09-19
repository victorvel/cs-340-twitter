package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowersPresenter {

    private static final int PAGE_SIZE = 10;

    FollowService followService;
    View view;
    private boolean hasMorePages;
    private boolean isLoading = false;
    private User lastFollowee;

    public FollowersPresenter(View view) {
        this.view = view;
        followService = new FollowService();
    }

    public interface View {
        void displayMessage(String message);
        void startUserActivity(User user);
        void setLoadingFooter(boolean value);
        void addFollowees(List<User> followees);
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
        followService.loadMoreItemsFollowers(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastFollowee, new GetFollowersObserver());
    }

    public void userInformation(String user) {
        followService.getUserInformation(Cache.getInstance().getCurrUserAuthToken(), user, new GetFollowersObserver());
    }

    private class GetFollowersObserver implements FollowService.GetFollowingObserver {

        @Override
        public void addFolowees(List<User> followees, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingFooter(false);
            lastFollowee = (followees.size() > 0) ? followees.get(followees.size() - 1) : null;
            view.addFollowees(followees);
            FollowersPresenter.this.hasMorePages = hasMorePages;
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
