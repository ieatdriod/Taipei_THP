package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.Util;

public class CpsMemberAction extends BasisCrudAction<CpsMember> {
	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<CpsMember> list = (List<CpsMember>) oldArr[1];
		for (CpsMember bean : list) {
			if ("CpsEntity".equals(bean.getSourceType())) {
				CpsEntity source = createDataTable(CpsEntity.class).get(bean.getSourceSysid());
				bean.setSourceSysid(source.getDataId() + getSplitChar() + source.getName());
			} else if (CpsVendor.class.getSimpleName().equals(bean.getSourceType())) {
				CpsVendor source = createDataTable(CpsVendor.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getName());
			} else {
				bean.setSourceSysid("");
			}
		}
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	@Override
	public String edit() {
		String result = super.edit();
		if (CpsEntity.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("entitySysid", bean.getSourceSysid());
			CpsEntity source = createDataTable(CpsEntity.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("entitySysidShow", source.getDataId() + getSplitChar() + source.getName());
		} else if (CpsVendor.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("vendorSysid", bean.getSourceSysid());
			CpsVendor source = createDataTable(CpsVendor.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("vendorSysidShow", source.getName());
		} else {
			bean.setSourceSysid("");
		}
		return result;
	}

	@Override
	protected boolean executeSave() {
		if (StringUtils.isNotBlank(beaninfo.get("password")))
			bean.setPassword(Util.encode(beaninfo.get("password")));

		return super.executeSave();
	}
}