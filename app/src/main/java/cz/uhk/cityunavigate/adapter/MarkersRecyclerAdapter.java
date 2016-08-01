package cz.uhk.cityunavigate.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import cz.uhk.cityunavigate.Database;
import cz.uhk.cityunavigate.DetailActivity;
import cz.uhk.cityunavigate.R;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Function;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Run;

/**
 * Friends list
 */
public class MarkersRecyclerAdapter extends RecyclerView.Adapter<MarkersRecyclerAdapter.CustomViewHolder> {

    private Context context;

    private List<Marker> markerList;

    public MarkersRecyclerAdapter(Context context, List<Marker> markerList) {
        this.context = context;
        this.markerList = markerList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_friend_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Marker feedItem = markerList.get(i);
        customViewHolder.bindView(feedItem);
    }

    @Override
    public int getItemCount() {
        if (markerList != null) {
            return markerList.size();
        } else {
            return 0;
        }
    }

    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Marker feedItem;

        private ImageView imgUser;
        private TextView txtTitle, txtText;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgUser);

            txtTitle = (TextView) view.findViewById(R.id.txtName);
            txtText = (TextView) view.findViewById(R.id.txtEmail);
            view.setOnClickListener(this);
        }

        public void bindView(final Marker feedItem) {

            this.feedItem = feedItem;

            //Setting text view title
            txtTitle.setText(feedItem.getTitle());
            txtText.setText(feedItem.getText());

            imgUser.setImageBitmap(null);

            Date created = new Date();
            created.setTime(feedItem.getCreated());

//            Database.getUserById(feedItem.getId())
//                    .successFlat(new Promise.SuccessListener<User, Promise<Bitmap>>() {
//                        @Override
//                        public Promise<Bitmap> onSuccess(User user) {
//                            if (feedItem == CustomViewHolder.this.feedItem) {
//                                txtTitle.setText(user.getName());
//                                txtText.setText(user.getEmail());
//                            }
//                            return Database.downloadImage(user.getImage());
//                        }
//                    }).successFlat(Run.promiseUi((Activity) context, new Function<Bitmap, Void>() {
//                        @Override
//                        public Void apply(Bitmap bitmap) {
//                            if (feedItem == CustomViewHolder.this.feedItem) {
//                                imgUser.setImageBitmap(bitmap);
//                            }
//                            return null;
//                        }
//                    })).error(new Promise.ErrorListener<Void>() {
//                        @Override
//                        public Void onError(Throwable error) {
//                            Log.e("Bitmap", "Error loading bitmap", error);
//                            return null;
//                        }
//                    });
        }

        @Override
        public void onClick(View view) {

            Intent detailIntent = new Intent(context, DetailActivity.class);
            detailIntent.putExtra("id", feedItem.getId());
            detailIntent.putExtra("groupid", feedItem.getIdGroup());
            context.startActivity(detailIntent);
        }

    }

}
