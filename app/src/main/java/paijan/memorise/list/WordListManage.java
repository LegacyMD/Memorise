package paijan.memorise.list;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Vector;

import paijan.memorise.R;
import paijan.memorise.general.WordGroupManager;
import paijan.memorise.grouplist.GroupList;

public class WordListManage extends AppCompatActivity {
	private static final int DIAL_ADDING = -1;

	private ListView mListView;
	private View mEmptyView;
	private WordGroupManager mWordGroupManager;
	private WordGroupList mWordGroupList;
	private int mContextSelectedItem;


	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWordGroupManager = (WordGroupManager) getIntent().getSerializableExtra(GroupList.KEY_WORD_GROUP_MANAGER);
		mWordGroupList = (WordGroupList) getIntent().getSerializableExtra(GroupList.KEY_WORD_GROUP);
		setTitle(mWordGroupList.getName());

		LinearLayout view = new LinearLayout(this);
		setContentView(view);

		view.addView(mListView = (ListView)getLayoutInflater().inflate(R.layout.list_listview, view, false));
		mListView.setAdapter(new ListViewAdapter(this, mWordGroupList.getWords(), mOnAddDialogShowListener));
		registerForContextMenu(mListView);

		view.addView(mEmptyView = getLayoutInflater().inflate(R.layout.usable_empty, view, false));
		((TextView)mEmptyView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.group_empty));
		mEmptyView.findViewById(R.id.usable_empty_button).setOnClickListener(mOnAddDialogShowListener);

		updateView();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Action Bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group_menu, menu);

		MenuItem searchViewItem = menu.findItem(R.id.group_menu_search);
		SearchView searchView = new SearchView(this);
		searchViewItem.setActionView(searchView);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override public boolean onQueryTextSubmit(String query) { return true; }
			@Override public boolean onQueryTextChange(String newText) {
				((Filterable) mListView.getAdapter()).getFilter().filter(newText);
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.group_menu_backup:
				mOnBackupSaveDialogShowListener.onClick(null);
				return true;

			case R.id.group_menu_search:
				return false;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Context menu

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if(v == mListView){
			getMenuInflater().inflate(R.menu.group_context, menu);
			mContextSelectedItem = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.group_context_add:
				mOnAddDialogShowListener.onClick(null);
				return true;

			case R.id.group_context_edit:
				mOnEditDialogShowListener.onClick(null);
				return true;

			case R.id.group_context_remove:
				mOnRemoveDialogShowListener.onClick(null);
				return true;

			default:
				return super.onContextItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog showing Listeners

	private View.OnClickListener mOnAddDialogShowListener = new View.OnClickListener() {
		@Override
		public void onClick(@Nullable View v) {
			mContextSelectedItem = DIAL_ADDING;
			mOnEditDialogShowListener.onClick(null);
		}
	};

	private View.OnClickListener mOnEditDialogShowListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("add_list");
			if(prev!=null) fragmentManager.beginTransaction().remove(prev).commit();
			if(mContextSelectedItem==DIAL_ADDING)
				AddDialog.newInstance(AddDialog.MODE.add, mOnWordListAddListener).show(getSupportFragmentManager(), "add_list");
			else
				AddDialog.newInstance(AddDialog.MODE.edit, mOnWordListAddListener, mWordGroupList.getWords().get(mContextSelectedItem)).show(getSupportFragmentManager(), "add_list");
		}
	};

	private View.OnClickListener mOnRemoveDialogShowListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(WordListManage.this);
		builder.setTitle(R.string.group_delete_question);
		builder.setPositiveButton(R.string.yes, mOnWordListRemoveListener);
		builder.setNegativeButton(R.string.no, null);
		builder.show(); //Actual removing performed in listener
		}
	};

	private View.OnClickListener mOnBackupSaveDialogShowListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("backup_save");
			if(prev!=null) fragmentManager.beginTransaction().remove(prev).commit();
			BackupSaveDialog.newInstance(mWordGroupManager, mWordGroupList).show(fragmentManager, "backup_save");

		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	private AddDialog.OnWordListAddListener mOnWordListAddListener = new AddDialog.OnWordListAddListener() {
		@Override
		public void onAdd(String word, String help) {
			if(mContextSelectedItem == DIAL_ADDING)
				mWordGroupList.addWord(word, help);
			else
				mWordGroupList.editWord(mContextSelectedItem, word, help);
			notifyListView();
		}
	};

	private DialogInterface.OnClickListener mOnWordListRemoveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mWordGroupList != null) {
				mWordGroupList.removeWord(mContextSelectedItem);
				notifyListView();
			}
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC

	public void notifyListView(){
		BaseAdapter adapter = (BaseAdapter)mListView.getAdapter();
		adapter.notifyDataSetChanged();
		updateView();
	}

	private void updateView(){
		if (mWordGroupList != null) {
			if(mWordGroupList.getWords().size()==0){
				mListView.setVisibility(View.GONE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else{
				mListView.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.GONE);
			}
		}

	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}

// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

class ListViewAdapter extends BaseAdapter implements Filterable{
	private static final int TYPE_WORD = 0;
	private static final int TYPE_BUTTON = 1;
	private final LayoutInflater mLayoutInflater;
	private final Vector<WordList> mWordLists;
	private Vector<WordList> mFilteredWordLists;
	private final String mAddButtonText;
	private final View.OnClickListener mOnClickListener;

	public ListViewAdapter(FragmentActivity fragmentActivity, Vector<WordList> words, View.OnClickListener onClickListener){
		mLayoutInflater = fragmentActivity.getLayoutInflater();
		mAddButtonText = fragmentActivity.getString(R.string.group_add);
		mOnClickListener = onClickListener;

		mWordLists = words;
		mFilteredWordLists = words;
	}
	@Override public Object getItem(int position) { return null; }
	@Override public long getItemId(int position) { return 0; }

	@Override public int getItemViewType(int position) { return (position < mFilteredWordLists.size()) ? TYPE_WORD : TYPE_BUTTON; }
	@Override public int getViewTypeCount() { return 2; }
	@Override public int getCount() { return mFilteredWordLists.size()+1; }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		switch(getItemViewType(position)){
			case TYPE_WORD:
				if(convertView==null){
					convertView = mLayoutInflater.inflate(R.layout.list_listview_item, parent, false);
					viewHolder = new ViewHolder();
					convertView.setTag(viewHolder);

					viewHolder.text1 = (TextView) convertView.findViewById(R.id.list_listview_item_word);
					viewHolder.text2 = (TextView) convertView.findViewById(R.id.list_listview_item_comment);
				} else viewHolder = (ViewHolder) convertView.getTag();

				viewHolder.text1.setText(mFilteredWordLists.get(position).getWord());

				String secondText = mFilteredWordLists.get(position).getHelp();
				if(secondText.equals("")) viewHolder.text2.setVisibility(View.GONE);
				else{
					viewHolder.text2.setVisibility(View.VISIBLE);
					viewHolder.text2.setText(secondText);
				}
				return convertView;

			case TYPE_BUTTON:
				if(convertView==null){
					convertView = mLayoutInflater.inflate(R.layout.usable_addbutton, parent, false);
					((TextView)convertView.findViewById(R.id.usable_addbutton_text)).setText(mAddButtonText);
					convertView.findViewById(R.id.usable_addbutton_button).setOnClickListener(mOnClickListener);
				}
				return convertView;

			default:
				throw new IllegalArgumentException("Grouplistadapter: type should be 0 or 1");
		}
	}

	private static class ViewHolder{
		TextView text1, text2;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				Vector<WordList> lists;
				if(constraint==null || constraint.toString().equals("")){
					lists = mWordLists;
				} else{
					String search = constraint.toString().toLowerCase();
					lists = new Vector<>();
					for(WordList list : mWordLists){
						if(list.getWord().toLowerCase().contains(search)){
							lists.add(list);
						}
					}
				}

				FilterResults filterResults = new FilterResults();
				filterResults.count = lists.size();
				filterResults.values = lists;
				return filterResults;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mFilteredWordLists = ((Vector<WordList>) results.values);
				notifyDataSetChanged();
			}
		};
	}
}