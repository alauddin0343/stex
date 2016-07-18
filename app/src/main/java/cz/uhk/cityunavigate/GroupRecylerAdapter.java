package cz.uhk.cityunavigate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cz.uhk.cityunavigate.model.Group;

/**
 * Created by petrw on 12.07.2016.
 */
public class GroupRecylerAdapter extends RecyclerView.Adapter<GroupRecylerAdapter.CustomViewHolder>{

    private List<Group> groupList;
    private MainActivity activity;

    public GroupRecylerAdapter(Context context, List<Group> groupList, MainActivity activity) {
        this.groupList = groupList;
        this.activity = activity;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_group_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Group group = groupList.get(i);
        //Setting text view title
        //customViewHolder.txtTitle.setText(Html.fromHtml(feedItem.getTitle()));
    }

    @Override
    public int getItemCount() {
        return (null != groupList ? groupList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        protected ImageView imgProfile1, imgProfile2, imgProfile3, imgProfile4, imgProfile5, imgNext;
        protected TextView txtGroupName;
        protected TextView txtAbout;

        public CustomViewHolder(View view) {
            super(view);
            //TODO - zde naplnit komponenty v layout
//            this.txtTitle = (TextView) view.findViewById(R.id.txtTitle);
//            this.txtContent = (TextView) view.findViewById(R.id.txtContent);
//            this.imgTimeline = (ImageView) view.findViewById(R.id.imageView);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent(activity, DetailActivity.class);
            myIntent.putExtra("id",/*groupList.get(getItemId()).getId()*/"ajdíčko"); // TODO přidat ID z objektu
            activity.startActivity(myIntent);
            Toast.makeText(activity.getApplicationContext(), "Něco?", Toast.LENGTH_SHORT).show();
        }

    }

}
