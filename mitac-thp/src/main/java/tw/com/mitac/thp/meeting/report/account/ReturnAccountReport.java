package tw.com.mitac.thp.meeting.report.account;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.base.ResponseType;


/**
 * 取得帳號報表明細
 * @author Administrator
 *
 */
public class ReturnAccountReport implements ResponseType{

	private String from;
	private String to;
	private Integer page_count;
	private Integer page_number;
	private Integer page_size;
	private String total_records;
	private String total_meetings;
	private String total_participants;
	private String total_meeting_minutes;
	private List<User1> users;
	
	public ReturnAccountReport(String src) throws JSONException, ParseException {
		JSONObject json = new JSONObject(src);
		this.from = json.getString("from");
		this.to = json.getString("to");
		this.page_count = json.getInt("page_count");
		this.page_number = json.getInt("page_number");
		this.page_size = json.getInt("page_size");
		this.total_records = json.get("total_records").toString();
		this.total_meetings = json.get("total_meetings").toString();
		this.total_participants = json.get("total_participants").toString();
		this.total_meeting_minutes = json.get("total_meeting_minutes").toString();
		if (json.has("users")) {
			users = new ArrayList<User1>();
			JSONArray arr = json.getJSONArray("users");
			User1 user = null;
			for(int i=0; i<arr.length(); i++) {
				user = new User1(arr.get(i).toString());
				users.add(user);
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
	public String getTotal_meetings() {
		return total_meetings;
	}
	public void setTotal_meetings(String total_meetings) {
		this.total_meetings = total_meetings;
	}
	public String getTotal_participants() {
		return total_participants;
	}
	public void setTotal_participants(String total_participants) {
		this.total_participants = total_participants;
	}
	public String getTotal_meeting_minutes() {
		return total_meeting_minutes;
	}
	public void setTotal_meeting_minutes(String total_meeting_minutes) {
		this.total_meeting_minutes = total_meeting_minutes;
	}
	public List<User1> getUsers() {
		return users;
	}
	public void setUsers(List<User1> users) {
		this.users = users;
	}
	@Override
	public String toString() {
		return "ReturnAccountReport [from=" + from + ", to=" + to + ", page_count=" + page_count + ", page_number=" + page_number
				+ ", page_size=" + page_size + ", total_records=" + total_records + ", total_meetings=" + total_meetings
				+ ", total_participants=" + total_participants + ", total_meeting_minutes=" + total_meeting_minutes
				+ ", users=" + users + "]";
	}
	
	
	
}
