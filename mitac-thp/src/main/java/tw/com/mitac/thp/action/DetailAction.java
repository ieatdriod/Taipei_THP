package tw.com.mitac.thp.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.jqgrid.bean.SysColumnConfig;
import tw.com.mitac.jqgrid.creator.SystemColumnConfigCreator;
import tw.com.mitac.poi.ExcelCreator;
import tw.com.mitac.poi.ExcelTitle;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class DetailAction<MO> extends DetailController<MO> {
	private static final long serialVersionUID = 1L;

	protected DetailInfo getJoinDetailInfo() {
		return getDetailInfoMap().get("");
	}

	protected boolean escapeJoin() {
		boolean returnValue = false;
		returnValue = true;
		return returnValue;
	}

	/**
	 * [jqgrid]依ColumnConfig產生前端欄位宣告
	 * 
	 * @see #createSystemColumnConfigMap(Class, String)
	 */
	@Override
	protected Map<String, Map> getJqgridColModelMap() {
		if (escapeJoin())
			return super.getJqgridColModelMap();
		return getJqgridColModelMap(getPersistentClass(), getJoinDetailInfo().getDetailClass(), "bean",
				getJoinDetailInfo().getDetailI18nKey());
	}

	/**
	 * [jqgrid]依ColumnConfig產生前端欄位宣告 其中包含join資料
	 * 
	 * @see #createSystemColumnConfigMap(Class, String)
	 * @see #createColumnConfigMap(Class)
	 */
	protected Map<String, Map> getJqgridColModelMap(Class<?> clazz, Class<?> joinClass, String i18nKey,
			String i18nKeyJoin) {
		logger.debug("start[join]");
		String tablename = clazz.getSimpleName() + "Join" + joinClass.getSimpleName();
		Map<String, Class> fields = joinFields(clazz, joinClass, i18nKey, i18nKeyJoin);
		String as1 = Util.buildJoinTableFrontKey(clazz);
		String as2 = Util.buildJoinTableFrontKey(joinClass);

		Map<String, Map> jqgridColModelMap = new LinkedHashMap<String, Map>();
		sessionSet("jqgridColModelMap", jqgridColModelMap);
		try {
			Map<String, SysColumnConfig> systemColumnConfigMap = createSystemColumnConfigMap(clazz, joinClass, i18nKey,
					i18nKeyJoin);
			Map<String, SysColumnConfig> columnConfigMap = getColumnConfigCreator().createColumnConfigMap(tablename,
					fields, createOperatorValue()

			);
			for (SysColumnConfig sysColumnConfig : columnConfigMap.values())
				jqgridColModelMap.put(sysColumnConfig.getColumnId(), null);

			Field[] f = clazz.getDeclaredFields();
			// Object defaultObj = getDefaultDMO(clazz);
			for (Field field : f)
				if (!Modifier.isStatic(field.getModifiers()))
					if (!field.getType().equals(Set.class)) {
						String key = as1 + "_" + field.getName();
						String columnId = as1 + "_" + field.getName();
						SysColumnConfig sysColumnConfig = columnConfigMap.get(key);
						SysColumnConfig systemSysColumnConfig = systemColumnConfigMap.get(key);
						String columnName = sysColumnConfig.getColumnName();

						Map<String, Object> colModelMap = getColModelMap(clazz, field, columnId, columnName,
								sysColumnConfig, systemSysColumnConfig);
						jqgridColModelMap.put(columnId, colModelMap);
					}
			f = joinClass.getDeclaredFields();
			for (Field field : f)
				if (!Modifier.isStatic(field.getModifiers()))
					if (!field.getType().equals(Set.class)) {
						String key = as2 + "_" + field.getName();
						String columnId = as2 + "_" + field.getName();
						SysColumnConfig sysColumnConfig = columnConfigMap.get(key);
						SysColumnConfig systemSysColumnConfig = systemColumnConfigMap.get(key);
						String columnName = sysColumnConfig.getColumnName();

						Map<String, Object> colModelMap = getColModelMap(clazz, field, columnId, columnName,
								sysColumnConfig, systemSysColumnConfig);
						jqgridColModelMap.put(columnId, colModelMap);
					}

			String columnId = as1 + "_" + PK;
			Map<String, Object> colModelMap = jqgridColModelMap.get(columnId);
			Map<String, Object> pkColModelMap = new HashMap<String, Object>();
			pkColModelMap.putAll(colModelMap);
			pkColModelMap.put("name", PK);
			pkColModelMap.put("index", PK);
			jqgridColModelMap.put(PK, pkColModelMap);
		} catch (Exception e) {
			logger.error(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}

		logger.debug("end[join] where jqgridColModelMap.size:" + jqgridColModelMap.size());
		return jqgridColModelMap;
	}

	protected final SystemColumnConfigCreator initSystemColumnConfigCreator(Class<?> clazz, Class<?> joinClass,
			String i18nKey, String i18nKeyJoin) {
		String tablename = clazz.getSimpleName() + "Join" + joinClass.getSimpleName();
		String as1 = Util.buildJoinTableFrontKey(clazz);
		String as2 = Util.buildJoinTableFrontKey(joinClass);

		SystemColumnConfigCreator systemColumnConfigCreator = getSystemColumnConfigCreator();
		Map<String, String> i18nMap = systemColumnConfigCreator.getI18nResource().get(tablename);

		if (i18nMap == null) {
			i18nMap = new HashMap<String, String>();
			systemColumnConfigCreator.getI18nResource().put(tablename, i18nMap);

			initSystemColumnConfigCreator(clazz, i18nKey);
			initSystemColumnConfigCreator(joinClass, i18nKeyJoin);
			Map<String, String> i18nMap1 = systemColumnConfigCreator.getI18nResource().get(clazz.getSimpleName());
			Map<String, String> i18nMap2 = systemColumnConfigCreator.getI18nResource().get(joinClass.getSimpleName());
			for (String columnId1 : i18nMap1.keySet()) {
				String columnId = as1 + "_" + columnId1;
				i18nMap.put(columnId, i18nMap1.get(columnId1));
			}
			String preText = getText("bean." + getJoinDetailInfo().getDetailResource()) + "-";
			for (String columnId2 : i18nMap2.keySet()) {
				String columnId = as2 + "_" + columnId2;
				i18nMap.put(columnId, preText + i18nMap2.get(columnId2));
			}
		}

		return systemColumnConfigCreator;
	}

	protected final Map<String, SysColumnConfig> createSystemColumnConfigMap(Class<?> clazz, Class<?> joinClass,
			String i18nKey, String i18nKeyJoin) {
		String tablename = clazz.getSimpleName() + "Join" + joinClass.getSimpleName();
		Map<String, Class> fields = joinFields(clazz, joinClass, i18nKey, i18nKeyJoin);
		return initSystemColumnConfigCreator(clazz, joinClass, i18nKey, i18nKeyJoin).createSystemColumnConfigMap(
				tablename, fields, createOperatorValue());
	}

	protected final Map<String, Class> joinFields(Class<?> clazz, Class<?> joinClass, String i18nKey, String i18nKeyJoin) {
		String as1 = Util.buildJoinTableFrontKey(clazz);
		String as2 = Util.buildJoinTableFrontKey(joinClass);
		Map<String, SysColumnConfig> systemColumnConfigMap1 = createSystemColumnConfigMap(clazz, i18nKey);
		Map<String, SysColumnConfig> systemColumnConfigMap2 = createSystemColumnConfigMap(joinClass, i18nKeyJoin);
		Map<String, Class> fields = new LinkedHashMap<String, Class>();
		for (String columnId1 : systemColumnConfigMap1.keySet())
			try {
				Field field = clazz.getDeclaredField(columnId1);
				fields.put(as1 + "_" + columnId1, field.getType());
			} catch (Exception e) {
				e.printStackTrace();
			}
		for (String columnId2 : systemColumnConfigMap2.keySet())
			try {
				Field field = joinClass.getDeclaredField(columnId2);
				fields.put(as2 + "_" + columnId2, field.getType());
			} catch (Exception e) {
				e.printStackTrace();
			}
		return fields;
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	@Override
	public String ajaxRemapHidden() {
		if (escapeJoin())
			return super.ajaxRemapHidden();
		return ajaxRemapHidden(getPersistentClass(), getJoinDetailInfo().getDetailClass(), "bean", getJoinDetailInfo()
				.getDetailI18nKey());
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	protected final String ajaxRemapHidden(Class<?> clazz, Class<?> joinClass, String i18nKey, String i18nKeyJoin) {
		String tablename = clazz.getSimpleName() + "Join" + joinClass.getSimpleName();
		Map<String, Class> fields = joinFields(clazz, joinClass, i18nKey, i18nKeyJoin);
		String remapStr = request.getParameter("remap");
		String hiddenStr = request.getParameter("hidden");
		resultString = getColumnConfigCreator().columnRemapHidden(tablename, fields, remapStr, hiddenStr,
				createOperatorValue());
		return JSON_RESULT;
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String ajaxColumnResize() {
		if (escapeJoin())
			return super.ajaxColumnResize();
		return ajaxColumnResize(getPersistentClass(), getJoinDetailInfo().getDetailClass(), "bean", getJoinDetailInfo()
				.getDetailI18nKey());
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	protected final String ajaxColumnResize(Class<?> clazz, Class<?> joinClass, String i18nKey, String i18nKeyJoin) {
		String tablename = clazz.getSimpleName() + "Join" + joinClass.getSimpleName();
		Map<String, Class> fields = joinFields(clazz, joinClass, i18nKey, i18nKeyJoin);
		String columnId = request.getParameter("columnId");
		String width = request.getParameter("width");
		resultString = getColumnConfigCreator().columnResize(tablename, fields, columnId, width, createOperatorValue());
		return JSON_RESULT;
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * [jqgrid] default sort name
	 * 
	 * @return
	 */
	public String getJqgridDefaultSidx() {
		if (escapeJoin())
			return super.getJqgridDefaultSidx();
		Class<?> clazz = getPersistentClass(), joinClass = getJoinDetailInfo().getDetailClass();
		String as1 = Util.buildJoinTableFrontKey(clazz);
		String as2 = Util.buildJoinTableFrontKey(joinClass);
		try {
			MO bean = (MO) ConstructorUtils.invokeConstructor(getPersistentClass(), null);
			if (PropertyUtils.getPropertyDescriptor(bean, ID) != null)
				return as1 + "_" + ID;
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILLNO) != null)
				return as1 + "_" + Util.BILLNO;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * [jqgrid] filters to QueryRule List
	 */
	protected QueryGroup searchByFilters() {
		if (escapeJoin())
			return super.searchByFilters();
		QueryGroup queryGroup = null;
		try {
			logger.debug("filters:" + filters);
			JSONObject group = new JSONObject(filters);
			queryGroup = Util.jqgridGroupJSONObjectToQueryGroup(getPersistentClass(), getJoinDetailInfo()
					.getDetailClass(), group);
			// queryGroup = resetInfoQuery(queryGroup);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return queryGroup;
	}

	/**
	 * [jqgrid] url
	 */
	public String jqgridList() {
		if (escapeJoin())
			return super.jqgridList();
		return jqgridList(getPersistentClass(), getJoinDetailInfo().getDetailClass());
	}

	/**
	 * [jqgrid] url
	 */
	protected String jqgridList(Class<?> clazz, Class<?> joinClass) {
		String returnStruts = JSON_RESULT;
		logger.debug("start where oper:" + oper);
		sessionSet("jqgridCrudRows", rows);
		sessionSet("jqgridCrudPage", page);
		sessionSet("jqgridCrudSidx", sidx);
		sessionSet("jqgridCrudSord", sord);
		logger.debug("sidx:" + sidx + " sord:" + sord);
		String as1 = Util.buildJoinTableFrontKey(clazz);
		String as2 = Util.buildJoinTableFrontKey(joinClass);
		List results = Collections.emptyList();
		int from = rows * (page - 1);
		int length = rows;

		List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
		if (StringUtils.isNotBlank(sidx)) {
			queryOrderList.add(new QueryOrder(sidx, sord));
			// String className = "";
			// try {
			// className = coreSysidMapping.getString(sidx);
			// } catch (MissingResourceException e) {
			// }
			// if (StringUtils.isNotBlank(className)) {
			// try {
			// Class<?> targetClass = Class.forName(Util.beanPackage + "." +
			// className);
			//
			// String displayFormat = DEFAULT_DISPLAY_FORMAT;
			// try {
			// displayFormat = tableToDisplay.getString(className);
			// } catch (MissingResourceException e1) {
			// }
			// String display = displayFormat;
			// while (display.indexOf("#") != -1) {
			// int i1 = display.indexOf("#");
			// int i2 = display.indexOf("#", i1 + 1);
			// if (i2 == -1)
			// break;
			// String displayColumn = display.substring(i1 + 1, i2);
			// queryOrderList.add(new QueryOrderWithTable(sidx, sord,
			// targetClass, PK, displayColumn));
			// break;
			// }
			// } catch (ClassNotFoundException e) {
			// }
			// } else {
			// queryOrderList.add(new QueryOrder(sidx, sord));
			// }
		}
		try {
			MO bean = (MO) ConstructorUtils.invokeConstructor(clazz, null);
			if (PropertyUtils.getPropertyDescriptor(bean, BILLNO) != null && !BILLNO.equals(sidx))
				queryOrderList.add(new QueryOrder(as1 + "_" + BILLNO, DESC));
			if (PropertyUtils.getPropertyDescriptor(bean, ID) != null && !ID.equals(sidx))
				queryOrderList.add(new QueryOrder(as1 + "_" + ID, ASC));
			if (PropertyUtils.getPropertyDescriptor(bean, PK) != null && !PK.equals(sidx))
				queryOrderList.add(new QueryOrder(as1 + "_" + PK, DESC));
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<QueryGroup> andQueryGroupsList = new ArrayList<QueryGroup>();
		try {
			sessionSet("jqgridCrudSearch", search);
			sessionSet("jqgridCrudFilters", filters);
			if (search)
				if (StringUtils.isNotBlank(filters))
					andQueryGroupsList.add(searchByFilters());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		// XXX 追加處理'as1_'???
		QueryGroup queryCondition = getQueryCondition();
		if (queryCondition != null)
			andQueryGroupsList.add(queryCondition);
		QueryGroup queryRestrict = getQueryRestrict();
		if (queryRestrict != null)
			andQueryGroupsList.add(queryRestrict);

		QueryGroup q = new QueryGroup(AND, null, andQueryGroupsList.toArray(new QueryGroup[0]));

		if (StringUtils.equals("excel", oper)) {
			record = cloudDao.queryJoinCount(sf(), clazz, joinClass, getJoinDetailInfo().getDetailResource(), q);
			results = cloudDao.queryJoinTable(sf(), clazz, joinClass, getJoinDetailInfo().getDetailResource(), q,
					queryOrderList.toArray(new QueryOrder[0]), null, null);
		} else {
			record = cloudDao.queryJoinCount(sf(), clazz, joinClass, getJoinDetailInfo().getDetailResource(), q);
			results = cloudDao.queryJoinTable(sf(), clazz, joinClass, getJoinDetailInfo().getDetailResource(), q,
					queryOrderList.toArray(new QueryOrder[0]), from, length);
		}
		// ---------- ---------- ---------- ---------- ----------
		List<Map> formatToMapResults = formatListToStaticMap(results);
		for (Map map : formatToMapResults)
			map.put(PK, map.get(as1 + "_" + PK));
		if (!"excel".equals(oper)) {
			String actionTypeText = getActionType();
			String methodText = "edit";// "view";
			String targetText = (String) application.get("editFrame");// "viewFrame";
			if (ACTION_TYPE_DIALOG.equals(getActionType())) {
				actionTypeText = ACTION_TYPE_CRUD;
				methodText = "edit";
				targetText = "_blank";// (String) application.get("editFrame");
			}
			for (int i = 0; i < formatToMapResults.size(); i++) {
				Map<String, Object> map = formatToMapResults.get(i);
				Object pkObj = map.get(PK);

				// button
				String url = request.getContextPath() + "/" + actionTypeText + "/" + getActionKey() + "_" + methodText
						+ "?bean.sysid=" + pkObj;
				String clickBtnEditText = "<a href='" + url + "' target='" + targetText + "'>"
						+ "<button type='button' class='mi-invisible-btn' style='width:65px;'>"
						+ "<i class='glyphicon glyphicon-edit' style='color:#ff3971;' title='"
						+ getText("jqgrid.clickBtnEdit") + "'/>" + "</button></a>";

				Object selectId = new Integer(i + 1);
				// if (StringUtils.isNotBlank(getTreeParentKey()))
				// selectId = pkObj;
				String clickBtnSelect = "<button type='button' class='mi-invisible-btn' style='width:65px;' onclick='fnJqgridSelect(\""
						+ selectId
						+ "\")'>"
						+ "<i  class='glyphicon glyphicon-check mi-pointer' style='color:#ff3971;' title='"
						+ getText("jqgrid.clickBtnSelect") + "'/>" + "</button>";

				map.put("clickBtnEdit", clickBtnEditText);
				map.put("clickBtnSelect", clickBtnSelect);
			}
		}
		// ---------- ---------- ---------- ---------- ----------
		total = (int) Math.ceil((double) record / (double) rows);

		// this.setGridModel(formatToMapResults);
		resultMap = new HashMap();
		resultMap.put("gridModel", formatToMapResults);

		resultMap.put("page", page);
		resultMap.put("record", record);
		resultMap.put("rows", rows);
		resultMap.put("total", total);

		if ("excel".equals(oper)) {
			jqgridListOperExcel();
			// if(hasActionErrors())
			// return ERROR;
			returnStruts = DOWN_STREAM;
		}

		logger.debug("end");
		return returnStruts;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected void jqgridListOperExcel() {
		if (escapeJoin()) {
			super.jqgridListOperExcel();
			return;
		}
		Class<?> clazz = getPersistentClass(), joinClass = getJoinDetailInfo().getDetailClass();
		String as1 = Util.buildJoinTableFrontKey(clazz);
		String as2 = Util.buildJoinTableFrontKey(joinClass);

		List<Map> formatToMapResults = (List<Map>) resultMap.get("gridModel");
		String fileName = PREFIX + ".xlsx";
		String fileName2 = FILE_DEFAULT_CREATE() + fileName;
		File dstFile = new File(FILE_DEFAULT_CREATE());
		if (!dstFile.exists()) {
			dstFile.mkdirs();// 建立資料夾
		}
		dstFile = new File(fileName2);
		dstFile.delete();

		XSSFWorkbook wwb = new XSSFWorkbook(); // 建立Excel物件
		logger.info("XSSFWorkbook create...");

		XSSFFont font = wwb.createFont();
		font.setColor(HSSFColor.BLACK.index); // 顏色
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL); // 粗細體
		font.setFontHeightInPoints((short) 12);// 字體大小
		font.setFontName("TIMES");

		// 設定儲存格格式
		XSSFCellStyle cellFormat10 = wwb.createCellStyle();
		{
			cellFormat10.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);// 填滿顏色
			cellFormat10.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFormat10.setFont(font); // 設定字體
			cellFormat10.setAlignment(CellStyle.ALIGN_CENTER); // 水平置中
			cellFormat10.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER); // 垂直置中
			// 設定框線
			cellFormat10.setBorderBottom((short) 1);
			cellFormat10.setBorderTop((short) 1);
			cellFormat10.setBorderLeft((short) 1);
			cellFormat10.setBorderRight((short) 1);
			cellFormat10.setWrapText(true); // 自動換行
		}

		XSSFCellStyle cellFormat11 = wwb.createCellStyle();
		{
			cellFormat11.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);// 填滿顏色
			cellFormat11.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFormat11.setFont(font); // 設定字體
			cellFormat11.setAlignment(CellStyle.ALIGN_CENTER); // 水平置中
			cellFormat11.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER); // 垂直置中
			// 設定框線
			cellFormat11.setBorderBottom((short) 1);
			cellFormat11.setBorderTop((short) 1);
			cellFormat11.setBorderLeft((short) 1);
			cellFormat11.setBorderRight((short) 1);
			cellFormat11.setWrapText(true); // 自動換行
		}
		// int widthRate = 5;
		int widthRate = 10;
		Map<String, XSSFSheet> writableSheetMap = new LinkedHashMap<String, XSSFSheet>();
		Map<String, ExcelCreator> excelCreatorMap = new LinkedHashMap<String, ExcelCreator>();
		List<ExcelTitle> mainTitleList = new ArrayList<ExcelTitle>();

		// 創建一個可寫入的工作表
		// Workbook的createSheet方法有兩個參數，第一個是工作表的名稱，第二個是工作表在工作薄中的位置
		ExcelCreator excelCreator = new ExcelCreator();
		excelCreatorMap.put("", excelCreator);
		excelCreator.setWorkbook(wwb);
		String mainSheetName = tw.com.mitac.ssh.util.Util.halfwidthToFullwidth(getActionTitle()).replace(":", "_")
				.replace("/", "_");
		logger.info("XSSFWorkbook createSheet..." + mainSheetName);
		XSSFSheet ws = wwb.createSheet(mainSheetName);
		excelCreator.setWritableSheet(ws);
		List<ExcelTitle> titleList = new ArrayList<ExcelTitle>();
		excelCreator.setTitleList(titleList);
		excelCreator.setDataList(formatToMapResults);
		excelCreator.setTitleFormat(cellFormat10);

		List<String> pkList = new ArrayList<String>();

		Field[] f = getCrudJqgridClass().getDeclaredFields();
		Map<String, Map> jqgridColModelMap = getJqgridColModelMap();

		for (Field field : f)
			if (!Modifier.isStatic(field.getModifiers())) {
				// 跳過join目標
				if (getJoinDetailInfo().getDetailResource().equals(field.getName()))
					continue;
				if (field.getType().equals(Set.class)) {
				} else {
					// if (ID.equals(field.getName()) ||
					// BILLNO.equals(field.getName()))
					// headKey = field.getName();

					Map<String, Object> colModelMap = jqgridColModelMap.get(as1 + "_" + field.getName());
					if (colModelMap == null)
						continue;
					Boolean hidden = (Boolean) colModelMap.get("hidden");
					if (hidden != null && hidden)
						continue;

					int width = (Integer) colModelMap.get("width");
					String label = (String) colModelMap.get("label");

					ExcelTitle excelTitle = new ExcelTitle();
					excelTitle.setKey(field.getName());
					excelTitle.setLabel(label);
					excelTitle.setWidth(width / widthRate);
					excelTitle.setAlignment(CellStyle.ALIGN_LEFT);

					if (Long.class.equals(field.getType()) || BigDecimal.class.equals(field.getType()))
						excelTitle.setAlignment(CellStyle.ALIGN_RIGHT);
					mainTitleList.add(excelTitle);
				}
			}
		for (ExcelTitle excelTitle : mainTitleList)
			try {
				ExcelTitle hExcelTitle = (ExcelTitle) BeanUtils.cloneBean(excelTitle);
				hExcelTitle.setKey(as1 + "_" + hExcelTitle.getKey());
				titleList.add(hExcelTitle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		for (Field field : joinClass.getDeclaredFields())
			if (!Modifier.isStatic(field.getModifiers())) {
				if (field.getType().equals(Set.class)) {
					continue;
				} else {
					Map<String, Object> colModelMap = jqgridColModelMap.get(as2 + "_" + field.getName());
					if (colModelMap == null)
						continue;
					Boolean hidden = (Boolean) colModelMap.get("hidden");
					if (hidden != null && hidden)
						continue;

					int width = (Integer) colModelMap.get("width");
					String label = (String) colModelMap.get("label");

					ExcelTitle excelTitle = new ExcelTitle();
					excelTitle.setKey(as2 + "_" + field.getName());
					excelTitle.setLabel(label);
					excelTitle.setWidth(width / widthRate);
					excelTitle.setAlignment(CellStyle.ALIGN_LEFT);
					excelTitle.setTitleFormat(cellFormat11);

					if (Long.class.equals(field.getType()) || BigDecimal.class.equals(field.getType()))
						excelTitle.setAlignment(CellStyle.ALIGN_RIGHT);
					titleList.add(excelTitle);
				}
			}

		// 資料整理
		for (Map data : formatToMapResults) {
			String pk = (String) data.get(as1 + "_" + PK);
			pkList.add(pk);

			for (Field field : f)
				if (!Modifier.isStatic(field.getModifiers())) {
					if (!field.getType().equals(Set.class)) {
						String _filedName = as1 + "_" + field.getName();
						Map<String, Object> colModelMap = jqgridColModelMap.get(_filedName);
						if (colModelMap == null)
							continue;

						Boolean hidden = (Boolean) colModelMap.get("hidden");
						if (hidden != null && hidden)
							continue;

						Object dataValue = data.get(_filedName);
						String text = "";
						String javaClass = (String) colModelMap.get("javaClass");
						if (dataValue != null) {
							text = dataValue.toString();
							if (StringUtils.isNotBlank(text)) {
								Map<String, Object> editoptionsMap = (Map<String, Object>) colModelMap
										.get("editoptions");
								Map<String, String> menu = (Map<String, String>) editoptionsMap.get("value");
								if (menu != null) {
									text = menu.get(text);
									if (StringUtils.isBlank(text))
										if (Boolean.class.getSimpleName().equals(javaClass))
											text = menu.get(dataValue);
									data.put(_filedName, text);
								}
							}
						}
					}
				}
			// TODO 尾檔
		}
		excelCreator.execute();

		// ---------- ---------- ---------- ---------- ----------

		// 尾檔
		for (Field field : f)
			if (!Modifier.isStatic(field.getModifiers())) {
				// 跳過join目標
				if (getJoinDetailInfo().getDetailResource().equals(field.getName()))
					continue;
				if (field.getType().equals(Set.class)
						&& !isColumnHidden(getPersistentClass().getSimpleName(), field.getName())) {
					Class<?> dClass = findDetailInfoByResource(field.getName()).getDetailClass();
					if (dClass == null)
						continue;

					String sheetKey = field.getName();
					String subSheetName = getText("bean." + field.getName());
					if (("bean." + field.getName()).equals(subSheetName))
						subSheetName = getActionTitle() + "Detail"
								+ (writableSheetMap.size() == 1 ? "" : writableSheetMap.size());
					subSheetName = tw.com.mitac.ssh.util.Util.halfwidthToFullwidth(subSheetName).replace(":", "_")
							.replace("/", "_");
					logger.info("XSSFWorkbook createSheet..." + subSheetName);

					ExcelCreator dExcelCreator = new ExcelCreator();
					excelCreatorMap.put(sheetKey, excelCreator);
					dExcelCreator.setWorkbook(wwb);
					XSSFSheet dws = wwb.createSheet(subSheetName);
					dExcelCreator.setWritableSheet(dws);
					writableSheetMap.put(sheetKey, dws);
					List<ExcelTitle> dTitleList = new ArrayList<ExcelTitle>();
					dExcelCreator.setTitleList(dTitleList);
					List dDataList = new ArrayList();
					dExcelCreator.setDataList(dDataList);
					dExcelCreator.setTitleFormat(cellFormat10);

					Map<String, Map> jqgridDetailColModelMap = null;
					try {
						Map<String, Map> jqgridDetailColModelInfoMap = (Map<String, Map>) PropertyUtils.getProperty(
								this, "jqgridDetailColModelInfoMap");
						String detailKey = findDetailInfoByResource(field.getName()).getDetailKey();
						jqgridDetailColModelMap = jqgridDetailColModelInfoMap.get(detailKey);
					} catch (Exception e) {
						e.printStackTrace();
					}
					logger.debug(field.getName() + ":jqgridDetailColModelMap.size="
							+ (jqgridDetailColModelMap == null ? "NULL" : jqgridDetailColModelMap.size()));

					// Map<String, Object> hColModelMap =
					// jqgridColModelMap.get(headKey);
					// int hWidth = (Integer) hColModelMap.get("width");
					// String hLabel = (String) hColModelMap.get("label");
					//
					// ExcelTitle hExcelTitle = new ExcelTitle();
					// hExcelTitle.setKey(headKey);
					// hExcelTitle.setLabel(hLabel);
					// hExcelTitle.setWidth(hWidth / widthRate);
					// hExcelTitle.setAlignment(Alignment.LEFT);
					// dTitleList.add(hExcelTitle);
					for (ExcelTitle excelTitle : mainTitleList)
						try {
							ExcelTitle hExcelTitle = (ExcelTitle) BeanUtils.cloneBean(excelTitle);
							hExcelTitle.setKey("h_" + hExcelTitle.getKey());
							dTitleList.add(hExcelTitle);
						} catch (Exception e) {
							e.printStackTrace();
						}

					for (String key : jqgridDetailColModelMap.keySet()) {
						try {
							Field dField = dClass.getDeclaredField(key);
							// if (ID.equals(field.getName()) ||
							// Util.BILLNO.equals(field.getName()))
							// headKey = field.getName();

							Map<String, Object> colModelMap = jqgridDetailColModelMap.get(dField.getName());

							Boolean hidden = (Boolean) colModelMap.get("hidden");
							if (hidden != null && hidden)
								continue;

							int width = (Integer) colModelMap.get("width");
							String label = (String) colModelMap.get("label");

							ExcelTitle excelTitle = new ExcelTitle();
							excelTitle.setKey(dField.getName());
							excelTitle.setLabel(label);
							excelTitle.setWidth(width / widthRate);
							excelTitle.setAlignment(CellStyle.ALIGN_LEFT);
							if (Long.class.equals(dField.getType()) || BigDecimal.class.equals(dField.getType()))
								excelTitle.setAlignment(CellStyle.ALIGN_RIGHT);
							excelTitle.setTitleFormat(cellFormat11);
							dTitleList.add(excelTitle);
						} catch (Exception e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					}

					// 資料整理
					List<?> detailList = cloudDao.queryTable(sf(), dClass,
							new QueryGroup(new QueryRule(FK, IN, pkList)), new QueryOrder[] { new QueryOrder(FK),
									new QueryOrder(PK) }, null, null);
					Map<Object, Set> detailSetMap = new LinkedHashMap<Object, Set>();
					try {
						for (Object detail : detailList) {
							Object fk = PropertyUtils.getProperty(detail, FK);
							Set detailSet = detailSetMap.get(fk);
							if (detailSet == null) {
								detailSet = new LinkedHashSet();
								detailSetMap.put(fk, detailSet);
							}
							detailSet.add(detail);
						}
					} catch (Exception e) {
					}

					for (Map data : formatToMapResults) {
						String pk = (String) data.get(as1 + "_" + PK);
						Set<?> detailSet = detailSetMap.get(pk);
						if (detailSet == null)
							continue;

						// String headKeyText = (String) data.get(headKey);
						Map headKeyTextMap = new LinkedHashMap();
						for (Object key : data.keySet())
							headKeyTextMap.put("h_" + key, data.get(as1 + "_" + key));

						for (Object detail : detailSet) {
							Map<String, Object> formatDetailMap = tw.com.mitac.ssh.util.Util
									.formatToMap((Serializable) detail);
							// formatDetailMap.put(headKey, headKeyText);
							formatDetailMap.putAll(headKeyTextMap);

							Field[] detailFields = dClass.getDeclaredFields();
							for (Field detailField : detailFields)
								if (!Modifier.isStatic(detailField.getModifiers())) {
									if (!detailField.getType().equals(Set.class)) {
										Map<String, Object> colModelMap = jqgridDetailColModelMap.get(detailField
												.getName());
										if (colModelMap == null)
											continue;
										Boolean hidden = (Boolean) colModelMap.get("hidden");
										if (hidden != null && hidden)
											continue;

										Object dataValue = formatDetailMap.get(detailField.getName());
										String text = "";
										String javaClass = (String) colModelMap.get("javaClass");
										if (dataValue != null) {
											text = dataValue.toString();
											if (StringUtils.isNotBlank(text)) {
												Map<String, Object> editoptionsMap = (Map<String, Object>) colModelMap
														.get("editoptions");
												Map<String, String> menu = (Map<String, String>) editoptionsMap
														.get("value");
												if (menu != null) {
													text = menu.get(text);
													if (StringUtils.isBlank(text))
														if (Boolean.class.getSimpleName().equals(javaClass))
															text = menu.get(dataValue);
													formatDetailMap.put(detailField.getName(), text);
												}
											}
										}
									}
								}

							dDataList.add(formatDetailMap);
						}
					}

					dExcelCreator.execute();
				}
			}

		try {
			FileOutputStream fOut = new FileOutputStream(dstFile);
			wwb.write(fOut);
			fOut.close();
			logger.info("XSSFWorkbook write...");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			downFileName = fileName;
			downInputStream = new FileInputStream(fileName2);
		} catch (Exception e) {
			addActionError(e.getMessage());
		}
	}

	@Override
	public String turnToConfirmed() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, DETAIL_SET) != null) {
				Set detailSet = findDetailSetWhenEdit(DETAIL_SET);
				if (detailSet.size() < 1) {
					addActionError(getText("errMsg.needDetail"));
					return EDIT_ERROR;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.turnToConfirmed();
	}
}