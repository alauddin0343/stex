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
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder>{

    private List<FeedItem> feedItemList;
    private Context mContext;

    private MainActivity activity;

    public MyRecyclerAdapter(Context context, List<FeedItem> feedItemList, MainActivity activity) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.activity = activity;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);

        //Download image using picasso library
        /* //TODO
        Picasso.with(mContext).load(feedItem.getThumbnail())
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(customViewHolder.imageView);
        */
        //Setting text view title
        customViewHolder.textView.setText(Html.fromHtml(feedItem.getTitle()));
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        protected ImageView imageView;
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            //this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.textView = (TextView) view.findViewById(R.id.feed_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(activity, DetailActivity.class);
            activity.startActivity(myIntent);
            Toast.makeText(activity.getApplicationContext(), "Něco?", Toast.LENGTH_SHORT).show();
        }

    }

}
