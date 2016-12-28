package cz.uhk.stex.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.uhk.stex.Database;
import cz.uhk.stex.DetailActivity;
import cz.uhk.stex.R;
import cz.uhk.stex.model.Category;
import cz.uhk.stex.model.Marker;
import cz.uhk.stex.util.Promise;

/**
 * Created by Alzaq on 12.07.2016.
 */
public class MarkersRecyclerAdapter extends RecyclerView.Adapter<MarkersRecyclerAdapter.CustomViewHolder> {

    private Context mContext;

    private List<Marker> mMarkerList;

    public MarkersRecyclerAdapter(Context context, List<Marker> markerList) {
        this.mContext = context;
        this.mMarkerList = markerList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_marker_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Marker feedItem = mMarkerList.get(i);
        customViewHolder.bindView(feedItem);
    }

    @Override
    public int getItemCount() {
        return (null != mMarkerList ? mMarkerList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Marker marker;

        private ImageView imgUser;

        private TextView txtTitle, txtText;

        private View viewCategory;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgImage);

            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtText = (TextView) view.findViewById(R.id.txtText);

            viewCategory = view.findViewById(R.id.viewCategory);
            view.setOnClickListener(this);
        }

        public void bindView(final Marker marker) {

            this.marker = marker;

            //Setting text view title
            txtTitle.setText(marker.getTitle());
            txtText.setText(marker.getText());

            Database.getCategoryById(marker.getIdCategory())
                    .success(new Promise.SuccessListener<Category, Object>() {
                        @Override
                        public Object onSuccess(Category result) throws Exception {
                            viewCategory.setBackgroundColor(Color.HSVToColor(150, new float[] { result.getHue(), 0.8f, 1.0f }));
                            return null;
                        }
                    });

            if (marker.getImage() != null) {
                Database.downloadImage(marker.getImage())
                        .success(new Promise.SuccessListener<Bitmap, Object>() {
                            @Override
                            public Object onSuccess(Bitmap result) throws Exception {
                                imgUser.setVisibility(View.VISIBLE);
                                imgUser.setImageBitmap(result);
                                return null;
                            }
                        })
                        .error(new Promise.ErrorListener<Object>() {
                            @Override
                            public Object onError(Throwable error) {
                                imgUser.setVisibility(View.GONE);
                                return null;
                            }
                        });
            } else {
                imgUser.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            detailIntent.putExtra(DetailActivity.EXTRA_ID, marker.getId());
            detailIntent.putExtra(DetailActivity.EXTRA_GROUP_ID, marker.getIdGroup());
            mContext.startActivity(detailIntent);
        }

    }

}