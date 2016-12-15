package tw.com.mitac.thp.meeting.report.user;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;

/**
 * 取得單一帳號報表、所有帳號會議明細表的明細資料
 * @author Administrator
 *
 */
public class Meeting {
	private String host_id;
	private String number;
	private String topic;
	private Date start_time; //TimeZone已轉為Asia/taipei
	private Date end_time;
	private Integer duration;
	private List<Participant> participants;
	
	public Meeting(String src) throws JSONException, ParseException, UnsupportedEncodingException {
		JSONObject json = new JSONObject(src);
		if (json.has("host_id")) { //單一帳號無此欄位
			this.host_id = json.getString("host_id");
		}
		if (json.has("number")) {//所有user 會議室ID回傳number, 日期格式與單一user也不同
			this.number = json.getString("number");
		}
		if (json.has("id")) { //單一user 會議室ID回傳id
			this.number = String.valueOf(json.getInt("id"));
		}
		this.topic = ZoomMeetingApi.toURLDecode(json.getString("topic"));
		this.start_time = ZoomMeetingApi.TimeZoneConverDate(json.get("start_time").toString(), Response.datePattern, "GMT", "Asia/Taipei");
		this.end_time = ZoomMeetingApi.TimeZoneConverDate(json.get("end_time").toString(), Response.datePattern, "GMT", "Asia/Taipei");

		this.duration = json.getInt("duration");
		if (json.has("participants")) {
			if (json.has("participants")) {
				participants = new ArrayList<Participant>();
				JSONArray arr = json.getJSONArray("participants");
				Participant participant = null;
				for(int i=0; i<arr.length(); i++) {
					participant = new Participant(arr.get(i).toString());
					participants.add(participant);
				}			
			}			
		}
	}
	public String getHost_id() {
		return host_id;
	}
	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public Date getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public List<Participant> getParticipants() {
		return participants;
	}
	public void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}
	@Override
	public String toString() {
		return "Meeting [number=" + number + ", topic=" + topic + ", start_time=" + start_time + ", end_time="
				+ end_time + ", duration=" + duration + ", participants=" + participants + "]";
	}
	
}
