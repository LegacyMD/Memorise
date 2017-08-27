package paijan.memorise.tester;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.util.Vector;

import paijan.memorise.R;
import paijan.memorise.general.WordGroup;

public class AddDialog extends DialogFragment {
	private static final String KEY_WORD = "wordtester";
	enum MODE {ADD, EDIT}

	private WordTester mWord;
	private WordGroup.LANG mLang1, mLang2;
	private EditText mEditText1, mEditText2;
	private WordDataHolder mLang1Holder, mLang2Holder;
	private Button mBtnOk;
	private MODE mMode;

	public static AddDialog newInstance(WordTester wordTester, WordGroupTester wordGroup, OnWordTesterAddListener listener, MODE mode){
		Bundle args = new Bundle();
		args.putSerializable(KEY_WORD, wordTester);

		AddDialog fragment = new AddDialog();
		fragment.setArguments(args);
		fragment.setLangs(wordGroup.getLang1(), wordGroup.getLang2());
		fragment.setOnWordTesterAddListener(listener);
		fragment.setMode(mode);

		return fragment;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		if(android.os.Build.VERSION.SDK_INT >= 11){
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_Tester);
		} else {
			builder = new AlertDialog.Builder(getActivity());
		}

		if(mMode==MODE.ADD) builder.setTitle(R.string.group_add);
		else if(mMode==MODE.EDIT) builder.setTitle(R.string.group_edit);

		builder.setPositiveButton(R.string.ok, mOnBtnOkListener);
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWord = (WordTester) getArguments().getSerializable(KEY_WORD);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tester_dialog_add, container, false);
		((AlertDialog)getDialog()).setView(view);

		getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				mBtnOk = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
				mTextWatcher.afterTextChanged(null);
			}
		});

		((ImageView) view.findViewById(R.id.tester_dialog_add_image1)).setImageResource(mLang1.getFlagId());
		((ImageView) view.findViewById(R.id.tester_dialog_add_image2)).setImageResource(mLang2.getFlagId());

		String[] texts = getResources().getStringArray(R.array.tester_add_langs);
		((TextView)view.findViewById(R.id.tester_dialog_add_text1)).setText(texts[mLang1.ordinal()]);
		((TextView)view.findViewById(R.id.tester_dialog_add_text2)).setText(texts[mLang2.ordinal()]);

		mEditText1 = (EditText) view.findViewById(R.id.tester_dialog_add_edittext1);
		mEditText1.addTextChangedListener(mTextWatcher);
		mEditText1.setText(mWord.getLangFirst1());
		if(mEditText1.requestFocus()){
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		mEditText2 = (EditText) view.findViewById(R.id.tester_dialog_add_edittext2);
		mEditText2.addTextChangedListener(mTextWatcher);
		mEditText2.setText(mWord.getLangFirst2());
		mEditText2.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mEditText2.setOnEditorActionListener(mOnEditTextOkListener);

		ImageButton imageButton1 = (ImageButton) view.findViewById(R.id.tester_dialog_add_button1);
		imageButton1.setOnClickListener(mOnShowAdvancedListener1);
		ImageButton imageButton2 = (ImageButton) view.findViewById(R.id.tester_dialog_add_button2);
		imageButton2.setOnClickListener(mOnShowAdvancedListener2);
		if(android.os.Build.VERSION.SDK_INT >= 11){
			TypedValue typedValue = new TypedValue();
			getActivity().getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true);
			imageButton1.setBackgroundResource(typedValue.resourceId);
			imageButton2.setBackgroundResource(typedValue.resourceId);
		}

		mLang1Holder = new WordDataHolder(mWord, 1);
		mLang2Holder = new WordDataHolder(mWord, 2);

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
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void afterTextChanged(Editable s) {
			if (mEditText1!=null && mEditText2!=null && mBtnOk!=null){
				//Setting enabled to logical value
				mBtnOk.setEnabled(!(mEditText1.getText().toString().equals("") || mEditText2.getText().toString().equals("")));
			}
		}
	};


	private DialogInterface.OnClickListener mOnBtnOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mOnWordTesterAddListener!=null) {
				mLang1Holder.lang.setElementAt((mEditText1.getText().toString()).trim(), 0);
				mLang2Holder.lang.setElementAt((mEditText2.getText().toString()).trim(), 0);

				mOnWordTesterAddListener.onAdd(mLang1Holder.lang, mLang2Holder.lang, mLang1Holder.help, mLang2Holder.help);
			} else{
				throw new NullListenerException("mOnBtnOkListener: mOnWordTesterAddListener==null");
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

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Advanced dialog utilities

	public static class WordDataHolder {
		public Vector<String> lang;
		public String help;

		public WordDataHolder(WordTester word, int which){
			switch(which){
				case 1:
					help = word.getHelp1();
					lang = word.getLang1();
					break;
				case 2:
					help = word.getHelp2();
					lang = word.getLang2();
					break;
				default:
					throw new IllegalArgumentException("Parameter has to be 1 or 2");
			}
		}
	}

	private View.OnClickListener mOnShowAdvancedListener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("advanced_dialog");
			if(prev!=null){
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.remove(prev);
				fragmentTransaction.commit();
			}
			AddDialogAdvanced.newInstance(mLang1Holder).show(fragmentManager, "advanced_dialog");
		}
	};

	private View.OnClickListener mOnShowAdvancedListener2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			Fragment prev = fragmentManager.findFragmentByTag("advanced_dialog");
			if(prev!=null){
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.remove(prev);
				fragmentTransaction.commit();
			}
			AddDialogAdvanced.newInstance(mLang2Holder).show(fragmentManager, "advanced_dialog");
		}
	};

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Activity communication
	public interface OnWordTesterAddListener{
		void onAdd(Vector<String> lang1, Vector<String> lang2, String help1, String help2);
	}
	private OnWordTesterAddListener mOnWordTesterAddListener;
	public void setOnWordTesterAddListener(OnWordTesterAddListener l){mOnWordTesterAddListener=l;}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC
	private class NullListenerException extends RuntimeException{
		NullListenerException(String message) {super(message);}
	}

	public void setLangs(WordGroup.LANG lang1, WordGroup.LANG lang2){
		mLang1 = lang1;
		mLang2 = lang2;
	}

	public void setMode(MODE mode){
		mMode = mode;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
