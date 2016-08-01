package cz.uhk.cityunavigate;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.uhk.cityunavigate.adapter.FriendsRecyclerAdapter;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Promise;

public class FriendsActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<User> userList;

    private FriendsRecyclerAdapter friendsRecylerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        userList = new ArrayList<>();
        friendsRecylerAdapter = new FriendsRecyclerAdapter(this, userList);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.friendsMainContent);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(friendsRecylerAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadFriends();
            }
        });

        showRefreshing();
        reloadFriends();

    }

    private void reloadFriends() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }, 2500);

        LoggedInUser.get(this).successFlat(new Promise.SuccessListener<LoggedInUser, Promise<List<User>>>() {
            @Override
            public Promise<List<User>> onSuccess(LoggedInUser result) throws Exception {
                List<Promise<User>> userPromises = new ArrayList<>();
                for (String s : result.getActiveGroup().getUserIds()) {
                    userPromises.add(Database.getUserById(s));
                }
                return Promise.all(userPromises);
            }
        }).success(new Promise.SuccessListener<List<User>, Void>() {
            @Override
            public Void onSuccess(List<User> users) throws Exception {
                updateFeedItemsInTimeLine(users);
                return null;
            }
        }).error(new Promise.ErrorListener<Void>() {
            @Override
            public Void onError(Throwable error) {
                Log.e("friends", "Error loading friends", error);
                return null;
            }
        });
    }

    private void updateFeedItemsInTimeLine(List<User> users) {
        userList.clear();
        userList.addAll(users);
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return u1.getName().compareTo(u2.getName());
            }
        });

        friendsRecylerAdapter.notifyDataSetChanged();
    }

    private void showRefreshing() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }
}
