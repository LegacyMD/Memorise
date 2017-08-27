package com.paijan.memorise.wordgroup.tester;

import android.support.annotation.NonNull;

import com.paijan.memorise.convenience.WordBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Word extends WordBase implements Comparable<Word> {
	public enum LangOrdinal {first, second}

	public static final long serialVersionUID = 5536493108877651236L;

	private ArrayList<String> mWords1 = new ArrayList<>();
	private ArrayList<String> mWords2 = new ArrayList<>();
	private String mHelp1;
	private String mHelp2;
	private boolean mEnabled = true;
	private int mCorrects = 0, mTries = 0;

	private boolean mMistaken = false;

	public Word(JSONObject jsonObject) throws JSONException {
		fromJson(jsonObject);
	}
	public Word() {
		mWords1.add("");
		mWords2.add("");
		mHelp1 = "";
		mHelp2 = "";
	}
	public Word(ArrayList<String> words1, ArrayList<String> words2, String help1, String help2) {
		setWords1(words1);
		setWords2(words2);
		setHelp1(help1);
		setHelp2(help2);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public String getLangFirst1() {return mWords1.get(0);}
	public String getLangFirst2() {return mWords2.get(0);}
	public ArrayList<String> getWords1() {return mWords1;}
	public ArrayList<String> getWords2() {return mWords2;}
	public ArrayList<String> getAllLangs() {
		ArrayList<String> result = new ArrayList<>();
		result.addAll(mWords1);
		result.addAll(mWords2);
		return result;
	}
	public String getHelp1() {return mHelp1;}
	public String getHelp2() {return mHelp2;}

	public void setWords1(ArrayList<String> s) {mWords1 = s;}
	public void setWords2(ArrayList<String> s) {mWords2 = s;}
	public void setHelp1(String s) {mHelp1 = s;}
	public void setHelp2(String s) {mHelp2 = s;}

	public boolean isEnabled() {
		return mEnabled;
	}
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public boolean isMistaken() {
		return mMistaken;
	}
	public void resetMistaken() {
		mMistaken = false;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accuracy and
	// answering

	public void answer(boolean correct) {
		mMistaken = !correct;
		if (correct) mCorrects++;
		mTries++;
	}
	public float getAccuracyPercent() {
		if (mTries != 0) {
			float result = (float) mCorrects*100.f/(float) mTries;
			return Math.round(result*100.f)/100.f;
		} else return 0;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Json

	@Override
	public void fromJson(JSONObject json) throws JSONException {
		mWords1.clear();
		JSONArray words1 = json.getJSONArray("words1");
		for(int i=0; i<words1.length(); i++) {
			mWords1.add(words1.getString(i));
		}

		mWords2.clear();
		JSONArray words2 = json.getJSONArray("words2");
		for(int i=0; i<words2.length(); i++) {
			mWords2.add(words2.getString(i));
		}

		mHelp1 = json.getString("help1");
		mHelp2 = json.getString("help2");
		mEnabled = json.getBoolean("enabled");
		mCorrects = json.getInt("corrects");
		mTries = json.getInt("tries");

	}
	@Override
	public JSONObject toJson() throws JSONException {
		JSONArray words1 = new JSONArray(mWords1);
		JSONArray words2 = new JSONArray(mWords2);
		return new JSONObject().put("words1", words1).put("words2", words2).put("help1", mHelp1).put("help2", mHelp2).put("enabled", mEnabled).put("corrects", mCorrects).put("tries", mTries);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Comparable

	@Override
	public int compareTo(@NonNull Word another) {
		int comparisonEnabled = compareEnabled(isEnabled(), another.isEnabled());
		if (comparisonEnabled != 0) return comparisonEnabled;
		return compareAccuracy(getAccuracyPercent(), another.getAccuracyPercent());
	}

	public static int compareEnabled(boolean first, boolean second) {
		if (first == second) return 0;
		if (first) return -1;
		return 1;
	}

	public static int compareAccuracy(float a, float b) {
		return (int) Math.signum(a - b);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
