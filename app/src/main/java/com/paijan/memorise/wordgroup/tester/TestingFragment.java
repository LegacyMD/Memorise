package com.paijan.memorise.wordgroup.tester;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.ListViewAdapterBase;

import java.util.ArrayList;

public class TestingFragment extends Fragment {
	public enum Active_View {test, empty, results, success}

	private View mViewEmpty, mViewTest, mViewResults, mViewSuccess;
	private Active_View mActiveView;

	private int RES_FLAG1, RES_FLAG2;
	private ImageView mFlag1, mFlag2;
	private TextView mTextWord, mTextComment;
	private EditText mEditText;

	private WordGroup mWordGroup;
	private OnFragmentReadyListener mOnFragmentReadyListener;
	private OnAnswerListener mOnAnswerListener;
	private KeyboardManager mKeyboardManager;
	private TestExecutor mTestExecutor;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Create

	public static TestingFragment newInstance(WordGroup wordGroup, OnFragmentReadyListener readyListener, OnAnswerListener answerListener, KeyboardManager keyboardManager) {
		TestingFragment result = new TestingFragment();
		result.mWordGroup = wordGroup;
		result.mOnFragmentReadyListener = readyListener;
		result.mOnAnswerListener = answerListener;
		result.mKeyboardManager = keyboardManager;
		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = initRootView(inflater);
		initFlags();
		startTest();
		mOnFragmentReadyListener.onReady();
		return rootView;
	}
	private View initRootView(LayoutInflater inflater) {
		LinearLayout view = new LinearLayout(getActivity());

		view.addView(mViewEmpty = inflater.inflate(R.layout.usable_empty, view, false));
		((TextView) mViewEmpty.findViewById(R.id.usable_empty_text)).setText(getString(R.string.group_empty));
		mViewEmpty.findViewById(R.id.usable_empty_button).setVisibility(View.GONE);

		view.addView(mViewTest = inflater.inflate(R.layout.tester_test, view, false));
		mTextWord = (TextView) view.findViewById(R.id.tester_test_word);
		mTextComment = (TextView) view.findViewById(R.id.tester_test_comment);
		mEditText = (EditText) view.findViewById(R.id.tester_test_edittext);
		mEditText.setOnEditorActionListener(mOnEditTextOkListener);

		view.addView(mViewResults = inflater.inflate(R.layout.usable_listview, view, false));

		view.addView(mViewSuccess = inflater.inflate(R.layout.tester_success, view, false));
		view.findViewById(R.id.tester_success_button).setOnClickListener(mOnRetryClickListener);
		return view;
	}
	private void initFlags() {
		mFlag1 = (ImageView) mViewTest.findViewById(R.id.tester_test_flag1);
		mFlag2 = (ImageView) mViewTest.findViewById(R.id.tester_test_flag2);
		RES_FLAG1 = mWordGroup.getLang1().getFlagId();
		RES_FLAG2 = mWordGroup.getLang2().getFlagId();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

	public void startTest() {
		if (mEditText != null) {
			mTestExecutor = mWordGroup.getNewTestExecutor(mMistakeListener);
			manageView(mTestExecutor.nextWord());
		}
	}

	private void manageView(TestExecutor.AskedWordData askedWordData) {
		switch (askedWordData.state) {
			case empty:
				setActiveView(Active_View.empty);
				break;

			case next:
				setViewTestItems(askedWordData);
				setActiveView(Active_View.test);
				mEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				break;

			case last:
				setViewTestItems(askedWordData);
				setActiveView(Active_View.test);
				mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				break;

			case end:
				if (mTestExecutor.getMistakeHolders().isEmpty()) {
					setActiveView(Active_View.success);
				} else {
					setViewResultsItems();
					setActiveView(Active_View.results);
				}
				mKeyboardManager.hideKeyboard();
				break;
		}
	}

	public void setViewTestItems(TestExecutor.AskedWordData askedWordData) {
		switch (askedWordData.whichLang) {
			case first:
				mFlag1.setImageResource(RES_FLAG2);
				mFlag2.setImageResource(RES_FLAG1);
				break;
			case second:
				mFlag1.setImageResource(RES_FLAG1);
				mFlag2.setImageResource(RES_FLAG2);
				break;
		}

		mTextWord.setText(askedWordData.question);
		mTextComment.setText(askedWordData.comment);
		if (askedWordData.comment.equals("")) {
			mTextComment.setVisibility(View.GONE);
		} else {
			mTextComment.setVisibility(View.VISIBLE);
		}
		mEditText.setText("");
	}
	public void setViewResultsItems() {
		ListView listView = (ListView) mViewResults;
		listView.setAdapter(new ResultsListViewAdapter(mOnRetryClickListener));
		listView.setOnItemClickListener(mOnResultsItemClickListener);
	}

	public void setActiveView(Active_View activeView) {
		if (activeView != mActiveView) {
			setViewsGone();
			switch (mActiveView = activeView) {
				case empty:
					mViewEmpty.setVisibility(View.VISIBLE);
					break;
				case test:
					mViewTest.setVisibility(View.VISIBLE);
					break;
				case results:
					mViewResults.setVisibility(View.VISIBLE);
					break;
				case success:
					mViewSuccess.setVisibility(View.VISIBLE);
					break;
			}
		}
	}
	private void setViewsGone() {
		mViewEmpty.setVisibility(View.GONE);
		mViewTest.setVisibility(View.GONE);
		mViewResults.setVisibility(View.GONE);
		mViewSuccess.setVisibility(View.GONE);
	}

	private void answer() {
		answer(mEditText.getText().toString());
	}
	private void answer(String answer) {
		mTestExecutor.answer(answer);
		manageView(mTestExecutor.nextWord());
		mOnAnswerListener.onAnswer();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	private AdapterView.OnItemClickListener mOnResultsItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mWordGroup != null) {
				ArrayList<String> answers = mTestExecutor.getMistakeHolders().get(position).answers;
				int answerCount = answers.size();
				String message = getResources().getQuantityString(R.plurals.tester_results_correct, answers.size());
				if (answerCount == 1) {
					message += (" " + answers.get(0));
				} else {
					for (String answer : answers) {
						message += "-" + answer + "\n";
					}
				}
				Toast.makeText(TestingFragment.this.getActivity(), message.trim(), Toast.LENGTH_LONG).show();
			}
		}
	};

	private TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
				answer();
				return true;
			}
			return false;
		}
	};

	private View.OnClickListener mOnRetryClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startTest();
			mKeyboardManager.showKeyboard(getEditText());
		}
	};

	private TestExecutor.MistakeListener mMistakeListener = new TestExecutor.MistakeListener() {
		@Override
		public void mistake() {
			Toast.makeText(TestingFragment.this.getActivity(), R.string.tester_mistake, Toast.LENGTH_SHORT).show();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc


	public EditText getEditText() {
		return mEditText;
	}
	public Active_View getActiveView() {
		return mActiveView;
	}

	public interface KeyboardManager {
		void hideKeyboard();
		void showKeyboard(View view);
	}

	public interface OnFragmentReadyListener {
		void onReady();
	}

	public interface OnAnswerListener {
		void onAnswer();
	}

	private class ResultsListViewAdapter extends ListViewAdapterBase {
		private final int MISTAKE_COLOR;

		private LayoutInflater mLayoutInflater;
		private ArrayList<TestExecutor.MistakeHolder> mMistakeHolders;
		private View.OnClickListener mOnClickListener;

		public ResultsListViewAdapter(View.OnClickListener onClickListener) {
			mLayoutInflater = getActivity().getLayoutInflater();
			mMistakeHolders = mTestExecutor.getMistakeHolders();
			mOnClickListener = onClickListener;
			MISTAKE_COLOR = ContextCompat.getColor(getContext(), R.color.TextTesterMistake);
		}

		@Override
		protected int getItemCount() {
			return mMistakeHolders.size();
		}

		@Override
		protected View getViewItem(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.tester_listview_item, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.text1 = (TextView) convertView.findViewById(R.id.tester_listview_item_text1);
				viewHolder.text2 = (TextView) convertView.findViewById(R.id.tester_listview_item_text2);
				convertView.setTag(viewHolder);
			} else viewHolder = (ViewHolder) convertView.getTag();

			TestExecutor.MistakeHolder mistakeHolder = mMistakeHolders.get(position);
			viewHolder.text1.setText(mistakeHolder.lang1);
			viewHolder.text2.setText(mistakeHolder.lang2);

			switch (mistakeHolder.whichFailed) {
				case first:
					viewHolder.text1.setTextColor(MISTAKE_COLOR);
					break;
				case second:
					viewHolder.text2.setTextColor(MISTAKE_COLOR);
					break;
			}
			return convertView;
		}

		@Override
		protected View getViewButton(View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.usable_addbutton, parent, false);
				((TextView) convertView.findViewById(R.id.usable_empty_text)).setText(R.string.tester_results_retry);
				ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.usable_empty_button);
				imageButton.setOnClickListener(mOnClickListener);
				imageButton.setImageResource(R.drawable.ic_action_refresh);
			}
			return convertView;
		}

		private class ViewHolder {
			TextView text1, text2;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
