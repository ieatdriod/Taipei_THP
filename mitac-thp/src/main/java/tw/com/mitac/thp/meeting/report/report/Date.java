package tw.com.mitac.thp.meeting.report.report;

import org.json.JSONException;
import org.json.JSONObject;

public class Date {
	private String meeting_date;
	private String meetings;
	private String participants;
	private String meeting_minutes;
	
	public Date(String src) throws JSONException {
		JSONObject json = new JSONObject(src);
		this.meeting_date = json.getString("meeting_date");
		this.meetings = json.getString("meetings");
		this.participants = json.getString("participants");
		this.meeting_minutes = json.getString("meeting_minutes");
	}
	
	public String getMeeting_date() {
		return meeting_date;
	}
	public void setMeeting_date(String meeting_date) {
		this.meeting_date = meeting_date;
	}
	public String getMeetings() {
		return meetings;
	}
	public void setMeetings(String meetings) {
		this.meetings = meetings;
	}
	public String getParticipants() {
		return participants;
	}
	public void setParticipants(String participants) {
		this.participants = participants;
	}
	public String getMeeting_minutes() {
		return meeting_minutes;
	}
	public void setMeeting_minutes(String meeting_minutes) {
		this.meeting_minutes = meeting_minutes;
	}
	@Override
	public String toString() {
		return "Date [meeting_date=" + meeting_date + ", meetings=" + meetings + ", participants=" + participants
				+ ", meeting_minutes=" + meeting_minutes + "]";
	}
	
}
