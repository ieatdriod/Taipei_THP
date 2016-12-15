package tw.com.mitac.thp.util;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.SerialRule;
import tw.com.mitac.ssh.util.BigDecimalTypeConverter;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.ssh.util.TimeTypeConverter;
import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.thp.bean.SysBillnomanagement;

public class Util implements ProjectArea {
	/** shoppingCartMap */
	public static final String SHOPPING_CART_MAP = "shoppingCartMap";
	protected static ResourceBundle setting;

	public static ResourceBundle globalSetting() {
		if (Util.setting == null)
			Util.setting = ResourceBundle.getBundle("application");
		return Util.setting;
	}

	public static MtMultiTenancy defaultTenancy = new MtMultiTenancy();
	static {
		defaultTenancy.setSysid("default");
		defaultTenancy.setTenancyId("default");

		defaultTenancy.setDbCatalog("thp");
		defaultTenancy.setDbDriver("com.mysql.jdbc.Driver");
		defaultTenancy.setDbDialect("org.hibernate.dialect.MySQL5Dialect");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		defaultTenancy.setLatestEffectiveDate(cal.getTime());
	}

	public static final String beanPackage = "tw.com.mitac.thp.bean";
	/** 是否使用登入或跳過登入 */
	public static final boolean isLogin = new Boolean(globalSetting().getString("app.isLogin"));
	protected static Logger logger = Logger.getLogger(Util.class);

	public static final String INFO_STAR = "*";
	public static String lnStr = new Character((char) 13).toString() + new Character((char) 10).toString();

