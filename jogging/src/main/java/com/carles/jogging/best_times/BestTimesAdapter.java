package com.carles.jogging.best_times;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;

import java.util.List;

/**
 * Created by carles1 on 9/09/14.
 */
public class BestTimesAdapter extends ArrayAdapter<JoggingModel> {

    public BestTimesAdapter(Context ctx, List<JoggingModel> joggings) {
        super(ctx, R.layout.item_best_time, joggings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final JoggingModel jogging = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_best_time, parent, false);
            holder = new Holder();
            holder.txtDistance = (TextView) convertView.findViewById(R.id.txt_distance);
            holder.txtTime = (TextView) convertView.findViewById(R.id.txt_time);
            holder.txtDatetime = (TextView)convertView.findViewById(R.id.txt_datetime);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        holder.txtDistance.setText(new StringBuilder().append(jogging.getTotalDistance()).append(" m."));
        holder.txtTime.setText(FormatUtil.time(jogging.getTotalTime()));
        holder.txtDatetime.setText(FormatUtil.datetime(jogging.getId()));

        return convertView;
    }

    static class Holder {
        TextView txtDistance;
        TextView txtTime;
        TextView txtDatetime;
    }
}
