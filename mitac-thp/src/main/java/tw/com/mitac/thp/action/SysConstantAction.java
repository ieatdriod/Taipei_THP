package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.SysConstant;

public class SysConstantAction extends BasisCrudAction<SysConstant> {
	@Override
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get("constantType")))
			rules.add(new QueryRule("constantType", beaninfo.get("constantType")));
		if (StringUtils.isNotBlank(beaninfo.get("constantOption")))
			rules.add(new QueryRule("constantOption", CN, beaninfo.get("constantOption")));
		if (StringUtils.isNotBlank(beaninfo.get("defaultValue")))
			rules.add(new QueryRule("defaultValue", CN, beaninfo.get("defaultValue")));
		return new QueryGroup(AND, rules.toArray(new QueryRule[0]), null);
	}
}