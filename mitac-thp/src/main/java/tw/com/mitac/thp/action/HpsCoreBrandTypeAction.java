package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.HpsCoreBrandType;
import tw.com.mitac.thp.bean.HpsCoreBrandTypeBrand;

public class HpsCoreBrandTypeAction extends DetailController<HpsCoreBrandType> {


	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get(ID)))
			rules.add(new QueryRule(ID, CN, beaninfo.get(ID)));
		if (StringUtils.isNotBlank(beaninfo.get(NAME)))
			rules.add(new QueryRule(NAME, CN, beaninfo.get(NAME)));
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}
}