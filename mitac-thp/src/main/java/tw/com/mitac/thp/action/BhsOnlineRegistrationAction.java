package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsArticle;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsExhibitionList;
import tw.com.mitac.thp.bean.CpsRegistration;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.MtsArticle;
import tw.com.mitac.thp.util.Util;

public class BhsOnlineRegistrationAction extends BasisFrontLoginAction {

	CpsRegistration bean;

	public CpsRegistration getBean() {
		return bean;
	}

	public void setBean(CpsRegistration bean) {
		this.bean = bean;
	}

	public String main() {

		String sysid = request.getParameter("sysid");
		CpsExhibitionList cpsExhibitionList = createDataTable(CpsExhibitionList.class).get(sysid);
		if (StringUtils.isBlank(sysid) || cpsExhibitionList == null) {
			request.setAttribute("isSuccess", false);
			return SUCCESS;
		}

		session.put("cpsExhibitionList", cpsExhibitionList);

		dfRegistration(sysid);
		sessionSet("tempBean", bean);
		return SUCCESS;
	}

	public String main_1() {

		String articleSysid = request.getParameter("articleSysid");
		BhsArticle bhsArticle = createDataTable(BhsArticle.class).get(articleSysid);
		if (StringUtils.isBlank(articleSysid) || bhsArticle == null) {
			request.setAttribute("isSuccess", false);
			return SUCCESS;
		}
		session.put("bhsArticle", bhsArticle);

		dfRegistration(articleSysid);
		sessionSet("tempBean", bean);
		return SUCCESS;
	}

	public String main_2() {

		String articleSysid = request.getParameter("articleSysid");
		MtsArticle mtsArticle = createDataTable(MtsArticle.class).get(articleSysid);
		if (StringUtils.isBlank(articleSysid) || mtsArticle == null) {
			request.setAttribute("isSuccess", false);
			return SUCCESS;
		}
		session.put("mtsArticle", mtsArticle);

		dfRegistration(articleSysid);
		sessionSet("tempBean", bean);
		return SUCCESS;
	}

	// 預設登入取值
	protected void dfRegistration(String sourceSysid) {
		CpsSiteMember member = getUserData2().getAccount();
		bean = new CpsRegistration();
		Util.defaultPK(bean);
		bean.setRegistrationName(member.getFirstName());
		bean.setLastName(member.getLastName());
		bean.setRegistrationCall(member.getMemberCall());
		bean.setRegistrationTitle(member.getMemberTitle());
		bean.setRegistrationUnit(member.getMemberUnit());
		bean.setRegistrationTel(member.getPhone());
		bean.setRegistrationTel2(member.getPhone2());
		bean.setCountryCode(member.getCountryCode());
		bean.setCountryCode2(member.getCountryCode2());
		bean.setRegistrationMail(member.getEmail());
		bean.setRegistrationCompany(member.getMemberCompany());
		bean.setRegistrationCountry(member.getCountrySysid());
		bean.setRegistrationAddr(member.getAddress());
		bean.setRegistrationWebsite(member.getMemberWebsite());
		bean.setRegistrationBus(member.getMemberBus());
		bean.setSiteMemberSysid(member.getSysid());
		bean.setSourceSysid(sourceSysid);
		bean.setRegistrationCompanyCountrySysid(member.getCompanyCountrySysid());
		bean.setRegistrationCompanyAreaCode(member.getCompanyAreaCode());
		String entityId = sourceSysid.substring(0, 3).toLowerCase();
		List<CpsEntity> cpsEntityList = cloudDao.queryTable(sf(), CpsEntity.class,
				new QueryGroup(new QueryRule(ID, entityId)), new QueryOrder[0], null, null);
		if (cpsEntityList.size() > 0)
			bean.setEntitySysid(cpsEntityList.get(0).getSysid());
	}

	public String toSubmit() {
		registrationType = "E";
		Boolean isSuccess = saveRegistration();
		request.setAttribute("isSuccess", isSuccess);
		if (isSuccess)
			sessionSet("tempBean", null);
		logger.debug("isSuccess:" + isSuccess + ",展覽館報名表:" + bean.getSysid());
		return SUCCESS;
	}

