package paijan.memorise.tester;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import paijan.memorise.general.WordGroup;
import paijan.memorise.general.WordGroupManager;

public class WordGroupTester extends WordGroup {
	public static final long serialVersionUID = 6347269137584754813L;
	public static final Random RANDOM = new Random();

	private Vector<WordTester> mWords = new Vector<>();
	public WordGroupTester(File dir, String name, LANG lang1, LANG lang2) {
		super(new File(dir, createFileName(name, lang1, lang2)), name, lang1);
		mLang2 = lang2;
	}

	private LANG mLang2;

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public LANG getLang2() {return mLang2;}
	public Vector<WordTester> getWords() {return mWords;}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Testing
	private transient Vector<WordTester> mTestedWords;
	private transient WordDataHolder mLastHolder;
	private transient Vector<MistakeHolder> mMistakeHolders;
	public Vector<MistakeHolder> getMistakeHolders() {return mMistakeHolders;}

	public static class WordDataHolder{
		public WordDataHolder(STATE arg1, WordTester wordTester){
			state = arg1;
			this.wordTester = wordTester;

			if(WordGroupTester.RANDOM.nextInt(2) == 0){
				word = wordTester.getLangFirst1();
				comment = wordTester.getHelp1();
				answer = wordTester.getLang2();
				reversedLangs = false;
			} else {
				word = wordTester.getLangFirst2();
				comment = wordTester.getHelp2();
				answer = wordTester.getLang1();
				reversedLangs = true;
			}
		}
		public WordDataHolder(STATE arg1){
			state = arg1;
		}
		public boolean reversedLangs;
		public enum STATE {empty, next, last, end}
		public String word, comment;
		public Vector<String> answer;
		public STATE state;
		public WordTester wordTester;
	}

	public void resetTesting(){
		for(WordTester wordTester : mWords)
			wordTester.mWronged = false;
		mTestedWords = null;
		mLastHolder = null;
		mMistakeHolders = null;
	}

	@SuppressWarnings("unchecked")
	public WordDataHolder initiate(){
		resetTesting();
		if(mWords.size() == 0) return new WordDataHolder(WordDataHolder.STATE.empty);
		mTestedWords = (Vector<WordTester>) mWords.clone();
		mMistakeHolders = new Vector<>();
		return randomizeWord();
	}

	private WordDataHolder randomizeWord(){
		int index = RANDOM.nextInt(mTestedWords.size());

		WordDataHolder.STATE state;
		if(mTestedWords.size()==1) state = WordDataHolder.STATE.last;
		else state = WordDataHolder.STATE.next;

		mLastHolder = new WordDataHolder(state, mTestedWords.elementAt(index));
		mTestedWords.remove(index);
		return mLastHolder;
	}

	public WordDataHolder answer(String ans, MistakeListener mistakeListener){
		boolean isCorrect = false;
		for(String lang : mLastHolder.answer){
			if(normalize(ans).equals(normalize(lang))) isCorrect = true;
		}
		if(!isCorrect){
			mLastHolder.wordTester.mWronged = true;
			mMistakeHolders.addElement(new MistakeHolder(mLastHolder.word, ans, mLastHolder.reversedLangs, mLastHolder.answer));
			mistakeListener.mistake();
		}
		mLastHolder.wordTester.answer(isCorrect);
		save();

		if(mLastHolder.state == WordDataHolder.STATE.last){
			mTestedWords = null;
			mLastHolder = null;
			return new WordDataHolder(WordDataHolder.STATE.end);
		}
		return randomizeWord();
	}

	public static class MistakeHolder{
		public MistakeHolder(String word, String reply, boolean areLangsReversed, Vector<String> ans){
			if(areLangsReversed){
				lang1 = reply;
				lang2 = word;
				firstFailed = true;
			} else{
				lang1 = word;
				lang2 = reply;
				firstFailed = false;
			}
			answers = ans;
		}
		public String lang1, lang2;
		public boolean firstFailed;
		public Vector<String> answers;
	}


	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- String normalization

	private String normalize(String s){
		String result = s.toLowerCase();
		for(int i=0; i<result.length(); i++)
		{
			char c = result.charAt(i);
			switch(c)
			{
				case 'ą': result = replace(result, i, 'a'); break;
				case 'ć': result = replace(result, i, 'c'); break;
				case 'ę': result = replace(result, i, 'e'); break;
				case 'ł': result = replace(result, i, 'l'); break;
				case 'ń': result = replace(result, i, 'n'); break;
				case 'ñ': result = replace(result, i, 'n'); break;
				case 'ó': result = replace(result, i, 'o'); break;
				case 'ś': result = replace(result, i, 's'); break;
				case 'ź': result = replace(result, i, 'z'); break;
				case 'ż': result = replace(result, i, 'z'); break;
			}
		}
		return result;
	}

	private String replace(String s, int index, char c){
		StringBuilder builder = new StringBuilder(s);
		builder.setCharAt(index, c);
		return builder.toString();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Word Management

	public void addWord(Vector<String> lang1, Vector<String> lang2, String help1, String help2){
		mWords.addElement(new WordTester(lang1, lang2, help1, help2));
		save();
	}

	public void editWord(int pos, Vector<String> lang1, Vector<String> lang2, String help1, String help2){
		if(pos>=mWords.size()) throw new IndexOutOfBoundsException("editWord");

		WordTester word = mWords.elementAt(pos);
		word.setLang1(lang1);
		word.setLang2(lang2);
		word.setHelp1(help1);
		word.setHelp2(help2);

		save();
	}

	public void deleteWord(int pos){
		if(pos>=mWords.size()) throw new IndexOutOfBoundsException("deleteWord");
		mWords.remove(pos);
		save();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Listener
	interface MistakeListener {public void mistake();}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
