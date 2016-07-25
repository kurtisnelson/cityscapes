package com.thisisnotajoke.android.cityscape.layer;

import android.content.res.Resources;
import com.thisisnotajoke.android.cityscape.lib.R;

public class Nashville extends SimpleCity {

  public Nashville(Resources resources) {
    super(resources.getDrawable(R.drawable.nashville), resources);
  }
}
