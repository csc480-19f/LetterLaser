package edu.oswego.sentiment;

public class SentimentScore {

	private double positive, negative, neutral, compound;

	public SentimentScore(double positive, double negative, double neutral, double compound) {
		this.positive = positive;
		this.negative = negative;
		this.neutral = neutral;
		this.compound = compound;
	}

	public double getPositive() {
		return positive;
	}

	public void setPositive(double positive) {
		this.positive = positive;
	}

	public double getNegative() {
		return negative;
	}

	public void setNegative(double negative) {
		this.negative = negative;
	}

	public double getNeutral() {
		return neutral;
	}

	public void setNeutral(double neutral) {
		this.neutral = neutral;
	}

	public double getCompound() {
		return compound;
	}

	public void setCompound(double compound) {
		this.compound = compound;
	}

}
