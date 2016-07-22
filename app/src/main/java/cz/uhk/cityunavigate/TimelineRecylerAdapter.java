package cz.uhk.cityunavigate;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Function;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Run;

/**
 * Created by petrw on 12.07.2016.
 */
public class TimelineRecylerAdapter extends RecyclerView.Adapter<TimelineRecylerAdapter.CustomViewHolder> {

    private Context mContext;
    private List<FeedItem> feedItemList;

    public TimelineRecylerAdapter(Context context, List<FeedItem> feedItemList) {
        this.mContext = context;
        this.feedItemList = feedItemList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_timeline_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);
        customViewHolder.bindView(feedItem);
    }

    public void runOnUiThred(Runnable runnable) {
        ((Activity) mContext).runOnUiThread(runnable);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private FeedItem feedItem;

        private ImageView imgUser, imgImage;
        private TextView txtTitle, txtText, txtAuthor, txtDate;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgUser);
            imgImage = (ImageView) view.findViewById(R.id.imgImage);

            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtText = (TextView) view.findViewById(R.id.txtText);
            txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            view.setOnClickListener(this);
        }

        public void bindView(final FeedItem feedItem) {

            this.feedItem = feedItem;

            //Setting text view title
            txtTitle.setText(feedItem.getTitle());
            txtText.setText(feedItem.getText());

            txtAuthor.setText("");
            imgUser.setImageBitmap(null);
            imgImage.setImageBitmap(null);

            Date created = new Date();
            created.setTime(feedItem.getCreated());
            txtDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(created));

            Database.getUserById(feedItem.getUserId())
                    .successFlat(new Promise.SuccessListener<User, Promise<Bitmap>>() {
                        @Override
                        public Promise<Bitmap> onSuccess(User user) {
                            if (feedItem == CustomViewHolder.this.feedItem)
                                txtAuthor.setText(user.getName());
                            return Database.downloadImage(user.getImage());
                        }
                    }).successFlat(Run.promiseUi((Activity) mContext, new Function<Bitmap, Void>() {
                        @Override
                        public Void apply(Bitmap bitmap) {
                            if (feedItem == CustomViewHolder.this.feedItem)
                                imgUser.setImageBitmap(bitmap);
                            return null;
                        }
                    })).error(new Promise.ErrorListener<Void>() {
                        @Override
                        public Void onError(Throwable error) {
                            Log.e("Bitmap", "Error loading bitmap", error);
                            return null;
                        }
                    });

            Database.getMarkerById(feedItem.getGroupId(), feedItem.getMarkerId()).success(new Promise.SuccessListener<Marker, Void>() {
                @Override
                public Void onSuccess(Marker result) {
                    txtTitle.setText(feedItem.getTitle() + " " + result.getTitle());
                    return null;
                }
            });

            if (feedItem.getThumbnail() != null) {
                Database.downloadImage(feedItem.getThumbnail())
                        .successFlat(Run.promiseUi((Activity) mContext, new Function<Bitmap, Void>() {
                            @Override
                            public Void apply(Bitmap bitmap) {
                                if (feedItem == CustomViewHolder.this.feedItem) {
                                    imgImage.setImageBitmap(bitmap);
                                    imgImage.setVisibility(View.VISIBLE);
                                }
                                return null;
                            }
                        })).error(new Promise.ErrorListener<Void>() {
                        @Override
                        public Void onError(Throwable error) {
                                Log.e("Bitmap", "Error loading thumbnail bitmap", error);
                                return null;
                            }
                        });
            } else {
                imgImage.setVisibility(View.GONE);
            }

            //customViewHolder.imgUser.setImageURI(feedItem.getThumbnail());

        }

        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(mContext, DetailActivity.class);
            myIntent.putExtra("id", feedItem.getMarkerId());
            myIntent.putExtra("groupid", feedItem.getGroupId());
            mContext.startActivity(myIntent);
        }

    }

}
