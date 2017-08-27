package paijan.memorise.tester;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import paijan.memorise.R;

public class WordTesterTest extends Fragment {
	private View mViewEmpty, mViewTest, mViewResults;
	public enum ACTIVE_VIEW {test, empty, results}
	private ACTIVE_VIEW mActiveView;
	public ACTIVE_VIEW getActiveView() {return mActiveView;}

	private WordGroupTester mWordGroupTester;

	private int RES_FLAG1, RES_FLAG2;
	private ImageView mFlag1, mFlag2;


	private TextView mTextWord, mTextComment;
	private EditText mEditText;


	public static WordTesterTest newInstance(WordGroupTester wt, OnFragmentReadyListener arg1){
		WordTesterTest wtm = new WordTesterTest();
		wtm.setWordGroupTester(wt);
		wtm.setOnFragmentReadyListener(arg1);
		return wtm;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout view = new LinearLayout(getActivity());

		view.addView(mViewEmpty = inflater.inflate(R.layout.usable_empty, view, false));
		((TextView)mViewEmpty.findViewById(R.id.usable_empty_text)).setText(getString(R.string.group_empty));
		mViewEmpty.findViewById(R.id.usable_empty_button).setVisibility(View.GONE);

		view.addView(mViewTest = inflater.inflate(R.layout.tester_test, view, false));
		mTextWord = (TextView) view.findViewById(R.id.tester_test_word);
		mTextComment = (TextView) view.findViewById(R.id.tester_test_comment);
		mEditText = (EditText) view.findViewById(R.id.tester_test_edittext);
		mEditText.setOnEditorActionListener(mOnEditTextOkListener);

		mFlag1 = (ImageView) mViewTest.findViewById(R.id.tester_test_flag1);
		mFlag2 = (ImageView) mViewTest.findViewById(R.id.tester_test_flag2);
		RES_FLAG1 = mWordGroupTester.getLang1().getFlagId();
		RES_FLAG2 = mWordGroupTester.getLang2().getFlagId();

		view.addView(mViewResults = inflater.inflate(R.layout.tester_test_results, view, false));

		resume();
		if (mOnFragmentReadyListener != null) {
			mOnFragmentReadyListener.onReady();
		}
		return view;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Tester functions

	public void resume() {
		if (mEditText != null) {
			WordGroupTester.WordDataHolder holder = mWordGroupTester.initiate();
			manageView(holder);
			showKeyboard();
		}
	}

	private void manageView(WordGroupTester.WordDataHolder holder){
		switch (holder.state) {
			case empty:
				setActiveView(ACTIVE_VIEW.empty);
				break;

			case next:
				setViewTestItems(holder);
				setActiveView(ACTIVE_VIEW.test);
				mEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				break;

			case last:
				setViewTestItems(holder);
				setActiveView(ACTIVE_VIEW.test);
				mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				break;

			case end:
				setViewResultsItems();
				setActiveView(ACTIVE_VIEW.results);
				hideKeyboard();
				break;
		}
	}

	public void setViewTestItems(WordGroupTester.WordDataHolder holder){
		if(holder.reversedLangs){
			mFlag1.setImageResource(RES_FLAG2);
			mFlag2.setImageResource(RES_FLAG1);
		} else{
			mFlag1.setImageResource(RES_FLAG1);
			mFlag2.setImageResource(RES_FLAG2);
		}
		mTextWord.setText(holder.word);
		mTextComment.setText(holder.comment);
		if(holder.comment.equals("")){
			mTextComment.setVisibility(View.GONE);
		} else{
			mTextComment.setVisibility(View.VISIBLE);
		}
		mEditText.setText("");
	}

	public void setViewResultsItems(){
		ListView listView = (ListView) mViewResults.findViewById(R.id.tester_test_results_listview);
		listView.setAdapter(new ResultsListViewAdapter(getActivity(), mWordGroupTester.getMistakeHolders(), mOnRetryClickListener));
		listView.setOnItemClickListener(mOnItemClickListener);
	}

	public void setActiveView(ACTIVE_VIEW activeView){
		if(activeView==mActiveView) return;
		switch (mActiveView = activeView){
			case empty:
				mViewEmpty.setVisibility(View.VISIBLE);
				mViewTest.setVisibility(View.GONE);
				mViewResults.setVisibility(View.GONE);
				break;

			case test:
				mViewEmpty.setVisibility(View.GONE);
				mViewTest.setVisibility(View.VISIBLE);
				mViewResults.setVisibility(View.GONE);
				break;

			case results:
				mViewEmpty.setVisibility(View.GONE);
				mViewTest.setVisibility(View.GONE);
				mViewResults.setVisibility(View.VISIBLE);
				break;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mWordGroupTester != null)
			{
				Vector<String> answers = mWordGroupTester.getMistakeHolders().get(position).answers;
				int answerCount = answers.size();
				String message = getResources().getQuantityString(R.plurals.tester_results_correct, answers.size());
				if(answerCount == 1){
					message += (" "+answers.elementAt(0));
				} else{
					for(String answer : answers){
						message+= "-"+answer+"\n";
					}
				}
				Toast.makeText(WordTesterTest.this.getActivity(), message.trim(), Toast.LENGTH_LONG).show();
			}
		}
	};

