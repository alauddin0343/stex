package cz.uhk.cityunavigate.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cz.uhk.cityunavigate.R;
import cz.uhk.cityunavigate.model.Comment;

/**
 * Created by petrw on 21.07.2016.
 */
public class CommentListAdapter extends ArrayAdapter<Comment> {

    Context context;
    public CommentListAdapter(Context context, int resource, ArrayList<Comment> commentsArray) {
        super(context, resource, commentsArray);
        this.context = context;
    }

    @Override
    public Comment getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.list_comment_row, null, true);

        Comment comment = getItem(position);

        //((TextView)rowView.findViewById(R.id.txtCommentAuthor)).setText(comment.get());
        ((TextView)rowView.findViewById(R.id.txtCommentText)).setText(comment.getText());

        return rowView;
    }
}
