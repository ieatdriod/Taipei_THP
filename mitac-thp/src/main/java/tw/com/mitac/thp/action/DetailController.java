package tw.com.mitac.thp.action;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.jqgrid.bean.SysColumnConfig;
import tw.com.mitac.ssh.util.BeanComparator;
import tw.com.mitac.ssh.util.BigDecimalTypeConverter;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.ssh.util.QueryGroupUtil;
import tw.com.mitac.ssh.util.TimeTypeConverter;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class DetailController<MO> extends BasisCrudAction<MO> {
	private static final long serialVersionUID = 1L;

	protected static final String JQGRID_SIDX = "_jqgrid_sidx";
	protected static final String JQGRID_SORD = "_jqgrid_sord";
	protected static final String JQGRID_ROWS = "_jqgrid_rows";
	protected static final String JQGRID_PAGE = "_jqgrid_page";
	protected static final String JQGRID_SEARCH = "_jqgrid_search";
	protected static final String JQGRID_FILTERS = "_jqgrid_filters";

	protected final boolean isMatch(Object object, QueryGroup queryGroup) {
		boolean isMatch = true;
		try {
			QueryRule[] rules = queryGroup.getRules() != null ? queryGroup.getRules() : new QueryRule[0];
			QueryGroup[] groups = queryGroup.getGroups() != null ? queryGroup.getGroups() : new QueryGroup[0];
			if (rules.length + groups.length <= 0)
				return isMatch;
			if (OR.equalsIgnoreCase(queryGroup.getGroupOp())) {
				isMatch = false;
				for (QueryRule queryRule : rules) {
					Object value = PropertyUtils.getProperty(object, queryRule.getFieldName());
					Object data = queryRule.getData();
					if (value == null)
						continue;

					int comparableResult = (!Collection.class.isInstance(data) && value instanceof Comparable) ? ((Comparable) value)
							.compareTo(data) : -2;

					if (EQ.equals(queryRule.getOp()) && (value.equals(data) || comparableResult == 0))
						logger.debug("value " + queryRule.getOp() + " data");
					else if (NE.equals(queryRule.getOp()) && !value.equals(data) && comparableResult != 0)
						;
					else if (LT.equals(queryRule.getOp()) && comparableResult < 0)
						;
					else if (LE.equals(queryRule.getOp()) && comparableResult <= 0)
						;
					else if (GT.equals(queryRule.getOp()) && comparableResult > 0)
						;
					else if (GE.equals(queryRule.getOp()) && comparableResult >= 0)
						;
					// else if (IN.equals(queryRule.getOp()) && ((Collection)
					// data).contains(value));
					else if (IN.equals(queryRule.getOp()) && ((Collection) data).contains(value)) {
						if (data instanceof Collection && ((Collection) data).contains(value))
							;
						else if (data instanceof String
								&& (StringUtils.isBlank((String) data) || ((String) data).contains((String) value)))
							;
						else
							continue;
					} else if (BW.equals(queryRule.getOp()) && ((String) value).startsWith((String) data))
						;
					else if (BN.equals(queryRule.getOp()) && !((String) value).startsWith((String) data))
						;
					else if (CN.equals(queryRule.getOp()) && ((String) value).contains((String) data))
						;
					else if (NC.equals(queryRule.getOp()) && !((String) value).contains((String) data))
						;
					else
						continue;
					isMatch = true;
					break;
				}
				if (!isMatch)
					for (QueryGroup group : groups)
						if (isMatch(object, group)) {
							isMatch = true;
							break;
						}
			} else {
				for (QueryRule queryRule : rules) {
					// logger.debug("fieldName:" + queryRule.getFieldName() +
					// " data:" + queryRule.getData() + " data class:" +
					// (queryRule.getData() == null ? "NULL" :
					// queryRule.getData().getClass()));
					Object value = PropertyUtils.getProperty(object, queryRule.getFieldName());
					Object data = queryRule.getData();
					if (value == null) {
						isMatch = false;
						break;
					}

					int comparableResult = (!Collection.class.isInstance(data) && value instanceof Comparable) ? ((Comparable) value)
							.compareTo(data) : -2;

					if (EQ.equals(queryRule.getOp()) && (value.equals(data) || comparableResult == 0))
						logger.debug("value " + queryRule.getOp() + " data");
					else if (NE.equals(queryRule.getOp()) && !value.equals(data) && comparableResult != 0)
						;
					else if (LT.equals(queryRule.getOp()) && comparableResult < 0)
						;
					else if (LE.equals(queryRule.getOp()) && comparableResult <= 0)
						;
					else if (GT.equals(queryRule.getOp()) && comparableResult > 0)
						;
					else if (GE.equals(queryRule.getOp()) && comparableResult >= 0)
						;
					// else if (IN.equals(queryRule.getOp()) && ((Collection)
					// data).contains(value));
					else if (IN.equals(queryRule.getOp())) {
						logger.debug("IN");
						if (data instanceof Collection && ((Collection) data).contains(value))
							logger.debug("Collection");
						else if (data instanceof String
								&& (StringUtils.isBlank((String) data) || ((String) data).contains((String) value)))
							logger.debug("String");
						else {
							isMatch = false;
							break;
						}
					} else if (BW.equals(queryRule.getOp()) && ((String) value).startsWith((String) data))
						;
					else if (BN.equals(queryRule.getOp()) && !((String) value).startsWith((String) data))
						;
					else if (CN.equals(queryRule.getOp()) && ((String) value).contains((String) data))
						;
					else if (NC.equals(queryRule.getOp()) && !((String) value).contains((String) data))
						;
					else {
						isMatch = false;
						break;
					}
				}
				if (isMatch)
					for (QueryGroup group : groups)
						if (!isMatch(object, group)) {
							isMatch = false;
							break;
						}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return isMatch;
	}

	protected Set searchByFilters(Set<?> oldSet) {
		logger.debug("start where oldSet size:" + (oldSet == null ? "NULL" : oldSet.size()));
		Set targetSet = new HashSet();
		try {
			if (oldSet == null || oldSet.isEmpty()) {
				targetSet = oldSet;
			} else {
				Class<?> clazz = null;
				for (Object object : oldSet) {
					clazz = object.getClass();
					break;
				}

				logger.debug("filters:" + filters);
				JSONObject group = new JSONObject(filters);
				QueryGroup queryGroup = QueryGroupUtil.jqgridGroupJSONObjectToQueryGroup(clazz, group);
				queryGroup = resetInfoQuery(queryGroup);
				for (Object object : oldSet)
					if (isMatch(object, queryGroup))
						targetSet.add(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			targetSet = oldSet;
		}

		logger.debug("end where targetSet size:" + targetSet.size());
		return targetSet;
	}

	/**
	 * [jqgrid] 共用邏輯：取得排序過的資料
	 * 
	 * @param targetSet
	 * @return
	 */
	protected final TreeSet jqgridDetailTreeSet(Set targetSet) {
		if (search)
			if (StringUtils.isNotBlank(filters))
				targetSet = searchByFilters(targetSet);
		Comparator comparator = new BeanComparator(new QueryOrder[] { new QueryOrder(sidx, sord),
				new QueryOrder(SN, sord), new QueryOrder(ID, sord), new QueryOrder(PK) });

		TreeSet treeSet = new TreeSet(comparator);
		if (targetSet != null)
			treeSet.addAll(targetSet);
		return treeSet;
	}

	protected final Set jqgridDetailSelectedSet(String resourceName, Class clazz) {
		return jqgridDetailSelectedSet(null, resourceName, clazz);
	}

	/**
	 * [jqgrid] 共用邏輯：取得被選中的資料
	 * 
	 * @param ids
	 * @param resourceName
	 * @param clazz
	 * @return
	 */
	protected final Set jqgridDetailSelectedSet(String ids, String resourceName, Class clazz) {
		Set selectedSet = new LinkedHashSet();
		if (StringUtils.isBlank(ids))
			ids = id;
		try {
			Set oldSet = findDetailSetWhenEdit(resourceName);
			Object detail = ConstructorUtils.invokeConstructor(clazz, null);
			String[] idArr = ids.split(",");
			sidx = (String) sessionGet(resourceName + JQGRID_SIDX);
			sord = (String) sessionGet(resourceName + JQGRID_SORD);
			rows = (Integer) sessionGet(resourceName + JQGRID_ROWS);
			page = (Integer) sessionGet(resourceName + JQGRID_PAGE);
			search = (Boolean) sessionGet(resourceName + JQGRID_SEARCH);
			filters = (String) sessionGet(resourceName + JQGRID_FILTERS);
			Set treeSet = jqgridDetailTreeSet(oldSet);
			List<Integer> idList = new ArrayList<Integer>();
			for (String string : idArr) {
				Integer realID = (Integer.parseInt(string) - 1) + (rows * (page - 1));
				idList.add(realID);
			}

			int i = 0;
			for (Object oldObj : treeSet) {
				if (idList.contains(i)) {
					selectedSet.add(oldObj);
				}
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return selectedSet;
	}

	/**
	 * [jqgrid] load url
	 * 
	 * @return
	 */
	public String jqgridDetailResource() {
		String detailKey = StringUtils.defaultString(request.getParameter("detailKey"));
		return jqgridDetailList(getDetailInfoMap().get(detailKey).getDetailResource());
	}

	/**
	 * [jqgrid] 共用邏輯：load url XXX 尾尾檔也會用喔～～
	 */
	protected final String jqgridDetailList(String resourceName) {
		if (isShowFlow)
			logger.debug("start");
		logger.info("oper:" + oper);
		logger.debug("sidx:" + sidx + " sord:" + sord);

		Set targetSet = null;
		try {
			targetSet = findDetailSetWhenEdit(resourceName);
			// logger.debug("targetSet size:" + (targetSet == null ? "NULL" :
			// targetSet.size()));
		} catch (Exception e) {
			logger.debug("jqgridDetailList Exception: " + e);
		}
		targetSet = jqgridDetailTreeSet(targetSet);
		// logger.debug("targetSet size:" + (targetSet == null ? "NULL" :
		// targetSet.size()));

		List targetList = new ArrayList();
		targetList.addAll(targetSet);
		sessionSet(resourceName + JQGRID_SIDX, sidx);
		sessionSet(resourceName + JQGRID_SORD, sord);
		sessionSet(resourceName + JQGRID_ROWS, rows);
		sessionSet(resourceName + JQGRID_PAGE, page);
		sessionSet(resourceName + JQGRID_SEARCH, search);
		sessionSet(resourceName + JQGRID_FILTERS, filters);
		List results = Collections.emptyList();
		int from = rows * (page - 1);
		int length = rows;
		record = targetSet.size();
		logger.debug("rows:" + rows + " page:" + page + " record:" + record);
		try {
			results = new ArrayList(targetList.subList(from, from + length));
		} catch (IndexOutOfBoundsException e) {
			results = new ArrayList(targetList.subList(from, targetList.size()));
		}
		// ---------- ---------- ---------- ---------- ----------
		List<Map> formatToMapResults = formatListToMap(results);
		// ---------- ---------- ---------- ---------- ----------
		total = (int) Math.ceil((double) record / (double) rows);

		// this.setGridModel(formatToMapResults);
		resultMap = new HashMap();
		resultMap.put("gridModel", formatToMapResults);

		resultMap.put("page", page);
		resultMap.put("record", record);
		resultMap.put("rows", rows);
		resultMap.put("total", total);

		if (isShowFlow)
			logger.debug("end");
		return JSON_RESULT;
	}

	/**
	 * [jqgrid] 共用邏輯：新增或編輯 XXX 尾尾檔也會用喔～～
	 * 
	 * @param resourceName
	 * @param clazz
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	protected Object jqgridDetailEditOrAdd(String resourceName, Class<?> clazz) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, InstantiationException {
		Set dataSet = findDetailSetWhenEdit(resourceName);
		logger.debug("start " + resourceName + ".size:" + dataSet.size());
		// Object detail = ConstructorUtils.invokeConstructor(clazz, null);
		Object detail = null;
		if ("edit".equals(oper)) { // 編輯
			Object newPk = request.getParameter(PK);
			logger.debug("newPk:" + newPk);
			for (Object oldObj : dataSet) {
				Object oldPk = PropertyUtils.getProperty(oldObj, PK);
				if (oldPk.equals(newPk)) {
					detail = oldObj;
					break;
				}
			}
			if (detail == null) {// dbclick to edit，新增時oper為edit，不會產sysid
				detail = getDefaultDMO(clazz);
				Util.defaultPK(detail);
				dataSet.add(detail);
				logger.info("PK - " + PropertyUtils.getProperty(detail, PK));
			}
		} else if ("add".equals(oper)) { // 新增
			detail = getDefaultDMO(clazz);
			Util.defaultPK(detail);
			dataSet.add(detail);
		} else {
			logger.error("oper=" + oper + " (Correct value : 'edit' or 'add' or 'del')");
			return null;// error
		}
		if (resourceName.contains("detailSet"))
			PropertyUtils.setProperty(detail, FK, PropertyUtils.getProperty(bean, PK));
		// TODO id重複判斷
		defaultValue(detail);

		Field[] f = clazz.getDeclaredFields();
		for (Field field : f) {
			if (Modifier.isStatic(field.getModifiers()))
				continue;
			String str = request.getParameter(field.getName());
			// logger.debug("測試頁面OPER收進來的參數: " + "fieldName => " +
			// field.getName() + " || str=> " + str);
			// if (StringUtils.isNotBlank(str))
			if (str != null) {// 因為頁面有可能故意把原本有值的東西改成空字串丟回來，所以必須要是只判斷null(也就是當這個欄位是hidden時)
				str = str.trim();
				if (field.getType().equals(Boolean.class)) {
					PropertyUtils.setProperty(detail, field.getName(), new Boolean(str));
				} else if (field.getType().equals(BigDecimal.class)) {
					PropertyUtils.setProperty(detail, field.getName(), BigDecimalTypeConverter.convertFromString(str));
					// 顯示%
					if (field.getName().lastIndexOf("Rate") != -1) {
						PropertyUtils.setProperty(
								detail,
								field.getName(),
								BigDecimalTypeConverter.convertFromString(str).divide(new BigDecimal(100), 8,
										BigDecimal.ROUND_HALF_UP));
					}

				} else if (field.getType().equals(Long.class)) {
					PropertyUtils.setProperty(detail, field.getName(), new Long(str));
				} else if (field.getType().equals(Integer.class)) {
					PropertyUtils.setProperty(detail, field.getName(), new Integer(str));
				} else if (field.getType().equals(Date.class)) {
					logger.debug("dateStr:" + str);
					PropertyUtils.setProperty(detail, field.getName(), DateTypeConverter.convertFromString(str));
				} else if (field.getType().equals(Time.class)) {
					logger.debug("timeStr:" + str);
					PropertyUtils.setProperty(detail, field.getName(), TimeTypeConverter.convertFromString(str));
				} else if (field.getType().equals(String.class)) {
					if (field.getName().equals(PK)) {
						if (StringUtils.isNotBlank(str))
							// TODO 如果是判斷null，那在add時sysid也會以空字串丟回來
							PropertyUtils.setProperty(detail, field.getName(), str);
					} else {
						PropertyUtils.setProperty(detail, field.getName(), str);
					}
				}
			}
			if (field.getName().equals(BILL_STATUS))
				PropertyUtils.setProperty(detail, field.getName(), BillStatusUtil.NOTENOUGH);
		}
		// SN或ID自動產
		if (PropertyUtils.getPropertyDescriptor(detail, SN) != null) {
			snGenerator(dataSet, detail, SN);
		}
		if (PropertyUtils.getPropertyDescriptor(detail, ID) != null) {
			snGenerator(dataSet, detail, ID);
		}
		logger.debug("end   dataSet:" + dataSet.size());
		return detail;
	}

	protected void jqgridDetailDel(String resourceName, Class clazz) {
		Set oldSet = findDetailSetWhenEdit(resourceName);
		Set delSet = jqgridDetailSelectedSet(resourceName, clazz);
		oldSet.removeAll(delSet);
	}

	/**
	 * [jqgrid] editurl
	 * 
	 * @return
	 */
	public String jqgridDetailExecute() {
		String detailKey = request.getParameter("detailKey");
		DetailInfo detailInfo = getDetailInfoMap().get(detailKey);
		return jqgridDetailOper(detailInfo.getDetailResource(), detailInfo.getDetailClass());
	}

	/**
	 * [jqgrid] 共用邏輯：editurl edit/add/delete
	 * 
	 * @param resourceName
	 * @param clazz
	 * @return
	 */
	protected final String jqgridDetailOper(String resourceName, Class<?> clazz) {
		resultString = SUCCESS;
		logger.info("oper:" + oper + " id:" + id);
		try {
			if ("del".equals(oper)) {
				jqgridDetailDel(resourceName, clazz);
			} else {
				jqgridDetailEditOrAdd(resourceName, clazz);
			}
		} catch (Exception e) {
			// result = ERROR;
			resultString = "儲存失敗";
			e.printStackTrace();
		}
		return JSON_RESULT;
	}

	protected Map<String, Object> getDetailColModelMap(Class<?> clazz, Field field, String columnId, String columnname,
			SysColumnConfig sysColumnConfig, SysColumnConfig systemSysColumnConfig, Object defaultObj) {
		Map<String, Object> colModelMap = new HashMap<String, Object>();
		colModelMap.put("name", columnId);
		colModelMap.put("index", columnId);
		colModelMap.put("align", getJqgridTextAlign());
		colModelMap.put("sortable", true);// 已提供TreeSet排序
		colModelMap.put("editable", systemSysColumnConfig.getIseditable());
		colModelMap.put("label", columnname);
		colModelMap.put("width", sysColumnConfig.getWidth().intValue());

		Map<String, Object> editoptionsMap = new HashMap<String, Object>();
		colModelMap.put("editoptions", editoptionsMap);
		editoptionsMap.put("dataEvents", new Object[0]);
		Map<String, Object> editrulesMap = new HashMap<String, Object>();
		colModelMap.put("editrules", editrulesMap);
		Map<String, Object> searchoptionsMap = new HashMap<String, Object>();
		colModelMap.put("searchoptions", searchoptionsMap);

		try {
			// 欄位內容預設值
			Object defaultValue = PropertyUtils.getProperty(defaultObj, field.getName());
			if (defaultValue != null) {
				editoptionsMap.put("defaultValue", defaultValue);
				if (defaultValue instanceof Date)
					editoptionsMap.put("defaultValue", DateTypeConverter.convertToString((Date) defaultValue));
				else if (defaultValue instanceof Time)
					editoptionsMap.put("defaultValue", TimeTypeConverter.convertToString((Time) defaultValue));
				else if (defaultValue instanceof Boolean)
					editoptionsMap.put("defaultValue", defaultValue.toString());
				else if (defaultValue instanceof BigDecimal)
					editoptionsMap.put("defaultValue",
							BigDecimalTypeConverter.convertToString((BigDecimal) defaultValue));
				else if (defaultValue instanceof Long || defaultValue instanceof Integer)
					editoptionsMap.put("defaultValue", defaultValue.toString());
			}
		} catch (Exception e1) {
		}

		/**
		 * 告知宣告型別可自動產生日曆模組 非jqgrid
		 */
		colModelMap.put("javaClass", field.getType().getSimpleName());

		if (field.getType().equals(BigDecimal.class)) {
			editrulesMap.put("number", true);
			searchoptionsMap.put("sopt", new String[] { EQ, NE, LT, LE, GT, GE });
			colModelMap.put("align", getJqgridNumberAlign());
		} else if (field.getType().equals(Long.class) || field.getType().equals(Integer.class)) {
			editrulesMap.put("integer", true);
			searchoptionsMap.put("sopt", new String[] { EQ, NE, LT, LE, GT, GE });
			colModelMap.put("align", getJqgridNumberAlign());
		} else if (field.getType().equals(Date.class) || field.getType().equals(Time.class)) {
			searchoptionsMap.put("sopt", new String[] { EQ, NE, LT, LE, GT, GE });
		} else if (field.getType().equals(Boolean.class)) {
			editoptionsMap.put("value", getSystemBooleanMap());
			searchoptionsMap.put("sopt", new String[] { EQ });
		} else if (field.getType().equals(String.class)) {
			if (getDataSysConstantIdMap(clazz).get(field.getName()) != null) {
				editoptionsMap.put("value", getConstantMenu(clazz).get(field.getName()));
			}
			searchoptionsMap.put("sopt", new String[] { BW, BN, CN, NC, EQ, NE, LT, LE, GT, GE });
		}

		// jqgrid 顯示%
		if (field.getType().equals(BigDecimal.class) && (field.getName().lastIndexOf("Rate") != -1)) {
			colModelMap.put("formatter", "currencyFmatterPercent");
			colModelMap.put("align", "right");
		}

		String className = "";
		try {
			className = coreSysidMapping.getString(field.getName());
		} catch (MissingResourceException e) {
		}
		String billName = "";
		try {
			billName = (String) billSysidMapping.getObject(field.getName());
		} catch (MissingResourceException e) {
		}

		if (StringUtils.isBlank(className) && StringUtils.isBlank(billName)) {
			if (field.getName().contains("sysid") && field.getName().indexOf("sysid") > 0
					&& field.getName().indexOf("sysid") + 5 == field.getName().length()) {
				logger.debug("取得不存在properties中的field： " + field.getName());
			}
		}

		if (StringUtils.isNotBlank(className)) {
			try {
				Class<?> targetClass = Class.forName(Util.beanPackage + "." + className);

				Map<String, String> editoptionsValueMap = createDataDisplay(targetClass);
				editoptionsMap.put("value", editoptionsValueMap);

				// 如果查出來的TABLE是沒有資料的，頁面會顯示object
				colModelMap.put("selectTool", className);
			} catch (ClassNotFoundException e) {
			}
		}
		if (StringUtils.isNotBlank(billName)) {
			try {
				Class<?> targetClass = Class.forName(Util.beanPackage + "." + billName);

				// Map<String, String> editoptionsValueMap =
				// createDataMenu(targetClass);
				// editoptionsMap.put("value", editoptionsValueMap);

				// 如果查出來的TABLE是沒有資料的，頁面會顯示object
				colModelMap.put("billTool", billName);
			} catch (ClassNotFoundException e) {
			}
		}
		boolean isColumnInfoStar = isColumnInfoStar(clazz.getSimpleName(), field.getName());
		/**
		 * 定義開窗模組 非jqgrid
		 */
		colModelMap.put("isColumnInfoStar", isColumnInfoStar);

		boolean ishidden = systemSysColumnConfig.getIshidden();
		if (!ishidden && new Boolean(Util.globalSetting().getString("crud.isDataLogHide"))
				&& Arrays.asList(DATA_LOG_MEMBER).contains(field.getName()))
			ishidden = true;
		if (ishidden) {
			if (!getIsTest()) {
				colModelMap.put("hidden", true);
				colModelMap.put("hidedlg", true);//
			}
			editoptionsMap.put("readonly", "readonly");
			colModelMap.put("search", false);
		} else if (sysColumnConfig.getIshidden()) {
			colModelMap.put("hidden", true);
			editoptionsMap.put("readonly", "readonly");
		}

		try {
			Method m = clazz.getMethod("get" + field.getName().substring(0, 1).toUpperCase()
					+ field.getName().substring(1));
			if (m.isAnnotationPresent(Column.class)) {
				Column column = m.getAnnotation(Column.class);
				int length = column.length();
				colModelMap.put("columnLength", length);

				if (field.getType().equals(String.class) && StringUtils.isBlank(className)
						&& StringUtils.isBlank(billName)) {
					editoptionsMap.put("maxlength", length);
				}

				if (!ishidden)
					if (!column.nullable())
						editrulesMap.put("required", true);
			}
		} catch (Exception e) {
		}

		if (SN.equals(field.getName())) {
			if (!getIsShowSequence()) {
				if (!getIsTest()) {
					colModelMap.put("hidden", true);
					colModelMap.put("hidedlg", true);
				}
				editoptionsMap.put("readonly", "readonly");
				colModelMap.put("search", false);
				editrulesMap.put("required", false);
			}
		}

		/*
		 * frozen 與 hidden 不可同時成立 hidden 成立時 frozen 必不成立
		 */
		if (sysColumnConfig.getIsfrozen() // || sysColumnConfig.getIshidden()
		)
			colModelMap.put("frozen", true);

		if (PK.equals(field.getName())) {
			colModelMap.put("editable", true);
		}
		if (BILL_STATUS.equals(field.getName()) || "turntowostatus".equals(field.getName())) {
			colModelMap.put("editable", false);
			editoptionsMap.put("value", getWfStatusMap());
		} else if ("itemName".equals(field.getName()) || "specification".equals(field.getName())) {
			colModelMap.put("editable", true);
			editoptionsMap.put("readonly", "readonly");
		}
		// ---------- ---------- ---------- ---------- ----------

		if (editrulesMap.get("required") != null
				&& String.valueOf(true).equals(String.valueOf(editrulesMap.get("required")))) {
			colModelMap.put("label", "*" + colModelMap.get("label"));
			colModelMap.put("hidedlg", true);
		}

		if (colModelMap.get("width") == null)
			colModelMap.put("width", dafaultWidthByLength * ((String) colModelMap.get("label")).length());

		boolean isColumnTextarea = isColumnTextarea(clazz.getSimpleName(), field.getName());
		if (StringUtils.equals(REMARK, field.getName()) || isColumnTextarea) {
			colModelMap.put("edittype", "textarea");
			editoptionsMap.put("rows", "2");
			editoptionsMap.put("cols", "16");
		}

		// XXX 是否統一設置於createDataMenu
		Map<String, String> menu = (Map<String, String>) editoptionsMap.get("value");
		if (menu != null && menu.size() > 0) {
			colModelMap.put("edittype", "select");
			colModelMap.put("formatter", "select");

			Map<String, String> newMenu = new LinkedHashMap<String, String>();
			// 取樣方式,取樣標準,檢驗水準,允收水準,下拉選單不放"..."
			// if ((!"samplepattern".equals(field.getName())) &&
			// (!"samplestandard".equals(field.getName()))
			// && (!"inspectionstandard".equals(field.getName())) &&
			// (!"acceptstandard".equals(field.getName()))
			// && !Util.TAXCHARGETYPE.equals(field.getName()) &&
			// !Util.ISTAXINCLUDE.equals(field.getName()))
			newMenu.put("", "...");

			// ----- info star -----
			if (isColumnInfoStar) {
				editoptionsMap.put("defaultValue", "*");
				newMenu.put("*", "*");
				newMenu.remove("");
			}

			newMenu.putAll(menu);
			editoptionsMap.put("value", newMenu);

			className = (String) colModelMap.get("selectTool");
			if (StringUtils.isBlank(className)
			// || CoreEntity.class.getSimpleName().equals(className)
			// || CoreSite.class.getSimpleName().equals(className)
			// || CoreDepartment.class.getSimpleName().equals(className)
			// || CoreWarehouse.class.getSimpleName().equals(className)
			) {
				colModelMap.put("stype", "select");

				if (searchoptionsMap.get("value") != null)
					menu = (Map<String, String>) searchoptionsMap.get("value");
				Map<String, String> newMenu2 = new LinkedHashMap<String, String>();
				newMenu2.put("", "...");// 查詢需有預設值，否則會查不到資料
				if (isColumnInfoStar)
					newMenu2.put("*", "*");
				if (BILL_STATUS.equals(field.getName())) {
					// newMenu2.putAll(getStatusInMenu());
					// searchoptionsMap.put("sopt", new String[] { IN });
					List<String> billStatusList = (List<String>) cloudDao.findProperty(sf(), clazz, null,
							new QueryOrder[] { new QueryOrder(BILL_STATUS) }, true, BILL_STATUS);
					for (String string : billStatusList)
						newMenu2.put(string, menu.get(string));
					searchoptionsMap.put("sopt", new String[] { EQ, NE });
				} else {
					newMenu2.putAll(menu);
					searchoptionsMap.put("sopt", new String[] { EQ, NE });
				}

				searchoptionsMap.put("value", newMenu2);
			} else {
				searchoptionsMap.put("sopt", new String[] { CN, BW, EW, EQ });
				// 所有否定型語法皆有問題
				// searchoptionsMap.put("sopt", new String[] { BW, BN, CN, NC,
				// EQ, NE });
			}
		}
		return colModelMap;
	}

	/**
	 * [jqgrid]
	 */
	public Map<String, Map> getJqgridDetailColModelInfoMap() {
		Map<String, Map> jqgridDetailColModelInfoMap = new LinkedHashMap<String, Map>();
		for (String detailKey : getDetailInfoMap().keySet()) {
			DetailInfo detailInfo = getDetailInfoMap().get(detailKey);
			jqgridDetailColModelInfoMap.put(detailKey,
					jqgridDetailColModelMap(detailInfo.getDetailClass(), detailInfo.getDetailI18nKey()));
		}
		return jqgridDetailColModelInfoMap;
	}

	/**
	 * [jqgrid] colModel
	 * 
	 * @return
	 */
	public String getJqgridDetailColModelInfo() {
		JSONObject jsonObject = new JSONObject(getJqgridDetailColModelInfoMap());
		String targetStr = jsonObject.toString();
		return targetStr;
	}

	/**
	 * [jqgrid] 共用邏輯：colModel
	 * 
	 * @param clazz
	 * @param i18nKey
	 * @return
	 */
	protected Map<String, Map> jqgridDetailColModelMap(Class<?> clazz, String i18nKey) {
		Map<String, Map> map = new LinkedHashMap<String, Map>();
		try {
			initSystemColumnConfigCreator(clazz, i18nKey);
			for (SysColumnConfig sysColumnConfig : getColumnConfigCreator().createColumnConfigMap(clazz, i18nKey)
					.values())
				map.put(sysColumnConfig.getColumnId(), null);

			Field[] f = clazz.getDeclaredFields();
			Object defaultObj = getDefaultDMO(clazz);
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (field.getType().equals(Set.class))
					continue;
				String columnId = field.getName();
				SysColumnConfig sysColumnConfig = getColumnConfigCreator().createColumnConfigMap(clazz, i18nKey).get(
						columnId);
				SysColumnConfig systemSysColumnConfig = createSystemColumnConfigMap(clazz, i18nKey).get(columnId);

				// String columnName = sysColumnConfig.getColumnName();
				String columnName = getText(field.getName());
				if (StringUtils.isNotBlank(i18nKey)) {
					columnName = getText(i18nKey + "." + field.getName());
					if (columnName.equals(i18nKey + "." + field.getName())) {
						columnName = getText("bean." + field.getName());
					}
				}

				Map<String, Object> colModelMap = getDetailColModelMap(clazz, field, columnId, columnName,
						sysColumnConfig, systemSysColumnConfig, defaultObj);
				map.put(columnId, colModelMap);
			}
		} catch (Exception e) {
			logger.error(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}
		return map;
	}

	// ------- subdetail -----
	protected String parentId;

	public final String getParentId() {
		return parentId;
	}

	public final void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * [jqgrid] 共用邏輯：尾尾檔 load url
	 * 
	 * //XXX 資源放置於session，一次只能focus在一筆資料的尾尾檔上，若展開多筆尾尾檔則會有問題
	 * 
	 * @param resourceName
	 * @param parentId
	 * @param detailSetName
	 * @return
	 */
	protected String childGridList(String resourceName, String parentId, String detailSetName) {
		if (StringUtils.isNotBlank(parentId)) {

			logger.debug("parentId:" + parentId);
			// request.setAttribute("grandParentStatus",
			// request.getParameter("status"));
			Set<?> querySet = findDetailSetWhenEdit(detailSetName);
			for (Object detailBean : querySet) {
				try {
					if (PropertyUtils.getProperty(detailBean, PK).equals(parentId)) {
						try {
							Set set = (Set) PropertyUtils.getProperty(detailBean, resourceName);
							if (set == null) {
								set = new LinkedHashSet();
								PropertyUtils.setProperty(detailBean, resourceName, set);
							}
							sessionSet(resourceName, set);
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
						logger.debug("child.session." + resourceName + ":" + findDetailSetWhenEdit(resourceName));
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return jqgridDetailList(resourceName);
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String detailColumnReset() {
		String detailKey = request.getParameter("detailKey");
		Class<?> clazz = getDetailInfoMap().get(detailKey).getDetailClass();
		columnReset(clazz);
		return EDIT;
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String ajaxDetailRemapHidden() {
		String detailKey = request.getParameter("detailKey");
		Class<?> clazz = getDetailInfoMap().get(detailKey).getDetailClass();
		columnReset(clazz);
		return ajaxRemapHidden(clazz);
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String ajaxDetailColumnResize() {
		String detailKey = request.getParameter("detailKey");
		Class<?> clazz = getDetailInfoMap().get(detailKey).getDetailClass();
		return ajaxColumnResize(clazz);
	}

	/**
	 * 尾檔排序：jqGrid完成（替代刪除事件
	 * 
	 * @return
	 */
	public String ajaxLoadComplete() {
		String msg = SUCCESS;
		try {
			String parentSysid = request.getParameter("parentSysid");
			String gridTableKey = request.getParameter("gridTableKey");
			if (StringUtils.isBlank(parentSysid) || gridTableKey == null) {
				resultString = "參數錯誤";
				return JSON_RESULT;
			}
			String detailSetKey = DETAIL_SET + gridTableKey;
			Class<?> clazz = getDetailInfoMap().get(gridTableKey).getDetailClass();

			int total = cloudDao.queryCount(sf(), clazz, new QueryGroup(new QueryRule(FK, parentSysid)));

			// remap order
			// Set<MO> detailSet = (Set<MO>) sessionGet(detailSetKey);
			Set<MO> detailSet = (Set<MO>) findDetailSetWhenEdit(detailSetKey);
			if (detailSet == null)
				detailSet = new LinkedHashSet<MO>();
			if (detailSet.size() != total) {
				List<MO> list = new ArrayList<MO>(detailSet);
				Collections.sort(list, new Comparator<MO>() {
					public int compare(MO ent1, MO ent2) {
						try {
							Integer d1 = (Integer) PropertyUtils.getProperty(ent1, DATA_ORDER);
							Integer d2 = (Integer) PropertyUtils.getProperty(ent2, DATA_ORDER);
							return (d1.compareTo(d2));
						} catch (Exception e) {
							e.printStackTrace();
						}
						return (0);
					}
				});
				Integer order = 1;
				for (MO detail : list) {
					PropertyUtils.setProperty(detail, DATA_ORDER, order);
					order++;
				}
				detailSet = new LinkedHashSet<MO>(list);
				sessionSet(detailSetKey, detailSet);

				MO newBean = (MO) cloudDao.get(sf(), getPersistentClass(), parentSysid);
				PropertyUtils.setProperty(newBean, detailSetKey, detailSet);
				List<Object> saveDetailList = new ArrayList<Object>();
				saveDetailList.add(newBean);
				msg = cloudDao.save(sf(), saveDetailList.toArray(), false, null);
				if (!SUCCESS.equals(msg)) {
					resultString = msg;
					return JSON_RESULT;
				} else {
					List<Object> deleteDetailList = new ArrayList<Object>();
					deleteDetailList.add(new DeleteStatement(clazz.getSimpleName(), new QueryGroup(new QueryRule(FK,
							NU, ""))));
					msg = cloudDao.save(sf(), deleteDetailList.toArray(), false, null);
					if (!SUCCESS.equals(msg)) {
						resultString = msg;
						return JSON_RESULT;
					} else {
						msg = "reloadGrid";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultString = "尾檔排序發生錯誤";
			return JSON_RESULT;
		}
		resultString = msg;
		return JSON_RESULT;
	}

	/**
	 * 尾檔排序：新增or更新
	 * 
	 * @return
	 */
	public String ajaxDetailSave() {
		String msg = SUCCESS;
		try {
			String sysid = request.getParameter("sysid");
			String newIds = request.getParameter("dataOrder");
			String parentSysid = request.getParameter("parentSysid");
			String gridTableKey = request.getParameter("gridTableKey");
			if (sysid == null || StringUtils.isBlank(newIds) || StringUtils.isBlank(parentSysid)
					|| gridTableKey == null) {
				resultString = "尾檔排序參數錯誤";
				return JSON_RESULT;
			}
			String detailSetKey = DETAIL_SET + gridTableKey;
			Class<?> clazz = getDetailInfoMap().get(gridTableKey).getDetailClass();
			int newOrder = Integer.parseInt(newIds);

			// Set<MO> detailSet = (Set<MO>) sessionGet(detailSetKey);
			Set<MO> detailSet = (Set<MO>) findDetailSetWhenEdit(detailSetKey);
			Boolean isNew = true;
			MO detailBean = (MO) getDefaultDMO(clazz);
			if (StringUtils.isNotBlank(sysid)) {
				// detailBean = (MO) cloudDao.get(sf(), clazz, sysid);
				for (MO detail : detailSet) {
					if (sysid.equals(PropertyUtils.getProperty(detail, PK))) {
						detailBean = detail;
						PropertyUtils.setProperty(detailBean, FK, parentSysid);
						break;
					}
				}
				isNew = false;
			} else {
				for (MO detail : detailSet) {
					if (PropertyUtils.getProperty(detail, FK) == null) {
						detailBean = detail;
						Util.defaultPK(detailBean);
						PropertyUtils.setProperty(detailBean, FK, parentSysid);
						PropertyUtils.setProperty(detailBean, DATA_ORDER, newOrder);
						break;
					}
				}
			}

			int total = cloudDao.queryCount(sf(), clazz, new QueryGroup(new QueryRule(FK, parentSysid)));
			if (isNew) {
				if (newOrder < (total + 1)) {
					// upper
					msg = shiftDetailOrder(clazz, parentSysid, newOrder, total, 1);
				} else if (newOrder > (total + 1)) {
					PropertyUtils.setProperty(detailBean, DATA_ORDER, total + 1);
				}
			} else {
				int oldOrder = (int) PropertyUtils.getProperty(detailBean, DATA_ORDER);
				PropertyUtils.setProperty(detailBean, DATA_ORDER, newOrder);
				if (newOrder < oldOrder) {
					// upper
					msg = shiftDetailOrder(clazz, parentSysid, newOrder, oldOrder - 1, 1);
				} else if (newOrder > oldOrder) {
					// downer
					msg = shiftDetailOrder(clazz, parentSysid, oldOrder + 1, newOrder, -1);
					if (newOrder > total) {
						PropertyUtils.setProperty(detailBean, DATA_ORDER, total);
					}
				}
			}

			defaultValue(detailBean);
			List<Object> saveDetailList = new ArrayList<Object>();
			saveDetailList.add(detailBean);
			msg = cloudDao.save(sf(), saveDetailList.toArray(), false, null);
			if (!SUCCESS.equals(msg)) {
				resultString = msg;
				return JSON_RESULT;
			}

			MO newBean = (MO) cloudDao.get(sf(), getPersistentClass(), parentSysid);
			sessionSet(detailSetKey, PropertyUtils.getProperty(newBean, detailSetKey));
		} catch (Exception e) {
			e.printStackTrace();
			resultString = "尾檔排序發生錯誤";
			return JSON_RESULT;
		}
		resultString = msg;
		return JSON_RESULT;
	}

	/**
	 * 尾檔排序：拖拉排序
	 * 
	 * @return
	 */
	public String ajaxDetailSoab() {
		String msg = SUCCESS;
		try {
			String sysid = request.getParameter("sysid");
			String newIds = request.getParameter("newIds");
			String parentSysid = request.getParameter("parentSysid");
			String gridTableKey = request.getParameter("gridTableKey");
			if (StringUtils.isBlank(sysid) || StringUtils.isBlank(newIds) || StringUtils.isBlank(parentSysid)
					|| gridTableKey == null) {
				resultString = "尾檔排序參數錯誤";
				return JSON_RESULT;
			}
			String detailSetKey = DETAIL_SET + gridTableKey;
			Class<?> clazz = getDetailInfoMap().get(gridTableKey).getDetailClass();
			int newOrder = Integer.parseInt(newIds);

			MO deatilbean = (MO) cloudDao.get(sf(), clazz, sysid);
			int oldOrder = (int) PropertyUtils.getProperty(deatilbean, DATA_ORDER);
			if (newOrder < oldOrder) {
				// upper
				msg = shiftDetailOrder(clazz, parentSysid, newOrder, oldOrder - 1, 1);
			} else if (newOrder > oldOrder) {
				// downer
				msg = shiftDetailOrder(clazz, parentSysid, oldOrder + 1, newOrder, -1);
			}
			if (!SUCCESS.equals(msg)) {
				resultString = msg;
				return JSON_RESULT;
			}

			PropertyUtils.setProperty(deatilbean, DATA_ORDER, newOrder);
			defaultValue(deatilbean);
			List<Object> saveDetailList = new ArrayList<Object>();
			saveDetailList.add(deatilbean);
			msg = cloudDao.save(sf(), saveDetailList.toArray(), false, "UPDATE");
			if (!SUCCESS.equals(msg)) {
				resultString = msg;
				return JSON_RESULT;
			}

			MO newBean = (MO) cloudDao.get(sf(), getPersistentClass(), parentSysid);
			sessionSet(detailSetKey, PropertyUtils.getProperty(newBean, detailSetKey));
		} catch (Exception e) {
			e.printStackTrace();
			resultString = "尾檔排序發生錯誤";
			return JSON_RESULT;
		}
		resultString = msg;
		return JSON_RESULT;
	}

	/**
	 * 尾檔排序HQL（以parentSysid為群組）
	 * 
	 * @param tableName
	 * @param parentSysid
	 * @param startIdx
	 * @param endIdx
	 * @param shiftNum
	 * @return
	 */
	protected String shiftDetailOrder(Class clazz, String parentSysid, int startIdx, int endIdx, int shiftNum) {
		try {
			// HQL
			Session session = sf().openSession();
			Transaction tx = session.beginTransaction();
			String tableName = clazz.getSimpleName();
			String columnName = DATA_ORDER;
			String qStr = "UPDATE " + tableName + " ";
			qStr += "SET " + columnName + " = " + columnName + " ";
			if (shiftNum >= 0)
				qStr += "+ ";
			qStr += shiftNum + " ";
			qStr += "WHERE " + columnName + " >= " + startIdx + " ";
			qStr += "AND " + columnName + " <= " + endIdx + " ";
			qStr += "AND " + FK + " = '" + parentSysid + "' ";
			Query query = session.createQuery(qStr);
			query.executeUpdate();
			tx.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "排序發生錯誤";
		}
		return SUCCESS;
	}

	/**
	 * 尾檔排序：重新排序
	 * 
	 * @param detailSetKey
	 * @return
	 */
	public String remapDataOrder(String detailSetKey) {
		Set<MO> detailSet = (Set<MO>) findDetailSetWhenEdit(detailSetKey);
		if (detailSet != null && detailSet.size() != total) {
			List<MO> list = new ArrayList<MO>(detailSet);
			Collections.sort(list, new Comparator<MO>() {
				public int compare(MO ent1, MO ent2) {
					try {
						Integer d1 = (Integer) PropertyUtils.getProperty(ent1, DATA_ORDER);
						Integer d2 = (Integer) PropertyUtils.getProperty(ent2, DATA_ORDER);
						if (d1.compareTo(d2) == 0) {
							String od1 = (String) PropertyUtils.getProperty(ent1, OD);
							String od2 = (String) PropertyUtils.getProperty(ent2, OD);
							return (od2.compareTo(od1));
						}
						return (d1.compareTo(d2));
					} catch (Exception e) {
						e.printStackTrace();
					}
					return (0);
				}
			});
			Integer order = 1;
			for (MO detail : list) {
				try {
					PropertyUtils.setProperty(detail, DATA_ORDER, order);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				order++;
			}
			detailSet = new LinkedHashSet<MO>(list);
			sessionSet(detailSetKey, detailSet);
		}
		return SUCCESS;
	}

	protected boolean bannerImgExecute(String PATH, DetailInfo detailInfo) {
		Set dataSet2 = findDetailSetWhenEdit(detailInfo.getDetailResource());
		File[] bannerImgForC = null;
		String[] bannerImgForCFileName = null;
		if (MultiPartRequestWrapper.class.isInstance(request)) {
			MultiPartRequestWrapper multipartRequest = (MultiPartRequestWrapper) request;
			String streamName = "bannerImgForC";
			bannerImgForCFileName = multipartRequest.getFileNames(streamName);
			bannerImgForC = multipartRequest.getFiles(streamName);
		}

		boolean checkTw = BooleanUtils.toBoolean(request.getParameter("checkTw"));
		boolean checkCn = BooleanUtils.toBoolean(request.getParameter("checkCn"));
		boolean checkUs = BooleanUtils.toBoolean(request.getParameter("checkUs"));

		String bannerUrlForC = request.getParameter("bannerUrlForC");
		try {
			String pk = (String) PropertyUtils.getProperty(bean, PK);
			if (bannerImgForC != null && bannerImgForC.length > 0) {
				String subMainFilePath = PATH + pk + File.separator;
				File dirFile = new File(subMainFilePath);
				// 如果我這個資料夾沒有就創建
				if (!dirFile.exists())
					dirFile.mkdirs();// create document
				if (bannerImgForCFileName != null)
					for (String fileName : bannerImgForCFileName)
						// 驗證圖片附檔名
						if (!FileUtil.validateExtention(resultFileExtention, fileName)) {
							addActionError(getText("errMsg.fileFormatWrong",
									new String[] { FileUtil.getExtention(fileName) }));
							return false;
						}

				for (int fileIndex = 0; fileIndex < bannerImgForCFileName.length; fileIndex++) {
					String finalFileName = bannerImgForCFileName[fileIndex];
					String saveFilePath = subMainFilePath + finalFileName;
					logger.debug("測試 儲存路徑:" + saveFilePath);
					File fileLocation = new File(saveFilePath);

					// 判斷檔案存不存在
					if (fileLocation.exists()) {
						// 如果存在的話應該使用不同名稱
						// 前面+日期
						finalFileName = reportName + finalFileName;
						saveFilePath = subMainFilePath + finalFileName;
						logger.debug("測試 儲存路徑:" + saveFilePath);
						fileLocation = new File(saveFilePath);
					}

					FileUtil.moveFile(bannerImgForC[fileIndex], fileLocation);

					Object f2 = getDefaultDMO(detailInfo.getDetailClass());
					PropertyUtils.setProperty(f2, FK, pk);
					PropertyUtils.setProperty(f2, "adsImage", finalFileName);
					PropertyUtils.setProperty(f2, "adsTw", checkTw);
					PropertyUtils.setProperty(f2, "adsCn", checkCn);
					PropertyUtils.setProperty(f2, "adsUs", checkUs);

					// if (orderKey <= 0)
					// f2.setDataOrder(1);
					// else
					// f2.setDataOrder(orderKey);
					PropertyUtils.setProperty(f2, DATA_ORDER, 1);

					PropertyUtils.setProperty(f2, "adsUrl", bannerUrlForC);
					PropertyUtils.setProperty(f2, "bannerType", "P");
					// 去掉附檔名
					String ext = FileUtil.getExtention(finalFileName);
					String linktxt = finalFileName.replace(ext, "");
					defaultValue(f2);
					tw.com.mitac.thp.util.Util.defaultPK(f2);
					dataSet2.add(f2);
				}
			} else if (StringUtils.isNotBlank(bannerUrlForC)) {
				Object f2 = getDefaultDMO(detailInfo.getDetailClass());
				PropertyUtils.setProperty(f2, FK, pk);
				PropertyUtils.setProperty(f2, "adsTw", checkTw);
				PropertyUtils.setProperty(f2, "adsCn", checkCn);
				PropertyUtils.setProperty(f2, "adsUs", checkUs);

				// if (orderKey <= 0)
				// f2.setDataOrder(1);
				// else
				// f2.setDataOrder(orderKey);
				PropertyUtils.setProperty(f2, DATA_ORDER, 1);

				PropertyUtils.setProperty(f2, "adsUrl", bannerUrlForC);
				PropertyUtils.setProperty(f2, "bannerType", "V");
				defaultValue(f2);
				tw.com.mitac.thp.util.Util.defaultPK(f2);
				dataSet2.add(f2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			addActionError(e.getClass() + ":" + e.getMessage());
			return false;
		}
		remapDataOrder(detailInfo.getDetailResource());
		return true;
	}

	protected final String bannerDelete(String PATH) {
		String msg = SUCCESS;

		String deletesysid = request.getParameter("sysid");
		String fileName = request.getParameter("bannerName");
		String deleteddsysid = request.getParameter("dsysid");
		String detailKey = request.getParameter("detailKey");

		// 刪除
		if (StringUtils.isNotBlank(deletesysid)) {
			List<Object> deleteDetailList = new ArrayList<Object>();
			deleteDetailList.add(new DeleteStatement(
					getDetailInfoMap().get(detailKey).getDetailClass().getSimpleName(), new QueryGroup(new QueryRule(
							PK, deletesysid))));
			msg = cloudDao.save(sf(), deleteDetailList.toArray(), false, null);
			if (StringUtils.equals(msg, SUCCESS)) {
				if (StringUtils.isNotBlank(fileName)) {
					String deleteFilePath = PATH + deleteddsysid + "/" + fileName;
					logger.debug("刪除檔案資料 deletesysid = " + deletesysid);
					logger.debug("刪除檔案資料 deletename = " + fileName);
					logger.debug("刪除檔案資料 deleteddsysid = " + deleteddsysid);

					logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
					File deleteLocation = new File(deleteFilePath);
					deleteLocation.delete();
				}

				logger.debug("測式是否進入刪除檔案資料迴圈前");

				Set detailSet = findDetailSetWhenEdit(getDetailInfoMap().get(detailKey).getDetailResource());
				logger.debug("刪除檔案資料 detailSet =" + detailSet);
				if (detailSet != null) {
					logger.debug("刪除檔案資料 detailSet.size() =" + detailSet.size());
				}
				Object b = null;
				try {
					for (Object object : detailSet) {
						String objectSysid = (String) PropertyUtils.getProperty(object, PK);
						if (objectSysid.equals(deletesysid)) {
							b = object;
							break;
						}
					}
					if (b != null) {
						boolean aa = detailSet.remove(b);

						logger.debug("is remove success:" + aa);
						logger.debug("刪除檔案資料 detailSet.size() =" + detailSet.size());
					}
					remapDataOrder(getDetailInfoMap().get(detailKey).getDetailResource());
				} catch (Exception e) {
					e.printStackTrace();
				}
				// super.save();//造成異常
			}
		}
		resultString = msg;
		return JSON_RESULT;
	}
}