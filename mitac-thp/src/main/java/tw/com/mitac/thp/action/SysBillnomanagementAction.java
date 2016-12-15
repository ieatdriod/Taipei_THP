package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.SysBillnomanagement;

public class SysBillnomanagementAction extends BasisCrudAction<SysBillnomanagement> {
	@Override
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get(ID)))
			rules.add(new QueryRule(ID, BW, beaninfo.get(ID)));
		if (StringUtils.isNotBlank(beaninfo.get("billname")))
			rules.add(new QueryRule("billname", CN, beaninfo.get("billname")));
		if (StringUtils.isNotBlank(beaninfo.get("classname")))
			rules.add(new QueryRule("classname", CN, beaninfo.get("classname")));
		if (StringUtils.isNotBlank(beaninfo.get("property")))
			rules.add(new QueryRule("property", CN, beaninfo.get("property")));
		if (StringUtils.isNotBlank(beaninfo.get("headword")))
			rules.add(new QueryRule("headword", BW, beaninfo.get("headword")));
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}
}