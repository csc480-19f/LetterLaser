package edu.oswego.Runnables;

import edu.oswego.model.UserFolder;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class FolderCallable implements Callable {
    private static ArrayList<UserFolder> folders;

    public FolderCallable(ArrayList<UserFolder> folders){
        this.folders = folders;
    }

    @Override
    public Object call() throws Exception {
        String answer = "";
        for(int i=0;i<folders.size();i++){

        }


        return answer;
    }
}
