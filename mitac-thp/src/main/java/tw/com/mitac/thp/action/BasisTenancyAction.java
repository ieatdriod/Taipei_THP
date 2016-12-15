package tw.com.mitac.thp.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.mitac.email.SendMailSetting;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.SerialRule;
import tw.com.mitac.ssh.util.SerialGenerator;
import tw.com.mitac.thp.bean.BhsAdsA;
import tw.com.mitac.thp.bean.BhsAdsB;
import tw.com.mitac.thp.bean.BhsArticle;
import tw.com.mitac.thp.bean.BhsArticleType;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMarquee;
import tw.com.mitac.thp.bean.BhsMenu;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsProductsCategory;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsAdvertisement;
import tw.com.mitac.thp.bean.CpsAdvertisementRent;
import tw.com.mitac.thp.bean.CpsClickHistory;
import tw.com.mitac.thp.bean.CpsConfig;
import tw.com.mitac.thp.bean.CpsConfigAd;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsFaqType;
import tw.com.mitac.thp.bean.CpsMarquee;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreBrand;
import tw.com.mitac.thp.bean.HpsCoreBrandType;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.bean.HpsCoreSearchKeyword;
import tw.com.mitac.thp.bean.HpsPromoteLimit;
import tw.com.mitac.thp.bean.MtsAdsA;
import tw.com.mitac.thp.bean.MtsAdsB;
import tw.com.mitac.thp.bean.MtsAppointmentCase;
import tw.com.mitac.thp.bean.MtsArticleType;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsDoctor;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsMarquee;
import tw.com.mitac.thp.bean.MtsMenu;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorCategory;
import tw.com.mitac.thp.bean.MtsVendorProfile;
import tw.com.mitac.thp.bean.SysBillnomanagement;
import tw.com.mitac.thp.bean.SysConstant;
import tw.com.mitac.thp.bean.SysSetting;
import tw.com.mitac.thp.login.TenancyData;
import tw.com.mitac.thp.login2.UserData2;
import tw.com.mitac.thp.tree.BeanTreeNode;
import tw.com.mitac.thp.tree.TreeUtil;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

