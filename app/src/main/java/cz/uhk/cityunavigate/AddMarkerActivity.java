package cz.uhk.cityunavigate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Util;

public class AddMarkerActivity extends AppCompatActivity {

    private MapView mapView;
    private GoogleMap map;
    private com.google.android.gms.maps.model.Marker googleMapMarker;

    private EditText editName, editText;
    private Spinner spinnerCategory;
    private ArrayAdapter<Category> adapterCategory;
    private List<Category> categoriesArray;

    private Uri thumbnail = null;

    private void centreMapToLatLng(LatLng latLng){
        if(map != null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.moveCamera(cameraUpdate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        editName = (EditText)findViewById(R.id.editMarkerName);
        editText = (EditText)findViewById(R.id.editMarkerText);

        spinnerCategory = (Spinner) findViewById(R.id.spinnerCategories);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Category category = (Category) adapterView.getItemAtPosition(i);
                Toast.makeText(AddMarkerActivity.this, category.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        categoriesArray = new ArrayList<>();
        adapterCategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesArray);
        spinnerCategory.setAdapter(adapterCategory);

        ObservableList<Category> categories = Database.getAllCategories();
        categories.addItemAddListener(new ObservableList.ItemAddListener<Category>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<Category> list, @NotNull Collection<Category> addedItems) {
                for(Category c : addedItems){
                    if(!categoriesArray.contains(c)){
                        categoriesArray.add(c);
                        adapterCategory.notifyDataSetChanged();
                    }
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = (MapView) findViewById(R.id.mapview);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_marker_save) {

            sendMarker();

        } else if (id == R.id.action_marker_thumbnail) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Util.REQUEST_ACTIVITY_PICK_PHOTO);
        }

        return super.onOptionsItemSelected(item);
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

                final ProgressDialog progressDialog = Util.progressDialog(this, R.string.firebase_picture_uploading);
                progressDialog.show();

                try {
                    Util.uploadPicture(
                        getContentResolver(),
                        data.getData(),
                        "markers",
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(AddMarkerActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } ,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                thumbnail = Uri.parse(taskSnapshot.getMetadata().getReference().toString());
                            }
                        }
                    );
                } catch (IOException exception) {
                    Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void sendMarker() {

        // TODO load current group
        final String groupId = "-KMviPeXMiaorp1p49au";
        final Category c = (Category) spinnerCategory.getSelectedItem();
        //final Group g = ()

        final String title = ((EditText) findViewById(R.id.editMarkerName)).getText().toString();
        final String text = ((EditText) findViewById(R.id.editMarkerText)).getText().toString();

        Marker marker = cz.uhk.cityunavigate.model.Marker.builder()
                .withId(null)
                .withIdGroup(groupId)
                .withIdUserAuthor(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .withIdCategory(c.getId())
                .withLocation(googleMapMarker.getPosition())
                .withTitle(title)
                .withText(text)
                .withCreated(System.currentTimeMillis())
                .withImage(thumbnail)
                .build();

        Database.addMarker(groupId, marker).success(new Promise.SuccessListener<cz.uhk.cityunavigate.model.Marker, Void>() {

            @Override
            public Void onSuccess(cz.uhk.cityunavigate.model.Marker result) {

                FeedItem feedItem = FeedItem.builder()
                        .withId(null)
                        .withUserId(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .withGroupId(groupId)
                        .withMarkerId(result.getId())
                        .withCreated(System.currentTimeMillis())
                        .withType(FeedItem.Type.MarkerAdd)
                        .withText(text)
                        .withTitle("Added marker")
                        .withThumbnail(thumbnail)
                        .build();

                Database.addFeedItem(groupId, feedItem);
                finish();
                return null;
            }
        });

        thumbnail = null;
    }

}
