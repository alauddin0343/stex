package cz.uhk.stex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import cz.uhk.stex.adapter.CommentsRecyclerAdapter;
import cz.uhk.stex.model.Comment;
import cz.uhk.stex.model.FeedItem;
import cz.uhk.stex.model.Marker;
import cz.uhk.stex.util.ObservableList;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.Run;
import cz.uhk.stex.util.Util;

public class DetailActivity extends AppCompatActivity {

    private boolean mapInited = false;
    private MapView mapView;
    private GoogleMap map;
    private Marker myMarker;

    //private TextView txtDetailTitle;
    private TextView txtDetailText;
    private RecyclerView recyclerView;
    private EditText editCommentText;

    private ImageView imgCommentPhoto;

    private ArrayList<Comment> commentsArray;
    private CommentsRecyclerAdapter commentsAdapter;

    private String markerId;
    private String groupId;

    private Uri thumbnail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        markerId = getIntent().getStringExtra("id");
        groupId = getIntent().getStringExtra("groupid");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtDetailText = (TextView)findViewById(R.id.txtDetailText);
        //txtDetailTitle = (TextView)findViewById(R.id.txtDetailTitle);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerComments);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);


        editCommentText = (EditText)findViewById(R.id.editCommentText);
        imgCommentPhoto = (ImageView) findViewById(R.id.imgCommentPhoto);

        mapView = (MapView)findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        commentsArray = new ArrayList<>();
        commentsAdapter = new CommentsRecyclerAdapter(this, commentsArray);
        recyclerView.setAdapter(commentsAdapter);
        fillDetailInfo(markerId, groupId);
    }

    private void fillDetailInfo(final String markerId, final String groupId){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            Promise<Marker> marker = Database.getMarkerById(groupId, markerId);
            marker.success(new Promise.SuccessListener<Marker, Object>() {
                @Override
                public Object onSuccess(Marker result) {

                    ((CollapsingToolbarLayout)findViewById(R.id.toolbar_layout)).setTitle(result.getTitle());
                    final ImageView imageView = (ImageView) findViewById(R.id.toolbar_layout).findViewById(R.id.header);

                    if (result.getImage() != null) {
                        Database.downloadImage(result.getImage())
                            .success(Run.promiseUi(DetailActivity.this, new Promise.SuccessListener<Bitmap, Void>() {
                                @Override
                                public Void onSuccess(Bitmap bitmap) {
                                    imageView.setImageBitmap(bitmap);
                                    return null;
                                }
                            }));
                    }

                    txtDetailText.setText(result.getText());

                    fillComments(markerId);

                    myMarker = result;

                    return null;
                }
            });

        }
        else{
            Toast.makeText(this,"YOU'RE NOT LOGGED IN", Toast.LENGTH_SHORT).show();
        }
    }
    private void fillComments(String markerId){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            final ObservableList<Comment> comments = Database.getCommentsForMarker(markerId);
            comments.addItemAddListener(new ObservableList.ItemAddListener<Comment>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<Comment> list, @NotNull Collection<Comment> addedItems) {
                    for (Comment addedItem : addedItems) {

                        boolean itemWasAdded = false;

                        for (int i = 0; i < commentsArray.size(); i++) {

                            Comment commentItem = commentsArray.get(i);

                            if (commentItem.getId().equals(addedItem.getId())) {
                                itemWasAdded = true;
                                break;
                            }

                            if (addedItem.getCreated() > commentItem.getCreated()) {
                                commentsArray.add(i, addedItem);
                                itemWasAdded = true;
                                break;
                            }
                        }

                        if (!itemWasAdded) {
                            commentsArray.add(addedItem);
                        }
                    }

                    commentsAdapter.notifyDataSetChanged();
                }
            });

        }
        else{
            Toast.makeText(this,"YOU'RE NOT LOGGED IN", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.action_show_marker_on_map_again_and_again:
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("detail", markerId);
                startActivity(intent);
                break;

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

    private void putMarker(final cz.uhk.stex.model.Marker marker) { //TODO STILL IN DEPLOY

        map.addMarker(new MarkerOptions() //saving in List<Marker> to be able to clear only one from all possible markers
                .position(marker.getLocation())
                .title(marker.getTitle())
                .snippet(marker.getText()));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getLocation(), 16);
        map.moveCamera(cameraUpdate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_ACTIVITY_PICK_PHOTO) {

            if (resultCode == RESULT_OK) {

                try {
                    Util.uploadPicture(
                            this,
                            getContentResolver(),
                            data.getData(),
                            "comments",
                            640,
                            new Util.BitmapPictureResizer() {
                                @Override
                                public void onBitmapPictureResized(Bitmap bitmap) {
                                    imgCommentPhoto.setImageBitmap(bitmap);
                                    imgCommentPhoto.setVisibility(View.VISIBLE);
                                }
                            }
                    ).success(new Promise.SuccessListener<Uri, Object>() {
                        @Override
                        public Object onSuccess(Uri result) throws Exception {
                            thumbnail = result;
                            return null;
                        }
                    });
                } catch (IOException exception) {
                    Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
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

    public void onAddPhotoButtonClick(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Util.REQUEST_ACTIVITY_PICK_PHOTO);
    }

    public void onSendCommentButtonClick(View view) {

        final String text = editCommentText.getText() == null ? null : editCommentText.getText().toString().trim();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        if (text !=  null && !text.isEmpty()) {

            Comment comment = Comment.builder()
                    .withId(null)
                    .withCreated(System.currentTimeMillis())
                    .withImage(thumbnail)
                    .withText(text)
                    .withUserId(user.getUid())
                    .build();

            Database.addComment(markerId, comment).success(new Promise.SuccessListener<Comment, Object>() {
                @Override
                public Object onSuccess(Comment result) {

                    editCommentText.setText("");
                    imgCommentPhoto.setVisibility(View.GONE);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editCommentText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    FeedItem feedItem = FeedItem.builder()
                            .withId(null)
                            .withUserId(user.getUid())
                            .withGroupId(groupId)
                            .withMarkerId(markerId)
                            .withCreated(System.currentTimeMillis())
                            .withType(FeedItem.Type.CommentAdd)
                            .withText(text)
                            .withTitle("Commented")
                            .withThumbnail(thumbnail)
                            .withReadBy(new HashMap<String, Long>() {{
                                put(user.getUid(), System.currentTimeMillis());
                            }})
                            .build();

                    Database.addFeedItem(groupId, feedItem);

                    thumbnail = null;

                    Toast.makeText(DetailActivity.this, R.string.detail_comment_added, Toast.LENGTH_SHORT).show();

                    return null;
                }
            });
        }
    }

}
