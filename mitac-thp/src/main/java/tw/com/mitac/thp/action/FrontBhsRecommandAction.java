package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsRecommand;

/** BHS_FW_002_標竿企業總覽 */
public class FrontBhsRecommandAction extends BasisTenancyAction {
	public String recommandInit() {
		String bhsRecommandSysid = request.getParameter("bhsRecommandSysid");
		if (StringUtils.isNotBlank(bhsRecommandSysid)) {
			List<BhsRecommand> bhsRecommandList = cloudDao.query(sf(), BhsRecommand.class, new QueryGroup(
					new QueryRule(PK, EQ, bhsRecommandSysid), new QueryRule(IS_ENABLED, true)), new QueryOrder[] {
					new QueryOrder(DATA_ORDER), new QueryOrder(PK) }, 0, null);
			addMultiLan(bhsRecommandList, sf(), BhsRecommand.class);
			request.setAttribute("bhsRecommandListFW002", bhsRecommandList);
		}
		return SUCCESS;
	}
}