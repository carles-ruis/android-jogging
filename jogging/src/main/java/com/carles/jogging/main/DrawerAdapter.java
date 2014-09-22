package com.carles.jogging.main;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.carles.jogging.R;

/**
 * Created by carles1 on 20/04/14.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerOption> {

    private final LayoutInflater inflater;

    public DrawerAdapter(Context context) {
        super(context, R.layout.item_drawer, DrawerOption.values());
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_drawer, parent, false);
        }

        final Context ctx = getContext();
        final DrawerOption item = getItem(position);

        final ImageView img = (ImageView) convertView.findViewById(R.id.img_item_icon);
        if (Build.VERSION.SDK_INT >= 16) {
            img.setImageAlpha(238); //#ee
        } else {
            img.setAlpha(238);
        }

        img.setImageDrawable(ctx.getResources().getDrawable(item.iconId));

        final TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_item_title);
        txtTitle.setText(ctx.getString(item.menuId));
//        txtTitle.setCompoundDrawablesWithIntrinsicBounds(item.iconId, 0, 0, 0);

        final TextView txtSubtitle = (TextView) convertView.findViewById(R.id.txt_item_subtitle);
        txtSubtitle.setText(ctx.getString(item.menuDescriptionId));

        return convertView;
    }
}
