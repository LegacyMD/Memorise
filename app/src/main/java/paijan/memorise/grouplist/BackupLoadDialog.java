package paijan.memorise.grouplist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Vector;

import paijan.memorise.R;
import paijan.memorise.general.WordGroup;
import paijan.memorise.general.WordGroupManager;

public class BackupLoadDialog extends DialogFragment {

	private ListView mListView;
	private WordGroupManager mWordGroupManager;

	public static BackupLoadDialog newInstance(File backupDirectory, OnBackupLoadListener listener) {
		BackupLoadDialog fragment = new BackupLoadDialog();
		fragment.mOnBackupLoadListener = listener;
		fragment.mWordGroupManager = new WordGroupManager(backupDirectory);
		return fragment;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	@Override
	public AlertDialog getDialog() {
		return (AlertDialog) super.getDialog();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Callbacks

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			builder = new AlertDialog.Builder(getActivity(), R.style.Theme_DialogTheme_Grouplist);
		} else {
			ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), R.style.Theme_DialogTheme_Grouplist);
			builder = new AlertDialog.Builder(ctw);
		}

		builder.setTitle(getString(R.string.group_backup_load));
		builder.setNegativeButton(R.string.cancel, null);

		return builder.create();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Context context = inflater.getContext();
		mListView = new ListView(context);
		getDialog().setView(mListView);
		mListView.setAdapter(new DialogListViewAdapter(getActivity(), mOnEntryDeleteListener, mWordGroupManager.getNames(), mWordGroupManager.getLangs1(), mWordGroupManager.getLangs2()));
		mListView.setOnItemClickListener(mOnItemClickListener);

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
	public interface OnBackupLoadListener {
		public void onLoad();
	}

	private OnBackupLoadListener mOnBackupLoadListener;

	private View.OnClickListener mOnEntryDeleteListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			deleteEntry((int) v.getTag());
		}
	};

	private void deleteEntry(int position) {
		mWordGroupManager.deleteWordGroup(position);
		if (mWordGroupManager.getGroupCount() > 0) notifyListView();
		else dismiss();
	}

	private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			File inFile = mWordGroupManager.selectFile(position);

			IncrementingFilename incrementingFilename = new IncrementingFilename(mWordGroupManager.getFilename(position));
			File dir = mWordGroupManager.getDIR().getParentFile();
			File outFile = new File(dir, getIncrementedFileName(dir.list(), incrementingFilename));

			FileChannel in, out;
			try {
				in = new FileInputStream(inFile).getChannel();
				out = new FileOutputStream(outFile).getChannel();
				out.transferFrom(in, 0, in.size());
			} catch (IOException e) {
				deleteEntry(position);
				//broken, unreadable file can be deleted
			}

			mOnBackupLoadListener.onLoad();
			BackupLoadDialog.this.dismiss();
		}
	};
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC

	public void notifyListView() {
		BaseAdapter adapter = (BaseAdapter) mListView.getAdapter();
		adapter.notifyDataSetChanged();
	}

	private String getIncrementedFileName(String[] existingS, IncrementingFilename incrementingFilename) {
		Filename[] existing = new Filename[existingS.length];
		for (int i = 0; i < existing.length; i++) {
			try {
				existing[i] = new Filename(existingS[i]);
			} catch (Filename.NoExtensionException e) {
				existing[i] = null;
			}
		}


		while (true) {
			boolean available = true;
			for (Filename filename : existing)
				if (incrementingFilename.equals(filename))
					available = false;

			if (available)
				return incrementingFilename.toString();
			else incrementingFilename.increment();
		}
	}


	private static class IncrementingFilename extends Filename {
		private int mIndex = 0;

		public IncrementingFilename(String name) {
			super(name);
		}

		public void increment() {
			mIndex++;
		}

		@Override
		protected String getCore() {
			if (mIndex > 0) return mCore + "(" + mIndex + ")";
			else return mCore;
		}
	}


	private static class Filename {
		protected final String mPrefix;
		protected final String mCore;
		protected final String mExtension;

		public Filename(String name) {
			Integer dotIndex = null;
			for (int i = name.length() - 1; i >= 0; i--) {
				if (name.charAt(i) == '.') {
					dotIndex = i;
					break;
				}
			}
			if (dotIndex == null) throw new NoExtensionException();

			Integer hyphenIndex = null;
			for (int i = 0; i < name.length(); i++) {
				if (name.charAt(i) == '-') {
					hyphenIndex = i;
					break;
				}
			}
			if (hyphenIndex == null) throw new NoPrefixException();

			mPrefix = name.substring(0, hyphenIndex + 1);
			mCore = name.substring(hyphenIndex + 1, dotIndex);
			mExtension = name.substring(dotIndex, name.length());
		}

		protected String getCore() {
			return mCore;
		}

		@Override
		public String toString() {
			return mPrefix + getCore() + mExtension;
		}

		public boolean equals(Filename o) {
			return o != null && getCore().equals(o.getCore());
		}

		public static class NoExtensionException extends RuntimeException {
		}

		public static class NoPrefixException extends RuntimeException {
		}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}


// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

class DialogListViewAdapter extends BaseAdapter {
	public final String mAddButtonText;

	private Vector<String> mNames;
	private Vector<WordGroup.LANG> mLangs1;
	private Vector<WordGroup.LANG> mLangs2;
	private LayoutInflater layoutInflater;
	private View.OnClickListener mOnClickListener;
	private TypedValue mButtonBackgroundValue;

	public DialogListViewAdapter(FragmentActivity fragmentActivity, View.OnClickListener listener, Vector<String> names, Vector<WordGroup.LANG> langs1, Vector<WordGroup.LANG> langs2) {
		mNames = names;
		mLangs1 = langs1;
		mLangs2 = langs2;
		layoutInflater = fragmentActivity.getLayoutInflater();
		mAddButtonText = fragmentActivity.getString(R.string.grouplist_add);
		mOnClickListener = listener;

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mButtonBackgroundValue = new TypedValue();
			fragmentActivity.getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, mButtonBackgroundValue, true);
		}
	}

	@Override
	public int getCount() {
		return mNames.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.group_backup_load_item, parent, false);
			viewHolder = new ViewHolder();

			viewHolder.img1 = (ImageView) convertView.findViewById(R.id.group_backup_load_item_img1);
			viewHolder.img2 = (ImageView) convertView.findViewById(R.id.group_backup_load_item_img2);
			viewHolder.text = (TextView) convertView.findViewById(R.id.group_backup_load_item_text);
			viewHolder.btn = (ImageView) convertView.findViewById(R.id.group_backup_load_item_img3);

			viewHolder.btn.setOnClickListener(mOnClickListener);
			if (mButtonBackgroundValue != null)
				viewHolder.btn.setBackgroundResource(mButtonBackgroundValue.resourceId);

			convertView.setTag(viewHolder);
		} else viewHolder = (ViewHolder) convertView.getTag();

		viewHolder.btn.setTag(position);
		viewHolder.img1.setImageResource(mLangs1.elementAt(position).getFlagId());
		int id2 = mLangs2.elementAt(position).getFlagId();
		if (id2 != 0) {
			viewHolder.img2.setVisibility(View.VISIBLE);
			viewHolder.img2.setImageResource(id2);
		} else {
			viewHolder.img2.setVisibility(View.GONE);
		}
		viewHolder.text.setText(mNames.elementAt(position));

		return convertView;
	}

	private static class ViewHolder {
		ImageView img1;
		ImageView img2;
		TextView text;
		ImageView btn;
	}
}