package edu.oswego.Runnables;

import edu.oswego.model.UserFolder;

import java.util.List;
import java.util.concurrent.Callable;

public class FolderCallable implements Callable {
	private static List<UserFolder> folders;

	public FolderCallable(List<UserFolder> folders) {
		FolderCallable.folders = folders;
	}

	@Override
	public Object call() throws Exception {
		String answer = "";
		for (int i = 0; i < folders.size(); i++) {

		}

		return answer;
	}
}
