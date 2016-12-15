package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEmailTemplate;

/**
 * <pre>
 * 洽詢廣告
 * </pre>
 * 
 * 純寄信通知
 */
public class FrontCpsAdApplyAction extends BasisFrontLoginAction {
	public String toSubmit() {
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");

		// 圖形驗證碼
		String uresponse = request.getParameter("g-recaptcha-response");
		List<String> msgList = recaptcha(uresponse);
		if (msgList.size() > 0) {
			StringBuilder msg = new StringBuilder();
			for (String error_code : msgList) {
				msg.append(error_code);
			}
			resultMap.put("msg", msg.toString());
			return JSON_RESULT;
		}

		if (StringUtils.isBlank(getCpsConfig().getCallCenterEmail())) {
			resultMap.put("msg", "查無客服信箱，無法通知");
			return JSON_RESULT;
		}

		List<CpsEmailTemplate> emailTemplateList = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "advertisement_Consultation")),
				new QueryOrder[0], null, null);
		if (emailTemplateList.size() > 0) {

			CpsEmailTemplate emailTemplate = emailTemplateList.get(0);
			// 寫入Email資訊
			String emailContent = emailTemplate.getEmailContent();

			
			List<String> emailClientList = new ArrayList<String>();
			String content = "";
		
			String[] logicArr = { "firstName", "lastName", "email", "telephone", "mobilePhone", "country",
					"companyName", "industry", "regionsOfInterest", "content" };
			for (String logic : logicArr) {
				String i18nKey = "web." + logic;
				String i18nValue = getText(i18nKey);
				String msg = i18nValue + ":" + request.getParameter(logic);
				logger.info(msg);
				content += msg + "<br/>";
			}
			
			/** 改寫文內 */
			if (StringUtils.isNotBlank(content)) {
				emailContent = emailContent.replace("$INFORMATION$", content);
			}
			emailClientList.add(emailContent);
			
			/**寄信給客服*/
			try {
				new MailThread(new MailBean(getCpsConfig().getCallCenterEmail(), emailTemplate.getEmailTitle(),
						emailClientList), getSendMailSetting()).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			/**寄信給客戶*/
			emailClient();
		}

		resultMap.put("isSuccess", true);
		resultMap.put("msg", getText("web.submitSuccess"));
		return JSON_RESULT;
	}
	
	public String emailClient() {

		List<CpsEmailTemplate> emailTemplateList = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "advertisement_For_Confirmation")),
				new QueryOrder[0], null, null);
		if (emailTemplateList.size() > 0) {

			CpsEmailTemplate emailTemplate = emailTemplateList.get(0);
			// 寫入Email資訊
			String emailContent = emailTemplate.getEmailContent();

			
			List<String> emailClientList = new ArrayList<String>();
			String content = "";
		
			String[] logicArr = { "firstName", "lastName", "email", "telephone", "mobilePhone", "country",
					"companyName", "industry", "regionsOfInterest", "content" };
			for (String logic : logicArr) {
				String i18nKey = "web." + logic;
				String i18nValue = getText(i18nKey);
				String msg = i18nValue + ":" + request.getParameter(logic);
				logger.info(msg);
				content += msg + "<br/>";
			}
			
			/** 改寫文內 */
			if (StringUtils.isNotBlank(content)) {
				emailContent = emailContent.replace("$INFORMATION$", content);
			}
			emailClientList.add(emailContent);
			
			try {
				new MailThread(new MailBean(request.getParameter("email"), emailTemplate.getEmailTitle(),
						emailClientList), getSendMailSetting()).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return SUCCESS;
	}
	
	
}