package com.paijan.memorise.wordgroup.tester;

import com.paijan.memorise.convenience.WordGroupBase;
import com.paijan.memorise.grouplist.manager.WordGroupHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class WordGroup extends WordGroupBase {
	public static final long serialVersionUID = 6347269137584754813L;

	private final ArrayList<Word> mWords = new ArrayList<>();

	public WordGroup(File file) {
		super(file);
	}
	public WordGroup (File file, WordGroupHeader header) {
		super(file, header);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public TestExecutor getNewTestExecutor(TestExecutor.MistakeListener mistakeListener){
		return new TestExecutor(this, mistakeListener);
	}
	public Word getWord(int pos) {return mWords.get(pos);}
	public ArrayList<Word> getWords() {return mWords;}
	public ArrayList<Word> getEnabledWords() {
		ArrayList<Word> result = new ArrayList<>();
		for(Word word : getWords()) {
			if(word.isEnabled()) result.add(word);
		}
		return result;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Word Management

	public void addWord(Word word) {
		mWords.add(word);
		save();
	}

	public void editWord(int pos, Word word) {
		mWords.set(pos, word);
		save();
	}

	public void removeWord (int pos){
		mWords.remove(pos);
		save();
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
