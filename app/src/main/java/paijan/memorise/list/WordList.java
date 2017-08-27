package paijan.memorise.list;

import java.io.Serializable;

public class WordList implements Serializable {
	public static final long serialVersionUID = -1356119034705054708L;

	private String mWord;
	private String mHelp;

	public WordList(){
		setStrings("", "");
	}

	public WordList(String word, String help){
		setStrings(word, help);
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public String getWord() {return mWord;}
	public String getHelp() {return mHelp;}

	public void setStrings(String word, String help){
		mWord = word;
		mHelp = help;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
