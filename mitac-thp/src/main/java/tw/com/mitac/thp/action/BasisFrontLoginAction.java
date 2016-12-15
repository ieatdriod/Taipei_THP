package tw.com.mitac.thp.action;

import tw.com.mitac.thp.login2.UserData2;

public class BasisFrontLoginAction extends BasisTenancyAction {
	protected final UserData2 getUserData2() {
		return (UserData2) session.get("userData2");
	}

	protected String createOperatorValue() {
		return getUserData2().getUid();
	}
}