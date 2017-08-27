package com.paijan.memorise.wordgroup.tester;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.paijan.memorise.convenience.ToastWrapper;
import com.paijan.memorise.grouplist.GroupListActivity;
import com.paijan.memorise.wordgroup.tester.dialog.AddDialog;
import com.paijan.memorise.wordgroup.tester.dialog.RemoveDialog;

import java.util.ArrayList;
import java.util.Collections;

public class ManageFragment extends Fragment {
	private WordGroup mWordGroup;
	private ListView mListView;
	private View mEmptyView;
	private int mContextListViewItemIndex;

	public static ManageFragment newInstance(WordGroup wordGroup) {
		Bundle bundle = new Bundle(1);
		bundle.putSerializable(GroupListActivity.KEY_WORD_GROUP, wordGroup);

		ManageFragment result = new ManageFragment();
		result.mWordGroup = wordGroup;
		result.setArguments(bundle);
		result.setHasOptionsMenu(true);
		return result;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWordGroup = (WordGroup) getArguments().getSerializable(GroupListActivity.KEY_WORD_GROUP);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getActivity());
		view.setOrientation(LinearLayout.VERTICAL);

		view.addView(mListView = (ListView) inflater.inflate(R.layout.usable_listview, view, false));
		mListView.setAdapter(new ListViewAdapter(new View.OnClickListener() {
			@Override
			public void onClick(View v) { showAddDialog(); }
		}));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) { displayWordEfficiency(position); }
		});
		manageContextMenuRegistry();

		view.addView(mEmptyView = inflater.inflate(R.layout.usable_empty, view, false));
		((TextView) mEmptyView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.group_empty));
		mEmptyView.findViewById(R.id.usable_empty_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { showAddDialog(); }
		});

		notifyListView();
		return view;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Action bar

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.group_menu, menu);
		initSearchView(menu);
	}
	private void initSearchView(Menu menu) {
		MenuItem searchItem = menu.findItem(R.id.group_menu_search);
		SearchView searchView = new SearchView(getContext());
		searchItem.setActionView(searchView);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) { return true; }
			@Override
			public boolean onQueryTextChange(String newText) {
				getAdapter().getFilter().filter(newText);
				manageContextMenuRegistry(newText);
				return true;
			}
		});
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Context Menu

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v == mListView) {
			getActivity().getMenuInflater().inflate(R.menu.group_context_tester, menu);
			mContextListViewItemIndex = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;

			Word word = mWordGroup.getWord(mContextListViewItemIndex);
			int titleRes = (word.isEnabled()) ? R.string.tester_disable : R.string.tester_enable;
			menu.findItem(R.id.group_context_tester_disable).setTitle(titleRes);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.group_context_tester_add:
				showAddDialog();
				return true;
			case R.id.group_context_tester_edit:
				showEditDialog();
				return true;
			case R.id.group_context_tester_disable:
				toggleWordEnabled();
				return true;
			case R.id.group_context_tester_remove:
				showRemoveDialog();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog showing

	private void showAddDialog() {
		AddDialog.newInstance(mAddDialogListener, mWordGroup).show(getActivity(), "add_dialog");
	}
	private final AddDialog.AddDialogListener mAddDialogListener = new AddDialog.AddDialogListener() {
		@Override
		public void onAdd(Word word) {
			mWordGroup.addWord(word);
			notifyListView();
			getOnWordGroupChangeListener().onWordGroupChange();
			mListView.setSelection(mListView.getCount() - 1);
		}
	};

	private void showEditDialog() {
		AddDialog.newInstance(mEditDialogListener, mWordGroup, mWordGroup.getWord(mContextListViewItemIndex)).show(getActivity(), "edit_dialog");
	}
	private final AddDialog.EditDialogListener mEditDialogListener = new AddDialog.EditDialogListener() {
		@Override
		public void onEdit(Word word) {
			mWordGroup.editWord(mContextListViewItemIndex, word);
			notifyListView();
			getOnWordGroupChangeListener().onWordGroupChange();
		}
	};

	private void showRemoveDialog() {
		RemoveDialog.newInstance(mRemoveDialogListener).show(getActivity(), "remove_dialog");
	}
	private final RemoveDialog.RemoveDialogListener mRemoveDialogListener = new RemoveDialog.RemoveDialogListener() {
		@Override
		public void onRemove() {
			mWordGroup.removeWord(mContextListViewItemIndex);
			notifyListView();
			getOnWordGroupChangeListener().onWordGroupChange();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Activity communication listeners

	public interface OnWordGroupChangeListener {
		void onWordGroupChange();
	}

	private OnWordGroupChangeListener getOnWordGroupChangeListener() {
		return (OnWordGroupChangeListener) getContext();
	}

	private ToastWrapper getToastWrapper() {
		return ((PagerActivity)getContext()).getToastWrapper();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	private void displayWordEfficiency(int position) {
		float eff = mWordGroup.getWord(position).getAccuracyPercent();
		String message = getString(R.string.tester_efficiency) + " " + eff + "%";
		getToastWrapper().show(message);
	}

	public void manageContextMenuRegistry() {
		manageContextMenuRegistry("");
	}
	public void manageContextMenuRegistry(String filter) {
		if (filter.isEmpty()) registerForContextMenu(mListView);
		else unregisterForContextMenu(mListView);
	}

	public void toggleWordEnabled() {
		Word word = mWordGroup.getWord(mContextListViewItemIndex);
		word.setEnabled(!word.isEnabled());
		mWordGroup.save();
		getOnWordGroupChangeListener().onWordGroupChange();
		notifyListView();
	}

	public void notifyListView() {
		getAdapter().notifyDataSetChanged();
		updateView();
	}
	private ListViewAdapter getAdapter() {
		return ((ListViewAdapter) mListView.getAdapter());
	}
	private void updateView() {
		if (getAdapter().getItemCount() == 0) {
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		}
	}


	private class ListViewAdapter extends ListViewAdapterBase implements Filterable {
		private final int CORRECT_COLOR;
		private final int DISABLED_COLOR;
		private final int MISTAKE_COLOR;

		private ArrayList<Word> mFilteredWords;
		private final View.OnClickListener mOnButtonClickListener;
		private Filter mFilter;

		public ListViewAdapter(View.OnClickListener listener) {
			mOnButtonClickListener = listener;

			MISTAKE_COLOR = ContextCompat.getColor(getContext(), R.color.TextTesterMistake);
			DISABLED_COLOR = ContextCompat.getColor(getContext(), R.color.TextDim);
			CORRECT_COLOR = ContextCompat.getColor(getContext(), R.color.Text);

			mFilteredWords = mWordGroup.getWords();
			notifyDataSetChanged();
		}

		private LayoutInflater getLayoutInflater() {
			return LayoutInflater.from(getContext());
		}

		@Override
		protected int getItemCount() {
			return mFilteredWords.size();
		}

		@Override
		protected View getViewItem(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.tester_listview_item, parent, false);
				viewHolder = new ViewHolder();

				viewHolder.text1 = (TextView) convertView.findViewById(R.id.tester_listview_item_text1);
				viewHolder.text2 = (TextView) convertView.findViewById(R.id.tester_listview_item_text2);

				convertView.setTag(viewHolder);
			} else viewHolder = (ViewHolder) convertView.getTag();

			Word word = mFilteredWords.get(position);
			viewHolder.text1.setText(word.getLangFirst1());
			viewHolder.text2.setText(word.getLangFirst2());

			int colorRes;
			if (!word.isEnabled()) colorRes = DISABLED_COLOR;
			else if (word.isMistaken()) colorRes = MISTAKE_COLOR;
			else colorRes = CORRECT_COLOR;
			viewHolder.text1.setTextColor(colorRes);
			viewHolder.text2.setTextColor(colorRes);

			return convertView;
		}

		@Override
		protected View getViewButton(View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.usable_addbutton, parent, false);
				convertView.findViewById(R.id.usable_empty_button).setOnClickListener(mOnButtonClickListener);
				((TextView) convertView.findViewById(R.id.usable_empty_text)).setText(R.string.group_add);
			}
			return convertView;
		}

		private class ViewHolder {
			TextView text1;
			TextView text2;
		}

		@Override
		public void notifyDataSetChanged() {
			Collections.sort(mFilteredWords);
			super.notifyDataSetChanged();
		}

		@Override
		public Filter getFilter() {
			if (mFilter == null) {
				mFilter = new Filter() {
					@Override
					protected FilterResults performFiltering(CharSequence constraint) {
						ArrayList<Word> testers;
						if (constraint == null || constraint.length() == 0) {
							testers = mWordGroup.getWords();
						} else {
							String search = constraint.toString().toLowerCase();
							testers = new ArrayList<>();
							for (Word tester : mWordGroup.getWords()) {
								for (String s : tester.getAllLangs()) {
									if (s.toLowerCase().contains(search)) {
										testers.add(tester);
										break;
									}
								}
							}
						}

						FilterResults filterResults = new FilterResults();
						filterResults.count = testers.size();
						filterResults.values = testers;

						return filterResults;
					}

					@SuppressWarnings ("unchecked")
					@Override
					protected void publishResults(CharSequence constraint, FilterResults results) {
						mFilteredWords = (ArrayList<Word>) results.values;
						notifyListView();
					}
				};
			}
			return mFilter;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}