package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsFavouriteList;
import tw.com.mitac.thp.login2.UserData2;
import tw.com.mitac.thp.util.Util;
import tw.com.mitac.tool.dao.CloudDAO;

@SuppressWarnings("rawtypes")
public class IndexMtsPageAction extends BasisTenancyAction {
	public String ajaxFindFavouriteList() {
		resultList = new ArrayList();
		String memberSysid = request.getParameter("memberSysid");
		String entityType = request.getParameter("entityType");
		String favouriteType = request.getParameter("favouriteType");
		String sourceSysid = request.getParameter("sourceSysid");
		List<QueryRule> ql = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(memberSysid)) {
			ql.add(new QueryRule("memberSysid", memberSysid));
			if (StringUtils.isNotBlank(entityType))
				ql.add(new QueryRule("entityType", entityType));
			if (StringUtils.isNotBlank(favouriteType))
				ql.add(new QueryRule("favouriteType", favouriteType));
			if (StringUtils.isNotBlank(sourceSysid))
				ql.add(new QueryRule("sourceSysid", sourceSysid));
			resultList = cloudDao.findProperty(sf(), CpsFavouriteList.class,
					new QueryGroup(ql.toArray(new QueryRule[0])), null, false, "entityType", "vendorSysid",
					"favouriteType", "sourceSysid");
		}
		return JSON_RESULT;
	}

	public String ajaxAddFavouriteList() {
		UserData2 userData = (UserData2) session.get("userData2");
		if (userData == null) {
			resultString = ERROR;
			return JSON_RESULT;
		}

		String memberSysid = request.getParameter("memberSysid");
		String vendorSysid = request.getParameter("vendorSysid");
		String entityType = request.getParameter("entityType");
		String favouriteType = request.getParameter("favouriteType");
		String sourceSysid = request.getParameter("sourceSysid");
		List<QueryRule> ql = new ArrayList<QueryRule>();
		ql.add(new QueryRule("memberSysid", memberSysid));
		ql.add(new QueryRule("vendorSysid", vendorSysid));
		ql.add(new QueryRule("entityType", entityType));
		ql.add(new QueryRule("favouriteType", favouriteType));
		ql.add(new QueryRule("sourceSysid", sourceSysid));
		QueryGroup queryGroup = new QueryGroup(ql.toArray(new QueryRule[0]));

		for (QueryRule queryRule : ql) {
			String data = (String) queryRule.getData();
			if (StringUtils.isBlank(data)) {
				resultString = "BLANK_DATA";
				return JSON_RESULT;
			}
		}

		int count = cloudDao.queryTableCount(sf(), CpsFavouriteList.class, queryGroup);
		if (count == 0) {
			CpsFavouriteList obj = new CpsFavouriteList();
			Util.defaultPK(obj);
			defaultValue(obj);
			obj.setMemberSysid(memberSysid);
			obj.setVendorSysid(vendorSysid);
			obj.setEntityType(entityType);
			obj.setFavouriteType(favouriteType);
			obj.setSourceSysid(sourceSysid);
			cloudDao.save(sf(), new Object[] { obj }, false, CloudDAO.SAVE_TYPE_SAVE);
		}

		resultString = SUCCESS;
		return JSON_RESULT;
	}

	public String ajaxDelFavouriteList() {
		String memberSysid = request.getParameter("memberSysid");
		String vendorSysid = request.getParameter("vendorSysid");
		String entityType = request.getParameter("entityType");
		String favouriteType = request.getParameter("favouriteType");
		String sourceSysid = request.getParameter("sourceSysid");
		List<QueryRule> ql = new ArrayList<QueryRule>();
		ql.add(new QueryRule("memberSysid", memberSysid));
		ql.add(new QueryRule("vendorSysid", vendorSysid));
		ql.add(new QueryRule("entityType", entityType));
		ql.add(new QueryRule("favouriteType", favouriteType));
		ql.add(new QueryRule("sourceSysid", sourceSysid));
		QueryGroup queryGroup = new QueryGroup(ql.toArray(new QueryRule[0]));

		for (QueryRule queryRule : ql) {
			String data = (String) queryRule.getData();
			if (StringUtils.isBlank(data)) {
				resultString = "BLANK_DATA";
				return JSON_RESULT;
			}
		}

		cloudDao.save(sf(), new DeleteStatement(CpsFavouriteList.class.getSimpleName(), queryGroup));

		resultString = SUCCESS;
		return JSON_RESULT;
	}
}