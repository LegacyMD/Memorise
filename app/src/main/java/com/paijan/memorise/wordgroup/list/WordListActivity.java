package com.paijan.memorise.wordgroup.list;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.ListViewAdapterBase;
import com.paijan.memorise.grouplist.GroupListActivity;
import com.paijan.memorise.wordgroup.list.dialog.AddDialog;
import com.paijan.memorise.wordgroup.list.dialog.RemoveDialog;

import java.util.ArrayList;

public class WordListActivity extends AppCompatActivity {
	private ListView mListView;
	private View mEmptyView;
	private WordGroup mWordGroupList;
	private int mContextListViewSelectedItem;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Create

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWordGroup();
		initRootView();
		notifyListView();
	}
	private void initWordGroup() {
		mWordGroupList = (WordGroup) getIntent().getSerializableExtra(GroupListActivity.KEY_WORD_GROUP);
		setTitle(mWordGroupList.getName());
	}
	private void initRootView() {
		LinearLayout rootView = new LinearLayout(this);
		setContentView(rootView);

		rootView.addView(mListView = (ListView)getLayoutInflater().inflate(R.layout.usable_listview, rootView, false));
		mListView.setAdapter(new ListViewAdapter(new View.OnClickListener() { @Override public void onClick (View v) { showAddDialog(); } }));
		manageContextMenuRegistry();

		rootView.addView(mEmptyView = getLayoutInflater().inflate(R.layout.usable_empty, rootView, false));
		((TextView)mEmptyView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.group_empty));
		mEmptyView.findViewById(R.id.usable_empty_button).setOnClickListener(new View.OnClickListener() { @Override public void onClick (View v) { showAddDialog(); } });
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Action Bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group_menu, menu);
		initSearchView(menu);
		return true;
	}
	private void initSearchView(Menu menu) {
		MenuItem searchViewItem = menu.findItem(R.id.group_menu_search);
		SearchView searchView = new SearchView(this);
		searchViewItem.setActionView(searchView);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override public boolean onQueryTextSubmit(String query) { return true; }
			@Override public boolean onQueryTextChange(String newText) {
				getAdapter().getFilter().filter(newText);
				manageContextMenuRegistry(newText);
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.group_menu_search:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Context menu

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if(v == mListView){
			getMenuInflater().inflate(R.menu.group_context_list, menu);
			mContextListViewSelectedItem = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.group_context_list_add:
				showAddDialog();
				return true;
			case R.id.group_context_list_edit:
				showEditDialog();
				return true;
			case R.id.group_context_list_remove:
				showRemoveDialog();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog showing Listeners

	private void showAddDialog() {
		AddDialog.newInstance(mAddDialogListener).show(this, "add_dialog");
	}
	private AddDialog.AddDialogListener mAddDialogListener = new AddDialog.AddDialogListener() {
		@Override
		public void onAdd (Word word) {
			mWordGroupList.addWord(word);
			notifyListView();
		}
	};

	private void showEditDialog() {
		Word word = mWordGroupList.getWords().get(mContextListViewSelectedItem);
		AddDialog.newInstance(word, mEditDialogListener).show(this, "edit_dialog");
	}
	private AddDialog.EditDialogListener mEditDialogListener = new AddDialog.EditDialogListener() {
		@Override
		public void onEdit (Word word) {
			mWordGroupList.editWord(mContextListViewSelectedItem, word);
			notifyListView();
		}
	};

	private void showRemoveDialog() {
		RemoveDialog.newInstance(mRemoveDialogListener).show(this, "remove_dialog");
	}
	private RemoveDialog.RemoveDialogListener mRemoveDialogListener = new RemoveDialog.RemoveDialogListener() {
		@Override
		public void onRemove () {
			mWordGroupList.removeWord(mContextListViewSelectedItem);
			notifyListView();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- UI setting

	private void notifyListView(){
		getAdapter().notifyDataSetChanged();
		updateCurrentView();
	}
	private void updateCurrentView (){
		if(mWordGroupList.getWords().size()==0){
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else{
			mListView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		}
	}
	private ListViewAdapter getAdapter() {
		return ((ListViewAdapter) mListView.getAdapter());
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	public void manageContextMenuRegistry() {
		manageContextMenuRegistry("");
	}
	public void manageContextMenuRegistry (String filter) {
		if(filter.isEmpty()) registerForContextMenu(mListView);
		else unregisterForContextMenu(mListView);
	}

	private class ListViewAdapter extends ListViewAdapterBase implements Filterable{
		private final View.OnClickListener mOnClickListener;
		private Filter mFilter;
		private ArrayList<Word> mWordsFiltered;

		public ListViewAdapter(View.OnClickListener onClickListener){
			mOnClickListener = onClickListener;
			mWordsFiltered = mWordGroupList.getWords();
		}

		@Override public int getItemCount() { return mWordsFiltered.size(); }

		@Override
		protected View getViewItem (int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if(convertView==null){
				convertView = getLayoutInflater().inflate(R.layout.list_listview_item, parent, false);
				viewHolder = new ViewHolder();
				convertView.setTag(viewHolder);

				viewHolder.text1 = (TextView) convertView.findViewById(R.id.list_listview_item_word);
				viewHolder.text2 = (TextView) convertView.findViewById(R.id.list_listview_item_comment);
			} else viewHolder = (ViewHolder) convertView.getTag();

			viewHolder.text1.setText(mWordsFiltered.get(position).getWord());

			String secondText = mWordsFiltered.get(position).getHelp();
			if(secondText.equals("")) viewHolder.text2.setVisibility(View.GONE);
			else{
				viewHolder.text2.setVisibility(View.VISIBLE);
				viewHolder.text2.setText(secondText);
			}
			return convertView;
		}
		@Override
		protected View getViewButton (View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = getLayoutInflater().inflate(R.layout.usable_addbutton, parent, false);
				((TextView)convertView.findViewById(R.id.usable_empty_text)).setText(R.string.group_add);
				convertView.findViewById(R.id.usable_empty_button).setOnClickListener(mOnClickListener);
			}
			return convertView;
		}

		private class ViewHolder{
			TextView text1, text2;
		}

		@Override
		public Filter getFilter() {
			if (mFilter == null) {
				mFilter = new Filter() {
					@Override
					protected FilterResults performFiltering (CharSequence constraint) {
						ArrayList<Word> words;
						if (constraint == null || constraint.length() == 0) {
							words = mWordGroupList.getWords();
						} else {
							String search = constraint.toString().toLowerCase();
							words = new ArrayList<>();
							for (Word word : mWordGroupList.getWords()) {
								if (word.getWord().toLowerCase().contains(search) || (word.getHelp().toLowerCase().contains(search))) {
									words.add(word);
								}
							}
						}

						FilterResults filterResults = new FilterResults();
						filterResults.count = words.size();
						filterResults.values = words;
						return filterResults;
					}

					@SuppressWarnings ("unchecked")
					@Override
					protected void publishResults (CharSequence constraint, FilterResults results) {
						mWordsFiltered = ((ArrayList<Word>) results.values);
						notifyListView();
					}
				};
			}
			return mFilter;
		}

	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}