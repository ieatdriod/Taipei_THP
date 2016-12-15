package tw.com.mitac.thp.action;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.HqlStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsCollaboration;
import tw.com.mitac.thp.bean.MtsCollaborationFeedback;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

import com.opensymphony.xwork2.ActionContext;

/**
 * MtsCollaborationAction generated by GenCode.java
 */
public class MtsCollaborationAction extends DetailController<MtsCollaboration> {

	/** 框架尾檔 */
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsCollaborationFeedback.class));
		return detailClassMap;
	}

	/**
	 * <ol>
	 * <li>查詢是否過期72小時沒有處理的預約單</li>
	 * </ol>
	 */
	public List<MtsCollaboration> getMtsCollaborationTimeOutList() {
		// 今日時間減三天，就是回到過去三天
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -3);
		Date newDate = calendar.getTime();
		// 設定日期格式
		DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// 進行轉換
		String formatDateToString = formatDate.format(newDate);
		System.out.println(formatDateToString);

		List<MtsCollaboration> mtsCollaborationTimeOutList = null;
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsCollaborationTimeOutList = cloudDao.queryTable(sf(), MtsCollaboration.class,
					new QueryGroup(new QueryRule("sourceId", EQ, "MTS"),
							new QueryRule("collaborationStatus", EQ, "Pending"),
							new QueryRule("creationDate", LE, formatDateToString)),
					new QueryOrder[0], null, null);
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsCollaborationTimeOutList = cloudDao.queryTable(sf(), MtsCollaboration.class,
					new QueryGroup(new QueryRule("sourceId", EQ, getUserAccount().getSourceSysid()),
							new QueryRule("collaborationStatus", EQ, "Pending"),
							new QueryRule("creationDate", LE, formatDateToString)),
					new QueryOrder[0], null, null);
		}

		return mtsCollaborationTimeOutList;
	}

	/**
	 * <ol>
	 * <li>未讀+待處理</li>
	 * </ol>
	 */
	public List<MtsCollaboration> getMtsCollaborationIsReadList() {
		List<MtsCollaboration> mtsCollaborationIsReadList = null;
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsCollaborationIsReadList = cloudDao.queryTable(sf(), MtsCollaboration.class,
					new QueryGroup(new QueryRule("sourceId", EQ, "MTS"),
							new QueryRule("collaborationStatus", EQ, "Pending"), new QueryRule("isRead", false)),
					new QueryOrder[0], null, null);
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

			mtsCollaborationIsReadList = cloudDao.queryTable(sf(), MtsCollaboration.class,
					new QueryGroup(new QueryRule("sourceId", EQ, getUserAccount().getSourceSysid()),
							new QueryRule("collaborationStatus", EQ, "Pending"), new QueryRule("isRead", false)),
					new QueryOrder[0], null, null);
		}

		return mtsCollaborationIsReadList;
	}

	/** 框架MAIN頁面篩選 */
	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule(SOURCE_ID, "MTS"));
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule(SOURCE_ID, getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		// orders = new QueryOrder[] { new QueryOrder("collaborationStatus"),
		// new QueryOrder("substring(" +
		// Util.buildJoinTableFrontKey(getPersistentClass()) + "." + CD +
		// ",1,13)"), new QueryOrder("isRead") };
		Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<MtsCollaboration> list = (List<MtsCollaboration>) oldArr[1];
		for (MtsCollaboration bean : list) {
			String sourceId = bean.getSourceId();
			if (!"MTS".equals(sourceId)) {
				CpsVendor source = createDataTable(CpsVendor.class).get(sourceId);
				if (source != null)
					bean.setSourceId(source.getName());
			}
		}
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	/**
	 * [jqgrid] url
	 */
	@Override
	public String jqgridList() {
		String result = super.jqgridList();
		if (!StringUtils.equals("excel", oper)) {
			List<Map> formatToMapResults = (List<Map>) resultMap.get("gridModel");
			jagridDataPlugin(formatToMapResults);
		}
		return result;
	}

	protected void jagridDataPlugin(List<Map> formatToMapResults) {
		for (int i = 0; i < formatToMapResults.size(); i++) {
			Map<String, Object> map = formatToMapResults.get(i);
			Object pkObj = map.get(PK);

			String creationDate = (String) map.get(CD);
			map.put("oldCreationDate", creationDate);
			if (StringUtils.length(creationDate) > 10) {
				creationDate = StringUtils.substring(creationDate, 0, 10);
				map.put(CD, creationDate);
			}

			// redo button
			String url = request.getContextPath() + "/app/collaborationActionEdit?actionKey=" + getActionKey()
					+ "&sysid=" + pkObj;
			String clickBtnEditText = "<a href='" + url + "' target='_self'>"
					+ "<button type='button' class='mi-invisible-btn' style='width:65px;'>"
					+ "<i class='glyphicon glyphicon-edit' style='color:#ff3971;' title='"
					+ getText("jqgrid.clickBtnEdit") + "'/>" + "</button></a>";

			map.put("clickBtnEdit", clickBtnEditText);
		}
	}

	/** 預設查詢功能處理 */
	@Override
	public String main() {
		String result = super.main();
		if (getQueryCondition() == null) {
			beaninfo = new HashMap<String, String>();
			beaninfo.put("selectAP", "All");
			find();
		}
		return result;
	}

	/** 查詢時間狀態功能 */
	@Override
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();

		// 日期
		if (StringUtils.isNotBlank(beaninfo.get("issueDateGE"))) {
			Date date = DateTypeConverter.convertFromString(beaninfo.get("issueDateGE"));
			if (date != null) {
				rules.add(new QueryRule(CD, GE, sdf.format(date)));
			}
		}

		if (StringUtils.isNotBlank(beaninfo.get("issueDateLE"))) {
			Date date = DateTypeConverter.convertFromString(beaninfo.get("issueDateLE"));
			if (date != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(Calendar.DATE, 1);
				date = cal.getTime();
				rules.add(new QueryRule(CD, LE, sdf.format(date)));
			}
		}

		// 狀態
		if (StringUtils.isNotBlank(beaninfo.get("selectAP"))) {
			String selectAP = beaninfo.get("selectAP");

			if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {

				rules.add(new QueryRule(SOURCE_ID, EQ, "MTS"));

			} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {

				rules.add(new QueryRule(SOURCE_ID, EQ, getUserAccount().getSourceSysid()));

			}

			if (!selectAP.equals("All")) {
				rules.add(new QueryRule("collaborationStatus", EQ, selectAP));
			}
		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

	@Override
	protected Object jqgridDetailEditOrAdd(String resourceName, Class<?> clazz)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Object b = super.jqgridDetailEditOrAdd(resourceName, clazz);
		// 確認轉換型別
		if (b instanceof MtsCollaborationFeedback) {
			// 轉型
			MtsCollaborationFeedback a = (MtsCollaborationFeedback) b;
			// 如果是空值
			if (StringUtils.isBlank(a.getBackMemberSysid()) && StringUtils.isBlank(a.getMemberSysid())) {
				// 將值寫入
				a.setBackMemberSysid(getUserAccount().getSysid());
			}
		}
		return b;
	}

	public String print() {
		edit();

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("logo", FileUtil.createInputStream("medical_index_logo.jpg"));
		MtsCollaboration itemBean = bean;
		parameter.put("itemBean", itemBean);

		String countryName = (String) request.getAttribute("countryName");
		parameter.put("country", countryName);

		String ptValue = (String) request.getAttribute("collaborationProductsValue");
		parameter.put("ptValue", ptValue);

		String sourceId = bean.getSourceId();
		if (sourceId.equals("MTS")) {
			parameter.put("vendorName", getText("web.node.mts"));
		} else {
			CpsVendor cpsVendor = getDataCpsVendorTable().get(sourceId);
			if (cpsVendor != null) {
				parameter.put("vendorName", cpsVendor.getName().toString());
			}
		}

		try {
			String creationDate = (String) PropertyUtils.getProperty(itemBean, "creationDate");
			parameter.put("creationDate", creationDate.substring(0, 10).replace("/", "-"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		parameter.put(JRParameter.REPORT_LOCALE, ActionContext.getContext().getLocale());

		// ResourceBundle rb = ResourceBundle.getBundle("globalFields",
		// ActionContext.getContext().getLocale());

		byte[] rawData = FileUtil.export(null, parameter, "colabo");
		downInputStream = new ByteArrayInputStream(rawData);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		downFileName = df.format(systemDate) + "-" + getText("web.cooperationNeedsReceipt") + ".pdf";
		return DOWN_STREAM;
	}

	@Override
	public String edit() {
		String e = super.edit();

		/**
		 * 國家
		 */
		String countrySysid = bean.getCollaborationCountry();
		logger.debug("國家" + countrySysid);
		List<CpsCountry> cpsCountryList = cloudDao.queryTable(sf(), CpsCountry.class,
				new QueryGroup(new QueryRule(PK, countrySysid)), new QueryOrder[0], null, null);
		addMultiLan(cpsCountryList, sf(), CpsCountry.class);
		logger.debug("國家筆數:" + cpsCountryList.size());
		if (cpsCountryList.size() > 0) {
			request.setAttribute("countryName", cpsCountryList.get(0).getName());
		}

		request.setAttribute("collaborationProductsValue",
				collaborationProductsDisplay(bean.getCollaborationProducts()));

		/** 當進入畫面未讀改成已讀 */
		logger.debug("是否已讀" + bean.getIsRead());
		if (bean.getIsRead() == false) {
			Map<String, Object> setMap = getUpdatePropertyMap();
			setMap.put("isRead", true);
			HqlStatement hql = new UpdateStatement(MtsCollaboration.class.getSimpleName(),
					new QueryGroup(new QueryRule(PK, bean.getSysid())), setMap);
			cloudDao.save(sf(), hql);
		}

		return e;
	}

	/**
	 * 合作項目 逗號隔開
	 */
	protected String collaborationProductsDisplay(String collaborationProductsValue) {
		collaborationProductsValue = StringUtils.defaultString(collaborationProductsValue);
		/** 可選合作項目 */
		String cpValueA = "";
		/** 其他合作項目 */
		String cpValueB = "";
		String cpValueAUP = "";
		int pOR = StringUtils.indexOf(collaborationProductsValue, "OR:");
		if (pOR != -1) {
			cpValueA = collaborationProductsValue.substring(0, pOR);
			cpValueB = collaborationProductsValue.substring(pOR + 3);
		} else {
			cpValueA = collaborationProductsValue;
		}

		List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class,
				new QueryGroup(new QueryRule(PK, IN, cpValueA)), new QueryOrder[0], null, null);
		addMultiLan(mtsProductsList, sf(), MtsProducts.class);

		List<MtsCooperation> mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class,
				new QueryGroup(new QueryRule(PK, IN, cpValueA)), new QueryOrder[0], null, null);
		addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);

		if (mtsProductsList.size() > 0) {
			for (MtsProducts mtsProducts : mtsProductsList) {
				cpValueAUP += "," + mtsProducts.getName();
			}
		}

		if (mtsCooperationList.size() > 0) {
			for (MtsCooperation mtsCooperation : mtsCooperationList) {
				cpValueAUP += "," + mtsCooperation.getCooperationName();
			}
		}

		if (StringUtils.isNotBlank(cpValueB)) {
			cpValueAUP += "," + cpValueB;
		}

		if (StringUtils.isNotBlank(cpValueAUP)) {
			cpValueAUP = cpValueAUP.substring(1);
		}
		return cpValueAUP;
	}

	public String ajaxFeedbackList() {
		List resultList = new ArrayList();

		// sort by feedbackTime
		Set<MtsCollaborationFeedback> detailSet = (Set<MtsCollaborationFeedback>) findDetailSetWhenEdit(DETAIL_SET);
		logger.debug("detailSet:" + detailSet.size());

		Set<String> backMemberSysidSet = new HashSet<String>(), memberSysidSet = new HashSet<String>();
		for (MtsCollaborationFeedback obj : detailSet) {
			if (StringUtils.isNotBlank(obj.getBackMemberSysid()))
				backMemberSysidSet.add(obj.getBackMemberSysid());
			if (StringUtils.isNotBlank(obj.getMemberSysid()))
				memberSysidSet.add(obj.getMemberSysid());
		}
		Map<String, String> backNmMap = new HashMap<String, String>(), nmMap = new HashMap<String, String>();
		if (backMemberSysidSet.size() > 0) {
			List<Map> memberList = (List<Map>) cloudDao.findProperty(sf(), CpsMember.class,
					new QueryGroup(new QueryRule(PK, IN, backMemberSysidSet)), null, false, PK, "memberName");
			for (Map<String, String> map : memberList) {
				backNmMap.put(map.get(PK), map.get("memberName"));
			}
		}
		if (memberSysidSet.size() > 0) {
			List<Map> memberList = (List<Map>) cloudDao.findProperty(sf(), CpsSiteMember.class,
					new QueryGroup(new QueryRule(PK, IN, memberSysidSet)), null, false, PK, "memberName");
			for (Map<String, String> map : memberList) {
				nmMap.put(map.get(PK), map.get("memberName"));
			}
		}

		DateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (MtsCollaborationFeedback obj : detailSet) {
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("feedbackTime", f.format(obj.getFeedbackTime()));
			resultData.put("feedbackDetail", obj.getFeedbackDetail());

			String spokesman = "";
			if (StringUtils.isNotBlank(obj.getBackMemberSysid()))
				spokesman = backNmMap.get(obj.getBackMemberSysid());
			if (StringUtils.isNotBlank(obj.getMemberSysid()))
				spokesman = nmMap.get(obj.getMemberSysid());
			resultData.put("spokesman", spokesman);

			resultList.add(resultData);
		}

		this.resultList = resultList;
		return JSON_RESULT;
	}

	/**
	 * <ol>
	 * <li>存檔</li>
	 * <li>寄信</li>
	 * </ol>
	 */
	public String ajaxFeedbackSave() {
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "");

		String fk = request.getParameter("sysid");
		String billno = request.getParameter("billno");
		String clientName = request.getParameter("clientName");
		String clientMail = request.getParameter("clientMail");
		String feedbackDetail = request.getParameter("feedbackDetail");
		MtsCollaborationFeedback obj = getDefaultDMO(MtsCollaborationFeedback.class);
		Util.defaultPK(obj);
		defaultValue(obj);
		obj.setParentSysid(fk);
		obj.setFeedbackDetail(feedbackDetail);
		obj.setBackMemberSysid(getUserAccount().getSysid());
		String daoMsg = cloudDao.save(sf(), obj);
		if (StringUtils.equals(SUCCESS, daoMsg)) {
			resultMap.put("isSuccess", true);
			if (StringUtils.isNotBlank(clientMail)) {
				// vendor to client
				List<CpsEmailTemplate> emailTemplate = cloudDao.queryTable(sf(), CpsEmailTemplate.class,
						new QueryGroup(new QueryRule("emailId", "collaboration_V2C")), null, null, null);
				if (emailTemplate.size() == 0) {
					resultMap.put("msg", "查無範本信件可以寄出，請告知管理員沒有範本信件");
				} else {
					CpsEmailTemplate emailClientEmailTemplate = emailTemplate.get(0);

					/** 標題部分 */
					String emailTitle = emailClientEmailTemplate.getEmailTitle();
					// 單號
					emailTitle = emailTitle.replace("$BILLNO$", billno);

					/** 內容部分 */
					String emailContent = emailClientEmailTemplate.getEmailContent();
					// 會員名稱
					emailContent = emailContent.replace("$CLIENT_NAME$", clientName);
					// 單號
					emailContent = emailContent.replace("$BILLNO$", billno);
					// 廠商名稱
					emailContent = emailContent.replace("$VENDOR_NAME$", getUserAccount().getMemberName());
					// 回覆內容
					emailContent = emailContent.replace("$FEEDBACK$", feedbackDetail);

					List<String> emailClientList = new ArrayList<String>();
					emailClientList.add(emailContent);

					try {
						new MailThread(new MailBean(clientMail, emailTitle, emailClientList), getSendMailSetting())
								.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				resultMap.put("msg", "信件沒有寄出");
			}
		} else {
			resultMap.put("msg", daoMsg);
		}
		return JSON_RESULT;
	}

	/**
	 * <ol>
	 * <li>存檔</li>
	 * <li>寄信</li>
	 * </ol>
	 */
	public String ajaxBillStatusSave() {
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "");

		String pk = request.getParameter("sysid");
		String billno = request.getParameter("billno");
		String clientName = request.getParameter("clientName");
		String clientMail = request.getParameter("clientMail");
		String billStatus = request.getParameter("billStatus");
		String vendorSysid = request.getParameter("vendorSysid");
		Map<String, Object> setMap = getUpdatePropertyMap();
		setMap.put("collaborationStatus", billStatus);
		HqlStatement hql = new UpdateStatement(MtsCollaboration.class.getSimpleName(),
				new QueryGroup(new QueryRule(PK, pk)), setMap);
		String daoMsg = cloudDao.save(sf(), hql);
		if (StringUtils.equals(SUCCESS, daoMsg)) {
			resultMap.put("isSuccess", true);
			resultMap.put("msg", "狀態更改成功");

			/** 20161205設計改成僅接受拒絕寄信+分別寄出不同信件 */
			if (billStatus.equals("Accepted") || billStatus.equals("Declined")) {
				if (StringUtils.isNotBlank(clientMail)) {
					String emailId = "";

					if (billStatus.equals("Accepted")) {
						emailId = "collaboration_Accepted_Mts";
					} else if (billStatus.equals("Declined")) {
						emailId = "collaboration_Declined_Mts";
					}

					// 找認證信範本
					List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
							CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", emailId)),
							new QueryOrder[0], null, null);
					if (emailTemplate.size() == 0) {
						resultMap.put("msg", "查無範本信件可以寄出，請告知管理員沒有範本信件");
					} else {
						String vendorName = "";
						if (StringUtils.isNotBlank(vendorSysid)) {
							List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
									new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);
							if (vendorList.size() > 0) {
								vendorName = vendorList.get(0).getName();
							}
						}

						retext(emailTemplate.get(0), clientMail, billStatus, clientName, vendorName, billno);
					}
				} else {
					resultMap.put("msg", "信件沒有寄出");
				}
			}

		} else {
			resultMap.put("msg", daoMsg);
		}
		return JSON_RESULT;
	}

	protected final void retext(CpsEmailTemplate emailTemplate, String clientMail, String billStatus, String clientName,
			String vendorName, String billNo) {

		/** 標題部分 */
		String emailTitle = emailTemplate.getEmailTitle();
		// 單號
		emailTitle = emailTitle.replace("$BILLNO$", billNo);

		/** 內文部分 */
		String emailContent = emailTemplate.getEmailContent();
		// 單號
		emailContent = emailContent.replace("$BILLNO$", billNo);

		// 廠商名稱
		emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

		// 會員名稱
		emailContent = emailContent.replace("$CLIENT_NAME$", clientName);

		List<String> contentStringList = new ArrayList<String>();
		contentStringList.add(emailContent);
		new MailThread(new MailBean(clientMail, emailTitle, contentStringList), getSendMailSetting()).start();
	}

	/**
	 * 實作特殊排序
	 * <ol>
	 * <li>red</li>
	 * <li>blue</li>
	 * <li>Pending</li>
	 * <li>Processing</li>
	 * <li>Accepted</li>
	 * <li>Declined</li>
	 * </ol>
	 */
	public String jqgridList2() {
		logger.debug("start");
		List<QueryGroup> andQueryGroupsList = new ArrayList<QueryGroup>();
		QueryGroup queryCondition = getQueryCondition();
		if (queryCondition != null)
			andQueryGroupsList.add(queryCondition);
		QueryGroup queryRestrict = getQueryRestrict();
		if (queryRestrict != null)
			andQueryGroupsList.add(queryRestrict);

		QueryGroup q = new QueryGroup(AND, null, andQueryGroupsList.toArray(new QueryGroup[0]));
		QueryOrder[] orderArr = { new QueryOrder("collaborationStatus"),
				new QueryOrder("substring(" + Util.buildJoinTableFrontKey(getPersistentClass()) + "." + CD + ",1,13)",
						DESC),
				new QueryOrder("isRead") };
		Object[] arr = jqgridList(getPersistentClass(), q, orderArr, null, null);
		List results = (List) arr[1];

		List<Map> formatToMapResults = formatListToStaticMap(results);
		jagridDataPlugin(formatToMapResults);

		// resultList = formatToMapResults;

		List resultList = new ArrayList();

		// 今日時間減三天，就是回到過去三天
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -3);
		Date newDate = calendar.getTime();
		// 設定日期格式
		DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// 進行轉換
		String formatDateToString = formatDate.format(newDate);

		List<Map> redList = new ArrayList<Map>();
		List<Map> blueList = new ArrayList<Map>();
		List<Map> l1List = new ArrayList<Map>();
		List<Map> l2List = new ArrayList<Map>();
		List<Map> l3List = new ArrayList<Map>();
		List<Map> l4List = new ArrayList<Map>();
		for (int i = 0; i < formatToMapResults.size(); i++) {
			Map<String, Object> map = formatToMapResults.get(i);
			String billStatus = (String) map.get("collaborationStatus");
			if (StringUtils.equals(billStatus, "Pending")) {
				String creationDate = (String) map.get("oldCreationDate");
				Boolean isRead = (Boolean) map.get("isRead");
				if (creationDate.compareTo(formatDateToString) <= 0) {
					redList.add(map);
				} else if (!isRead) {
					blueList.add(map);
				} else {
					l1List.add(// 0,
							map);
				}
			} else if (StringUtils.equals(billStatus, "Processing")) {
				l2List.add(// 0,
						map);
			} else if (StringUtils.equals(billStatus, "Accepted")) {
				l3List.add(// 0,
						map);
			} else if (StringUtils.equals(billStatus, "Declined")) {
				l4List.add(// 0,
						map);
			}
		}
		resultList.addAll(redList);
		resultList.addAll(blueList);
		resultList.addAll(l1List);
		resultList.addAll(l2List);
		resultList.addAll(l3List);
		resultList.addAll(l4List);

		this.resultList = resultList;
		logger.debug("end");
		return JSON_RESULT;
	}
}