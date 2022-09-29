package edu.byu.cs.tweeter.client.model.service;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.client.R;
import edu.byu.cs.tweeter.client.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFeedTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetStoryTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.backgroundTask.IsFollowerTask;
import edu.byu.cs.tweeter.client.backgroundTask.UnfollowTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.presenter.FollowersPresenter;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.client.view.main.feed.FeedFragment;
import edu.byu.cs.tweeter.client.view.main.following.FollowingFragment;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {

    public interface GetFollowingObserver {
        void addFolowees(List<User> followees, boolean hasMorePages);
        void displayErrorMessage(String message);
        void displayMessage(String message);
        void isFollower(boolean isFollower);
        void updateFollowingAndFollowers(boolean updateFollowButton);
        void setFollowerCount(int count);
        void setFolloweeCount(int count);
    }

    public void loadMoreItems(AuthToken currUserAuthToken, User user, int pageSize, User lastFollowee, GetFollowingObserver getFollowingObserver) {
        GetFollowingTask getFollowingTask = new GetFollowingTask(currUserAuthToken, user, pageSize, lastFollowee, new GetFollowingHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getFollowingTask);
    }

    public void loadMoreItemsFollowers(AuthToken currUserAuthToken, User user, int pageSize, User lastFollowee, GetFollowingObserver getFollowingObserver) {
        GetFollowersTask getFollowersTask = new GetFollowersTask(currUserAuthToken, user, pageSize, lastFollowee, new GetFollowersHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getFollowersTask);
    }

    public void isFollower(AuthToken currUserAuthToken, User currUser, User selectedUser, GetFollowingObserver getFollowingObserver) {
        IsFollowerTask isFollowerTask = new IsFollowerTask(currUserAuthToken, currUser, selectedUser, new IsFollowerHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(isFollowerTask);
    }

    public void unFollow(AuthToken currUserAuthToken, User selectedUser, GetFollowingObserver getFollowingObserver) {
        UnfollowTask unfollowTask = new UnfollowTask(currUserAuthToken, selectedUser, new UnfollowHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(unfollowTask);
    }

    public void follow(AuthToken currUserAuthToken, User selectedUser, GetFollowingObserver getFollowingObserver) {
        FollowTask followTask = new FollowTask(currUserAuthToken,
                selectedUser, new FollowHandler(getFollowingObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(followTask);
    }

    public void getFollowersCount(AuthToken currUserAuthToken, User selectedUser, GetFollowingObserver getFollowingObserver, ExecutorService executor) {
        // Get count of most recently selected user's followers.
        GetFollowersCountTask followersCountTask = new GetFollowersCountTask(currUserAuthToken, selectedUser, new GetFollowersCountHandler(getFollowingObserver));
        executor.execute(followersCountTask);
    }

    public void getFollowingCount(AuthToken currUserAuthToken, User selectedUser, GetFollowingObserver getFollowingObserver, ExecutorService executor) {
        // Get count of most recently selected user's followees (who they are following)
        GetFollowingCountTask followingCountTask = new GetFollowingCountTask(currUserAuthToken, selectedUser, new GetFollowingCountHandler(getFollowingObserver));
        executor.execute(followingCountTask);
    }
    
    /**
     * Message handler (i.e., observer) for GetFollowingTask.
     */
    private class GetFollowingHandler extends Handler {

        private GetFollowingObserver observer;

        public GetFollowingHandler (GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowingTask.SUCCESS_KEY);
            if (success) {
                List<User> followees = (List<User>) msg.getData().getSerializable(GetFollowingTask.FOLLOWEES_KEY);
                observer.addFolowees(followees, msg.getData().getBoolean(GetFollowingTask.MORE_PAGES_KEY));
            } else if (msg.getData().containsKey(GetFollowingTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowingTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to get following: " + message);
            } else if (msg.getData().containsKey(GetFollowingTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowingTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to get following because of exception: " + ex.getMessage());
            }
        }
    }

    /**
     * Message handler (i.e., observer) for GetFollowersTask.
     */
    private class GetFollowersHandler extends Handler {

        private GetFollowingObserver observer;

        public GetFollowersHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowersTask.SUCCESS_KEY);
            if (success) {
                List<User> followers = (List<User>) msg.getData().getSerializable(GetFollowersTask.FOLLOWERS_KEY);
                observer.addFolowees(followers, msg.getData().getBoolean(GetFollowersTask.MORE_PAGES_KEY));
            } else if (msg.getData().containsKey(GetFollowersTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowersTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to get followers: " + message);
            } else if (msg.getData().containsKey(GetFollowersTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowersTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to get followers because of exception: " + ex.getMessage());
            }
        }
    }

    // IsFollowerHandler

    private class IsFollowerHandler extends Handler {

        private GetFollowingObserver observer;

        public IsFollowerHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(IsFollowerTask.SUCCESS_KEY);
            if (success) {
                observer.isFollower(msg.getData().getBoolean(IsFollowerTask.IS_FOLLOWER_KEY));
            } else if (msg.getData().containsKey(IsFollowerTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(IsFollowerTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to determine following relationship: " + message);
            } else if (msg.getData().containsKey(IsFollowerTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(IsFollowerTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to determine following relationship because of exception: " + ex.getMessage());
            }
        }
    }

    // UnfollowHandler

    private class UnfollowHandler extends Handler {

        private GetFollowingObserver observer;

        public UnfollowHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(UnfollowTask.SUCCESS_KEY);
            if (success) {
                observer.updateFollowingAndFollowers(true);
            } else if (msg.getData().containsKey(UnfollowTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(UnfollowTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to unfollow: " + message);
            } else if (msg.getData().containsKey(UnfollowTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(UnfollowTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to unfollow because of exception: " + ex.getMessage());
            }
        }
    }

    // FollowHandler

    private class FollowHandler extends Handler {

        private GetFollowingObserver observer;

        public FollowHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(FollowTask.SUCCESS_KEY);
            if (success) {
                observer.updateFollowingAndFollowers(false);
            } else if (msg.getData().containsKey(FollowTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(FollowTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to follow: " + message);
            } else if (msg.getData().containsKey(FollowTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(FollowTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to follow because of exception: " + ex.getMessage());
            }
        }
    }

    // GetFollowersCountHandler

    private class GetFollowersCountHandler extends Handler {

        private GetFollowingObserver observer;

        public GetFollowersCountHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowersCountTask.SUCCESS_KEY);
            if (success) {
                int count = msg.getData().getInt(GetFollowersCountTask.COUNT_KEY);
                observer.setFollowerCount(count);
            } else if (msg.getData().containsKey(GetFollowersCountTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowersCountTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to get followers count: " + message);
            } else if (msg.getData().containsKey(GetFollowersCountTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowersCountTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to get followers count because of exception: " + ex.getMessage());
            }
        }
    }

    // GetFollowingCountHandler

    private class GetFollowingCountHandler extends Handler {

        private GetFollowingObserver observer;

        public GetFollowingCountHandler(GetFollowingObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowingCountTask.SUCCESS_KEY);
            if (success) {
                int count = msg.getData().getInt(GetFollowingCountTask.COUNT_KEY);
                observer.setFolloweeCount(count);
            } else if (msg.getData().containsKey(GetFollowingCountTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowingCountTask.MESSAGE_KEY);
                observer.displayErrorMessage("Failed to get following count: " + message);
            } else if (msg.getData().containsKey(GetFollowingCountTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowingCountTask.EXCEPTION_KEY);
                observer.displayErrorMessage("Failed to get following count because of exception: " + ex.getMessage());
            }
        }
    }
}
