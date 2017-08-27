package paijan.memorise.grouplist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import paijan.memorise.general.WordGroupManager;

public class EditDialog extends DialogFragment{
	private String mPrevious;
	private EditText mEditText;
	private Button mBtnOk;
	private TextView mWarningTextView;

	public static EditDialog newInstance(OnValidateListener validateListener, OnGroupEditListener editListener, String previous){
		EditDialog fragment = new EditDialog();
		fragment.mPrevious = previous.trim();
		fragment.mOnValidateListener = validateListener;
		fragment.mOnGroupEditListener = editListener;
		return fragment;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	@Override public AlertDialog getDialog() { return ((AlertDialog) super.getDialog()); }

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		if(android.os.Build.VERSION.SDK_INT >= 11){
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_Grouplist);
		} else {
			builder = new AlertDialog.Builder(getActivity());
		}
		builder.setTitle(R.string.grouplist_edit);
		builder.setPositiveButton(getResources().getString(R.string.ok), mOnBtnOkListener);
		builder.setNegativeButton(getResources().getString(R.string.cancel), null);
		return builder.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.grouplist_dialog_edit, container, false);
		getDialog().setView(view);

		mWarningTextView = (TextView) view.findViewById(R.id.grouplist_edit_text_warning);
		mWarningTextView.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
			@Override public void afterTextChanged(Editable s) {
				if(s.toString().trim().length()==0) mWarningTextView.setVisibility(View.GONE);
				else mWarningTextView.setVisibility(View.VISIBLE);
			}
		});

		getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				mBtnOk = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
				mTextWatcher.afterTextChanged(mEditText.getText());
			}
		});

		mEditText = (EditText) view.findViewById(R.id.group_edit_dialog_edittext);
		mEditText.setText(mPrevious);
		mEditText.selectAll();
		mEditText.addTextChangedListener(mTextWatcher);
		mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditText.setOnEditorActionListener(mOnEditTextOkListener);
		if (mEditText.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}

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
	TextWatcher mTextWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void afterTextChanged(Editable s) {
			if(mBtnOk==null) return;
			if(mOnValidateListener != null) {
				WordGroupManager.VALIDATION_RESULT validation = mOnValidateListener.onValidate(s.toString());
				if(s.toString().trim().equals(mPrevious)) validation = WordGroupManager.VALIDATION_RESULT.available;

				mBtnOk.setEnabled(validation == WordGroupManager.VALIDATION_RESULT.available);

				switch(validation){
					case available: mWarningTextView.setText(""); break;
					case taken: mWarningTextView.setText(R.string.file_exists); break;
					case illegal: mWarningTextView.setText(R.string.file_illegal); break;
					case empty: mWarningTextView.setText(R.string.file_empty); break;
				}
			} else{
				throw new NullListenerException("afterTextChanged, mOnValidateListener == null");
			}
		}
	};

	DialogInterface.OnClickListener mOnBtnOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mOnGroupEditListener!=null){
				String name = mEditText.getText().toString().trim();
				if(!name.equals(mPrevious)){
					mOnGroupEditListener.onEdit(name);
				}
			} else{
				throw new NullListenerException("mOnBtnOkListener, mOnGroupEditListener == null");
			}
		}
	};

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_DONE){
				mOnBtnOkListener.onClick(getDialog(), Dialog.BUTTON_POSITIVE);
				dismiss();
				return true;
			}
			return false;
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Activity communication


	public interface OnGroupEditListener {void onEdit(String name);}
	private OnGroupEditListener mOnGroupEditListener;

	public interface OnValidateListener {WordGroupManager.VALIDATION_RESULT onValidate(String name);}
	private OnValidateListener mOnValidateListener;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC
	private class NullListenerException extends RuntimeException{
		NullListenerException(String message) {super(message);}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
