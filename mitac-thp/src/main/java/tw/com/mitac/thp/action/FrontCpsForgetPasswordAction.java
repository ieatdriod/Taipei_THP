package tw.com.mitac.thp.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.Util;

/**
 * 前台忘記密碼
 */
public class FrontCpsForgetPasswordAction extends BasisTenancyAction {
	final Base64 base64 = new Base64();

	protected CpsSiteMember bean;

	public final CpsSiteMember getBean() {
		return bean;
	}

	public final void setBean(CpsSiteMember bean) {
		this.bean = bean;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	/** 查詢畫面輸入的email */
	public String ajaxDoForgetPassword() {
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");

		String cpsEmail = request.getParameter("cpsEmail").trim(); // 抓畫面上輸入的email
		logger.debug("查詢忘記密碼畫面輸入的email:" + cpsEmail);
		List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class,
				new QueryGroup(new QueryRule("email", cpsEmail)), new QueryOrder[0], null, null);
		if (memberList.size() == 0) {
			// 沒有查到
			resultMap.put("msg", getText("loginError.accountNotFound"));// 很抱歉，系統無法辨識此會員資料，請與客服中心連絡!
		} else {
			// 驗證碼
			String verifycode = request.getParameter("verifycode");
			List<String> msgList = recaptcha(verifycode);
			if (msgList.size() > 0) {
				StringBuilder msg = new StringBuilder();
				for (String error_code : msgList) {
					msg.append(error_code);
				}
				resultMap.put("msg", msg.toString());
				return JSON_RESULT;
			}

			String verifyEmailPath = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath()
					+ "/pages2/changePassword";
			logger.debug("驗證信連回的serverpath:" + verifyEmailPath);

			logger.debug("mail-Sysidcount:" + memberList.size());
			// 有查到，寄mail
			logger.debug("@@@send mail");
			for (int i = 0; i < memberList.size(); i++) {
				logger.debug("mail-Sysid:" + memberList.get(i).getSysid());

				// 找認證信範本
				List<CpsEmailTemplate> l = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(), CpsEmailTemplate.class,
						new QueryGroup(new QueryRule("emailId", "forgetPassword")), new QueryOrder[0], null, null);
				if (l.size() == 0) {
					resultMap.put("msg", "查無範本");
					return JSON_RESULT;
				}

				CpsEmailTemplate cpsEmailTemplate = l.get(0);
				String emailContent = cpsEmailTemplate.getEmailContent();

				// String emailHref = "\"按此認證，重設密碼\"";

				// 取得4位數(1000~9999)亂數作為驗證碼
				int fandomNum = (int) (Math.random() * 999 + 1000);
				logger.debug("@@@fandomNum:" + fandomNum);

				// 將驗證碼寫入資料表
				Map<String, Object> setMap = getUpdatePropertyMap();
				// 隨機亂碼數字
				setMap.put("emailVerifyCode", Integer.toString(fandomNum));
				// 有效時間 =(申請時間+24小時)
				Calendar codeTime = Calendar.getInstance();
				codeTime.setTime(new Date());
				codeTime.add(Calendar.DATE, 3);
				setMap.put("emailVerifyCodeTime", codeTime.getTime());

				String daoMsg = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(),
						new QueryGroup(new QueryRule(PK, memberList.get(i).getSysid())), setMap));

				logger.debug("mail-Sysid:" + memberList.get(i).getSysid());

