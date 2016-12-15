package tw.com.mitac.thp.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreItemType;

public class HpsCoreItemTypeAction extends BasisCrudAction<HpsCoreItemType> {
	@Override
	public String getTreeParentKey() {
		return "parentItemTypeSysid";
	}

	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return QueryGroup.DEFAULT;
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	public String main() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			addActionMessage("管理者可以查詢所有項目");
		}
		return super.main();
	}

	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get(ID)))
			rules.add(new QueryRule(ID, CN, beaninfo.get(ID)));
		if (StringUtils.isNotBlank(beaninfo.get(NAME)))
			rules.add(new QueryRule(NAME, CN, beaninfo.get(NAME)));
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

	// @Override
	// protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup,
	// QueryOrder[] orders, Integer from,
	// Integer length) {
	// List<MtsOrders> list = new ArrayList<MtsOrders>();
	// List<?> qList = (List<String>) cloudDao.findProperty(sf(),
	// CpsMember.class, new QueryGroup(new QueryRule(
	// "uuid", getUserData().getUid())), new QueryOrder[0], false, "sourceType",
	// "sourceSysid");
	// if (qList.size() == 0)
	// return new Object[] { 0, list };
	//
	// // 平台（看全部） or 供應商（看自己）
	// Map<String, String> qMap = (Map<String, String>) qList.get(0);
	// QueryGroup newQueryGroup;
	// if ("CpsEntity".equals(qMap.get("sourceType"))) {
	// newQueryGroup = queryGroup;
	// } else if ("CpsVendor".equals(qMap.get("sourceType"))) {
	// newQueryGroup = new QueryGroup(AND,
	// new QueryRule[] { new QueryRule("vendorSysid", qMap.get("sourceSysid"))
	// }, queryGroup.getGroups());
	// } else {
	// return new Object[] { 0, list };
	// }
	//
	// Object[] oldArr = super.jqgridList(clazz, newQueryGroup, orders, from,
	// length);
	// list = (List<MtsOrders>) oldArr[1];
	// Object[] newArr = new Object[] { oldArr[0], list };
	// return newArr;
	// }

	@Override
	protected boolean executeSave() {
		// 未填廠商sysid表示為平台設定，填入＊號
		if (StringUtils.isBlank(bean.getVendorSysid()))
			bean.setVendorSysid("*");

		return super.executeSave();
	}

	public Boolean getIsLockAllotRate() {
		Boolean isLock = true;
		// 1.僅有平台管理員有權限設定
		// 2.PARENT_ITEM_TYPE_SYSID=null && VENDOR_SYSID=null及ALLOT_RATE=0才可開放編輯
		// List<String> qList = (List<String>) cloudDao.findProperty(sf(),
		// CpsMember.class, new QueryGroup(new QueryRule(
		// "uuid", getUserData().getUid()), new QueryRule("sourceType",
		// "CpsEntity")), new QueryOrder[0], false,
		// "sourceSysid");
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())
				&& StringUtils.isBlank(bean.getParentItemTypeSysid())
				&& (StringUtils.isBlank(bean.getVendorSysid()) || bean.getVendorSysid() == "*")
				&& BigDecimal.ZERO.compareTo(bean.getAllotRate()) == 0)
			isLock = false; // 符合則解鎖

		return isLock;
	}

	public Boolean getIsHideAllotRate() {
		Boolean isHide = false;
		// 3.如果登入是店家隱藏此欄位
		// List<String> qList = (List<String>) cloudDao.findProperty(sf(),
		// CpsMember.class, new QueryGroup(new QueryRule(
		// "uuid", getUserData().getUid()), new QueryRule("sourceType",
		// "CpsVendor")), new QueryOrder[0], false,
		// "sourceSysid");
		if (!CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
			isHide = true;

		return isHide;
	}
}