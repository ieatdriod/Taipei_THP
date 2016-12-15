package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsCollaboration;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;
import tw.com.mitac.thp.util.Util;

/**
 * <ul>
 * <li>合作需求單</li>
 * <li>視訊預約單</li>
 * </ul>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontMtsCollaborationAction extends BasisFrontLoginAction {

	protected MtsCollaboration bean;
	protected CpsSiteMember member;

	public MtsCollaboration getBean() {
		return bean;
	}

	public void setBean(MtsCollaboration bean) {
		this.bean = bean;
	}

	public CpsSiteMember getMember() {
		return member;
	}

	public void setMember(CpsSiteMember member) {
		this.member = member;
	}

	public String collaborationPage() {
		member = getUserData2().getAccount();
		bean = (MtsCollaboration) sessionRemove("bean");

		if (bean == null) {
			bean = new MtsCollaboration();
			Util.defaultPK(bean);
			bean.setCollaborationName(member.getMemberName());
			bean.setCollaborationTitle(member.getMemberTitle());

			// // 電話處理20160825
			// String memberPhone = "";
			// if (StringUtils.isNotBlank(member.getPhone())) {
			// if (StringUtils.isNotBlank(member.getCountryCode())) {
			// memberPhone = "(" + member.getCountryCode() + ")" +
			// member.getPhone();
			// } else {
			// memberPhone = member.getPhone();
			// }
			// } else if (StringUtils.isBlank(member.getPhone()) &&
			// StringUtils.isNotBlank(member.getPhone2())) {
			// if (StringUtils.isNotBlank(member.getCountryCode2())) {
			// memberPhone = "(" + member.getCountryCode2() + ")" +
			// member.getPhone2();
			// } else {
			// memberPhone = member.getPhone2();
			// }
			// }
			//
			// bean.setCollaborationPhone(memberPhone);

			bean.setCountryCode(member.getCountryCode());
			bean.setCollaborationPhone(member.getPhone());
			bean.setCountryCode2(member.getCountryCode2());
			bean.setReservePhone(member.getPhone2());

			bean.setCollaborationMail(member.getEmail());
			bean.setCollaborationCompany(member.getMemberCompany());
			bean.setCollaborationUnit(member.getMemberUnit());
			bean.setCollaborationCountry(member.getCountrySysid());
			bean.setCollaborationCompanyCountry(member.getCompanyCountrySysid());
			bean.setCollaborationCompanyAreaCode(member.getCompanyAreaCode());
			bean.setCollaborationAddr(member.getAddress());
			bean.setCollaborationWebsite(member.getMemberWebsite());
			bean.setCollaborationBus(member.getMemberBus());
			bean.setSiteMemberSysid(member.getSysid());

			String sysid = request.getParameter("sysid");
			// String vendorName = "";
			if (StringUtils.isBlank(sysid)) {
				bean.setSourceId("MTS");
			} else {
				String vendorSysid = sysid;
				bean.setSourceId(vendorSysid);
			}

		}

		System.out.println(bean.getSourceId());
		if (!bean.getSourceId().equals("MTS")) {

			// 取得公司基本資料
			// MtsVendorProfile mtsVendorProfile =
			// createDataTable(MtsVendorProfile.class).get(sysid);
			CpsVendor cpsVendor = createDataTable(CpsVendor.class).get(bean.getSourceId());
			if (cpsVendor == null) {
				request.setAttribute("isSuccess", false);
				request.setAttribute("errMsg", "vendor not found!");
				logger.warn("vendor not found!");
				return SUCCESS;
			}
			request.setAttribute("cpsVendor", cpsVendor);
			String vendorSysid = bean.getSourceId();

			// vendorName = cpsVendor.getName();

			request.setAttribute("coList", getCooperationList(vendorSysid));
		}

		// 處理合作項目其他:
		String othp = "OR:";
		if (StringUtils.isNotBlank(bean.getCollaborationProducts()) && bean.getCollaborationProducts().contains(othp)) {
			String[] string2 = bean.getCollaborationProducts().split(othp);
			String otherValueProducts = string2[string2.length - 1];
			request.setAttribute("otherValueProducts", otherValueProducts);
		}

		return SUCCESS;
	}

	protected List<Map> getCooperationList(String vendorSysid) {
		List<Map> list = (List<Map>) cloudDao.findProperty(sf(), MtsCooperation.class,
				new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)),
				new QueryOrder[] {}, false, PK, "cooperationName");
		return list;
	}

	public String collaborationSubmit() {
		Boolean isSuccess = saveCollaboration();

		request.setAttribute("isSuccess", isSuccess);
		// String sysid = request.getParameter("sysid");
		String sysid = bean.getSourceId();

		if (sysid.equals("MTS")) {

		} else {
			// 兩種方法
			List<MtsVendorProfile> mtsVendorProfile = cloudDao.queryTable(sf(), MtsVendorProfile.class,
					new QueryGroup(new QueryRule("vendorSysid", sysid)), new QueryOrder[0], null, null);
			request.setAttribute("cpsv", mtsVendorProfile.get(0).getSysid());

			// request.setAttribute("cpsv",sysid );
		}

		sessionRemove("bean");
		return SUCCESS;
	}

	/** 預覽頁面 */
	public String previewWithValue() {
		// 狀態：待處理
		bean.setCollaborationStatus("Pending");

		// 預約單號
		defaultBillno(bean);

		// 是否讀取-預設否
		bean.setIsRead(false);

		// 合作{項目}底下欄位處理
		logger.debug("合作項目勾選值" + bean.getCollaborationProducts());
		String other_collaborationProducts = request.getParameter("other_collaborationProducts");
		logger.debug("合作項目底下欄位資料：" + other_collaborationProducts);
		if (StringUtils.isNotBlank(other_collaborationProducts)) {
			String collaborationProducts = StringUtils.defaultString(bean.getCollaborationProducts());
			if (StringUtils.isNotBlank(bean.getCollaborationProducts())) {
				collaborationProducts += ",OR:" + other_collaborationProducts;
			} else {
				collaborationProducts += "OR:" + other_collaborationProducts;
			}
			logger.debug("額外處理欄位部分：" + collaborationProducts);
			bean.setCollaborationProducts(collaborationProducts);
		}
		request.setAttribute("collaborationProductsValue",
				collaborationProductsDisplay(bean.getCollaborationProducts()));

		// 國別
		String countrySysid = bean.getCollaborationCountry();
		List<CpsCountry> countryPut = cloudDao.queryTable(sf(), CpsCountry.class,
				new QueryGroup(new QueryRule(PK, countrySysid)), new QueryOrder[0], null, null);
		if (countryPut.size() > 0) {
			session.put("countryName", countryPut.get(0).getName());
		}

		String countrySysid1 = bean.getCollaborationCompanyCountry();
		List<CpsCountry> countryPut1 = cloudDao.queryTable(sf(), CpsCountry.class,
				new QueryGroup(new QueryRule(PK, countrySysid1)), new QueryOrder[0], null, null);

		if (countryPut1.size() > 0) {
			session.put("companyCountry", countryPut1.get(0).getName());
		}

		String cpsVenderSysid = bean.getSourceId();
		if (cpsVenderSysid.equals("MTS")) {
			request.setAttribute("sysid", "");
		} else {
			request.setAttribute("sysid", cpsVenderSysid);
		}
		bean.setSiteMemberSysid(getUserData2().getAccount().getSysid());
		Util.defaultPK(bean);
		defaultValue(bean);
		sessionSet("bean", bean);
		// logger.debug("bean:" + ToStringBuilder.reflectionToString(bean,
		// ToStringStyle.MULTI_LINE_STYLE));
		return SUCCESS;
	}

	protected boolean saveCollaboration() {
		try {
			bean = (MtsCollaboration) sessionGet("bean");
			// logger.debug(ToStringBuilder.reflectionToString(bean,
			// ToStringStyle.MULTI_LINE_STYLE));

			String cpsVenderSysid = bean.getSourceId();
			if (cpsVenderSysid.equals("MTS")) {
				request.setAttribute("sysid", "");
				request.setAttribute("cpsVenderName", "MTS");

			} else {

				logger.debug("廠商SYSID" + cpsVenderSysid);
				List<CpsVendor> cpsVenderList = cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, EQ, cpsVenderSysid)), new QueryOrder[0], null, null);
				addMultiLan(cpsVenderList, sf(), CpsVendor.class);
				if (cpsVenderList.size() > 0) {
					request.setAttribute("sysid", cpsVenderSysid);
					request.setAttribute("cpsVenderName", cpsVenderList.get(0).getName()); // 這樣前端就不用s:iterator，可直接取用
				}
			}

			List saveList = new ArrayList();

			saveList.add(bean);
			String daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
			if (!daoMsg.equals(SUCCESS)) {
				// request.setAttribute("errMsg", daoMsg);
				logger.debug(daoMsg);
				return false;
			} else {
				/** 寄信功能-儲存成功寄信 */
				logger.debug("客戶姓名:" + bean.getCollaborationName());
				logger.debug("客戶信箱:" + bean.getCollaborationMail());
				logger.debug("廠商SYSID" + bean.getSourceId());

				String vendorName = "";
				if (StringUtils.isNotBlank(bean.getSourceId())) {
					if (bean.getSourceId().equals("MTS")) {
						vendorName = getText("web.mts");
					} else {
						List<CpsVendor> vendorList = cloudDao.queryTable(sf(), CpsVendor.class,
								new QueryGroup(new QueryRule(PK, bean.getSourceId())), new QueryOrder[0], null, null);
						if (vendorList.size() > 0) {
							vendorName = vendorList.get(0).getName();
						}
					}
				}

				/** 寄送客戶 */
				emailClient(bean.getCollaborationName(), bean.getCollaborationMail(), vendorName,
						bean.getCollaborationNo());

				/** 寄送廠商 */
				emailVendor(bean.getCollaborationName(), bean.getSourceId(), bean.getCollaborationNo());

			}
		} catch (Exception e) {
			e.printStackTrace();
			// request.setAttribute("errMsg", e.toString());
			return false;
		}
		return true;
	}

	public String emailClient(String clientName, String clientEmail, String vendorName, String billNo) {

		logger.debug("寄信客戶資訊:" + clientName + "," + clientEmail);

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "collaboration_Confirmation")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() == 1) {
			CpsEmailTemplate emailClientEmailTemplate = emailTemplate.get(0);

			/** 標題部分 */
			String emailTitle = emailClientEmailTemplate.getEmailTitle();
			// 單號
			emailTitle = emailTitle.replace("$BILLNO$", billNo);

			/** 內文部分 */
			String emailContent = emailClientEmailTemplate.getEmailContent();

			// 填寫人名稱
			emailContent = emailContent.replace("$CLIENT_NAME$", clientName);

			// 廠商名稱
			emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

			// 單號
			emailContent = emailContent.replace("$BILLNO$", billNo);

			List<String> cpsQaReceiveConfirmationList = new ArrayList<String>();
			cpsQaReceiveConfirmationList.add(emailContent);

			try {
				new MailThread(new MailBean(clientEmail, emailTitle, cpsQaReceiveConfirmationList),
						getSendMailSetting()).start();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		return SUCCESS;
	}

	public String emailVendor(String clientName, String vendorSysid, String billNo) {
		// collaboration_Notice_Vendor
		logger.debug("寄信廠商資訊:" + vendorSysid);

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "collaboration_Notice_Vendor")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() == 1) {
			CpsEmailTemplate emailVendorEmailTemplate = emailTemplate.get(0);

			/** 標題部分 */
			String emailTitle = emailVendorEmailTemplate.getEmailTitle();
			// 單號
			emailTitle = emailTitle.replace("$BILLNO$", billNo);

			/** 內文部分 */
			String emailContent = emailVendorEmailTemplate.getEmailContent();

			// 填寫人名稱
			emailContent = emailContent.replace("$CLIENT_NAME$", clientName);

			// 單號
			emailContent = emailContent.replace("$BILLNO$", billNo);

			if (vendorSysid.equals("MTS")) {
				// 館主名稱
				emailContent = emailContent.replace("$VENDOR_NAME$", getText("web.mts"));
			} else {
				// 廠商名稱
				List<CpsVendor> vendorList = cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);
				if (vendorList.size() > 0) {
					emailContent = emailContent.replace("$VENDOR_NAME$", vendorList.get(0).getName());
				}
			}

			List<String> emailVendorList = new ArrayList<String>();

			emailVendorList.add(emailContent);

			if (vendorSysid.equals("MTS")) {
				/** 寄信給館主 */
				List<CpsEntity> ent = cloudDao.queryTable(sf(), CpsEntity.class,
						new QueryGroup(new QueryRule("dataId", "mts")), new QueryOrder[0], null, null);
				if (ent.size() > 0) {
					List<CpsMember> memberList = cloudDao.queryTable(sf(), CpsMember.class,
							new QueryGroup(new QueryRule("sourceSysid", ent.get(0).getSysid())), new QueryOrder[0],
							null, null);
					if (memberList.size() > 0) {
						for (CpsMember cpsMember : memberList) {
							try {
								new MailThread(new MailBean(cpsMember.getEmail(), emailTitle, emailVendorList),
										getSendMailSetting()).start();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
						}
					}
				}
			} else {
				/** 寄信給廠商 */
				List<CpsMember> memberList = cloudDao.queryTable(sf(), CpsMember.class,
						new QueryGroup(new QueryRule("sourceSysid", vendorSysid)), new QueryOrder[0], null, null);

				if (memberList.size() > 0) {
					for (CpsMember cpsMember : memberList) {

						try {
							new MailThread(new MailBean(cpsMember.getEmail(), emailTitle, emailVendorList),
									getSendMailSetting()).start();
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			}

		}

		return SUCCESS;
	}

	/**
	 * 合作項目 逗號隔開
	 */
	protected String collaborationProductsDisplay(String collaborationProductsValue) {
		collaborationProductsValue = StringUtils.defaultString(collaborationProductsValue);
		if (StringUtils.isBlank(collaborationProductsValue))
			return "";
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

		List<MtsProducts> mtsProductsList = new ArrayList<MtsProducts>();
		List<MtsCooperation> mtsCooperationList = new ArrayList<MtsCooperation>();
		if (StringUtils.isNotBlank(cpValueA)) {
			mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class,
					new QueryGroup(new QueryRule(PK, IN, cpValueA)), new QueryOrder[0], null, null);
			addMultiLan(mtsProductsList, sf(), MtsProducts.class);

			mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class,
					new QueryGroup(new QueryRule(PK, IN, cpValueA)), new QueryOrder[0], null, null);
			addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);
		}

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
}