package paijan.memorise.general;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import paijan.memorise.R;

abstract public class BackupSaveDialogBase extends DialogFragment {

	protected WordGroupManager mWordGroupManager;
	protected WordGroup mWordGroup;
	protected EditText mEditText;
	protected TextView mWarningTextView;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	abstract protected File getTypedFile();

	@Override
	public AlertDialog getDialog() { return ((AlertDialog)super.getDialog()); }

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		//TODO clean up the redundant OS checks
		if(Build.VERSION.SDK_INT >= 11){
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_List);
		} else{
			builder = new AlertDialog.Builder(getActivity());
		}
		builder.setTitle(R.string.group_backup_save);
		builder.setPositiveButton(R.string.ok, onOkListener);
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.group_backup_save, container);
		getDialog().setView(view);
		mWordGroupManager.reload();

		mWarningTextView = (TextView) view.findViewById(R.id.group_backup_save_textview);
		mWarningTextView.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
			@Override public void afterTextChanged(Editable s) {
				if(s.toString().trim().length()==0) mWarningTextView.setVisibility(View.GONE);
				else mWarningTextView.setVisibility(View.VISIBLE);
			}
		});

		mEditText = (EditText) view.findViewById(R.id.group_backup_save_edittext);
		mEditText.addTextChangedListener(mTextWatcher);
		getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
			@Override public void onShow(DialogInterface dialog) {
				mEditText.setText("");
			}
		});
		if(mEditText.requestFocus())
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override public void afterTextChanged(Editable s) {
			WordGroupManager.VALIDATION_RESULT validation = mWordGroupManager.validateNameFull(s.toString());

			Button okButton = BackupSaveDialogBase.this.getDialog().getButton(DialogInterface.BUTTON_POSITIVE);
			if(okButton!=null) okButton.setEnabled(validation== WordGroupManager.VALIDATION_RESULT.available);

			switch(validation){
				case available: mWarningTextView.setText(""); break;
				case taken: mWarningTextView.setText(R.string.file_exists); break;
				case illegal: mWarningTextView.setText(R.string.file_illegal); break;
				case empty: mWarningTextView.setText(R.string.file_empty); break;
			}
		}
	};

	private DialogInterface.OnClickListener onOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mWordGroup.save(getTypedFile());
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
