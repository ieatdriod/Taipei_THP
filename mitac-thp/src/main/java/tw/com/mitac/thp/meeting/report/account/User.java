package tw.com.mitac.thp.meeting.report.account;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 帳號現況明細資料
 * @author Administrator
 *
 */
public class User {
	private String host_id;
	private Integer notified;
	
	public User(String src) throws JSONException {
		JSONObject json = new JSONObject(src);
		this.host_id = json.getString("host_id");
		this.notified = json.getInt("notified");
	}
	
	public String getHost_id() {
		return host_id;
	}
	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}
	public Integer getNotified() {
		return notified;
	}
	public void setNotified(Integer notified) {
		this.notified = notified;
	}
	@Override
	public String toString() {
		return "User [host_id=" + host_id + ", notified=" + notified + "]";
	}
	
}
