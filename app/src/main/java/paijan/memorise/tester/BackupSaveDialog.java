package paijan.memorise.tester;

import java.io.File;

import paijan.memorise.general.BackupSaveDialogBase;
import paijan.memorise.general.WordGroup;
import paijan.memorise.general.WordGroupManager;

public class BackupSaveDialog extends BackupSaveDialogBase {

	public static BackupSaveDialog newInstance(WordGroupManager manager, WordGroupTester group) {
		BackupSaveDialog fragment = new BackupSaveDialog();
		fragment.mWordGroupManager = manager;
		fragment.mWordGroup = group;
		return fragment;
	}

	@Override
	protected File getTypedFile() {
		String filename = WordGroup.createFileName(mEditText.getText().toString().trim(), mWordGroup.getLang1(), ((WordGroupTester) mWordGroup).getLang2());
		return new File(mWordGroup.getBackupDir(), filename);
	}
}
