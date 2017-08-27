package com.paijan.memorise.wordgroup.list;

import com.paijan.memorise.convenience.WordBase;

import org.json.JSONException;
import org.json.JSONObject;

public class Word extends WordBase {
	public static final long serialVersionUID = -1356119034705054708L;

	private String mWord;
	private String mHelp;

	public Word(JSONObject json) throws JSONException {
		fromJson(json);
	}
	public Word() {
		this("", "");
	}
	public Word(String word, String help) {
		mWord = word;
		mHelp = help;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public String getWord() {
		return mWord;
	}
	public String getHelp() {
		return mHelp;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Json

	@Override
	public void fromJson(JSONObject json) throws JSONException {
		mWord = json.getString("word");
		mHelp = json.getString("help");
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return new JSONObject().put("word", mWord).put("help", mHelp);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
