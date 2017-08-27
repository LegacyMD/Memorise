package com.paijan.memorise.wordgroup.tester;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Random;

public class TestExecutor {
	public static final Random RANDOM = new Random();

	private final MistakeListener mMistakeListener;
	private final WordGroup mWordGroup;
	private final ArrayList<Word> mWords;
	private final ArrayList<MistakeHolder> mMistakeHolders;
	private final boolean mEmpty;
	private AskedWordData mLastHolder;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- General

	public TestExecutor (WordGroup wordGroup, MistakeListener mistakeListener) {
		mMistakeListener = mistakeListener;
		mWordGroup = wordGroup;
		mWords = wordGroup.getEnabledWords();
		mMistakeHolders = new ArrayList<>();
		mEmpty = (mWords.size() == 0);
		for(Word word : mWords) word.resetMistaken();
	}

	public AskedWordData nextWord (){
		if(mEmpty) return AskedWordData.newInstanceEmpty();
		int size = mWords.size();
		if(size == 0) return AskedWordData.newInstanceEnd();

		Word word = mWords.remove(RANDOM.nextInt(mWords.size()));
		AskedWordData.State state = (size == 1) ? AskedWordData.State.last : AskedWordData.State.next;
		mLastHolder = AskedWordData.newInstance(state, word);
		return mLastHolder;
	}

	public void answer(String answer){
		boolean isCorrect = checkCorrect(answer);
		if(!isCorrect) {
			handleMistake(answer);
		}
		mLastHolder.word.answer(isCorrect);
		mWordGroup.save();
	}
	private boolean checkCorrect (String input) {
		for(String lang : mLastHolder.answer){
			if(normalizeForComparison(input).equals(normalizeForComparison(lang))) return true;
		}
		return false;
	}
	public static String normalizeForComparison(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}
	private void handleMistake(String answer) {
		mMistakeHolders.add(new MistakeHolder(mLastHolder.question, answer, mLastHolder.whichLang, mLastHolder.answer));
		mMistakeListener.mistake();
	}


	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Data holder classes

	public ArrayList<MistakeHolder> getMistakeHolders() {return mMistakeHolders;}

	public interface MistakeListener {
		void mistake();
	}

	public static class MistakeHolder{
		public String lang1, lang2;
		public Word.LangOrdinal whichFailed;
		public ArrayList<String> answers;

		public MistakeHolder(String word, String answer, Word.LangOrdinal whichFailed, ArrayList<String> answers){
			this.answers = answers;
			switch (this.whichFailed = whichFailed) {
				case first:
					lang1 = answer;
					lang2 = word;
					break;
				case second:
					lang1 = word;
					lang2 = answer;
					break;
			}
		}
	}

	public static class AskedWordData {
		public enum State {empty, next, last, end}
		public Word word;
		public String question, comment;
		public ArrayList<String> answer;
		public State state;
		public Word.LangOrdinal whichLang;

		private AskedWordData () {}
		private AskedWordData (State state) { this.state = state;}

		public static AskedWordData newInstance(State state, Word word) {
			AskedWordData result = new AskedWordData();
			result.state = state;
			result.word = word;
			if(RANDOM.nextBoolean()){
				result.question = word.getLangFirst1();
				result.comment = word.getHelp1();
				result.answer = word.getWords2();
				result.whichLang = Word.LangOrdinal.first;
			} else {
				result.question = word.getLangFirst2();
				result.comment = word.getHelp2();
				result.answer = word.getWords1();
				result.whichLang = Word.LangOrdinal.second;
			}
			return result;
		}
		public static AskedWordData newInstanceEmpty() {
			return new AskedWordData(State.empty);
		}
		public static AskedWordData newInstanceEnd() {
			return new AskedWordData(State.end);
		}
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
