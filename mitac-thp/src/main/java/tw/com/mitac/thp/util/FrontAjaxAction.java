package tw.com.mitac.thp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsCity;
import tw.com.mitac.thp.bean.CpsZipcode;
import tw.com.mitac.thp.bean.HpsCoreFavoriteItem;
import tw.com.mitac.tool.dao.CloudDAO;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontAjaxAction extends tw.com.mitac.thp.action.BasisTenancyAction {
	public String queryTableCpsCity() {
		String countrySysid = request.getParameter("countrySysid");

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<CpsCity> list = cloudDao.queryTable(sf(), CpsCity.class, new QueryGroup(new QueryRule(IS_ENABLED, true),
				new QueryRule("countrySysid", countrySysid)), new QueryOrder[] { new QueryOrder("isFar"),
				new QueryOrder(ID) }, null, null);
		ObjectMapper m = new ObjectMapper();
		for (CpsCity bean : list) {
			Map<String, Object> props = m.convertValue(bean, Map.class);
			mapList.add(props);
		}

		JSONArray jsonArray = new JSONArray(mapList);
		resultString = jsonArray.toString();
		logger.debug("resultString:" + resultString.length());
		return JSON_RESULT;
	}

	public String queryTableCpsZipcode() {
		String citySysid = request.getParameter("citySysid");

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<CpsZipcode> list = cloudDao.queryTable(sf(), CpsZipcode.class, new QueryGroup(new QueryRule("citySysid",
				citySysid)), new QueryOrder[] { new QueryOrder(ID) }, null, null);
		ObjectMapper m = new ObjectMapper();
		for (CpsZipcode bean : list) {
			Map<String, Object> props = m.convertValue(bean, Map.class);
			mapList.add(props);
		}

		JSONArray jsonArray = new JSONArray(mapList);
		resultString = jsonArray.toString();
		logger.debug("resultString:" + resultString.length());
		return JSON_RESULT;
	}

	public String ajaxAddFavoriteItem() {
		String memberSysid = request.getParameter("memberSysid");
		String vendorSysid = request.getParameter("vendorSysid");
		String itemSysid = request.getParameter("itemSysid");
		QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", memberSysid), new QueryRule("vendorSysid",
				vendorSysid), new QueryRule("itemSysid", itemSysid));
		if (StringUtils.isNotBlank(memberSysid) && StringUtils.isNotBlank(vendorSysid)
				&& StringUtils.isNotBlank(itemSysid)) {
			int count = cloudDao.queryTableCount(sf(), HpsCoreFavoriteItem.class, queryGroup);
			if (count == 0) {
				HpsCoreFavoriteItem obj = new HpsCoreFavoriteItem();
				Util.defaultPK(obj);
				defaultValue(obj);
				obj.setMemberSysid(memberSysid);
				obj.setVendorSysid(vendorSysid);
				obj.setItemSysid(itemSysid);
				cloudDao.save(sf(), new Object[] { obj }, false, CloudDAO.SAVE_TYPE_SAVE);
			}
		}
		resultString = SUCCESS;
		return JSON_RESULT;
	}

	public String ajaxDelFavoriteItem() {
		String memberSysid = request.getParameter("memberSysid");
		String itemSysid = request.getParameter("itemSysid");
		QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", memberSysid), new QueryRule("itemSysid",
				itemSysid));
		if (StringUtils.isNotBlank(memberSysid) && StringUtils.isNotBlank(itemSysid)) {
			cloudDao.save(sf(), new DeleteStatement(HpsCoreFavoriteItem.class.getSimpleName(), queryGroup));
		}
		resultString = SUCCESS;
		return JSON_RESULT;
	}
}