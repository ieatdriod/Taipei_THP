package tw.com.mitac.thp.action;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.struts2.interceptor.CookiesAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.ssh.util.InfoUtils;
import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.tenancy.util.TenancySessionFactoryUtil;
import tw.com.mitac.thp.bean.SysConstant;
import tw.com.mitac.thp.util.ProjectArea;
import tw.com.mitac.thp.util.Util;

import com.opensymphony.xwork2.ActionContext;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BasisAction extends tw.com.mitac.struts2.BasisAction implements CookiesAware, ServletResponseAware,
		ProjectArea {
	private static final long serialVersionUID = 1L;
	protected static final String PROJECT_NAME = Util.globalSetting().getString("app.projectName");
	protected static final boolean isMultiTenancy = new Boolean(Util.globalSetting().getString("app.isMultiTenancy"));
	protected static final boolean isChangeLocale = new Boolean(Util.globalSetting().getString("app.isChangeLocale"));
	@Autowired
	@Qualifier("globalSetting")
	protected Properties globalSetting;

	public final Properties getGlobalSetting() {
		return globalSetting;
	}

	protected SessionFactory sessionFactory;

	@Autowired(required = false)
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * default sessionFactory
	 * 
	 * @return
	 */
	protected SessionFactory sf() {
		return sessionFactory;
	}

	/**
	 * default application
	 * 
	 * @return
	 */
	protected Map<String, Object> appMap() {
		return application;
	}

	protected Map<String, String> cookiesMap;

	@Override
	public void setCookiesMap(Map<String, String> cookiesMap) {
		this.cookiesMap = cookiesMap;

		// logger.debug("request:" + request);
		if (request != null) {
			String refreshLan = request.getParameter("refreshLan");
			if (StringUtils.isNotBlank(refreshLan)) {
				logger.debug("refreshLan:" + refreshLan);
				if (languageTypeMap.containsKey(refreshLan)) {
					Cookie cookie = new Cookie(getCookieLanKey(), StringUtils.defaultString(refreshLan));
					cookie.setMaxAge(60 * 60 * 24 * 365);
					response.addCookie(cookie);
					logger.info("set cookie [" + getCookieLanKey() + "]:" + refreshLan);
					this.cookieLan = refreshLan;
				}
			}
		}

		String cookieLan = getCookieLan();
		if (StringUtils.isNotBlank(cookieLan)) {
			logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);
		}
	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
		logger.debug("BasisAction網址:" + request.getRequestURI());

		super.setServletRequest(request);
	}

	public String getCookieLanKey() {
		return "siteLanguage";
	}

	protected String cookieLan;

	/**
	 * 目前語系
	 */
	public final String getCookieLan() {
		if (isChangeLocale && StringUtils.isBlank(cookieLan)) {
			// String requestLocale=request.getLocale().toString();
			String requestLocale = "";// 不參考
			logger.debug("request.getLocale():" + requestLocale);
			// logger.debug("response.getLocale():" + response.getLocale());
			cookieLan = StringUtils.defaultString(cookiesMap.get(getCookieLanKey()));

			if (StringUtils.equals(getCookieLanKey(), "siteLanguage"))
				cookieLan = "";// 強制前臺使用預設語系

			// 在多語系專案 確保cookies.language有值
			if (!languageTypeMap.containsKey(cookieLan)) {
				if (languageTypeMap.containsKey(requestLocale))
					cookieLan = requestLocale;
				else
					for (String lan : languageTypeMap.keySet()) {
						cookieLan = lan;
						break;
					}

				Cookie cookie = new Cookie(getCookieLanKey(), StringUtils.defaultString(cookieLan));
				// 設置Cookie的生命周期
				cookie.setMaxAge(60 * 60 * 24 * 365);
				// cookie.setDomain("??");
				response.addCookie(cookie);
				logger.info("set default cookie [" + getCookieLanKey() + "]:" + cookieLan);

				// String[] arr = cookieLan.split("_");
				// response.setLocale(new Locale(arr[0], arr[1]));
			}
		}
		return cookieLan;
	}

	protected HttpServletResponse response;

	@Override
	public final void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	protected static final String I18N_DELETE_SUCCESS = "delete.success";
	protected static final String I18N_SAVE_SUCCESS = "save.success";
	protected static final String SAVE_TIMEOUT = "save.timeout";
	protected static final String FIND_NOTFOUNT = "find.notfound";

	/** 圖形驗證碼 */
	protected static final String DYNA_NUMBER = "NUMBER";

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * <ul>
	 * <li>A</li>
	 * <li>B</li>
	 * </ul>
	 * 
	 * @return
	 */
	public final String getMiaaType() {
		if (!Util.isLogin)
			return "B";
		return "B";
	}

	public boolean getIsMenuExpandAll() {
		return false;
	}

	public boolean getIsJqgrid20() {
		return true;
	}

	/**
	 * <ul>
	 * <li>A</li>
	 * <li>B</li>
	 * </ul>
	 * 
	 * @return
	 */
	public final String getTreeType() {
		return "B";
	}

	// ---------- ---------- ---------- ---------- ----------
	protected final String DEFAULT_DISPLAY_FORMAT = "#" + NAME + "#";
	protected static final ResourceBundle coreSysidMapping = ResourceBundle.getBundle("SysidToDisplay");
	protected static final ResourceBundle tableToDisplay = ResourceBundle.getBundle("TableToDisplay");

	protected final String DEFAULT_BILL_DISPLAY_FORMAT = "#" + BILLNO + "#";
	protected static final ResourceBundle billSysidMapping = ResourceBundle.getBundle("SysidToBillno");
	protected static final ResourceBundle tableToBillno = ResourceBundle.getBundle("TableToBillno");

	/**
	 * dataId+splitChar+name
	 * 
	 * @return
	 */
	public String getSplitChar() {
		return "：";
	}

	/**
	 * <pre>
	 * 找出此類別鍵值可能出現之欄位
	 * </pre>
	 * 
	 * @param clazz
	 * @return
	 */
	protected static List<String> linkPropertyList(Class<?> clazz) {
		List<String> propertyNameList = new ArrayList<String>();
		Enumeration<String> enumeration = coreSysidMapping.getKeys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			String value = coreSysidMapping.getString(key);
			if (clazz.getSimpleName().equals(value)) {
				propertyNameList.add(key);
			}
		}

		enumeration = billSysidMapping.getKeys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			String value = billSysidMapping.getString(key);
			if (clazz.getSimpleName().equals(value)) {
				propertyNameList.add(key);
			}
		}
		return propertyNameList;
	}

	// ---------- ---------- ---------- ---------- ----------
	// here because i18n
	public Map<Boolean, String> getSystemBooleanMap() {
		Map<Boolean, String> map = new LinkedHashMap<Boolean, String>();
		map.put(true, this.getText("boolean.true"));
		map.put(false, this.getText("boolean.false"));
		return map;
	}

	private Map<String, String> wfStatusMap;

	/**
	 * 所有單據狀態
	 *
	 * @return wfStatusMap
	 */
	public final Map<String, String> getWfStatusMap() {
		if (wfStatusMap == null) {
			wfStatusMap = new LinkedHashMap<String, String>();
			ResourceBundle rb = getTexts("billStatus");
			for (String key : rb.keySet())
				wfStatusMap.put(key.replaceFirst("billStatus.", ""), rb.getString(key));
		}
		return wfStatusMap;
	}

	protected Map<String, List> texthtmlMap;
	protected List<String> halfwidthToFullwidthFalse;
	protected List<String> dateDefaultNullList;
	protected List<String> dateDefaultMaxList;
	protected List<String> pictureExtention;
	protected List<String> resultFileExtention;

	protected Map<String, String> languageTypeMap;
	protected List<String> skipChangeLocaleList;
	protected Map<String, String> pagesUrl;

	public final void setTexthtmlMap(@Qualifier("texthtmlMap") Map<String, List> texthtmlMap) {
		this.texthtmlMap = texthtmlMap;
	}

	public final void setHalfwidthToFullwidthFalse(
			@Qualifier("halfwidthToFullwidthFalse") List<String> halfwidthToFullwidthFalse) {
		this.halfwidthToFullwidthFalse = halfwidthToFullwidthFalse;
	}

	public final void setDateDefaultNullList(@Qualifier("dateDefaultNullList") List<String> dateDefaultNullList) {
		this.dateDefaultNullList = dateDefaultNullList;
	}

	public final void setDateDefaultMaxList(@Qualifier("dateDefaultMaxList") List<String> dateDefaultMaxList) {
		this.dateDefaultMaxList = dateDefaultMaxList;
	}

	@Autowired
	public final void setPictureExtention(@Qualifier("pictureExtention") List<String> pictureExtention) {
		this.pictureExtention = pictureExtention;
	}

	@Autowired
	public final void setResultFileExtention(@Qualifier("resultFileExtention") List<String> resultFileExtention) {
		this.resultFileExtention = resultFileExtention;
	}

	public final Map<String, String> getLanguageTypeMap() {
		return languageTypeMap;
	}

	@Autowired
	public final void setLanguageTypeMap(@Qualifier("languageTypeMap") Map<String, String> languageTypeMap) {
		this.languageTypeMap = languageTypeMap;
	}

	@Autowired
	public final void setSkipChangeLocaleList(@Qualifier("skipChangeLocaleList") List<String> skipChangeLocaleList) {
		this.skipChangeLocaleList = skipChangeLocaleList;
	}

	public final Map<String, String> getPagesUrl() {
		return pagesUrl;
	}

	@Autowired
	public final void setPagesUrl(@Qualifier("pagesUrl") Map<String, String> pagesUrl) {
		this.pagesUrl = pagesUrl;
	}

	/**
	 * 2013/09/26 福利說 若有權限，則不應因流程不對而看不到按鈕，也就是一定看得到，只是當流程不符時，提示無法使用功能。
	 */
	public final boolean getIsShowNouseButton() {
		return false;
	}

	// ---------- ---------- ---------- ---------- ----------
	public final boolean getIsTest() {
		// return false;
		Boolean isTest = (Boolean) session.get("isTest");
		if (isTest == null)
			isTest = (Boolean) appMap().get("isTest");
		if (isTest == null)
			// return true;
			return false;
		return isTest;
	}

	// ---------- ---------- ---------- ---------- ----------
	public final boolean getIsShowSequence() {
		// Boolean isShowSequence = (Boolean) appMap().get("isShowSequence");
		// if (isShowSequence == null)
		// return false;
		// // return isShowSequence;
		return true;
	}

	public final boolean getIsMultiTenancy() {
		return isMultiTenancy;
	}

	// ---------- ---------- ---------- ---------- ----------
	public List<MtMultiTenancy> getTenancyList() {
		logger.debug("start");
		if (getIsMultiTenancy()) {
			// FIXME how to refresh
			List<MtMultiTenancy> tenancyList = (List<MtMultiTenancy>) appMap().get("tenancyList");
			if (tenancyList == null) {
				tenancyList = TenancySessionFactoryUtil.allTenancy();

				appMap().put("tenancyList", tenancyList);
			}
			return tenancyList;
		} else {
			List<MtMultiTenancy> tenancyList = (List<MtMultiTenancy>) appMap().get("tenancyList");
			if (tenancyList == null) {
				tenancyList = new ArrayList<MtMultiTenancy>();
				tenancyList.add(Util.defaultTenancy);

				appMap().put("tenancyList", tenancyList);
			}
			return tenancyList;
		}
	}

	// ---------- ---------- ---------- ---------- ----------
	protected Map<String, SysConstant> findSysConstantIdMap(Object daoSolution, Class<?> clazz) {
		// logger.debug("start where clazz:" + clazz);
		Map<String, SysConstant> targetIdMap = new LinkedHashMap<String, SysConstant>();

		List<String> propertyNameList = new ArrayList<String>();
		Field[] f = clazz.getDeclaredFields();
		for (Field field : f)
			propertyNameList.add(field.getName());
		// logger.debug("propertyNameList:" + propertyNameList);
		List<SysConstant> list = cloudDao.queryTable(daoSolution, SysConstant.class, new QueryGroup(new QueryRule(
				"tableName", clazz.getSimpleName()), new QueryRule("constantId", IN, propertyNameList)),
				new QueryOrder[0], null, null);
		for (SysConstant bean : list)
			targetIdMap.put(bean.getConstantId(), bean);
		list = cloudDao.queryTable(daoSolution, SysConstant.class, new QueryGroup(new QueryRule("tableName",
				Util.INFO_STAR), new QueryRule("constantId", IN, propertyNameList), new QueryRule("constantId", NI,
				targetIdMap.keySet())), new QueryOrder[0], null, null);
		for (SysConstant bean : list)
			targetIdMap.put(bean.getConstantId(), bean);
		// logger.debug("end");
		return targetIdMap;
	}

	protected Map<String, Map> sysConstantIdMapToConstantMenu(Map<String, SysConstant> map) {
		Map<String, Map> constantMenu = new LinkedHashMap<String, Map>();
		for (Object key : map.keySet()) {
			Map<String, String> menu = new LinkedHashMap<String, String>();
			SysConstant bean = (SysConstant) map.get(key);
			String[] arr = bean.getConstantOption().split(",");
			for (String string : arr) {
				String[] option = string.split(":");
				String optionKey = option[0].replaceAll("\r\n", "").replaceAll("\n", "");
				String optionValue = optionKey;
				if (option.length >= 2)
					optionValue = option[1].replaceAll("\r\n", "").replaceAll("\n", "");
				menu.put(optionKey, optionValue);
			}
			constantMenu.put(bean.getConstantId(), menu);
		}
		return constantMenu;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected String createOperatorValue() {
		return getClass().getSimpleName();
	}

	/**
	 * 產生dataLog:creationDate,operationDate,creator,operator
	 * 
	 * @param bean
	 */
	protected final void defaultValue(Object bean) {
		try {
			try {
				String creator = (String) PropertyUtils.getProperty(bean, CR);
				if (StringUtils.isBlank(creator))
					PropertyUtils.setProperty(bean, CR, createOperatorValue());
			} catch (NoSuchMethodException e) {
			}
			try {
				PropertyUtils.setProperty(bean, OP, createOperatorValue());
			} catch (NoSuchMethodException e) {
			}

			if (String.class.equals(Util.timestampClass)) {
				try {
					String creationdate = (String) PropertyUtils.getProperty(bean, CD);
					if (StringUtils.isBlank(creationdate))
						PropertyUtils.setProperty(bean, CD, systemDatetimeStr);
				} catch (NoSuchMethodException e) {
				}
				try {
					PropertyUtils.setProperty(bean, OD, systemDatetimeStr);
				} catch (NoSuchMethodException e) {
				}
			} else if (Date.class.equals(Util.timestampClass)) {
				try {
					Date creationdate = (Date) PropertyUtils.getProperty(bean, CD);
					if (creationdate == null)
						PropertyUtils.setProperty(bean, CD, systemDatetime);
				} catch (NoSuchMethodException e) {
				}
				try {
					PropertyUtils.setProperty(bean, OD, systemDatetime);
				} catch (NoSuchMethodException e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 產生dataLog:operationDate,operator
	 * 
	 * @return setMap
	 */
	protected final Map<String, Object> getUpdatePropertyMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		if (String.class.equals(Util.timestampClass)) {
			map.put(OD, systemDatetimeStr);
		} else if (Date.class.equals(Util.timestampClass)) {
			map.put(OD, systemDatetime);
		}
		map.put(OP, createOperatorValue());
		return map;
	}

	// ---------- ---------- ---------- ---------- ----------
	public List<Map> formatListToMap(Collection<?> list) {
		if (list == null)
			return null;
		List<Map> formatToMapResults = new ArrayList<Map>();
		for (Object object : list)
			if (object != null) {
				// XXX 日期是否可用jqgrid format處理?若不行則使用字串處理
				Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(object);
				formatToMapResults.add(map);
			}
		return formatToMapResults;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected Class<?> multiLanClass(String language) {
		Class<?> targetClass = null;
		try {
			targetClass = Class.forName(Util.beanPackage + "." + multiLanClassName(language));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return targetClass;
	}

	protected String multiLanClassName(String language) {
		return "SysMultiLanguage" + "_" + language;
	}

	/**
	 * 增加多國語系資訊
	 * 
	 * @param results
	 * @param sessionFactory
	 * @param clazz
	 */
	protected void addMultiLan(Collection<?> results, SessionFactory sessionFactory, Class<?> clazz) {
		addMultiLan(results.toArray(), sessionFactory, clazz);
	}

	/**
	 * 增加多國語系資訊
	 * 
	 * @param results
	 * @param sessionFactory
	 * @param clazz
	 */
	protected void addMultiLan(Object[] results, SessionFactory sessionFactory, Class<?> clazz) {
		if (isChangeLocale && !skipChangeLocaleList.contains(clazz.getSimpleName())) {
			if (results.length > 0) {
				try {
					List<String> sysidList = new ArrayList<String>();
					for (Object object : results) {
						String sysid = (String) InfoUtils.getProperty(object, PK);
						sysidList.add(sysid);
					}
					List sysMultiLanguageList = cloudDao.queryTable(sessionFactory, multiLanClass(getCookieLan()),
							new QueryGroup(new QueryRule("sourceTable", clazz.getSimpleName()), new QueryRule(
									"sourceSysid", IN, sysidList)), new QueryOrder[0], null, null);
					Map<String, List> sysMultiLanguageMapBySysid = new HashMap<String, List>();
					for (Object sysMultiLanguage : sysMultiLanguageList) {
						String sourceSysid = (String) PropertyUtils.getProperty(sysMultiLanguage, "sourceSysid");
						List sysMultiLanguageListBySysid = sysMultiLanguageMapBySysid.get(sourceSysid);
						if (sysMultiLanguageListBySysid == null) {
							sysMultiLanguageListBySysid = new ArrayList();
							sysMultiLanguageMapBySysid.put(sourceSysid, sysMultiLanguageListBySysid);
						}
						sysMultiLanguageListBySysid.add(sysMultiLanguage);
					}
					for (Object object : results) {
						String sysid = (String) InfoUtils.getProperty(object, PK);
						List sysMultiLanguageListBySysid = sysMultiLanguageMapBySysid.get(sysid);
						if (sysMultiLanguageListBySysid != null)
							for (Object sysMultiLanguage : sysMultiLanguageListBySysid) {
								String sourceColumn = (String) PropertyUtils.getProperty(sysMultiLanguage,
										"sourceColumn");
								String columnValueString = (String) PropertyUtils.getProperty(sysMultiLanguage,
										"columnValueString");
								String columnValueText = (String) PropertyUtils.getProperty(sysMultiLanguage,
										"columnValueText");
								String lanValue = StringUtils.isNotBlank(columnValueString) ? columnValueString
										: columnValueText;
								try {
									InfoUtils.setProperty(object, sourceColumn, lanValue);
								} catch (NoSuchMethodException e) {
								}
							}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * 直接輸出字串
	 */
	protected String renderText(String text) {
		return render(text, "text/plain;charset=UTF-8");
	}

	protected String render(String text, String contentType) {
		try {
			response.setContentType(contentType);
			response.getWriter().write(text);
		} catch (IOException e) {
		}
		return null;
	}

	// ---------- ---------- ---------- ---------- ----------
	public Map<String, String> getSearchRulesMap() {
		return (Map<String, String>) createResourceObject("outerPageSearchRules");// spring-resource.xml定義
	}

	/** 註冊頁出生年 */
	public final Map<String, String> getSystemYearMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		DateFormat sdfYMD = new SimpleDateFormat("yyyy");
		String yearStr = sdfYMD.format(systemDate);
		int year = Integer.parseInt(yearStr);
		for (int i = year; i >= year - 100; i--)
			map.put("" + i, "" + i);
		return map;
	}

	/** 註冊頁出生月 */
	public final Map<String, String> getSystemMonthMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("01", "01");
		map.put("02", "02");
		map.put("03", "03");
		map.put("04", "04");
		map.put("05", "05");
		map.put("06", "06");
		map.put("07", "07");
		map.put("08", "08");
		map.put("09", "09");
		map.put("10", "10");
		map.put("11", "11");
		map.put("12", "12");
		return map;
	}

	/** 信用卡有效：年 */
	public final Map<String, String> getSystemYearMap2() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		DateFormat sdfYMD = new SimpleDateFormat("yyyy");
		String yearStr = sdfYMD.format(systemDate);
		int year = Integer.parseInt(yearStr);
		for (int i = year; i <= year + 10; i++)
			map.put("" + i, "" + i);
		return map;
	}
}