package com.paijan.memorise.convenience;

import com.paijan.memorise.R;
import com.paijan.memorise.grouplist.manager.WordGroupHeader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public abstract class WordGroupBase implements Serializable {
	public enum Lang {
		POL("pl", R.drawable.ic_flag_pl),
		ENG("en", R.drawable.ic_flag_en),
		FR("fr", R.drawable.ic_flag_fr),
		DE("de", R.drawable.ic_flag_de),
		SPA("sp", R.drawable.ic_flag_sp),
		VOID("", 0);
		private final String mIdentifier;
		private final int mFlagId;

		Lang(String identifier, int flagId) {
			mIdentifier = identifier;
			mFlagId = flagId;
		}
		public String getIdentifier() {
			return mIdentifier;
		}
		public int getFlagId() {
			return mFlagId;
		}

		public static Lang fromOrdinal(int pos) {
			return values()[pos];
		}
		public static Lang fromIdentifier(String val) {
			for (Lang lang : values()) {
				if (lang.mIdentifier.equals(val)) return lang;
			}
			return null;
		}
	}

	private final File mFile;
	private String mName;
	private Lang mLang1;
	private Lang mLang2;

	protected WordGroupBase(File file, WordGroupHeader header) {
		this(file, header.getName(), header.getLang1(), header.getLang2());
	}
	protected WordGroupBase(File file) {
		mFile = file;
	}
	private WordGroupBase(File file, String name, Lang lang1, Lang lang2) {
		mFile = file;
		mName = name;
		mLang1 = lang1;
		mLang2 = lang2;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Accessors

	public File getFile() {
		return mFile;
	}
	public String getName() {
		return mName;
	}
	public Lang getLang1() {
		return mLang1;
	}
	public Lang getLang2() {
		return mLang2;
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- Json

	public final boolean save() {
		return save(mFile);
	}
	public final boolean save(File file) {
		if (file != null) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				JSONObject jsonObject = toJson();
				String string = jsonObject.toString();
				writer.write(string);
				return true;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			} finally {
				if (writer != null) try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	protected JSONObject toJson() throws JSONException {
		return new JSONObject().
				put("name", mName).
				put("lang1", mLang1.getIdentifier()).
				put("lang2", mLang2.getIdentifier());
	}

	public final boolean load() {
		if (mFile != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(mFile));
				JSONObject json = getJsonFromReader(reader);
				fromJson(json);
				return true;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	private JSONObject getJsonFromReader(BufferedReader reader) throws IOException, JSONException {
		String content = "";
		for(String line; (line=reader.readLine())!=null;) {
			content += line;
		}
		return new JSONObject(content);
	}
	protected void fromJson(JSONObject json) throws JSONException {
		mName = json.getString("name");
		mLang1 = Lang.fromIdentifier(json.getString("lang1"));
		mLang2 = Lang.fromIdentifier(json.getString("lang2"));
	}

	// -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
}