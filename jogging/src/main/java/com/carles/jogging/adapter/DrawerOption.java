package com.carles.jogging.adapter;

import com.carles.jogging.R;

/**
 * Created by carles1 on 20/04/14.
 */
public enum DrawerOption {
    NEW(R.string.menu_new, R.string.title_new, R.string.menu_description_new, R.drawable.ic_menu_new),
    BEST_TIMES(R.string.menu_best, R.string.title_best, R.string.menu_description_best, R.drawable.ic_menu_best),
    LAST_TIMES(R.string.menu_last, R.string.title_last, R.string.menu_description_last, R.drawable.ic_menu_last);

    public final int menuId;
    public final int titleId;
    public final int menuDescriptionId;
    public final int iconId;

    DrawerOption(int menuId, int titleId, int menuDescriptionId, int iconId) {
        this.menuId = menuId;
        this.titleId = titleId;
        this.menuDescriptionId = menuDescriptionId;
        this.iconId = iconId;
    }
}
