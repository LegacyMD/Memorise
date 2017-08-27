package paijan.memorise.general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import paijan.memorise.list.WordGroupList;
import paijan.memorise.tester.WordGroupTester;

public class WordGroupManager implements Serializable {
	@SuppressWarnings("unused")
	public static final long SerialVersionUID = 2L;
	public static final String EXTENSION = ".txt";

	public WordGroupManager(File directory) {
		mDIR = directory;
		loadNames(); //instantiate and populate mNames, mLangs1, mLang2;
	}

	private final File mDIR;
	private Vector<String> mNames;
	private Vector<WordGroup.LANG> mLangs1;
	private Vector<WordGroup.LANG> mLangs2;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public File getBackupDir() {
		return WordGroup.getBackupDir(new File(mDIR, "."));
	}

	public File getDIR() {
		return mDIR;
	}

	public Vector<String> getNames() {
		return mNames;
	}

	public String getName(int pos) {
		return pos < mNames.size() ? mNames.elementAt(pos) : null;
	}

	public int getGroupCount() {
		return mNames.size();
	}

	public Vector<WordGroup.LANG> getLangs1() {
		return mLangs1;
	}

	public Vector<WordGroup.LANG> getLangs2() {
		return mLangs2;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- WordGroup management

	public void addWordGroup(String name, WordGroup.LANG lang1, WordGroup.LANG lang2, WordGroup.TYPE type) {
		if (!validateName(name)) return;

		switch (type) {
			case TEST:
				new WordGroupTester(getDIR(), name, lang1, lang2).save();
				break;
			case LIST:
				new WordGroupList(getDIR(), name, lang1).save();
				break;
			default:
				return;
		}

		mNames.addElement(name);
		mLangs1.addElement(lang1);
		mLangs2.addElement(lang2);
	}

	public void editWordGroup(int position, String newName) {
		if (checkForInvalidPosition(position, "editWordGroup")) return;
		if (!validateName(newName)) return; //if name is invalid
		String oldName = mNames.elementAt(position);
		if (oldName.equals(newName))
			return; //user requested the same name, we don't have to change anything

		File oldFile = getOldFile(position);
		File newFile = getNewFile(position, newName);
		if (!oldFile.renameTo(newFile)) return; //if renaming failed

		mNames.setElementAt(newName, position);
		//Not changing mLangs1 and mLangs2, it's just no allowed in the app
	}

	public void deleteWordGroup(int position) {
		if (checkForInvalidPosition(position, "deleteWordGroup")) return;

		File file = getOldFile(position);
		if (!file.delete()) return;

		mNames.removeElementAt(position);
		mLangs1.removeElementAt(position);
		mLangs2.removeElementAt(position);
	}

	/**
	 * Called when WordGroup is selected, loads object to memory
	 *
	 * @return proper WordGroup; null if IO operation failed or wrong index has been sent
	 */
	public WordGroup selectWordGroup(int position) {
		if (checkForInvalidPosition(position, "selectWordGroup")) return null;

		String fullName = WordGroup.createFileName(mNames.elementAt(position), mLangs1.elementAt(position), mLangs2.elementAt(position));
		WordGroup wordGroup = WordGroup.load(mDIR, fullName);
		if (wordGroup == null) {
			//Group has not been read properly, it's broken and we can delete it
			// deleteWordGroup(position);
			return null;
		} else return wordGroup;
	}

	public File selectFile(int position) {
		if (checkForInvalidPosition(position, "selectWordGroup")) return null;

		String fullName = WordGroup.createFileName(mNames.elementAt(position), mLangs1.elementAt(position), mLangs2.elementAt(position));
		return new File(mDIR, fullName);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Names input/output

	private void loadNames() {
		File[] files = getDIR().listFiles();

		if (mNames == null) {
			mNames = new Vector<>(files.length);
			mLangs1 = new Vector<>(files.length);
			mLangs2 = new Vector<>(files.length);
		} else {
			mNames.clear();
			mLangs1.clear();
			mLangs2.clear();
		}

		for (File file : files) {
			String name = file.getName();

			try {
				mLangs1.addElement(WordGroup.LANG.fromIdentifier(name.substring(0, 2)));
				if (name.charAt(2) == '-') mLangs2.addElement(WordGroup.LANG.VOID);
				else mLangs2.addElement(WordGroup.LANG.fromIdentifier(name.substring(2, 4)));
				mNames.addElement(WordGroup.createGroupName(name));
			} catch (IllegalArgumentException e) {
				if (mLangs1.size() > mLangs2.size()) mLangs1.remove(mLangs1.size() - 1);
			}
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Validation
	public enum VALIDATION_RESULT {
		available, taken, illegal, empty
	}

	public boolean validateName(String arg) {
		return (validateNameFull(arg) == VALIDATION_RESULT.available);
	}

	public VALIDATION_RESULT validateNameFull(String arg) {
		arg = arg.trim();

		if (arg.isEmpty()) return VALIDATION_RESULT.empty;


		CharSequence[] forbidden = {"/", "*", "\"", ":", "?", "<", ">", "\\", "|", "\n", "\t"};
		for (CharSequence forbiddenCharacter : forbidden) {
			if (arg.contains(forbiddenCharacter)) return VALIDATION_RESULT.illegal;
		}

		for (String name : mNames) {
			if (arg.equals(name)) return VALIDATION_RESULT.taken;
		}

		return VALIDATION_RESULT.available;
	}

	private boolean checkForInvalidPosition(int position, String methodName) {
		if (position >= mNames.size()) {
//			Log.e(methodName, " - invalid index passed");
			return true;
		}
		return false;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- File naming

	public String getFilename(int pos) {
		return WordGroup.createFileName(mNames.get(pos), mLangs1.get(pos), mLangs2.get(pos));
	}

	private File getOldFile(int pos) {
		return new File(mDIR, getFilename(pos));
	}

	private File getNewFile(int pos, String newName) {
		String name = WordGroup.createFileName(newName, mLangs1.get(pos), mLangs2.get(pos));
		return new File(mDIR, name);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Testing

	public void clearMemory() {
		clearStorage(mDIR);
		mNames.setSize(0);
		mLangs1.setSize(0);
		mLangs2.setSize(0);
	}

	public static void clearStorage(File dir) {
		File[] files = dir.listFiles();
		for (File file : files)
			if (file.isDirectory())
				clearStorage(file);
			else if (!file.delete()) ;
//				Log.d("clearMemory", "fail during deleting "+file.getName());
	}

	@SuppressWarnings("unused")
	public void listMemory() {
		File[] checkFiles = getDIR().listFiles();
		Vector<String> checkNames = mNames;
		loadNames();
		boolean checkGoodList = mNames.equals(checkNames);
		boolean checkGoodArrays = (mNames.size() == mLangs1.size() && mNames.size() == mLangs2.size());
	}

	public void reload() {
		loadNames();
	}

	@SuppressWarnings("unused")
	public static String showFileContent(File file) {
		String content = "";

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			for (String line; (line = reader.readLine()) != null && line.length() > 0; )
				content += line + "\n";
		} catch (IOException e) {
//			Log.e("showFileContent", e.getMessage());
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException e) {
//				Log.e("showFileContents", e.getMessage());
			}
		}
		return content;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
