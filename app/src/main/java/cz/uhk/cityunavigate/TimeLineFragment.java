package cz.uhk.cityunavigate;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {

            ObservableList<Group> userGroups = Database.getUserGroups(firebaseUser);
            userGroups.addItemAddListener(new ObservableList.ItemAddListener<Group>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<Group> list, @NotNull Collection<Group> addedItems) {
                    for (Group group : addedItems) {

                        ObservableList<FeedItem> feedItems = Database.getGroupFeed(group.getId(), 10);

                        feedItems.addItemAddListener(new ObservableList.ItemAddListener<FeedItem>() {
                            @Override
                            public void onItemAdded(@NotNull ObservableList<FeedItem> list, @NotNull Collection<FeedItem> addedItems) {
                                feedsList.addAll(addedItems);
                                mRecyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });

            // TODO onItemsRemoved
        }

        return view;
    }

    //SWIPE REFRESH LAYOUT SETTINGS AND FUNCTIONS
    void refreshItems() {
        // Load items
        // ...

        Toast.makeText(getActivity(), "IN PROGRESS...", Toast.LENGTH_SHORT).show();

        // Load complete
        onItemsLoadComplete();
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
}
