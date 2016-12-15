package tw.com.mitac.thp.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsQa;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "unchecked", "rawtypes" })
/**
 * 意見反應
 */
public class FrontFeedbackAction extends BasisTenancyAction {
	/**
	 * 送出後執行部分
	 */
	public String toSubmit() throws IOException {
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");

		// 根據form取值
		String form = request.getParameter("form").replaceAll("^\"|\"$", "");
		String[] dataArr = form.split("&");
		Map<String, String> dataMap = new HashMap<String, String>();

		for (String s : dataArr) {
			String[] ss = s.split("=");
			if (ss.length == 1) {
				dataMap.put(ss[0], "");
			} else {
				dataMap.put(ss[0], ss[1].replace("+", " "));
			}
		}

		String uresponse = dataMap.get("g-recaptcha-response");
		List<String> msgList = recaptcha(uresponse);
		if (msgList.size() > 0) {
			StringBuilder msg = new StringBuilder();
			for (String error_code : msgList) {
				msg.append(error_code);
			}
			resultMap.put("msg", msg.toString());
			return JSON_RESULT;
		}

		String qaSubject = dataMap.get("qaSubject");
		if (StringUtils.isBlank(qaSubject)) {
			resultMap.put("msg", "標題不為空");
			return JSON_RESULT;
		}

		String memberSysid = "";
		if (dataMap.get("accRadio").equalsIgnoreCase("yes")) {
			// 是否為會員?是
			List<String> mL = (List<String>) cloudDao.findProperty(sf(), CpsSiteMember.class,
					new QueryGroup(new QueryRule("uuid", dataMap.get("account"))), new QueryOrder[0], false, PK);
			if (mL.size() > 0)
				memberSysid = mL.get(0);
		}

		CpsQa cpsQa = new CpsQa();
		Util.defaultPK(cpsQa);// PK
		defaultValue(cpsQa);// 預設資料填入

		// /mitac-thp/pages2/CPS_FW_004?qaFor=MTS
		// /mitac-thp/pages2/CPS_FW_004?qaFor=BHS
		if (StringUtils.isBlank(dataMap.get("qaFor")))
			cpsQa.setQaDepartment("CPS");// 處理單位CPS
		else
			cpsQa.setQaDepartment(dataMap.get("qaFor"));

		String createName = StringUtils.defaultString(dataMap.get("createName"));
		String qaText = StringUtils.defaultString(dataMap.get("qaText"));
		cpsQa.setQaType("U");// 客服單狀態
		cpsQa.setQaTitle(StringUtils.defaultString(dataMap.get("qaSubject")));// 提問主題
		cpsQa.setQaText(qaText);// 提問內容
		logger.debug("createName:" + createName);
		cpsQa.setCreateName(createName);// 提問人員
		cpsQa.setEmail(StringUtils.defaultString(dataMap.get("email")));// 提問人員電子郵件
		cpsQa.setMemberSysid(memberSysid);// 如果是會員就寫入SYSID，不是預設帶空字串
		String daoMsg = cloudDao.save(sf(), cpsQa);

		if (SUCCESS.equals(daoMsg)) {

			emailClient(createName, qaText, StringUtils.defaultString(dataMap.get("email")));

			emailVendor(createName, qaText, dataMap.get("qaFor"));

			// 回饋前台資料已完成
			resultMap.put("isSuccess", true);
			resultMap.put("msg", getText("web.submitSuccess"));

		}

		return JSON_RESULT;
	}

