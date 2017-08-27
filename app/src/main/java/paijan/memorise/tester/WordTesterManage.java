package paijan.memorise.tester;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Vector;

import paijan.memorise.R;
import paijan.memorise.grouplist.GroupList;

public class WordTesterManage extends Fragment {
	private static final int DIAL_ADDING = -1;

	private WordGroupTester mWordGroupTester;
	ListView mListView;
	private View mEmptyView;
	private int mContextListViewItemIndex = DIAL_ADDING;

	public static WordTesterManage newInstance(WordGroupTester wt, OnWordGroupChangeListener onWordGroupChangeListener){
		Bundle args = new Bundle();
		args.putSerializable(GroupList.KEY_WORD_GROUP, wt);

		WordTesterManage wtm = new WordTesterManage();
		wtm.setArguments(args);
		wtm.setOnGroupChangeListener(onWordGroupChangeListener);
		return wtm;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mWordGroupTester = (WordGroupTester) getArguments().getSerializable(GroupList.KEY_WORD_GROUP);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getActivity());
		view.setOrientation(LinearLayout.VERTICAL);

		view.addView(mListView = (ListView)inflater.inflate(R.layout.tester_manage_list, view, false));
		mListView.setAdapter(new ListViewAdapter(getActivity(), mOnShowAddDialog, mWordGroupTester));
		mListView.setOnItemClickListener(mListViewOnClickListener);
		registerForContextMenu(mListView);

