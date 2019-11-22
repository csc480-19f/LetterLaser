package runnables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import edu.oswego.model.Email;
import edu.oswego.model.SentimentScore;
import edu.oswego.runnables.SentimentScoreCallable;
import edu.oswego.sentiment.AnalyzeThis;

/**
 * This class runs tests for the sentiment callable.
 * @author Phoenix Boisnier
 */
public class SentimentScoreTest {

    ArrayList<Email> emails = new ArrayList<>();
    SentimentScoreCallable scorer;

    @Test
    public void testSentimentCall(){

        //First test tests all positive.
        for(int q = 0; q < 100; q++){
            Email e = new Email(q, null, null, 0, false,
                    false, new SentimentScore(1,0,0, 1), null);
            emails.add(e);
        }
        scorer = new SentimentScoreCallable(emails);
        try {
            int score = (int) scorer.call();
            System.out.println("Positive score "+score);
            assertEquals(score, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //This test tests all negative.
        emails = new ArrayList<>();
        for(int q = 0; q < 100; q++){
            Email e = new Email(q, null, null, 0, false,
                    false, new SentimentScore(0,1,0, -1), null);
            emails.add(e);
        }
        scorer = new SentimentScoreCallable(emails);
        try {
            int score = (int) scorer.call();
            System.out.println("Negative score "+score);
            assertEquals(score, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //This test tests half and half.
        emails = new ArrayList<>();
        int pos = 0;
        int neg = 0;
        for(int q = 0; q < 100; q++){
            int even = q%2;
            if(even==0) neg++;
            else pos++;
            Email e = new Email(q, null, null, 0, false,
                    false, new SentimentScore(0,0,0, even), null);
            emails.add(e);
        }
        System.out.println("P:"+pos+",N:"+neg);
        scorer = new SentimentScoreCallable(emails);
        try {
            int score = (int) scorer.call();
            System.out.println("Half and half score "+score);
            assertEquals(50, score);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //This test tests a random distribution.
        emails = new ArrayList<>();
        pos = 0;
        for(int q = 0; q < 100; q++){
            double comp = Math.random()*2-1;
            SentimentScore s = new SentimentScore(0,0,0, comp);
            int resultingNum = AnalyzeThis.evaluateSentiment(s);
            if(resultingNum==2) pos++;
            Email e = new Email(q, null, null, 0, false,
                    false, s, null);
            emails.add(e);
        }
        System.out.println("P:"+pos);
        scorer = new SentimentScoreCallable(emails);
        try {
            int score = (int) scorer.call();
            System.out.println("Random score "+score);
            assertEquals(pos, score);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
