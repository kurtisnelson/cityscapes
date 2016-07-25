package com.thisisnotajoke.android.cityscape.controller;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.thisisnotajoke.android.cityscape.R;
import com.thisisnotajoke.android.cityscape.view.ListItemViewHolder;

final class ListViewAdapter extends WearableListView.Adapter {
  private String[] mDataset;
  private final LayoutInflater mInflater;

  ListViewAdapter(Context context, String[] dataset) {
    mInflater = LayoutInflater.from(context);
    mDataset = dataset;
  }

  @Override
  public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
    return new ListItemViewHolder(mInflater.inflate(R.layout.list_item, null));
  }

  @Override
  public void onBindViewHolder(WearableListView.ViewHolder holder,
                               int position) {
    ListItemViewHolder itemHolder = (ListItemViewHolder) holder;
    itemHolder.setText(mDataset[position]);
    itemHolder.setTag(position);
  }

  @Override
  public int getItemCount() {
    return mDataset.length;
  }
}
