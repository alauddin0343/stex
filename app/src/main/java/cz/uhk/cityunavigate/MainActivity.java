package cz.uhk.cityunavigate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.uhk.cityunavigate.adapter.TimelineRecyclerAdapter;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.services.NotificationService;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private SwipeRefreshLayout swipeRefreshLayout;

    private List<FeedItem> feedItemList;

    private RecyclerView recyclerView;

    private TimelineRecyclerAdapter timelineRecylerAdapter;

    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        feedItemList = new ArrayList<>();
        timelineRecylerAdapter = new TimelineRecyclerAdapter(this, feedItemList);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerMainContent);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(timelineRecylerAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadTimeLine();
            }
        });

        setLoggedInUser();

        startService(new Intent(this, NotificationService.class));

        showRefreshing();
        reloadTimeLine();
    }

    private void setLoggedInUser() {

        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Object>() {
            @Override
            public Object onSuccess(LoggedInUser result) throws Exception {
                ((TextView) findViewById(R.id.textViewDrawerUserName)).setText(result.getUser().getName());
                ((TextView) findViewById(R.id.textViewDrawerGroupName)).setText(result.getActiveGroup().getName());

                result.addUserChangeListener(new LoggedInUser.UserChangeListener() {
                    @Override
                    public void userChanged(LoggedInUser newUser) {
                        feedItemList.clear();
                        timelineRecylerAdapter.notifyDataSetChanged();
                        showRefreshing();
                        reloadTimeLine();
                    }
                });

                return null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLoggedInUser();
    }

    private void showRefreshing() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void reloadTimeLine() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }, 5000);

        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Void>() {
            @Override
            public Void onSuccess(LoggedInUser user) throws Exception {
                updateFeedItemsInTimeLine(user, user.getActiveGroup());
                return null;
            }
        });
    }

    private void updateFeedItemsInTimeLine(final LoggedInUser user, Group group) {

        final ObservableList<FeedItem> feedItems = Database.getGroupFeed(group.getId(), Integer.MAX_VALUE);
        feedItems.addItemAddListener(new ObservableList.ItemAddListener<FeedItem>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<FeedItem> list, @NotNull Collection<FeedItem> addedItems) {

                for (FeedItem addedItem : addedItems) {

                    boolean itemWasAdded = false;

                    for (int i = 0; i < feedItemList.size(); i++) {

                        FeedItem feedItem = feedItemList.get(i);

                        if (feedItem.getId().equals(addedItem.getId())) {
                            itemWasAdded = true;
                            break;
                        }

                        if (addedItem.getCreated() > feedItem.getCreated()) {
                            feedItemList.add(i, addedItem);
                            itemWasAdded = true;
                            break;
                        }
                    }

                    if (!itemWasAdded) {
                        feedItemList.add(addedItem);
                    }

                    if (!addedItem.getReadBy().containsKey(user.getUser().getId()))
                        Database.markFeedItemAsRead(addedItem, user.getUser().getId());
                }

                timelineRecylerAdapter.notifyDataSetChanged();
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_marker_add) {
            startActivity(new Intent(this, MarkerAddActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_timeline) {
            showRefreshing();
            reloadTimeLine();
        } else if (id == R.id.nav_markers) {
            Intent intent = new Intent(this, MarkersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_friends) {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
