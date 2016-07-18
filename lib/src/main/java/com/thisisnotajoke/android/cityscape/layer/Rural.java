package com.thisisnotajoke.android.cityscape.layer;

import android.content.res.Resources;
import com.thisisnotajoke.android.cityscape.R;

public class Rural extends SimpleCity {

  public Rural(Resources resources) {
    super(resources.getDrawable(R.drawable.rural), resources);
  }
}
