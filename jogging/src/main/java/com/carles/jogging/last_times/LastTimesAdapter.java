package com.carles.jogging.last_times;

import android.content.Context;
import android.provider.Telephony;
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
 * Created by carles1 on 10/09/14.
 */
public class LastTimesAdapter extends ArrayAdapter<JoggingModel> {

    public LastTimesAdapter(Context ctx, List<JoggingModel> joggings) {
        super(ctx, R.layout.item_last_time, joggings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final JoggingModel jogging = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_last_time, parent, false);
            holder = new Holder();
            holder.txtTime = (TextView) convertView.findViewById(R.id.txt_time);
            holder.txtDatetime = (TextView) convertView.findViewById(R.id.txt_datetime);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        holder.txtTime.setText(FormatUtil.time(jogging.getTotalTime()));
        holder.txtDatetime.setText(FormatUtil.datetime(jogging.getId()));

        return convertView;
    }

    static class Holder {
        TextView txtTime;
        TextView txtDatetime;
    }
}
