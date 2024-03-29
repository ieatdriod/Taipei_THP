package tw.com.mitac.thp.action;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;

// Generated Mon Mar 21 11:29:23 CST 2016 by GenCode.java

import tw.com.mitac.thp.bean.CpsConfig;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsVendorProfile;

/**
 * CpsConfigAction generated by GenCode.java
 */
public class CpsConfigAction extends BasisCrudAction<CpsConfig> {

	
	public boolean getWithoutSaveAndNew() {
		return true;
	}
	
	
	
	@Override
	public String main() {

		String result = super.main();

		List<CpsConfig> cpsConfigList = cloudDao.queryTable(sf(), CpsConfig.class,
				new QueryGroup(), new QueryOrder[0], null, null);
		if (cpsConfigList.size() > 0) {
			String cpsConfigListSysid = cpsConfigList.get(0).getSysid();
			String url = getActionKey() + "_edit?bean.sysid=" + cpsConfigListSysid;
			redirectPage = url;
			return REDIRECT_PAGE;

		}
		return result;
	}

}