package edu.oswego.model;

public class SentimentScore {

	private double positive, negative, neutral, compound;

	public SentimentScore() {}

	public SentimentScore(double positive, double negative, double neutral, double compound) {
		this.positive = positive;
		this.negative = negative;
		this.neutral = neutral;
		this.compound = compound;
	}

	public double getPositive() {
		return positive;
	}

	public double getNegative() {
		return negative;
	}

	public double getNeutral() {
		return neutral;
	}

	public double getCompound() {
		return compound;
	}

}
