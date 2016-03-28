package com.thisisnotajoke.android.cityscape.lib.layer;

import android.content.res.Resources;

import com.thisisnotajoke.android.cityscape.lib.R;

public class Empire extends SimpleCity {
    public Empire(Resources resources) {
        super(resources.getDrawable(R.drawable.empire), resources);
    }
}
