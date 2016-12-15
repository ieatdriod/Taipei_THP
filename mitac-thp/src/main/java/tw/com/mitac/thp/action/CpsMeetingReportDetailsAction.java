package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMeetingReportDetails;

public class CpsMeetingReportDetailsAction extends BasisCrudAction<CpsMeetingReportDetails> {

	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = null;
		try {
			rules = new ArrayList<QueryRule>();
			if (StringUtils.isNotBlank(beaninfo.get("meetingDateS"))) {
				rules.add(new QueryRule("meetingDate", GE, sdfYMD.parse(beaninfo.get("meetingDateS"))));
			}
			if (StringUtils.isNotBlank(beaninfo.get("meetingDateE"))) {
				rules.add(new QueryRule("meetingDate", LE, sdfYMD.parse(beaninfo.get("meetingDateE"))));
			}

			if (rules.size() == 0) {
				rules = new ArrayList<QueryRule>();
				String dateStr = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
				rules.add(new QueryRule("meetingDate", GE, sdfYMD.parse(dateStr)));
				rules.add(new QueryRule("meetingDate", LE, sdfYMD.parse(dateStr)));
			}
		} catch (Exception e) {

		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		if (queryGroup.getGroups().length == 0) { // 未給條件時不帶資料
			return new Object[] { 0, new ArrayList() };
		}
		return super.jqgridList(clazz, queryGroup, orders, from, length);
	}
}