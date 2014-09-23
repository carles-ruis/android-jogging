package com.carles.jogging.result;

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
 * Created by carles1 on 20/04/14.
 */
public class PartialResultsAdapter extends ArrayAdapter<JoggingModel> {

    public PartialResultsAdapter(Context ctx, List<JoggingModel> partials) {
        super(ctx, R.layout.item_partial_result, partials);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final JoggingModel jogging = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_partial_result, parent, false);
            holder = new Holder();
            holder.txtCounter = (TextView) convertView.findViewById(R.id.txt_partial_counter);
            holder.txtTime = (TextView) convertView.findViewById(R.id.txt_partial_time);
            holder.txtDistance = (TextView) convertView.findViewById(R.id.txt_partial_distance);
            convertView.setTag(holder);

        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.txtCounter.setText(new StringBuilder().append(position+1).append("."));
        holder.txtTime.setText(FormatUtil.time(jogging.getGoalTime()));
        holder.txtDistance.setText(String.format("%5d", (int)jogging.getGoalDistance()));

        return convertView;
    }

    static class Holder {
        TextView txtCounter;
        TextView txtTime;
        TextView txtDistance;
    }
}
