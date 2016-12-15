package tw.com.mitac.thp.action;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.CpsEntity;

// Generated Mon May 30 17:50:03 CST 2016 by GenCode.java

import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsCollaboration;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;

/**
 * CpsSiteMember2Action generated by GenCode.java
 */
public class CpsSiteMember2Action extends CpsSiteMember2BasisAction<CpsSiteMember> {

	// 共通移至CpsSiteMember2BasisAction
	@Override
	protected String getEntityType() {
		return "MTS";
	}

	public List<MtsVendorProfile> getMtsVendorProfileList() {
		List<MtsVendorProfile> mtsVendorProfileList = null;
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class, new QueryGroup(),
					new QueryOrder[0], null, null);

		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class,
					new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid())), new QueryOrder[0],
					null, null);

		}
		request.setAttribute("mtsVendorProfileList", mtsVendorProfileList);
		return mtsVendorProfileList;
	}

	public List<MtsCooperation> getMtsCooperationList() {
		List<MtsCooperation> mtsCooperationList = null;
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class, new QueryGroup(), new QueryOrder[0],
					null, null);

		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class,
					new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid())), new QueryOrder[0],
					null, null);

		}
		request.setAttribute("mtsCooperationList", mtsCooperationList);
		return mtsCooperationList;
	}

	public List<MtsProducts> getMtsProductsList() {
		List<MtsProducts> mtsProductsList = null;
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(),
					new QueryOrder[0], null, null);

		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class,
					new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid())), new QueryOrder[0],
					null, null);

		}
		request.setAttribute("mtsProductsList", mtsProductsList);
		return mtsProductsList;
	}

	public List<MtsHighlight> getMtsHighlightList() {
		List<MtsHighlight> mtsHighlightList = null;
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsHighlightList = cloudDao.queryTable(sf(), MtsHighlight.class, new QueryGroup(), new QueryOrder[0], null,
					null);

		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsHighlightList = cloudDao.queryTable(sf(), MtsHighlight.class,
					new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid())), new QueryOrder[0],
					null, null);

		}
		request.setAttribute("mtsHighlightList", mtsHighlightList);
		return mtsHighlightList;
	}
}