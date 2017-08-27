package com.paijan.memorise.wordgroup.tester.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.AlertDialogFragment;
import com.paijan.memorise.wordgroup.tester.Word;

import java.util.ArrayList;

public class AddDialogAdvanced extends AlertDialogFragment {
	private static final int ALTERNATIVE_COUNT = 3;

	private AdvancedLangHolder mDataHolder;
	private EditText mEditTextHelp;
	private EditText[] mEditTextAlt;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --  Create

	public static AddDialogAdvanced newInstance(AdvancedLangHolder data){
		AddDialogAdvanced fragment = new AddDialogAdvanced();
		fragment.mDataHolder = data;
		return fragment;
	}

	@Override
	protected View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tester_dialog_add_advanced, container, false);
	}

	@Override
	protected String getTitle () {
		return null;
	}

	@Override
	protected AnswerType getAnswerType () {
		return AnswerType.OK_CANCEL;
	}

	@Override
	protected void onClickPositive () {
		mDataHolder.help = mEditTextHelp.getText().toString().trim();

		mDataHolder.words.clear();
		for (EditText editText : mEditTextAlt) {
			String str = editText.getText().toString().trim();
			if(!str.equals("")) {
				mDataHolder.words.add(str);
			}
		}
		((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public void onViewSetup (View view) {
		setStyle(STYLE_NO_TITLE, R.style.Theme_DialogTheme);

		mEditTextHelp = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_comment);
		mEditTextHelp.setText(mDataHolder.help);
		if(mEditTextHelp.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}

		mEditTextAlt = new EditText[ALTERNATIVE_COUNT];
		mEditTextAlt[0] = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_edittext1);
		mEditTextAlt[1] = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_edittext2);
		mEditTextAlt[2] = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_edittext3);
		for(int i = 0; i<mDataHolder.words.size(); i++){
			mEditTextAlt[i].setText(mDataHolder.words.get(i));
		}
		mEditTextAlt[ALTERNATIVE_COUNT-1].setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditTextAlt[ALTERNATIVE_COUNT-1].setOnEditorActionListener(mOnEditTextOkListener);
	}

	@Override
	public void onStart() {
		super.onStart();

		Point p = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(p);
		int windowWidth = p.x;

		WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
		lp.gravity = Gravity.BOTTOM;
		lp.width = windowWidth;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		getDialog().getWindow().setAttributes(lp);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE){
				getDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
				return true;
			}
			return false;
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	public static class AdvancedLangHolder {
		public ArrayList<String> words;
		public String help;

		public AdvancedLangHolder (Word word, Word.LangOrdinal which){
			switch(which){
				case first:
					help = word.getHelp1();
					words = (getArraySubList(word.getWords1(), 1));
					break;
				case second:
					help = word.getHelp2();
					words = (getArraySubList(word.getWords2(), 1));
					break;
			}
		}
		private static <T> ArrayList<T> getArraySubList (ArrayList<T> arg, int from) {
			ArrayList<T> result = new ArrayList<>();
			for(int i=from; i<arg.size(); i++) {
				result.add(arg.get(i));
			}
			return result;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
