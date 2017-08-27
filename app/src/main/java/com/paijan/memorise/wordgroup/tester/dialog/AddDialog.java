package com.paijan.memorise.wordgroup.tester.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.AlertDialogFragment;
import com.paijan.memorise.convenience.TextWatcherAdapter;
import com.paijan.memorise.convenience.WordGroupBase;
import com.paijan.memorise.wordgroup.tester.WordGroup;
import com.paijan.memorise.wordgroup.tester.Word;

public class AddDialog extends AlertDialogFragment {
	private EditText mEditText1, mEditText2;

	private AddDialogAdvanced.AdvancedLangHolder mLang1Holder, mLang2Holder;
	private DialogListenerBase mOkListener;
	private boolean mEditing;

	private Word mWord;
	private WordGroupBase.Lang mLang1, mLang2;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Setup

	public static AddDialog newInstance(AddDialogListener listener, WordGroup wordGroup) {
		return newInstance(listener, wordGroup, new Word(), false);
	}
	public static AddDialog newInstance(EditDialogListener listener, WordGroup wordGroup, Word word) {
		return newInstance(listener, wordGroup, word, true);
	}
	private static AddDialog newInstance(DialogListenerBase listener, WordGroup wordGroup, Word word, boolean editing){
		AddDialog fragment = new AddDialog();
		fragment.mOkListener = listener;
		fragment.mLang1 = wordGroup.getLang1();
		fragment.mLang2 = wordGroup.getLang2();
		fragment.mWord = word;
		fragment.mEditing = editing;
		return fragment;
	}

	@Override
	protected View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tester_dialog_add, container, false);
	}

	@Override
	protected String getTitle () {
		return getString(R.string.group_edit);
	}

	@Override
	protected AnswerType getAnswerType () {
		return AnswerType.OK_CANCEL;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	protected void onClickPositive () {
		mLang1Holder.words.add(0, (mEditText1.getText().toString()).trim());
		mLang2Holder.words.add(0, (mEditText2.getText().toString()).trim());
		Word resultWord = new Word(mLang1Holder.words, mLang2Holder.words, mLang1Holder.help, mLang2Holder.help);
		if(mEditing){
			((EditDialogListener)mOkListener).onEdit(resultWord);
		} else {
			((AddDialogListener)mOkListener).onAdd(resultWord);
		}
	}

	@Override
	public void onViewSetup (View view) {
		((ImageView) view.findViewById(R.id.tester_dialog_add_image1)).setImageResource(mLang1.getFlagId());
		((ImageView) view.findViewById(R.id.tester_dialog_add_image2)).setImageResource(mLang2.getFlagId());

		String[] texts = getResources().getStringArray(R.array.tester_add_langs);
		((TextView)view.findViewById(R.id.tester_dialog_add_text1)).setText(texts[mLang1.ordinal()]);
		((TextView)view.findViewById(R.id.tester_dialog_add_text2)).setText(texts[mLang2.ordinal()]);

		mEditText1 = (EditText) view.findViewById(R.id.tester_dialog_add_edittext1);
		mEditText1.addTextChangedListener(mTextWatcher);

		if(mEditText1.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		mEditText2 = (EditText) view.findViewById(R.id.tester_dialog_add_edittext2);
		mEditText2.addTextChangedListener(mTextWatcher);

		mEditText2.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditText2.setOnEditorActionListener(mOnEditTextOkListener);

		ImageButton imageButton1 = (ImageButton) view.findViewById(R.id.tester_dialog_add_button1);
		imageButton1.setOnClickListener(new View.OnClickListener() { @Override public void onClick (View v) { showAdvancedDialog(Word.LangOrdinal.first); } });
		ImageButton imageButton2 = (ImageButton) view.findViewById(R.id.tester_dialog_add_button2);
		imageButton2.setOnClickListener(new View.OnClickListener() { @Override public void onClick (View v) { showAdvancedDialog(Word.LangOrdinal.second); } });

		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true);
		imageButton1.setBackgroundResource(typedValue.resourceId);
		imageButton2.setBackgroundResource(typedValue.resourceId);

		mLang1Holder = new AddDialogAdvanced.AdvancedLangHolder(mWord, Word.LangOrdinal.first);
		mLang2Holder = new AddDialogAdvanced.AdvancedLangHolder(mWord, Word.LangOrdinal.second);
	}

	@Override
	public void onStart() {
		super.onStart();
		fillEditTexts();
	}
	private void fillEditTexts() {
		String langFirst1 = mWord.getLangFirst1();
		mEditText1.setText(langFirst1);
		mEditText1.setSelection(langFirst1.length());
		mEditText2.setText(mWord.getLangFirst2());
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	private TextWatcher mTextWatcher = new TextWatcherAdapter() {
		@Override
		public void afterTextChanged(Editable s) {
			Button btnOk = getPositiveButton();
			if (btnOk != null && mEditText1 != null && mEditText2 != null){
				btnOk.setEnabled(!(mEditText1.getText().toString().equals("") || mEditText2.getText().toString().equals("")));
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

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	private void showAdvancedDialog(Word.LangOrdinal which) {
		AddDialogAdvanced.AdvancedLangHolder holder = ((which == Word.LangOrdinal.first) ? mLang1Holder : mLang2Holder);
		AddDialogAdvanced.newInstance(holder).show(getActivity(), "advanced_dialog");
	}

	private interface DialogListenerBase{}
	public interface AddDialogListener extends DialogListenerBase {
		void onAdd(Word word);
	}
	public interface EditDialogListener extends DialogListenerBase {
		void onEdit(Word word);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
