package tw.com.mitac.thp.action;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsVendorHotItem;

public class HpsVendorHotItemAction extends BasisCrudAction<HpsVendorHotItem> {
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

	@Override
	public String edit() {
		if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			bean.setVendorSysid(getUserAccount().getSourceSysid());
			beaninfo.put("vendorSysid" + "Show",
					createDataDisplay(CpsVendor.class).get(getUserAccount().getSourceSysid()));
		}
		return super.edit();
	}
}