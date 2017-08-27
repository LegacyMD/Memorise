package com.paijan.memorise.convenience;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AlertRemoveDialogFragment extends AlertDialogFragment{

	@Override
	protected final View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return null;
	}

	@Override
	public final void onViewSetup (View view) {
	}


	@Override
	protected final AnswerType getAnswerType () {
		return AnswerType.YES_NO;
	}

}
