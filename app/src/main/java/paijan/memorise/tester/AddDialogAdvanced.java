package paijan.memorise.tester;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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

import paijan.memorise.R;

public class AddDialogAdvanced extends DialogFragment {
	private static final int ALTERNATIVE_COUNT = 3;

	private AddDialog.WordDataHolder mDataHolder;
	private EditText mEditTextHelp;
	private EditText[] mEditTextAlt;

	public static AddDialogAdvanced newInstance(AddDialog.WordDataHolder data){
		AddDialogAdvanced fragment = new AddDialogAdvanced();
		fragment.mDataHolder = data;
		return fragment;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@Override
	public AlertDialog getDialog() { return ((AlertDialog)super.getDialog()); }

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		if(android.os.Build.VERSION.SDK_INT >= 11){
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_Tester);
		} else {
			builder = new AlertDialog.Builder(getActivity());
		}
		builder.setPositiveButton(R.string.ok, mOnOkListener);
		setStyle(STYLE_NO_TITLE, R.style.Theme_DialogTheme_Tester);
		return builder.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tester_dialog_add_advanced, container, false);
		getDialog().setView(view);

		mEditTextHelp = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_comment);
		mEditTextHelp.setText(mDataHolder.help);
		if(mEditTextHelp.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}


		mEditTextAlt = new EditText[ALTERNATIVE_COUNT];
		mEditTextAlt[0] = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_edittext1);
		mEditTextAlt[1] = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_edittext2);
		mEditTextAlt[2] = (EditText) view.findViewById(R.id.tester_dialog_add_advanced_edittext3);
		for(int i=1; i<mDataHolder.lang.size(); i++){
			mEditTextAlt[i-1].setText(mDataHolder.lang.elementAt(i));
		}
		mEditTextAlt[ALTERNATIVE_COUNT-1].setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditTextAlt[ALTERNATIVE_COUNT-1].setOnEditorActionListener(mOnEditTextOkListener);

		return super.onCreateView(inflater, container, savedInstanceState);
	}


	@Override
	public void onStart() {
		super.onStart();

		int windowWidth;
		if(android.os.Build.VERSION.SDK_INT >= 13){
			Point p = new Point();
			getActivity().getWindowManager().getDefaultDisplay().getSize(p);
			windowWidth = p.x;
		} else //noinspection deprecation
			windowWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();


		WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
		lp.gravity = Gravity.BOTTOM;
		lp.width = windowWidth;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		getDialog().getWindow().setAttributes(lp);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listeners

	DialogInterface.OnClickListener mOnOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mDataHolder.help = mEditTextHelp.getText().toString().trim();
			mDataHolder.lang.setSize(1);

			for(int i=0; i<ALTERNATIVE_COUNT; i++){
				String str = mEditTextAlt[i].getText().toString().trim();
				if(!str.equals("")) {
					mDataHolder.lang.add(str);
				}
			}
			((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
		}
	};

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

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
