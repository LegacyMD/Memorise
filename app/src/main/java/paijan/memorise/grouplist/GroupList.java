package paijan.memorise.grouplist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

import paijan.memorise.R;
import paijan.memorise.general.WordGroup;
import paijan.memorise.general.WordGroupManager;
import paijan.memorise.list.WordGroupList;
import paijan.memorise.list.WordListManage;
import paijan.memorise.tester.WordGroupTester;
import paijan.memorise.tester.WordTesterPager;

public class GroupList extends AppCompatActivity {
	private View mEmptyView; //visibility states are updated in updateView()
	private ListView mListView;
	private WordGroupManager mWordGroupManager;
	private int mContextListViewItemIndex;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		File dir;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.name_app));
		else
			dir = getFilesDir();
		if(!dir.exists())
			if(!dir.mkdir())
				throw new IllegalAccessError("dir.mkdir() failed");
		mWordGroupManager = new WordGroupManager(dir);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setIcon(R.mipmap.logo_noshadow);
		}

		LinearLayout view = new LinearLayout(this);
		setContentView(view);
		setOverscrollColor();

		view.addView(mEmptyView =  getLayoutInflater().inflate(R.layout.usable_empty, view, false));
		((TextView)mEmptyView.findViewById(R.id.usable_empty_text)).setText(getString(R.string.grouplist_empty));
		mEmptyView.findViewById(R.id.usable_empty_button).setOnClickListener(onShowAddDialogListener);

		view.addView(mListView = (ListView) getLayoutInflater().inflate(R.layout.grouplist_list, view, false));
		mListView.setOnItemClickListener(mListViewOnClickListener);
		registerForContextMenu(mListView); //long clicks
		mListView.setAdapter(new ListViewAdapter(this, onShowAddDialogListener, mWordGroupManager.getNames(), mWordGroupManager.getLangs1(), mWordGroupManager.getLangs2()));

		updateView();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Action Bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.grouplist_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.grouplist_backup_load:
				onShowBackupLoadDialogListener.onClick(null);
				return true;

			case R.id.grouplist_list_memory:
				mWordGroupManager.listMemory();
				notifyListView();
				return true;

			case R.id.grouplist_clear_memory:
				mWordGroupManager.clearMemory();
				notifyListView();
				updateView();
				return true;

			case R.id.grouplist_list_reload:
				mWordGroupManager.reload();
				notifyListView();

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Context menu

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if(v == mListView){
			getMenuInflater().inflate(R.menu.grouplist_context, menu);
			mContextListViewItemIndex = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.grouplist_context_add:
				onShowAddDialogListener.onClick(null);
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

	private View.OnClickListener onShowAddDialogListener = new View.OnClickListener() {
		@Override
		public void onClick(@Nullable View v) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("add_dialog");
			if(prev!=null){
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.remove(prev);
				fragmentTransaction.commit();
			}

			AddDialog.newInstance(mOnAddValidateListener, mOnGroupAddListener)
					.show(fragmentManager, "add_dialog");
		}
	};

	public void showEditDialog(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment prev = fragmentManager.findFragmentByTag("edit_dialog");
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(prev!=null){
			fragmentTransaction.remove(prev);
			fragmentTransaction.commit();
		}

		EditDialog.newInstance(mOnEditValidateListener, mOnGroupEditListener, mWordGroupManager.getName(mContextListViewItemIndex))
				.show(fragmentManager, "edit_dialog");
	}

	public void showRemoveDialog(){
		AlertDialog.Builder builder;
		if(Build.VERSION.SDK_INT >= 11){
			builder	= new AlertDialog.Builder(this, R.style.Theme_DialogTheme_Grouplist);
		} else{
			builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_DialogTheme_Grouplist));
		}

		builder.setTitle(R.string.grouplist_delete_question);
		builder.setPositiveButton(R.string.yes, mOnGroupRemoveListener);
		builder.setNegativeButton(R.string.no, null);
		builder.show(); //Actual removing performed in listener
	}

	private View.OnClickListener onShowBackupLoadDialogListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			WordGroupManager wordGroupBackupanager = new WordGroupManager(mWordGroupManager.getBackupDir());
			if(wordGroupBackupanager.getGroupCount() > 0){
				FragmentManager fragmentManager = getSupportFragmentManager();
				Fragment prev = fragmentManager.findFragmentByTag("backup_load");
				if(prev!=null) fragmentManager.beginTransaction().remove(prev).commit();

				BackupLoadDialog.newInstance(wordGroupBackupanager.getDIR(), mOnBackupLoadListener).show(fragmentManager, "backup_load");
			}
			else Toast.makeText(GroupList.this, R.string.group_backup_load_nogroups, Toast.LENGTH_LONG).show();

		}
	};

	private BackupLoadDialog.OnBackupLoadListener mOnBackupLoadListener = new BackupLoadDialog.OnBackupLoadListener() {
		@Override
		public void onLoad() {
			mWordGroupManager.reload();
			notifyListView();
			updateView();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	AddDialog.OnGroupAddListener mOnGroupAddListener = new AddDialog.OnGroupAddListener() {
		@Override
		public void onAdd(String name, WordGroup.LANG lang1, WordGroup.LANG lang2, WordGroup.TYPE type) {
			mWordGroupManager.addWordGroup(name, lang1, lang2, type);
			notifyListView();
			updateView();
		}
	};
	AddDialog.OnValidateListener mOnAddValidateListener = new AddDialog.OnValidateListener() {
		@Override
		public WordGroupManager.VALIDATION_RESULT onValidate(String string) {
			return mWordGroupManager.validateNameFull(string);
		}
	};

	EditDialog.OnGroupEditListener mOnGroupEditListener = new EditDialog.OnGroupEditListener() {
		@Override
		public void onEdit(String name) {
			mWordGroupManager.editWordGroup(mContextListViewItemIndex, name);
			notifyListView();
		}
	};
	EditDialog.OnValidateListener mOnEditValidateListener = new EditDialog.OnValidateListener(){
		@Override
		public WordGroupManager.VALIDATION_RESULT onValidate(String name) {
			return mWordGroupManager.validateNameFull(name);
		}
	};

	DialogInterface.OnClickListener mOnGroupRemoveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mWordGroupManager.deleteWordGroup(mContextListViewItemIndex);
			notifyListView();
			updateView();
			dialog.dismiss();
		}
	};

	public static final String KEY_WORD_GROUP = "key_word_group";
	public static final String KEY_WORD_GROUP_MANAGER = "key_word_group_manager";
	AdapterView.OnItemClickListener mListViewOnClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			WordGroup wordGroup = mWordGroupManager.selectWordGroup(position);
			if(wordGroup==null) return;

			Intent intent;
			if(wordGroup instanceof WordGroupTester){
				intent = new Intent(GroupList.this, WordTesterPager.class);
			} else if(wordGroup instanceof WordGroupList){
				intent = new Intent(GroupList.this, WordListManage.class);
			}
			else throw new AssertionError("wordGroupType");

			intent.putExtra(KEY_WORD_GROUP, wordGroup);
			intent.putExtra(KEY_WORD_GROUP_MANAGER, new WordGroupManager(mWordGroupManager.getBackupDir()));

			startActivity(intent);
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC
	public void notifyListView(){
		BaseAdapter adapter = (BaseAdapter)mListView.getAdapter();
		adapter.notifyDataSetChanged();
	}

	private void updateView(){
		if(mWordGroupManager.getGroupCount() == 0){
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		}
	}

	@SuppressWarnings("deprecation")
	private void setOverscrollColor(){
		Resources res = getResources();
		int brandColor = res.getColor(R.color.Effect);
		Drawable glow = res.getDrawable(res.getIdentifier("overscroll_glow", "drawable", "android"));
		Drawable edge = res.getDrawable(res.getIdentifier("overscroll_edge", "drawable", "android"));
		if(glow!=null) glow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
		if(edge!=null) edge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
} //activity


// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --


class ListViewAdapter extends BaseAdapter {
	public static final int TYPE_GROUP = 0;
	public static final int TYPE_BUTTON = 1;
	public final String mAddButtonText;

	private Vector<String> mNames;
	private Vector<WordGroup.LANG> mLangs1;
	private Vector<WordGroup.LANG> mLangs2;
	private LayoutInflater layoutInflater;
	private View.OnClickListener mOnClickListener;

	public ListViewAdapter(FragmentActivity fragmentActivity, View.OnClickListener listener, Vector<String> names, Vector<WordGroup.LANG> langs1, Vector<WordGroup.LANG> langs2){
		mNames = names;
		mLangs1 = langs1;
		mLangs2 = langs2;
		layoutInflater = fragmentActivity.getLayoutInflater();
		mAddButtonText = fragmentActivity.getString(R.string.grouplist_add);
		mOnClickListener = listener;
	}

	@Override
	public int getItemViewType(int position) {
		return (position == getCount()-1) ? TYPE_BUTTON : TYPE_GROUP;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		return mNames.size()+1;
	}
	@Override
	public Object getItem(int position) { return null;}
	@Override
	public long getItemId(int position) { return 0;}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;
		switch (getItemViewType(position)) {

			case TYPE_GROUP:
				if (convertView == null) {
					convertView = layoutInflater.inflate(R.layout.grouplist_listview_item, parent, false);
					viewHolder = new ViewHolder();

					viewHolder.img1 = (ImageView) convertView.findViewById(R.id.grouplist_listview_item_img1);
					viewHolder.img2 = (ImageView) convertView.findViewById(R.id.grouplist_listview_item_img2);
					viewHolder.text = (TextView) convertView.findViewById(R.id.grouplist_listview_item_text);

					convertView.setTag(viewHolder);
				} else viewHolder = (ViewHolder) convertView.getTag();

				if(mLangs1.elementAt(position) == null){
					throw new RuntimeException();
				}

				viewHolder.img1.setImageResource(mLangs1.elementAt(position).getFlagId());
				int id2 = mLangs2.elementAt(position).getFlagId();
				if(id2 != 0){
					viewHolder.img2.setVisibility(View.VISIBLE);
					viewHolder.img2.setImageResource(id2);
				} else{
					viewHolder.img2.setVisibility(View.GONE);
				}
				viewHolder.text.setText(mNames.elementAt(position));

				return convertView;

			case TYPE_BUTTON:
				if (convertView == null) {
					convertView = layoutInflater.inflate(R.layout.usable_addbutton, parent, false);
					convertView.findViewById(R.id.usable_addbutton_button).setOnClickListener(mOnClickListener);
					((TextView)convertView.findViewById(R.id.usable_addbutton_text)).setText(mAddButtonText);
				}
				return convertView;

			default:
				throw new IllegalArgumentException("Grouplistadapter: type should be 0 or 1");
		}
	}

	private static class ViewHolder{
		ImageView img1;
		ImageView img2;
		TextView text;
	}
}


