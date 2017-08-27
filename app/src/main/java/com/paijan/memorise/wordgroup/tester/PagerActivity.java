package com.paijan.memorise.wordgroup.tester;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.OnPageChangeListenerAdapter;
import com.paijan.memorise.convenience.ToastWrapper;
import com.paijan.memorise.grouplist.GroupListActivity;


public class PagerActivity extends AppCompatActivity implements TestingFragment.OnAnswerListener, TestingFragment.KeyboardManager, ManageFragment.OnWordGroupChangeListener, TestingFragment.OnFragmentReadyListener {
	private static final int PAGE_ORDINAL_TESTING = 0;
	private static final int PAGE_ORDINAL_MANAGE = 1;

	private WordGroup mWordGroup;
	private ViewPager mViewPager;
	private ToastWrapper mToastWrapper;
	private TestingFragment mTestingFragment;
	private ManageFragment mManageFragment;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Create

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tester_activity);

		initWordGroupTester();
		initViewPagerStrip();
		initViewPager();
		initFragments();
		initToastWrapper();
	}
	private void initWordGroupTester() {
		mWordGroup = (WordGroup) getIntent().getSerializableExtra(GroupListActivity.KEY_WORD_GROUP);
		setTitle(mWordGroup.getName());
	}
	private void initViewPagerStrip() {
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.word_tester_detials_strip);
		int tabColor = ContextCompat.getColor(this, R.color.BlueHolo);
		pagerTabStrip.setTabIndicatorColor(tabColor);
		pagerTabStrip.setTextColor(tabColor);
	}
	private void initViewPager() {
		mViewPager = (ViewPager) findViewById(R.id.word_tester_details_viewpager);
		mViewPager.setAdapter(new ViewPagerAdapter());
		mViewPager.addOnPageChangeListener(new OnPageChangeListenerAdapter(){ @Override public void onPageSelected(int position) { onPageChange(position); } });
	}
	private void initFragments() {
		mTestingFragment = TestingFragment.newInstance(mWordGroup);
		mManageFragment = ManageFragment.newInstance(mWordGroup);
	}
	private void initToastWrapper() {
		mToastWrapper = new ToastWrapper(this);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners for fragments communication

	@Override
	public void onFragmentReady() {
		onPageChange(0);
	}

	@Override
	public void onWordGroupChange() {
		mTestingFragment.startTest();
	}

	@Override
	public void onAnswer() {
		mManageFragment.notifyListView();
	}

	@Override
	public void showKeyboard(View view) {
		if (view.requestFocus()) {
			getInputMethodManager().showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	@Override
	public void hideKeyboard() {
		getInputMethodManager().hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	private InputMethodManager getInputMethodManager() {
		return (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	private IBinder getWindowToken() {
		return getWindow().getDecorView().getWindowToken();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	public ToastWrapper getToastWrapper() {
		return mToastWrapper;
	}

	private void onPageChange(int position) {
		switch (position) {
			case PAGE_ORDINAL_TESTING:
				switch (mTestingFragment.getActiveView()) {
					case empty:
						mViewPager.setCurrentItem(PAGE_ORDINAL_MANAGE);
						break;
					case test:
						showKeyboard(mTestingFragment.getEditText());
						break;
				}
				break;
			case PAGE_ORDINAL_MANAGE:
				hideKeyboard();
				break;
			default:
				throw new RuntimeException(getClass().getName() + ".onPageChange(), position=" + position);
		}
	}

	private class ViewPagerAdapter extends FragmentPagerAdapter {
		private final String[] tab_names;

		public ViewPagerAdapter() {
			super(getSupportFragmentManager());
			tab_names = getResources().getStringArray(R.array.group_tabnames);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return mTestingFragment;
				case 1:
					return mManageFragment;
				default:
					throw new RuntimeException(getClass().getName() + ".getItem() position=" + position);
			}
		}

		@Override
		public int getCount() { return 2; }

		@Override
		public CharSequence getPageTitle(int position) {
			return (position < tab_names.length) ? tab_names[position] : getString(R.string.sample_short);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
