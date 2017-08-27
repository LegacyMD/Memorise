package com.paijan.memorise.grouplist.manager;

import com.paijan.memorise.convenience.WordGroupBase;

import java.io.Serializable;
import java.util.ArrayList;

public final class WordGroupsSection implements Serializable {
	@SuppressWarnings("unused")
	public static final long SerialVersionUID = 212335L;
	private WordGroupBase.Lang mLang1, mLang2;
	private ArrayList<String> mNames;
	private WordGroupsSection () {}

	public static WordGroupsSection newInstanceInvalid(){
		return newInstance(WordGroupBase.Lang.VOID, WordGroupBase.Lang.VOID, null);
	}
	public static WordGroupsSection newInstance(WordGroupBase.Lang lang1, WordGroupBase.Lang lang2) {
		return newInstance(lang1, lang2, new ArrayList<String>());
	}
	public static WordGroupsSection newInstance(WordGroupBase.Lang lang1, ArrayList<String> names) {
		return newInstance(lang1, WordGroupBase.Lang.VOID, names);
	}
	public static WordGroupsSection newInstance(WordGroupBase.Lang lang1, WordGroupBase.Lang lang2, ArrayList<String> names) {
		WordGroupsSection result = new WordGroupsSection();
		if(lang1==null) lang1 = WordGroupBase.Lang.VOID;
		if(lang2==null) lang2 = WordGroupBase.Lang.VOID;
		if(lang1.ordinal() > lang2.ordinal()){
			result.mLang1 = lang2;
			result.mLang2 = lang1;
		} else {
			result.mLang1 = lang1;
			result.mLang2 = lang2;
		}
		result.mNames = names;
		return result;
	}

	public ArrayList<String> getNames () {
		return mNames;
	}
	public WordGroupBase.Lang getLang1 () {
		return mLang1;
	}
	public WordGroupBase.Lang getLang2 () {
		return mLang2;
	}

	public int getNamesCount() {return mNames.size();}
	public String getName(int p) {return mNames.get(p);}

	public boolean isValidSection() { return mLang1 != WordGroupBase.Lang.VOID; }
	public boolean isTestSection() { return mLang1 != WordGroupBase.Lang.VOID && mLang2 != WordGroupBase.Lang.VOID;}
	public boolean isListSection() { return mLang1 != WordGroupBase.Lang.VOID && mLang2 == WordGroupBase.Lang.VOID;}
}
