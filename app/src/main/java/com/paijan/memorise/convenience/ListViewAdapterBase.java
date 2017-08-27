package com.paijan.memorise.convenience;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ListViewAdapterBase extends BaseAdapter {
	public static final int TYPE_ERR = -1;
	public static final int TYPE_ITEM = 0;
	public static final int TYPE_BUTTON = 1;

	@Override
	public final int getItemViewType (int position) {
		int count = getCount();
		if (position >= count || position < 0) return TYPE_ERR;
		if (position ==  count - 1) return TYPE_BUTTON;
		return TYPE_ITEM;
	}

	@Override
	public final int getViewTypeCount () {
		return 2;
	}

	@Override
	public final int getCount () {
		return getItemCount()+1;
	}

	@Override
	public final View getView (int position, View convertView, ViewGroup parent) {
		switch (getItemViewType(position)){
			case TYPE_ITEM: return getViewItem(position, convertView, parent);
			case TYPE_BUTTON: return getViewButton(convertView, parent);
			default: throw new RuntimeException(getClass().getName()+".getView(), itemViewType="+getItemViewType(position));
		}
	}

	protected abstract int getItemCount();
	protected abstract View getViewItem (int position, View convertView, ViewGroup parent);
	protected abstract View getViewButton (View convertView, ViewGroup parent);

	@Override public Object getItem (int position) { return null; }
	@Override public long getItemId (int position) { return 0; }
}
