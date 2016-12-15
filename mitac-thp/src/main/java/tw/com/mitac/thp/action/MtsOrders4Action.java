package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersFeedback;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;

public class MtsOrders4Action extends DetailController<MtsOrders> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsOrdersFeedback.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", MtsOrdersProducts.class));
		detailClassMap.put("3", new DetailInfo("3", DETAIL_SET3, "detail3", MtsOrdersItems.class));
		return detailClassMap;
	}

	// jqgrid再加上過濾條件
	@Override
	protected QueryGroup getQueryRestrict() {
		// 以轉單後供應商才可見
		QueryGroup newQueryGroup = new QueryGroup(
		// new QueryRule(BILL_STATUS, "mts90"),
				new QueryRule("ordersType", "SO"));
		return newQueryGroup;
	}

	// 加入常用查詢條件
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = null;
		try {
			rules = new ArrayList<QueryRule>();

			// 起始日期
			if (StringUtils.isNotBlank(beaninfo.get("issueDateS"))) {
				rules.add(new QueryRule("issueDate", GE, sdfYMD.parse(beaninfo.get("issueDateS"))));
				// 大於
			}

			// 迄止日期
			if (StringUtils.isNotBlank(beaninfo.get("issueDateE"))) {
				rules.add(new QueryRule("issueDate", LE, sdfYMD.parse(beaninfo.get("issueDateE"))));
				// 小於等於
			}

			if (rules.size() == 0) {
				rules = new ArrayList<QueryRule>();
				String dateStr = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
				rules.add(new QueryRule("issueDate", GE, sdfYMD.parse(dateStr)));
				rules.add(new QueryRule("issueDate", LE, sdfYMD.parse(dateStr)));
			}

			// 訂單類別
			if (StringUtils.isNotBlank(beaninfo.get("odersType"))) {
				rules.add(new QueryRule("odersType", beaninfo.get("odersType")));
			}

			// 供應商
			if (StringUtils.isNotBlank(beaninfo.get("vendorSysid"))) {
				rules.add(new QueryRule("vendorSysid", beaninfo.get("vendorSysid")));
			}

		} catch (Exception e) {

		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}
}