	public interface OnFragmentReadyListener{
		public void onReady();
	}
	private OnFragmentReadyListener mOnFragmentReadyListener;
	private void setOnFragmentReadyListener(OnFragmentReadyListener l){mOnFragmentReadyListener = l;}

	private TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE){
				WordGroupTester.WordDataHolder holder = mWordGroupTester.answer(mEditText.getText().toString(), mMistakeListener);
				manageView(holder);
				return true;
			}
			return false;
		}
	};

	private View.OnClickListener mOnRetryClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			resume();
		}
	};

	private WordGroupTester.MistakeListener mMistakeListener = new WordGroupTester.MistakeListener() {
		@Override
		public void mistake() {
			Toast.makeText(WordTesterTest.this.getActivity(), R.string.tester_mistake, Toast.LENGTH_SHORT).show();
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC

	public void setWordGroupTester(WordGroupTester tester){
		mWordGroupTester = tester;
	}

	public void hideKeyboard() {
		InputMethodManager imm = ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	public void showKeyboard() {
		if(mEditText.requestFocus()) {
			InputMethodManager imm = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
			imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}


// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --


class ResultsListViewAdapter extends BaseAdapter{
	private static final int TYPE_WORD = 0;
	private static final int TYPE_BUTTON = 1;
	private static final int TYPE_SUCCESS = 2;
	private final int MISTAKE_COLOR, SUCCESS_COLOR;
	private final String mAddButtonText;

	private LayoutInflater mLayoutInflater;
	private Vector<WordGroupTester.MistakeHolder> mMistakeHolders;
	private View.OnClickListener mOnClickListener;

	public ResultsListViewAdapter(FragmentActivity activity, Vector<WordGroupTester.MistakeHolder> mistakeHolders, View.OnClickListener onClickListener){
		mLayoutInflater = activity.getLayoutInflater();
		mMistakeHolders = mistakeHolders;
		mOnClickListener = onClickListener;
		MISTAKE_COLOR = activity.getResources().getColor(R.color.TextTesterMistake);
		SUCCESS_COLOR = activity.getResources().getColor(R.color.TextTesterSuccess);
		mAddButtonText = activity.getString(R.string.tester_results_retry);
	}

	@Override public int getItemViewType(int position) {
		if(position==getCount()-1)
			return TYPE_BUTTON;
		else if(mMistakeHolders.size()==0)
			return TYPE_SUCCESS;
		else
			return TYPE_WORD;
	}
	@Override public int getViewTypeCount() { return 3; }
	@Override public Object getItem(int position) { return null; }
	@Override public long getItemId(int position) { return 0; }
	@Override
	public int getCount() {
		int size = mMistakeHolders.size();
		return (size==0) ? 2 : size+1;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;
		switch(getItemViewType(position)){
			case TYPE_SUCCESS:
				if(convertView==null){
					TextView textView = new TextView(mLayoutInflater.getContext());
					textView.setText(R.string.tester_results_success);
					textView.setTextColor(SUCCESS_COLOR);
					textView.setGravity(Gravity.CENTER);
					int pad = (int) mLayoutInflater.getContext().getResources().getDimension(R.dimen.margin_level_1);
					textView.setPadding(0, pad, 0, pad);
					convertView = textView;
				}
				return convertView;

			case TYPE_WORD:
				if(convertView==null){
					convertView = mLayoutInflater.inflate(R.layout.tester_listview_item, parent, false);

					viewHolder = new ViewHolder();
					viewHolder.text1 = (TextView) convertView.findViewById(R.id.tester_listview_item_text1);
					viewHolder.text2 = (TextView) convertView.findViewById(R.id.tester_listview_item_text2);
					convertView.setTag(viewHolder);
				} else viewHolder = (ViewHolder) convertView.getTag();

				WordGroupTester.MistakeHolder currentMistake = mMistakeHolders.elementAt(position);
				viewHolder.text1.setText(currentMistake.lang1);
				viewHolder.text2.setText(currentMistake.lang2);

				if(currentMistake.firstFailed)
					viewHolder.text1.setTextColor(MISTAKE_COLOR);
				else
					viewHolder.text2.setTextColor(MISTAKE_COLOR);

				return convertView;

			case TYPE_BUTTON:
				if(convertView==null){
					convertView = mLayoutInflater.inflate(R.layout.usable_addbutton, parent, false);
					((TextView)convertView.findViewById(R.id.usable_addbutton_text)).setText(mAddButtonText);
					ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.usable_addbutton_button);
					imageButton.setOnClickListener(mOnClickListener);
					imageButton.setImageResource(R.drawable.ic_action_refresh);

				}
				return convertView;

			default:
				throw new IllegalArgumentException("ResultsListViewAdapter: type should be 0 or 1");
		}
	}

	private static class ViewHolder {
		TextView text1, text2;
	}
}

