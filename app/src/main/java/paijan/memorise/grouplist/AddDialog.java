package paijan.memorise.grouplist;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
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

import paijan.memorise.R;
import paijan.memorise.general.WordGroup;
import paijan.memorise.general.WordGroupManager;

public class AddDialog extends DialogFragment {

	private Button mBtnOk;
	private EditText mEditName;
	private Spinner mSpinnerLang1;
	private Spinner mSpinnerLang2;
	private Spinner mSpinnerType;
	private TextView mWarningTextView;
	private TextView mLanguagesTextView;

	public static AddDialog newInstance(OnValidateListener validateListener, OnGroupAddListener addListener) {
		AddDialog fragment = new AddDialog();
		fragment.setOnGroupAddListener(addListener);
		fragment.mOnValidateListener = validateListener;
		return fragment;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks


	@Override
	public AlertDialog getDialog() {
		return ((AlertDialog) super.getDialog());
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_Grouplist);
		} else {
			ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), R.style.Theme_App);
			builder = new AlertDialog.Builder(ctw);
		}

		builder.setTitle(R.string.grouplist_add);
		builder.setPositiveButton(R.string.ok, mOnBtnOkListener);
		builder.setNegativeButton(R.string.cancel, null);

		return builder.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.grouplist_dialog_add, container, false);
		getDialog().setView(view);

		mLanguagesTextView = (TextView) view.findViewById(R.id.grouplist_dialog_add_spinnerrow_text);

		mWarningTextView = (TextView) view.findViewById(R.id.grouplist_add_text_warning);
		mWarningTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().trim().length() == 0) mWarningTextView.setVisibility(View.GONE);
				else mWarningTextView.setVisibility(View.VISIBLE);
			}
		});

		mEditName = (EditText) view.findViewById(R.id.group_add_dialog_edittext_name);
		mEditName.addTextChangedListener(mEditTextWatcher);
		mEditName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditName.setOnEditorActionListener(mOnEditTextOkListener);
		if (mEditName.requestFocus()) {
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}

		getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				mBtnOk = getDialog().getButton(DialogInterface.BUTTON_POSITIVE);
				mBtnOk.setTextColor(getResources().getColorStateList(R.color.dialog_button_color));
				mEditName.setText("");
			}
		});

		String[] langs = getResources().getStringArray(R.array.grouplist_add_languageslist);
		int resourceId = android.R.layout.simple_spinner_dropdown_item;

		mSpinnerLang1 = (Spinner) view.findViewById(R.id.grouplist_add_spinner_lang1);
		mSpinnerLang2 = (Spinner) view.findViewById(R.id.grouplist_add_spinner_lang2);
		mSpinnerLang1.setAdapter(new SpinnerAdapter(getActivity(), resourceId, langs, mSpinnerLang2));
		mSpinnerLang2.setAdapter(new SpinnerAdapter(getActivity(), resourceId, langs, mSpinnerLang1));
		mSpinnerLang2.setSelection(1);

		String[] types = getResources().getStringArray(R.array.grouplist_add_typeslist);
		mSpinnerType = (Spinner) view.findViewById(R.id.grouplist_add_spinner_type);
		mSpinnerType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, types));
		mSpinnerType.setOnItemSelectedListener(mOnTypeChangeListener);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		int dividerId = getResources().getIdentifier("titleDivider", "id", "android");
		View divider = (getDialog().findViewById(dividerId));
		if (divider != null) divider.setBackgroundResource(R.color.DialogLine);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	private AdapterView.OnItemSelectedListener mOnTypeChangeListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (mSpinnerLang2 != null) {
				int textResId = 0;
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSpinnerLang1.getLayoutParams();
				switch (position) {
					case 0:
						textResId = R.string.grouplist_add_field_langs;
						mSpinnerLang2.setVisibility(View.VISIBLE);
						params.width = 0;
						params.weight = 1;
						break;

					case 1:
						textResId = R.string.grouplist_add_field_lang;
						mSpinnerLang2.setVisibility(View.GONE);
						params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
						params.weight = 0;
						break;
				}
				mSpinnerLang1.setLayoutParams(params);
				mLanguagesTextView.setText(textResId);
			}
		}
	};

	private DialogInterface.OnClickListener mOnBtnOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (mOnGroupAddListener != null) {
				String name = mEditName.getText().toString().trim();

				WordGroup.LANG[] values = WordGroup.LANG.values();
				WordGroup.LANG lang1 = values[mSpinnerLang1.getSelectedItemPosition()];
				WordGroup.TYPE type = WordGroup.TYPE.values()[mSpinnerType.getSelectedItemPosition()];
				WordGroup.LANG lang2;
				if (type == WordGroup.TYPE.TEST)
					lang2 = values[mSpinnerLang2.getSelectedItemPosition()];
				else lang2 = WordGroup.LANG.VOID;

				mOnGroupAddListener.onAdd(name, lang1, lang2, type);
			} else {
				throw new NullListenerException("mOnBtnOkListener, mOnGroupAddListener == null");
			}
		}
	};

	TextView.OnEditorActionListener mOnEditTextOkListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				mOnBtnOkListener.onClick(getDialog(), Dialog.BUTTON_POSITIVE);
				dismiss();
				return true;
			}
			return false;
		}
	};

	private TextWatcher mEditTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (mBtnOk == null) return;
			if (mOnValidateListener != null) {
				WordGroupManager.VALIDATION_RESULT validation = mOnValidateListener.onValidate(s.toString());
				mBtnOk.setEnabled(validation == WordGroupManager.VALIDATION_RESULT.available);

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
			} else {
				throw new NullListenerException("mEditTextWatcher, mOnValidateListener == null");
			}
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Activity communication

	public interface OnGroupAddListener {
		void onAdd(String name, WordGroup.LANG lang1, WordGroup.LANG lang2, WordGroup.TYPE type);
	}

	private OnGroupAddListener mOnGroupAddListener;

	public void setOnGroupAddListener(OnGroupAddListener l) {
		mOnGroupAddListener = l;
	}

	public interface OnValidateListener {
		WordGroupManager.VALIDATION_RESULT onValidate(String string);
	}

	private OnValidateListener mOnValidateListener;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC
	private class NullListenerException extends RuntimeException {
		NullListenerException(String message) {
			super(message);
		}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}

// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

class SpinnerAdapter extends ArrayAdapter<String> {
	private final String[] mLangs;
	private final Spinner mCorrespondingSpinner;
	private final Resources mRes;

	public SpinnerAdapter(FragmentActivity context, int resource, String[] langs, Spinner correspondingSpinner) {
		super(context, resource, langs);
		mLangs = langs;
		mCorrespondingSpinner = correspondingSpinner;
		mRes = context.getResources();
	}

	@Override
	public boolean isEnabled(int position) {
		return mCorrespondingSpinner.getSelectedItemPosition() != position;
	}

	@Override
	public int getCount() {
		return mLangs.length;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getDropDownView(position, convertView, parent);
		view.setTextColor(isEnabled(position) ? mRes.getColor(R.color.TextSpinnerItem) : mRes.getColor(R.color.TextDim));
		return view;
	}
}

