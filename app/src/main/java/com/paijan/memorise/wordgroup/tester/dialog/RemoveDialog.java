package com.paijan.memorise.wordgroup.tester.dialog;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.AlertRemoveDialogFragment;

public class RemoveDialog extends AlertRemoveDialogFragment {
	private RemoveDialogListener mRemoveDialogListener;

	public static RemoveDialog newInstance(RemoveDialogListener listener) {
		RemoveDialog result = new RemoveDialog();
		result.mRemoveDialogListener = listener;
		return result;
	}

	@Override
	protected String getTitle () {
		return getString(R.string.group_delete_question);
	}

	@Override
	protected void onClickPositive () {
		mRemoveDialogListener.onRemove();
	}

	public interface RemoveDialogListener{
		void onRemove();
	}
}
