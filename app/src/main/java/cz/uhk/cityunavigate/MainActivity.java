package cz.uhk.cityunavigate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.uhk.cityunavigate.adapter.TimelineRecyclerAdapter;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.util.ObservableList;

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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

        showRefreshing();
        reloadTimeLine();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }, 2500);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {

            // TODO groups
            ObservableList<Group> userGroups = Database.getUserGroups(firebaseUser);
            userGroups.addItemAddListener(new ObservableList.ItemAddListener<Group>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<Group> list, @NotNull Collection<Group> addedItems) {
                    for (Group group : addedItems) {
                        updateFeedItemsInTimeLine(group);
                    }
                }
            });

            // TODO onItemsRemoved
        }
    }

    private void updateFeedItemsInTimeLine(Group group) {

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
                }

                timelineRecylerAdapter.notifyDataSetChanged();
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
            startActivity(new Intent(this, AddMarkerActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_timeline) {
            showRefreshing();
            reloadTimeLine();

        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_group) {
            Intent intent = new Intent(this, GroupActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
