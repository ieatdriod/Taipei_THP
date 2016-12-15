package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.HpsCoreFaq;
import tw.com.mitac.thp.bean.HpsCoreFaqType;

public class FrontHpsCoreFaqTypeAction extends BasisFrontLoginAction {

	@Override
	public String execute() throws Exception {

		String sysid = request.getParameter(PK);

		List<HpsCoreFaqType> hpsCoreFaqTypeList = cloudDao.queryTable(sf(), HpsCoreFaqType.class,
				new QueryGroup(new QueryRule(IS_ENABLED, true)), new QueryOrder[0],
				// 起始點
				null,
				// 比數
				null);

		logger.debug(hpsCoreFaqTypeList.size());
		request.setAttribute("typeList", hpsCoreFaqTypeList);

		if (
		// sysid==null || sysid.equals("")
		StringUtils.isBlank(sysid)) {
			sysid = hpsCoreFaqTypeList.get(0).getSysid();
		}

		List<HpsCoreFaq> hpsCoreFaqList = cloudDao.queryTable(sf(), HpsCoreFaq.class,
				new QueryGroup(new QueryRule("hpsCoreFaqTypeSysid",EQ,sysid)), new QueryOrder[0],
				// 起始點
				null,
				// 比數
				null);
		request.setAttribute("hpsCoreFaqList", hpsCoreFaqList);

		
		return SUCCESS;

	}

}