	public static void defaultPK(Object bean) {
		try {
			Object pk = PropertyUtils.getProperty(bean, PK);
			// Method getSysid = mo.getClass().getMethod("getSysid", new
			// Class[0]);
			// Object sysid = getSysid.invoke(mo, new Class[0]);
			if (StringUtils.isBlank((String) pk)) {
				String className = bean.getClass().getSimpleName();
				pk = tw.com.mitac.ssh.util.SysidUtil.generateSysid(className);
				PropertyUtils.setProperty(bean, PK, pk);
				// Method setSysid = mo.getClass().getMethod("setSysid", new
				// Class[] { String.class });
				// setSysid.invoke(mo, new Object[] { sysid });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void resetBasisValue(Object bean) {
		String[] logicArr = { PK, CR, OP, CD, OD, REMARK, BILLNO, SN };
		for (String logic : logicArr)
			try {
				PropertyUtils.setProperty(bean, logic, null);
			} catch (NoSuchMethodException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	// public static String encode(String pwd) {
	// try {
	// tipo.SI.DataEncrypt dataEncrypt = new tipo.SI.DataEncrypt();
	// return dataEncrypt.encrypt(pwd);
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

	public static String encode(String pwd) {
		return encode("SHA", pwd);
	}

	/**
	 * @param algorithm
	 *            "SHA" or "MD5"
	 * @param pwd
	 */
	public static String encode(String algorithm, String pwd) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance(algorithm);
			md.update(pwd.getBytes());
			byte[] digest = md.digest();
			return new javax.xml.bind.annotation.adapters.HexBinaryAdapter().marshal(digest);
		} catch (java.security.NoSuchAlgorithmException e) {
			logger.error(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 去除tag
	 * 
	 * @param input
	 * @return
	 */
	public static String replaceAllTag(String input) {
		if (StringUtils.isBlank(input))
			return "";
		logger.debug("input :" + input);
		input = input.replaceAll("[\\t\\n\\r]", "");// 将内容区域的回车换行去除

		// 定义script的正则表达式
		String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
		// 定义style的正则表达式
		String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";

		Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(input);
		input = m_script.replaceAll("");

		Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(input);
		input = m_style.replaceAll(""); // 过滤style标签

		String output = input.replaceAll("\\<.*?>", "");
		logger.debug("output:" + output);
		return output;
	}

	/**
	 * 去除line-height
	 * 
	 * @param input
	 * @return
	 */
	public static String replaceLineHeight(String input) {
		if (StringUtils.isBlank(input))
			return "";
		String output = input.replaceAll("line\\-height[\\s\\S]*?px", "");
		return output;
	}

	public static String buildJoinTableFrontKey(Class<?> clazz) {
		return tw.com.mitac.ssh.util.Util.buildJoinTableFrontKey(clazz);
	}

	public static String joinTableFrontKeyToClassName(String asString) {
		String key = asString.substring(0, 1).toUpperCase() + asString.substring(1);
		return key;
	}

	public static <T> Map<Integer, T> buildKeyByParameterMap(Collection<T> list) {
		Map<Integer, T> map = new LinkedHashMap<Integer, T>();

		int key = 0;
		for (T objBean : list)
			map.put(key++, objBean);

		return map;
	}

	/**
	 * 利用 paramter 當成key 建立map 當paramter == null 用排序當key
	 * 
	 * @param list
	 * @param parameter
	 *            串接成 paramter[0]_paramter[1]_.....
	 * @return
	 */
	public static <T> Map<String, T> buildKeyByParameterMap(Collection<T> list, String... parameters) {
		Map<String, T> map = new LinkedHashMap<String, T>();
		if (CollectionUtils.isEmpty(list))
			return map;
		try {
			for (T objBean : list) {
				// 參數不是空值 由parameter 組成key
				String key = "";
				for (String parameter : parameters)
					key += "_" + PropertyUtils.getProperty(objBean, parameter);
				if (StringUtils.isNotBlank(key))
					key = key.substring(1);
				map.put(key, objBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return map;
		}
		return map;
	}

	public static <T> Map<String, List<T>> buildKeyByParameterMapList(Collection<T> list, String parameter) {
		Map<String, List<T>> map = new LinkedHashMap<String, List<T>>();
		if (CollectionUtils.isEmpty(list))
			return map;
		if (parameter != null)
			// 參數不是空值 由parameter 組成key
			for (T objBean : list) {
				try {
					String key = (String) PropertyUtils.getProperty(objBean, parameter);
					List<T> tempList = map.get(key);
					if (CollectionUtils.isEmpty(tempList)) {
						tempList = new ArrayList<T>();
						map.put(key, tempList);
					}
					tempList.add(objBean);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		return map;
	}

	public static QueryGroup jqgridGroupJSONObjectToQueryGroup(Class<?> clazz, Class<?> joinClass, JSONObject group) {
		QueryGroup queryGroup = null;
		try {
			String groupOp = group.getString("groupOp");

			List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
			if (group.has("rules")) {
				JSONArray rules = group.getJSONArray("rules");
				queryRuleList = jqgridRulesJSONArrayToQueryRuleList(clazz, joinClass, rules);
			}

			List<QueryGroup> queryGroupList = new ArrayList<QueryGroup>();
			if (group.has("groups")) {
				JSONArray groups = group.getJSONArray("groups");
				for (int i = 0; i < groups.length(); i++) {
					JSONObject subGroup = groups.getJSONObject(i);
					queryGroupList.add(jqgridGroupJSONObjectToQueryGroup(clazz, joinClass, subGroup));
				}
			}

			queryGroup = new QueryGroup(groupOp, queryRuleList.toArray(new QueryRule[0]),
					queryGroupList.toArray(new QueryGroup[0]));
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return queryGroup;
	}

	public static List<QueryRule> jqgridRulesJSONArrayToQueryRuleList(Class<?> clazz, Class<?> joinClass,
			JSONArray rules) {
		String t1 = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
		String t2 = joinClass.getSimpleName().substring(0, 1).toLowerCase() + joinClass.getSimpleName().substring(1);

		List<QueryRule> queryRuleList = new ArrayList<QueryRule>();
		try {
			for (int i = 0; i < rules.length(); i++) {
				JSONObject rule = rules.getJSONObject(i);
				String field = rule.getString("field");
				String op = rule.getString("op");
				String data = rule.has("data") ? rule.getString("data") : "";

				Object bean = ConstructorUtils.invokeConstructor(clazz, null);
				PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean,
						field.replaceFirst(t1 + "_", ""));
				if (propertyDescriptor == null) {
					bean = ConstructorUtils.invokeConstructor(joinClass, null);
					propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean, field.replaceFirst(t2 + "_", ""));
				}
				if (propertyDescriptor != null) {

					QueryRule queryRule = new QueryRule(field, op, data);
					Class<?> fieldClazz = propertyDescriptor.getPropertyType();

					if (fieldClazz.equals(Boolean.class))
						queryRule = new QueryRule(field, op, new Boolean(data));
					else if (fieldClazz.equals(BigDecimal.class))
						try {
							queryRule = new QueryRule(field, op, BigDecimalTypeConverter.convertFromString(data));
						} catch (NumberFormatException e) {
							logger.warn(field + " data is not a " + fieldClazz);
						}
					else if (fieldClazz.equals(Long.class))
						queryRule = new QueryRule(field, op, Long.parseLong(data));
					else if (fieldClazz.equals(Date.class))
						queryRule = new QueryRule(field, op, DateTypeConverter.convertFromString(data));
					else if (fieldClazz.equals(Time.class))
						queryRule = new QueryRule(field, op, TimeTypeConverter.convertFromString(data));

					queryRuleList.add(queryRule);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return queryRuleList;
	}

	public static SerialRule toSerialRule(SysBillnomanagement operateBean) throws ClassNotFoundException {
		Class<?> targetClass = Class.forName(SysBillnomanagement.class.getPackage().getName() + "."
				+ operateBean.getClassname());
		return new SerialRule(targetClass, operateBean.getProperty(), operateBean.getHeadword(), operateBean
				.getHeadlength().intValue(), operateBean.getTotallength().intValue());
	}

	/**
	 * 用來取得再指定的路徑(packageName)中所包含的CLASS
	 * 
	 * @param packageName
	 * @return List<Class>
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();// 取得/WEB-INF/classes/的所在
		assert classLoader != null;// classLoader絕不為空
		String path = packageName.replace('.', '/');// 建成取得CLASS的路徑
		Enumeration<URL> resources = classLoader.getResources(path);// 將所有在該路徑中的資料夾或檔案之路徑(URL)取出為列舉
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			// 將列舉(Enumeration)轉成LIST,該列舉中值的數量會因為給訂的packageName的詳細度而改變,給得夠細,它甚至可以只有一個值,
			// 而因getResources方法回傳的是列舉，所以不論有多少值，都只好用迴圈進行轉換
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		List<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));// 找出路徑中所含的CLASS並加入到回傳的LIST中
		}
		return classes;
	}

	/**
	 * 用來遍歷指定的路徑(directory)中所有的檔案，並將符合指定的檔案類型的檔案放入回傳的LIST中(這裡可以回傳任何想找的檔案類型)
	 * 
	 * @param directory
	 * @param packageName
	 * @return List<Class>
	 * @throws ClassNotFoundException
	 */
	public static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {// 如果該路徑不存在則直接回傳空的LIST
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {// 當傳來的路徑(directory)是個資料夾而非單一檔案的時候
				assert !file.getName().contains(".");// 路徑中絕不包含.
				classes.addAll(findClasses(file, packageName + "." + file.getName()));// 再執行一次自己，進入這個資料夾內找單一檔案
			} else if (file.getName().endsWith(".class")) {// 當該路徑指向一個單一檔案並且是.class結尾時
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				// 回傳以指定字串為名的CLASS並加入到整個method會回傳的LIST中(-6是為了跳過檔名中".class"這六個字原)
				// 在這裡也可以將要找的檔案類型換掉，比如說file.getName().endsWith(".hbm.xml")之類的，就可以找別的東西
			}
		}
		return classes;
	}

	public static List<String> allIpList() throws SocketException {
		List<String> allIpList = new ArrayList<String>();
		Enumeration nets = NetworkInterface.getNetworkInterfaces();
		for (Object netInterface : Collections.list(nets)) {
			String ip = "";
			if (netInterface != null)
				for (Object inetAdd : Collections.list(((NetworkInterface) netInterface).getInetAddresses()))
					ip = ((InetAddress) inetAdd).getHostAddress();
			if (StringUtils.isNotBlank(ip) && !"127.0.0.1".equals(ip))
				allIpList.add(ip);
			// System.out.println("test:" + ip);
		}
		System.out.println("allIpList:" + allIpList);
		return allIpList;
	}

	public static Boolean ipValidate(String validateStr) {
		try {
			for (String ipStr : allIpList())
				if (ipStr.equals(validateStr))
					return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		// return true;//XXX 本機測試排程
	}

	public static Map<String, String> parseToMapByFormat(String displayCheck, String strCheck) {
		Map<String, String> dataMap = new LinkedHashMap<String, String>();
		// 去起始識別字 ~#aaa#
		if (!displayCheck.startsWith("#")) {
			String firstMark = displayCheck.substring(0, displayCheck.indexOf("#"));
			if (strCheck.startsWith(firstMark)) {
				strCheck = strCheck.substring(firstMark.length());
			}
			displayCheck = displayCheck.substring(firstMark.length());
		}
		// 去結束識別字 #aaa#~
		if (!displayCheck.endsWith("#")) {
			String endMark = displayCheck.substring(displayCheck.lastIndexOf("#") + 1);
			if (strCheck.endsWith(endMark)) {
				strCheck = strCheck.substring(0, strCheck.length() - endMark.length());
			}
			displayCheck = displayCheck.substring(0, displayCheck.length() - endMark.length());
		}

		while (displayCheck.indexOf("#") != -1) {
			int i1 = displayCheck.indexOf("#");
			int i2 = displayCheck.indexOf("#", i1 + 1);
			if (i2 == -1)
				break;
			String displayColumn = displayCheck.substring(i1 + 1, i2);
			displayCheck = displayCheck.replaceFirst(("#" + displayColumn + "#"), "");

			if (StringUtils.isBlank(displayCheck)) {
				dataMap.put(displayColumn, strCheck);
				strCheck = "";
			} else {
				// 去中間識別字
				// #aaa#~
				String splitMark = displayCheck;
				if (displayCheck.indexOf("#") != -1) {
					// #aaa#~#bbb#
					splitMark = displayCheck.substring(0, displayCheck.indexOf("#"));
				}
				displayCheck = displayCheck.replaceFirst(splitMark, "");
				dataMap.put(displayColumn, strCheck.substring(0, strCheck.indexOf(splitMark)));
				strCheck = strCheck.substring(strCheck.indexOf(splitMark) + splitMark.length());

			}
		}

		return dataMap;
	}

	public static String checkBrowser(String $agent) {
		logger.info("user-agent:" + $agent);
		String agent = "";
		if (StringUtils.contains($agent, "Trident/7.0; rv:11.0"))
			agent = "Internet Explorer 11.0";
		else if (StringUtils.contains($agent, "MSIE 10.0"))
			agent = "Internet Explorer 10.0";
		else if (StringUtils.contains($agent, "MSIE 9.0"))
			agent = "Internet Explorer 9.0";
		else if (StringUtils.contains($agent, "MSIE 8.0"))
			agent = "Internet Explorer 8.0";
		else if (StringUtils.contains($agent, "MSIE 7.0"))
			agent = "Internet Explorer 7.0";
		else if (StringUtils.contains($agent, "MSIE 6.0"))
			agent = "Internet Explorer 6.0";
		else if (StringUtils.contains($agent, "Firefox/3"))
			agent = "Firefox 3";
		else if (StringUtils.contains($agent, "Firefox/2"))
			agent = "Firefox 2";
		else if (StringUtils.contains($agent, "Chrome"))
			agent = "Google Chrome";
		else if (StringUtils.contains($agent, "Safari"))
			agent = "Safari";
		else if (StringUtils.contains($agent, "Opera"))
			agent = "Opera";
		else
			agent = $agent;
		return agent;
	}
}