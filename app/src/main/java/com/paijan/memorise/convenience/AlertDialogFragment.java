package com.paijan.memorise.convenience;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.paijan.memorise.R;

public abstract class AlertDialogFragment extends DialogFragment {
	protected enum AnswerType {YES_NO, OK_CANCEL, OK}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Creating

	@NonNull
	@Override
	public final Dialog onCreateDialog (Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme);
		builder.setTitle(getTitle());
		switch (getAnswerType()){
			case YES_NO:
				builder.setPositiveButton(android.R.string.yes, getPositiveListener());
				builder.setNegativeButton(android.R.string.no, getNegativeListener());
				break;
			case OK_CANCEL:
				builder.setPositiveButton(android.R.string.ok, getPositiveListener());
				builder.setNegativeButton(android.R.string.cancel, getNegativeListener());
				break;
			case OK:
				builder.setPositiveButton(android.R.string.ok, getNegativeListener());
				break;
		}
		return builder.create();
	}

	@Nullable
	@Override
	public final View onCreateView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = createRootView(inflater, container, savedInstanceState);
		getDialog().setView(rootView);
		onViewSetup(rootView);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	protected abstract View createRootView (LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
	protected abstract void onViewSetup(View view);
	protected abstract String getTitle();
	protected abstract AnswerType getAnswerType();

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Buttons

	protected final Button getButton(int which){
		return getDialog().getButton(which);
	}
	protected final Button getPositiveButton() {
		return getButton(AlertDialog.BUTTON_POSITIVE);
	}
	protected final Button getNeutralButton() {
		return getButton(AlertDialog.BUTTON_NEUTRAL);
	}
	protected final Button getNegativeButton() {
		return getButton(AlertDialog.BUTTON_NEGATIVE);
	}

	protected abstract void onClickPositive ();
	protected void onClickNeutral() {}
	protected void onClickNegative () {}

	private DialogInterface.OnClickListener getPositiveListener() {
		return new DialogInterface.OnClickListener() {
			@Override public void onClick (DialogInterface dialog, int which) {
				onClickPositive();
			}
		};
	}
	private DialogInterface.OnClickListener getNeutralListener() {
		return new DialogInterface.OnClickListener() {
			@Override public void onClick (DialogInterface dialog, int which) {
				onClickNeutral();
			}
		};
	}
	private DialogInterface.OnClickListener getNegativeListener() {
		return new DialogInterface.OnClickListener() {
			@Override public void onClick (DialogInterface dialog, int which) {
				onClickNegative();
			}
		};
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Misc

	@Override
	public final AlertDialog getDialog() {
		return ((AlertDialog) super.getDialog());
	}

	@Override
	public void onStart() {
		super.onStart();
		int dividerId = getResources().getIdentifier("titleDivider", "id", "android");
		View divider = (getDialog().findViewById(dividerId));
		if (divider != null) divider.setBackgroundResource(R.color.DialogDivider);
	}

	public final void show(FragmentActivity context, String fragmentTag) {
		FragmentManager fragmentManager = context.getSupportFragmentManager();
		Fragment prev = fragmentManager.findFragmentByTag(fragmentTag);
		if(prev!=null){
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.remove(prev);
			fragmentTransaction.commit();
		}

		show(fragmentManager, fragmentTag);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
