package com.paijan.memorise.wordgroup.list;

import com.paijan.memorise.convenience.WordGroupBase;
import com.paijan.memorise.grouplist.manager.WordGroupHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class WordGroup extends WordGroupBase {
	public static final long serialVersionUID = -4400901924666858093L;

	private final ArrayList<Word> mWords = new ArrayList<>();

	public WordGroup(File file) {
		super(file);
	}
	public WordGroup(File file, WordGroupHeader header) {
		super(file, header);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public ArrayList<Word> getWords() {
		return mWords;
	}

	public void addWord(Word word) {
		mWords.add(word);
		save();
	}

	public void editWord(int pos, Word word) {
		mWords.set(pos, word);
		save();
	}

	public void removeWord(int pos) {
		if (mWords != null) if (pos < mWords.size()) {
			mWords.remove(pos);
			save();
		}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

	@Override
	protected JSONObject toJson() throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (Word word : mWords) {
			jsonArray.put(word.toJson());
		}
		return super.toJson().put("words", jsonArray);
	}
	@Override
	protected void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);
		mWords.clear();
		JSONArray words = json.getJSONArray("words");
		for (int i = 0; i < words.length(); i++) {
			JSONObject jsonObject = words.getJSONObject(i);
			mWords.add(new Word(jsonObject));
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
