package edu.oswego.runnables;

import com.google.gson.JsonObject;
import edu.oswego.model.Email;
import edu.oswego.sentiment.AnalyzeThis;

import java.util.List;
import java.util.concurrent.Callable;

public class SentimentScoreCallable implements Callable {
	private List<Email> emails;

	public SentimentScoreCallable(List<Email> emails) {
		this.emails = emails;
	}

	/**
	 * This method determines the percentage of positive emails, disregarding that
	 * neutral scores exist and are meaningful.
	 */
	@Override
	public Object call() throws Exception {
		int positive = 0;
		int negative = 0;
		int neutral = 0;
		for (Email e : emails) {
			if(e.getSentimentScore()==null){continue;}
			int score = AnalyzeThis.evaluateSentiment(e.getSentimentScore());
			switch(score){
				case 0:
					negative++;
					break;
				case 1:
					neutral++;
					break;
				case 2:
					positive++;
					break;
			}
		}

		JsonObject sentiment = new JsonObject();

		sentiment.addProperty("positive", (positive/100) / emails.size());
		sentiment.addProperty("negative", (negative/100) / emails.size());
		sentiment.addProperty("neutral", (neutral/100) / emails.size());

		return sentiment;
	}
}
