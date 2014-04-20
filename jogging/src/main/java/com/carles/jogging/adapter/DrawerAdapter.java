package com.carles.jogging.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.carles.jogging.R;

/**
 * Created by carles1 on 20/04/14.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerOption> {

    private final LayoutInflater inflater;

    public DrawerAdapter(Context context) {
        super(context, R.layout.drawer_item, DrawerOption.values());
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.drawer_item, parent, false);
        }

        final Context context = getContext();
        final DrawerOption item = getItem(position);

        final TextView text = (TextView) convertView.findViewById(R.id.drawer_item_text);
        text.setText(context.getString(item.menuId));
        text.setCompoundDrawablesWithIntrinsicBounds(item.iconId, 0, 0, 0);

        final TextView subtitle = (TextView) convertView.findViewById(R.id.drawer_item_subtitle);
        subtitle.setText(context.getString(item.menuDescriptionId));

        return convertView;
    }
}
