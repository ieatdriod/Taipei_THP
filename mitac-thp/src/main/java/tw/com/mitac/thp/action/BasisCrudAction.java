package tw.com.mitac.thp.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import javax.persistence.Column;
import javax.servlet.http.Cookie;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.HqlStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryOrderWithTable;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.jqgrid.bean.SysColumnConfig;
import tw.com.mitac.jqgrid.creator.ColumnConfigCreator;
import tw.com.mitac.jqgrid.creator.SystemColumnConfigCreator;
import tw.com.mitac.jqgrid.creator.UserColumnConfigCreator;
import tw.com.mitac.miaa.bean.Miaa03FileProperty;
import tw.com.mitac.poi.ExcelCreator;
import tw.com.mitac.poi.ExcelTitle;
import tw.com.mitac.ssh.util.BigDecimalTypeConverter;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.ssh.util.QueryGroupUtil;
import tw.com.mitac.ssh.util.TimeTypeConverter;
import tw.com.mitac.thp.bean.BhsInfoLink;
import tw.com.mitac.thp.bean.BhsMenuLink;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsMenuLink;
import tw.com.mitac.thp.bean.SysBillnomanagement;
import tw.com.mitac.thp.bean.SysConstant;
import tw.com.mitac.thp.tree.BeanTreeNode;
import tw.com.mitac.thp.tree.TreeUtil;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

