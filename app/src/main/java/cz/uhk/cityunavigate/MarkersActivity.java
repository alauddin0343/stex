package cz.uhk.cityunavigate;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.uhk.cityunavigate.adapter.FriendsRecyclerAdapter;
import cz.uhk.cityunavigate.adapter.MarkersRecyclerAdapter;
import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;

public class MarkersActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Marker> markerList;

    private MarkersRecyclerAdapter markersRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markers);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        markerList = new ArrayList<>();
        markersRecyclerAdapter = new MarkersRecyclerAdapter(this, markerList);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.markersMainContent);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(markersRecyclerAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
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
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }, 2500);
    }

    private void showRefreshing() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_markers, menu);
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
}
