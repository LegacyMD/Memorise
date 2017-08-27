package paijan.memorise.tester;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Vector;

public class WordTester implements Serializable, Comparable {
	public static final long serialVersionUID = 5536493108877651236L;

	private int mCorrects, mTries;
	private Vector<String> mLang1 = new Vector<>();
	private Vector<String> mLang2 = new Vector<>();
	private String mHelp1;
	private String mHelp2;
	boolean mWronged = false;

	public WordTester(){
		Vector<String> lang1 = new Vector<>(1);
		lang1.add("");
		setLang1(lang1);

		Vector<String> lang2 = new Vector<>(1);
		lang2.add("");
		setLang2(lang2);

		setHelp1("");
		setHelp2("");

		resetAccuracy();
	}

	public WordTester(Vector<String> lang1, Vector<String> lang2, String help1, String help2){
		setLang1(lang1);
		setLang2(lang2);
		setHelp1(help1);
		setHelp2(help2);

		resetAccuracy();
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public void setLang1(Vector<String> s) {mLang1 = s;}
	public void setLang2(Vector<String> s) {mLang2 = s;}

	public Vector<String> getLang1() {return mLang1;}
	public Vector<String> getLang2() {return mLang2;}
	public Vector<String> getAllLangs(){
		Vector<String> result = new Vector<>();
		result.addAll(mLang1);
		result.addAll(mLang2);
		return result;
	}

	public String getLangFirst1() {return mLang1.elementAt(0);}
	public String getLangFirst2() {return mLang2.elementAt(0);}

	public void setHelp1(String s) {mHelp1 = s;}
	public void setHelp2(String s) {mHelp2 = s;}
	public String getHelp1() {return mHelp1;}
	public String getHelp2() {return mHelp2;}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accuracy

	public void answer(boolean correct){
		if(correct) mCorrects++;
		mTries++;
	}
	public float getAccuracyPercent(){
		if(mTries!=0){
			float result = (float) mCorrects*100.f / (float) mTries;
			return Math.round(result*100.f) / 100.f;
		}
		else return 0;
	}

	public void resetAccuracy(){
		mTries = 0;
		mCorrects = 0;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Comparable

	@Override
	public int compareTo(@NonNull Object another) {
		float a = getAccuracyPercent();
		float b = ((WordTester)another).getAccuracyPercent();
		return (int)Math.signum(a-b);
	}


	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}
