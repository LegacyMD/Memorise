package paijan.memorise.general;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import paijan.memorise.R;
import paijan.memorise.tester.WordGroupTester;

public abstract class WordGroup implements Serializable {
	public static final long serialVersionUID = -4499489363853122669L; //will be overriden by subclasses

	public enum LANG {
		POL("pl"), ENG("en"), FR("fr"), DEU("de"), SPA("sp"), VOID("vo");
		private String mIdentifier;

		LANG(String Identifier) {
			mIdentifier = Identifier;
		}

		public static LANG fromIdentifier(String val) {
			if (val == null) throw new IllegalArgumentException("Null string");
			LANG[] array = values();
			for (LANG l : array) {
				if (l.mIdentifier.equals(val)) return l;
			}
			throw new IllegalArgumentException("Illegal argument: " + val);
		}

		public String toIdentifier() {
			return mIdentifier;
		}

		private static int[] FLAG_ID = {R.drawable.ic_flag_pl, R.drawable.ic_flag_en, R.drawable.ic_flag_fr, R.drawable.ic_flag_de, R.drawable.ic_flag_sp, 0};

		public int getFlagId() {
			return FLAG_ID[ordinal()];
		}
	}

	public enum TYPE {TEST, LIST}

	protected String mName;
	protected File mFile;
	protected LANG mLang1;

	protected WordGroup(File file, String name, LANG lang1) {
		mName = name;
		mFile = file;
		mLang1 = lang1;
		getBackupDir(); //to ensure directory exists
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public String getName() {
		return mName;
	}

	public LANG getLang1() {
		return mLang1;
	}

	public void setName(String fileName) {
		mName = createGroupName(fileName);
		if (mFile != null) {
			mFile = new File(mFile.getParentFile(), fileName);
		}
	}

	public File getFile() {
		return mFile;
	}

	public File getBackupDir() {
		return getBackupDir(mFile);
	}

	public static File getBackupDir(File file) {
		File dir = new File(file.getParentFile(), "backup");
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				if (!dir.delete()) throw new BackupDirException("Error in dir.delete()");
				if (!dir.mkdir()) throw new BackupDirException("Error in dir.mkdir()");
			}
		} else if (!dir.mkdirs()) throw new BackupDirException("Error in dir.mkdir()");
		return dir;
	}


	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Serialization loading/saving

	public boolean save() {
		return save(mFile);
	}

	public boolean save(File file) {
		ObjectOutputStream writer = null;
		try {
			writer = new ObjectOutputStream(new FileOutputStream(file));
			writer.writeObject(this);
			return true;
		} catch (IOException e) {
//			Log.e("WordGroup.save", e.getMessage());
			return false;
		} finally {
			try {
				if (writer != null) writer.close();
			} catch (IOException e) {
//				Log.e("WordGroup.save", e.getMessage());
			}
		}
	}

	public static WordGroup load(File dir, String fileName) {
		ObjectInputStream reader = null;
		try {
			File file = new File(dir, fileName);
			reader = new ObjectInputStream(new FileInputStream(file));

			WordGroup wordGroup = (WordGroup) reader.readObject();
			wordGroup.setName(fileName);
			if (wordGroup instanceof WordGroupTester) {
				((WordGroupTester) wordGroup).resetTesting();
			}
			wordGroup.getBackupDir(); //to ensure directory exists
			return wordGroup;

		} catch (IOException e) {
//			Log.e("WordGroup.load.IO", e.getMessage());
			return null;

		} catch (ClassNotFoundException e) {
//			Log.e("WordGroup.load.CNF", e.getMessage());
			return null;

		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException e) {
//				Log.e("WordGroup.load", e.getMessage());
			}
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Naming creation

	public static String createFileName(String name, LANG lang1, LANG lang2) {
		String result = lang1.toIdentifier();
		if (lang2 != LANG.VOID) {
			result += lang2.toIdentifier();
		}
		result += "-";
		result += name;
		result += WordGroupManager.EXTENSION;
		return result;
	}

	public static String createFileName(String name, LANG lang) {
		String result = lang.toIdentifier();
		result += "-";
		result += name;
		result += WordGroupManager.EXTENSION;
		return result;
	}

	public static String createGroupName(String name) {
		switch (name.indexOf('-')) {
			case 2:
				return name.substring(3, name.length() - WordGroupManager.EXTENSION.length());
			case 4:
				return name.substring(5, name.length() - WordGroupManager.EXTENSION.length());
			default:
				return null;
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- MISC
	public static class BackupDirException extends RuntimeException {
		public BackupDirException(String detailMessage) {
			super(detailMessage);
		}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}