		view.addView(mEmptyView = inflater.inflate(R.layout.usable_empty, view, false));
		((TextView)mEmptyView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.group_empty));
		mEmptyView.findViewById(R.id.usable_empty_button).setOnClickListener(mOnShowAddDialog);

		updateView();
		return view;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Context Menu

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v==mListView){
			getActivity().getMenuInflater().inflate(R.menu.group_context, menu);
			mContextListViewItemIndex = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.group_context_add:
				mOnShowAddDialog.onClick(null);
				return true;
			case R.id.group_context_edit:
				mOnShowEditDialog.onClick(null);
				return true;
			case R.id.group_context_remove:
				showRemoveDialog();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog showing

	private View.OnClickListener mOnShowAddDialog = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("tester_dialog");
			if(prev!=null){
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.remove(prev);
				fragmentTransaction.commit();
			}
			mContextListViewItemIndex = DIAL_ADDING; //it means we're adding, not editing
			AddDialog.newInstance(new WordTester(), mWordGroupTester, mOnWordTesterAddListener, AddDialog.MODE.ADD).show(fragmentManager, "tester_dialog");
		}
	};

	private View.OnClickListener mOnShowEditDialog = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			//mContextListViewItemIndex is set to proper index now, no need to mention it
			AddDialog.newInstance(mWordGroupTester.getWords().get(mContextListViewItemIndex), mWordGroupTester, mOnWordTesterAddListener, AddDialog.MODE.EDIT).show(fragmentManager, "tester_dialog");
		}
	};

	private void showRemoveDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.group_delete_question);
		builder.setPositiveButton(R.string.yes, mOnWordTesterRemoveListener);
		builder.setNegativeButton(R.string.no, null);
		builder.show(); //Actual removing performed in listener
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	public interface OnWordGroupChangeListener{ public void onChange(); }
	private OnWordGroupChangeListener mOnWordGroupChangeListener;
	public void setOnGroupChangeListener(OnWordGroupChangeListener l) {mOnWordGroupChangeListener = l;}

	private AddDialog.OnWordTesterAddListener mOnWordTesterAddListener = new AddDialog.OnWordTesterAddListener() {
		@Override
		public void onAdd(Vector<String> lang1, Vector<String> lang2, String help1, String help2) {
			if(mContextListViewItemIndex == DIAL_ADDING) {
				mWordGroupTester.addWord(lang1, lang2, help1, help2);
			} else {
				mWordGroupTester.editWord(mContextListViewItemIndex, lang1, lang2, help1, help2);
			}
			notifyListView();
			mListView.setSelection(mListView.getAdapter().getCount()-1);
			mOnWordGroupChangeListener.onChange();
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	};

	private DialogInterface.OnClickListener mOnWordTesterRemoveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mWordGroupTester.deleteWord(mContextListViewItemIndex);
			notifyListView();
			mOnWordGroupChangeListener.onChange();
		}
	};

	AdapterView.OnItemClickListener mListViewOnClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			float eff = WordTesterManage.this.mWordGroupTester.getWords().elementAt(position).getAccuracyPercent();
			Toast.makeText(WordTesterManage.this.getActivity(), getString(R.string.tester_efficiency) + " " + eff + "%", Toast.LENGTH_SHORT).show();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC

	public void notifyListView(){
		BaseAdapter adapter = (BaseAdapter)mListView.getAdapter();
		adapter.notifyDataSetChanged();
		updateView();
	}

	private void updateView(){
		if(mWordGroupTester.getWords().size() == 0){
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else{
			mListView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}


// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --


class ListViewAdapter extends BaseAdapter implements Filterable{
	private static final int TYPE_ITEM = 0;
	private static final int TYPE_BUTTON = 1;
	private final int MISTAKE_COLOR;
	private final int CORRECT_COLOR;
	private final String mAddButtonText;

	private final LayoutInflater mLayoutInflater;
	private final Vector<WordTester> mWordTesters;
	private Vector<WordTester> mFilteredWordTesters;
	private final View.OnClickListener mOnButtonClickListener;

	public ListViewAdapter(FragmentActivity fragmentActivity, View.OnClickListener listener, WordGroupTester wgt){
		mLayoutInflater = fragmentActivity.getLayoutInflater();
		mOnButtonClickListener = listener;
		Resources res = fragmentActivity.getResources();
		MISTAKE_COLOR = res.getColor(R.color.TextTesterMistake);
		CORRECT_COLOR = res.getColor(R.color.Text);
		mAddButtonText = res.getString(R.string.group_add);

		mWordTesters = wgt.getWords();
		mFilteredWordTesters = mWordTesters;

		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return (position == getCount()-1) ? TYPE_BUTTON : TYPE_ITEM;
	}
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	@Override
	public int getCount() {return mFilteredWordTesters.size()+1;}
	@Override
	public Object getItem(int position) {
		return null;
	}
	@Override
	public long getItemId(int position) {return 0;}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		switch(getItemViewType(position)){
			case TYPE_ITEM:
				if(convertView == null){
					convertView = mLayoutInflater.inflate(R.layout.tester_listview_item, parent, false);
					viewHolder = new ViewHolder();

					viewHolder.text1 = (TextView) convertView.findViewById(R.id.tester_listview_item_text1);
					viewHolder.text2 = (TextView) convertView.findViewById(R.id.tester_listview_item_text2);

					convertView.setTag(viewHolder);
				} else viewHolder = (ViewHolder) convertView.getTag();

				WordTester tester = mFilteredWordTesters.elementAt(position);
				viewHolder.text1.setText(tester.getLangFirst1());
				viewHolder.text2.setText(tester.getLangFirst2());

				if(mFilteredWordTesters.get(position).mWronged) {
					viewHolder.text1.setTextColor(MISTAKE_COLOR);
					viewHolder.text2.setTextColor(MISTAKE_COLOR);
				} else{
					viewHolder.text1.setTextColor(CORRECT_COLOR);
					viewHolder.text2.setTextColor(CORRECT_COLOR);
				}

				return convertView;

			case TYPE_BUTTON:
				if(convertView == null){
					convertView = mLayoutInflater.inflate(R.layout.usable_addbutton, parent, false);
					convertView.findViewById(R.id.usable_addbutton_button).setOnClickListener(mOnButtonClickListener);
					((TextView)convertView.findViewById(R.id.usable_addbutton_text)).setText(mAddButtonText);
				}
				return convertView;

			default:
				throw new IllegalArgumentException("TesterManageAdapter: type should be 0 or 1");

		}
	}

	static class ViewHolder{
		TextView text1;
		TextView text2;
	}

	@Override
	public void notifyDataSetChanged() {
		Collections.sort(mFilteredWordTesters);
		super.notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				Vector<WordTester> testers;
				if(constraint == null || constraint.toString().toLowerCase().equals("")){
					testers = mWordTesters;
				} else{
					String search = constraint.toString().toLowerCase();
					testers = new Vector<>();
					for(WordTester tester : mWordTesters){
						Vector<String> strings = tester.getAllLangs();
						for(String s : strings){
							if(s.toLowerCase().contains(search)){
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

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mFilteredWordTesters = (Vector<WordTester>) results.values;
				notifyDataSetChanged();
			}
		};
	}
}