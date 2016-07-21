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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import cz.uhk.cityunavigate.model.Comment;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.util.CommentListAdapter;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;

public class DetailActivity extends AppCompatActivity {

    private MapView mapView;
    private GoogleMap map;

    private TextView txtDetailTitle;
    private TextView txtDetailText;
    private ImageView imgDetailPic;
    private ListView lstComments;
    private EditText editCommentText;
    private Button btnSendComment;

    private ArrayList<Comment> commentsArray;
    private CommentListAdapter commentsAdapter;

    private String markerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markerId = getIntent().getStringExtra("id");
        final String groupId = getIntent().getStringExtra("groupid");

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

        return super.onOptionsItemSelected(item);
    }

    //FOLLOWING METHODS ARE FOR MAPVIEW CONTROLLING (map fragment must have)
    @Override
    public void onResume() {
        if(mapView != null)
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
        if(mapView != null)
            mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null)
            mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mapView != null)
            mapView.onLowMemory();
    }

}
