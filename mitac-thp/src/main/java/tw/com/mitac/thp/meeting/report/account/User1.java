package tw.com.mitac.thp.meeting.report.account;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;

/**
 * 取得帳號報表明細
 * @author Administrator
 *
 */
public class User1 {
	private String user_id;
	private String email;
	private Integer type;
	private Integer meetings;
	private Integer participants;
	private Integer meeting_minutes;
	private String last_client_version;
	private Date last_login_time;
	
	public User1(String src) throws JSONException, ParseException {
		JSONObject json = new JSONObject(src);
		this.user_id = json.getString("user_id");
		this.email = json.getString("email");
		this.type = json.getInt("type");
		this.meetings = json.getInt("meetings");
		this.participants = json.getInt("participants");
		this.meeting_minutes = json.getInt("meeting_minutes");
		this.last_client_version = json.getString("last_client_version");
		this.last_login_time = ZoomMeetingApi.TimeZoneConverDate(json.getString("last_login_time"), Response.datePattern, "GMT", "Asia/Taipei");		
	}
	
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getMeetings() {
		return meetings;
	}
	public void setMeetings(Integer meetings) {
		this.meetings = meetings;
	}
	public Integer getParticipants() {
		return participants;
	}
	public void setParticipants(Integer participants) {
		this.participants = participants;
	}
	public Integer getMeeting_minutes() {
		return meeting_minutes;
	}
	public void setMeeting_minutes(Integer meeting_minutes) {
		this.meeting_minutes = meeting_minutes;
	}
	public String getLast_client_version() {
		return last_client_version;
	}
	public void setLast_client_version(String last_client_version) {
		this.last_client_version = last_client_version;
	}
	public Date getLast_login_time() {
		return last_login_time;
	}
	public void setLast_login_time(Date last_login_time) {
		this.last_login_time = last_login_time;
	}
	@Override
	public String toString() {
		return "User1 [user_id=" + user_id + ", email=" + email + ", type=" + type + ", meetings=" + meetings
				+ ", participants=" + participants + ", meeting_minutes=" + meeting_minutes + ", last_client_version="
				+ last_client_version + ", last_login_time=" + last_login_time + "]";
	}

}
