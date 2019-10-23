package edu.oswego.Runnables;

import edu.oswego.model.Email;s

import java.util.List;
import java.util.concurrent.Callable;

public class FolderCallable implements Callable {
	private static List<Email> folders;

	public FolderCallable(List<Email> folders) {
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
