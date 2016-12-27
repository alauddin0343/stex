package cz.uhk.stex.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import cz.uhk.stex.Database;
import cz.uhk.stex.DetailActivity;
import cz.uhk.stex.R;
import cz.uhk.stex.model.Category;
import cz.uhk.stex.model.FeedItem;
import cz.uhk.stex.model.Marker;
import cz.uhk.stex.model.User;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.Run;

/**
 * Created by petrw on 12.07.2016.
 */
public class TimelineRecyclerAdapter extends RecyclerView.Adapter<TimelineRecyclerAdapter.CustomViewHolder> {

    private Context context;

    private List<FeedItem> feedItemList;

    public TimelineRecyclerAdapter(Context context, List<FeedItem> feedItemList) {
        this.context = context;
        this.feedItemList = feedItemList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_timeline_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);
        customViewHolder.bindView(feedItem);
    }

    @Override
    public int getItemCount() {
        if (feedItemList != null) {
            return feedItemList.size();
        } else {
            return 0;
        }
    }

    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private FeedItem feedItem;

        private ImageView imgUser, imgImage;

        private TextView txtTitle, txtText, txtAuthor, txtDate;

        private View viewCategory;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgUser);
            imgImage = (ImageView) view.findViewById(R.id.imgImage);

            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtText = (TextView) view.findViewById(R.id.txtText);
            txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
            txtDate = (TextView) view.findViewById(R.id.txtDate);

            viewCategory = view.findViewById(R.id.viewCategory);

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
                    }).successFlat(Run.promiseUi((Activity) context, new Promise.SuccessListener<Bitmap, Void>() {
                        @Override
                        public Void onSuccess(Bitmap bitmap) {
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

                    Database.getCategoryById(result.getIdCategory())
                            .success(new Promise.SuccessListener<Category, Object>() {
                                @Override
                                public Object onSuccess(Category result) throws Exception {
                                    viewCategory.setBackgroundColor(Color.HSVToColor(150, new float[] { result.getHue(), 0.8f, 1f }));
                                    return null;
                                }
                            });

                    return null;
                }
            });

            if (feedItem.getThumbnail() != null) {
                Database.downloadImage(feedItem.getThumbnail())
                        .successFlat(Run.promiseUi((Activity) context, new Promise.SuccessListener<Bitmap, Void>() {
                            @Override
                            public Void onSuccess(Bitmap bitmap) {
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
            Intent myIntent = new Intent(context, DetailActivity.class);
            myIntent.putExtra("id", feedItem.getMarkerId());
            myIntent.putExtra("groupid", feedItem.getGroupId());
            context.startActivity(myIntent);
        }

    }

}
