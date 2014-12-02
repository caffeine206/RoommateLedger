package com.android.demo.roommateledger;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Derek on 11/29/2014.
 */
public class BalanceAdapter extends BaseAdapter {
    // store the context (as an inflated layout)
    private LayoutInflater inflater;
    // store the resource (typically list_item.xml)
    private int resource;
    private ArrayList<Roommate> roommates;

    public BalanceAdapter(Context context, int resource, ArrayList<Roommate> roommates) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resource = resource;
        this.roommates = roommates;
    }

    @Override
    public int getCount() {
        return roommates.size();
    }

    /**
     * Return an object in the data set.
     */
    public Object getItem(int position) {
        return this.roommates.get(position);
    }

    /**
     * Return the position provided.
     */
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        // bind the data to the view object
        return bindData(view, position);
    }


    /**
     * Bind the provided data to the view.
     * This is the only method not required by base adapter.
     */
    public View bindData(View view, int position) {
        // make sure it's worth drawing the view
        if (this.roommates.get(position) == null) {
            return view;
        }

        // pull out the object
        Roommate roommate = this.roommates.get(position);

        // extract the view object
        View viewElement = view.findViewById(R.id.roommate_text);
        // cast to the correct type
        TextView tv = (TextView)viewElement;
        // set the value
        tv.setText(roommate.name);

        viewElement = view.findViewById(R.id.balance_text);
        tv = (TextView)viewElement;
        tv.setText(String.valueOf(roommate.balance));

        // return the final view object
        return view;
    }
}
