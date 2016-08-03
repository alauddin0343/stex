package cz.uhk.cityunavigate.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.uhk.cityunavigate.Database;
import cz.uhk.cityunavigate.R;
import cz.uhk.cityunavigate.model.Comment;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Run;

/**
 * Created by petrw on 12.07.2016.
 */
public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.CustomViewHolder>{

    private Context mContext;
    private List<Comment> commentList;

    public CommentsRecyclerAdapter(Context context, List<Comment> commentList) {
        this.mContext = context;
        this.commentList = commentList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CustomViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_comment_row, null));
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Comment comment = commentList.get(i);
        customViewHolder.bindView(comment);
    }

    public void runOnUiThred(Runnable runnable) {
        ((Activity) mContext).runOnUiThread(runnable);
    }

    @Override
    public int getItemCount() {
        return (null != commentList ? commentList.size() : 0);
    }


    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private Comment comment;

        private ImageView imgUser, imgImage;
        private TextView txtText, txtAuthor, txtDate;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgUser);
            imgImage = (ImageView) view.findViewById(R.id.imgImage);

            txtText = (TextView) view.findViewById(R.id.txtText);
            txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            //view.setOnClickListener(this);
        }

        public void bindView(final Comment comment) {

            this.comment = comment;

            //Setting text view title
            txtText.setText(comment.getText());

            imgUser.setImageBitmap(null);
            imgImage.setImageBitmap(null);
            txtAuthor.setText("");

            Date created = new Date();
            created.setTime(comment.getCreated());
            txtDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(created));

            Database.getUserById(comment.getUserId()).successFlat(new Promise.SuccessListener<User, Promise<Bitmap>>() {
                @Override
                public Promise<Bitmap> onSuccess(User user) {
                    if (comment == CustomViewHolder.this.comment)
                        txtAuthor.setText(user.getName());
                    return Database.downloadImage(user.getImage());
                }
            }).success(Run.promiseUi((Activity) mContext, new Promise.SuccessListener<Bitmap, Void>() {
                @Override
                public Void onSuccess(Bitmap bitmap) {
                    if (comment == CustomViewHolder.this.comment)
                        imgUser.setImageBitmap(bitmap);
                    return null;
                }
            }));

            if (comment.getImage() != null) {
                Database.downloadImage(comment.getImage())
                        .success(Run.promiseUi((Activity) mContext, new Promise.SuccessListener<Bitmap, Void>() {
                            @Override
                            public Void onSuccess(Bitmap bitmap) {
                                if (comment == CustomViewHolder.this.comment)
                                    imgImage.setImageBitmap(bitmap);
                                return null;
                            }
                        }));
            } else {
                imgImage.setVisibility(View.GONE);
            }

            //customViewHolder.imgUser.setImageURI(feedItem.getThumbnail());

        }

        @Override
        public void onClick(View view) {

        }

    }

}
