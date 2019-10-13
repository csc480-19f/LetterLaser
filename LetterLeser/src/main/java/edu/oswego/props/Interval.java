package edu.oswego.props;

public enum Interval {
	
	WEEK,
	MONTH,
	YEAR;
	
	public static Interval parse(String interval) {
		if (interval.equals("WEEK"))
			return WEEK;
		else if (interval.equals("MONTH"))
			return MONTH;
		else if (interval.equals("YEAR"))
			return YEAR;
		
		return null;
	}

}
