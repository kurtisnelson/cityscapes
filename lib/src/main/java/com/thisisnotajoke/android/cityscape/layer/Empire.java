package com.thisisnotajoke.android.cityscape.layer;

import android.content.res.Resources;

import com.thisisnotajoke.android.cityscape.R;

public class Empire extends SimpleCity {
    public Empire(Resources resources) {
        super(resources.getDrawable(R.drawable.empire), resources);
    }
}
