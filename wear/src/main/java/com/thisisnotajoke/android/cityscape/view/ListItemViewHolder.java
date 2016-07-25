package com.thisisnotajoke.android.cityscape.view;

import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thisisnotajoke.android.cityscape.R;


public class ListItemViewHolder extends WearableListView.ViewHolder {
  private TextView mTextView;
  private ImageView mCircle;

  public ListItemViewHolder(View itemView) {
    super(itemView);
    mTextView = (TextView) itemView.findViewById(R.id.name);
    mCircle = (ImageView) itemView.findViewById(R.id.circle);
  }

  public void setText(String text) {
    mTextView.setText(text);
  }

  public void setTag(Object tag) {
    itemView.setTag(tag);
  }

  public Object getTag() {
    return itemView.getTag();
  }
}
