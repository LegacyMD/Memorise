package com.paijan.memorise.wordgroup.list.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.AlertDialogFragment;
import com.paijan.memorise.convenience.TextWatcherAdapter;
import com.paijan.memorise.wordgroup.list.Word;

public class AddDialog extends AlertDialogFragment {
	private EditText mEditTextWord, mEditTextHelp;
	private DialogListenerBase mOkListener;
	private Word mWord;
	private boolean mEditing; //if not, it's adding

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Setup

	public static AddDialog newInstance(AddDialogListener listener){
		return newInstance(new Word(), listener, false);
	}
	public static AddDialog newInstance(Word word, EditDialogListener listener){
		return newInstance(word, listener, true);
	}
	private static AddDialog newInstance(Word word, DialogListenerBase listener, boolean isEditing) {
		AddDialog fragment = new AddDialog();
		fragment.mWord = word;
		fragment.mOkListener = listener;
		fragment.mEditing = isEditing;
		return fragment;
	}

	@Override
	protected View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_dialog_add, container, false);
	}

	@Override
	protected String getTitle () {
		return getString(mEditing ? R.string.group_edit : R.string.group_add);
	}

	@Override
	protected AnswerType getAnswerType () {
		return AnswerType.OK_CANCEL;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --  Callbacks

	@Override
	protected void onClickPositive () {
		Word inputWord = getInputWord();
		if(mEditing) {
			((EditDialogListener) mOkListener).onEdit(inputWord);
		} else {
			((AddDialogListener) mOkListener).onAdd(inputWord);
		}
	}

	@Override
	public void onViewSetup (View view) {
		mEditTextWord = (EditText) view.findViewById(R.id.list_add_dialog_edit1);
		mEditTextWord.addTextChangedListener(mTextWatcher);
		if(mEditTextWord.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		mEditTextHelp = (EditText) view.findViewById(R.id.list_add_dialog_edit2);
		mEditTextHelp.addTextChangedListener(mTextWatcher);
		mEditTextHelp.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditTextHelp.setOnEditorActionListener(mOnEditTextOkListener);
	}

	@Override
	public void onStart () {
		super.onStart();
		mEditTextWord.setText(mWord.getWord());
		mEditTextHelp.setText(mWord.getHelp());
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	TextWatcher mTextWatcher = new TextWatcherAdapter() {
		@Override
		public void afterTextChanged(Editable s) {
			Button btnOk = getPositiveButton();
			if (btnOk != null && mEditTextWord !=null && mEditTextHelp !=null){
				btnOk.setEnabled(!(mEditTextWord.getText().toString().equals("")));
			}
		}
	};

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE){
				onClickPositive();
				dismiss();
				return true;
			}
			return false;
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC

	private Word getInputWord() {
		return new Word(mEditTextWord.getText().toString(), mEditTextHelp.getText().toString());
	}

	private interface DialogListenerBase {}
	public interface AddDialogListener extends DialogListenerBase {
		void onAdd(Word word);
	}
	public interface EditDialogListener extends DialogListenerBase {
		void onEdit(Word word);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

}
