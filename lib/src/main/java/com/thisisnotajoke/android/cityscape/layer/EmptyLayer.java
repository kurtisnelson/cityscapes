package com.thisisnotajoke.android.cityscape.layer;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.thisisnotajoke.android.cityscape.FaceLayer;

public class EmptyLayer extends FaceLayer {

  public EmptyLayer(Resources resources) {
    super(resources);
  }

  public EmptyLayer() {

  }

  @Override
  public void draw(Canvas canvas, Rect bounds) {

  }

  @Override
  public void onAmbientModeChanged(boolean inAmbientMode) {

  }
}
