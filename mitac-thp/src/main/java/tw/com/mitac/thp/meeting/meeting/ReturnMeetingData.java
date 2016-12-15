package tw.com.mitac.thp.meeting.meeting;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.base.ResponseType;

public class ReturnMeetingData extends BaseMeetingData implements ResponseType {

	//找不到會議室資料時會有error code
	private Integer error_code;
	private String error_message;
	
	private String uuid;
	private String id;
	private String start_url;
	private String join_url;
	private Date created_at;
	private String host_id;
	private Integer status;
	
	public ReturnMeetingData(String src) throws JSONException, ParseException, UnsupportedEncodingException {
		JSONObject json = new JSONObject(src);
		if (json.has("error")) { //查詢會議室時給無效id會用到這個錯誤判斷, 跟id有效但是找不到會議室的錯誤不同
			this.host_id = json.get("host_id").toString();			
			JSONObject errjson = new JSONObject(json.get("error").toString());
			this.error_code = errjson.getInt("code");
			this.error_message = errjson.get("message").toString();
			return;
		}		
		
		this.setTopic(ZoomMeetingApi.toURLDecode(json.get("topic").toString()));
		this.setType(json.getInt("type"));
		
		//轉換GMT時間為Taipei時間, zoom回傳start_time字串範例:2016-03-05T06:00:00Z
		this.setStart_time(ZoomMeetingApi.TimeZoneConverDate(json.get("start_time").toString(), Response.datePattern, "GMT", "Asia/Taipei"));
		this.setDuration(json.getInt("duration"));
		this.setTimezone(json.get("timezone").toString());
		this.setPassword(json.get("password").toString());
		this.setOption_jbh(json.getBoolean("option_jbh"));
		this.setOption_start_type(json.get("option_start_type").toString());
		this.setOption_host_video(json.getBoolean("option_host_video"));
		this.setOption_participants_video(json.getBoolean("option_participants_video"));
		this.setOption_audio(json.get("option_audio").toString());
		this.uuid = json.get("uuid").toString();
		this.id = json.get("id").toString();
		this.start_url = json.get("start_url").toString();
		this.join_url = json.get("join_url").toString();
		this.created_at = ZoomMeetingApi.TimeZoneConverDate(json.get("created_at").toString(), Response.datePattern, "GMT", "Asia/Taipei");
		this.host_id = json.get("host_id").toString();
		if (json.has("status")) {
			this.status = json.getInt("status");			
		}
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStart_url() {
		return start_url;
	}
	public void setStart_url(String start_url) {
		this.start_url = start_url;
	}
	public String getJoin_url() {
		return join_url;
	}
	public void setJoin_url(String join_url) {
		this.join_url = join_url;
	}
	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}
	public String getHost_id() {
		return host_id;
	}
	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}
	

	public Integer getError_code() {
		return error_code;
	}

	public void setError_code(Integer error_code) {
		this.error_code = error_code;
	}

	public String getError_message() {
		return error_message;
	}

	public void setError_Message(String error_message) {
		this.error_message = error_message;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "ReturnMeetingData [error_code=" + error_code + ", error_message=" + error_message + ", uuid=" + uuid + ", id=" + id
				+ ", start_url=" + start_url + ", join_url=" + join_url + ", created_at=" + created_at + ", host_id="
				+ host_id + ", status=" + status 
				+ ", " + super.toString() + "]";
	}
	
}
