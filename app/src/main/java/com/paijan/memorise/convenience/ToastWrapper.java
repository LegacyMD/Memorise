package com.paijan.memorise.convenience;

import android.content.Context;
import android.support.annotation.IntDef;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ToastWrapper {
	@IntDef ( {Toast.LENGTH_SHORT, Toast.LENGTH_LONG})
	@Retention (RetentionPolicy.SOURCE)
	public @interface Duration {}

	private final int TOAST_LENGTH;
	private Toast mToast;
	private Context mContext;

	public ToastWrapper(Context context) {
		this(context, Toast.LENGTH_SHORT);
	}
	public ToastWrapper(Context context, @Duration int toastLength) {
		mContext = context;
		TOAST_LENGTH = toastLength;
	}

	public void show(String message) {
		if (mToast != null) mToast.cancel();

		mToast = Toast.makeText(mContext, message, TOAST_LENGTH);
		mToast.show();
	}
}
