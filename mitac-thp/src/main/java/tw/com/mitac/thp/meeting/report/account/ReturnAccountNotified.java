package tw.com.mitac.thp.meeting.report.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.base.ResponseType;

/**
 * 帳號現況明細資料
 * @author Administrator
 *
 */
public class ReturnAccountNotified implements ResponseType{
	private Integer total_records;
	private List<User> users;
	
	public ReturnAccountNotified(String src) throws JSONException {
		JSONObject json = new JSONObject(src);
		total_records = json.getInt("total_records");
		if (json.has("users")) {
			users = new ArrayList<User>();
			JSONArray arr = json.getJSONArray("users");
			User user = null;
			for(int i=0; i<arr.length(); i++) {
				user = new User(arr.get(i).toString());
				users.add(user);
			}			
		}
	}
	
	public Integer getTotal_records() {
		return total_records;
	}
	public void setTotal_records(Integer total_records) {
		this.total_records = total_records;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	@Override
	public String toString() {
		return "ReturnAccountNotified [total_records=" + total_records + ", users=" + users + "]";
	}

	
}
