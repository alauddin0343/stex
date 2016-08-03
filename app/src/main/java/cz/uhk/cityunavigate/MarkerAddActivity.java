package cz.uhk.cityunavigate;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.util.Function;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Run;
import cz.uhk.cityunavigate.util.Util;

public class MarkerAddActivity extends AppCompatActivity {

    private GoogleMap map;

    private com.google.android.gms.maps.model.Marker googleMapMarker;

    private List<Category> categoriesArray;

    private TextView txtMarkerTitle, txtMarkerText, txtMarkerPhoto, txtMarkerCategory;

    private ImageView imgMarkerPhoto;

    private MapView mapView;


    private String markerTitle, markerText, markerCategoryId, markerUserId, markerGroupId;

    private Uri markerThumbnail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtMarkerTitle = (TextView) findViewById(R.id.txtMarkerTitle);
        txtMarkerText = (TextView) findViewById(R.id.txtMarkerText);
        txtMarkerCategory = (TextView) findViewById(R.id.txtMarkerCategory);
        txtMarkerPhoto = (TextView) findViewById(R.id.txtMarkerPhoto);

        imgMarkerPhoto = (ImageView) findViewById(R.id.imgMarkerPhoto);

        mapView = (MapView) findViewById(R.id.mapView);

        // spinner
        categoriesArray = new ArrayList<>();
        Database.getAllCategories().addItemAddListener(new ObservableList.ItemAddListener<Category>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<Category> list, @NotNull Collection<Category> addedItems) {
                for (Category category : addedItems){
                    if (!categoriesArray.contains(category)) {
                        categoriesArray.add(category);
                    }
                }
            }
        });

        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Object>() {
            @Override
            public Object onSuccess(LoggedInUser result) throws Exception {
                markerUserId = result.getFirebaseUser().getUid();
                markerGroupId = result.getActiveGroup().getId();
                return null;
            }
        });

        // mapview
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMyLocationButtonEnabled(false);

                //map.setMyLocationEnabled(true);
                map.setBuildingsEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);

                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 10);
                //TODO - ADD CENTER TO MY POSITION
                centreMapToLatLng(new LatLng(22.336292, 114.173910));

                //PUT MARKER IN THE MIDDLE OF SCREEN AND IT IS FULLY DRAGGABLE OR YOU CAN CLICK SOMEWHERE TO CHANGE HIS LOC
                googleMapMarker = map.addMarker(new MarkerOptions().draggable(true)
                        .position(new LatLng(22.336292, 114.173910))
                );

                //MAP LISTENERS
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                        //just to disable info window
                        return false;
                    }
                });
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        googleMapMarker.setPosition(latLng);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_marker_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.action_marker_save:
                sendMarker();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void centreMapToLatLng(LatLng latLng){
        if(map != null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.moveCamera(cameraUpdate);
        }
    }


    //FOLLOWING METHODS ARE FOR MAPVIEW CONTROLLING (map fragment must have)
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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
                        "markers",
                        640
                    ).success(new Promise.SuccessListener<Uri, Object>() {
                        @Override
                        public Object onSuccess(Uri result) throws Exception {
                            markerThumbnail = result;

                            Database.downloadImage(markerThumbnail)
                                    .successFlat(Run.promiseUi(MarkerAddActivity.this, new Function<Bitmap, Void>() {
                                        @Override
                                        public Void apply(Bitmap bitmap) {
                                            imgMarkerPhoto.setImageBitmap(bitmap);
                                            imgMarkerPhoto.setVisibility(View.VISIBLE);
                                            txtMarkerPhoto.setVisibility(View.GONE);
                                            return null;
                                        }
                                    })).error(new Promise.ErrorListener<Void>() {
                                @Override
                                public Void onError(Throwable error) {
                                    Log.e("Bitmap", "Error loading thumbnail bitmap", error);
                                    return null;
                                }
                            });
                            return null;
                        }
                    });
                } catch (IOException exception) {
                    Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onMarkerSetTitleClick(View view) {

        final EditText editText = new EditText(this);
        editText.setText(markerTitle);

        new AlertDialog.Builder(this)
                .setTitle("Enter name")
                .setView(editText, 19, 8, 19, 8)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        markerTitle = editText.getText().toString();
                        txtMarkerTitle.setText(markerTitle);
                    }
                })
                .create()
                .show();

    }

    public void onMarkerSetTextClick(View view) {

        final EditText editText = new EditText(this);
        editText.setText(markerText);

        new AlertDialog.Builder(this)
                .setTitle("Enter description")
                .setView(editText, 19, 8, 19, 8)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        markerText = editText.getText().toString();
                        txtMarkerText.setText(markerText);
                    }
                })
                .create()
                .show();
    }

    public void onMarkerSetCategoryClick(View view) {

        int selectedCategoryIndex = 0;

        final String[] categoryNames = new String[categoriesArray.size()];
        for (int i = 0; i < categoriesArray.size(); i++) {
            Category category = categoriesArray.get(i);
            categoryNames[i] = category.getName();
            if (category.getId().equals(markerCategoryId)) {
                selectedCategoryIndex = i;
            }
        }

        AlertDialog alertDialog =
                new AlertDialog.Builder(this)
                .setTitle("Select category")
                .setSingleChoiceItems(categoryNames, selectedCategoryIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Category category = categoriesArray.get(which);
                        markerCategoryId = category.getId();

                        txtMarkerCategory.setText(category.getName());

                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        alertDialog.show();

    }

    public void onMarkerSetPhotoClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Util.REQUEST_ACTIVITY_PICK_PHOTO);
    }

    public void sendMarker() {

        if (markerTitle == null || markerText == null || markerCategoryId == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
        } else {

            Marker marker = cz.uhk.cityunavigate.model.Marker.builder()
                    .withId(null)
                    .withIdGroup(markerGroupId)
                    .withIdUserAuthor(markerUserId)
                    .withIdCategory(markerCategoryId)
                    .withLocation(googleMapMarker.getPosition())
                    .withTitle(markerTitle)
                    .withText(markerText)
                    .withCreated(System.currentTimeMillis())
                    .withImage(markerThumbnail)
                    .build();

            Database.addMarker(markerGroupId, marker).success(new Promise.SuccessListener<cz.uhk.cityunavigate.model.Marker, Object>() {

                @Override
                public Object onSuccess(cz.uhk.cityunavigate.model.Marker result) {

                    FeedItem feedItem = FeedItem.builder()
                            .withId(null)
                            .withUserId(markerUserId)
                            .withGroupId(markerGroupId)
                            .withMarkerId(result.getId())
                            .withCreated(System.currentTimeMillis())
                            .withType(FeedItem.Type.MarkerAdd)
                            .withText(markerText)
                            .withTitle("Added marker")
                            .withThumbnail(markerThumbnail)
                            .build();

                    Database.addFeedItem(markerGroupId, feedItem);
                    finish();
                    return null;
                }
            });
        }
    }

}
