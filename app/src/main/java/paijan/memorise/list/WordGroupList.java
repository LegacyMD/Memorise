package paijan.memorise.list;

import java.io.File;
import java.util.Vector;

import paijan.memorise.general.WordGroup;

public class WordGroupList extends WordGroup {
	public static final long serialVersionUID = -4400901924666858093L;

	private Vector<WordList> mWords = new Vector<>();
	public WordGroupList(File dir, String name, LANG lang1) {
		super(new File(dir, createFileName(name, lang1)), name, lang1);
	}


	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public Vector<WordList> getWords(){return mWords;}

	public void addWord(String word, String help){
		if (mWords != null) {
			mWords.addElement(new WordList(word, help));
			save();
		}
	}
	public void editWord(int pos, String word, String help){
		if (mWords != null)
			if (pos<mWords.size()) {
				mWords.setElementAt(new WordList(word, help), pos);
				save();
			}
	}
	public void removeWord(int pos){
		if (mWords != null)
			if (pos<mWords.size()) {
				mWords.removeElementAt(pos);
				save();
			}
	}
	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
