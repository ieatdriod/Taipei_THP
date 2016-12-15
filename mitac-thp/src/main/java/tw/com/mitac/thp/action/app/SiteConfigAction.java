package tw.com.mitac.thp.action.app;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsConfig;

public class SiteConfigAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "app/SiteConfig1";
	}

	private String siteTitle;
	private String siteAdminEmail;
	private String callCenterEmail;
	private String medicalManagerEmail;
	private String medicalCallCenterEmail;
	private String bioCompaniesManagerEmail;
	private String bioCompaniesCallCenterEmail;
	private String siteMetaKeywords;
	private String siteMetaDescription;
	private String indexDesc1;
	private String indexDesc2;
	private String smtpServer;
	private Integer smtpPort;
	private String smtpMailFromPersonal;
	private String smtpAuthUsername;
	private String smtpAuthPassword;

	private String aboutUs;
	private String privacyPolicy;
	private String termsService;

	public String getSiteTitle() {
		return siteTitle;
	}

	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}

	public String getSiteAdminEmail() {
		return siteAdminEmail;
	}

	public void setSiteAdminEmail(String siteAdminEmail) {
		this.siteAdminEmail = siteAdminEmail;
	}

	public String getCallCenterEmail() {
		return callCenterEmail;
	}

	public void setCallCenterEmail(String callCenterEmail) {
		this.callCenterEmail = callCenterEmail;
	}

	public String getMedicalManagerEmail() {
		return medicalManagerEmail;
	}

	public void setMedicalManagerEmail(String medicalManagerEmail) {
		this.medicalManagerEmail = medicalManagerEmail;
	}

	public String getMedicalCallCenterEmail() {
		return medicalCallCenterEmail;
	}

	public void setMedicalCallCenterEmail(String medicalCallCenterEmail) {
		this.medicalCallCenterEmail = medicalCallCenterEmail;
	}

	public String getBioCompaniesManagerEmail() {
		return bioCompaniesManagerEmail;
	}

	public void setBioCompaniesManagerEmail(String bioCompaniesManagerEmail) {
		this.bioCompaniesManagerEmail = bioCompaniesManagerEmail;
	}

	public String getBioCompaniesCallCenterEmail() {
		return bioCompaniesCallCenterEmail;
	}

	public void setBioCompaniesCallCenterEmail(String bioCompaniesCallCenterEmail) {
		this.bioCompaniesCallCenterEmail = bioCompaniesCallCenterEmail;
	}

	public String getSiteMetaKeywords() {
		return siteMetaKeywords;
	}

	public void setSiteMetaKeywords(String siteMetaKeywords) {
		this.siteMetaKeywords = siteMetaKeywords;
	}

	public String getSiteMetaDescription() {
		return siteMetaDescription;
	}

	public void setSiteMetaDescription(String siteMetaDescription) {
		this.siteMetaDescription = siteMetaDescription;
	}

	public String getIndexDesc1() {
		return indexDesc1;
	}

	public void setIndexDesc1(String indexDesc1) {
		this.indexDesc1 = indexDesc1;
	}

	public String getIndexDesc2() {
		return indexDesc2;
	}

	public void setIndexDesc2(String indexDesc2) {
		this.indexDesc2 = indexDesc2;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public final String getSmtpMailFromPersonal() {
		return smtpMailFromPersonal;
	}

	public final void setSmtpMailFromPersonal(String smtpMailFromPersonal) {
		this.smtpMailFromPersonal = smtpMailFromPersonal;
	}

	public String getSmtpAuthUsername() {
		return smtpAuthUsername;
	}

	public void setSmtpAuthUsername(String smtpAuthUsername) {
		this.smtpAuthUsername = smtpAuthUsername;
	}

	public String getSmtpAuthPassword() {
		return smtpAuthPassword;
	}

	public void setSmtpAuthPassword(String smtpAuthPassword) {
		this.smtpAuthPassword = smtpAuthPassword;
	}

	public String getAboutUs() {
		return aboutUs;
	}

	public void setAboutUs(String aboutUs) {
		this.aboutUs = aboutUs;
	}

	public String getPrivacyPolicy() {
		return privacyPolicy;
	}

	public void setPrivacyPolicy(String privacyPolicy) {
		this.privacyPolicy = privacyPolicy;
	}

	public String getTermsService() {
		return termsService;
	}

	public void setTermsService(String termsService) {
		this.termsService = termsService;
	}

	public String init() {
		// TODO 把Config資料撈出來放在Session裡

		// 取得該集合的session
		List<CpsConfig> CpsConfigKeyword = (List<CpsConfig>) session.get("KeywordList");
		// 判斷是否為null，如果是就產生新的值，不是就回傳
		if (CpsConfigKeyword == null) {
			CpsConfigKeyword = cloudDao.queryTable(sf(), CpsConfig.class, new QueryGroup(
			// GT:> LT:< GE:>= LE:<= EQ=
			// new QueryRule("startDate",LE,systemDate),
			// new QueryRule("endDate",GE,systemDate)
					), new QueryOrder[0],
					// 起始點預設null
					0,
					// 顯示筆數null
					1);
			// logger.debug(hpsCoreSearchKeyword);
			session.put("KeywordList", CpsConfigKeyword);
		}
		return SUCCESS;
	}

	public String save() {
		// TODO 執行存檔 從頁面收到資料存進資料庫
		// logger.debug(siteTitle);
		// logger.debug(siteAdminEmail);
		// logger.debug(callCenterEmail);
		// logger.debug(medicalManagerEmail);
		// logger.debug(medicalCallCenterEmail);
		// logger.debug(bioCompaniesManagerEmail);
		// logger.debug(bioCompaniesCallCenterEmail);
		// logger.debug(siteMetaKeywords);
		// logger.debug(siteMetaDescription);
		logger.debug(indexDesc1);
		logger.debug(indexDesc2);
		// logger.debug(smtpServer);
		// logger.debug(smtpPort);
		// logger.debug(smtpAuthUsername);
		// logger.debug(smtpAuthPassword);
		//
		//
		// logger.debug(aboutUs);
		// logger.debug(privacyPolicy);
		// logger.debug(termsService);

		// 1.再查出來一次
		List<CpsConfig> CpsConfigKeyword = (List<CpsConfig>) session.get("KeywordList");
		if (CpsConfigKeyword == null) {
			CpsConfigKeyword = cloudDao.queryTable(sf(), CpsConfig.class, new QueryGroup(), new QueryOrder[0],
			// 起始點預設null
					0,
					// 顯示筆數null
					1);
			session.put("KeywordList", CpsConfigKeyword);
		}

		// 2.執行SET
		for (CpsConfig bean : CpsConfigKeyword) {
			bean.setSiteTitle(siteTitle);
			bean.setSiteAdminEmail(siteAdminEmail);
			bean.setCallCenterEmail(callCenterEmail);
			bean.setMedicalManagerEmail(medicalManagerEmail);
			bean.setMedicalCallCenterEmail(medicalCallCenterEmail);
			bean.setBioCompaniesManagerEmail(bioCompaniesManagerEmail);
			bean.setBioCompaniesCallCenterEmail(bioCompaniesCallCenterEmail);
			bean.setSiteMetaKeywords(siteMetaKeywords);
			bean.setSiteMetaKeywords(siteMetaDescription);
			bean.setIndexDesc1(indexDesc1);
			bean.setIndexDesc2(indexDesc2);
			bean.setSmtpServer(smtpServer);
			bean.setSmtpPort(smtpPort);
			bean.setSmtpMailFromPersonal(smtpMailFromPersonal);
			bean.setSmtpAuthUsername(smtpAuthUsername);
			bean.setSmtpAuthPassword(smtpAuthPassword);

			bean.setAboutUs(aboutUs);
			bean.setPrivacyPolicy(privacyPolicy);
			bean.setTermsService(termsService);

		}

		// 3.執行SAVE
		cloudDao.save(sf(), CpsConfigKeyword);
		resetDataMap(CpsConfig.class); // 重新刷新

		return SUCCESS;
	}
}