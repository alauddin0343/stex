package cz.uhk.stex.adapter;

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

import cz.uhk.stex.Database;
import cz.uhk.stex.R;
import cz.uhk.stex.model.Comment;
import cz.uhk.stex.model.User;
import cz.uhk.stex.util.Promise;
import cz.uhk.stex.util.Run;

/**
 * Created by Alzaq on 12.07.2016.
 */
public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.CustomViewHolder>{

    private Context mContext;

    private List<Comment> mCommentList;

    public CommentsRecyclerAdapter(Context context, List<Comment> commentList) {
        this.mContext = context;
        this.mCommentList = commentList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CustomViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_comment_row, null));
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Comment comment = mCommentList.get(i);
        customViewHolder.bindView(comment);
    }

    @Override
    public int getItemCount() {
        return (null != mCommentList ? mCommentList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
            view.setOnClickListener(this);
        }

        public void bindView(final Comment comment) {

            this.comment = comment;

            txtText.setText(comment.getText());

            imgUser.setImageResource(R.drawable.ic_person);
            imgImage.setImageBitmap(null);
            txtAuthor.setText("");

            Date created = new Date();
            created.setTime(comment.getCreated());
            txtDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(created));

            Database.getUserById(comment.getUserId()).successFlat(new Promise.SuccessListener<User, Promise<Bitmap>>() {
                @Override
                public Promise<Bitmap> onSuccess(User user) {
                    if (comment == CustomViewHolder.this.comment) {
                        txtAuthor.setText(user.getName());
                    }
                    return Database.downloadImage(user.getImage());
                }
            }).success(Run.promiseUi((Activity) mContext, new Promise.SuccessListener<Bitmap, Void>() {
                @Override
                public Void onSuccess(Bitmap bitmap) {
                    if (comment == CustomViewHolder.this.comment) {
                        if (bitmap != null) {
                            imgUser.setImageBitmap(bitmap);
                        }
                    }
                    return null;
                }
            }));

            if (comment.getImage() != null) {
                Database.downloadImage(comment.getImage())
                        .success(Run.promiseUi((Activity) mContext, new Promise.SuccessListener<Bitmap, Void>() {
                            @Override
                            public Void onSuccess(Bitmap bitmap) {
                                if (comment == CustomViewHolder.this.comment) {
                                    imgImage.setImageBitmap(bitmap);
                                }
                                return null;
                            }
                        }));
            } else {
                imgImage.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {

        }

    }

}