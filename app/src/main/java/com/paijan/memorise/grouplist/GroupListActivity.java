package com.paijan.memorise.grouplist;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.ListViewAdapterBase;
import com.paijan.memorise.convenience.WordGroupBase;
import com.paijan.memorise.grouplist.dialog.AddDialog;
import com.paijan.memorise.grouplist.dialog.EditDialog;
import com.paijan.memorise.grouplist.dialog.RemoveDialog;
import com.paijan.memorise.grouplist.manager.WordGroupHeader;
import com.paijan.memorise.grouplist.manager.WordGroupManager;
import com.paijan.memorise.wordgroup.list.WordGroup;
import com.paijan.memorise.wordgroup.list.WordListActivity;
import com.paijan.memorise.wordgroup.tester.PagerActivity;

import java.io.File;

public class GroupListActivity extends AppCompatActivity {
	public static final String KEY_WORD_GROUP = "key_word_group";

	private View mEmptyView; //visibility states are updated in updateCurrentView()
	private ListView mListView;
	private WordGroupManager mWordGroupManager;
	private int mContextListViewItemIndex;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Create

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWordGroupManager();
		initActionBar();
		initOverScrollColor();
		initRootView();
		notifyListView();
	}
	private void initWordGroupManager() {
		File dir;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.name_app));
		} else {
			dir = getFilesDir();
		}
		if (!dir.exists()) if (!dir.mkdirs()) throw new RuntimeException("dir.mkdir() failed");
		mWordGroupManager = new WordGroupManager(dir);
	}
	private void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setIcon(R.mipmap.logo_noshadow);
		}
	}
	private void initRootView() {
		LinearLayout rootView = new LinearLayout(this);
		setContentView(rootView);

		rootView.addView(mEmptyView = getLayoutInflater().inflate(R.layout.usable_empty, rootView, false));
		((TextView) mEmptyView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.grouplist_empty));
		mEmptyView.findViewById(R.id.usable_empty_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { showAddDialog(); }
		});

		rootView.addView(mListView = (ListView) getLayoutInflater().inflate(R.layout.usable_listview, rootView, false));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) { selectWordGroup(position); }
		});
		registerForContextMenu(mListView); //long clicks
		mListView.setAdapter(new ListViewAdapter(new View.OnClickListener() {
			@Override
			public void onClick(View v) { showAddDialog(); }
		}));
	}
	@SuppressWarnings ("deprecation")
	private void initOverScrollColor() {
		Resources res = getResources();
		int brandColor = res.getColor(R.color.Effect);
		Drawable glow = res.getDrawable(res.getIdentifier("overscroll_glow", "drawable", "android"));
		Drawable edge = res.getDrawable(res.getIdentifier("overscroll_edge", "drawable", "android"));
		if (glow != null) glow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
		if (edge != null) edge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Context menu

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v == mListView) {
			getMenuInflater().inflate(R.menu.grouplist_context, menu);
			mContextListViewItemIndex = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.grouplist_context_add:
				showAddDialog();
				return true;
			case R.id.grouplist_context_edit:
				showEditDialog();
				return true;
			case R.id.grouplist_context_remove:
				showRemoveDialog();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog showing

	private void showAddDialog() {
		AddDialog.newInstance(mAddDialogListener).show(this, "add_dialog");
	}
	AddDialog.AddDialogListener mAddDialogListener = new AddDialog.AddDialogListener() {
		@Override
		public void onAdd(WordGroupHeader header) {
			mWordGroupManager.addWordGroup(header);
			notifyListView();
		}
		@Override
		public WordGroupManager.ValidationResult onValidate(WordGroupHeader header) {
			return mWordGroupManager.validateName(header);
		}
	};

	public void showEditDialog() {
		EditDialog.newInstance(mEditDialogListener, mWordGroupManager.getHeader(mContextListViewItemIndex)).show(this, "edit_dialog");
	}
	private final EditDialog.EditDialogListener mEditDialogListener = new EditDialog.EditDialogListener() {
		@Override
		public void onEdit(WordGroupHeader oldHeader, String newName) {
			mWordGroupManager.editWordGroup(oldHeader, newName);
			notifyListView();
		}
		@Override
		public WordGroupManager.ValidationResult onValidate(WordGroupHeader header) {
			return mWordGroupManager.validateName(header);
		}
	};

	public void showRemoveDialog() {
		RemoveDialog.newInstance(mRemoveDialogListener, mWordGroupManager.getHeader(mContextListViewItemIndex)).show(this, "remove_dialog");
	}
	RemoveDialog.RemoveDialogListener mRemoveDialogListener = new RemoveDialog.RemoveDialogListener() {
		@Override
		public void onRemove(WordGroupHeader header) {
			mWordGroupManager.deleteWordGroup(header);
			notifyListView();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	private void selectWordGroup(int position) {
		WordGroupBase wordGroup = mWordGroupManager.retrieveWordGroup(position);

		Intent intent;
		if (wordGroup instanceof com.paijan.memorise.wordgroup.tester.WordGroup) {
			intent = new Intent(GroupListActivity.this, PagerActivity.class);
		} else if (wordGroup instanceof WordGroup) {
			intent = new Intent(GroupListActivity.this, WordListActivity.class);
		} else {
			//Log.e("GroupList", "wordGroup is " + (wordGroup == null ? "null" : wordGroup.getClass().getName()));
			return;
		}

		intent.putExtra(KEY_WORD_GROUP, wordGroup);
		startActivity(intent);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- UI setting

	private void notifyListView() {
		getAdapter().notifyDataSetChanged();
		updateCurrentView();
	}
	private void updateCurrentView() {
		if (mWordGroupManager.isEmpty()) {
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		}
	}
	private ListViewAdapter getAdapter() {
		return ((ListViewAdapter) mListView.getAdapter());
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	private class ListViewAdapter extends ListViewAdapterBase {
		private final View.OnClickListener mOnAddButtonClickListener;
		public ListViewAdapter(View.OnClickListener listener) {
			mOnAddButtonClickListener = listener;
		}

		@Override
		public int getItemCount() {
			return mWordGroupManager.getHeadersCount();
		}

		@Override
		protected View getViewItem(int position, View convertView, ViewGroup parent) {
			ViewHolderGroup viewHolder;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.grouplist_listview_item, parent, false);
				viewHolder = new ViewHolderGroup();
				convertView.setTag(viewHolder);
				viewHolder.text = (TextView) convertView.findViewById(R.id.grouplist_listview_item_text);
				viewHolder.img1 = (ImageView) convertView.findViewById(R.id.grouplist_listview_item_img1);
				viewHolder.img2 = (ImageView) convertView.findViewById(R.id.grouplist_listview_item_img2);
			} else viewHolder = (ViewHolderGroup) convertView.getTag();

			WordGroupHeader header = mWordGroupManager.getHeader(position);
			viewHolder.text.setText(header.getName());
			viewHolder.img1.setImageResource(header.getLang1().getFlagId());
			if (header.getLang2() != WordGroupBase.Lang.VOID) {
				viewHolder.img2.setImageResource(header.getLang2().getFlagId());
				viewHolder.img2.setVisibility(View.VISIBLE);
			} else viewHolder.img2.setVisibility(View.GONE);

			return convertView;
		}
		@Override
		protected View getViewButton(View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.usable_addbutton, parent, false);
				convertView.findViewById(R.id.usable_empty_button).setOnClickListener(mOnAddButtonClickListener);
				((TextView) convertView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.grouplist_add));
			}
			return convertView;
		}

		private class ViewHolderGroup {
			ImageView img1, img2;
			TextView text;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
} //activity