/**
 * 
 * @author everybody
 * 
 *         2016-06-06:{@link #getDataCpsConfigAdETable()}、
 *         {@link #getDataCpsMarqueeETable()}新增 by Adair <BR>
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BasisTenancyAction extends BasisAction {
	private static final long serialVersionUID = 1L;

	protected final String FILE_DEFAULT_CREATE() {
		return getSettingResource().get("file.defaultCreate");
	}

	@Override
	protected SessionFactory sf() {
		return getTenancyData().getTenancySessionFactory();
	}

	private Map<String, Object> tenancyApplication;

	@Override
	protected Map<String, Object> appMap() {
		if (this.tenancyApplication == null) {
			String tenancyKey = getTenancyData().getTenancy().getSysid();
			this.tenancyApplication = (Map<String, Object>) application.get(tenancyKey);
			if (this.tenancyApplication == null) {
				this.tenancyApplication = new HashMap<String, Object>();
				application.put(tenancyKey, this.tenancyApplication);
			}
		}
		return this.tenancyApplication;
	}

	protected final TenancyData getTenancyData() {
		return (TenancyData) session.get("tenancyData");
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * <pre>
	 * 是否為兩台主機
	 * </pre>
	 * 
	 * Low Balance 造成 appMap() 的 dataTable/dataMenu 的 (最新)有效性
	 */
	public final boolean getIsLowBalance() {
		return false;
	}

	/**
	 * <pre>
	 * 利用維護時間最大值判斷是否須重置資源
	 * </pre>
	 * 
	 * @param clazz
	 */
	protected final void checkLowBalanceData(Class<?> clazz) {
		if (getIsLowBalance()) {
			Map<String, Object> odMap = (Map<String, Object>) appMap().get("odMap");
			if (odMap == null) {
				odMap = new HashMap<String, Object>();
				appMap().put("odMap", odMap);
			}

			Object maxOd = odMap.get(clazz.getSimpleName());
			Object dbMaxOd = cloudDao.selectMax(sf(), clazz.getSimpleName(), OD, QueryGroup.DEFAULT);

			if (dbMaxOd != null) {
				if (maxOd != null) {
					if (!dbMaxOd.equals(maxOd))
						resetDataMap(clazz);
				}
				odMap.put(clazz.getSimpleName(), dbMaxOd);
			}
		}
	}

	protected final void resetDataMap(Class<?> clazz) {
		String dataMapKey = "data" + clazz.getSimpleName();

		for (Map<String, Object> application : new Map[] { this.appMap(), this.application }) {
			List<String> removeKeyList = new ArrayList<String>();
			for (Map.Entry<String, Object> entry : application.entrySet())
				if (entry.getKey().contains(dataMapKey))
					// if (entry.getKey().startsWith(dataMapKey))
					removeKeyList.add(entry.getKey());
			for (String key : removeKeyList)
				application.remove(key);
		}
	}

	protected <HB> Map<String, HB> createDataMap(Class<HB> clazz) {
		return createDataMap(clazz, "Map", null);
	}

	protected <HB> Map<String, HB> createDataMap(Class<HB> clazz, String subResourceKey, QueryGroup queryGroup) {
		checkLowBalanceData(clazz);
		String resourceKey = "data" + clazz.getSimpleName() + subResourceKey;
		Map<String, HB> targetMap = (Map<String, HB>) appMap().get(resourceKey);
		if (targetMap == null) {
			targetMap = new LinkedHashMap<String, HB>();
			try {
				List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
				Object queryInstance = org.apache.commons.beanutils.ConstructorUtils.invokeConstructor(clazz, null);
				if (PropertyUtils.getPropertyDescriptor(queryInstance, DATA_ORDER) != null) {
					boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), DATA_ORDER);
					if (!isColumnHidden)
						queryOrderList.add(new QueryOrder(DATA_ORDER));
				}
				if (queryOrderList.isEmpty())
					if (PropertyUtils.getPropertyDescriptor(queryInstance, NAME) != null) {
						boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), NAME);
						if (!isColumnHidden)
							queryOrderList.add(new QueryOrder(NAME));
					}
				if (queryOrderList.isEmpty())
					if (PropertyUtils.getPropertyDescriptor(queryInstance, ID) != null) {
						boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), ID);
						if (!isColumnHidden)
							queryOrderList.add(new QueryOrder(ID));
					}

				List<HB> list = cloudDao.query(sf(), clazz, queryGroup, queryOrderList.toArray(new QueryOrder[0]),
						null, null);
				for (HB obj : list) {
					String pk = (String) PropertyUtils.getProperty(obj, PK);
					targetMap.put(pk, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			appMap().put(resourceKey, targetMap);
		}
		return targetMap;
	}

	protected <HB> Map<String, HB> createDataTable(Class<HB> clazz) {
		return createDataTable(clazz, "Table", null, null);
	}

	protected <HB> Map<String, HB> createDataTable(Class<HB> clazz, String subResourceKey, QueryGroup queryGroup,
			QueryOrder[] orders) {
		checkLowBalanceData(clazz);
		String resourceKey = "data" + clazz.getSimpleName() + subResourceKey;
		if (!skipChangeLocaleList.contains(clazz.getSimpleName()))
			resourceKey += "_" + getCookieLan();

		Map<String, HB> targetMap = (Map<String, HB>) appMap().get(resourceKey);
		if (targetMap == null) {
			targetMap = new LinkedHashMap<String, HB>();
			try {
				List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
				if (orders != null)
					for (QueryOrder order : orders)
						queryOrderList.add(order);
				Object queryInstance = org.apache.commons.beanutils.ConstructorUtils.invokeConstructor(clazz, null);
				if (PropertyUtils.getPropertyDescriptor(queryInstance, DATA_ORDER) != null) {
					boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), DATA_ORDER);
					if (!isColumnHidden)
						queryOrderList.add(new QueryOrder(DATA_ORDER));
				}
				if (queryOrderList.isEmpty())
					if (PropertyUtils.getPropertyDescriptor(queryInstance, NAME) != null) {
						boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), NAME);
						if (!isColumnHidden)
							queryOrderList.add(new QueryOrder(NAME));
					}
				if (queryOrderList.isEmpty())
					if (PropertyUtils.getPropertyDescriptor(queryInstance, ID) != null) {
						boolean isColumnHidden = isColumnHidden(clazz.getSimpleName(), ID);
						if (!isColumnHidden)
							queryOrderList.add(new QueryOrder(ID));
					}

				List<HB> list = cloudDao.queryTable(sf(), clazz, queryGroup, queryOrderList.toArray(new QueryOrder[0]),
						null, null);
				for (HB obj : list) {
					String pk = (String) PropertyUtils.getProperty(obj, PK);
					targetMap.put(pk, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			appMap().put(resourceKey, targetMap);
			addMultiLan(targetMap.values(), sf(), clazz);
		}
		return targetMap;
	}

	protected Map<String, String> createDataDisplay(Class<?> clazz) {
		return createDataDisplay(clazz, "Display", null);
	}

	protected Map<String, String> createDataDisplay(Class<?> clazz, String subResourceKey, QueryGroup queryGroup) {
		long t1 = System.currentTimeMillis();
		checkLowBalanceData(clazz);
		String resourceKey = "data" + clazz.getSimpleName() + subResourceKey;
		if (!skipChangeLocaleList.contains(clazz.getSimpleName()))
			resourceKey += "_" + getCookieLan();

		String displayFormat = DEFAULT_DISPLAY_FORMAT;
		try {
			displayFormat = tableToDisplay.getString(clazz.getSimpleName());
		} catch (MissingResourceException e1) {
		}

		Map<String, String> targetMap = (Map<String, String>) appMap().get(resourceKey);
		if (targetMap == null) {
			targetMap = new LinkedHashMap<String, String>();

			Map<String, Object> dataTable = null;
			if (queryGroup == null) {
				dataTable = (Map<String, Object>) appMap().get("data" + clazz.getSimpleName() + "Table");
				if (dataTable == null)
					dataTable = (Map<String, Object>) appMap().get("data" + clazz.getSimpleName() + "Map");
				if (dataTable == null) {
					int count = cloudDao.queryTableCount(sf(), clazz, null);
					if (count < 500)
						dataTable = (Map<String, Object>) createDataTable(clazz);
				}
			}

			if (dataTable != null) {
				try {
					for (Map.Entry<String, Object> entry : dataTable.entrySet()) {
						Object dataBean = entry.getValue();
						String display = displayFormat;
						while (display.indexOf("#") != -1) {
							int i1 = display.indexOf("#");
							int i2 = display.indexOf("#", i1 + 1);
							if (i2 == -1)
								break;
							String displayColumn = display.substring(i1 + 1, i2);
							Object displayValue = "";
							// if (!isColumnHidden(clazz.getSimpleName(),
							// displayColumn))
							{
								displayValue = PropertyUtils.getProperty(dataBean, displayColumn);
								if (displayValue == null)
									displayValue = "";
							}
							display = display.replace(("#" + displayColumn + "#"), displayValue.toString());
						}
						display = tw.com.mitac.ssh.util.Util.halfwidthToFullwidth(display);

						targetMap.put(entry.getKey(), display);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
				String displayCheck = displayFormat;
				while (displayCheck.indexOf("#") != -1) {
					int i1 = displayCheck.indexOf("#");
					int i2 = displayCheck.indexOf("#", i1 + 1);
					if (i2 == -1)
						break;
					String displayColumn = displayCheck.substring(i1 + 1, i2);
					if (!isColumnHidden(clazz.getSimpleName(), displayColumn)) {
						queryOrderList.add(new QueryOrder(displayColumn));
					}
					displayCheck = displayCheck.replace(("#" + displayColumn + "#"), "");
				}
				List<?> list = cloudDao.queryTable(sf(), clazz, queryGroup, queryOrderList.toArray(new QueryOrder[0]),
						null, null);
				addMultiLan(list, sf(), clazz);

				try {
					for (Object dataBean : list) {
						String display = displayFormat;
						while (display.indexOf("#") != -1) {
							int i1 = display.indexOf("#");
							int i2 = display.indexOf("#", i1 + 1);
							if (i2 == -1)
								break;
							String displayColumn = display.substring(i1 + 1, i2);
							// if (!isColumnHidden(clazz.getSimpleName(),
							// displayColumn))
							{
								Object displayValue = PropertyUtils.getProperty(dataBean, displayColumn);
								if (displayValue == null)
									displayValue = "";
								display = display.replace(("#" + displayColumn + "#"), displayValue.toString());
							}
						}
						display = tw.com.mitac.ssh.util.Util.halfwidthToFullwidth(display);

						targetMap.put((String) PropertyUtils.getProperty(dataBean, PK), display);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			appMap().put(resourceKey, targetMap);
		}
		long t2 = System.currentTimeMillis();
		if (getIsTest())
			logger.debug(clazz + " cost time:" + (t2 - t1));
		return targetMap;
	}

	// /**
	// * isEnabled:true
	// */
	// public Map<String, String> getDataCoreSoftwareEMenu() {
	// return createDataMenu(CoreSoftware.class, "EMenu", new
	// QueryGroup(AND,
	// new QueryRule[] { new QueryRule(IS_ENABLED, true) }, null));
	// }
	//
	// /**
	// * userType:CONSULTANT
	// */
	// public Map<String, String> getDataCoreConsulatantUcMenu() {
	// return createDataMenu(CoreConsulatant.class, "UcMenu", new
	// QueryGroup(AND,
	// new QueryRule[] { new QueryRule("userType", "CONSULTANT") },
	// null));
	// }
	// ---------- ---------- ---------- ---------- ----------
	public List<Map> formatListToStaticMap(Collection<?> list) {
		if (list == null)
			return null;
		List<Map> formatToMapResults = formatListToMap(list);
		Map<String, Map> menuMap = new LinkedHashMap<String, Map>();
		Map<String, Map> billMenuMap = new LinkedHashMap<String, Map>();

		// scan all data
		for (Map<String, Object> map : formatToMapResults) {
			for (String key : map.keySet()) {
				String fieldKey = key;
				if (key.contains("_")) {
					String[] arr = key.split("_");
					fieldKey = arr[1];
				}

				// 優先處理單據/多筆資料
				if (!billMenuMap.keySet().contains(key)) {
					try {
						String billName = (String) billSysidMapping.getObject(fieldKey);
						// Class<?> targetClass = Class.forName(Util.beanPackage
						// + "." + billName);
						billMenuMap.put(key, new LinkedHashMap());
					} catch (MissingResourceException e) {
						billMenuMap.put(key, null);
					}
					// catch (ClassNotFoundException e) {
					// bullMenuMap.put(key, null);
					// }
					catch (Exception e) {
						e.printStackTrace();
						billMenuMap.put(key, null);
					}
				}
				Map<String, String> billMenu = billMenuMap.get(key);
				if (billMenu != null) {
					String linkSysid = (String) map.get(key);
					billMenu.put(linkSysid, null);
				} else {
					if (!menuMap.keySet().contains(key)) {
						try {
							String className = coreSysidMapping.getString(fieldKey);
							Class<?> targetClass = Class.forName(Util.beanPackage + "." + className);
							menuMap.put(key, createDataDisplay(targetClass));
						} catch (MissingResourceException e) {
							menuMap.put(key, null);
						} catch (ClassNotFoundException e) {
							menuMap.put(key, null);
						} catch (Exception e) {
							e.printStackTrace();
							menuMap.put(key, null);
						}
					}
				}
			}
		}

		for (String key : billMenuMap.keySet()) {
			String fieldKey = key;
			if (key.contains("_")) {
				String[] arr = key.split("_");
				fieldKey = arr[1];
			}
			Map<String, String> billMenu = billMenuMap.get(key);
			if (billMenu != null && billMenu.size() > 0) {
				try {
					String billName = (String) billSysidMapping.getObject(fieldKey);
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

					List<Map> l = (List<Map>) cloudDao.findProperty(sf(), targetClass, new QueryGroup(new QueryRule(PK,
							IN, billMenu.keySet())), new QueryOrder[0], false, queryColumns.toArray(new String[0]));
					// List<Map> l = (List<Map>) cloudDao.findProperty(sf(),
					// targetClass, new QueryGroup(new QueryRule(PK,
					// IN, billMenu.keySet())), new QueryOrder[0], false, PK,
					// BILLNO);
					for (Map<String, String> map : l) {
						// billMenu.put(map.get(PK), map.get(BILLNO));
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

						billMenu.put(map.get(PK), display);
					}
				} catch (MissingResourceException e) {
					billMenuMap.put(key, null);
				} catch (ClassNotFoundException e) {
					billMenuMap.put(key, null);
				} catch (Exception e) {
					e.printStackTrace();
					billMenuMap.put(key, null);
				}
			}
		}

		for (String key : billMenuMap.keySet()) {
			Map<String, String> billMenu = billMenuMap.get(key);
			if (billMenu != null && billMenu.size() > 0)
				menuMap.put(key, billMenu);
		}

		for (Map<String, Object> map : formatToMapResults) {
			Map<String, Object> linkData = new LinkedHashMap<String, Object>();
			for (String key : map.keySet()) {
				Map<String, String> menu = menuMap.get(key);
				if (menu != null) {
					String linkSysid = (String) map.get(key);
					linkData.put(key + "Link", linkSysid);// java.util.ConcurrentModificationException
					if (StringUtils.isNotBlank(linkSysid))
						if (Util.INFO_STAR.equals(linkSysid))
							map.put(key, Util.INFO_STAR);
						else
							map.put(key, menu.get(linkSysid));
				}
			}
			map.putAll(linkData);
		}
		return formatToMapResults;
	}

	// ---------- ---------- ---------- ---------- ----------
	public Map<String, String> getSettingResource() {
		String resourceKey = "data" + SysSetting.class.getSimpleName() + "Resource";
		Map<String, String> targetMap = (Map) appMap().get(resourceKey);
		if (targetMap == null) {
			targetMap = new HashMap<String, String>();

			List<SysSetting> list = cloudDao.queryTable(sf(), SysSetting.class, QueryGroup.DEFAULT, new QueryOrder[0],
					null, null);
			for (SysSetting sysSetting : list)
				targetMap.put(sysSetting.getSettingId(), sysSetting.getSettingValue());

			appMap().put(resourceKey, targetMap);
		}
		return targetMap;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected String getLanImgSavePath() {
		return getSettingResource().get("file.lanImg");
	}

	public String getWebLanImg() {
		return getSettingResource().get("web.lanImg");
	}

	protected String getDfImgSavePath() {
		return getSettingResource().get("file.dfImg");
	}

	public String getWebDfImg() {
		return getSettingResource().get("web.dfImg");
	}

	// ---------- ---------- ---------- ---------- ----------
	protected final List<String> recaptcha(String verifycode) {
		// logger.info("verifycode:" + verifycode);
		// 紀錄曾經驗證過的驗證碼
		List<String> msgList = new ArrayList<String>();
		String recaptchaSuccessCode = (String) session.get("recaptchaSuccessCode");
		if (StringUtils.equals(recaptchaSuccessCode, verifycode)) {
			// logger.info("skip this check");
			return msgList;
		}

		String secret = getSettingResource().get("google.recaptcha.secretKey");
		String remoteAddr = request.getRemoteAddr();
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
		String verifyUrl = VERIFY_URL;
		verifyUrl += "?secret=" + secret;
		verifyUrl += "&response=" + verifycode;
		verifyUrl += "&remoteip=" + remoteAddr;
		// logger.info("secret:" + secret);
		// logger.info("remoteAddr:" + remoteAddr);

		try {
			HttpPost httpPost = new HttpPost(verifyUrl);

			HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200 && httpResponse.getEntity() != null) {
				String respText = EntityUtils.toString(httpResponse.getEntity());
				// logger.debug("tokenRespText:" + respText);
				JSONObject respObj = new JSONObject(respText);
				boolean success = respObj.getBoolean("success");
				if (success) {
					session.put("recaptchaSuccessCode", verifycode);
				} else {
					if (respObj.has("error-codes")) {
						JSONArray error_codes = respObj.getJSONArray("error-codes");
						for (int i = 0; i < error_codes.length(); i++) {
							String error_code = error_codes.getString(i);
							if ("missing-input-secret".equals(error_code))
								error_code = "尚未輸入登入密碼";
							else if ("invalid-input-secret".equals(error_code))
								error_code = "無效的登入密碼";
							else if ("missing-input-response".equals(error_code))
								error_code = getText("web.captchaBox");// 尚未進行安全驗證
							else if ("invalid-input-response".equals(error_code))
								error_code = "無效的驗證";
							msgList.add(error_code);
						}
					} else {
						msgList.add("無錯誤代碼");
					}
				}
			} else {
				msgList.add(
				// httpResponse.getStatusLine().getStatusCode()+" "+
				EntityUtils.toString(httpResponse.getEntity()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgList;
	}

	// ---------- ---------- ---------- ---------- ----------
	/**
	 * tableName == *
	 * 
	 * @return
	 */
	public Map<String, SysConstant> getDataSysConstantIdMap() {
		String idMapKey = "data" + SysConstant.class.getSimpleName() + "IdMap" + "Basis";
		Map targetIdMap = (Map) appMap().get(idMapKey);
		if (targetIdMap == null) {
			targetIdMap = new LinkedHashMap<String, SysConstant>();

			List<SysConstant> list = cloudDao.queryTable(sf(), SysConstant.class, new QueryGroup(new QueryRule(
					"tableName", Util.INFO_STAR)), new QueryOrder[0], null, null);
			for (SysConstant bean : list)
				targetIdMap.put(bean.getConstantId(), bean);

			appMap().put(idMapKey, targetIdMap);
		}
		return targetIdMap;
	}

	/**
	 * tableName == * 常數選單
	 * 
	 * @return
	 */
	public Map<String, Map> getConstantMenu() {
		Map<String, Map> constantMenu = new HashMap<String, Map>();

		String menuKey = "data" + SysConstant.class.getSimpleName() + "Resource" + "Basis" + getCookieLan();
		constantMenu = (Map<String, Map>) appMap().get(menuKey);
		if (constantMenu == null) {
			constantMenu = sysConstantIdMapToConstantMenu(getDataSysConstantIdMap());
			appMap().put(menuKey, constantMenu);

			// i18n
			for (String constantId : constantMenu.keySet()) {
				Map<String, String> menu = constantMenu.get(constantId);
				for (String optionKey : menu.keySet()) {
					// String optionValue=menu.get(optionKey);
					String i18nKey = "bean." + constantId + "." + optionKey;
					String optionValue = getText(i18nKey);
					if (!optionValue.equals(i18nKey)) {
						logger.debug("optionKey:" + optionKey + " optionValue:" + optionValue);
						menu.put(optionKey, optionValue);
					}
				}
			}
		}
		return constantMenu;
	}

	public final Map<String, SysConstant> getDataSysConstantIdMap(Class<?> clazz) {
		String idMapKey = "data" + SysConstant.class.getSimpleName() + "IdMap" + clazz.getSimpleName();
		Map targetIdMap = (Map) appMap().get(idMapKey);
		if (targetIdMap == null) {
			targetIdMap = findSysConstantIdMap(sf(), clazz);
			appMap().put(idMapKey, targetIdMap);
		}
		return targetIdMap;
	}

	/**
	 * 常數選單
	 * 
	 * @return
	 */
	public final Map<String, Map> getConstantMenu(Class<?> clazz) {
		String menuKey = "data" + SysConstant.class.getSimpleName() + "Resource" + clazz.getSimpleName()
				+ getCookieLan();
		Map<String, Map> constantMenu = (Map<String, Map>) appMap().get(menuKey);
		if (constantMenu == null) {
			constantMenu = sysConstantIdMapToConstantMenu(getDataSysConstantIdMap(clazz));
			appMap().put(menuKey, constantMenu);

			// i18n
			for (String constantId : constantMenu.keySet()) {
				Map<String, String> menu = constantMenu.get(constantId);
				for (String optionKey : menu.keySet()) {
					// String optionValue=menu.get(optionKey);
					String i18nKey = "bean." + constantId + "." + optionKey;
					String optionValue = getText(i18nKey);
					if (!optionValue.equals(i18nKey)) {
						// logger.debug("optionKey:" + optionKey +
						// " optionValue:" + optionValue);
						menu.put(optionKey, optionValue);
					}
				}
			}
		}
		return constantMenu;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected Map<String, SerialGenerator> serialGeneratorMap = new LinkedHashMap<String, SerialGenerator>();

	protected String generateBillno(Class<?> clazz, SysBillnomanagement operateBean, Integer index,
			QueryGroup queryGroup) {
		String no = "";
		try {
			SerialGenerator serialGenerator = serialGeneratorMap.get(operateBean.getSysid());
			if (serialGenerator == null) {
				SerialRule serialRule = Util.toSerialRule(operateBean);
				DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
				String head = (operateBean.getHeadword() + dateFormatter.format(systemDatetime)).substring(0,
						operateBean.getHeadlength().intValue());
				String maxNo = (String) cloudDao.selectMax(sf(), clazz.getSimpleName(), operateBean.getProperty(),
						new QueryGroup(new QueryRule(operateBean.getProperty(), BW, head)));
				serialGenerator = new SerialGenerator(serialRule, head, maxNo);

				serialGeneratorMap.put(operateBean.getSysid(), serialGenerator);
			}

			no = serialGenerator.generate(index);
		} catch (ClassNotFoundException e) {
		}
		return no;
	}

	protected final void defaultBillno(Object bean) {
		try {
			String site = null;
			if (PropertyUtils.getPropertyDescriptor(bean, SITE_SYSID) != null)
				site = (String) PropertyUtils.getProperty(bean, SITE_SYSID);
			defaultBillno(bean, null, site);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected final void defaultBillno(Object bean, Integer index, String siteSysid) {
		try {
			Map<String, SysBillnomanagement> sbMap = getDataSysBillnomanagementResourceMap().get(
					bean.getClass().getSimpleName());
			if (sbMap != null)
				for (SysBillnomanagement operateBean : sbMap.values()) {
					String no = (String) PropertyUtils.getProperty(bean, operateBean.getProperty());
					if (StringUtils.isNotBlank(no))
						continue;
					QueryGroup queryGroup = null;
					if (StringUtils.isNotBlank(siteSysid))
						queryGroup = new QueryGroup(new QueryRule(SITE_SYSID, siteSysid));
					else
						logger.debug("建立單號時，營運點是空值，發生在:" + getClass().getSimpleName() + "，日期是:" + systemDatetimeStr);
					no = generateBillno(bean.getClass(), operateBean, index, queryGroup);
					PropertyUtils.setProperty(bean, operateBean.getProperty(), no);
				}
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public Map<String, Map> getDataSysBillnomanagementResourceMap() {
		String resourceKey = "data" + SysBillnomanagement.class.getSimpleName() + "ResourceMap";
		Map<String, Map> targetMap = (Map<String, Map>) appMap().get(resourceKey);
		if (targetMap == null) {
			List<SysBillnomanagement> sysBillnomanagementList = cloudDao.queryTable(sf(), SysBillnomanagement.class,
					QueryGroup.DEFAULT, new QueryOrder[0], null, null);
			targetMap = new LinkedHashMap<String, Map>();
			for (SysBillnomanagement bean : sysBillnomanagementList) {
				String classname = bean.getClassname();
				Map<String, SysBillnomanagement> classnameMap = targetMap.get(classname);
				if (classnameMap == null) {
					classnameMap = new LinkedHashMap<String, SysBillnomanagement>();
					targetMap.put(classname, classnameMap);
				}
				classnameMap.put(bean.getProperty(), bean);
			}

			appMap().put(resourceKey, targetMap);
		}
		return targetMap;
	}

	public final CpsConfig getCpsConfig() {
		String resourceKey = "data" + CpsConfig.class.getSimpleName() + "OnlyOneTarget" + getCookieLan();
		CpsConfig cpsConfig = (CpsConfig) appMap().get(resourceKey);
		if (cpsConfig == null) {
			List<CpsConfig> list = cloudDao.queryTable(sf(), CpsConfig.class, QueryGroup.DEFAULT,
					new QueryOrder[] { new QueryOrder(PK) }, 0, 1);
			addMultiLan(list, sf(), CpsConfig.class);
			if (list.size() > 0)
				cpsConfig = list.get(0);
			else
				cpsConfig = new CpsConfig();

			appMap().put(resourceKey, cpsConfig);
		}
		return cpsConfig;
	}

	private SendMailSetting sendMailSetting;

	/**
	 * Mail info
	 */
	public final SendMailSetting getSendMailSetting() {
		if (sendMailSetting == null) {
			sendMailSetting = new SendMailSetting();
			sendMailSetting.setSmtpHostName(getCpsConfig().getSmtpServer());
			sendMailSetting.setSmtpPort(String.valueOf(getCpsConfig().getSmtpPort()));
			sendMailSetting.setMailAccountId(getCpsConfig().getSmtpAuthUsername());
			sendMailSetting.setMailAccountPassword(getCpsConfig().getSmtpAuthPassword());
			sendMailSetting.setMailAccountPersonal(getCpsConfig().getSmtpMailFromPersonal());
		}
		logger.debug("sendMailSetting:"
				+ ReflectionToStringBuilder.toString(sendMailSetting, ToStringStyle.MULTI_LINE_STYLE));
		return sendMailSetting;
	}

	// ---------- ---------- ---------- ---------- ----------
	public final Map<String, CpsCountry> getAllCountry() {
		Map<String, CpsCountry> allCountry = createDataTable(CpsCountry.class, "ETable", new QueryGroup(new QueryRule(
				IS_ENABLED, true)), new QueryOrder[] { new QueryOrder("isForeign"), new QueryOrder("dataId") });
		return allCountry;
	}

	public final Map<String, CpsEntity> getDataCpsEntityETable() {
		return createDataTable(CpsEntity.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(ID) });
	}

	/**
	 * 取得網站廣告設定檔cps_config_ad
	 * 
	 * @return
	 */
	public final Map<String, CpsConfigAd> getDataCpsConfigAdETable() {
		return createDataTable(CpsConfigAd.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) });
	}

	/**
	 * 取得大首頁公告訊息cps_marquee
	 * 
	 * @return
	 */
	public final Map<String, CpsMarquee> getDataCpsMarqueeETable() {
		return createDataTable(CpsMarquee.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(OD, DESC) });
	}

	/**
	 * 醫療館首頁-menu
	 */
	public final Map<String, MtsMenu> getDataMtsMenuTable() {
		return createDataTable(MtsMenu.class, "ETable", QueryGroup.DEFAULT, new QueryOrder[0]);
	}

	public final Map<String, MtsVendorCategory> getDataMtsVendorCategoryTable() {
		return createDataTable(MtsVendorCategory.class, "Table", QueryGroup.DEFAULT, new QueryOrder[] { new QueryOrder(
				"categoryName") });
	}

	/**
	 * 首頁-NEWS
	 */
	public Map<String, BhsArticle> getDataBhsArticleTable() {
		return createDataTable(BhsArticle.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	public Map<String, CpsMeeting> getCpsMeetingData() {
		return createDataTable(CpsMeeting.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	public Map<String, CpsAdvertisement> getCpsAdvertisementTable() {

		return createDataTable(CpsAdvertisement.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	public Map<String, CpsAdvertisementRent> getCpsAdvertisementRentTable() {

		return createDataTable(CpsAdvertisementRent.class, "RoundTable", new QueryGroup(new QueryRule("startDate", LE,
				systemDate), new QueryRule("endDate", GE, systemDate), new QueryRule(BILL_STATUS, IN,
		//
				new String[] { BillStatusUtil.APPROVED, BillStatusUtil.FINISH }
		// "wf75,Wf90"
				)), new QueryOrder[0]);
	}

	/**
	 * 商品類別
	 */
	public Map<String, HpsCoreItemType> getDataHpsCoreItemTypeTable() {
		return createDataTable(HpsCoreItemType.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	public Map<String, HpsCoreBrand> getDataHpsCoreBrandETable() {
		return createDataTable(HpsCoreBrand.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(ID) });
	}

	public Map<String, HpsCoreBrandType> getDataHpsCoreBrandTypeETable() {
		return createDataTable(HpsCoreBrandType.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(ID) });
	}

	// 限時搶購
	public Map<String, HpsPromoteLimit> getHpsPromoteLimitETable() {
		return createDataTable(HpsPromoteLimit.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	protected List<BeanTreeNode> createDataTree(Class clazz, String treeParentKey) {
		String resourceKey = "data" + clazz.getSimpleName() + "Tree";
		if (!skipChangeLocaleList.contains(clazz.getSimpleName()))
			resourceKey += "_" + getCookieLan();

		List<BeanTreeNode> targetTree = (List<BeanTreeNode>) appMap().get(resourceKey);
		if (targetTree == null) {
			List<QueryOrder> queryOrderList = new ArrayList<QueryOrder>();
			queryOrderList.add(new QueryOrder(treeParentKey));
			try {
				Object queryInstance = org.apache.commons.beanutils.ConstructorUtils.invokeConstructor(clazz, null);
				if (PropertyUtils.getPropertyDescriptor(queryInstance, DATA_ORDER) != null) {
					queryOrderList.add(new QueryOrder(DATA_ORDER));
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			List l = cloudDao.queryTable(sf(), clazz, QueryGroup.DEFAULT, queryOrderList.toArray(new QueryOrder[0]),
					null, null);
			addMultiLan(l, sf(), clazz);

			try {
				Map treeObj = new HashMap();
				treeObj.put("request", l);
				targetTree = TreeUtil.treeMachine(treeObj, treeParentKey, PK);
				appMap().put(resourceKey, targetTree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return targetTree;
	}

	public final List<BeanTreeNode> getDataMtsMenuTree() {
		return createDataTree(MtsMenu.class, "parentMtsMenuSysid");
	}

	public final List<BeanTreeNode> getDataBhsMenuTree() {
		return createDataTree(BhsMenu.class, "parentBhsMenuSysid");
	}

	/**
	 * 生技館首頁-menu
	 */
	public final Map<String, BhsMenu> getDataBhsMenuTable() {
		return createDataTable(BhsMenu.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	public final Map<String, BhsProductsCategory> getDataBhsProductsCategoryTable() {
		return createDataTable(BhsProductsCategory.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(NAME) });
	}

	public final Map<String, CpsCountry> getDataCpsCountryETable() {
		return createDataTable(CpsCountry.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(NAME) });
	}

	public final Map<String, CpsVendor> getDataCpsVendorTable() {
		return createDataTable(CpsVendor.class);
	}

	public final Map<String, MtsDoctor> getDataMtsDoctorTable() {
		return createDataTable(MtsDoctor.class);
	}

	public final Map<String, MtsVendorProfile> getDataMtsVendorProfileTable() {
		return createDataTable(MtsVendorProfile.class);
	}

	public final Map<String, MtsProducts> getDataMtsProductsTable() {
		return createDataTable(MtsProducts.class, "Table", QueryGroup.DEFAULT, new QueryOrder[] {
				new QueryOrder("vendorSysid"), new QueryOrder(DATA_ORDER) });
	}

	public final Map<String, MtsHighlight> getDataMtsHighlightTable() {
		return createDataTable(MtsHighlight.class);
	}

	public final Map<String, MtsCooperation> getDataMtsCooperationTable() {
		return createDataTable(MtsCooperation.class);
	}

	public final Map<String, BhsVendorProfile> getDataBhsVendorProfileTable() {
		return createDataTable(BhsVendorProfile.class);
	}

	public final Map<String, BhsProducts> getDataBhsProductsTable() {
		return createDataTable(BhsProducts.class);
	}

	public final Map<String, BhsHighlight> getDataBhsHighlightTable() {
		return createDataTable(BhsHighlight.class);
	}

	public final Map<String, BhsTechnology> getDataBhsTechnologyTable() {
		return createDataTable(BhsTechnology.class);
	}

	public final Map<String, MtsArticleType> getDataMtsArticleTypeTable() {
		return createDataTable(MtsArticleType.class, "Table", QueryGroup.DEFAULT, new QueryOrder[] { new QueryOrder(
				DATA_ORDER) });
	}

	public final Map<String, BhsArticleType> getDataBhsArticleTypeTable() {
		return createDataTable(BhsArticleType.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] { new QueryOrder(DATA_ORDER) });
	}

	public final String getCpsEntitySysid() {
		checkLowBalanceData(CpsEntity.class);
		String resourceKey = "data" + CpsEntity.class.getSimpleName() + "CpsEntitySysid";
		String entitySysid = (String) appMap().get(resourceKey);
		if (entitySysid == null) {
			List<String> eL = (List<String>) cloudDao.findProperty(sf(), CpsEntity.class, new QueryGroup(new QueryRule(
					ID, "cps")), new QueryOrder[0], false, PK);
			entitySysid = eL.size() > 0 ? eL.get(0) : "";
			appMap().put(resourceKey, entitySysid);
		}
		return entitySysid;
	}

	public final List<CpsFaqType> getCpsFaqTypeList() {
		checkLowBalanceData(CpsFaqType.class);
		String resourceKey = "data" + CpsFaqType.class.getSimpleName() + "CpsList";

		List<CpsFaqType> cpsFaqTypeList = (List<CpsFaqType>) appMap().get(resourceKey);

		if (cpsFaqTypeList == null) {
			cpsFaqTypeList = cloudDao.queryTable(sf(), CpsFaqType.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true), new QueryRule("entitySysid", getCpsEntitySysid())),
					new QueryOrder[] { new QueryOrder(ID) }, null, null);
			appMap().put(resourceKey, cpsFaqTypeList);
		}
		return cpsFaqTypeList;
	}

	public List<HpsCoreSearchKeyword> getKeywordList() {
		// 取得該集合的session
		List<HpsCoreSearchKeyword> hpsCoreSearchKeyword = (List<HpsCoreSearchKeyword>) session.get("KeywordList");
		// 判斷是否為null，如果是就產生新的值，不是就回傳
		if (hpsCoreSearchKeyword == null) {
			hpsCoreSearchKeyword = cloudDao.queryTable(sf(), HpsCoreSearchKeyword.class, new QueryGroup(
			// GT:> LT:< GE:>= LE:<= EQ=
					new QueryRule("startDate", LE, systemDate), new QueryRule("endDate", GE, systemDate)),

			new QueryOrder[] {
			// ASC 順時針 DESC逆時針
					new QueryOrder("startDate", DESC) },
					// 起始點預設
					0,
					// 顯示筆數
					8);
			// logger.debug(hpsCoreSearchKeyword);
			session.put("KeywordList", hpsCoreSearchKeyword);
		}
		// logger.debug(hpsCoreSearchKeyword);
		return hpsCoreSearchKeyword;
	}

	public final Map<String, CpsMeetingCfg> getDataCpsMeetingCfgTable() {
		return createDataTable(CpsMeetingCfg.class);
	}

	public Map<String, CpsConfigAd> getCpsConfigAdTable() {
		return createDataTable(CpsConfigAd.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)),
				new QueryOrder[0]);
	}

	public Map<String, CpsMarquee> getCpsMarqueeTable() {
		return createDataTable(CpsMarquee.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, MtsAdsA> getMtsAdsATable() {
		return createDataTable(MtsAdsA.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, MtsAdsB> getMtsAdsBTable() {
		return createDataTable(MtsAdsB.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, MtsMarquee> getMtsMarqueeTable() {
		return createDataTable(MtsMarquee.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, BhsAdsA> getBhsAdsATable() {
		return createDataTable(BhsAdsA.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, BhsAdsB> getBhsAdsBTable() {
		return createDataTable(BhsAdsB.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, BhsMarquee> getBhsMarqueeTable() {
		return createDataTable(BhsMarquee.class, "ETable", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	public Map<String, MtsAppointmentCase> getMtsAppointmentCaseTable() {
		return createDataTable(MtsAppointmentCase.class, "Table", new QueryGroup(new QueryRule(IS_ENABLED, true)), null);
	}

	// ---------- ---------- ---------- ---------- ----------
	protected <B> List<B> randomList(List<B> mArray) {
		return randomList(mArray, 0);
	}

	protected <B> List<B> randomList(List<B> mArray, int requestCount) {
		if (requestCount > mArray.size())
			requestCount = mArray.size();

		int mLength = mArray.size();
		int mRandom;
		B mNumber;// temp

		for (int i = 0; i < mLength; i++) {
			mRandom = (int) (Math.random() * mLength);
			mNumber = mArray.get(i);
			mArray.set(i, mArray.get(mRandom));
			mArray.set(mRandom, mNumber);
		}

		if (requestCount <= 0)
			return mArray;
		return mArray.subList(0, requestCount);
	}

	/**
	 * 取得隨機的 其他人也喜歡 pk值
	 * 
	 * @param clazz
	 * @param requestCount
	 * @param neKey
	 * @return
	 */
	protected <B> List<String> createRankList(Class<B> clazz, String imgCol, String neKey) {
		return createRankList(clazz, imgCol, neKey, 10);
	}

	/**
	 * 取得隨機的 其他人也喜歡 pk值
	 * 
	 * @param clazz
	 * @param requestCount
	 * @param neKey
	 * @return
	 */
	protected <B> List<String> createRankList(Class<B> clazz, String imgCol, String neKey, int requestCount) {
		// XXX 先用隨機

		String resourceKey = "data" + clazz.getSimpleName() + "_data" + CpsVendor.class.getSimpleName() + "SysidList";
		List<String> sysidList = (List<String>) appMap().get(resourceKey);
		if (sysidList == null) {
			List<QueryRule> rl = new ArrayList<QueryRule>();
			try {
				B bean = (B) ConstructorUtils.invokeConstructor(clazz, null);
				if (PropertyUtils.getPropertyDescriptor(bean, IS_ENABLED) != null)
					rl.add(new QueryRule(IS_ENABLED, true));
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<String> vendorSysidList = (List<String>) cloudDao.findProperty(sf(), CpsVendor.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true)), null, false, PK);
			rl.add(new QueryRule("vendorSysid", IN, vendorSysidList));
			if (StringUtils.isNotBlank(imgCol)) {
				rl.add(new QueryRule(imgCol, NN, null));
				rl.add(new QueryRule(imgCol, NE, ""));
			}
			sysidList = (List<String>) cloudDao.findProperty(sf(), clazz, new QueryGroup(rl.toArray(new QueryRule[0])),
					null, false, PK);
			appMap().put(resourceKey, sysidList);
		}

		List<String> dataList = new ArrayList<String>(sysidList);
		dataList.remove(neKey);

		List<String> randomSysidList = randomList(dataList, requestCount);

		return randomSysidList;
	}

	// ---------- ---------- ---------- ---------- ----------
	protected String[] class2EntityTypeAndDataType(Class<?> clazz) {
		String entityType = null, favouriteType = null;
		entityType = clazz.getSimpleName().substring(0, 3).toUpperCase();
		if (MtsVendorProfile.class.equals(clazz))
			favouriteType = "T";
		else if (MtsProducts.class.equals(clazz))
			favouriteType = "S";
		else if (MtsHighlight.class.equals(clazz))
			favouriteType = "H";
		else if (MtsCooperation.class.equals(clazz))
			favouriteType = "I";
		else if (BhsVendorProfile.class.equals(clazz))
			favouriteType = "B";
		else if (BhsProducts.class.equals(clazz))
			favouriteType = "P";
		else if (BhsTechnology.class.equals(clazz))
			favouriteType = "T";
		else if (BhsHighlight.class.equals(clazz))
			favouriteType = "H";

		return new String[] { entityType, favouriteType };
	}

	/**
	 * 累計點擊次數
	 * 
	 * @param clazz
	 * @param sourceSysid
	 * @return
	 */
	protected boolean addClickHistory(Class<?> clazz, String vendorSysid, String sourceSysid) {
		CpsClickHistory h = new CpsClickHistory();
		Util.defaultPK(h);
		defaultValue(h);

		UserData2 userData2 = (UserData2) session.get("userData2");
		if (userData2 != null) {
			CpsSiteMember a = userData2.getAccount();
			h.setCountrySysid(a.getCountrySysid());
			h.setGender(a.getGender());
			h.setBirthday(a.getBirthday());
			if (a.getBirthday() != null) {
				Calendar cal1 = Calendar.getInstance(), cal2 = Calendar.getInstance();
				cal1.setTime(a.getBirthday());
				h.setAge(cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR));
			}
		}
		String[] msgs = class2EntityTypeAndDataType(clazz);
		h.setEntityType(msgs[0]);
		h.setFavouriteType(msgs[1]);
		h.setVendorSysid(vendorSysid);
		h.setSourceSysid(sourceSysid);
		String daoMsg = cloudDao.save(sf(), new Object[] { h }, false, "INSERT");
		return SUCCESS.equals(daoMsg);
	}
	
	
	public String getBhsPath(){
		String bhsPath = "/pages2/BIO_001" ;
		return bhsPath;
	}
	
	public String getMtsPath(){
		String mtsPath = "/pages2/MED_001" ;
		return mtsPath;
	}
	
	public String getHotTopicPath(){
		String hotTopicPath = "/pages2/HotTopic" ;
		return hotTopicPath;
	}
}