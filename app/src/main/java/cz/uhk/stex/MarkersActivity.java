package cz.uhk.stex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.uhk.stex.adapter.MarkersRecyclerAdapter;
import cz.uhk.stex.model.Group;
import cz.uhk.stex.model.Marker;
import cz.uhk.stex.util.ObservableList;
import cz.uhk.stex.util.Promise;

public class MarkersActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Marker> markerList;

    private MarkersRecyclerAdapter markersRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                reloadMarkers();
            }
        });

        showRefreshing();
        reloadMarkers();

    }

    private void reloadMarkers() {

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

        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Object>() {
            @Override
            public Object onSuccess(LoggedInUser result) throws Exception {

                Group group = result.getActiveGroup();

                final ObservableList<Marker> markers = Database.getGroupMarkers(group.getId());
                markers.addItemAddListener(new ObservableList.ItemAddListener<Marker>() {
                    @Override
                    public void onItemAdded(@NotNull ObservableList<Marker> list, @NotNull Collection<Marker> addedItems) {

                        for (Marker addedMarker : addedItems) {

                            boolean itemWasAdded = false;

                            for (int i = 0; i < markerList.size(); i++) {

                                Marker marker = markerList.get(i);

                                if (marker.getId().equals(addedMarker.getId())) {
                                    itemWasAdded = true;
                                    break;
                                }

                                if (addedMarker.getTitle().compareToIgnoreCase(marker.getTitle()) < 0) {
                                    markerList.add(i, addedMarker);
                                    itemWasAdded = true;
                                    break;
                                }
                            }

                            if (!itemWasAdded) {
                                markerList.add(addedMarker);
                            }
                        }

                        markersRecyclerAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                });

                return null;
            }
        });
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

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.action_marker_add:
                startActivity(new Intent(this, MarkerAddActivity.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}
