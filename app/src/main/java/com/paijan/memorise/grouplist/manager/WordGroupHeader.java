package com.paijan.memorise.grouplist.manager;

import com.paijan.memorise.convenience.WordGroupBase;

public final class WordGroupHeader {
	private String mName;
	private WordGroupBase.Lang mLang1, mLang2;
	private boolean mLegit;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Create

	private WordGroupHeader(){}

	public static WordGroupHeader newInstance(String name, WordGroupBase.Lang lang1){
		return newInstance(name, lang1, WordGroupBase.Lang.VOID);
	}
	public static WordGroupHeader newInstance(String name, WordGroupBase.Lang lang1, WordGroupBase.Lang lang2){
		if(lang2 == null) lang2 = WordGroupBase.Lang.VOID;

		WordGroupHeader result = new WordGroupHeader();
		if(lang1 == null || (lang1 == WordGroupBase.Lang.VOID && lang2 == WordGroupBase.Lang.VOID)) {
			result.mLegit = false;
		} else{
			result.mLegit = true;
			result.mName = name.trim();
			if(lang1.ordinal() > lang2.ordinal()){
				result.mLang1 = lang2;
				result.mLang2 = lang1;
			} else {
				result.mLang1 = lang1;
				result.mLang2 = lang2;
			}
		}
		if(name.equals("")) result.mLegit = false;
		return result;
	}
	public static WordGroupHeader newInstanceInvalid() {
		return newInstance("", WordGroupBase.Lang.VOID, WordGroupBase.Lang.VOID);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public String getName () {
		return mName;
	}
	public WordGroupBase.Lang getLang1 () {
		return mLang1;
	}
	public WordGroupBase.Lang getLang2 () {
		return mLang2;
	}
	public boolean isLegit () {
		return mLegit;
	}

	public boolean isTesterHeader() { return mLegit && mLang2 != WordGroupBase.Lang.VOID;}
	public boolean isListHeader() { return mLegit && mLang2 == WordGroupBase.Lang.VOID;}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
