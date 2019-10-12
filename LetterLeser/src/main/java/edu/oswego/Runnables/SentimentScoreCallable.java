package edu.oswego.Runnables;

import edu.oswego.model.Email;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class SentimentScoreCallable implements Callable {
    private static ArrayList<Email> emails;

    public SentimentScoreCallable(ArrayList<Email> emails){
        this.emails = emails;
    }

    @Override
    public Object call() throws Exception {
        double score = 0;
        for(int i=0;i<emails.size();i++){
            score = score + emails.get(i).getSentimentScore().getCompound();
        }
        return score/emails.size();
    }
}
