package tw.com.mitac.thp.login;

import tw.com.mitac.thp.action.BasisLoginAction;

public class IsLoginAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "x";
	}

	/**
	 * 切換測試模式
	 * 
	 * @return
	 */
	public String testMode() {
		Boolean isTest = (Boolean) session.get("isTest");
		if (isTest == null || !isTest)
			session.put("isTest", true);
		else
			session.put("isTest", false);
		return SUCCESS;
	}
}