	/**
	 * 寄送使用者信件
	 */
	protected String emailClient(String createName, String qaText, String email) {

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "cpsQa_Receive_Confirmation")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() == 0) {
			resultMap.put("msg", "確認信件異常，請告知管理員");
			return JSON_RESULT;
		}

		CpsEmailTemplate cpsQaReceiveConfirmationEmailTemplate = emailTemplate.get(0);
		// 寫入Email資訊
		String emailContent = cpsQaReceiveConfirmationEmailTemplate.getEmailContent();

		/** 填寫人名稱 */
		if (StringUtils.isNotBlank(createName)) {
			emailContent = emailContent.replace("$CLIENT_NAME$", createName);
		}
		/** 填寫內容 */
		if (StringUtils.isNotBlank(qaText)) {
			emailContent = emailContent.replace("$CLIENT_QUESTIONS$", qaText);
		}

		List<String> cpsQaReceiveConfirmationList = new ArrayList<String>();
		cpsQaReceiveConfirmationList.add(emailContent);

		try {
			new MailThread(new MailBean(email, cpsQaReceiveConfirmationEmailTemplate.getEmailTitle(),
					cpsQaReceiveConfirmationList), getSendMailSetting()).start();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return SUCCESS;
	}

	/**
	 * 寄送後台客服人員
	 */
	protected String emailVendor(String createName, String qaText, String vendorSysid) {

		List<CpsEmailTemplate> emailTemplateForVendor = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "cpsQa_Notice_Vendor")),
				new QueryOrder[0], null, null);
		if (emailTemplateForVendor.size() == 0) {
			resultMap.put("msg", "確認信件異常，請告知管理員");
			return JSON_RESULT;
		}

		CpsEmailTemplate cpsQaNoticeVendorEmailTemplate = emailTemplateForVendor.get(0);

		/** 內容部分 */
		String emailContentForVendor = cpsQaNoticeVendorEmailTemplate.getEmailContent();

		// 填寫人名稱
		emailContentForVendor = emailContentForVendor.replace("$CLIENT_NAME$", createName);

		// 填寫內容
		emailContentForVendor = emailContentForVendor.replace("$CLIENT_QUESTIONS$", qaText);

		// 廠商名稱
		if (vendorSysid.equals("CPS")) {

			emailContentForVendor = emailContentForVendor.replace("$VENDOR_NAME$ ",  getText("web.node.cps"));

		} else if (vendorSysid.equals("MTS")) {

			emailContentForVendor = emailContentForVendor.replace("$VENDOR_NAME$ ", getText("web.node.mts"));

		} else if (vendorSysid.equals("BHS")) {

			emailContentForVendor = emailContentForVendor.replace("$VENDOR_NAME$ ", getText("web.node.bhs"));

		} else {

			List<CpsVendor> vendorList = cloudDao.queryTable(sf(), CpsVendor.class,
					new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);

			if (vendorList.size() > 0) {
				emailContentForVendor = emailContentForVendor.replace("$VENDOR_NAME$ ", vendorList.get(0).getName());
			}
		}

		List<String> cpsQaReceiveConfirmationList = new ArrayList<String>();
		cpsQaReceiveConfirmationList.add(emailContentForVendor);

		if (vendorSysid.equals("MTS") || vendorSysid.equals("BHS") || vendorSysid.equals("CPS")) {
			List<CpsMember> memberList = cloudDao.queryTable(sf(), CpsMember.class,
					new QueryGroup(new QueryRule("sourceType", "CpsEntity"), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[0], null, null);

			if (memberList.size() > 0) {
				for (CpsMember cpsMember : memberList) {
					try {
						new MailThread(new MailBean(cpsMember.getEmail(),
								cpsQaNoticeVendorEmailTemplate.getEmailTitle(), cpsQaReceiveConfirmationList),
								getSendMailSetting()).start();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				}
			}

		} else {
			List<CpsMember> memberList = cloudDao.queryTable(sf(), CpsMember.class,
					new QueryGroup(new QueryRule("sourceSysid", vendorSysid), new QueryRule(IS_ENABLED, true)),
					new QueryOrder[0], null, null);

			if (memberList.size() > 0) {
				for (CpsMember cpsMember : memberList) {
					try {
						new MailThread(new MailBean(cpsMember.getEmail(),
								cpsQaNoticeVendorEmailTemplate.getEmailTitle(), cpsQaReceiveConfirmationList),
								getSendMailSetting()).start();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				}
			}
		}
		return SUCCESS;
	}
}