/**
 * crud
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class BasisCrudAction<MO> extends BasisLoginAction {
	private static final long serialVersionUID = 1L;
	protected static final boolean isShowFlow = false;

	public boolean getIsDataChangeLocale() {
		if (!isChangeLocale)
			return false;
		boolean isSkip = skipChangeLocaleList.contains(getPersistentClass().getSimpleName());
		return !isSkip;
	}

	public final boolean getIsImgChangeLocale() {
		return false;
	}

	protected static final String ACTION_TYPE_CRUD = "crud";
	protected static final String ACTION_TYPE_DIALOG = "dialog";

	private Class<MO> persistentClass;
	private Map<String, Map> declaredFieldMap;
	protected MO bean;
	protected Map<String, String> beaninfo = new HashMap<String, String>();
	protected String andQueryRulesStr;
	protected String andQueryGroupsStr;

	protected Class<MO> getPersistentClass() {
		return persistentClass;
	}

	protected Class<?> getCrudJqgridClass() {
		return getPersistentClass();
	}

	/**
	 * <pre>
	 * 新定義尾檔
	 * </pre>
	 * 
	 * @return
	 */
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		return new LinkedHashMap<String, DetailInfo>();
	}

	public DetailInfo findDetailInfoByResource(String detailResource) {
		for (DetailInfo detailInfo : getDetailInfoMap().values())
			if (detailResource.equals(detailInfo.getDetailResource()))
				return detailInfo;
		return null;
	}

	public final Map<String, Map> getDeclaredFieldMap() {
		if (declaredFieldMap == null) {
			declaredFieldMap = new LinkedHashMap<String, Map>();
			Field[] f = getPersistentClass().getDeclaredFields();
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				Map<String, Object> fieldMap = new HashMap<String, Object>();
				fieldMap.put("javaClass", field.getType().getSimpleName());

				try {
					Method m = getPersistentClass().getMethod(
							"get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
					if (m.isAnnotationPresent(Column.class)) {
						Column column = m.getAnnotation(Column.class);
						int length = column.length();
						fieldMap.put("columnLength", length);
						fieldMap.put("columnUnique", column.unique());
						fieldMap.put("columnNullable", column.nullable());
					}

					// if (field.getType().equals(Date.class)) {
					// if
					// (m.isAnnotationPresent(javax.persistence.Temporal.class))
					// {
					// javax.persistence.Temporal temporal =
					// m.getAnnotation(javax.persistence.Temporal.class);
					// if
					// (temporal.value().equals(javax.persistence.TemporalType.TIME))
					// {
					//
					// }
					// }
					// }
				} catch (Exception e) {
				}

				String className = "";
				try {
					className = coreSysidMapping.getString(field.getName());
				} catch (MissingResourceException e) {
				}
				fieldMap.put("selectTool", className);

				String billName = "";
				try {
					billName = billSysidMapping.getString(field.getName());
				} catch (MissingResourceException e) {
				}
				fieldMap.put("billTool", billName);

				boolean isColumnTextarea = isColumnTextarea(getPersistentClass().getSimpleName(), field.getName());
				if (isColumnTextarea)
					fieldMap.put("isColumnTextarea", true);

				boolean isColumnTexthtml = isColumnTexthtml(getPersistentClass().getSimpleName(), field.getName());
				if (isColumnTexthtml)
					fieldMap.put("isColumnTexthtml", true);

				boolean isColumnHidden = isColumnHidden(getPersistentClass().getSimpleName(), field.getName());
				if (!getIsTest()) {
					if (Arrays.asList(DATA_LOG_MEMBER).contains(field.getName()))
						isColumnHidden = true;
				}
				if (isColumnHidden)
					fieldMap.put("isColumnHidden", true);

				Boolean FIELD_ALLOW = true;
				if (Util.isLogin) {
					Boolean FIELD_ALL_ALLOW = (Boolean) request.getAttribute("FIELD_ALL_ALLOW");
					FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALL_ALLOW);
					if (!FIELD_ALLOW) {
						FIELD_ALLOW = (Boolean) request.getAttribute("FIELD_" + field.getName());
						FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALLOW);
					}
				}
				fieldMap.put("FIELD_ALLOW", FIELD_ALLOW);

				declaredFieldMap.put(field.getName(), fieldMap);
			}
		}
		return declaredFieldMap;
	}

	/** get object for edit */
	public MO getBean() {
		return bean;
	}

	/** set object for edit */
	public void setBean(MO bean) {
		this.bean = bean;
	}

	/** set beaninfo for edit/query */
	public final Map<String, String> getBeaninfo() {
		return beaninfo;
	}

	/** set beaninfo for edit/query */
	public final void setBeaninfo(Map<String, String> beaninfo) {
		this.beaninfo = beaninfo;
	}

	public final String getAndQueryRulesStr() {
		return andQueryRulesStr;
	}

	public final void setAndQueryRulesStr(String andQueryRulesStr) {
		this.andQueryRulesStr = andQueryRulesStr;
	}

	public final String getAndQueryGroupsStr() {
		return andQueryGroupsStr;
	}

	public final void setAndQueryGroupsStr(String andQueryGroupsStr) {
		this.andQueryGroupsStr = andQueryGroupsStr;
	}

	public BasisCrudAction() {
		Class<?> targetClass = getClass();
		while (!ParameterizedType.class.isInstance(targetClass.getGenericSuperclass())) {
			if (targetClass.equals(Object.class))
				break;
			String msg = targetClass.getName();
			targetClass = targetClass.getSuperclass();
			msg += " -> " + targetClass.getName();
			logger.debug(msg);
		}
		if (!targetClass.equals(Object.class))
			this.persistentClass = (Class<MO>) ((ParameterizedType) targetClass.getGenericSuperclass())
					.getActualTypeArguments()[0];
		try {
			bean = (MO) ConstructorUtils.invokeConstructor(getPersistentClass(), null);
		} catch (Exception e) {
			logger.error(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}

	// ---------- ---------- ---------- ---------- ----------
	/** get action title */
	public String getActionTitle() {
		String initUrl = "crud/" + getClass().getSimpleName();
		String i18nKey = "menu." + initUrl;
		String result = getText(i18nKey);
		if (i18nKey.equals(result))
			result = getPersistentClass().getSimpleName();
		return result;
	}

	@Override
	public final String getPageBigTitle() {
		// return super.getPageBigTitle() + "/" + getActionTitle();
		return getActionTitle();
	}

	// ---------- ---------- ---------- ---------- ----------
	/** get queryInfo */
	protected QueryGroup getQueryCondition() {
		return (QueryGroup) sessionGet(SESSION_QUERY_CONDITION);
	}

	/**
	 * set queryInfo
	 * 
	 * @see #find()
	 * @see createQueryCondition()
	 * @see andQueryRulesStr
	 * @see andQueryGroupsStr
	 */
	protected void setQueryCondition(QueryGroup condition) {
		sessionSet(SESSION_QUERY_CONDITION, condition);
	}

	/** get queryInfo */
	protected Map<String, String> getQueryBeaninfo() {
		return (Map<String, String>) sessionGet(SESSION_QUERY_BEANINFO);
	}

	/** set queryInfo */
	protected void setQueryBeaninfo(Map<String, String> beaninfo) {
		sessionSet(SESSION_QUERY_BEANINFO, beaninfo);
	}

	/**
	 * 查詢限制條件
	 * <p>
	 * When an object extending class <code>BasisCrudAction</code> is used to
	 * create a CRUD action, using the action's query causes the object's
	 * <code>getQueryRestrict</code> method to be called.
	 * <p>
	 * The general contract of the method <code>getQueryRestrict</code> is that
	 * it may return any query condition.
	 * 
	 * @see #jqgridList()
	 */
	protected QueryGroup getQueryRestrict() {
		return null;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected final String getMiaaInitUrl() {
		return "crud/" + getClass().getSimpleName();
	}

	@Override
	public void validate() {
		super.validate();
		if (!hasErrors()) {
			if (getUserData() == null) {
				logger.info("SKIP - TOMEOUT");
			} else {
				if (Util.isLogin) {
					String fileSysid = getMiaaFileSysid();

					if (StringUtils.isNotBlank(fileSysid)) {
						if (StringUtils.equals(fileSysid, "x")) {
						} else {
							Map<String, Boolean> i18n_miaa_map = (Map<String, Boolean>) appMap().get("i18n_miaa_map");
							if (i18n_miaa_map == null) {
								i18n_miaa_map = new LinkedHashMap<String, Boolean>();
								appMap().put("i18n_miaa_map", i18n_miaa_map);
							}
							Boolean i18n_miaa = i18n_miaa_map.get(fileSysid);
							if (i18n_miaa == null || !i18n_miaa) {
								String businesscode = tw.com.mitac.miaa.Util.FIELD_ALL_ALLOW;
								List<Miaa03FileProperty> l = cloudDao.queryTable(sf(), Miaa03FileProperty.class,
										new QueryGroup(new QueryRule("fileSysid", fileSysid), new QueryRule(
												"filePropertyId", businesscode)), new QueryOrder[0], null, null);
								if (l.size() > 0) {
									Miaa03FileProperty appfunction_all_allow = l.get(0);
									if (businesscode.equals(appfunction_all_allow.getFilePropertyName())) {
										// i18n
										Collection<Miaa03FileProperty> appFunctionList = new ArrayList<Miaa03FileProperty>();
										appfunction_all_allow.setFilePropertyName("欄位_所有欄位");
										appFunctionList.add(appfunction_all_allow);

										l = cloudDao.queryTable(sf(), Miaa03FileProperty.class, new QueryGroup(
												new QueryRule("fileSysid", fileSysid), new QueryRule("filePropertyId",
														NE, businesscode), new QueryRule("filePropertyId", BW,
														tw.com.mitac.miaa.Util.FIELD_)), new QueryOrder[0], null, null);
										logger.debug("l.size:" + l.size());
										for (Miaa03FileProperty appfunction : l) {
											String i18nKey = "bean."
													+ (appfunction.getFilePropertyId().replace(
															tw.com.mitac.miaa.Util.FIELD_, ""));
											String i18nValue = getText(i18nKey);
											logger.debug("i18nKey:" + i18nKey + " i18nValue:" + i18nValue);
											if (!i18nValue.equals(i18nKey)) {
												appfunction.setFilePropertyName("欄位_" + i18nValue);
												appFunctionList.add(appfunction);
											}
										}

										String daoMsg = cloudDao.save(sf(), appFunctionList.toArray(), false, "UPDATE");
										// if (!daoMsg.SUCCESS) {
										// addActionError(response.getMessage());
										// return APP_LIST;
										// }
									}
								}

								i18n_miaa_map.put(fileSysid, true);
							}
						}
					}
				}
				// else {
				// // TODO ??
				// }
			}
		}
	}

	public final String getActionKey() {
		return PREFIX;
	}

	public final String getBeanKey() {
		return getPersistentClass().getSimpleName();
	}

	protected static final String SESSION_QUERY_CONDITION = "session-query-condition";
	protected static final String SESSION_QUERY_BEANINFO = "session-query-beaninfo";
	// protected static final String SESSION_QUERY_INFO = "session-query-info";
	// protected static final String SESSION_EDIT_OBJECT =
	// "session-edit-object";
	protected static final String APP_VALIDATE_SAVE = "app-validate-save";
	protected static final String APP_VALIDATE_MSG = "app-validate-msg";

	protected List<Object> saveList = new ArrayList<Object>();
	protected static final String TO_MAIN = "toMain";
	protected static final String MAIN = "main";
	protected static final String EDIT = "edit";
	protected static final String EDIT_SUCCESS = "editSuccess";
	protected static final String EDIT_ERROR = "editError";

	// ---------- ---------- ---------- ---------- ----------
	protected String editPK() {
		String pk = (String) request.getAttribute(PK);
		logger.debug("req.sysid=" + pk);
		if (StringUtils.isBlank(pk)) {
			try {
				pk = (String) PropertyUtils.getProperty(bean, PK);
				logger.debug("bean.sysid=" + pk);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		request.setAttribute(PK, pk);// for method override
		return pk;
	}

	/**
	 * to edit page
	 * 
	 * @return EDIT
	 */
	public String edit() {
		request.setAttribute("mainPageErrors", sessionRemove("mainPageErrors"));
		request.setAttribute("mainPageMsgs", sessionRemove("mainPageMsgs"));
		// setViewMode(false);
		try {
			String pk = editPK();
			Field[] f = getPersistentClass().getDeclaredFields();
			if (StringUtils.isNotBlank(pk)) {
				logger.info("pk:" + pk);
				bean = (MO) cloudDao.get(sf(), bean.getClass(), pk);
				if (bean == null) {
					addActionError(getText(FIND_NOTFOUNT));
					return EDIT_ERROR;
				}
				for (Field field : f) {
					if (Modifier.isStatic(field.getModifiers()))
						continue;
					if (!field.getType().equals(Set.class))
						continue;
					Set<?> setObj = (Set<?>) PropertyUtils.getProperty(bean, field.getName());
					if (setObj == null)
						logger.error("plz CHECK " + getPersistentClass().getSimpleName() + ".hbm.xml "
								+ field.getName());
					if (getNewDetail()) {
						request.setAttribute("EDIT_" + field.getName(), setObj);
					} else {
						sessionSet(field.getName(), setObj);
					}
				}
				// setEditObject(bean);

				if (getIsDataChangeLocale()) {
					try {
						for (String lan : languageTypeMap.keySet()) {
							List sysMultiLanguageList = cloudDao.queryTable(sf(), multiLanClass(lan), new QueryGroup(
									new QueryRule("sourceTable", getPersistentClass().getSimpleName()), new QueryRule(
											"sourceSysid", pk)), new QueryOrder[0], null, null);
							for (Object sysMultiLanguage : sysMultiLanguageList) {
								String sourceColumn = (String) PropertyUtils.getProperty(sysMultiLanguage,
										"sourceColumn");
								String columnValueString = (String) PropertyUtils.getProperty(sysMultiLanguage,
										"columnValueString");
								String columnValueText = (String) PropertyUtils.getProperty(sysMultiLanguage,
										"columnValueText");

								String sysMultiLanguageKey = sourceColumn + "_" + lan;
								String lanValue = StringUtils.isNotBlank(columnValueString) ? columnValueString
										: columnValueText;
								beaninfo.put(sysMultiLanguageKey, lanValue);

								if (getCookieLan().equals(lan))
									try {
										PropertyUtils.setProperty(bean, sourceColumn, lanValue);
									} catch (java.lang.NoSuchMethodException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (getIsImgChangeLocale()) {
						// 圖檔完全不參考原表內容
						for (String imgCol : getImgCols()) {
							String sysMultiLanguageKey = imgCol + "_" + getCookieLan();
							String lanValue = beaninfo.get(sysMultiLanguageKey);

							// if(StringUtils.isBlank(lanValue))
							PropertyUtils.setProperty(bean, imgCol, lanValue);
						}
					}
				}
			} else {
				logger.info("default edit value");
				Util.defaultPK(bean);//
				for (Field field : f) {
					if (Modifier.isStatic(field.getModifiers()))
						continue;

					boolean isColumnInfoStar = isColumnInfoStar(getPersistentClass().getSimpleName(), field.getName());
					if (isColumnInfoStar)
						PropertyUtils.setProperty(bean, field.getName(), Util.INFO_STAR);
					if (field.getType().equals(Set.class)) {
						if (getNewDetail()) {
							request.setAttribute("EDIT_" + field.getName(), new LinkedHashSet());
						} else {
							sessionSet(field.getName(), new LinkedHashSet());
						}
					} else if (field.getType().equals(Boolean.class)) {
						PropertyUtils.setProperty(bean, field.getName(), false);
						if (field.getName().equals(IS_ENABLED) || field.getName().equals("adsTw")
								|| field.getName().equals("adsCn") || field.getName().equals("adsUs")) {
							PropertyUtils.setProperty(bean, field.getName(), true);
						}
					} else if (field.getType().equals(BigDecimal.class)) {
						if (field.getName().equals(Util.EXCHANGE_RATE))
							PropertyUtils.setProperty(bean, field.getName(), BigDecimal.ONE);
						else
							PropertyUtils.setProperty(bean, field.getName(), BigDecimal.ZERO);
					} else if (field.getType().equals(Long.class)) {
						PropertyUtils.setProperty(bean, field.getName(), 0L);
					} else if (field.getType().equals(Integer.class)) {
						if (field.getName().equals(DATA_ORDER)) {
							PropertyUtils.setProperty(bean, field.getName(), 1);
						} else {
							PropertyUtils.setProperty(bean, field.getName(), 0);
						}
					} else if (field.getType().equals(Date.class)) {
						if (PropertyUtils.getProperty(bean, field.getName()) == null)
							if (!field.getName().equals(OD) && !field.getName().equals(CD)
									&& !dateDefaultNullList.contains(field.getName()))
								if (dateDefaultMaxList.contains(field.getName())) {
									Calendar cal = Calendar.getInstance();
									cal.set(Calendar.YEAR, 9999);
									cal.set(Calendar.MONTH, Calendar.DECEMBER);
									cal.set(Calendar.DAY_OF_MONTH, 31);
									cal.set(Calendar.HOUR_OF_DAY, 0);
									cal.set(Calendar.MINUTE, 0);
									cal.set(Calendar.SECOND, 0);
									PropertyUtils.setProperty(bean, field.getName(), cal.getTime());
								} else {
									Map<String, Object> fieldMap = getDeclaredFieldMap().get(field.getName());
									Integer length = (Integer) fieldMap.get("columnLength");
									if (length == 19)
										PropertyUtils.setProperty(bean, field.getName(), systemDatetime);
									else
										PropertyUtils.setProperty(bean, field.getName(), systemDate);
								}
					} else if (field.getType().equals(String.class)) {
						if (StringUtils.isBlank((String) PropertyUtils.getProperty(bean, field.getName()))) {
							String[] arr = { PK, OD, OP, CD, CR, ID };
							// String[] siteArr = { "sitesysid",
							// "journalizesitesysid",
							// "journalworksheetsitesysid",
							// "destinationsitesysid" };
							// String[] empArr = { "issueempsysid",
							// "requestempsysid", "businessempsysid" };
							// String[] deptArr = { "issuedeptsysid",
							// "requestdeptsysid", "businessdeptsysid",
							// "expensedeptsysid" };
							if (!isColumnInfoStar && !Arrays.asList(arr).contains(field.getName())) {
								if (getDataSysConstantIdMap().get(field.getName()) != null) {
									PropertyUtils.setProperty(bean, field.getName(),
											getDataSysConstantIdMap().get(field.getName()).getDefaultOption());
								} else if (field.getName().toLowerCase().endsWith("yearmonth")) {
									DateFormat df = new SimpleDateFormat("yyyy/MM");
									PropertyUtils.setProperty(bean, field.getName(), df.format(systemDatetime));
								}
								// else if
								// (field.getName().equals(Util.SITE_SYSID))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), getUserData().getEmp()
								// .getSiteSysid());
								// } else if
								// (field.getName().equals(Util.ISSUE_DEPT_SYSID)
								// ||
								// field.getName().equals("salesDeptSysid")
								// ||
								// field.getName().equals("purchaseDeptSysid"))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), getUserData().getEmp()
								// .getDeptSysid());
								// } else if
								// (field.getName().equals(Util.ISSUE_EMP_SYSID))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), getUserData().getEmp()
								// .getSysid());
								// } else if
								// (field.getName().equals("entitysysid")) {
								// CoreSite site =
								// getDataCoreSiteTable().get(
								// getUserData().getEmployee().getSitesysid());
								// PropertyUtils.setProperty(bean,
								// field.getName(), site.getEntitysysid());
								// } else if
								// (Arrays.asList(siteArr).contains(field.getName()))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(),
								// getUserData().getEmployee()
								// .getSitesysid());
								// } else if
								// (field.getName().endsWith("currencysysid"))
								// {
								// CoreSite site =
								// getDataCoreSiteTable().get(
								// getUserData().getEmployee().getSitesysid());
								// logger.debug("測試預設帶給幣別的是什麼幣別:"
								// + ((CoreCurrency)
								// createDataTable(CoreCurrency.class).get(
								// site.getEntrycurrencysysid())).getName());
								// PropertyUtils.setProperty(bean,
								// field.getName(),
								// site.getEntrycurrencysysid());
								// } else if
								// (field.getName().equals("exchangerategroupsysid"))
								// {
								// CoreSite site =
								// getDataCoreSiteTable().get(
								// getUserData().getEmployee().getSitesysid());
								// PropertyUtils.setProperty(bean,
								// field.getName(),
								// site.getExchangerategroupsysid());
								// } else if
								// (field.getName().equals("warehousesysid"))
								// {
								// for (String key :
								// createDataMenu(CoreWarehouse.class).keySet())
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), key);
								// break;
								// }
								// } else if
								// (field.getName().equals("locationsysid"))
								// {
								// for (String key :
								// createDataMenu(CoreWarehouseLocation.class).keySet())
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), key);
								// break;
								// }
								// } else if
								// (Arrays.asList(empArr).contains(field.getName()))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(),
								// getUserData().getEmployee()
								// .getSysid());
								// } else if
								// (Arrays.asList(deptArr).contains(field.getName()))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(),
								// getUserData().getEmployee()
								// .getDeptsysid());
								// } else if
								// (field.getName().equals("acceptstandard"))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), "");
								// } else if
								// (field.getName().equals("samplequantitystart"))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), "");
								// } else if
								// (field.getName().equals("taxtypesysid"))
								// {
								// PropertyUtils.setProperty(bean,
								// field.getName(), Util.TAX_DEFAULT_SYSID);
								// }
							}
						}
					}
				}
			}
			// setEditObject((MO)
			// ConstructorUtils.invokeConstructor(getPersistentClass(),
			// null));

			formatInfo();
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}
		return EDIT();
	}

	protected final String EDIT() {
		if (getNewDetail()) {
			Field[] f = getPersistentClass().getDeclaredFields();
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (field.getType().equals(Set.class)) {
					Set<Object> detailSet = (Set<Object>) request.getAttribute("EDIT_" + field.getName());
					List<Map> formatToMapResults = formatListToMap(detailSet);
					JSONArray jsonArray = new JSONArray(formatToMapResults);
					beaninfo.put(field.getName(), jsonArray.toString());
				}
			}
		}
		return EDIT;
	}

	protected final String EDIT_SUCCESS() {
		EDIT();
		return EDIT_SUCCESS;
	}

	/** 取代session */
	public boolean getNewDetail() {
		return false;
	}

	protected Map<String, Class> detailClassInfo = new LinkedHashMap<String, Class>();

	/**
	 * <pre>
	 * 設定樹狀結構
	 * </pre>
	 * 
	 * @return
	 */
	public String getTreeParentKey() {
		return "";
	}

	/**
	 * 限制樹狀結構最大層數(由1開始)
	 * 
	 * @return
	 */
	public int getTreeMaxLevel() {
		return 4;
	}

	protected boolean executeSave() {
		try {
			String pk = (String) PropertyUtils.getProperty(bean, PK);

			if (StringUtils.isBlank(pk)) {
				addActionError("empty pk");
				logger.warn("empty pk");
				return false;
			}

			for (String imgCol : getImgCols())
				if (getIsDataChangeLocale() && getIsImgChangeLocale()) {
					String fileName = uploadData((imgCol + "Stream"), getLanSavePath(getCookieLan()));
					if (hasErrors())
						return false;
					if (StringUtils.isNotBlank(fileName))
						try {
							// 成功上傳檔案
							PropertyUtils.setProperty(bean, imgCol, fileName);

							for (String lan : languageTypeMap.keySet())
								if (!getCookieLan().equals(lan)) {
									if (StringUtils.isBlank(beaninfo.get(imgCol + "_" + lan))) {
										// 複製預設檔案給其他語系

										String subMainFilePath = getLanSavePath(getCookieLan()) + pk + File.separator;
										File floder = new File(subMainFilePath);
										if (!floder.exists())
											floder.mkdirs();

										String saveFilePath = subMainFilePath + fileName;
										File src = new File(saveFilePath);

										subMainFilePath = getLanSavePath(lan) + pk + File.separator;
										floder = new File(subMainFilePath);
										if (!floder.exists())
											floder.mkdirs();

										saveFilePath = subMainFilePath + fileName;
										File dst = new File(saveFilePath);

										FileUtil.copyRealFile(src, dst);
										beaninfo.put((imgCol + "_" + lan), fileName);
									}
								}
						} catch (Exception e) {
							e.printStackTrace();
						}

					for (String lan : languageTypeMap.keySet())
						if (!getCookieLan().equals(lan)) {
							fileName = uploadData((imgCol + "Stream" + "_" + lan), getLanSavePath(lan));
							if (hasErrors())
								return false;
							if (StringUtils.isNotBlank(fileName))
								beaninfo.put((imgCol + "_" + lan), fileName);
						}
				} else {
					String fileName = uploadData((imgCol + "Stream"), getDfSavePath());
					if (hasErrors())
						return false;
					if (StringUtils.isNotBlank(fileName))
						try {
							PropertyUtils.setProperty(bean, imgCol, fileName);
						} catch (Exception e) {
							e.printStackTrace();
						}
				}

			if (StringUtils.isNotBlank(getTreeParentKey())) {
				try {
					String treeParentValue = (String) PropertyUtils.getProperty(bean, getTreeParentKey());
					if (StringUtils.isNotBlank(treeParentValue)) {
						if (treeParentValue.equals(pk)) {
							PropertyUtils.setProperty(bean, getTreeParentKey(), "");
							beaninfo.remove(getTreeParentKey() + "Show");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Field[] f = getPersistentClass().getDeclaredFields();

			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (!field.getType().equals(String.class))
					continue;

				try {
					{
						String formValue = (String) PropertyUtils.getProperty(bean, field.getName());
						formValue = StringUtils.trim(formValue);
						PropertyUtils.setProperty(bean, field.getName(), formValue);
					}
					if (getIsDataChangeLocale()) {
						for (String lan : languageTypeMap.keySet()) {
							if (lan.equals(getCookieLan()))
								continue;

							String sysMultiLanguageKey = field.getName() + "_" + lan;

							String lanValue = beaninfo.get(sysMultiLanguageKey);
							lanValue = StringUtils.trim(lanValue);
							beaninfo.put(sysMultiLanguageKey, lanValue);
						}
					}

					if (isColumnTexthtml(getPersistentClass().getSimpleName(), field.getName())) {
						String txtPropertyName = field.getName() + "Txt";
						boolean isNeedReplaceAllTag = PropertyUtils.isWriteable(bean, txtPropertyName);
						{
							String formValue = (String) PropertyUtils.getProperty(bean, field.getName());
							formValue = Util.replaceLineHeight(formValue);
							PropertyUtils.setProperty(bean, field.getName(), formValue);

							if (isNeedReplaceAllTag) {
								String textValue = "";
								if (StringUtils.isNotBlank(formValue)) {
									textValue = Util.replaceAllTag(formValue);
									if (textValue.contains(Util.lnStr))
										textValue = textValue.replace(Util.lnStr, "");
								}
								PropertyUtils.setProperty(bean, txtPropertyName, textValue);
							}
						}
						if (getIsDataChangeLocale()) {
							for (String lan : languageTypeMap.keySet()) {
								if (lan.equals(getCookieLan()))
									continue;

								String sysMultiLanguageKey = field.getName() + "_" + lan;

								String lanValue = beaninfo.get(sysMultiLanguageKey);
								lanValue = Util.replaceLineHeight(lanValue);
								beaninfo.put(sysMultiLanguageKey, lanValue);

								if (isNeedReplaceAllTag) {
									String lanValueTxt = "";
									if (StringUtils.isNotBlank(lanValue)) {
										lanValueTxt = Util.replaceAllTag(lanValue);
										if (lanValueTxt.contains(Util.lnStr))
											lanValueTxt = lanValueTxt.replace(Util.lnStr, "");
									}
									beaninfo.put(txtPropertyName + "_" + lan, lanValueTxt);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (getIsDataChangeLocale()) {
				try {
					/** data in db */
					Map<String, Object> sysMultiLanguageMap = new HashMap<String, Object>();
					String sourceTable = getPersistentClass().getSimpleName();
					for (String lan : languageTypeMap.keySet()) {
						List sysMultiLanguageList = cloudDao.queryTable(sf(), multiLanClass(lan), new QueryGroup(
								new QueryRule("sourceTable", sourceTable), new QueryRule("sourceSysid", pk)),
								new QueryOrder[0], null, null);
						for (Object sysMultiLanguage : sysMultiLanguageList) {
							String sourceColumn = (String) PropertyUtils.getProperty(sysMultiLanguage, "sourceColumn");
							String sysMultiLanguageKey = sourceColumn + "_" + lan;
							sysMultiLanguageMap.put(sysMultiLanguageKey, sysMultiLanguage);
						}
						for (Field field : f) {
							if (Modifier.isStatic(field.getModifiers()))
								continue;
							if (!field.getType().equals(String.class))
								continue;

							// 樹狀結構關聯禁止多語系
							if (StringUtils.equals(field.getName(), getTreeParentKey()))
								continue;

							Map<String, Object> fieldMap = getDeclaredFieldMap().get(field.getName());
							Boolean isColumnHidden = (Boolean) fieldMap.get("isColumnHidden");
							if (isColumnHidden == null)
								isColumnHidden = false;
							Boolean FIELD_ALLOW = (Boolean) fieldMap.get("FIELD_ALLOW");
							if (FIELD_ALLOW == null)
								FIELD_ALLOW = false;
							if (isColumnHidden || !FIELD_ALLOW)
								continue;

							if (!getIsImgChangeLocale() && ArrayUtils.contains(getImgCols(), field.getName()))
								continue;

							String columnDatatype = "STRING";
							Integer length = (Integer) fieldMap.get("columnLength");
							if (length != null && length > 255)
								columnDatatype = "TEXT";

							String sysMultiLanguageKey = field.getName() + "_" + lan;
							String lanValue = beaninfo.get(sysMultiLanguageKey);
							if (lan.equals(getCookieLan()))
								lanValue = (String) PropertyUtils.getProperty(bean, field.getName());
							if (StringUtils.isNotBlank(lanValue)) {
								Object sysMultiLanguage = sysMultiLanguageMap.get(sysMultiLanguageKey);
								if (sysMultiLanguage == null) {
									sysMultiLanguage = ConstructorUtils.invokeConstructor(multiLanClass(lan), null);
									sysMultiLanguageMap.put(sysMultiLanguageKey, sysMultiLanguage);
									Util.defaultPK(sysMultiLanguage);
									PropertyUtils.setProperty(sysMultiLanguage, "sourceSysid", pk);
									PropertyUtils.setProperty(sysMultiLanguage, "sourceTable", sourceTable);
									PropertyUtils.setProperty(sysMultiLanguage, "sourceColumn", field.getName());
									PropertyUtils.setProperty(sysMultiLanguage, "columnDatatype", columnDatatype);
									PropertyUtils.setProperty(sysMultiLanguage, "columnLength", (long) length);
								}
								if ("STRING".equals(columnDatatype))
									PropertyUtils.setProperty(sysMultiLanguage, "columnValueString", lanValue);
								else if ("TEXT".equals(columnDatatype))
									PropertyUtils.setProperty(sysMultiLanguage, "columnValueText", lanValue);
							} else {
								Object sysMultiLanguage = sysMultiLanguageMap.remove(sysMultiLanguageKey);
								if (sysMultiLanguage != null)
									saveList.add(new DeleteStatement(multiLanClassName(lan), new QueryGroup(
											new QueryRule("sourceTable", sourceTable),
											new QueryRule("sourceSysid", pk), new QueryRule("sourceColumn", field
													.getName()))));
							}
						} // end of declaredFields
					} // end of languageTypeMap
					saveList.addAll(sysMultiLanguageMap.values());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (field.getType().equals(Set.class)) {
					try {
						if (getNewDetail()) {
							Set detailSet = findDetailSetWhenEdit(field.getName());
							PropertyUtils.setProperty(bean, field.getName(), detailSet);
						} else {
							Set<?> detailSet = findDetailSetWhenEdit(field.getName());
							// 儲存前確認detail.FK=bean.PK
							String sysid = (String) PropertyUtils.getProperty(bean, PK);
							if (detailSet != null)
								for (Object obj : detailSet) {
									String parentSysid = (String) PropertyUtils.getProperty(obj, Util.FK);
									if (StringUtils.isBlank(parentSysid))
										PropertyUtils.setProperty(obj, Util.FK, sysid);

									Field[] subF = obj.getClass().getDeclaredFields();
									for (Field subField : subF)
										if (!Modifier.isStatic(subField.getModifiers())) {
											if (subField.getType().equals(Set.class)) {
												Set subDetailSet = (Set) PropertyUtils.getProperty(obj,
														subField.getName());
												String subParentSysid = (String) PropertyUtils.getProperty(obj, PK);
												if (subDetailSet != null) {
													for (Object subObj : subDetailSet) {
														// String
														// oriSubParentSysid =
														// (String)
														// PropertyUtils.getProperty(
														// subObj, Util.FK);
														// if
														// (StringUtils.isBlank(oriSubParentSysid))
														PropertyUtils.setProperty(subObj, Util.FK, subParentSysid);
													}
												}
											}
										}
								}
							else
								logger.warn("fieldName:" + field.getName() + " null resource");
							PropertyUtils.setProperty(bean, field.getName(), detailSet);
						}
					} catch (NoSuchMethodException e) {
						// skip public static final String
					}
				}
			}

			try {
				// 頁面紀錄上次異動時間
				if (String.class.equals(Util.timestampClass)) {
					Object newOperationdate = PropertyUtils.getProperty(bean, OD);
					if (StringUtils.isNotBlank(pk)) {
						// 儲存前比對是否為最新資料，否則提示無法儲存
						List<?> oldOperationdateList = cloudDao.findProperty(sf(), bean.getClass(), new QueryGroup(
								new QueryRule(PK, pk)), new QueryOrder[0], false, OD);
						Object oldOperationdate = (oldOperationdateList != null && oldOperationdateList.size() == 1) ? oldOperationdateList
								.get(0) : null;

						if (oldOperationdate != null) {
							if (!oldOperationdate.equals(newOperationdate)) {
								logger.info("oldOperationdate:" + oldOperationdate);
								logger.info("newOperationdate:" + newOperationdate);
								addActionError(getText(SAVE_TIMEOUT));
								return false;
							}
						}
					}
				} else if (Date.class.equals(Util.timestampClass)) {
					DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date nOD = (Date) PropertyUtils.getProperty(bean, OD);
					Object newOperationdate = nOD != null ? df.format(nOD) : null;
					if (StringUtils.isNotBlank(pk)) {
						// 儲存前比對是否為最新資料，否則提示無法儲存
						List<?> oldOperationdateList = cloudDao.findProperty(sf(), bean.getClass(), new QueryGroup(
								new QueryRule(PK, pk)), new QueryOrder[0], false, OD);
						Date oOD = (Date) ((oldOperationdateList != null && oldOperationdateList.size() == 1) ? oldOperationdateList
								.get(0) : null);
						Object oldOperationdate = oOD != null ? df.format(oOD) : null;

						if (oldOperationdate != null) {
							if (!oldOperationdate.equals(newOperationdate)) {
								logger.info("oldOperationdate:" + oldOperationdate);
								logger.info("newOperationdate:" + newOperationdate);
								addActionError(getText(SAVE_TIMEOUT));
								return false;
							}
						}
					}
				}

				// 若有排序欄位
				if (getJqgridDefaultSoab()) {
					String msg = preSave();
					if (!SUCCESS.equals(msg)) {
						addActionError(msg);
						return false;
					}
				}
			} catch (NoSuchMethodException e) {
			}
			defaultValue(bean);

			defaultBillno(bean);

			/**
			 * [BILL_STATUS] 預設狀態為開立
			 */
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) != null) {
				String oldStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
				if (StringUtils.isBlank(oldStatus))
					PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.NEW);
			}

			saveList.add(bean);
			boolean isHtml = halfwidthToFullwidthFalse.contains(getPersistentClass().getSimpleName())
					|| texthtmlMap.keySet().contains(getPersistentClass().getSimpleName());
			boolean halfwidthToFullwidth = !isHtml;
			logger.debug("halfwidthToFullwidth:" + halfwidthToFullwidth);
			for (Object savingBean : saveList)
				if (savingBean != null)
					if (!HqlStatement.class.isInstance(savingBean))
						defaultValue(savingBean);

			String daoMsg = "";
			if (StringUtils.isBlank(daoMsg)) {
				logger.info("save without billno rule");
				daoMsg = cloudDao.save(sf(), saveList.toArray(), halfwidthToFullwidth, null);
			}

			if (!daoMsg.equals(SUCCESS)) {
				// PropertyUtils.setProperty(bean, OD, newOperationdate);
				addActionError(daoMsg);
				return false;
			}
			pk = (String) PropertyUtils.getProperty(bean, PK);

			// setEditObject(bean);
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		addActionMessage(getText(I18N_SAVE_SUCCESS));
		resetDataMap(getPersistentClass());
		for (DetailInfo detailInfo : getDetailInfoMap().values())
			resetDataMap(detailInfo.getDetailClass());
		return true;
	}

	/**
	 * to save success/error page
	 * 
	 * @return EDIT
	 */
	public final String save() {
		if (!executeSave())
			return EDIT_ERROR;

		if (isChangeLocale) {
			try {
				if (hasErrors()) {
					List<String> mainPageErrors = new ArrayList<String>();
					if (hasActionErrors())
						for (String e : getActionErrors()) {
							mainPageErrors.add(e);
						}
					if (hasFieldErrors())
						for (String key : getFieldErrors().keySet()) {
							for (String e : getFieldErrors().get(key)) {
								mainPageErrors.add(key + ":" + e);
							}
						}
					sessionSet("mainPageErrors", mainPageErrors);
				}
				if (hasActionMessages()) {
					List<String> mainPageMsgs = new ArrayList<String>();
					for (String m : getActionMessages()) {
						mainPageMsgs.add(m);
					}
					sessionSet("mainPageMsgs", mainPageMsgs);
				}

				String pk = (String) PropertyUtils.getProperty(bean, PK);
				redirectPage = getActionKey() + "_edit?bean.sysid=" + pk;
				String lan = request.getParameter("refreshLan1");
				logger.debug("lan:" + lan);
				if (StringUtils.isNotBlank(lan)) {
					// redirectPage += "&refreshLan=" + lan;

					// cookiesMap.put("tw.com.mitac.petInspect.language", lan);
					Cookie cookie = new Cookie(getCookieLanKey(), StringUtils.defaultString(lan));
					// 設置Cookie的生命周期
					cookie.setMaxAge(60 * 60 * 24 * 365);
					// cookie.setDomain(".petInspect.mitac.com.tw");
					response.addCookie(cookie);
				}
				return REDIRECT_PAGE;
			} catch (Exception e) {
			}
		}

		return EDIT_SUCCESS();
	}

	public String saveAndNew() {
		if (!executeSave())
			return EDIT_ERROR;

		if (isChangeLocale) {
			redirectPage = getActionKey() + "_edit";
			return REDIRECT_PAGE;
		}

		String result = EDIT_SUCCESS();
		try {
			bean = (MO) ConstructorUtils.invokeConstructor(getPersistentClass(), null);
			beaninfo = new HashMap<String, String>();
		} catch (Exception e) {
			logger.error(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}
		edit();
		return result;
	}

	public String saveAndReturnMain() {
		if (!executeSave())
			return EDIT_ERROR;

		if (isChangeLocale) {
			if (hasErrors()) {
				List<String> mainPageErrors = new ArrayList<String>();
				if (hasActionErrors())
					for (String e : getActionErrors()) {
						mainPageErrors.add(e);
					}
				if (hasFieldErrors())
					for (String key : getFieldErrors().keySet()) {
						for (String e : getFieldErrors().get(key)) {
							mainPageErrors.add(key + ":" + e);
						}
					}
				sessionSet("mainPageErrors", mainPageErrors);
			}
			if (hasActionMessages()) {
				List<String> mainPageMsgs = new ArrayList<String>();
				for (String m : getActionMessages()) {
					mainPageMsgs.add(m);
				}
				sessionSet("mainPageMsgs", mainPageMsgs);
			}
			return TO_MAIN;
		}

		return main();
	}

	/**
	 * 頁面不跳轉的儲存 (開發階段)
	 */
	public String ajaxCrudSave() {
		resultMap = new HashMap();
		resultMap.put("isSuccess", executeSave());
		resultMap.put("bean", bean);
		resultMap.put("beaninfo", beaninfo);

		StringBuilder msg = new StringBuilder();
		if (hasErrors()) {
			if (hasActionErrors())
				for (String e : getActionErrors()) {
					msg.append(e + "\n");
				}
			if (hasFieldErrors())
				for (String key : getFieldErrors().keySet()) {
					for (String e : getFieldErrors().get(key)) {
						msg.append(key + ":" + e + "\n");
					}
				}
		}
		if (hasActionMessages()) {
			for (String m : getActionMessages()) {
				msg.append(m + "\n");
			}
		}
		resultMap.put("msg", msg.toString());

		return JSON_RESULT;
	}

	public boolean getWithoutSaveBtn() {
		return new Boolean(Util.globalSetting().getString("crud.isSaveAndContinueBtnHide"));
	}

	public boolean getWithoutSaveAndNew() {
		return new Boolean(Util.globalSetting().getString("crud.isSaveAndNewBtnHide"));
	}

	public boolean getWithoutSaveAndReturnMain() {
		return new Boolean(Util.globalSetting().getString("crud.isSaveAndReturnMainBtnHide"));
	}

	// ---------- ---------- ---------- ---------- ----------
	public String delete() {
		try {
			String pk = (String) PropertyUtils.getProperty(bean, PK);
			if (StringUtils.isBlank(pk)) {
				// addActionMessage("無執行");
				return main();
			}
			logger.info("try to DELETE " + pk);
			Object[] arr = tryDeleteEnable(getPersistentClass(), pk, false);
			boolean isDeleteEnable = (Boolean) arr[0];
			List<Object> reportList = (List<Object>) arr[1];
			if (!isDeleteEnable && StringUtils.isNotBlank(getTreeParentKey())) {
				List<MO> subBecause = new ArrayList<MO>();
				List<Object> otherBecause = new ArrayList<Object>();
				for (Object object : reportList) {
					if (getPersistentClass().isInstance(object)) {
						Object parentValue = PropertyUtils.getProperty(object, getTreeParentKey());
						if (pk.equals(parentValue)) {
							subBecause.add((MO) object);
							continue;
						}
						logger.info("because:"
								+ ReflectionToStringBuilder.toString(object, ToStringStyle.MULTI_LINE_STYLE));
						otherBecause.add(object);
					}
				}
				if (subBecause.size() > 0 && otherBecause.size() == 0) {
					// 強制移除所有子結點
					List<BeanTreeNode> dataTree = createDataTree(getPersistentClass(), getTreeParentKey());
					BeanTreeNode beanTreeNode = TreeUtil.findNodeByEq(dataTree, PK, pk);
					List<BeanTreeNode> withSubList = TreeUtil.findWithSub(beanTreeNode.getSub());
					List alsoDeleteSysidList = new ArrayList<String>();
					for (BeanTreeNode alsoDeleteNode : withSubList) {
						logger.info("alsoDeleteNode:"
								+ ReflectionToStringBuilder.toString(alsoDeleteNode.getBean(),
										ToStringStyle.MULTI_LINE_STYLE));
						alsoDeleteSysidList.add(PropertyUtils.getProperty(alsoDeleteNode.getBean(), PK));
					}
					saveList.add(new DeleteStatement(getPersistentClass().getSimpleName(), new QueryGroup(
							new QueryRule(PK, IN, alsoDeleteSysidList))));
					isDeleteEnable = true;
				}
			}
			if (isDeleteEnable) {
				saveList.add(new DeleteStatement(getPersistentClass().getSimpleName(), new QueryGroup(new QueryRule(PK,
						pk))));
				for (DetailInfo di : getDetailInfoMap().values())
					saveList.add(new DeleteStatement(di.getDetailClass().getSimpleName(), new QueryGroup(new QueryRule(
							FK, pk))));

				if (getIsDataChangeLocale())
					for (String lan : languageTypeMap.keySet())
						saveList.add(new DeleteStatement(multiLanClassName(lan), new QueryGroup(new QueryRule(
								"sourceTable", getPersistentClass().getSimpleName()), new QueryRule("sourceSysid", pk))));

				boolean isHtml = halfwidthToFullwidthFalse.contains(getPersistentClass().getSimpleName())
						|| texthtmlMap.keySet().contains(getPersistentClass().getSimpleName());
				boolean halfwidthToFullwidth = !isHtml;
				logger.debug("halfwidthToFullwidth:" + halfwidthToFullwidth);
				for (Object savingBean : saveList)
					if (savingBean != null)
						if (!HqlStatement.class.isInstance(savingBean))
							defaultValue(savingBean);

				// 若有排序欄位
				if (getJqgridDefaultSoab()) {
					String msg = preDelete();
					if (!SUCCESS.equals(msg)) {
						addActionError(msg);
						return EDIT_ERROR;
					}
				}
				String daoMsg = "";
				if (StringUtils.isBlank(daoMsg)) {
					logger.info("save without billno rule");
					daoMsg = cloudDao.save(sf(), saveList.toArray(), halfwidthToFullwidth, null);
				}

				if (!daoMsg.equals(SUCCESS)) {
					// PropertyUtils.setProperty(bean, OD,
					// newOperationdate);
					addActionError(daoMsg);
					return EDIT_ERROR;
				}

				addActionMessage(getText(I18N_DELETE_SUCCESS));
				pk = (String) PropertyUtils.getProperty(bean, PK);

				if (getIsDataChangeLocale() && getIsImgChangeLocale()) {
					for (String lan : languageTypeMap.keySet()) {
						String subMainFilePath = getLanSavePath(lan) + pk + File.separator;
						File floder = new File(subMainFilePath);
						if (floder.exists())
							floder.delete();
					}
				} else {
					String subMainFilePath = getDfSavePath() + pk + File.separator;
					logger.debug("subMainFilePath:" + subMainFilePath);
					File floder = new File(subMainFilePath);
					logger.debug("floder.exists():" + floder.exists());
					if (floder.exists()) {
						boolean b = floder.delete();
						if (b) {
							logger.info("刪除資料夾成功");
						} else {
							logger.warn("刪除資料夾失敗:" + subMainFilePath);
						}
					}
				}

				resetDataMap(getPersistentClass());
				for (DetailInfo detailInfo : getDetailInfoMap().values())
					resetDataMap(detailInfo.getDetailClass());

				if (isChangeLocale) {
					if (hasErrors()) {
						List<String> mainPageErrors = new ArrayList<String>();
						if (hasActionErrors())
							for (String e : getActionErrors()) {
								mainPageErrors.add(e);
							}
						if (hasFieldErrors())
							for (String key : getFieldErrors().keySet()) {
								for (String e : getFieldErrors().get(key)) {
									mainPageErrors.add(key + ":" + e);
								}
							}
						sessionSet("mainPageErrors", mainPageErrors);
					}
					if (hasActionMessages()) {
						List<String> mainPageMsgs = new ArrayList<String>();
						for (String m : getActionMessages()) {
							mainPageMsgs.add(m);
						}
						sessionSet("mainPageMsgs", mainPageMsgs);
					}
					return TO_MAIN;
				}

				return main();
			} else {
				for (Object object : reportList) {
					logger.info("because:" + ReflectionToStringBuilder.toString(object, ToStringStyle.MULTI_LINE_STYLE));
				}
				addActionError(getText("errMsg.deleteFailedUsed"));
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return EDIT_ERROR;
		}
	}

	protected Object[] tryDeleteEnable(Class<?> deleteClass, Object deletePk, boolean isReport) throws Exception {
		boolean isDeleteEnable = true;
		List<Object> reportList = new ArrayList<Object>();
		List<String> propertyNameList = linkPropertyList(deleteClass);
		List<Class> classList = Util.getClasses(Util.beanPackage);
		for (Class<?> clazz : classList) {
			Field[] f = clazz.getDeclaredFields();
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (field.getType().equals(Set.class))
					continue;
				if (propertyNameList.contains(field.getName())) {
					try {
						List<?> list = cloudDao.queryTable(sf(), clazz, new QueryGroup(new QueryRule(field.getName(),
								deletePk)), new QueryOrder[0], null, null);
						if (list.size() > 0) {
							isDeleteEnable = false;
							reportList.addAll(list);
							if (!isReport)
								break;
						}
					} catch (org.hibernate.hql.internal.ast.QuerySyntaxException e) {
						logger.warn(e.getMessage());
					}
				}
			}
			if (!isReport && !isDeleteEnable)
				break;
		}
		return new Object[] { isDeleteEnable, reportList };
	}

	// ---------- ---------- ---------- ---------- ----------
	protected final Set<?> findDetailSetWhenEdit(String key) {
		if (getNewDetail()) {
			Set<Object> detailSet = (Set<Object>) request.getAttribute("EDIT_" + key);
			if (detailSet == null) {
				detailSet = new LinkedHashSet<Object>();
				request.setAttribute("EDIT_" + key, detailSet);

				try {
					Class<?> detailClass = detailClassInfo.get(key);
					List<Field> f2 = tw.com.mitac.ssh.util.Util.declaredFields(detailClass);

					JSONArray jsonArray = new JSONArray(beaninfo.get(key));

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Object detail = getDefaultDMO(detailClass);
						for (Field field2 : f2) {
							String str = jsonObject.getString(field2.getName());
							PropertyUtils.setProperty(detail, field2.getName(),
									strToDetailFieldValue(field2.getType(), field2.getName(), str));
						}
						Util.defaultPK(detail);
						defaultValue((Serializable) detail);
						detailSet.add(detail);

						// SN或ID自動產
						if (PropertyUtils.getPropertyDescriptor(detail, SN) != null) {
							snGenerator(detailSet, detail, SN);
						}
						if (PropertyUtils.getPropertyDescriptor(detail, ID) != null) {
							snGenerator(detailSet, detail, ID);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return detailSet;
		} else {
			Set<?> detailSet = (Set<?>) sessionGet(key);
			return detailSet;
		}
	}

	protected final Object strToDetailFieldValue(Class<?> clazz, String fieldName, String str) {
		if (str != null) {// 因為頁面有可能故意把原本有值的東西改成空字串丟回來，所以必須要是只判斷null(也就是當這個欄位是hidden時)
			str = str.trim();
			if (clazz.equals(Boolean.class)) {
				return new Boolean(str);
			} else if (clazz.equals(BigDecimal.class)) {
				// 顯示%
				if (fieldName.lastIndexOf("Rate") != -1) {
					return BigDecimalTypeConverter.convertFromString(str).divide(new BigDecimal(100), 8,
							BigDecimal.ROUND_HALF_UP);
				}
				return BigDecimalTypeConverter.convertFromString(str);
			} else if (clazz.equals(Long.class)) {
				return new Long(str);
			} else if (clazz.equals(Date.class)) {
				logger.debug("dateStr:" + str);
				return DateTypeConverter.convertFromString(str);
			} else if (clazz.equals(Time.class)) {
				logger.debug("timeStr:" + str);
				return TimeTypeConverter.convertFromString(str);
			} else if (clazz.equals(String.class)) {
				return str;
			}
		}
		return null;
	}

	/**
	 * 根據所指定的DETAIL_SET產生一個sequence，產出的值會是該DETAIL_SET中最大號碼的下一號
	 * 
	 * @param operateSet
	 *            尾檔當前列表資料
	 * @param propertyName
	 *            尾檔的BEAN中，存放Sequenceno的field名稱
	 * @param digit
	 *            需要幾位數的編碼
	 * @param plusDigit
	 *            數字後要補幾個零
	 * @return String
	 */
	protected static final String createSnBase(Collection operateSet, String propertyName, int digit, int plusDigit) {
		String startNO = "0";
		for (int i = 1; i < digit; i++) {
			startNO = startNO + "0";
		}
		String returnSN = startNO;
		try {
			for (Object operateBean : operateSet) {
				try {
					String competeSN = (String) PropertyUtils.getProperty(operateBean, propertyName); // 取得當前或給定SN
					if (StringUtils.isBlank(competeSN)) {
						competeSN = startNO;
					} else if (Integer.parseInt(competeSN) > Integer.parseInt(returnSN)) {
						returnSN = competeSN;
					}
				} catch (NumberFormatException e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.format("%0" + digit + "d", (Integer.parseInt(returnSN) + (int) Math.pow(10, plusDigit)));
	}

	/**
	 * 根據所指定的DETAIL_SET產生一個sequence，產出的值會是該DETAIL_SET中最大號碼的下一號
	 * 
	 * @param operateSet
	 *            尾檔當前列表資料
	 * @param propertyName
	 *            尾檔的BEAN中，存放Sequenceno的field名稱
	 * @return
	 */
	protected static final String createSn(Collection operateSet, String propertyName) {
		return createSnBase(operateSet, propertyName, 4, 1);
	}

	/**
	 * 對指定的欄位產出00?0的序號，第一個參數是收需要被產出序號的SET，第二個參數收的是需要序號的Object(BEAN) ，
	 * 第三個參數收的是要對什麼欄位產出序號(可能是SN或ID)；實際產生數字的不是這個method而是creatSn
	 * 
	 * @param operateSet
	 *            尾檔當前列表資料
	 * @param targetObj
	 * @param propertyName
	 */
	protected void snGenerator(Collection operateSet, Object targetObj, String propertyName) {
		try {
			// detail為取出的bean
			String sn = (String) PropertyUtils.getProperty(targetObj, propertyName); // sn為取出detail中的id或sequenceNo
			if (StringUtils.isBlank(sn)) {
				PropertyUtils.setProperty(targetObj, propertyName, createSn(operateSet, propertyName));
			}
			Map<String, String> snValidateMap = new LinkedHashMap<String, String>();
			for (Object operateBean : operateSet) {
				String operateSN = (String) PropertyUtils.getProperty(operateBean, propertyName);
				String snCounter = snValidateMap.get(operateSN);
				if (snCounter != null) {
					logger.debug("取得重複之尾檔序號:" + operateSN);
					PropertyUtils.setProperty(operateBean, propertyName, createSn(operateSet, propertyName));
				} else
					snValidateMap.put(operateSN, SUCCESS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * detail tool 產生尾檔初始值物件 需等application設定完成後執行
	 */
	protected final <HB> HB getDefaultDMO(Class<HB> clazz) {
		try {
			HB defaultDMO = (HB) org.apache.commons.beanutils.ConstructorUtils.invokeConstructor(clazz, null);
			Field[] f = clazz.getDeclaredFields();
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (field.getType().equals(Boolean.class)) {
					PropertyUtils.setProperty(defaultDMO, field.getName(), false);
					if (field.getName().equals(IS_ENABLED))
						PropertyUtils.setProperty(defaultDMO, field.getName(), true);
				} else if (field.getType().equals(BigDecimal.class)) {
					PropertyUtils.setProperty(defaultDMO, field.getName(), BigDecimal.ZERO);
				} else if (field.getType().equals(Long.class)) {
					PropertyUtils.setProperty(defaultDMO, field.getName(), 0L);
				} else if (field.getType().equals(Integer.class)) {
					PropertyUtils.setProperty(defaultDMO, field.getName(), 0);
				} else if (field.getType().equals(Date.class)) {
					if (!field.getName().equals(OD) && !field.getName().equals(CD)
							&& !dateDefaultNullList.contains(field.getName()))
						if (dateDefaultMaxList.contains(field.getName())) {
							Calendar cal = Calendar.getInstance();
							cal.set(Calendar.YEAR, 9999);
							cal.set(Calendar.MONTH, Calendar.DECEMBER);
							cal.set(Calendar.DAY_OF_MONTH, 31);
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							PropertyUtils.setProperty(defaultDMO, field.getName(), cal.getTime());
						} else {
							try {
								Method m = clazz.getMethod("get" + field.getName().substring(0, 1).toUpperCase()
										+ field.getName().substring(1));
								if (m.isAnnotationPresent(Column.class)) {
									Column column = m.getAnnotation(Column.class);
									int length = column.length();
									if (length == 19)
										PropertyUtils.setProperty(defaultDMO, field.getName(), systemDatetime);
									else
										PropertyUtils.setProperty(defaultDMO, field.getName(), systemDate);
								}
							} catch (Exception e) {
							}
						}
				} else if (field.getType().equals(String.class)) {
					if (getDataSysConstantIdMap(clazz).get(field.getName()) != null) {
						PropertyUtils.setProperty(defaultDMO, field.getName(),
								getDataSysConstantIdMap(clazz).get(field.getName()).getDefaultOption());
						if (getIsTest())
							logger.debug("default option: "
									+ getDataSysConstantIdMap(clazz).get(field.getName()).getDefaultOption());
					}
				} else if (field.getType().equals(Set.class)) {
					PropertyUtils.setProperty(defaultDMO, field.getName(), new LinkedHashSet());
				}
			}
			return defaultDMO;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// ---------- ---------- ---------- ---------- ----------
	public String turnToConfirmed() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (StringUtils.isBlank(billStatus) || BillStatusUtil.NEW.equals(billStatus)) {
				PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.CONFIRM);
			} else {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.confirm") }));
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	public String cancelConfirmed() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (StringUtils.isBlank(billStatus)
					|| (!BillStatusUtil.CONFIRM.equals(billStatus) && !BillStatusUtil.UNAPPROVED.equals(billStatus))) {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.cancelConfirm") }));
				return EDIT_ERROR;
			}
			PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.NEW);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	public String turnToUnapproved() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (BillStatusUtil.CONFIRM.equals(billStatus)) {
				PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.UNAPPROVED);
				String inputUnapprovedReason = request.getParameter("inputUnapprovedReason");
				if (PropertyUtils.getPropertyDescriptor(bean, "unapprovedReason") != null) {
					// String inputUnapprovedReason =
					// request.getParameter("inputUnapprovedReason");
					PropertyUtils.setProperty(bean, "unapprovedReason", inputUnapprovedReason);
				}
			} else {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.unapproved") }));
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	public String turnToApproved() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (BillStatusUtil.CONFIRM.equals(billStatus)) {
				PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.APPROVED);
			} else {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.approved") }));
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	public String cancelApproved() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (StringUtils.isBlank(billStatus) || !BillStatusUtil.APPROVED.equals(billStatus)) {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.cancelApproved") }));
				return EDIT_ERROR;
			}
			PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.CONFIRM);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	public String turnToInvalid() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (StringUtils.isBlank(billStatus) || !BillStatusUtil.NEW.equals(billStatus)) {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.invalid") }));
				return EDIT_ERROR;
			}
			PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.INVALID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	public String turnToFinish() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, BILL_STATUS) == null) {
				addActionError(getText("errMsg.withoutBillStatus"));
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, BILL_STATUS);
			if (BillStatusUtil.APPROVED.equals(billStatus)) {
				PropertyUtils.setProperty(bean, BILL_STATUS, BillStatusUtil.FINISH);
			} else {
				addActionError(getText("errMsg.wrongBillStatus", new String[] { getText("button.finish") }));
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	/**
	 * 將sysid轉換為dataId-name或billno
	 */
	protected void formatInfo() {
		try {
			Field[] f = getPersistentClass().getDeclaredFields();
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (!field.getType().equals(String.class))
					continue;
				if (field.getName().equals(PK))
					continue;

				try {
					String fieldName = field.getName();
					String value = (String) PropertyUtils.getProperty(bean, fieldName); // 取當前值，應該不可能noSuchMethod
					String infoShow = fieldName + "Show";

					String showText = "";
					if (StringUtils.isBlank(value)) {
					} else if (Util.INFO_STAR.equals(value)) {
						showText = Util.INFO_STAR;
					} else {

						// 單據/多筆資料
						if (StringUtils.isBlank(showText)) {
							String billName = "";
							try {
								billName = (String) billSysidMapping.getObject(fieldName);
							} catch (MissingResourceException e) {
							}
							if (StringUtils.isNotBlank(billName)) {
								Class<?> targetClass = Class.forName(Util.beanPackage + "." + billName);

								String displayFormat = DEFAULT_BILL_DISPLAY_FORMAT;
								try {
									displayFormat = tableToBillno.getString(targetClass.getSimpleName());
								} catch (MissingResourceException e1) {
								}

								List<String> queryColumns = new ArrayList<String>();
								queryColumns.add(PK);
								String displayCheck = displayFormat;
								while (displayCheck.indexOf("#") != -1) {
									int i1 = displayCheck.indexOf("#");
									int i2 = displayCheck.indexOf("#", i1 + 1);
									if (i2 == -1)
										break;
									String displayColumn = displayCheck.substring(i1 + 1, i2);
									queryColumns.add(displayColumn);
									displayCheck = displayCheck.replace(("#" + displayColumn + "#"), "");
								}

								List<Map> l = (List<Map>) cloudDao.findProperty(sf(), targetClass, new QueryGroup(
										new QueryRule(PK, value)), new QueryOrder[0], false, queryColumns
										.toArray(new String[0]));
								// List<String> l = (List<String>)
								// cloudDao.findProperty(sf(), targetClass,
								// new QueryGroup(new QueryRule(PK, value)), new
								// QueryOrder[0], false, BILLNO);
								if (l.size() > 0) {
									// String billno = l.get(0);
									// showText = billno;
									Map<String, String> map = l.get(0);
									String display = displayFormat;
									while (display.indexOf("#") != -1) {
										int i1 = display.indexOf("#");
										int i2 = display.indexOf("#", i1 + 1);
										if (i2 == -1)
											break;
										String displayColumn = display.substring(i1 + 1, i2);
										Object displayValue = map.get(displayColumn);
										if (displayValue == null)
											displayValue = "";
										display = display.replace(("#" + displayColumn + "#"), displayValue.toString());
									}
									display = tw.com.mitac.ssh.util.Util.halfwidthToFullwidth(display);
									showText = display;
								}
							}
						} // 單據/多筆資料

						if (StringUtils.isBlank(showText)) {
							String className = "";
							try {
								className = coreSysidMapping.getString(fieldName);
							} catch (MissingResourceException e) {
							}
							if (StringUtils.isNotBlank(className)) {
								Class<?> targetClass = Class.forName(Util.beanPackage + "." + className);

								Object target = createDataTable(targetClass).get(value);
								if (target != null) {
									String displayFormat = DEFAULT_DISPLAY_FORMAT;
									try {
										displayFormat = tableToDisplay.getString(className);
									} catch (MissingResourceException e1) {
									}
									String display = displayFormat;
									while (display.indexOf("#") != -1) {
										int i1 = display.indexOf("#");
										int i2 = display.indexOf("#", i1 + 1);
										if (i2 == -1)
											break;
										String displayColumn = display.substring(i1 + 1, i2);
										if (!isColumnHidden(className, displayColumn)) {
											Object displayValue = PropertyUtils.getProperty(target, displayColumn);
											if (displayValue == null)
												displayValue = "";
											display = display.replace(("#" + displayColumn + "#"),
													displayValue.toString());
										}
									}
									display = tw.com.mitac.ssh.util.Util.halfwidthToFullwidth(display);
									showText = display;

								}
							}
						}

					}
					beaninfo.put(infoShow, showText);
				} catch (NoSuchMethodException e) {
				}
			}
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean getIsColumnUniqueByDataId() {
		return false;
	}

	public String getValidateSave() {
		logger.debug("start");
		String target = "";
		String key = getActionType() + RESOURCE_SPLIT + PREFIX + RESOURCE_SPLIT + APP_VALIDATE_SAVE;
		target = (String) appMap().get(key);
		if (StringUtils.isBlank(target)) {
			try {
				Map map = new LinkedHashMap();

				Map<String, SysBillnomanagement> sbMap = getDataSysBillnomanagementResourceMap().get(
						getPersistentClass().getSimpleName());
				if (sbMap == null)
					sbMap = new LinkedHashMap<String, SysBillnomanagement>();

				Field[] f = getPersistentClass().getDeclaredFields();
				for (Field field : f) {
					if (Modifier.isStatic(field.getModifiers()))
						continue;
					if (field.getType().equals(Set.class))
						continue;
					Map ruleMap = new LinkedHashMap();

					String fieldName = getText("bean." + field.getName());

					if (field.getType().equals(BigDecimal.class)) {
						ruleMap.put("numberBigZero", new Object[] { true, fieldName });
					} else if (field.getType().equals(Long.class)) {
						ruleMap.put("digitsParam", new Object[] { true, fieldName });
					} else if (field.getType().equals(Date.class)) {
						ruleMap.put("dateOrDateTime", new Object[] { true, fieldName });
					} else if (field.getType().equals(Time.class)) {
						// TODO 在jquery.validate-1.15.1.js加入純時間的驗證 [hh:mm] or
						// [hh:mm:ss]
						// ruleMap.put("dateOrDateTime", new Object[] {
						// true, fieldName });
					}

					if (!Arrays.asList(DATA_LOG_MEMBER).contains(field.getName())) {
						Map<String, Object> fieldMap = getDeclaredFieldMap().get(field.getName());
						boolean columnNullable = (boolean) fieldMap.get("columnNullable");
						if (sbMap.get(field.getName()) != null)
							columnNullable = true;

						/*
						 * 2016-06-09 ckeditor使用iframe->被迫忽略
						 */
						Boolean isColumnTexthtml = (Boolean) fieldMap.get("isColumnTexthtml");
						if (isColumnTexthtml == null)
							isColumnTexthtml = false;

						if (!columnNullable && !isColumnTexthtml) {
							ruleMap.put("requiredParam", new Object[] { true, fieldName });
						}

						// int columnLength = (int)
						// fieldMap.get("columnLength");
						// ruleMap.put("maxlength", columnLength);
					}

					if (!isColumnInfoStar(getPersistentClass().getSimpleName(), field.getName()))
						ruleMap.put("escapeInfoStar", new Object[] { true, fieldName });

					// if (ruleMap.size() > 0)
					map.put("bean." + field.getName(), ruleMap);
				}

				JSONObject jsonObject = new JSONObject(map);
				target = jsonObject.toString();
			} catch (Exception e) {
				e.printStackTrace();
				if (StringUtils.isBlank(target))
					target = dafaultJsonObj;
			}
			if (isChangeLocale)
				appMap().put(key, target);
		}
		logger.debug("end");
		return target;
	}

	public String getValidateMsg() {
		String target = "";
		String key = getActionType() + RESOURCE_SPLIT + PREFIX + RESOURCE_SPLIT + APP_VALIDATE_MSG;
		target = (String) application.get(key);
		if (StringUtils.isBlank(target)) {
			Map map = new LinkedHashMap();

			if (getIsColumnUniqueByDataId()) {
				try {
					Field field = getPersistentClass().getDeclaredField(ID);
					Map msgMap = new LinkedHashMap();
					String fieldName = getText("bean." + ID);
					// use i18n
					// SysColumnConfig sysColumnConfig =
					// getCrudColumnConfig().get(field.getName());
					// if (sysColumnConfig != null)
					// fieldName = sysColumnConfig.getColumnName();
					// else
					// logger.warn("[" + field.getName() +
					// "] sysColumnConfig is null");
					msgMap.put("remote", getText("errMsg.repeat", new String[] { fieldName }));
					map.put("bean." + ID, msgMap);
				} catch (NoSuchFieldException e) {
				}
			} else {
				Field[] f = getPersistentClass().getDeclaredFields();
				for (Field field : f)
					if (!Modifier.isStatic(field.getModifiers())) {
						if (field.getType().equals(Set.class))
							continue;
						Map msgMap = new LinkedHashMap();
						String fieldName = getText("bean." + field.getName());
						Map<String, Object> fieldMap = getDeclaredFieldMap().get(field.getName());
						if (!PK.equals(field.getName())) {
							boolean columnUnique = (Boolean) fieldMap.get("columnUnique");
							if (columnUnique) {
								msgMap.put("remote", getText("errMsg.repeat", new String[] { fieldName }));
							}
						}
						if (msgMap.size() > 0)
							map.put("bean." + field.getName(), msgMap);
					}
			}

			JSONObject jsonObject = new JSONObject(map);
			target = jsonObject.toString();

			if (StringUtils.isBlank(target))
				target = dafaultJsonObj;
			if (isChangeLocale)
				application.put(key, target);
		}
		return target;
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * clean session resource
	 * 
	 * @return INIT
	 */
	public String init() {
		setQueryBeaninfo(null);
		setQueryCondition(null);
		// setAndQueryRulesHistory(null);
		// setAndQueryGroupsHistory(null);
		sessionRemove("jqgridCrudSearch");
		sessionRemove("jqgridCrudFilters");
		return TO_MAIN;
	}

	/**
	 * to main page
	 * 
	 * @return MAIN
	 */
	public String main() {
		request.setAttribute("mainPageErrors", sessionRemove("mainPageErrors"));
		request.setAttribute("mainPageMsgs", sessionRemove("mainPageMsgs"));
		beaninfo = getQueryBeaninfo();
		return MAIN;
	}

	/**
	 * to find page
	 * 
	 * @return MAIN
	 */
	public String find() {
		setQueryBeaninfo(beaninfo);
		// setQueryCondition(createQueryCondition());
		try {
			if (StringUtils.isBlank(andQueryRulesStr))
				andQueryRulesStr = "[]";
			JSONArray andQueryRulesArr = new JSONArray(andQueryRulesStr);

			List<QueryRule> andQueryRuleList = QueryGroupUtil.jqgridRulesJSONArrayToQueryRuleList(getCrudJqgridClass(),
					andQueryRulesArr);

			QueryRule[] andQueryRules = andQueryRuleList.toArray(new QueryRule[0]);

			if (StringUtils.isBlank(andQueryGroupsStr))
				andQueryGroupsStr = "[]";
			JSONArray andQueryGroupsArr = new JSONArray(andQueryGroupsStr);
			List<QueryGroup> andQueryGroupsList = new ArrayList<QueryGroup>();
			for (int i = 0; i < andQueryGroupsArr.length(); i++) {
				JSONObject group = andQueryGroupsArr.getJSONObject(i);
				andQueryGroupsList.add(QueryGroupUtil.jqgridGroupJSONObjectToQueryGroup(getCrudJqgridClass(), group));
			}

			andQueryGroupsList.add(createQueryCondition());

			QueryGroup[] andQueryGroups = andQueryGroupsList.toArray(new QueryGroup[0]);

			setQueryCondition(new QueryGroup(AND, andQueryRules, andQueryGroups));
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return TO_MAIN;
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * 開窗模組:執行目標
	 * 
	 * @return
	 */
	public String getWindowTarget() {
		return (String) sessionGet("windowTarget");
	}

	/**
	 * 開窗模組:執行目標
	 * 
	 * @return
	 */
	public void setWindowTarget(String windowTarget) {
		sessionSet("windowTarget", windowTarget);
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * 測試識別代碼是否重複
	 * 
	 * @return
	 */
	public String tryid() {
		if (isShowFlow)
			logger.debug("start");
		boolean b = false;
		try {
			List<QueryRule> ruleList = new ArrayList<QueryRule>();
			Object pk = PropertyUtils.getProperty(bean, PK);
			Object columnValue = PropertyUtils.getProperty(bean, ID);
			ruleList.add(new QueryRule(PK, NE, pk));
			ruleList.add(new QueryRule(ID, columnValue));
			logger.debug("pk:" + pk + " id:" + columnValue);

			try {
				String siteSysid = (String) PropertyUtils.getProperty(bean, "siteSysid");
				if (StringUtils.isNotBlank(siteSysid))
					ruleList.add(new QueryRule("siteSysid", siteSysid));
			} catch (NoSuchMethodException e) {
			}

			int count = cloudDao.queryTableCount(sf(), getPersistentClass(),
					new QueryGroup(ruleList.toArray(new QueryRule[0])));
			if (count > 0)
				b = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isShowFlow)
			logger.debug("end");
		return renderText(String.valueOf(!b));
	}

	/**
	 * 測試欄位是否重複
	 * 
	 * @return
	 */
	public String tryColumnUnique() {
		if (isShowFlow)
			logger.debug("start");
		boolean b = false;
		try {
			List<QueryRule> ruleList = new ArrayList<QueryRule>();
			Object pk = PropertyUtils.getProperty(bean, PK);
			String columnId = request.getParameter("columnId");
			Object columnValue = PropertyUtils.getProperty(bean, columnId);
			ruleList.add(new QueryRule(PK, NE, pk));
			ruleList.add(new QueryRule(columnId, columnValue));
			logger.debug("pk:" + pk + " [" + columnId + "]:" + columnValue);

			try {
				String siteSysid = (String) PropertyUtils.getProperty(bean, "siteSysid");
				if (StringUtils.isNotBlank(siteSysid))
					ruleList.add(new QueryRule("siteSysid", siteSysid));
			} catch (NoSuchMethodException e) {
			}

			int count = cloudDao.queryTableCount(sf(), getPersistentClass(),
					new QueryGroup(ruleList.toArray(new QueryRule[0])));
			if (count > 0)
				b = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isShowFlow)
			logger.debug("end");
		return renderText(String.valueOf(!b));
	}

	// ---------- ---------- ---------- ---------- ----------
	public static String dafaultJsonObj = "{}";

	/**
	 * [jqgrid]
	 * 
	 * @param clazz
	 * @param field
	 * @param columnid
	 * @param sysColumnConfig
	 * @param systemSysColumnConfig
	 * @return
	 */
	protected Map<String, Object> getColModelMap(Class<?> clazz, Field field, String columnid, String columnname,
			SysColumnConfig sysColumnConfig, SysColumnConfig systemSysColumnConfig) {
		Map<String, Object> colModelMap = new HashMap<String, Object>();
		try {
			colModelMap.put("name", columnid);
			colModelMap.put("index", columnid);
			colModelMap.put("align", getJqgridTextAlign());
			colModelMap.put("sortable", true); // sql order
			colModelMap.put("editable", true);
			colModelMap.put("label", columnname);
			colModelMap.put("width", sysColumnConfig.getWidth().intValue());

			Map<String, Object> editoptionsMap = new HashMap<String, Object>();
			colModelMap.put("editoptions", editoptionsMap);
			Map<String, Object> editrulesMap = new HashMap<String, Object>();
			colModelMap.put("editrules", editrulesMap);
			Map<String, Object> searchoptionsMap = new HashMap<String, Object>();
			colModelMap.put("searchoptions", searchoptionsMap);
			searchoptionsMap.put("dataEvents", new Object[0]);

			// Object defaultValue =
			// PropertyUtils.getProperty(defaultObj,
			// field.getName());
			// if (defaultValue != null)
			// editoptionsMap.put("defaultValue", defaultValue);

			/**
			 * 告知宣告型別可自動產生日曆模組 非jqgrid
			 */
			colModelMap.put("javaClass", field.getType().getSimpleName());

			// 避免使用 getDeclaredFieldMap() 因為這隻僅限頭檔
			try {
				Method m = clazz.getMethod("get" + field.getName().substring(0, 1).toUpperCase()
						+ field.getName().substring(1));
				if (m.isAnnotationPresent(Column.class)) {
					Column column = m.getAnnotation(Column.class);
					int length = column.length();
					colModelMap.put("columnLength", length);

					if (!column.nullable())
						editrulesMap.put("required", true);
				}
			} catch (Exception e) {
			}

			if (field.getType().equals(BigDecimal.class)) {
				editrulesMap.put("number", true);
				searchoptionsMap.put("sopt", new String[] { EQ, NE, LT, LE, GT, GE });
				colModelMap.put("align", getJqgridNumberAlign());
			} else if (field.getType().equals(Long.class)) {
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
				if ("typesourcesysid".equals(field.getName())) {
					searchoptionsMap.put("sopt", new String[] { BW, BN, CN, NC, EQ, NE });
				} else {
					searchoptionsMap.put("sopt", new String[] { BW, BN, CN, NC, EQ, NE, LT, LE, GT, GE });
				}
			}
			// 顯示%
			if (field.getType().equals(BigDecimal.class) && (field.getName().lastIndexOf("Rate") != -1)) {
				colModelMap.put("formatter", "currencyFmatterPercent");
				colModelMap.put("align", "right");
			}

			String className = "";
			try {
				className = coreSysidMapping.getString(field.getName());
			} catch (MissingResourceException e) {
			}
			if (StringUtils.isNotBlank(className)) {
				try {
					Class<?> targetClass = Class.forName(Util.beanPackage + "." + className);
					// editoptionsMap.put("value", createDataMenu(targetClass));
					// 如果查出來的TABLE是沒有資料的，頁面會顯示object
					/**
					 * 定義開窗模組 非jqgrid
					 */
					colModelMap.put("selectTool", className);
				} catch (ClassNotFoundException e) {
				}
			}
			String billName = "";
			try {
				billName = (String) billSysidMapping.getObject(field.getName());
			} catch (MissingResourceException e) {
			}
			if (StringUtils.isNotBlank(billName)) {
				try {
					Class<?> targetClass = Class.forName(Util.beanPackage + "." + billName);
					// editoptionsMap.put("value", createDataMenu(targetClass));
					// 如果查出來的TABLE是沒有資料的，頁面會顯示object
					/**
					 * 定義開窗模組 非jqgrid
					 */
					colModelMap.put("billTool", billName);
				} catch (ClassNotFoundException e) {
				}
			}

			if (StringUtils.isBlank(className) && StringUtils.isBlank(billName)) {
				if (field.getName().contains("sysid") && field.getName().indexOf("sysid") > 0
						&& field.getName().indexOf("sysid") + 5 == field.getName().length()) {
					logger.debug("取得不存在properties中的field： " + field.getName());
				}
			}

			if (isColumnTexthtml(clazz.getSimpleName(), columnid))
				colModelMap.put("isColumnTexthtml", true);

			Boolean FIELD_ALLOW = true;
			if (Util.isLogin) {
				Boolean FIELD_ALL_ALLOW = (Boolean) request.getAttribute("FIELD_ALL_ALLOW");
				FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALL_ALLOW);
				if (!FIELD_ALLOW) {
					FIELD_ALLOW = (Boolean) request.getAttribute("FIELD_" + field.getName());
					FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALLOW);
				}
			}

			boolean ishidden = systemSysColumnConfig.getIshidden();
			if (!ishidden && new Boolean(Util.globalSetting().getString("crud.isDataLogHide"))
					&& Arrays.asList(DATA_LOG_MEMBER).contains(field.getName()))
				ishidden = true;
			if (ishidden || !FIELD_ALLOW) {
				if (!getIsTest()) {
					colModelMap.put("hidden", true);
					colModelMap.put("hidedlg", true);
				}
				editoptionsMap.put("readonly", "readonly");
				colModelMap.put("search", false);
			} else if (sysColumnConfig.getIshidden()) {
				colModelMap.put("hidden", true);
				editoptionsMap.put("readonly", "readonly");
				// colModelMap.put("search", false);
			}

			if (SN.equals(field.getName())) {
				if (!getIsShowSequence()) {
					if (!getIsTest()) {
						colModelMap.put("hidden", true);
						colModelMap.put("hidedlg", true);
					}
					editoptionsMap.put("readonly", "readonly");
					colModelMap.put("search", false);
				}
			}

			/*
			 * frozen 與 hidden 不可同時成立 hidden 成立時 frozen 必不成立
			 */
			if (sysColumnConfig.getIsfrozen()
			// || sysColumnConfig.getIshidden()
			)
				colModelMap.put("frozen", true);

			if (BILL_STATUS.equals(field.getName())) {
				editoptionsMap.put("value", getWfStatusMap());
			}
			// else if (Util.JWSSTATUS.equals(field.getName())) {
			// editoptionsMap.put("value", getEfStatusMap());
			// } else if (Util.VERSIONS.equals(field.getName())) {
			// colModelMap.put("editable", false);
			// }
			// // sysid menu
			// else if ("taxtypesysid".equals(field.getName())) {
			// editoptionsMap.put("value", getDataCoreTaxtypeMenu());
			// } else if (Util.SOURCE_TYPE.equals(field.getName())) {
			// // 折讓單尾檔開窗
			// colModelMap.put("editable", false);
			// Map<String, String> menu = new LinkedHashMap<String, String>();
			// menu.put("OTHER", "其他");
			// menu.putAll(getBillMenu());
			// editoptionsMap.put("value", menu);
			// } else if (CoreEntity.class.getSimpleName().equals(className)
			// // "entitysysid".equals(field.getName())
			// ) {
			// editoptionsMap.put("value", getMyEntityMenu());
			// } else if (CoreSite.class.getSimpleName().equals(className)
			// // "sitesysid".equals(field.getName())
			// ) {
			// editoptionsMap.put("value", getMySiteMenu());
			// } else if
			// (CoreDepartment.class.getSimpleName().equals(className)) {
			// editoptionsMap.put("value", getMyDepartmentMenu());
			// } else if (CoreWarehouse.class.getSimpleName().equals(className))
			// {
			// // 只能查出有營運點權限之倉庫資料
			// Map<String, CoreWarehouse> dataMap =
			// createDataTable(CoreWarehouse.class);
			// Map<String, String> menu = new LinkedHashMap<String, String>();
			// for (CoreWarehouse accessWare : dataMap.values())
			// if (getMySiteStr().contains(accessWare.getSitesysid()))
			// menu.put(accessWare.getSysid(), accessWare.getDataId() + "-" +
			// accessWare.getName());
			// editoptionsMap.put("value", menu);
			// }

			// if (editrulesMap.get("required") != null
			// &&
			// String.valueOf(true).equals(String.valueOf(editrulesMap.get("required"))))
			// colModelMap.put("label", "*" + colModelMap.get("label"));

			if (colModelMap.get("width") == null)
				colModelMap.put("width", dafaultWidthByLength * ((String) colModelMap.get("label")).length());

			// XXX 是否統一設置於createDataMenu
			Map<String, String> menu = (Map<String, String>) editoptionsMap.get("value");
			if (menu != null && menu.size() > 0) {
				colModelMap.put("edittype", "select");
				colModelMap.put("formatter", "select");

				Map<String, String> newMenu = new LinkedHashMap<String, String>();
				newMenu.put("", "...");

				// ----- info star -----
				boolean isColumnInfoStar = isColumnInfoStar(clazz.getSimpleName(), field.getName());
				if (isColumnInfoStar) {
					newMenu.put(Util.INFO_STAR, Util.INFO_STAR);
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
					newMenu2.put("", "...");
					if (isColumnInfoStar)
						newMenu2.put(Util.INFO_STAR, Util.INFO_STAR);
					if (false
					// Util.STATUS.equals(field.getName())
					) {
						// newMenu2.put(StatusUtil.PENDING_STATUS,
						// getText("billStatus.pending"));
						// newMenu2.put(StatusUtil.CLOSED_STATUS,
						// getText("billStatus.closed"));
						// newMenu2.put(StatusUtil.INVALID_STATUS,
						// getText("billStatus.invalid"));
						// searchoptionsMap.put("sopt", new String[] { IN });
					} else {
						newMenu2.putAll(menu);
						searchoptionsMap.put("sopt", new String[] { EQ, NE });
					}

					searchoptionsMap.put("value", newMenu2);
				} else {
					searchoptionsMap.put("sopt", new String[] { CN, BW, EW, EQ });
					// 所有否定型語法皆有問題
					// searchoptionsMap.put("sopt", new String[] { BW, BN, CN,
					// NC, EQ, NE });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colModelMap;
	}

	/**
	 * [jqgrid]依ColumnConfig產生前端欄位宣告
	 * 
	 * @see #createColumnConfigMap(Class)
	 */
	protected Map<String, Map> getJqgridColModelMap(Class<?> clazz, String i18nKey) {
		Boolean ACTION_SAVE = (Boolean) request.getAttribute("ACTION_SAVE");
		Boolean FIELD_ALL_ALLOW = (Boolean) request.getAttribute("FIELD_ALL_ALLOW");
		Map<String, Map> jqgridColModelMap = new LinkedHashMap<String, Map>();
		sessionSet("jqgridColModelMap", jqgridColModelMap);
		try {
			Map<String, SysColumnConfig> systemColumnConfigMap = createSystemColumnConfigMap(clazz, i18nKey);
			// Map<String, SysColumnConfig> userColumnConfigMap =
			// createUserColumnConfigMap(clazz);
			Map<String, SysColumnConfig> columnConfigMap = getColumnConfigCreator().createColumnConfigMap(clazz,
					createOperatorValue());
			if ("alter".equals(actionType)) {
				String[] alterArr = { BILLNO, OD, OP };
				for (String columnid : alterArr)
					if (columnConfigMap.get(columnid) != null)
						jqgridColModelMap.put(columnid, null);
			}
			// 先制排序
			for (SysColumnConfig sysColumnConfig : columnConfigMap.values()) {
				jqgridColModelMap.put(sysColumnConfig.getColumnId(), null);
				jqgridColModelMap.put(sysColumnConfig.getColumnId() + "Element", null);
			}
			Field[] f = clazz.getDeclaredFields();
			// Object defaultObj = getDefaultDMO(clazz);
			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (!field.getType().equals(Set.class)) {

					String key = field.getName();
					String columnId = field.getName();
					SysColumnConfig sysColumnConfig = columnConfigMap.get(key);
					SysColumnConfig systemSysColumnConfig = systemColumnConfigMap.get(key);

					// String columnName = sysColumnConfig.getColumnName();
					String columnName = getText(field.getName());
					if (StringUtils.isNotBlank(i18nKey)) {
						columnName = getText(i18nKey + "." + field.getName());
						if (columnName.equals(i18nKey + "." + field.getName())) {
							columnName = getText("bean." + field.getName());
						}
					}

					Map<String, Object> colModelMap = getColModelMap(clazz, field, columnId, columnName,
							sysColumnConfig, systemSysColumnConfig);
					jqgridColModelMap.put(columnId, colModelMap);

					Boolean FIELD_ALLOW = true;
					if (Util.isLogin) {
						FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALL_ALLOW);
						if (!FIELD_ALLOW) {
							FIELD_ALLOW = (Boolean) request.getAttribute("FIELD_" + field.getName());
							FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALLOW);
						}
					}
					boolean isColumnReadonly = isColumnReadonly(clazz.getSimpleName(), field.getName());
					boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), field.getName());
					if (BooleanUtils.isTrue(ACTION_SAVE) && FIELD_ALLOW && !isColumnReadonly && !isColumnHidden) {
						if (field.getType().equals(Boolean.class)) {
							Map<String, Object> btnColModelMap = new HashMap<String, Object>();
							jqgridColModelMap.put(columnId + "Element", btnColModelMap);
							btnColModelMap.put("name", columnId + "Element");
							btnColModelMap.put("index", columnId + "Element");
							String lable = getText("element." + columnId);
							if (StringUtils.equals(lable, "element." + columnId))
								lable = (String) colModelMap.get("label");
							btnColModelMap.put("label", lable);
							btnColModelMap.put("align", "center");
							btnColModelMap.put("sortable", false);
							btnColModelMap.put("width", 80);
							btnColModelMap.put("search", false);
							Map<String, Object> btnSearchoptionsMap = new HashMap<String, Object>();
							btnSearchoptionsMap.put("searchhidden", true);
							btnColModelMap.put("searchoptions", btnSearchoptionsMap);

							colModelMap.put("hidden", true);
							colModelMap.put("hidedlg", true);
							colModelMap.put("search", true);
							Map<String, Object> searchoptionsMap = (Map<String, Object>) colModelMap
									.get("searchoptions");
							searchoptionsMap.put("searchhidden", true);
						}
					}

					String selectTool = (String) colModelMap.get("selectTool");
					String billTool = (String) colModelMap.get("billTool");
					if (StringUtils.isNotBlank(selectTool) || StringUtils.isNotBlank(billTool)) {
						Map<String, Object> linkMap = new HashMap<String, Object>();
						linkMap.put("name", columnId + "Link");
						linkMap.put("index", columnId + "Link");
						linkMap.put("hidden", true);
						linkMap.put("hidedlg", true);
						jqgridColModelMap.put(columnId + "Link", linkMap);
					}
				} else {
					Class<?> joinClass = findDetailInfoByResource(field.getName()).getDetailClass();
					String t2 = joinClass.getSimpleName().substring(0, 1).toLowerCase()
							+ joinClass.getSimpleName().substring(1);
					String d_i18nKey = field.getName().replace("Set", "");
					String preText = getText("bean." + field.getName()) + "-";
					Map<String, SysColumnConfig> joinSystemColumnConfigMap = createSystemColumnConfigMap(joinClass,
							d_i18nKey);
					Map<String, SysColumnConfig> joinColumnConfigMap = getColumnConfigCreator().createColumnConfigMap(
							joinClass, createOperatorValue());
					for (SysColumnConfig sysColumnConfig : joinColumnConfigMap.values())
						jqgridColModelMap.put(t2 + "_" + sysColumnConfig.getColumnId(), null);
					for (Field joinField : joinClass.getDeclaredFields()) {
						if (Modifier.isStatic(joinField.getModifiers()))
							continue;
						if (!joinField.getType().equals(Set.class)) {
							String key = joinField.getName();
							String columnid = t2 + "_" + joinField.getName();
							SysColumnConfig sysColumnConfig = joinColumnConfigMap.get(key);
							SysColumnConfig systemSysColumnConfig = joinSystemColumnConfigMap.get(key);

							// String columnName =
							// sysColumnConfig.getColumnName();
							String columnName = getText(joinField.getName());
							if (StringUtils.isNotBlank(d_i18nKey)) {
								columnName = getText(d_i18nKey + "." + joinField.getName());
								if (columnName.equals(d_i18nKey + "." + joinField.getName())) {
									columnName = getText("bean." + joinField.getName());
								}
							}

							String fullColumnName = preText + columnName;

							Map<String, Object> colModelMap = getColModelMap(joinClass, joinField, columnid,
									fullColumnName, sysColumnConfig, systemSysColumnConfig);
							jqgridColModelMap.put(columnid, colModelMap);
							Boolean hidden = (Boolean) colModelMap.get("hidden");
							if (hidden == null || !hidden) {
								colModelMap.put("hidden", true);
								colModelMap.put("hidedlg", true);// 妍彤表示
																	// 不會顯示在chooseColumn
								colModelMap.put("search", true);
								Map<String, Object> searchoptionsMap = (Map<String, Object>) colModelMap
										.get("searchoptions");
								searchoptionsMap.put("searchhidden", true);
							}
						}
					}
				}
			}
			// logger.debug("point e");
		} catch (Exception e) {
			logger.error(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		}
		// logger.debug("point z");
		return jqgridColModelMap;
	}

	/**
	 * [jqgrid]依ColumnConfig產生前端欄位宣告
	 */
	protected Map<String, Map> getJqgridColModelMap() {
		return getJqgridColModelMap(getCrudJqgridClass(), "bean");
	}

	public String getJqgridColModel() {
		if (isShowFlow)
			logger.debug("start");
		String targetStr = dafaultJsonObj;
		try {
			Map<String, Map> target = getJqgridColModelMap();
			logger.debug("target.size:" + target.size());
			JSONObject jsonObject = new JSONObject(target);
			targetStr = jsonObject.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isShowFlow)
			logger.debug("end");
		return targetStr;
	}

	/**
	 * [jqgrid] default sortable
	 * 
	 * @return
	 */
	public Boolean getJqgridDefaultSoab() {
		try {
			MO bean = (MO) ConstructorUtils.invokeConstructor(getCrudJqgridClass(), null);
			for (String column : new String[] { DATA_ORDER }) {
				if (PropertyUtils.getPropertyDescriptor(bean, column) != null) {
					boolean isColumnHidden = isColumnHidden(getCrudJqgridClass().getSimpleName(), column);
					if (!isColumnHidden)
						return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * [jqgrid] default sort name
	 * 
	 * @return
	 */
	public String getJqgridDefaultSidx() {
		try {
			MO bean = (MO) ConstructorUtils.invokeConstructor(getCrudJqgridClass(), null);
			for (String column : new String[] { DATA_ORDER, ID, NAME, BILLNO }) {
				if (PropertyUtils.getPropertyDescriptor(bean, column) != null) {
					boolean isColumnHidden = isColumnHidden(getCrudJqgridClass().getSimpleName(), column);
					if (!isColumnHidden)
						return column;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * [jqgrid] default sord
	 * 
	 * @return
	 */
	public String getJqgridDefaultSord() {
		try {
			if (StringUtils.equals(getJqgridDefaultSidx(), ID)) {
				return "asc";
			}
			if (StringUtils.equals(getJqgridDefaultSidx(), NAME)) {
				return "asc";
			}
			if (StringUtils.equals(getJqgridDefaultSidx(), BILLNO)) {
				return "desc";
			}
			if (StringUtils.equals(getJqgridDefaultSidx(), DATA_ORDER)) {
				return "asc";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "asc";
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String columnReset() {
		return columnReset(getCrudJqgridClass());
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	protected final String columnReset(Class<?> clazz) {
		resultString = getColumnConfigCreator().columnReset(clazz, createOperatorValue());
		return TO_MAIN;
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String columnFrozen() {
		return columnFrozen(getCrudJqgridClass());
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	protected final String columnFrozen(Class<?> clazz) {
		String frozenStr = "";
		String[] chooseFrozenColumns = request.getParameterValues("chooseFrozenColumns");
		if (chooseFrozenColumns != null)
			for (String string : chooseFrozenColumns)
				frozenStr += "," + string;
		if (StringUtils.isNotBlank(frozenStr))
			frozenStr = frozenStr.substring(1);
		resultString = getColumnConfigCreator().columnFrozen(clazz, frozenStr, createOperatorValue());
		return TO_MAIN;
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String ajaxRemapHidden() {
		return ajaxRemapHidden(getCrudJqgridClass());
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	protected final String ajaxRemapHidden(Class<?> clazz) {
		String remapStr = request.getParameter("remap");
		String hiddenStr = request.getParameter("hidden");
		resultString = getColumnConfigCreator().columnRemapHidden(clazz, remapStr, hiddenStr, createOperatorValue());
		return JSON_RESULT;
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	public String ajaxColumnResize() {
		return ajaxColumnResize(getCrudJqgridClass());
	}

	/**
	 * [jqgrid]
	 * 
	 * @return
	 */
	protected final String ajaxColumnResize(Class<?> clazz) {
		String columnid = request.getParameter("columnId");
		String width = request.getParameter("width");
		resultString = getColumnConfigCreator().columnResize(clazz, columnid, width, createOperatorValue());
		return JSON_RESULT;
	}

	/**
	 * treeType:B
	 * 
	 * @return
	 * @throws Exception
	 */
	public String ajaxTreeParentOrder() throws Exception {
		return ajaxTreeParentOrder(getCrudJqgridClass());
	}

	/**
	 * treeType:B
	 * 
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	protected final String ajaxTreeParentOrder(Class<?> clazz) throws Exception {
		boolean hasDataOrder = false;
		MO bean = (MO) ConstructorUtils.invokeConstructor(clazz, null);
		if (PropertyUtils.getPropertyDescriptor(bean, DATA_ORDER) != null) {
			hasDataOrder = true;
		}

		Map<String, Map> m = new HashMap<String, Map>();
		String[] paramArrayOfString = { PK, getTreeParentKey() };
		if (hasDataOrder)
			paramArrayOfString = new String[] { PK, getTreeParentKey(), DATA_ORDER };
		List<Map> l = (List<Map>) cloudDao.findProperty(sf(), clazz, QueryGroup.DEFAULT, null, false,
				paramArrayOfString);
		for (Map<String, Object> map : l) {
			String sysid = (String) map.get(PK);
			m.put(sysid, map);
		}

		String PointArry = request.getParameter("PointArry");
		JSONArray jsonArray = new JSONArray(PointArry);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String sysid = jsonObject.getString("sysid");
			int dataorder = jsonObject.getInt("dataorder");
			String parentKey = jsonObject.getString("parentKey");

			boolean theSame = true;
			if (theSame && !StringUtils.equals(parentKey, (String) m.get(sysid).get(getTreeParentKey())))
				theSame = false;
			if (theSame && hasDataOrder && !m.get(sysid).get(DATA_ORDER).equals(dataorder))
				theSame = false;
			if (!theSame) {
				Map<String, Object> setMap = getUpdatePropertyMap();
				if (hasDataOrder)
					setMap.put(DATA_ORDER, dataorder);
				setMap.put(getTreeParentKey(), parentKey);
				saveList.add(new UpdateStatement(clazz.getSimpleName(), new QueryGroup(new QueryRule(PK, sysid)),
						setMap));
				logger.debug("jsonObject:" + jsonObject);
			}
		}
		resultString = cloudDao.save(sf(), saveList);
		if (StringUtils.equals(SUCCESS, resultString))
			resetDataMap(clazz);
		return JSON_RESULT;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected Integer rows = 0;
	protected Integer page = 0;
	protected Integer total = 0;
	protected Integer record = 0;
	protected String sord;
	protected String sidx;
	protected boolean search;
	protected String filters;

	protected String oper;
	protected String id;

	public final Integer getRows() {
		return rows;
	}

	public final void setRows(Integer rows) {
		this.rows = rows;
	}

	public final Integer getPage() {
		return page;
	}

	public final void setPage(Integer page) {
		this.page = page;
	}

	public final Integer getTotal() {
		return total;
	}

	public final void setTotal(Integer total) {
		this.total = total;
	}

	public final Integer getRecord() {
		return record;
	}

	public final void setRecord(Integer record) {
		this.record = record;
	}

	public final String getSord() {
		return sord;
	}

	public final void setSord(String sord) {
		this.sord = sord;
	}

	public final String getSidx() {
		return sidx;
	}

	public final void setSidx(String sidx) {
		this.sidx = sidx;
	}

	public final boolean getSearch() {
		return search;
	}

	public final void setSearch(boolean search) {
		this.search = search;
	}

	public final String getFilters() {
		return filters;
	}

	public final void setFilters(String filters) {
		this.filters = filters;
	}

	public final String getOper() {
		return oper;
	}

	public final void setOper(String oper) {
		this.oper = oper;
	}

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	/**
	 * beaninfo to QueryGroup
	 * 
	 * @return
	 */
	protected QueryGroup createQueryCondition() {
		return QueryGroup.DEFAULT;
	}

	/**
	 * [jqgrid] resultMap.gridModel to excel 尾檔邏輯請參考DetailAction FIXME 邏輯太複雜
	 */
	protected void jqgridListOperExcel() {
		List<Map> formatToMapResults = (List<Map>) resultMap.get("gridModel");

		String fileLocation = FILE_DEFAULT_CREATE();
		String fileName = PREFIX + ".xlsx";
		String fileName2 = fileLocation + fileName;
		File dstFile = new File(fileLocation);
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

		// String headKey = PK;
		List<String> pkList = new ArrayList<String>();

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

		Field[] f = getCrudJqgridClass().getDeclaredFields();
		Map<String, Map> jqgridColModelMap = getJqgridColModelMap();

		for (String key : jqgridColModelMap.keySet())
			try {
				Field field = getCrudJqgridClass().getDeclaredField(key);
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				// if (ID.equals(key) || BILLNO.equals(key))
				// headKey = key;

				Map<String, Object> colModelMap = jqgridColModelMap.get(key);
				if (colModelMap == null)
					continue;

				Boolean hidden = (Boolean) colModelMap.get("hidden");
				if (hidden != null && hidden)
					continue;

				int width = (Integer) colModelMap.get("width");
				String label = (String) colModelMap.get("label");

				ExcelTitle excelTitle = new ExcelTitle();
				excelTitle.setKey(key);
				excelTitle.setLabel(label);
				excelTitle.setWidth(width / widthRate);
				excelTitle.setAlignment(CellStyle.ALIGN_LEFT);

				if (Long.class.equals(field.getType()) || BigDecimal.class.equals(field.getType()))
					excelTitle.setAlignment(CellStyle.ALIGN_RIGHT);
				titleList.add(excelTitle);
			} catch (NoSuchFieldException e) {
				if (key.endsWith("Link")) {
				} else if (key.startsWith(tw.com.mitac.ssh.util.Util.buildJoinTableFrontKey(getCrudJqgridClass()))) {
				} else {
					boolean isDetailKeySkip = false;
					for (DetailInfo detailInfo : getDetailInfoMap().values()) {
						if (StringUtils.startsWithIgnoreCase(key, detailInfo.getDetailClass().getSimpleName())) {
							isDetailKeySkip = true;
							break;
						}
					}
					if (!isDetailKeySkip) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		// 資料整理
		for (Map data : formatToMapResults) {
			String pk = (String) data.get(PK);
			pkList.add(pk);

			for (Field field : f) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				if (!field.getType().equals(Set.class)) {
					Map<String, Object> colModelMap = jqgridColModelMap.get(field.getName());
					if (colModelMap == null)
						continue;

					Boolean hidden = (Boolean) colModelMap.get("hidden");
					if (hidden != null && hidden)
						continue;

					Object dataValue = data.get(field.getName());
					String text = "";
					String javaClass = (String) colModelMap.get("javaClass");
					if (dataValue != null) {
						text = dataValue.toString();
						if (StringUtils.isNotBlank(text)) {
							Map<String, Object> editoptionsMap = (Map<String, Object>) colModelMap.get("editoptions");
							Map<String, String> menu = (Map<String, String>) editoptionsMap.get("value");
							if (menu != null) {
								text = menu.get(text);
								if (StringUtils.isBlank(text))
									if (Boolean.class.getSimpleName().equals(javaClass))
										text = menu.get(dataValue);
								data.put(field.getName(), text);
							}
						}
					}
				}
			}
		}
		excelCreator.execute();
		// ws.getSettings().setOrientation(PageOrientation.LANDSCAPE);//
		// 橫向列印
		writableSheetMap.put("", ws);

		// 尾檔
		for (Field field : f)
			if (!Modifier.isStatic(field.getModifiers())) {
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
					for (ExcelTitle excelTitle : titleList)
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
							// BILLNO.equals(field.getName()))
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
					List<?> detailList = cloudDao.queryTable(sf(), dClass, new QueryGroup(new QueryRule(Util.FK, IN,
							pkList)), new QueryOrder[] { new QueryOrder(FK), new QueryOrder(PK) }, null, null);
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
						String pk = (String) data.get(PK);
						Set<?> detailSet = detailSetMap.get(pk);
						if (detailSet == null)
							continue;

						// String headKeyText = (String) data.get(headKey);
						Map headKeyTextMap = new LinkedHashMap();
						for (Object key : data.keySet())
							headKeyTextMap.put("h_" + key, data.get(key));

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

	/**
	 * [jqgrid]dataId-name/billno like
	 * 
	 * @param queryGroup
	 * @return
	 */
	protected QueryGroup resetInfoQuery(QueryGroup queryGroup) {
		List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
		for (QueryRule queryRule : queryGroup.getRules())
			queryRuleList.add(queryRule);
		List<QueryGroup> queryGroupList = new ArrayList<QueryGroup>();
		for (QueryGroup group : queryGroup.getGroups())
			queryGroupList.add(resetInfoQuery(group));

		List<QueryRule> delQueryRuleList = new ArrayList<QueryRule>();
		List<QueryRule> addQueryRuleList = new ArrayList<QueryRule>();
		for (QueryRule queryRule : queryRuleList) {
			String fieldName = queryRule.getFieldName();

			String className = "";
			try {
				className = coreSysidMapping.getString(fieldName);
			} catch (MissingResourceException e) {
				String[] arr = fieldName.split("_");
				for (String string : arr)
					try {
						className = coreSysidMapping.getString(string);
						break;
					} catch (MissingResourceException e1) {
					}
			}
			String billName = "";
			try {
				billName = (String) billSysidMapping.getObject(fieldName);
			} catch (MissingResourceException e) {
				String[] arr = fieldName.split("_");
				for (String string : arr)
					try {
						billName = (String) billSysidMapping.getObject(string);
						break;
					} catch (MissingResourceException e1) {
					}
			}

			if (StringUtils.isBlank(className) && StringUtils.isBlank(billName))
				if (fieldName.contains("sysid") && fieldName.indexOf("sysid") > 0
						&& fieldName.indexOf("sysid") + 5 == fieldName.length()) {
					logger.debug("取得不存在properties中的field:" + fieldName);
				}

			if (StringUtils.isNotBlank(className)) {
				try {
					Class targetClass = Class.forName(Util.beanPackage + "." + className);
					delQueryRuleList.add(queryRule);

					List<QueryRule> displayQueryRuleList = new ArrayList<QueryRule>();

					String displayFormat = DEFAULT_DISPLAY_FORMAT;
					try {
						displayFormat = tableToDisplay.getString(className);
					} catch (MissingResourceException e1) {
					}
					String displayCheck = displayFormat;
					while (displayCheck.indexOf("#") != -1) {
						int i1 = displayCheck.indexOf("#");
						int i2 = displayCheck.indexOf("#", i1 + 1);
						if (i2 == -1)
							break;
						String displayColumn = displayCheck.substring(i1 + 1, i2);
						if (!isColumnHidden(className, displayColumn)) {
							displayQueryRuleList.add(new QueryRule(displayColumn, queryRule.getOp(), queryRule
									.getData()));
							displayCheck = displayCheck.replace(("#" + displayColumn + "#"), "");
						}
					}

					List<String> sysidList = (List<String>) cloudDao.findProperty(sf(), targetClass, new QueryGroup(OR,
							displayQueryRuleList.toArray(new QueryRule[0]), null), new QueryOrder[0], false, PK);
					if (sysidList.isEmpty())
						addQueryRuleList.add(new QueryRule(queryRule.getFieldName(), "x"));
					else
						addQueryRuleList.add(new QueryRule(queryRule.getFieldName(), IN, sysidList));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else if (StringUtils.isNotBlank(billName)) {
				try {
					Class targetClass = Class.forName(Util.beanPackage + "." + billName);
					delQueryRuleList.add(queryRule);
					List<String> sysidList = (List<String>) cloudDao.findProperty(sf(), targetClass, new QueryGroup(
							new QueryRule(BILLNO, queryRule.getOp(), queryRule.getData())), new QueryOrder[0], false,
							PK);
					if (sysidList.isEmpty())
						addQueryRuleList.add(new QueryRule(queryRule.getFieldName(), "x"));
					else
						addQueryRuleList.add(new QueryRule(queryRule.getFieldName(), IN, sysidList));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				// 查詢頁面必須提供空白選項，此選項視為無輸入查詢條件
				if (EQ.equalsIgnoreCase(queryRule.getOp()))
					if (queryRule.getData() == null
							|| (queryRule.getData() instanceof String && StringUtils.isBlank((String) queryRule
									.getData())))
						delQueryRuleList.add(queryRule);
			}
		}
		queryRuleList.removeAll(delQueryRuleList);
		queryRuleList.addAll(addQueryRuleList);

		queryGroup = new QueryGroup(queryGroup.getGroupOp(), queryRuleList.toArray(new QueryRule[0]),
				queryGroupList.toArray(new QueryGroup[0]));
		return queryGroup;
	}

	/**
	 * [jqgrid] filters to QueryRule List
	 */
	protected QueryGroup searchByFilters() {
		QueryGroup queryGroup = null;
		try {
			logger.debug("filters:" + filters);
			JSONObject group = new JSONObject(filters);
			queryGroup = QueryGroupUtil.jqgridGroupJSONObjectToQueryGroup(getCrudJqgridClass(), group);

			// Field[] f = getCrudJqgridClass().getDeclaredFields();
			// for (Field field : f)
			// if (!Modifier.isStatic(field.getModifiers()))
			// if (field.getType().equals(Set.class)) {
			// Class<?> joinClass =
			// findDetailInfoByResource(field.getName()).getDetailClass();
			// String as2 = joinClass.getSimpleName().substring(0,
			// 1).toLowerCase()
			// + joinClass.getSimpleName().substring(1);
			// queryGroup = Util.queryGroupScanClass(joinClass, as2 + "_",
			// queryGroup);
			// }

			queryGroup = resetInfoQuery(queryGroup);
			// queryGroup = resetJoinQuery(queryGroup);
			logger.debug("queryGroup:" + queryGroup);
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
		return jqgridList(getCrudJqgridClass());
	}

	/**
	 * [jqgrid] url
	 * 
	 * @param clazz
	 * @param andQueryRules
	 * @param andQueryGroups
	 * @param orders
	 * @param from
	 * @param length
	 * @return Object[] { record, results }
	 */
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Integer record = cloudDao.queryTableCount(sf(), clazz, queryGroup);
		List<?> results = cloudDao.queryTable(sf(), clazz, queryGroup, orders, from, length);
		if (getIsDataChangeLocale())
			addMultiLan(results, sf(), getPersistentClass());
		return new Object[] { record, results };
	}

	/**
	 * [jqgrid]
	 * 
	 * @param list
	 */
	protected void jqgridListResultMap(List<?> list) {
		resultMap = new HashMap();
		resultMap.put("gridModel", list);

		resultMap.put("page", page);
		resultMap.put("record", record);
		resultMap.put("rows", rows);
		resultMap.put("total", total);
	}

	/**
	 * [jqgrid] url
	 */
	protected String jqgridList(Class<?> clazz) {
		Boolean ACTION_SAVE = (Boolean) request.getAttribute("ACTION_SAVE");
		Boolean FIELD_ALL_ALLOW = (Boolean) request.getAttribute("FIELD_ALL_ALLOW");

		String returnStruts = JSON_RESULT;
		logger.debug("start where oper:" + oper);
		sessionSet("jqgridCrudRows", rows);
		sessionSet("jqgridCrudPage", page);
		sessionSet("jqgridCrudSidx", sidx);
		sessionSet("jqgridCrudSord", sord);
		logger.debug("sidx:" + sidx + " sord:" + sord);
		List<?> results = Collections.emptyList();
		int from = rows * (page - 1);
		int length = rows;

		List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
		if (StringUtils.isNotBlank(sidx)) {
			String className = "";
			try {
				className = coreSysidMapping.getString(sidx);
			} catch (MissingResourceException e) {
			}
			if (StringUtils.isNotBlank(className)) {
				try {
					Class<?> targetClass = Class.forName(Util.beanPackage + "." + className);

					String displayFormat = DEFAULT_DISPLAY_FORMAT;
					try {
						displayFormat = tableToDisplay.getString(className);
					} catch (MissingResourceException e1) {
					}
					String display = displayFormat;
					while (display.indexOf("#") != -1) {
						int i1 = display.indexOf("#");
						int i2 = display.indexOf("#", i1 + 1);
						if (i2 == -1)
							break;
						String displayColumn = display.substring(i1 + 1, i2);
						queryOrderList.add(new QueryOrderWithTable(sidx, sord, targetClass, PK, displayColumn));
						break;
					}
				} catch (ClassNotFoundException e) {
				}
			} else {
				queryOrderList.add(new QueryOrder(sidx, sord));
			}
		}
		try {
			MO bean = (MO) ConstructorUtils.invokeConstructor(clazz, null);
			if (PropertyUtils.getPropertyDescriptor(bean, BILLNO) != null && !BILLNO.equals(sidx))
				queryOrderList.add(new QueryOrder(BILLNO, DESC));
			if (PropertyUtils.getPropertyDescriptor(bean, ID) != null && !ID.equals(sidx))
				queryOrderList.add(new QueryOrder(ID, ASC));
			if (PropertyUtils.getPropertyDescriptor(bean, PK) != null && !PK.equals(sidx))
				queryOrderList.add(new QueryOrder(PK, DESC));
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
		// 限制可查詢之範圍
		Field[] f = clazz.getDeclaredFields();
		for (Field field : f)
			if (!Modifier.isStatic(field.getModifiers())) {
				try {
					// String className =
					// coreSysidMapping.getString(field.getName());
					// String columnId = field.getName();
					// String uid = getUserData().getAccount().getUid();
					// String sitesysid = getUserData().getSite().getSysid();
					// if (!uid.equals("admin")) {
					// if (CoreSite.class.getSimpleName().equals(className) &&
					// !columnId.equals("referenceSiteSysid")) {
					// andQueryGroupsList.add(new QueryGroup(new
					// QueryRule(columnId, sitesysid)));
					// } else if
					// (CoreVendor.class.getSimpleName().equals(className)) {
					// List<String> vendorList = (List<String>)
					// cloudService.findProperty(sf(),
					// CoreVendor.class, new QueryGroup(new
					// QueryRule(Util.SITE_SYSID, sitesysid)), null,
					// false, PK);
					// if (vendorList.size() > 0) {
					// andQueryGroupsList.add(new QueryGroup(new
					// QueryRule(columnId, IN, vendorList)));
					// } else if (vendorList.size() == 0) {
					// andQueryGroupsList.add(new QueryGroup(new
					// QueryRule(columnId, "x")));
					// }
					// }
					// }
				} catch (MissingResourceException e) {
				}
			}
		QueryGroup queryCondition = getQueryCondition();
		if (queryCondition != null)
			andQueryGroupsList.add(queryCondition);
		QueryGroup queryRestrict = getQueryRestrict();
		if (queryRestrict != null)
			andQueryGroupsList.add(queryRestrict);

		QueryGroup q = new QueryGroup(AND, null, andQueryGroupsList.toArray(new QueryGroup[0]));

		Object[] arr = null;
		if ("excel".equals(oper)) {
			arr = jqgridList(clazz, q, queryOrderList.toArray(new QueryOrder[0]), null, null);
		} else {
			arr = jqgridList(clazz, q, queryOrderList.toArray(new QueryOrder[0]), from, length);
		}
		record = (Integer) arr[0];
		results = (List<?>) arr[1];
		// ---------- ---------- ---------- ---------- ----------
		List<Map> formatToMapResults = formatListToStaticMap(results);
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

				// for text
				for (String key : map.keySet()) {
					if (!isColumnTexthtml(clazz.getSimpleName(), key) && !isColumnTextarea(clazz.getSimpleName(), key)
							&& !REMARK.equals(key))
						continue;
					Object valueObj = map.get(key);
					if (valueObj instanceof String) {
						String value = (String) valueObj;
						if (StringUtils.isBlank(value))
							continue;
						if (value.contains(Util.lnStr)) {
							value = value.substring(0, value.indexOf(Util.lnStr)) + "...";
							map.put(key, value);
						}
					}
				}

				// for img
				for (String imgCol : getImgCols()) {
					String sysid = (String) map.get(PK);
					String ori = (String) map.get(imgCol);

					String imgSubUrl = "/" + getWebDfImg() + "/" + getPersistentClass().getSimpleName() + "/";
					String imgUrl = imgSubUrl + sysid + "/" + ori;
					String img = "";
					if (StringUtils.isNotBlank(ori)) {
						img = "<img style='width:80px;height:40px;' src='" + imgUrl + "'>";
						map.put(imgCol, img);
					} else {
						img = "";
					}
					map.put(imgCol, img);

				}

				for (Field field : f) {
					Boolean FIELD_ALLOW = true;
					if (Util.isLogin) {
						FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALL_ALLOW);
						if (!FIELD_ALLOW) {
							FIELD_ALLOW = (Boolean) request.getAttribute("FIELD_" + field.getName());
							FIELD_ALLOW = BooleanUtils.isTrue(FIELD_ALLOW);
						}
					}
					boolean isColumnReadonly = isColumnReadonly(clazz.getSimpleName(), field.getName());
					boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), field.getName());
					if (BooleanUtils.isTrue(ACTION_SAVE) && FIELD_ALLOW && !isColumnReadonly && !isColumnHidden) {
						Object valueObj = map.get(field.getName());
						if (field.getType().equals(Boolean.class)) {
							boolean boo = (boolean) valueObj;
							String elementText = "<input id='" + field.getName() + "-" + pkObj + "' ";
							elementText += " type='checkbox' ";
							if (boo)
								elementText += " checked='checked' ";
							elementText += " onchange='fnJqgridUpdateBoolean( \"" + field.getName() + "\" , \"" + pkObj
									+ "\" )' />";

							map.put(field.getName() + "Element", elementText);
						}
					}
				}

				// button
				String url = request.getContextPath() + "/" + actionTypeText + "/" + getActionKey() + "_" + methodText
						+ "?bean.sysid=" + pkObj;
				String btnClass = "mi-invisible-btn";
				if (StringUtils.isNotBlank(getTreeParentKey()) && StringUtils.equals(getTreeType(), "B"))
					btnClass = "";
				String clickBtnEditText = "<a href='" + url + "' target='" + targetText + "'>"
						+ "<button type='button' class='" + btnClass + "' style='width:65px;'>"
						+ "<i class='glyphicon glyphicon-edit' style='color:#ff3971;' title='"
						+ getText("jqgrid.clickBtnEdit") + "'/>" + "</button></a>";

				Object selectId = new Integer(i + 1);
				if (StringUtils.isNotBlank(getTreeParentKey()))
					selectId = map.get(PK);
				String clickBtnSelect = "<button type='button' class='" + btnClass
						+ "' style='width:65px;' onclick='fnJqgridSelect(\"" + selectId + "\")'>"
						+ "<i  class='glyphicon glyphicon-check mi-pointer' style='color:#ff3971;' title='"
						+ getText("jqgrid.clickBtnSelect") + "'/>" + "</button>";

				map.put("clickBtnEdit", clickBtnEditText);
				map.put("clickBtnSelect", clickBtnSelect);
			}
		}
		// ---------- ---------- ---------- ---------- ----------
		total = (int) Math.ceil((double) record / (double) rows);

		jqgridListResultMap(formatToMapResults);

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
	public Map<String, SysColumnConfig> getCrudColumnConfig() {
		initSystemColumnConfigCreator(getPersistentClass(), "bean");
		return getColumnConfigCreator().createColumnConfigMap(getPersistentClass(), createOperatorValue());
	}

	// ---------- ---------- ---------- ---------- ----------
	public String jqgridQuickSave() {
		String data = request.getParameter("data");
		JSONObject jsonObject = new JSONObject(data);
		List saveList = new ArrayList();
		for (String pk : jsonObject.keySet()) {
			Map<String, Object> setMap = getUpdatePropertyMap();
			HqlStatement h = new UpdateStatement(getPersistentClass().getSimpleName(), new QueryGroup(new QueryRule(PK,
					pk)), setMap);

			JSONObject setObj = jsonObject.getJSONObject(pk);
			if (setObj.length() == 0)
				continue;
			for (String col : setObj.keySet()) {
				setMap.put(col, setObj.get(col));
			}

			saveList.add(h);
		}

		resultString = cloudDao.save(sf(), saveList);
		if (StringUtils.equals(SUCCESS, resultString))
			resetDataMap(getPersistentClass());
		return JSON_RESULT;
	}

	protected static final int dafaultWidthByLength = 20;

	protected final SystemColumnConfigCreator getSystemColumnConfigCreator() {
		// 開發階段用session,上線可用application
		final Map location = getIsTest() ? session : appMap();
		SystemColumnConfigCreator systemColumnConfigCreator = (SystemColumnConfigCreator) location
				.get("systemColumnConfigCreator");
		if (systemColumnConfigCreator == null) {
			List<String> leftColumnList = (List<String>) createResourceObject("leftColumnList");
			List<String> rightColumnList = (List<String>) createResourceObject("rightColumnList");
			final BasisCrudAction _this = this;
			systemColumnConfigCreator = new SystemColumnConfigCreator(sf()) {
				@Override
				public boolean isColumnReadonly(String className, String propertyName) {
					if (!propertyName.contains("_"))
						return _this.isColumnReadonly(className, propertyName);
					String[] arr = propertyName.split("_");
					return _this.isColumnReadonly(Util.joinTableFrontKeyToClassName(arr[0]), arr[1]);
				}

				@Override
				public boolean isColumnHidden(String className, String propertyName) {
					if (!propertyName.contains("_"))
						return _this.isColumnHidden(className, propertyName);
					String[] arr = propertyName.split("_");
					return _this.isColumnHidden(Util.joinTableFrontKeyToClassName(arr[0]), arr[1]);
				}

				@Override
				public boolean isColumnFrozen(String className, String propertyName) {
					if (!propertyName.contains("_"))
						return _this.isColumnFrozen(className, propertyName);
					String[] arr = propertyName.split("_");
					return _this.isColumnFrozen(Util.joinTableFrontKeyToClassName(arr[0]), arr[1]);
				}
			};
			systemColumnConfigCreator.setLeftColumnList(leftColumnList);
			systemColumnConfigCreator.setRightColumnList(rightColumnList);
			location.put("systemColumnConfigCreator", systemColumnConfigCreator);
		}
		return systemColumnConfigCreator;
	}

	protected final SystemColumnConfigCreator initSystemColumnConfigCreator(Class<?> clazz, String i18nKey) {
		SystemColumnConfigCreator systemColumnConfigCreator = getSystemColumnConfigCreator();

		Map<String, String> i18nMap = systemColumnConfigCreator.getI18nResource().get(clazz.getSimpleName());
		if (i18nMap == null) {
			i18nMap = new HashMap<String, String>();
			systemColumnConfigCreator.getI18nResource().put(clazz.getSimpleName(), i18nMap);
			Field[] f = clazz.getDeclaredFields();
			for (Field field : f) {
				String label = getText(field.getName());
				if (StringUtils.isNotBlank(i18nKey)) {
					label = getText(i18nKey + "." + field.getName());
					if (label.equals(i18nKey + "." + field.getName())) {
						label = getText("bean." + field.getName());
					}

					if (label.equals("bean." + field.getName())) {
						label = label.replaceFirst("bean.", "");

						label = label.replaceAll("Sysid", "");
						// label.

						String newLabel = label.substring(0, 1).toUpperCase();
						String subLabel = label.substring(1);

						char[] charArr = subLabel.toCharArray();
						for (char c : charArr) {
							int i = (int) c;
							if (65 <= i && i <= 90) {
								// 大寫
								newLabel += " ";
								newLabel += new Character(c).toString().toLowerCase();
							}
							// else if (97 <= i && i <= 122) {
							// // 小寫
							// }
							else {
								newLabel += c;
							}
						}
						label = newLabel;
					}
				}
				i18nMap.put(field.getName(), label);
			}
		}

		return systemColumnConfigCreator;
	}

	protected final Map<String, SysColumnConfig> createSystemColumnConfigMap(Class<?> clazz, String i18nKey) {
		// logger.debug("start");
		SystemColumnConfigCreator sccc = initSystemColumnConfigCreator(clazz, i18nKey);
		// logger.debug("mid");
		Map<String, SysColumnConfig> result = sccc.createSystemColumnConfigMap(clazz, createOperatorValue());
		// logger.debug("end");
		return result;
	}

	protected final UserColumnConfigCreator getUserColumnConfigCreator() {
		UserColumnConfigCreator userColumnConfigCreator = (UserColumnConfigCreator) session
				.get("userColumnConfigCreator");
		if (userColumnConfigCreator == null) {
			userColumnConfigCreator = new UserColumnConfigCreator(sf(), getUserID());
			session.put("userColumnConfigCreator", userColumnConfigCreator);
		}
		return userColumnConfigCreator;
	}

	protected final ColumnConfigCreator getColumnConfigCreator() {
		ColumnConfigCreator columnConfigCreator = (ColumnConfigCreator) session.get("columnConfigCreator");
		if (columnConfigCreator == null) {
			columnConfigCreator = new ColumnConfigCreator(getSystemColumnConfigCreator(), getUserColumnConfigCreator());
			session.put("columnConfigCreator", columnConfigCreator);
		}
		return columnConfigCreator;
	}

	public void turnBillCore() {
		try {
			edit();
			String sourceBeanName = request.getParameter("sourceBean");
			String sourcePK = request.getParameter("sourceSysid");
			Class<?> sourceClass = (Class<?>) Class.forName(Util.beanPackage + "." + sourceBeanName);
			Object sourceBean = cloudDao.get(sf(), sourceClass, sourcePK);
			Field[] f = sourceClass.getDeclaredFields();
			for (Field field : f) {
				Object value = PropertyUtils.getProperty(sourceBean, field.getName());
				if (!PK.equals(field.getName()) && !OD.equals(field.getName()) && !OP.equals(field.getName())
						&& !CR.equals(field.getName()) && !CD.equals(field.getName())
						&& !REMARK.equals(field.getName()) && !BILLNO.equals(field.getName())
						&& !BILL_STATUS.equals(field.getName()) && !ISSUE_DATE.equals(field.getName())
						&& !ISSUE_DEPT_SYSID.equals(field.getName()) && !ISSUE_EMP_SYSID.equals(field.getName())
						&& !AUDIT_DATE.equals(field.getName()) && !AUDIT_EMP_SYSID.equals(field.getName())
						&& !CLOSE_DATE.equals(field.getName()) && !Set.class.equals(field.getType())) {
					try {
						PropertyUtils.setProperty(bean, field.getName(), value);// 除了這些欄位，其他的來源單有什麼就塞什麼
					} catch (NoSuchMethodException e) {
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (Set.class.equals(field.getType())) {
					Class<?> sourceDetailClass = null;
					try {
						sourceDetailClass = Class.forName(Util.beanPackage + "." + sourceBeanName + "Item");
					} catch (Exception e) {
						try {
							sourceDetailClass = Class.forName(Util.beanPackage + "." + sourceBeanName + "Data");
						} catch (Exception e2) {
							e.printStackTrace();
							e2.printStackTrace();
						}
					}
					Class<?> nowDetailClass = null;
					try {
						nowDetailClass = Class.forName(Util.beanPackage + "." + getPersistentClass().getSimpleName()
								+ "Item");
					} catch (Exception e) {
						try {
							nowDetailClass = Class.forName(Util.beanPackage + "."
									+ getPersistentClass().getSimpleName() + "Data");

						} catch (Exception e2) {
							e.printStackTrace();
							e2.printStackTrace();
						}
					}
					Field[] detailF = sourceDetailClass.getDeclaredFields();
					Set<Serializable> oldDetailSet = (Set<Serializable>) value;
					Set<Serializable> newDetailSet = new LinkedHashSet<Serializable>();
					PropertyUtils.setProperty(bean, field.getName(), newDetailSet);
					sessionSet("detailSet", newDetailSet);
					for (Serializable oldDetail : oldDetailSet) {
						Serializable newBillDetailBean = (Serializable) org.apache.commons.beanutils.ConstructorUtils
								.invokeConstructor(nowDetailClass, null);
						for (Field detailField : detailF) {
							Object detailValue = PropertyUtils.getProperty(oldDetail, detailField.getName());
							if (!PK.equals(detailField.getName()) && !OD.equals(detailField.getName())
									&& !OP.equals(detailField.getName()) && !CR.equals(detailField.getName())
									&& !CD.equals(detailField.getName()) && !FK.equals(detailField.getName())
									&& !REMARK.equals(detailField.getName())
									&& !SOURCE_NO.equals(detailField.getName())
									&& !BILL_SOURCE_TYPE.equals(detailField.getName())
									&& !SOURCE_SN.equals(detailField.getName())
									&& !Set.class.equals(detailField.getType())) {
								try {
									PropertyUtils.setProperty(newBillDetailBean, detailField.getName(), detailValue);
								} catch (NoSuchMethodException e) {
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						PropertyUtils.setProperty(newBillDetailBean, SOURCE_NO,
								PropertyUtils.getProperty(sourceBean, BILLNO));
						PropertyUtils.setProperty(newBillDetailBean, BILL_SOURCE_TYPE, sourceClass.getSimpleName());
						PropertyUtils.setProperty(newBillDetailBean, SOURCE_SN,
								PropertyUtils.getProperty(oldDetail, SN));
						PropertyUtils.setProperty(newBillDetailBean, FK, PropertyUtils.getProperty(bean, PK));
						Util.defaultPK(newBillDetailBean);
						defaultValue(newBillDetailBean);
						newDetailSet.add(newBillDetailBean);
					}
				}
			}
			defaultBillno(bean);
			if (PropertyUtils.getPropertyDescriptor(sourceBean, BILL_STATUS) != null)
				PropertyUtils.setProperty(sourceBean, BILL_STATUS, BillStatusUtil.FINISH);
			saveList.add(sourceBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String turnBill() {
		turnBillCore();
		save();
		return TO_MAIN;
	}

	// ---------- ---------- ---------- ---------- ----------
	// protected MO oldBean;
	protected Boolean isSourceId() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if (SOURCE_ID.equals(field.getName()))
				return true;
		return false;
	}

	protected List<String> getSourceId() {
		List<String> list = new ArrayList<String>();
		String sourceSysid = getUserAccount().getSourceSysid();
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			String key = getPersistentClass().getSimpleName().substring(0, 3).toUpperCase();
			list.add(key);
			// 區隔館主,目前非必要?(可用aa關閉對應功能權限)
			// String key =
			// createDataTable(CpsEntity.class).get(getUserAccount().getSourceSysid()).getDataId().toUpperCase();
			// if ("CPS".equals(key))
			// for (String sysid : createDataTable(CpsEntity.class).keySet())
			// list.add(createDataTable(CpsEntity.class).get(sysid).getDataId().toUpperCase());
			// 加上sourceSysid提高容忍性,目前非必要(目前僅有BHS/MTS/供應商Sysid的情況)
			// list.add(sourceSysid);
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			list.add(sourceSysid);
		} else {
			list.add("x");
		}
		return list;
	}

	protected Boolean isVendorSysid() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if ("vendorSysid".equals(field.getName()))
				return true;
		return false;
	}

	protected Boolean isParentMtsMenuSysid() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if ("parentMtsMenuSysid".equals(field.getName()))
				return true;
		return false;
	}

	protected Boolean isParentBhsMenuSysid() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if ("parentBhsMenuSysid".equals(field.getName()))
				return true;
		return false;
	}

	protected Boolean isHottopicSysid() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if ("hottopicSysid".equals(field.getName()))
				return true;
		return false;
	}

	protected Boolean isMtsArticleType() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if ("mtsArticleType".equals(field.getName()))
				return true;
		return false;
	}

	protected Boolean isBhsArticleType() {
		Field[] f = getPersistentClass().getDeclaredFields();
		for (Field field : f)
			if ("bhsArticleType".equals(field.getName()))
				return true;
		return false;
	}

	protected String getVendorSysid() {
		try {
			String vendorSysid = "";
			if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
				vendorSysid = (String) PropertyUtils.getProperty(bean, "vendorSysid");
			else
				vendorSysid = getUserAccount().getSourceSysid();
			return vendorSysid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "x";
	}

	protected String getParentMtsMenuSysid() {
		try {
			String parentMtsMenuSysid = "";
			parentMtsMenuSysid = (String) PropertyUtils.getProperty(bean, "parentMtsMenuSysid");
			return parentMtsMenuSysid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "x";
	}

	protected String getParentBhsMenuSysid() {
		try {
			String parentBhsMenuSysid = "";
			parentBhsMenuSysid = (String) PropertyUtils.getProperty(bean, "parentBhsMenuSysid");
			return parentBhsMenuSysid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "x";
	}

	protected String getHottopicSysid() {
		try {
			String hottopicSysid = "";
			hottopicSysid = (String) PropertyUtils.getProperty(bean, "hottopicSysid");
			return hottopicSysid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "x";
	}

	protected String getMtsArticleType() {
		try {
			String mtsArticleType = "";
			mtsArticleType = (String) PropertyUtils.getProperty(bean, "mtsArticleType");
			return mtsArticleType;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "x";
	}

	protected String getBhsArticleType() {
		try {
			String bhsArticleType = "";
			bhsArticleType = (String) PropertyUtils.getProperty(bean, "bhsArticleType");
			return bhsArticleType;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "x";
	}

	protected int getOrderTotal() {
		if (isVendorSysid()) {
			return cloudDao.queryCount(sf(), bean.getClass(), new QueryGroup(new QueryRule("vendorSysid",
					getVendorSysid())));
		} else if (isSourceId()) {
			String str = "";
			for (String s : getSourceId())
				str += "," + s;
			str = StringUtils.isBlank(str) ? "x" : str.substring(1);
			return cloudDao.queryCount(sf(), bean.getClass(), new QueryGroup(new QueryRule(SOURCE_ID, IN, str)));
		} else {
			return cloudDao.queryCount(sf(), bean.getClass(), new QueryGroup(new QueryRule[0]));
		}
	}

	public String preSave() {
		String msg = SUCCESS;
		try {
			// 檢核
			Integer newOrder = (Integer) PropertyUtils.getProperty(bean, DATA_ORDER);
			if (newOrder == null || newOrder < 1) {
				return "排序請輸入正整數（從1開始）";
			}
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			if (StringUtils.isBlank(sysid)) {
				return "sysid錯誤";
			}

			MO oldBean = (MO) cloudDao.get(sf(), bean.getClass(), sysid);
			int total = getOrderTotal();

			if (oldBean == null) {
				if (newOrder < (total + 1)) {
					// upper
					msg = shiftOrder(newOrder, total, 1);
				} else if (newOrder > (total + 1)) {
					addActionMessage("排序調整:" + newOrder + "->" + (total + 1) + "(最後的序號)");
					PropertyUtils.setProperty(bean, DATA_ORDER, (total + 1));
				}
			} else {
				Integer oldOrder = (Integer) PropertyUtils.getProperty(oldBean, DATA_ORDER);
				if (newOrder < oldOrder) {
					// upper
					msg = shiftOrder(newOrder, oldOrder - 1, 1);
				} else if (newOrder > oldOrder) {
					// downer
					msg = shiftOrder(oldOrder + 1, newOrder, -1);
					if (newOrder > total) {
						addActionMessage("排序調整:" + newOrder + "->" + total + "(最後的序號)");
						PropertyUtils.setProperty(bean, DATA_ORDER, total);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "preSave發生錯誤";
		}
		return msg;
	}

	public String preDelete() {
		String msg = SUCCESS;
		try {
			// 檢核
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			if (StringUtils.isBlank(sysid))
				return "sysid錯誤";

			MO oldBean = (MO) cloudDao.get(sf(), bean.getClass(), sysid);
			int total = getOrderTotal();
			Integer oldOrder = (Integer) PropertyUtils.getProperty(oldBean, DATA_ORDER);
			if (oldOrder < total)
				msg = shiftOrder(oldOrder + 1, total, -1);
		} catch (Exception e) {
			e.printStackTrace();
			return "preDelete發生錯誤";
		}
		return msg;
	}

	public String ajaxSoab() {
		String msg = SUCCESS;
		try {
			String sysid = request.getParameter("sysid");
			String newIds = request.getParameter("newIds");
			if (StringUtils.isBlank(sysid) || StringUtils.isBlank(newIds)) {
				resultString = "排序參數錯誤";
				return JSON_RESULT;
			}
			int newOrder = Integer.parseInt(newIds);

			bean = (MO) cloudDao.get(sf(), bean.getClass(), sysid);
			int oldOrder = (int) PropertyUtils.getProperty(bean, DATA_ORDER);
			if (newOrder < oldOrder) {
				// upper
				msg = shiftOrder(newOrder, oldOrder - 1, 1);
			} else if (newOrder > oldOrder) {
				// downer
				msg = shiftOrder(oldOrder + 1, newOrder, -1);
			}
			if (!SUCCESS.equals(msg)) {
				resultString = msg;
				return JSON_RESULT;
			}

			PropertyUtils.setProperty(bean, DATA_ORDER, newOrder);
			defaultValue(bean);
			saveList.add(bean);
			msg = cloudDao.save(sf(), saveList.toArray(), false, "UPDATE");
			if (!SUCCESS.equals(msg)) {
				resultString = msg;
				return JSON_RESULT;
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultString = "排序發生錯誤";
			return JSON_RESULT;
		}
		resultString = msg;
		resetDataMap(getPersistentClass());
		return JSON_RESULT;
	}

	/**
	 * 排序位移 - 設定附屬欄位判斷
	 * 
	 * @param startIdx
	 * @param endIdx
	 * @param shiftNum
	 * @return
	 */
	protected String shiftOrder(int startIdx, int endIdx, int shiftNum) {
		try {
			// HQL
			Session session = sf().openSession();
			Transaction tx = session.beginTransaction();
			String tableName = getPersistentClass().getSimpleName();
			String columnName = DATA_ORDER;
			String qStr = "UPDATE " + tableName + " ";
			qStr += "SET " + columnName + " = " + columnName + " ";
			if (shiftNum >= 0)
				qStr += "+ ";
			qStr += shiftNum + " ";
			qStr += "WHERE " + columnName + " >= " + startIdx + " ";
			qStr += "AND " + columnName + " <= " + endIdx + " ";
			// 依照欄位增加篩選條件
			if (isVendorSysid())
				qStr += "AND " + "vendorSysid" + " = '" + getVendorSysid() + "' ";
			if (isHottopicSysid())
				qStr += "AND " + "hottopicSysid" + " = '" + getHottopicSysid() + "' ";
			if (isMtsArticleType())
				qStr += "AND " + "mtsArticleType" + " = '" + getMtsArticleType() + "' ";
			if (isBhsArticleType())
				qStr += "AND " + "bhsArticleType" + " = '" + getBhsArticleType() + "' ";
			if (isParentMtsMenuSysid())
				qStr += "AND " + "parentMtsMenuSysid" + " = '" + getParentMtsMenuSysid() + "' ";
			if (isParentBhsMenuSysid())
				qStr += "AND " + "parentBhsMenuSysid" + " = '" + getParentBhsMenuSysid() + "' ";
			if (isSourceId()) {
				String str = "";
				for (String s : getSourceId())
					str += ",'" + s + "'";
				str = StringUtils.isBlank(str) ? "'x'" : str.substring(1);
				qStr += "AND " + SOURCE_ID + " IN (" + str + ") ";
			}
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

	// ---------- ---------- ---------- ---------- ----------
	@Override
	public final Map<String, SysConstant> getDataSysConstantIdMap() {
		return getDataSysConstantIdMap(getPersistentClass());
	}

	/**
	 * 常數選單
	 * 
	 * @return
	 */
	@Override
	public final Map<String, Map> getConstantMenu() {
		return getConstantMenu(getPersistentClass());
	}

	/**
	 * <pre>
	 * 由於getConstantMenu()無法取得無宣告之常數，但頁面需要使用，故使用此方法取得
	 * </pre>
	 * 
	 * @param insertConstantKey
	 * @return
	 */
	public final Map<String, Map> getGlobalConstantMenu() {
		return super.getConstantMenu();
	}

	// ---------- ---------- ---------- ---------- ----------
	public String[] getImgCols() {
		return new String[0];
	}

	protected String getLanSavePath(String lan) {
		return getLanImgSavePath() + lan + File.separator + getBeanKey() + File.separator;
	}

	protected String getDfSavePath() {
		return getDfImgSavePath() + getBeanKey() + File.separator;
	}

	/**
	 * execute when save()
	 * 
	 * @param streamName
	 * @param savePath
	 * @return filename
	 */
	protected String uploadData(String streamName, String savePath) {
		if (MultiPartRequestWrapper.class.isInstance(request)) {
			MultiPartRequestWrapper multipartRequest = (MultiPartRequestWrapper) request;
			String[] dataFileName = multipartRequest.getFileNames(streamName);
			File[] data = multipartRequest.getFiles(streamName);
			if (data != null && data.length > 0) {
				String pk = editPK();
				String subMainFilePath = savePath + pk + File.separator;
				File floder = new File(subMainFilePath);
				if (!floder.exists())
					floder.mkdirs();
				for (int fileIndex = 0; fileIndex < dataFileName.length; fileIndex++) {
					String fileName = dataFileName[fileIndex];
					if (!FileUtil.validateExtention(pictureExtention, fileName)) {
						addActionError(getText("errMsg.fileFormatWrong",
								new String[] { FileUtil.getExtention(fileName) }));
					}

					String saveFilePath = subMainFilePath + fileName;
					logger.debug("測試 itemPicture儲存路徑:" + saveFilePath);
					File fileLocation = new File(saveFilePath);
					if (fileLocation.exists()) {
						String newFileName = streamName + FileUtil.getExtention(fileName);
						fileName = newFileName;
						saveFilePath = subMainFilePath + newFileName;
						logger.info("檔案名稱重複，故更名為:" + saveFilePath);
						fileLocation = new File(saveFilePath);
					}
					boolean isRenameToSuccess = FileUtil.moveFile(data[fileIndex], fileLocation);
					logger.debug("isRenameToSuccess:" + isRenameToSuccess);
					if (fileIndex == 0) {
						return fileName;
					}
				}
			}
		}
		return "";
	}

	public String ajaxImgColDel() {
		resultMap = new HashMap<Object, Object>();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");

		// bean_filePath beaninfo_filePath_zh_TW
		String imgColDelId = request.getParameter("imgColDelId");
		final String imgColDelValue = request.getParameter("imgColDelValue");
		String newOperationdate = request.getParameter(OD);
		String pk = request.getParameter(PK);

		List<?> oldOperationdateList = cloudDao.findProperty(sf(), bean.getClass(), new QueryGroup(
				new QueryRule(PK, pk)), new QueryOrder[0], false, OD);
		Object oldOperationdate = (oldOperationdateList != null && oldOperationdateList.size() == 1) ? oldOperationdateList
				.get(0) : null;
		if (oldOperationdate != null) {
			if (!oldOperationdate.equals(newOperationdate)) {
				logger.info("oldOperationdate:" + oldOperationdate);
				logger.info("newOperationdate:" + newOperationdate);
				resultMap.put("msg", getText(SAVE_TIMEOUT));
				return JSON_RESULT;
			}
		}

		String[] imgColDelIdArr = imgColDelId.split("_");
		if (getIsDataChangeLocale() && getIsImgChangeLocale()) {
			List saveList = new ArrayList();

			Map<String, Object> setMap = getUpdatePropertyMap();
			saveList.add(new UpdateStatement(getPersistentClass().getSimpleName(),
					new QueryGroup(new QueryRule(PK, pk)), setMap));
			String lan = getCookieLan();
			if ("bean".equals(imgColDelIdArr[0])) {
				setMap.put(imgColDelIdArr[1], "");
			} else if ("beaninfo".equals(imgColDelIdArr[0])) {
				lan = imgColDelIdArr[2] + "_" + imgColDelIdArr[3];
			}
			saveList.add(new DeleteStatement(multiLanClassName(lan), new QueryGroup(new QueryRule("sourceTable",
					getPersistentClass().getSimpleName()), new QueryRule("sourceSysid", pk), new QueryRule(
					"sourceColumn", imgColDelIdArr[1]))));

			String daoMsg = cloudDao.save(sf(), saveList);
			if (!SUCCESS.equals(daoMsg)) {
				resultMap.put("msg", daoMsg);
				return JSON_RESULT;
			}

			String subMainFilePath = getLanSavePath(lan) + pk + File.separator;
			File floder = new File(subMainFilePath);
			if (!floder.exists())
				floder.mkdirs();
			File[] targets = floder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.equals(imgColDelValue);
				}
			});
			for (File file : targets) {
				if (file.delete())
					logger.warn("delete file " + subMainFilePath + imgColDelValue + " failed.");
			}
		} else {
			List saveList = new ArrayList();

			Map<String, Object> setMap = getUpdatePropertyMap();
			setMap.put(imgColDelIdArr[1], "");
			HqlStatement hs = new UpdateStatement(getPersistentClass().getSimpleName(), new QueryGroup(new QueryRule(
					PK, pk)), setMap);
			saveList.add(hs);

			// 清理異常資料
			for (String lan : getLanguageTypeMap().keySet()) {
				saveList.add(new DeleteStatement(multiLanClassName(lan), new QueryGroup(new QueryRule("sourceTable",
						getPersistentClass().getSimpleName()), new QueryRule("sourceColumn", imgColDelIdArr[1]))));
			}

			String daoMsg = cloudDao.save(sf(), saveList);
			if (!SUCCESS.equals(daoMsg)) {
				resultMap.put("msg", daoMsg);
				return JSON_RESULT;
			}

			String subMainFilePath = getDfSavePath() + pk + File.separator;
			File floder = new File(subMainFilePath);
			if (!floder.exists())
				floder.mkdirs();
			File[] targets = floder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.equals(imgColDelValue);
				}
			});
			for (File file : targets) {
				if (file.delete())
					logger.warn("delete file " + subMainFilePath + imgColDelValue + " failed.");
			}
		}
		resetDataMap(getPersistentClass());
		resultMap.put("isSuccess", true);
		resultMap.put("msg", "");
		resultMap.put(OD, systemDatetimeStr);
		return JSON_RESULT;
	}

	// 假欄位預設值
	protected Map<String, Object> fakeColModel(String key) {
		Map<String, Object> colModelMap = new LinkedHashMap<String, Object>();
		colModelMap.put("name", key);
		colModelMap.put("index", key);
		colModelMap.put("align", getJqgridTextAlign());
		colModelMap.put("sortable", false);
		colModelMap.put("editable", true);
		colModelMap.put("label", getText("bean." + key));
		colModelMap.put("width", 150);
		return colModelMap;
	}

	// BHS_MENU_LINK用
	protected String menuSel;

	public final String getMenuSel() {
		return menuSel;
	}

	public final void setMenuSel(String menuSel) {
		this.menuSel = menuSel;
	}

	protected final String getMtsMenuSel() {
		String menuSel = "";
		try {
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			List<String> list = (List<String>) cloudDao.findProperty(sf(), MtsMenuLink.class, new QueryGroup(
					new QueryRule(FK, sysid)), new QueryOrder[0], false, "menuSysid");
			for (String s : list)
				menuSel += "," + s;
			menuSel = StringUtils.isBlank(menuSel) ? "" : menuSel.substring(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return menuSel;
	}

	protected final String saveMtsMenuSel() {
		logger.info("agent:" + Util.checkBrowser(request.getHeader("user-agent")));
		try {
			// 1.刪除
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			logger.debug("sysid:" + sysid);
			if (StringUtils.isBlank(menuSel))
				logger.warn("menuSel: DO NOT write any!!");
			else
				logger.debug("menuSel:" + menuSel);

			QueryGroup q = new QueryGroup(new QueryRule(FK, sysid));
			List l = cloudDao.queryTable(sf(), MtsMenuLink.class, q, null, null, null);
			for (Object link : l) {
				logger.debug("[即將刪除]" + ReflectionToStringBuilder.toString(link));
			}
			HqlStatement del = new DeleteStatement(MtsMenuLink.class.getSimpleName(), q);
			// String daoMsg = cloudDao.save(sf(), del);
			// if (!SUCCESS.equals(daoMsg))
			// return daoMsg;
			saveList.add(del);

			// 2.新增勾選的選單分類
			if (StringUtils.isNotBlank(menuSel)) {
				String[] arr = menuSel.split(",");
				for (String _menuSysid : arr) {
					if (StringUtils.isNotBlank(_menuSysid)) {
						MtsMenuLink ml = new MtsMenuLink();
						Util.defaultPK(ml);
						defaultValue(ml);
						ml.setMenuSysid(StringUtils.trim(_menuSysid));
						ml.setParentSysid(sysid);
						saveList.add(ml);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return SUCCESS;
	}

	protected final String getBhsMenuSel() {
		String menuSel = "";
		try {
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			List<String> list = (List<String>) cloudDao.findProperty(sf(), BhsMenuLink.class, new QueryGroup(
					new QueryRule(FK, sysid)), new QueryOrder[0], false, "menuSysid");
			for (String s : list)
				menuSel += "," + s;
			menuSel = StringUtils.isBlank(menuSel) ? "" : menuSel.substring(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return menuSel;
	}

	protected final String saveBhsMenuSel() {
		logger.info("agent:" + Util.checkBrowser(request.getHeader("user-agent")));
		try {
			// 1.刪除
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			logger.debug("sysid:" + sysid);
			if (StringUtils.isBlank(menuSel))
				logger.warn("menuSel: DO NOT write any!!");
			else
				logger.debug("menuSel:" + menuSel);

			QueryGroup q = new QueryGroup(new QueryRule(FK, sysid));
			List l = cloudDao.queryTable(sf(), BhsMenuLink.class, q, null, null, null);
			if (l.size() > 0)
				for (Object link : l)
					logger.debug("[即將刪除]" + ReflectionToStringBuilder.toString(link));
			else
				logger.debug("[無須刪除]查無需要刪除資料");
			HqlStatement del = new DeleteStatement(BhsMenuLink.class.getSimpleName(), q);
			// String daoMsg = cloudDao.save(sf(), del);
			// if (!SUCCESS.equals(daoMsg))
			// return daoMsg;
			saveList.add(del);

			// 2.新增勾選的選單分類
			if (StringUtils.isNotBlank(menuSel)) {
				String[] arr = menuSel.split(",");
				for (String _menuSysid : arr) {
					if (StringUtils.isNotBlank(_menuSysid)) {
						BhsMenuLink ml = new BhsMenuLink();
						Util.defaultPK(ml);
						defaultValue(ml);
						ml.setMenuSysid(StringUtils.trim(_menuSysid));
						ml.setParentSysid(sysid);
						saveList.add(ml);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return SUCCESS;
	}

	// BHS_INFO_LINK用
	protected List<File> detailImg;
	protected List<String> detailImgFileName;

	public List<File> getDetailImg() {
		return detailImg;
	}

	public void setDetailImg(List<File> detailImg) {
		this.detailImg = detailImg;
	}

	public List<String> getDetailImgFileName() {
		return detailImgFileName;
	}

	public void setDetailImgFileName(List<String> detailImgFileName) {
		this.detailImgFileName = detailImgFileName;
	}

	protected String updateBhsInfoLinkPic() {
		Set<BhsInfoLink> dataSet = (Set<BhsInfoLink>) findDetailSetWhenEdit(DETAIL_SET);
		try {
			String sysid = (String) PropertyUtils.getProperty(bean, PK);
			if (detailImg != null && detailImg.size() > 0) {
				String subMainFilePath = getDfSavePath() + sysid + File.separator;
				File dirFile = new File(subMainFilePath);
				if (!dirFile.exists())
					dirFile.mkdirs();// create document

				// 驗證圖片副檔名
				for (String fileName : detailImgFileName) {
					if (!FileUtil.validateExtention(resultFileExtention, fileName)) {
						return getText("errMsg.fileFormatWrong", new String[] { FileUtil.getExtention(fileName) });
					}
				}

				for (int fileIndex = 0; fileIndex < detailImgFileName.size(); fileIndex++) {
					String finalFileName = detailImgFileName.get(fileIndex);
					String saveFilePath = subMainFilePath + finalFileName;
					logger.debug("測試 儲存路徑:" + saveFilePath);
					File fileLocation = new File(saveFilePath);

					// 判斷檔案重複
					if (fileLocation.exists()) {
						finalFileName = reportName + finalFileName;
						saveFilePath = subMainFilePath + finalFileName;
						logger.debug("測試 儲存路徑:" + saveFilePath);
						fileLocation = new File(saveFilePath);
					}
					FileUtil.moveFile(detailImg.get(fileIndex), fileLocation);

					BhsInfoLink f = getDefaultDMO(BhsInfoLink.class);
					defaultValue(f);
					Util.defaultPK(f);
					dataSet.add(f);
					f.setParentSysid(sysid);
					f.setFileName(finalFileName);
					String ext = FileUtil.getExtention(finalFileName);
					String linktxt = finalFileName.replace(ext, "");
					f.setLinktxt(linktxt); // 去掉附檔名
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	public String deleteBhsInfoLinkCol() {
		String sysid = request.getParameter("sysid");
		String fileName = request.getParameter("fileName");
		String parentSysid = request.getParameter("parentSysid");
		if (StringUtils.isNotBlank(sysid) && StringUtils.isNotBlank(fileName) && StringUtils.isNotBlank(parentSysid)) {
			String deleteFilePath = getDfSavePath() + parentSysid + File.separator + fileName;
			logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
			File deleteLocation = new File(deleteFilePath);
			deleteLocation.delete();

			List<Object> deleteList = new ArrayList<Object>();
			deleteList.add(new DeleteStatement(BhsInfoLink.class.getSimpleName(), new QueryGroup(new QueryRule(PK,
					sysid))));
			String msg = cloudDao.save(sf(), deleteList.toArray(), false, null);
			if (!SUCCESS.equals(msg)) {
				resultString = "刪除發生錯誤:" + msg;
				return JSON_RESULT;
			}

			Set<BhsInfoLink> dataSet = (Set<BhsInfoLink>) findDetailSetWhenEdit(DETAIL_SET);
			for (BhsInfoLink object : dataSet) {
				logger.debug(object.getSysid());
				if (object.getSysid().equals(sysid)) {
					dataSet.remove(object);
					break;
				}
			}
			resultString = msg;
		} else {
			resultString = "參數錯誤";
		}
		return JSON_RESULT;
	}
}