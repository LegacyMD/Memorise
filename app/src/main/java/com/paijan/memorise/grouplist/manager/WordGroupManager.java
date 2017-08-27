package com.paijan.memorise.grouplist.manager;

import android.support.annotation.NonNull;

import com.paijan.memorise.convenience.WordGroupBase;
import com.paijan.memorise.wordgroup.list.WordGroup;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public final class WordGroupManager implements Serializable {
	public enum ValidationResult {available, taken, illegal, empty}

	@SuppressWarnings ("unused")
	public static final long SerialVersionUID = 2L;
	public static final String EXTENSION = ".txt";

	private final File mDir;
	private final ArrayList<WordGroupsSection> mSections;

	public WordGroupManager(File directory) {
		mDir = directory;
		mSections = new ArrayList<>();
		reload();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public boolean isEmpty() {
		return getSections().size() == 0;
	}

	public ArrayList<WordGroupsSection> getSections() { return mSections; }

	@NonNull
	public WordGroupsSection getSection(WordGroupHeader header) {
		for (WordGroupsSection section : getSections()) {
			if (section.getLang1() == header.getLang1() && section.getLang2() == header.getLang2())
				return section;
		}
		return WordGroupsSection.newInstance(header.getLang1(), header.getLang2());
	}
	@NonNull
	public WordGroupsSection getSection(int position) {
		int index = 0;
		for (WordGroupsSection section : getSections()) {
			index += section.getNamesCount();
			if (position < index) return section;
		}
		return WordGroupsSection.newInstanceInvalid();
	}
	@NonNull
	public WordGroupHeader getHeader(int position) {
		int index = 0;
		for (WordGroupsSection section : getSections()) {
			int sectionNamesCount = section.getNamesCount();
			index += sectionNamesCount;

			if (position < index) {
				return WordGroupHeader.newInstance(section.getName(sectionNamesCount - (index - position)), section.getLang1(), section.getLang2());
			}
		}
		return WordGroupHeader.newInstanceInvalid();
	}
	public int getHeadersCount() {
		int result = 0;
		for (WordGroupsSection section : getSections()) {
			result += section.getNamesCount();
		}
		return result;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --  File accessors

	public File getDir() {
		return mDir;
	}
	private File getSectionDir(WordGroupsSection section) {
		File result = new File(getDir(), section.getLang1().getIdentifier() + section.getLang2().getIdentifier());
		//noinspection ResultOfMethodCallIgnored
		result.mkdirs();
		return result;
	}
	private File getSectionDir(WordGroupHeader header) {
		File result = new File(getDir(), header.getLang1().getIdentifier() + header.getLang2().getIdentifier());
		//noinspection ResultOfMethodCallIgnored
		result.mkdirs();
		return result;
	}
	private File getWordGroupFile(WordGroupHeader header) {
		if (header.isLegit()) {
			return new File(getSectionDir(header), header.getName() + WordGroupManager.EXTENSION);
		} else return null;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Files management

	private boolean canAdd(WordGroupHeader header) {
		return header.isLegit() && validateName(header) == ValidationResult.available;
	}
	public void addWordGroup(WordGroupHeader header) {
		if (canAdd(header)) {
			File wordGroupFile = getWordGroupFile(header);
			if (header.getLang2() == WordGroupBase.Lang.VOID)
				new WordGroup(wordGroupFile, header).save();
			else new com.paijan.memorise.wordgroup.tester.WordGroup(wordGroupFile, header).save();
			reload();
		}
	}

	private boolean canEdit(WordGroupHeader header) {
		return header.isLegit() && validateName(header) == ValidationResult.taken;
	}
	public void editWordGroup(WordGroupHeader oldHeader, String newName) {
		WordGroupHeader newHeader = WordGroupHeader.newInstance(newName, oldHeader.getLang1(), oldHeader.getLang2());
		if (canEdit(oldHeader) && canAdd(newHeader)) {
			File oldFile = getWordGroupFile(oldHeader);
			File newFile = getWordGroupFile(newHeader);
			if (oldFile != null && newFile != null) {
				if (oldFile.renameTo(newFile)) {
					reload();
				}
			}
		}
	}

	public void deleteWordGroup(WordGroupHeader header) {
		if (canEdit(header)) {
			File oldFile = getWordGroupFile(header);
			if (oldFile != null && oldFile.delete()) {
				deleteDirIfEmpty(getSection(header));
				reload();
			}
		}
	}
	private void deleteDirIfEmpty(WordGroupsSection section) {
		File sectionDir = getSectionDir(section);
		if (sectionDir.exists() && sectionDir.list().length == 0) {
			//noinspection ResultOfMethodCallIgnored
			sectionDir.delete();
		}
	}

	public WordGroupBase retrieveWordGroup(int position) {
		WordGroupHeader header = getHeader(position);
		return retrieveWordGroup(header);
	}
	public WordGroupBase retrieveWordGroup(WordGroupHeader header) {
		if (canEdit(header)) {
			File wordGroupFile = getWordGroupFile(header);
			WordGroupBase result = header.isListHeader() ? new WordGroup(wordGroupFile) : new com.paijan.memorise.wordgroup.tester.WordGroup(wordGroupFile);
			boolean load = result.load();
			if(load)
				return result;
		}
		return null;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Names input/output

	public void reload() {
		mSections.clear();
		SectionLoader sectionLoader = new SectionLoader();
		for (File file : getDir().listFiles()) {
			WordGroupsSection section = sectionLoader.load(file);
			if (section != null && section.getNamesCount() != 0) mSections.add(section);
		}
	}

	private final class SectionLoader {
		private File mDir;
		private WordGroupBase.Lang mLang1, mLang2;
		private ArrayList<String> mNames;

		public WordGroupsSection load(File file) {
			mDir = file;
			checkLangs();
			if (isSectionDirectory()) {
				loadNames();
				return getSection();
			} else return null;
		}
		private void checkLangs() {
			if (mDir == null || !mDir.exists() || !mDir.isDirectory()) {
				voidifyLangs();
			} else {
				String name = mDir.getName();
				switch (name.length()) {
					case 2:
						mLang1 = WordGroupBase.Lang.fromIdentifier(name);
						mLang2 = WordGroupBase.Lang.VOID;
						break;
					case 4:
						mLang1 = WordGroupBase.Lang.fromIdentifier(name.substring(0, 2));
						mLang2 = WordGroupBase.Lang.fromIdentifier(name.substring(2, 4));
						break;
					default:
						voidifyLangs();
						break;
				}
			}
		}
		private void voidifyLangs() {
			mLang1 = WordGroupBase.Lang.VOID;
			mLang2 = WordGroupBase.Lang.VOID;
		}
		private boolean isSectionDirectory() {
			return mLang1.ordinal() < mLang2.ordinal() && mLang1 != WordGroupBase.Lang.VOID;
		}

		private void loadNames() {
			String[] fileNames = mDir.list();
			mNames = new ArrayList<>(fileNames.length);
			for (String fileName : fileNames) {
				mNames.add(withoutExtension(fileName));
			}
		}

		private WordGroupsSection getSection() {
			return isTesterSectionDirectory() ? getTesterSection() : getListSection();
		}
		private boolean isTesterSectionDirectory() {
			return mLang1 != WordGroupBase.Lang.VOID && mLang2 != WordGroupBase.Lang.VOID;
		}
		private WordGroupsSection getTesterSection() {
			return WordGroupsSection.newInstance(mLang1, mLang2, mNames);
		}
		private WordGroupsSection getListSection() {
			return WordGroupsSection.newInstance(mLang1, mNames);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Validation

	public static String withoutExtension(String arg) {
		if (arg.endsWith(EXTENSION)) return arg.substring(0, arg.length() - EXTENSION.length());
		else return arg;
	}

	public ValidationResult validateName(WordGroupHeader header) {
		if (header.getName().isEmpty()) return ValidationResult.empty;

		CharSequence[] forbidden = {"/", "*", "\"", ":", "?", "<", ">", "\\", "|", "\n", "\t"};
		for (CharSequence forbiddenCharacter : forbidden) {
			if (header.getName().contains(forbiddenCharacter)) return ValidationResult.illegal;
		}
		if (!header.isLegit()) return ValidationResult.illegal;

		for (String string : getSection(header).getNames()) {
			if (header.getName().equals(string)) return ValidationResult.taken;
		}

		return ValidationResult.available;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}