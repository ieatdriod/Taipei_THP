package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreDeliveryWay;

public class HpsCoreDeliveryWayAction extends BasisCrudAction<HpsCoreDeliveryWay> {

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
		String result = super.edit();
		// 預設帶出健康食品館
		if (StringUtils.isBlank(bean.getCreator())) {
			List<CpsEntity> entity = (List<CpsEntity>) cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(
					new QueryRule("dataId", "hps")), new QueryOrder[0], null, null);
			if (entity.size() > 0) {
				bean.setEntitySysid(entity.get(0).getSysid());
				beaninfo.put("entitySysidShow", entity.get(0).getDataId() + "：" + entity.get(0).getName());
			}
		}
		return result;
	}

	@Override
	protected boolean executeSave() {
		// TODO 運費計算

		return super.executeSave();
	}
}