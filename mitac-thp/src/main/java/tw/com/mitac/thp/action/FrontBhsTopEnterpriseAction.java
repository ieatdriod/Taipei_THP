package tw.com.mitac.thp.action;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsVendorMore;

public class FrontBhsTopEnterpriseAction extends BasisTenancyAction {

	public String bhsTopEnterpriseList() {
		List<BhsVendorMore> bhsVendorMoreList = (List<BhsVendorMore>) session.get("bhsVendorMoreList");
		if (bhsVendorMoreList == null) {
			bhsVendorMoreList = cloudDao.query(sf(), BhsVendorMore.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule("recommandStartDate", LE, systemDate),
					new QueryRule("recommandEndDate", GE, systemDate)), new QueryOrder[] {
					new QueryOrder("recommandOrder"), new QueryOrder("recommandStartDate") }, null, null);

			session.put("bhsVendorMoreList", bhsVendorMoreList);
			session.put("bhsVendorMoreSize", bhsVendorMoreList.size());
		}
		System.out.println("=bhsTopEnterpriseList=");
		return SUCCESS;
	}

}