package com.paijan.memorise.grouplist.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.paijan.memorise.grouplist.manager.WordGroupHeader;
import com.paijan.memorise.grouplist.manager.WordGroupManager;

public class EditDialog extends AlertDialogFragment {
	private EditText mEditText;
	private TextView mWarningTextView;

	private WordGroupHeader mPrevious;
	private EditDialogListener mEditDialogListener;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog setup

	public static EditDialog newInstance (@NonNull EditDialogListener editDialogListener, WordGroupHeader previous) {
		EditDialog fragment = new EditDialog();
		fragment.mPrevious = previous;
		fragment.mEditDialogListener = editDialogListener;
		return fragment;
	}

	@Override
	protected String getTitle () {
		return getString(R.string.grouplist_edit);
	}

	@Override
	protected AnswerType getAnswerType () {
		return AnswerType.OK_CANCEL;
	}

	@Override
	protected View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.grouplist_dialog_edit, container, false);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public void onViewSetup (View view) {
		mWarningTextView = (TextView) view.findViewById(R.id.grouplist_edit_text_warning);
		mWarningTextView.addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged (Editable s) {
				if (s.toString().trim().length() == 0) mWarningTextView.setVisibility(View.GONE);
				else mWarningTextView.setVisibility(View.VISIBLE);
			}
		});

		mEditText = (EditText) view.findViewById(R.id.group_edit_dialog_edittext);
		mEditText.selectAll();
		mEditText.addTextChangedListener(mTextWatcher);
		mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditText.setOnEditorActionListener(mOnEditTextOkListener);
		if (mEditText.requestFocus()) {
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	}

	@Override
	public void onStart () {
		super.onStart();
		mEditText.setText(mPrevious.getName()); //called so late, because okButton is uninitialized in onViewSetup
		mEditText.selectAll();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	@Override
	protected void onClickPositive () {
		String name = mEditText.getText().toString().trim();
		if (!name.equals(mPrevious.getName())) {
			mEditDialogListener.onEdit(mPrevious, name);
		}
	}

	TextWatcher mTextWatcher = new TextWatcherAdapter() {
		@Override
		public void afterTextChanged (Editable s) {
			Button btnOk = getPositiveButton();
			if (btnOk != null) {
				WordGroupManager.ValidationResult validation = mEditDialogListener.onValidate(getHeader());
				if (s.toString().trim().equals(mPrevious.getName())) validation = WordGroupManager.ValidationResult.available;

				btnOk.setEnabled(validation == WordGroupManager.ValidationResult.available);
				switch (validation) {
					case available:
						mWarningTextView.setText("");
						break;
					case taken:
						mWarningTextView.setText(R.string.file_exists);
						break;
					case illegal:
						mWarningTextView.setText(R.string.file_illegal);
						break;
					case empty:
						mWarningTextView.setText(R.string.file_empty);
						break;
				}
			}
		}
	};

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onClickPositive();
				dismiss();
				return true;
			}
			return false;
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	private WordGroupHeader getHeader(){
		return WordGroupHeader.newInstance(mEditText.getText().toString().trim(), mPrevious.getLang1(), mPrevious.getLang2());
	}

	public interface EditDialogListener {
		void onEdit (WordGroupHeader oldHeader, String newName);
		WordGroupManager.ValidationResult onValidate (WordGroupHeader header);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}


