package tw.com.mitac.thp.meeting.report.report;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.base.ResponseType;

public class DailyReport implements ResponseType {
	private String year;
	private String month;
	private List<Date> dates;
	
	public DailyReport(String src) throws JSONException {
		JSONObject json = new JSONObject(src);
		this.year = json.getString("year");
		this.month = json.getString("month");
		if (json.has("dates")) {
			dates = new ArrayList<Date>();
			JSONArray arr = json.getJSONArray("dates");
			Date date = null;
			for(int i=0; i<arr.length(); i++) {
				date = new Date(arr.get(i).toString());
				dates.add(date);
			}			
		}
	}
	
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public List<Date> getDates() {
		return dates;
	}
	public void setDates(List<Date> dates) {
		this.dates = dates;
	}
	@Override
	public String toString() {
		return "DailyReport [year=" + year + ", month=" + month + ", dates=" + dates + "]";
	}
	
}