				if ("success".equals(daoMsg)) {
					// String emailHref =
					// "http://localhost:8080"+request.getContextPath()+"/cps2/changePassword?urlSysid="
					// + SiteMemberList.get(i).getSysid().toString() + ""
					// + "&urlVerifyCode=" + Integer.toString(fandomNum);

					String emailHref = "urlSysid=" + memberList.get(i).getSysid().toString() + "" + "&urlVerifyCode="
							+ Integer.toString(fandomNum);

					String encodedText = "";
					try {
						// final String text = "字串文字";
						String text = emailHref;
						byte[] textByte = text.getBytes("UTF-8");
						// 編碼
						encodedText = base64.encodeToString(textByte);
						System.out.println("aaaaaaaa-" + encodedText);
						// 解碼
						System.out.println("bbbbbbbb-" + new String(base64.decode(encodedText), "UTF-8"));
					} catch (Exception e) {
						e.printStackTrace();
					}

					String verifyCode = "<a href=\"" + verifyEmailPath + "?" + encodedText + "\">按此認證，重設密碼<a>";

					logger.debug("@@@-emailHref:" + emailHref);
					logger.debug("@@@-verifyCode:" + verifyCode);

					// String verifyCode =
					// "<a
					// href=\"http://localhost:8080"+request.getContextPath()+"/cps2/changePassword\">\"按此認證，重設密碼\"<a>";
					emailContent = emailContent.replace("$CHANGE_PSAAWORD_LINK$", verifyCode);

					List<String> contentStringList = new ArrayList<String>();
					contentStringList.add(emailContent);
					new MailThread(new MailBean(cpsEmail, cpsEmailTemplate.getEmailTitle(), contentStringList),
							getSendMailSetting()).start();

					resultMap.put("isSuccess", true);
					resultMap.put("msg", getText("web.chcekMail"));

				} else {
					resultMap.put("msg", "There was a problem:" + daoMsg + "，Please check again!");
				}
			} // end for
		}
		return JSON_RESULT;
	}

	// 驗證時間
	public String confirmAging() {

		String urlPath = request.getQueryString(); // 抓認證信傳來的驗證碼
		logger.debug("接收認證信-urlPath:" + urlPath);

		if (StringUtils.isNotBlank(urlPath)) {
			String urlSysid = "";
			String urlVerifyCode = "";
			try {
				String decodeUrlPath = new String(base64.decode(urlPath), "UTF-8");
				logger.debug("解碼後資訊：" + decodeUrlPath);
				String[] arrPara = decodeUrlPath.split("&");

				for (int i = 0; i < arrPara.length; i++) {
					String[] arrValue = arrPara[i].split("=");
					if ("urlSysid".equals(arrValue[0].toString())) {
						urlSysid = arrValue[1].toString().trim();
					}
					if ("urlVerifyCode".equals(arrValue[0].toString())) {
						urlVerifyCode = arrValue[1].toString().trim();
					}
				}
				logger.debug("解碼完成SYSID-urlSysid:" + urlSysid);
				logger.debug("解碼完成資訊-urlVerifyCode:" + urlVerifyCode);

			} catch (Exception e) {
				e.printStackTrace();
			}

			SimpleDateFormat codeTimeSdf = new SimpleDateFormat("yyyy-mm-dd hh-mm-ss");
			Calendar codeTime = Calendar.getInstance();
			codeTime.setTime(new Date());

			List<CpsSiteMember> siteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class,
					new QueryGroup(new QueryRule(PK, urlSysid),
							new QueryRule("emailVerifyCodeTime", GT, codeTimeSdf.getCalendar().getTime())),
					new QueryOrder[0], null, null);
			logger.debug("檢查時間+PK是否符合：" + siteMemberList.size());
			if (siteMemberList.size() == 0) {
				logger.debug("時間驗證失敗-該連結失效");
				request.setAttribute("msg", "您的連結已經失效");
			} else {
				logger.debug("符合資訊");
			}

		}else{
			request.setAttribute("msg", "您的連結已經失效");
		}
		
		return SUCCESS;
	}

	// 修改密碼畫面
	public String ajaxDoChangePassword() {
		resultMap = new HashMap();
		// resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");

		// String urlSysid = request.getParameter("urlSysid"); //抓認證信傳來的會員sysid
		// String urlVerifyCode = request.getParameter("urlVerifyCode");
		// //抓認證信傳來的驗證碼
		// logger.debug("@@@@@@-urlSysid:" + urlSysid);
		// logger.debug("@@@@@@-urlVerifyCode:" + urlVerifyCode);

		String urlPath = request.getParameter("urlPath"); // 抓認證信傳來的驗證碼
		logger.debug("接收認證信-urlPath:" + urlPath);

		String urlSysid = "";
		String urlVerifyCode = "";
		try {
			String decodeUrlPath = new String(base64.decode(urlPath), "UTF-8");
			logger.debug("解碼後資訊：" + decodeUrlPath);
			String[] arrPara = decodeUrlPath.split("&");

			for (int i = 0; i < arrPara.length; i++) {
				String[] arrValue = arrPara[i].split("=");
				if ("urlSysid".equals(arrValue[0].toString())) {
					urlSysid = arrValue[1].toString().trim();
				}
				if ("urlVerifyCode".equals(arrValue[0].toString())) {
					urlVerifyCode = arrValue[1].toString().trim();
				}
			}
			logger.debug("解碼完成SYSID-urlSysid:" + urlSysid);
			logger.debug("解碼完成資訊-urlVerifyCode:" + urlVerifyCode);

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug("解碼完成開始查詢");
		// 查詢是否有這筆會員資料
		List<CpsSiteMember> SiteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class,
				new QueryGroup(new QueryRule(PK, urlSysid), new QueryRule("emailVerifyCode", urlVerifyCode)),
				new QueryOrder[0], null, null);

		resultString = "";
		logger.debug("ChangePassword-Sysidcount:" + SiteMemberList.size());
		if (SiteMemberList.size() > 0) {
			String newPassword = request.getParameter("newPassword"); // 抓畫面上輸入的密碼
			logger.debug("修改密碼畫面輸入的密碼:" + newPassword);

			Map<String, Object> setMap = getUpdatePropertyMap();

			newPassword = Util.encode(newPassword); // 編碼密碼
			logger.debug("修改密碼畫面編碼過的密碼:" + newPassword);
			// 密碼
			setMap.put("password", newPassword);
			// 隨機亂碼數字
			setMap.put("emailVerifyCode", null);
			// 有效時間
			setMap.put("emailVerifyCodeTime", null);

			if (StringUtils.isNotBlank(urlSysid)) {
				resultString = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(),
						new QueryGroup(new QueryRule(PK, urlSysid)), setMap));
			}
			
		} else {
			resultString = "會員資料比對失敗!";
		}

		logger.debug("@@@resultString:" + resultString);

		return JSON_RESULT;
	}
}