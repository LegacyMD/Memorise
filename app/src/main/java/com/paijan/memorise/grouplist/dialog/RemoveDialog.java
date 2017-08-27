package com.paijan.memorise.grouplist.dialog;

import com.paijan.memorise.R;
import com.paijan.memorise.convenience.AlertRemoveDialogFragment;
import com.paijan.memorise.grouplist.manager.WordGroupHeader;

public class RemoveDialog extends AlertRemoveDialogFragment {
	private WordGroupHeader mHeader;
	private RemoveDialogListener mRemoveDialogListener;

	public static RemoveDialog newInstance(RemoveDialogListener listener, WordGroupHeader header){
		RemoveDialog fragment = new RemoveDialog();
		fragment.mRemoveDialogListener = listener;
		fragment.mHeader = header;
		return fragment;
	}

	@Override
	protected String getTitle () {
		return getString(R.string.grouplist_delete_question);
	}


	@Override
	protected void onClickPositive () {
		mRemoveDialogListener.onRemove(mHeader);
	}

	public interface RemoveDialogListener{
		void onRemove(WordGroupHeader header);
	}
}