	public String toSubmit_1() {
		registrationType = "F";
		Boolean isSuccess = saveRegistration();
		request.setAttribute("isSuccess", isSuccess);
		if (isSuccess)
			sessionSet("tempBean", null);
		logger.debug("isSuccess:" + isSuccess + ",論壇報名表:" + bean.getSysid());
		return SUCCESS;
	}

	public String toSubmit_2() {
		registrationType = "F";
		Boolean isSuccess = saveRegistration();
		request.setAttribute("isSuccess", isSuccess);
		if (isSuccess)
			sessionSet("tempBean", null);
		logger.debug("isSuccess:" + isSuccess + ",論壇報名表:" + bean.getSysid());
		return SUCCESS;
	}

	protected String registrationType;

	protected boolean saveRegistration() {
		try {
			CpsRegistration tempBean = (CpsRegistration) sessionGet("tempBean");
			bean.setSysid(tempBean.getSysid());
			bean.setSiteMemberSysid(tempBean.getSiteMemberSysid());
			bean.setSourceSysid(tempBean.getSourceSysid());
			bean.setEntitySysid(tempBean.getEntitySysid());
			bean.setIsPay(false);
			bean.setRegistrationType(registrationType);
			defaultValue(bean);

			List saveList = new ArrayList();
			saveList.add(bean);
			String daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
			if (!daoMsg.equals(SUCCESS)) {
				logger.debug(daoMsg);
				return false;
			} else {
				logger.debug("論壇寄信客戶:" + bean.getRegistrationMail());
				String name = bean.getRegistrationName() + bean.getLastName();

				/** 取得Article名稱 */
				int m = bean.getSourceSysid().indexOf("Mts");
				int b = bean.getSourceSysid().indexOf("Bhs");

				String forumName = "";
				if (m != -1) {
					logger.debug("醫療館Articlet查詢");
					List<MtsArticle> mtsArticleList = cloudDao.queryTable(sf(), MtsArticle.class,
							new QueryGroup(new QueryRule(PK, bean.getSourceSysid())), new QueryOrder[0], null, null);
					if (mtsArticleList.size() > 0) {
						logger.debug("醫療館Article寫入");	
						forumName = mtsArticleList.get(0).getArticleTitle();
					}

				} else if (b != -1) {
					logger.debug("生技館Articlet查詢");
					List<BhsArticle> bhsArticleList = cloudDao.queryTable(sf(), BhsArticle.class,
							new QueryGroup(new QueryRule(PK, bean.getSourceSysid())), new QueryOrder[0], null, null);
					if (bhsArticleList.size() > 0) {
						logger.debug("生技館Article寫入");
						forumName = bhsArticleList.get(0).getArticleTitle();
					}

				}

				emailClient(name, bean.getRegistrationMail(), forumName);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String emailClient(String clientName, String clientEmail, String forumName) {

		logger.debug("寄信客戶資訊:" + clientName + "," + clientEmail);

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "application_Form_Confirmation")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() == 1) {
			CpsEmailTemplate emailClientEmailTemplate = emailTemplate.get(0);
			// 寫入Email資訊
			String emailContent = emailClientEmailTemplate.getEmailContent();

			/** 填寫人名稱 */
			if (StringUtils.isNotBlank(clientName)) {
				emailContent = emailContent.replace("$CLIENT_NAME$", clientName);
			}

			/** 論壇名稱 */
			if (StringUtils.isNotBlank(forumName)) {
				emailContent = emailContent.replace("$FORUM_NAME$", forumName);
			}

			/** 論壇日期 2016/12/07  暫時給空*/
			if (StringUtils.isNotBlank(forumName)) {
				emailContent = emailContent.replace("$FORUM_DATE$", "");
			}
			
			List<String> emailClientList = new ArrayList<String>();
			emailClientList.add(emailContent);

			try {
				new MailThread(new MailBean(clientEmail, emailClientEmailTemplate.getEmailTitle(), emailClientList),
						getSendMailSetting()).start();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		return SUCCESS;
	}

}