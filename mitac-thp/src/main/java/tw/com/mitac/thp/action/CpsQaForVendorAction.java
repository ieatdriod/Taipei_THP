package tw.com.mitac.thp.action;

// Generated Tue Mar 15 14:23:28 CST 2016 by GenCode.java

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsVendor;

/**
 * CpsQaAction generated by GenCode.java
 */
public class CpsQaForVendorAction extends CpsQaAction {
	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("qaDepartment", getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}
}