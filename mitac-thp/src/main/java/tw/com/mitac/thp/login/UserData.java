package tw.com.mitac.thp.login;

import java.util.HashMap;
import java.util.Map;

public class UserData implements java.io.Serializable {
	protected String uid;

	protected Map data = new HashMap();

	public UserData(String uid) {
		this.uid = uid;
	}

	public final String getUid() {
		return uid;
	}

	public final Map getData() {
		return data;
	}
}