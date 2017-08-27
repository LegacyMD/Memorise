package paijan.memorise.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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

import paijan.memorise.R;

public class AddDialog extends DialogFragment {
	enum MODE {add, edit}

	private MODE mMode;
	private String mStr1, mStr2;
	private EditText mEditText1, mEditText2;
	private Button mBtnOk;

	public static AddDialog newInstance(MODE mode, OnWordListAddListener onWordListAddListener, WordList wordList){
		return newInstance(mode, onWordListAddListener, wordList.getWord(), wordList.getHelp());
	}

	public static AddDialog newInstance(MODE mode, OnWordListAddListener onWordListAddListener){
		return newInstance(mode, onWordListAddListener, "", "");
	}

	public static AddDialog newInstance(MODE mode, OnWordListAddListener onWordListAddListener, String str1, String str2){
		AddDialog fragment = new AddDialog();

		fragment.setMode(mode);
		fragment.setStartString(str1, str2);
		fragment.setOnWordListAddListener(onWordListAddListener);

		return fragment;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;

		if(Build.VERSION.SDK_INT >= 11){
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_List);
		} else{
			builder = new AlertDialog.Builder(getActivity());
		}

		if(mMode == MODE.add) builder.setTitle(R.string.group_add);
		else if(mMode == MODE.edit) builder.setTitle(R.string.group_edit);

		builder.setPositiveButton(R.string.ok, mOnOkClickListener);
		builder.setNegativeButton(R.string.cancel, null);

		return builder.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_dialog_add, container, false);
		((AlertDialog)getDialog()).setView(view);

		getDialog().setOnShowListener(new DialogInterface.OnShowListener() { @Override public void onShow(DialogInterface dialog) { mBtnOk = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE); mTextWatcher.afterTextChanged(null); } });

		mEditText1 = (EditText) view.findViewById(R.id.list_add_dialog_edit1);
		mEditText1.setText(mStr1);
		mEditText1.addTextChangedListener(mTextWatcher);
		if(mEditText1.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		mEditText2 = (EditText) view.findViewById(R.id.list_add_dialog_edit2);
		mEditText2.setText(mStr2);
		mEditText2.addTextChangedListener(mTextWatcher);
		mEditText2.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditText2.setOnEditorActionListener(mOnEditTextOkListener);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		int dividerId = getResources().getIdentifier("titleDivider", "id", "android");
		View divider = (getDialog().findViewById(dividerId));
		if(divider != null)	divider.setBackgroundResource(R.color.DialogLine);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	interface OnWordListAddListener{ public void onAdd(String word, String help);}
	private OnWordListAddListener mOnWordListAddListener;
	void setOnWordListAddListener(OnWordListAddListener l) {mOnWordListAddListener = l;}

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void afterTextChanged(Editable s) {
			if (mEditText1!=null && mEditText2!=null && mBtnOk!=null){
				//Setting enabled to logical value
				mBtnOk.setEnabled(!(mEditText1.getText().toString().equals("")));
			}
		}
	};

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE){
				mOnOkClickListener.onClick(getDialog(), Dialog.BUTTON_POSITIVE);
				dismiss();
				return true;
			}
			return false;
		}
	};

	DialogInterface.OnClickListener mOnOkClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mOnWordListAddListener != null) {
				mOnWordListAddListener.onAdd(mEditText1.getText().toString(), mEditText2.getText().toString());
				getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			} else{
				throw new NullListenerException("list.AddDialog, mOnClickListener");
			}
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC

	private void setStartString(String s1, String s2) {mStr1=s1; mStr2=s2;}
	private void setMode(MODE m) {mMode = m;}


	private class NullListenerException extends RuntimeException{
		NullListenerException(String message) {super(message);}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

}
