package cz.uhk.cityunavigate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cz.uhk.cityunavigate.model.FeedItem;

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
        return new CustomViewHolder(view, feedItemList.get(i));
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);

        //Setting text view title
        customViewHolder.txtTitle.setText(feedItem.getTitle());
        customViewHolder.txtText.setText(feedItem.getText());
        customViewHolder.txtDate.setText("" + feedItem.getCreated());

        //customViewHolder.imgUser.setImageURI(feedItem.getThumbnail());
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private FeedItem feedItem;

        protected ImageView imgUser, imgImage;
        protected TextView txtTitle, txtText, txtAuthor, txtDate;

        public CustomViewHolder(View view, FeedItem feedItem) {
            super(view);

            this.feedItem = feedItem;

            this.imgUser = (ImageView) view.findViewById(R.id.imgUser);
            this.imgImage = (ImageView) view.findViewById(R.id.imgUser);

            this.txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            this.txtText = (TextView) view.findViewById(R.id.txtText);
            this.txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
            this.txtDate = (TextView) view.findViewById(R.id.txtDate);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(mContext, DetailActivity.class);
            myIntent.putExtra("id", feedItem.getId());
            mContext.startActivity(myIntent);
        }

    }

}
