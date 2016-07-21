package cz.uhk.cityunavigate;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import cz.uhk.cityunavigate.model.Comment;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.util.CommentListAdapter;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;

public class DetailActivity extends AppCompatActivity {

    private boolean mapInited = false;
    private MapView mapView;
    private GoogleMap map;
    private Marker myMarker;

    private TextView txtDetailTitle;
    private TextView txtDetailText;
    private ImageView imgDetailPic;
    private ListView lstComments;
    private EditText editCommentText;
    private Button btnSendComment;

    private ArrayList<Comment> commentsArray;
    private CommentListAdapter commentsAdapter;

    private String markerId;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markerId = getIntent().getStringExtra("id");
        groupId = getIntent().getStringExtra("groupid");

        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtDetailText = (TextView)findViewById(R.id.txtDetailText);
        txtDetailTitle = (TextView)findViewById(R.id.txtDetailTitle);
        imgDetailPic = (ImageView)findViewById(R.id.imgDetailPic);
        lstComments = (ListView)findViewById(R.id.lstComments);
        editCommentText = (EditText)findViewById(R.id.editCommentText);
        btnSendComment = (Button)findViewById(R.id.btnSendComment);

        mapView = (MapView)findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        //TODO if (imageIsMissing)
        imgDetailPic.setVisibility(View.GONE);

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = editCommentText.getText().toString().trim();
                if(s!= null && !s.isEmpty()){
                    sendComment(s);
                }

            }
        });

        commentsArray = new ArrayList<>();
        commentsAdapter = new CommentListAdapter(getApplicationContext(), R.layout.list_comment_row, commentsArray);
        lstComments.setAdapter(commentsAdapter);

        fillDetailInfo(markerId, groupId);
    }

    private void fillDetailInfo(final String markerId, final String groupId){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            Promise<Marker> marker = Database.getMarkerById(groupId, markerId);
            marker.success(new Promise.SuccessListener<Marker, Object>() {
                @Override
                public Object onSuccess(Marker result) {
                    txtDetailTitle.setText(result.getTitle());
                    txtDetailText.setText(result.getText());

                    fillComents(markerId);

                    myMarker = result;

                    return null;
                }
            });

        }
        else{
            Toast.makeText(this,"YOU'RE NOT LOGGED IN", Toast.LENGTH_SHORT).show();
        }
    }
    private void fillComents(String markerId){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){

            final ObservableList<Comment> comments = Database.getCommentsForMarker(markerId);
            comments.addItemAddListener(new ObservableList.ItemAddListener<Comment>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<Comment> list, @NotNull Collection<Comment> addedItems) {
                    commentsAdapter.addAll(addedItems);
                }
            });

        }
        else{
            Toast.makeText(this,"YOU'RE NOT LOGGED IN", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendComment(String s){
        Comment c = Comment.builder().withId(null).withCreated(System.currentTimeMillis())
                .withImage(null).withText(s).withUserId(FirebaseAuth.getInstance().getCurrentUser().getUid()).build();
        Database.addComment(markerId,c);

        commentsAdapter.add(c);
        editCommentText.setText("");

        FeedItem fi = FeedItem.builder().withId(null).withUserId(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .withGroupId(groupId).withMarkerId(markerId).withCreated(System.currentTimeMillis())
                .withType(FeedItem.Type.CommentAdd).withText(s).withTitle("Commented").withThumbnail(null)
                .build();
        Database.addFeedItem(groupId,fi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        if (id == R.id.action_show_marker_on_map_again_and_again){
            if(!mapInited)
                initMap();
            else
                mapView.setVisibility(View.VISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initMap(){
        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setBuildingsEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                        //WE DONT NEED AN INFO WINDOW ANYWAY
                        return false;
                    }
                });

                mapInited = true;

                mapView.setVisibility(View.VISIBLE);

                mapView.onResume();

                putMarker(myMarker);
            }
        });
    }

    private void putMarker(final cz.uhk.cityunavigate.model.Marker marker) { //TODO STILL IN DEPLOY

        map.addMarker(new MarkerOptions() //saving in List<Marker> to be able to clear only one from all possible markers
                .position(marker.getLocation())
                .title(marker.getTitle())
                .snippet(marker.getText()));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getLocation(), 16);
        map.moveCamera(cameraUpdate);
    }

    @Override
    public void onBackPressed() {
        if(mapView != null && mapView.getVisibility()==View.VISIBLE){
            mapView.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    //FOLLOWING METHODS ARE FOR MAPVIEW CONTROLLING (map fragment must have)
    @Override
    public void onResume() {
        if(mapInited)
            mapView.onResume();
        //METHOD FOR HIDING KEYBOARD AFTER ACTIVITY OPENS..
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mapInited)
            mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapInited)
            mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mapInited)
            mapView.onLowMemory();
    }

}
