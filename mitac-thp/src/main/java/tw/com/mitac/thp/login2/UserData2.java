package tw.com.mitac.thp.login2;

import tw.com.mitac.thp.bean.CpsSiteMember;

/**
 * for site member
 */
public class UserData2 implements java.io.Serializable {
	protected String uid;
	protected CpsSiteMember account;

	public UserData2(String uid, CpsSiteMember account) {
		super();
		this.uid = uid;
		this.account = account;
	}

	public final String getUid() {
		return uid;
	}

	public final CpsSiteMember getAccount() {
		return account;
	}
}