package com.thisisnotajoke.android.cityscape.layer;

import android.content.res.Resources;
import com.thisisnotajoke.android.cityscape.lib.R;

public class Atlanta extends SimpleCity {

  public Atlanta(Resources resources) {
    super(resources.getDrawable(R.drawable.atlanta), resources);
  }
}
