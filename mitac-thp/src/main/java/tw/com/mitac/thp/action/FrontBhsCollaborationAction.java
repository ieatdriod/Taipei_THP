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
import tw.com.mitac.thp.bean.BhsCollaboration;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMenu;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.Util;

/**
 * <ul>
 * <li>合作需求單</li>
 * <li>視訊預約單</li>
 * </ul>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontBhsCollaborationAction extends BasisFrontLoginAction {
	public String getPurposeKey() {
		// return "Purpose";
		return "Collaboration Option";
	}

	protected BhsCollaboration bean;
	protected CpsSiteMember member;

	public BhsCollaboration getBean() {
		return bean;
	}

	public void setBean(BhsCollaboration bean) {
		this.bean = bean;
	}

	public CpsSiteMember getMember() {
		return member;
	}

	public void setMember(CpsSiteMember member) {
		this.member = member;
	}

	public String collaborationPage() {
		logger.debug("start");
		member = getUserData2().getAccount();
		bean = (BhsCollaboration) sessionRemove("bean");

		if (bean == null) {
			bean = new BhsCollaboration();
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
				bean.setSourceId("BHS");
			} else {
				String vendorSysid = sysid;
				bean.setSourceId(vendorSysid);
			}

		}

		System.out.println(bean.getSourceId());
		if (!bean.getSourceId().equals("BHS")) {

			// 取得公司基本資料 >>
			// BhsVendorProfile bhsVendorProfile =
			// createDataTable(BhsVendorProfile.class).get(sysid);
			CpsVendor cpsVendor = createDataTable(CpsVendor.class).get(bean.getSourceId());
			if (cpsVendor == null) {
				request.setAttribute("isSuccess", false);
				request.setAttribute("errMsg", "vendor not found!");
				logger.warn("vendor not found!");
				return SUCCESS;
			}
			request.setAttribute("cpsVendor", cpsVendor);

			String vendorSysid = bean.getSourceId();

			List<BhsVendorProfile> vendorProfile = cloudDao.queryTable(sf(), BhsVendorProfile.class,
					new QueryGroup(new QueryRule("vendorSysid", cpsVendor.getSysid())), new QueryOrder[0], null, null);

			if (vendorProfile.size() > 0) {
				logger.debug("vendorProfileSysid:" + vendorProfile.get(0).getSysid());
				request.setAttribute("vendorProfileSysid", vendorProfile.get(0).getSysid());
			} else {
				logger.warn("vendorProfile:" + null);
				request.setAttribute("vendorProfileSysid", null);
			}

			request.setAttribute("vendorProfileSysid", vendorProfile.get(0).getSysid());

			String[] menuTypeArr = { "T", "P" };
			for (String menuType : menuTypeArr) {
				List<String> rootSysidList = (List<String>) cloudDao.findProperty(sf(), BhsMenu.class,
						new QueryGroup(new QueryRule("menuType", menuType)), null, false, PK);
				if (rootSysidList.size() > 0) {
					List<String> l1SysidList = (List<String>) cloudDao.findProperty(sf(), BhsMenu.class,
							new QueryGroup(new QueryRule("parentBhsMenuSysid", IN, rootSysidList),
									new QueryRule(NAME, getPurposeKey())),
							null, false, PK);
					if (l1SysidList.size() > 0) {
						List<Map> purposeList = (List<Map>) cloudDao.findProperty(sf(), BhsMenu.class,
								new QueryGroup(new QueryRule("parentBhsMenuSysid", IN, l1SysidList)), null, false, PK,
								NAME);
						request.setAttribute("purposeList_" + menuType, purposeList);
					}
				}
			}

			List<Map> bhsProductsList = bhsProductsList(vendorSysid);
			request.setAttribute("bhsProductsList", bhsProductsList);
			List<Map> bhsTechnologyList = bhsTechnologyList(vendorSysid);
			request.setAttribute("bhsTechnologyList", bhsTechnologyList);
			List<Map> bhsHighlightList = bhsHighlightList(vendorSysid);
			request.setAttribute("bhsHighlightList", bhsHighlightList);

			List<Map> list = new ArrayList<Map>();
			list.addAll(bhsProductsList);
			list.addAll(bhsTechnologyList);
			request.setAttribute("ptList", list);
		}

		logger.debug("end");
		return SUCCESS;
	}

	protected List<Map> bhsProductsList(String vendorSysid) {
		List<Map> bhsProductsList = (List<Map>) cloudDao.findProperty(sf(), BhsProducts.class,
				new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)), null, false,
				PK, NAME);
		addMultiLan(bhsProductsList, sf(), BhsProducts.class);
		return bhsProductsList;
	}

	protected List<Map> bhsTechnologyList(String vendorSysid) {
		List<Map> bhsTechnologyList = (List<Map>) cloudDao.findProperty(sf(), BhsTechnology.class,
				new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)), null, false,
				PK, NAME);
		addMultiLan(bhsTechnologyList, sf(), BhsTechnology.class);
		return bhsTechnologyList;
	}

	protected List<Map> bhsHighlightList(String vendorSysid) {
		List<Map> bhsHighlightList = (List<Map>) cloudDao.findProperty(sf(), BhsHighlight.class,
				new QueryGroup(new QueryRule("vendorSysid", vendorSysid), new QueryRule(IS_ENABLED, true)), null, false,
				PK, NAME);
		addMultiLan(bhsHighlightList, sf(), BhsHighlight.class);
		return bhsHighlightList;
	}

	public String collaborationSubmit() {
		Boolean isSuccess = saveCollaboration();
		request.setAttribute("isSuccess", isSuccess);

		String sysid = bean.getSourceId();
		logger.debug("aaaaaa廠商sysid:" + sysid);

		if (sysid.equals("BHS")) {

		} else {

			List<BhsVendorProfile> bhsVendorProfile = cloudDao.queryTable(sf(), BhsVendorProfile.class,
					new QueryGroup(new QueryRule("vendorSysid", sysid)), new QueryOrder[0], null, null);
			request.setAttribute("cpsv", bhsVendorProfile.get(0).getSysid());
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
		if (cpsVenderSysid.equals("BHS")) {
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
			bean = (BhsCollaboration) sessionGet("bean");
			// logger.debug("bean:" + ToStringBuilder.reflectionToString(bean,
			// ToStringStyle.MULTI_LINE_STYLE));

			String cpsVenderSysid = bean.getSourceId();
			logger.debug("cpsVenderSysid:" + cpsVenderSysid);
			if (cpsVenderSysid.equals("BHS")) {

				request.setAttribute("cpsVenderName", "BHS");

			} else {

				logger.debug("廠商SYSID" + cpsVenderSysid);
				List<CpsVendor> cpsVenderList = cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, EQ, cpsVenderSysid)), new QueryOrder[0], null, null);
				addMultiLan(cpsVenderList, sf(), CpsVendor.class);
				if (cpsVenderList.size() > 0) {

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
					if (bean.getSourceId().equals("BHS")) {
						vendorName = getText("web.bhs");
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

			List<String> emailClientList = new ArrayList<String>();
			emailClientList.add(emailContent);

			try {
				new MailThread(new MailBean(clientEmail, emailTitle, emailClientList), getSendMailSetting()).start();
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

			if (vendorSysid.equals("BHS")) {
				// 館主名稱
				emailContent = emailContent.replace("$VENDOR_NAME$", getText("web.bhs"));
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

			if (vendorSysid.equals("BHS")) {
				/** 寄信給館主 */
				List<CpsEntity> ent = cloudDao.queryTable(sf(), CpsEntity.class,
						new QueryGroup(new QueryRule("dataId", "bhs")), new QueryOrder[0], null, null);
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
}