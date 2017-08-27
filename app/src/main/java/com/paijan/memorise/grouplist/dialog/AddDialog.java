package com.paijan.memorise.grouplist.dialog;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.AlertDialogFragment;
import com.paijan.memorise.convenience.OnItemSelectedListenerAdapter;
import com.paijan.memorise.convenience.TextWatcherAdapter;
import com.paijan.memorise.convenience.WordGroupBase;
import com.paijan.memorise.grouplist.manager.WordGroupHeader;
import com.paijan.memorise.grouplist.manager.WordGroupManager;

public class AddDialog extends AlertDialogFragment {
	private final static int TYPE_SPINNER_TEST_POSITION = 0;
	private final static int TYPE_SPINNER_LIST_POSITION = 1;

	private EditText mEditName;
	private Spinner mSpinnerLang1;
	private Spinner mSpinnerLang2;
	private Spinner mSpinnerType;
	private TextView mWarningTextView;
	private TextView mLanguagesTextView;
	private AddDialogListener mAddDialogListener;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Dialog setup

	public static AddDialog newInstance(AddDialogListener addDialogListener) {
		AddDialog fragment = new AddDialog();
		fragment.mAddDialogListener = addDialogListener;
		return fragment;
	}

	@Override
	protected String getTitle () {
		return getString(R.string.grouplist_add);
	}

	@Override
	protected AnswerType getAnswerType () {
		return AnswerType.OK_CANCEL;
	}

	@Override
	protected View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.grouplist_dialog_add, container, false);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public void onViewSetup (View rootView) {
		mLanguagesTextView = (TextView) rootView.findViewById(R.id.grouplist_dialog_add_spinnerrow_text);

		mWarningTextView = (TextView) rootView.findViewById(R.id.grouplist_add_text_warning);
		mWarningTextView.addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged (Editable s) {
				if (s.toString().trim().length() == 0) mWarningTextView.setVisibility(View.GONE);
				else mWarningTextView.setVisibility(View.VISIBLE);
			}
		});

		mEditName = (EditText) rootView.findViewById(R.id.group_add_dialog_edittext_name);
		mEditName.addTextChangedListener(mEditTextWatcher);
		mEditName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditName.setOnEditorActionListener(mOnEditTextOkListener);
		if (mEditName.requestFocus()) {
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}

		String[] langs = getResources().getStringArray(R.array.grouplist_add_languageslist);
		int resourceId = android.R.layout.simple_spinner_dropdown_item;

		mSpinnerLang1 = (Spinner) rootView.findViewById(R.id.grouplist_add_spinner_lang1);
		mSpinnerLang2 = (Spinner) rootView.findViewById(R.id.grouplist_add_spinner_lang2);
		mSpinnerLang1.setAdapter(new LangSpinnerAdapter(resourceId, langs, mSpinnerLang2));
		mSpinnerLang2.setAdapter(new LangSpinnerAdapter(resourceId, langs, mSpinnerLang1));
		mSpinnerLang1.setOnItemSelectedListener(mOnLangChangeListener);
		mSpinnerLang2.setOnItemSelectedListener(mOnLangChangeListener);
		mSpinnerLang1.setSelection(0);
		mSpinnerLang2.setSelection(1);

		String[] types = getResources().getStringArray(R.array.grouplist_add_typeslist);
		mSpinnerType = (Spinner) rootView.findViewById(R.id.grouplist_add_spinner_type);
		mSpinnerType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, types));
		mSpinnerType.setOnItemSelectedListener(mOnTypeChangeListener);
	}

	@Override
	public void onStart () {
		super.onStart();
		//called so late, because okButton is uninitialized in onViewSetup
		mEditName.setText("");
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	@Override
	protected void onClickPositive () {
		mAddDialogListener.onAdd(getHeader());
	}

	private TextWatcher mEditTextWatcher = new TextWatcherAdapter() {
		@Override
		public void afterTextChanged(Editable s) {
			validate();
		}
	};

	private AdapterView.OnItemSelectedListener mOnLangChangeListener = new OnItemSelectedListenerAdapter() {
		@Override
		public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
			validate();
		}
	};

	private AdapterView.OnItemSelectedListener mOnTypeChangeListener = new OnItemSelectedListenerAdapter() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (mSpinnerLang2 != null) {
				int textResId = 0;
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSpinnerLang1.getLayoutParams();
				switch (position) {
					case TYPE_SPINNER_TEST_POSITION:
						textResId = R.string.grouplist_add_field_langs;
						mSpinnerLang2.setVisibility(View.VISIBLE);
						params.width = 0;
						params.weight = 1;
						fixIdenticalLangs();
						break;

					case TYPE_SPINNER_LIST_POSITION:
						textResId = R.string.grouplist_add_field_lang;
						mSpinnerLang2.setVisibility(View.GONE);
						params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
						params.weight = 0;
						break;
				}
				mSpinnerLang1.setLayoutParams(params);
				mLanguagesTextView.setText(textResId);
				validate();
			}
		}
	};

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onClickPositive();
				dismiss();
				return true;
			}
			return false;
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	private void validate() {
		Button btnOk = getPositiveButton();
		if (btnOk != null) {
			WordGroupManager.ValidationResult validation = mAddDialogListener.onValidate(getHeader());
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

	private void fixIdenticalLangs(){
		int pos1 = mSpinnerLang1.getSelectedItemPosition();
		int pos2 = mSpinnerLang2.getSelectedItemPosition();
		if(pos1 == pos2){
			pos2 = (pos2 == 0) ? 1 : 0;
			mSpinnerLang2.setSelection(pos2);
		}
	}

	private WordGroupHeader getHeader(){
		String name = mEditName.getText().toString().trim();
		WordGroupBase.Lang lang1 = WordGroupBase.Lang.fromOrdinal(mSpinnerLang1.getSelectedItemPosition());
		WordGroupBase.Lang lang2;
		if (mSpinnerType.getSelectedItemPosition() == TYPE_SPINNER_TEST_POSITION)
			lang2 = WordGroupBase.Lang.fromOrdinal(mSpinnerLang2.getSelectedItemPosition());
		else lang2 = WordGroupBase.Lang.VOID;
		return WordGroupHeader.newInstance(name, lang1, lang2);
	}

	public interface AddDialogListener {
		void onAdd(WordGroupHeader header);
		WordGroupManager.ValidationResult onValidate(WordGroupHeader header);
	}

	private class LangSpinnerAdapter extends ArrayAdapter<String> {
		private final String[] mLangs;
		private final Spinner mOtherSpinner;

		public LangSpinnerAdapter (int resource, String[] langs, Spinner otherSpinner) {
			super(AddDialog.this.getContext(), resource, langs);
			mLangs = langs;
			mOtherSpinner = otherSpinner;
		}

		@Override
		public boolean isEnabled(int position) {
			return mOtherSpinner.getVisibility() != View.VISIBLE || mOtherSpinner.getSelectedItemPosition() != position;
		}

		@Override
		public int getCount() {
			return mLangs.length;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) super.getDropDownView(position, convertView, parent);
			view.setTextColor(ContextCompat.getColor(getContext(), isEnabled(position) ? R.color.TextBright : R.color.TextDim));
			return view;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

}