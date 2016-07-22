package cz.uhk.cityunavigate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import java.util.List;

import cz.uhk.cityunavigate.model.Category;

/**
 * Created by petrw on 22.07.2016.
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {

    private final Context context;

    public CategorySpinnerAdapter(Context context, int resource, List<Category> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null, true);

        ((CheckedTextView)rowView.findViewById(android.R.id.text1)).setText(getItem(position).getName());

        return rowView;
    }



    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        return super.getDropDownView(position, convertView, parent);
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null, true);

        ((CheckedTextView)rowView.findViewById(android.R.id.text1)).setText(getItem(position).getName());

        return rowView;
    }

    @Override
    public Category getItem(int position) {
        return super.getItem(position);
    }
}
