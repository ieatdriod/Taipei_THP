package tw.com.mitac.thp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.ssh.util.QueryGroupUtil;
import tw.com.mitac.tool.dao.CloudDAO;
import tw.com.mitac.tool.dao.CloudTableDAO;

/**
 * <pre>
 * 萬用ajax
 * </pre>
 * 
 * 實現jsp版CloudDAO 限制必須登入使用(若不登入就可使用太過危險)
 */
public class AjaxAction extends tw.com.mitac.thp.action.BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "x";
	}

	/**
	 * @see CloudDAO#get(Object, Class, java.io.Serializable)
	 */
	public String get() {
		try {
			String className = request.getParameter("class");
			String pk = request.getParameter("pk");
			if (StringUtils.isNotBlank(pk)) {
				Class<?> clazz = Class.forName(Util.beanPackage + "." + className);
				Object bean = cloudDao.get(sf(), clazz, pk);
				if (bean != null) {
					JSONObject jsonObject = new JSONObject(bean);
					resultString = jsonObject.toString();
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSON_RESULT;
	}

	/**
	 * @see CloudDAO#queryTableCount(Object daoSolution,Class, QueryRule[],
	 *      QueryGroup[])
	 */
	public String queryTableCount() {
		try {
			String className = request.getParameter("class");
			String andQueryRulesStr = request.getParameter("andQueryRules");
			String andQueryGroupsStr = request.getParameter("andQueryGroups");
			if (StringUtils.isBlank(andQueryRulesStr))
				andQueryRulesStr = "[]";
			if (StringUtils.isBlank(andQueryGroupsStr))
				andQueryGroupsStr = "[]";
			Class<?> clazz = Class.forName(Util.beanPackage + "." + className);
			JSONArray andQueryRulesArr = new JSONArray(andQueryRulesStr);
			List<QueryRule> andQueryRuleList = QueryGroupUtil.jqgridRulesJSONArrayToQueryRuleList(clazz,
					andQueryRulesArr);

			JSONArray andQueryGroupsArr = new JSONArray(andQueryGroupsStr);
			List<QueryGroup> andQueryGroupsList = new ArrayList<QueryGroup>();
			for (int i = 0; i < andQueryGroupsArr.length(); i++) {
				JSONObject group = andQueryGroupsArr.getJSONObject(i);
				andQueryGroupsList.add(QueryGroupUtil.jqgridGroupJSONObjectToQueryGroup(clazz, group));
			}

			int count = cloudDao.queryTableCount(
					sf(),
					clazz,
					new QueryGroup(AND, andQueryRuleList.toArray(new QueryRule[0]), andQueryGroupsList
							.toArray(new QueryGroup[0])));
			resultString = String.valueOf(count);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return JSON_RESULT;
	}

	/**
	 * @see CloudTableDAO#queryTable(Object, Class, QueryGroup, QueryOrder[],
	 *      Integer, Integer)
	 */
	public String queryTable() {
		logger.debug("start");
		try {
			String className = request.getParameter("class");
			String andQueryRulesStr = request.getParameter("andQueryRules");
			String andQueryGroupsStr = request.getParameter("andQueryGroups");
			String orderStr = request.getParameter("orderBy");
			String firstResultStr = request.getParameter("firstResult");
			String maxResultsStr = request.getParameter("maxResults");
			logger.debug("queryTable 測試 class: " + className + " || andQueryRules: " + andQueryRulesStr);
			if (getIsTest()) {
				logger.debug("andQueryRulesStr: " + andQueryRulesStr);
				logger.debug("andQueryGroupsStr: " + andQueryGroupsStr);
				logger.debug("orderStr: " + orderStr);
				logger.debug("firstResultStr: " + firstResultStr);
				logger.debug("maxResultsStr: " + maxResultsStr);
			}
			if (StringUtils.isBlank(andQueryRulesStr))
				andQueryRulesStr = "[]";
			if (StringUtils.isBlank(andQueryGroupsStr))
				andQueryGroupsStr = "[]";
			if (StringUtils.isBlank(orderStr))
				orderStr = "[]";
			Class<?> clazz = null;
			try {
				clazz = Class.forName(Util.beanPackage + "." + className);
			} catch (ClassNotFoundException e1) {
			}
			JSONArray andQueryRulesArr = new JSONArray(andQueryRulesStr);
			List<QueryRule> andQueryRuleList = QueryGroupUtil.jqgridRulesJSONArrayToQueryRuleList(clazz,
					andQueryRulesArr);

			JSONArray andQueryGroupsArr = new JSONArray(andQueryGroupsStr);
			List<QueryGroup> andQueryGroupsList = new ArrayList<QueryGroup>();
			for (int i = 0; i < andQueryGroupsArr.length(); i++) {
				JSONObject group = andQueryGroupsArr.getJSONObject(i);
				andQueryGroupsList.add(QueryGroupUtil.jqgridGroupJSONObjectToQueryGroup(clazz, group));
			}

			JSONArray orderByArr = new JSONArray(orderStr);
			List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
			for (int i = 0; i < orderByArr.length(); i++) {
				JSONObject orderByObj = orderByArr.getJSONObject(i);
				String field = orderByObj.getString("field");
				String op = orderByObj.getString("op");

				queryOrderList.add(new QueryOrder(field, op));
			}

			Integer firstResult = null;
			Integer maxResults = null;
			if (StringUtils.isNotBlank(firstResultStr))
				try {
					firstResult = Integer.parseInt(firstResultStr);
				} catch (Exception e) {
				}
			if (StringUtils.isNotBlank(maxResultsStr))
				try {
					maxResults = Integer.parseInt(maxResultsStr);
				} catch (Exception e) {
				}
			if (getIsTest()) {
				logger.debug("firstResult: " + firstResult);
				logger.debug("maxResults: " + maxResults);
			}

			List<?> list = cloudDao.queryTable(
					sf(),
					clazz,
					new QueryGroup(AND, andQueryRuleList.toArray(new QueryRule[0]), andQueryGroupsList
							.toArray(new QueryGroup[0])), queryOrderList.toArray(new QueryOrder[0]), firstResult,
					maxResults);

			if (getIsTest()) {
				for (Object obj : list) {
					logger.debug("obj:" + ReflectionToStringBuilder.toString(obj));
					logger.debug("obj.getClass():" + obj.getClass());
				}
				logger.debug("size:" + list.size());
			}
			JSONArray jsonArray = new JSONArray(formatListToMap(list));
			resultString = jsonArray.toString();
			// logger.debug("resultString:" + resultString);
			// resultList = list;
			// resultList = formatListToMap(list);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		logger.debug("end");
		return JSON_RESULT;
	}

	/**
	 * @see CloudDAO#findProperty(Object ,Class, QueryGroup,
	 *      QueryOrder[],boolean, String...)
	 */
	public String findProperty() {
		try {
			String className = request.getParameter("class");
			String andQueryRulesStr = request.getParameter("andQueryRules");
			String andQueryGroupsStr = request.getParameter("andQueryGroups");
			String orderStr = request.getParameter("orderBy");
			String propertyNamesStr = request.getParameter("property");
			String isDistinctStr = request.getParameter("isDistinct");

			if (StringUtils.isBlank(andQueryRulesStr))
				andQueryRulesStr = "[]";
			if (StringUtils.isBlank(andQueryGroupsStr))
				andQueryGroupsStr = "[]";
			if (StringUtils.isBlank(orderStr))
				orderStr = "[]";

			Class<?> clazz = Class.forName(Util.beanPackage + "." + className);
			JSONArray andQueryRulesArr = new JSONArray(andQueryRulesStr);
			List<QueryRule> andQueryRuleList = QueryGroupUtil.jqgridRulesJSONArrayToQueryRuleList(clazz,
					andQueryRulesArr);

			JSONArray andQueryGroupsArr = new JSONArray(andQueryGroupsStr);
			List<QueryGroup> andQueryGroupsList = new ArrayList<QueryGroup>();
			for (int i = 0; i < andQueryGroupsArr.length(); i++) {
				JSONObject group = andQueryGroupsArr.getJSONObject(i);
				andQueryGroupsList.add(QueryGroupUtil.jqgridGroupJSONObjectToQueryGroup(clazz, group));
			}

			JSONArray orderByArr = new JSONArray(orderStr);
			List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
			for (int i = 0; i < orderByArr.length(); i++) {
				JSONObject orderByObj = orderByArr.getJSONObject(i);
				String field = orderByObj.getString("field");
				String op = orderByObj.getString("op");

				queryOrderList.add(new QueryOrder(field, op));
			}

			boolean isDistinct = Boolean.parseBoolean(isDistinctStr);
			String[] propertyArr = propertyNamesStr.split(",");
			List<?> list = cloudDao.findProperty(
					sf(),
					clazz,
					new QueryGroup(AND, andQueryRuleList.toArray(new QueryRule[0]), andQueryGroupsList
							.toArray(new QueryGroup[0])), queryOrderList.toArray(new QueryOrder[0]), isDistinct,
					propertyArr);

			JSONArray jsonArray = new JSONArray(list);
			resultString = jsonArray.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return JSON_RESULT;
	}

	// ---------- ---------- ---------- ---------- ----------
	public String findDisplayFormat() {
		String displayFormat = DEFAULT_DISPLAY_FORMAT;
		String className = request.getParameter("class");
		try {
			displayFormat = tableToDisplay.getString(className);
		} catch (MissingResourceException e1) {
		}
		resultString = displayFormat;
		return JSON_RESULT;
	}

	public String findBillDisplayFormat() {
		String displayFormat = DEFAULT_BILL_DISPLAY_FORMAT;
		String className = request.getParameter("class");
		try {
			displayFormat = tableToBillno.getString(className);
		} catch (MissingResourceException e1) {
		}
		resultString = displayFormat;
		return JSON_RESULT;
	}

	public String parseToSysidByFormat() {
		resultString = "";
		try {
			String displayCheck = request.getParameter("displayFormat");
			String strCheck = request.getParameter("displayValue");
			String beanClassName = request.getParameter("beanClassName");
			Class<?> targetClass = Class.forName(Util.beanPackage + "." + beanClassName);
			Map<String, String> dataMap = Util.parseToMapByFormat(displayCheck, strCheck);

			List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
			for (String key : dataMap.keySet()) {
				queryRuleList.add(new QueryRule(key, dataMap.get(key)));
			}

			List<String> pkList = (List<String>) cloudDao.findProperty(sf(), targetClass,
					new QueryGroup(queryRuleList.toArray(new QueryRule[0])), null, false, PK);
			if (pkList.size() > 0)
				resultString = pkList.get(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return JSON_RESULT;
	}

	public String parseToFormatBySysid() {
		resultString = "";
		try {
			// String displayCheck = request.getParameter("displayFormat");
			String strCheck = request.getParameter("hiddenValue");
			String beanClassName = request.getParameter("beanClassName");
			Class<?> targetClass = Class.forName(Util.beanPackage + "." + beanClassName);
			resultString = createDataDisplay(targetClass).get(strCheck);
			// Map<String, String> dataMap =
			// Util.parseToMapByFormat(displayCheck, strCheck);
			//
			// List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
			// for (String key : dataMap.keySet()) {
			// queryRuleList.add(new QueryRule(key, dataMap.get(key)));
			// }
			//
			// List<String> pkList = (List<String>) cloudDao.findProperty(sf(),
			// targetClass,
			// new QueryGroup(queryRuleList.toArray(new QueryRule[0])), null,
			// false, PK);
			// if (pkList.size() > 0)
			// resultString = pkList.get(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return JSON_RESULT;
	}
}