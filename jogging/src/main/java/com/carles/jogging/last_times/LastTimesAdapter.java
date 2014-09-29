package com.carles.jogging.last_times;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 10/09/14.
 */
public class LastTimesAdapter extends ArrayAdapter<JoggingModel> {

    private List<JoggingModel> joggings = new ArrayList<JoggingModel>();
    private SparseBooleanArray checked = new SparseBooleanArray();

    public LastTimesAdapter(Context ctx, List<JoggingModel> joggings) {
        super(ctx, R.layout.item_last_time, joggings);
        this.joggings = joggings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final JoggingModel jogging = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_last_time, parent, false);
            holder = new Holder();
            holder.txtTime = (TextView) convertView.findViewById(R.id.txt_time);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txt_date);
            holder.txtHour = (TextView) convertView.findViewById(R.id.txt_hour);

            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        if (checked.get(position)) {
            convertView.setBackgroundResource(R.drawable.item_checked_list_selector);
        } else {
            convertView.setBackgroundResource(R.drawable.item_list_selector);
        }

        holder.txtTime.setText(FormatUtil.time(jogging.getGoalTime()));
        holder.txtDate.setText(FormatUtil.date(jogging.getId()));
        holder.txtHour.setText(FormatUtil.timePattern(jogging.getId()));

        return convertView;
    }

    public void remove(int position) {
        joggings.remove(getItem(position));
    }

    public void setChecked(SparseBooleanArray checked) {
        this.checked = checked;
    }

    static class Holder {
        TextView txtTime;
        TextView txtDate;
        TextView txtHour;
    }
}
