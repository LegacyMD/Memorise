package paijan.memorise.tester;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filterable;

import paijan.memorise.R;
import paijan.memorise.general.WordGroupManager;
import paijan.memorise.grouplist.GroupList;


public class WordTesterPager extends AppCompatActivity {
	private static String[] TAB_NAMES;

	private WordGroupManager mWordGroupManager;
	private WordGroupTester mWordGroupTester;
	private ViewPager mViewPager;
	private WordTesterTest mFragmentTest;
	private WordTesterManage mFragmentManage;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tester);
		if(TAB_NAMES == null) TAB_NAMES = getResources().getStringArray(R.array.group_tabnames);

		PagerTabStrip pagerTabStrip = (PagerTabStrip)findViewById(R.id.word_tester_detials_strip);
		int tabColor = getResources().getColor(R.color.BlueHolo);
		pagerTabStrip.setTabIndicatorColor(tabColor);
		pagerTabStrip.setTextColor(tabColor);

		mWordGroupManager = (WordGroupManager) getIntent().getSerializableExtra(GroupList.KEY_WORD_GROUP_MANAGER);
		mWordGroupTester = (WordGroupTester) getIntent().getSerializableExtra(GroupList.KEY_WORD_GROUP);
		setTitle(mWordGroupTester.getName());

		mViewPager = (ViewPager) findViewById(R.id.word_tester_details_viewpager);
		mFragmentTest = WordTesterTest.newInstance(mWordGroupTester, mOnFragmentReadyListener);
		mFragmentTest.resume();

		mFragmentManage = WordTesterManage.newInstance(mWordGroupTester, new WordTesterManage.OnWordGroupChangeListener() {
			@Override
			public void onChange() {
				mFragmentTest.resume();
			}
		});
		mViewPager.setAdapter(new ViewPagerAdapter(this, mFragmentTest, mFragmentManage));
		mViewPager.addOnPageChangeListener(mOnPageChangeListener);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Action bar
	private MenuItem mSearchView;
	private void refreshSearchViewVisibility(){
		if(mSearchView!=null){
			mSearchView.setVisible(mViewPager.getCurrentItem()==1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group_menu, menu);

		mSearchView = menu.findItem(R.id.group_menu_search);
		refreshSearchViewVisibility();
		SearchView searchView = new SearchView(this);
		mSearchView.setActionView(searchView);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override public boolean onQueryTextSubmit(String query) { return true; }
			@Override public boolean onQueryTextChange(String newText) {
				((Filterable) mFragmentManage.mListView.getAdapter()).getFilter().filter(newText);
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



	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners



	private View.OnClickListener mOnBackupSaveDialogShowListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("backup_save");
			if(prev!=null) fragmentManager.beginTransaction().remove(prev).commit();
			BackupSaveDialog.newInstance(mWordGroupManager, mWordGroupTester).show(fragmentManager, "backup_save");

		}
	};

	private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
		@Override public void onPageScrollStateChanged(int state) { }
		@Override public void onPageSelected(int position) {
			if(position==0) {
				switch (WordTesterPager.this.mFragmentTest.getActiveView()) {
					case empty:
						mViewPager.setCurrentItem(1);
						return;

					case test:
						mFragmentTest.showKeyboard();
						break;
				}
			}
			else mFragmentTest.hideKeyboard();
			refreshSearchViewVisibility();
		}
	};

	private WordTesterTest.OnFragmentReadyListener mOnFragmentReadyListener = new WordTesterTest.OnFragmentReadyListener() {
		@Override
		public void onReady() {
			mOnPageChangeListener.onPageSelected(0);
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}


// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --


class ViewPagerAdapter extends FragmentPagerAdapter{
	private final String[] tab_names;
	private final WordTesterTest mFragmentTest;
	private final WordTesterManage mFragmentManage;

	public ViewPagerAdapter(FragmentActivity activity, WordTesterTest fragmentTest, WordTesterManage fragmentManage){
		super(activity.getSupportFragmentManager());
		tab_names = activity.getResources().getStringArray(R.array.group_tabnames);
		mFragmentTest = fragmentTest;
		mFragmentManage = fragmentManage;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0: return mFragmentTest;
			case 1: return mFragmentManage;
			default: return null;
		}
	}

	@Override public int getCount() { return 2; }
	@Override public CharSequence getPageTitle(int position) { return (position < tab_names.length) ? tab_names[position] : "ERROR_TITLE";	}
}