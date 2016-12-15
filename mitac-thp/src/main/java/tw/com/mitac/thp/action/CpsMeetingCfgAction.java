package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.thp.bean.CpsMeetingCfg;

public class CpsMeetingCfgAction extends BasisCrudAction<CpsMeetingCfg> {
	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		// 將UsageEntitySysid代碼改為名稱
		Object[] rtnArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		System.out.println(rtnArr[1]);
		List<CpsMeetingCfg> list = (List<CpsMeetingCfg>) rtnArr[1];
		for (CpsMeetingCfg cmc : list) {
			String cmcStr = cmc.getUsageEntitySysid();
			String entity = "";
			if (StringUtils.isNotBlank(cmcStr)) {
				StringBuffer sb = new StringBuffer();
				String[] arrStr = cmcStr.split(",");
				for (String tmp : arrStr) {
					tmp = tmp.trim();
					sb.append("、").append(getText("bean." + tmp));
				}
				entity = sb.deleteCharAt(0).toString();
			}
			cmc.setUsageEntitySysid(entity);
		}

		return rtnArr;
	}
}