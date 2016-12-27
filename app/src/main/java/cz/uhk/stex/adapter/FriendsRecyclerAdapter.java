package cz.uhk.stex.adapter;

import android.app.Activity;
import android.content.Context;
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

import cz.uhk.stex.Database;
import cz.uhk.stex.R;
import cz.uhk.stex.model.User;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.Run;

/**
 * Friends list
 */
public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.CustomViewHolder> {

    private Context context;

    private List<User> feedItemList;

    public FriendsRecyclerAdapter(Context context, List<User> feedItemList) {
        this.context = context;
        this.feedItemList = feedItemList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_friend_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        User feedItem = feedItemList.get(i);
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

        private User feedItem;

        private ImageView imgUser;
        private TextView txtTitle, txtText;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgUser);

            txtTitle = (TextView) view.findViewById(R.id.txtName);
            txtText = (TextView) view.findViewById(R.id.txtEmail);
            view.setOnClickListener(this);
        }

        public void bindView(final User feedItem) {

            this.feedItem = feedItem;

            //Setting text view title
            txtTitle.setText(feedItem.getName());
            txtText.setText(feedItem.getEmail());

            imgUser.setImageBitmap(null);

            Date created = new Date();
            created.setTime(feedItem.getCreated());

            Database.getUserById(feedItem.getId())
                    .successFlat(new Promise.SuccessListener<User, Promise<Bitmap>>() {
                        @Override
                        public Promise<Bitmap> onSuccess(User user) {
                            if (feedItem == CustomViewHolder.this.feedItem) {
                                txtTitle.setText(user.getName());
                                txtText.setText(user.getEmail());
                            }
                            return Database.downloadImage(user.getImage());
                        }
                    }).successFlat(Run.promiseUi((Activity) context, new Promise.SuccessListener<Bitmap, Void>() {
                        @Override
                        public Void onSuccess(Bitmap bitmap) {
                            if (feedItem == CustomViewHolder.this.feedItem) {
                                imgUser.setImageBitmap(bitmap);
                            }
                            return null;
                        }
                    })).error(new Promise.ErrorListener<Void>() {
                        @Override
                        public Void onError(Throwable error) {
                            Log.e("Bitmap", "Error loading bitmap", error);
                            return null;
                        }
                    });
        }

        @Override
        public void onClick(View view) {

        }

    }

}
