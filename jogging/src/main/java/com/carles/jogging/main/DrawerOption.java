package com.carles.jogging.main;

import com.carles.jogging.R;

/**
 * Created by carles1 on 20/04/14.
 */
public enum DrawerOption {
    NEW(R.string.drawer_main, R.string.title_main, R.string.drawer_main_desc, R.drawable.ic_drawer_running_girl_selector),
    BEST_TIMES(R.string.drawer_best, R.string.title_best, R.string.drawer_best_desc, R.drawable.ic_drawer_best_selector),
    LAST_TIMES(R.string.drawer_last, R.string.title_last, R.string.drawer_last_desc, R.drawable.ic_drawer_history_selector);

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
