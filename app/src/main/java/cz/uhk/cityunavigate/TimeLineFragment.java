package cz.uhk.cityunavigate;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.util.ObservableList;

public class TimeLineFragment extends Fragment {

    private SwipeRefreshLayout mSwipeRefresh;
    private List<FeedItem> feedsList;
    private RecyclerView mRecyclerView;
    private TimelineRecylerAdapter mRecyclerAdapter;

    private OnFragmentInteractionListener mListener;

    public TimeLineFragment() { }

    public static TimeLineFragment newInstance() {
        TimeLineFragment fragment = new TimeLineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_line, container, false);

        setHasOptionsMenu(true);

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerMainContent);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);

        feedsList = new ArrayList<>();

        mRecyclerAdapter = new TimelineRecylerAdapter(getActivity(), feedsList);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mSwipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefresh.setRefreshing(true);
            }
        });

        refreshItems();

        return view;
    }

    private void updateTimeLine(Group group) {

        final ObservableList<FeedItem> feedItems = Database.getGroupFeed(group.getId(), 50);
        feedItems.addItemAddListener(new ObservableList.ItemAddListener<FeedItem>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<FeedItem> list, @NotNull Collection<FeedItem> addedItems) {

                for (FeedItem addedItem : addedItems) {

                    boolean itemWasAdded = false;

                    for (int i = 0; i < feedsList.size(); i++) {

                        FeedItem feedItem = feedsList.get(i);

                        if (feedItem.getId().equals(addedItem.getId())) {
                            itemWasAdded = true;
                            break;
                        }

                        if (addedItem.getCreated() > feedItem.getCreated()) {
                            feedsList.add(i, addedItem);
                            itemWasAdded = true;
                            break;
                        }
                    }

                    if (!itemWasAdded) {
                        feedsList.add(addedItem);
                    }
                }

                mRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    //SWIPE REFRESH LAYOUT SETTINGS AND FUNCTIONS
    void refreshItems() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        }, 2500);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {

            ObservableList<Group> userGroups = Database.getUserGroups(firebaseUser);
            userGroups.addItemAddListener(new ObservableList.ItemAddListener<Group>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<Group> list, @NotNull Collection<Group> addedItems) {
                    for (Group group : addedItems) {
                        updateTimeLine(group);
                    }
                }
            });

            // TODO onItemsRemoved
        }
    }

    void onItemsLoadComplete() {

        // Stop refresh animation
        mSwipeRefresh.setRefreshing(false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_timeline, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//noinspection SimplifiableIfStatement

        int id = item.getItemId();

        if (id == R.id.action_marker_add){
            getActivity().startActivity(new Intent(getActivity(), AddMarkerActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
