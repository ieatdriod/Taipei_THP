package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.jqgrid.bean.SysColumnConfig;

public class SysColumnConfigAction extends BasisCrudAction<SysColumnConfig> {
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get("tablename")))
			rules.add(new QueryRule("tablename", CN, beaninfo.get("tablename")));
		if (StringUtils.isNotBlank("columnId"))
			rules.add(new QueryRule("columnId", BW, beaninfo.get("columnId")));
		if (StringUtils.isNotBlank(beaninfo.get("columnName")))
			rules.add(new QueryRule("columnName", CN, beaninfo.get("columnName")));
		return new QueryGroup(AND, rules.toArray(new QueryRule[0]), null);
	}
}