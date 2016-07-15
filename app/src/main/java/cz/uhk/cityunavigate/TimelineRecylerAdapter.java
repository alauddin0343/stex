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

/**
 * Created by petrw on 12.07.2016.
 */
public class TimelineRecylerAdapter extends RecyclerView.Adapter<TimelineRecylerAdapter.CustomViewHolder>{

    private List<FeedItem> feedItemList;
    private Context mContext;

    private MainActivity activity;

    public TimelineRecylerAdapter(Context context, List<FeedItem> feedItemList, MainActivity activity) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.activity = activity;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_timeline_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);
        //Setting text view title
        customViewHolder.txtTitle.setText(Html.fromHtml(feedItem.getTitle()));
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        protected ImageView imgTimeline;
        protected TextView txtTitle;
        protected TextView txtContent;

        public CustomViewHolder(View view) {
            super(view);
            this.txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            this.txtContent = (TextView) view.findViewById(R.id.txtContent);
            this.imgTimeline = (ImageView) view.findViewById(R.id.imageView);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(activity, DetailActivity.class);
            myIntent.putExtra("id","ídéčko objektu"); // TODO přidat ID z objektu
            activity.startActivity(myIntent);
            Toast.makeText(activity.getApplicationContext(), "Něco?", Toast.LENGTH_SHORT).show();
        }

    }

}
