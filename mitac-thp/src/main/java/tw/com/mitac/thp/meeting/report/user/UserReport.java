package tw.com.mitac.thp.meeting.report.user;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.base.ResponseType;

/**
 * 取得單一帳號報表、所有帳號會議明細表的明細資料
 * @author Administrator
 *
 */
public class UserReport implements ResponseType {
	
	private String from;
	private String to;
	private Integer page_count;
	private Integer page_number;
	private Integer page_size;
	private String total_records;
	private List<Meeting> meetings;
	
	public UserReport(String src) throws JSONException, ParseException, UnsupportedEncodingException {
		JSONObject json = new JSONObject(src);
		this.from = json.getString("from");
		this.to = json.getString("to");
		this.page_count = json.getInt("page_count");
		this.page_number = json.getInt("page_number");
		this.page_size = json.getInt("page_size");
		this.total_records = json.get("total_records").toString();
		if (json.has("meetings")) {
			meetings = new ArrayList<Meeting>();
			JSONArray arr = json.getJSONArray("meetings");
			Meeting meeting = null;
			for(int i=0; i<arr.length(); i++) {
				meeting = new Meeting(arr.get(i).toString());
				meetings.add(meeting);
			}			
		}
		
	}
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Integer getPage_count() {
		return page_count;
	}
	public void setPage_count(Integer page_count) {
		this.page_count = page_count;
	}
	public Integer getPage_number() {
		return page_number;
	}
	public void setPage_number(Integer page_number) {
		this.page_number = page_number;
	}
	public Integer getPage_size() {
		return page_size;
	}
	public void setPage_size(Integer page_size) {
		this.page_size = page_size;
	}
	public String getTotal_records() {
		return total_records;
	}
	public void setTotal_records(String total_records) {
		this.total_records = total_records;
	}
	public List<Meeting> getMeetings() {
		return meetings;
	}
	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
	}
	
	@Override
	public String toString() {
		return "UserData [from=" + from + ", to=" + to + ", page_count=" + page_count + ", page_number=" + page_number
				+ ", page_size=" + page_size + ", total_records=" + total_records + ", meetings=" + meetings + "]";
	}
	
	
	
}
