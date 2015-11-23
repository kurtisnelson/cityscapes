package com.thisisnotajoke.android.cityscape.lib.layer;

import android.content.res.Resources;

import com.thisisnotajoke.android.cityscape.lib.R;

public class BayArea extends SimpleCity {
    public BayArea(Resources resources) {
        super(resources.getDrawable(R.drawable.bayarea), resources);
    }
}
