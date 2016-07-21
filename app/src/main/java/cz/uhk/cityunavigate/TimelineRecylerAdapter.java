package cz.uhk.cityunavigate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StreamDownloadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Promise;

/**
 * Created by petrw on 12.07.2016.
 */
public class TimelineRecylerAdapter extends RecyclerView.Adapter<TimelineRecylerAdapter.CustomViewHolder>{

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
        ((MainActivity) mContext).runOnUiThread(runnable);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

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

            Date created = new Date();
            created.setTime(feedItem.getCreated());
            txtDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(created));

            Database.getUserById(feedItem.getUserId()).success(new Promise.SuccessListener<User, Void>() {
                @Override
                public Void onSuccess(User result) {
                    txtAuthor.setText(result.getName());
                    new AsyncTask<String, Void, Void>() {
                        @Override
                        protected Void doInBackground(String... strings) {
                            FirebaseStorage.getInstance().getReferenceFromUrl(strings[0]).getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                                    final Bitmap bitmap = BitmapFactory.decodeStream(taskSnapshot.getStream());
                                    runOnUiThred(new Runnable() {
                                        @Override
                                        public void run() {
                                            imgUser.setImageBitmap(bitmap);
                                        }
                                    });
                                }
                            });
                            return null;
                        }
                    }.execute(result.getImage().toString());

                    return null;
                }
            });

            if (feedItem.getThumbnail() != null) {
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... strings) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(strings[0]).getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                                final Bitmap bitmap = BitmapFactory.decodeStream(taskSnapshot.getStream());
                                runOnUiThred(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgImage.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        });
                        return null;
                    }
                }.execute(feedItem.getThumbnail().toString());
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
