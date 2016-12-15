package tw.com.mitac.thp.action;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsEpaper;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.bean.HpsVendorItem;

// Generated Mon May 09 14:17:39 CST 2016 by GenCode.java

import tw.com.mitac.thp.bean.MtsMarquee;

/**
 * MtsMarqueeAction generated by GenCode.java
 */
public class MtsMarqueeAction extends BasisCrudAction<MtsMarquee> {
	
	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule(SOURCE_ID, "MTS"));
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule(SOURCE_ID, getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSourceId())) {
			CpsMember user = getUserAccount();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setSourceId("MTS");
			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setSourceId(user.getSourceSysid());
			}
		}
		String result = super.edit();
		return result;
	}
	
	
}