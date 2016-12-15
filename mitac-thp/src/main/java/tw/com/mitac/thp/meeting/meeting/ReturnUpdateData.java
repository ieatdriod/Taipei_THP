package tw.com.mitac.thp.meeting.meeting;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.base.ResponseType;

public class ReturnUpdateData implements ResponseType {
	
	private Integer error_code;
	private String error_message;
	
	private String id;
	private Date updated_at;
	
	public ReturnUpdateData(String src) throws JSONException, ParseException {
		JSONObject json = new JSONObject(src);
		
		if (json.has("error")) { //查詢會議室時給無效id會用到這個錯誤判斷, 跟id有效但是找不到會議室的錯誤不同
			JSONObject errjson = new JSONObject(json.get("error").toString());
			this.error_code = errjson.getInt("code");
			this.error_message = errjson.get("message").toString();
			return;
		}		
		
		this.id = json.get("id").toString();
		this.updated_at = ZoomMeetingApi.TimeZoneConverDate(json.get("updated_at").toString(), Response.datePattern, "GMT", "Asia/Taipei");
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
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

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	@Override
	public String toString() {
		return "ReturnUpdateData [error_code=" + error_code + ", error_message=" + error_message + ", id=" + id
				+ ", updated_at=" + updated_at + "]";
	}


	
}
