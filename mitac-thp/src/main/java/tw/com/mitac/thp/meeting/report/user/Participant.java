package tw.com.mitac.thp.meeting.report.user;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.ZoomMeetingApi;

/**
 * 取得單一帳號報表、所有帳號會議明細表的明細資料
 * @author Administrator
 *
 */
public class Participant {
	private String name;
	private Date join_time;
	private Date leave_time;
	
	public Participant(String src) throws JSONException,  ParseException{
		JSONObject json = new JSONObject(src);
		this.name = json.getString("name");
		String[] pattern = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
		this.join_time = ZoomMeetingApi.TimeZoneConverDate(json.getString("join_time"), pattern, "GMT", "Asia/Taipei");
		this.leave_time = ZoomMeetingApi.TimeZoneConverDate(json.getString("leave_time"), pattern, "GMT", "Asia/Taipei");		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getJoin_time() {
		return join_time;
	}
	public void setJoin_time(Date join_time) {
		this.join_time = join_time;
	}
	public Date getLeave_time() {
		return leave_time;
	}
	public void setLeave_time(Date leave_time) {
		this.leave_time = leave_time;
	}
	@Override
	public String toString() {
		return "Participant [name=" + name + ", join_time=" + join_time + ", leave_time=" + leave_time + "]";
	}

	
}
