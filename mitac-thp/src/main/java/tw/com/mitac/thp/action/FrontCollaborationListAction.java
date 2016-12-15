package tw.com.mitac.thp.action;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsCollaboration;
import tw.com.mitac.thp.bean.BhsCollaborationFeedback;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMenu;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsMemberForVendor;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAppointment;
import tw.com.mitac.thp.bean.MtsCollaboration;
import tw.com.mitac.thp.bean.MtsCollaborationFeedback;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

import com.opensymphony.xwork2.ActionContext;

/** CPS_FW_016_合作需求單 */
public class FrontCollaborationListAction extends BasisFrontLoginAction {
	public String getPurposeKey() {
		// return "Purpose";
		return "Collaboration Option";
	}

	protected final String multiSysidToName(Class<?> clazz, String multiSysid, String displayColumn) {
		StringBuilder s = new StringBuilder();
		if (StringUtils.isNotBlank(multiSysid)) {
			String[] arr = multiSysid.split(",");
			for (String pk : arr) {
				try {
					Object b = createDataTable(clazz).get(StringUtils.trim(pk));
					String name = (String) PropertyUtils.getProperty(b, displayColumn);

					if (s.length() > 0)
						s.append(",");
					s.append(name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return s.toString();
	}

	/**
	 * markup:html
	 * 
	 * @param itemBean
	 * @return
	 */
	protected final String interestForPrint(BhsCollaboration itemBean) {
		StringBuilder itemSp = new StringBuilder();
		String ln = "<br>";
		String fontBig1 = "<font size='4' color='black'>";
		String fontBig2 = "</font>";
		String title1 = "<u>";
		String title2 = "</u>";
		if (StringUtils.isNotBlank(itemBean.getInterestTechnologySysid())) {
			itemSp.append(fontBig1);
			itemSp.append(title1);
			itemSp.append("Pipeline & Platform");
			itemSp.append(title2);
			itemSp.append(":");
			itemSp.append(fontBig2);
			itemSp.append(multiSysidToName(BhsTechnology.class, itemBean.getInterestTechnologySysid(), NAME));
			itemSp.append(ln);
			itemSp.append(fontBig1);
			itemSp.append(title1);
			itemSp.append(getPurposeKey());
			itemSp.append(title2);
			itemSp.append(":");
			itemSp.append(fontBig2);
			itemSp.append(multiSysidToName(BhsMenu.class, itemBean.getProposeTechnologyMenuSysid(), NAME));
			itemSp.append(ln);
			itemSp.append(ln);
		}
		if (StringUtils.isNotBlank(itemBean.getInterestProductsSysid())) {
			itemSp.append(fontBig1);
			itemSp.append(title1);
			itemSp.append("On Market Product");
			itemSp.append(title2);
			itemSp.append(":");
			itemSp.append(fontBig2);
			itemSp.append(multiSysidToName(BhsProducts.class, itemBean.getInterestProductsSysid(), NAME));
			itemSp.append(ln);
			itemSp.append(fontBig1);
			itemSp.append(title1);
			itemSp.append(getPurposeKey());
			itemSp.append(title2);
			itemSp.append(":");
			itemSp.append(fontBig2);
			itemSp.append(multiSysidToName(BhsMenu.class, itemBean.getProposeProductsMenuSysid(), NAME));
			itemSp.append(ln);
			itemSp.append(ln);
		}
		if (StringUtils.isNotBlank(itemBean.getInterestHighlightSysid())) {
			itemSp.append(fontBig1);
			itemSp.append(title1);
			itemSp.append("On Market Product");
			itemSp.append(title2);
			itemSp.append(":");
			itemSp.append(fontBig2);
			itemSp.append(multiSysidToName(BhsHighlight.class, itemBean.getInterestHighlightSysid(), NAME));
			itemSp.append(ln);
			itemSp.append(ln);
		}
		if (StringUtils.isNotBlank(itemBean.getInterestOther())) {
			itemSp.append(fontBig1);
			itemSp.append(title1);
			itemSp.append(getText("web.cps.other"));
			itemSp.append(title2);
			itemSp.append(":");
			itemSp.append(fontBig2);
			itemSp.append(itemBean.getInterestOther());
			itemSp.append(ln);
		}
		return itemSp.toString();
	}

	List<String> rtnBhsCollaborationList = new ArrayList<String>();
	List<String> rtnMtsCollaborationList = new ArrayList<String>();
	List<String> rtnBhsCollaborationFeedbackList = new ArrayList<String>();
	List<String> rtnMtsAppointmentList = new ArrayList<String>();

	/** 合作需求單列表 */
	public String initCollaborationData() {
		String sysid = getUserData2().getAccount().getSysid();

		// 抓BHS的合作需求單
		List<BhsCollaboration> bhsCollaborationList = cloudDao.queryTable(sf(), BhsCollaboration.class,
				new QueryGroup(new QueryRule("siteMemberSysid", sysid)), new QueryOrder[] { new QueryOrder(PK, DESC) },
				null, null);
		addMultiLan(bhsCollaborationList, sf(), BhsCollaboration.class);

		rtnBhsCollaborationList.clear();
		String rtnBhsCollaborationString = "";
		String[] bhsDateArr;
		String bhsCollaborationSysid = "";
		String bhsCreationDate = ""; // 日期
		String bhsCollaborationNo = "";// 單號
		String bhsVendorName = ""; // 企業名稱
		String bhsCollaborationStatus = ""; // 合作單狀態
		String bhsCollaborationDescribe = ""; // 合作內容說明

		if (bhsCollaborationList.size() > 0) {
			for (int i = 0; i < bhsCollaborationList.size(); i++) {
				bhsCollaborationSysid = bhsCollaborationList.get(i).getSysid().toString();

				if (StringUtils.isNotBlank(bhsCollaborationList.get(i).getCreationDate())) {
					bhsDateArr = bhsCollaborationList.get(i).getCreationDate().toString().split(" ");
					bhsCreationDate = bhsDateArr[0].toString();

				}

				if (StringUtils.isNotBlank(bhsCollaborationList.get(i).getCollaborationNo())) {
					bhsCollaborationNo = bhsCollaborationList.get(i).getCollaborationNo().toString();
				}

				if (StringUtils.isNotBlank(bhsCollaborationList.get(i).getSourceId())) {
					String bhsSourcdId = bhsCollaborationList.get(i).getSourceId().toString();

					if ("BHS".equals(bhsSourcdId)) {
						bhsVendorName = "BHS";
					} else {
						CpsVendor cpsVendor = getDataCpsVendorTable().get(bhsSourcdId);
						if (cpsVendor != null)
							bhsVendorName = cpsVendor.getName().toString();
					}

				}

				if (StringUtils.isNotBlank(bhsCollaborationList.get(i).getCollaborationStatus())) {
					bhsCollaborationStatus = bhsCollaborationList.get(i).getCollaborationStatus().toString();
				}

				if (StringUtils.isNotBlank(bhsCollaborationList.get(i).getCollaborationDescribe())) {
					bhsCollaborationDescribe = bhsCollaborationList.get(i).getCollaborationDescribe().toString();
				}

				rtnBhsCollaborationString = bhsCollaborationSysid + "$*$" + bhsCreationDate + "$*$" + bhsCollaborationNo
						+ "$*$" + bhsVendorName + "$*$" + bhsCollaborationStatus + "$*$" + bhsCollaborationDescribe;
				rtnBhsCollaborationList.add(rtnBhsCollaborationString);
			}

			session.put("rtnBhsCollaborationList", rtnBhsCollaborationList);

		} else {
			session.put("rtnBhsCollaborationList", null);
		}

		// 抓MTS的合作需求單
		List<MtsCollaboration> mtsCollaborationList = cloudDao.queryTable(sf(), MtsCollaboration.class,
				new QueryGroup(new QueryRule("siteMemberSysid", sysid)), new QueryOrder[] { new QueryOrder(PK, DESC) },
				null, null);
		addMultiLan(mtsCollaborationList, sf(), MtsCollaboration.class);

		rtnMtsCollaborationList.clear();
		String rtnMtsCollaborationString = "";
		String[] mtsDateArr;
		String mtsCollaborationSysid = "";
		String mtsCreationDate = ""; // 日期
		String mtsCollaborationNo = "";// 單號
		String mtsVendorName = ""; // 團隊名稱
		String mtsCollaborationStatus = ""; // 合作單狀態
		String mtsCollaborationDescribe = ""; // 合作內容說明

		if (mtsCollaborationList.size() > 0) {
			for (int i = 0; i < mtsCollaborationList.size(); i++) {
				mtsCollaborationSysid = mtsCollaborationList.get(i).getSysid().toString();

				if (StringUtils.isNotBlank(mtsCollaborationList.get(i).getCreationDate())) {
					mtsDateArr = mtsCollaborationList.get(i).getCreationDate().toString().split(" ");
					mtsCreationDate = mtsDateArr[0].toString();
				}

				if (StringUtils.isNotBlank(mtsCollaborationList.get(i).getCollaborationNo())) {
					mtsCollaborationNo = mtsCollaborationList.get(i).getCollaborationNo().toString();
				}

				if (StringUtils.isNotBlank(mtsCollaborationList.get(i).getSourceId())) {
					String mtsSourcdId = mtsCollaborationList.get(i).getSourceId().toString();
					// logger.debug("Collaboration-mtsSourcdId:" + mtsSourcdId);

					if ("MTS".equals(mtsSourcdId)) {
						mtsVendorName = "MTS";
					} else {
						CpsVendor cpsVendor = getDataCpsVendorTable().get(mtsSourcdId);
						if (cpsVendor != null) {
							mtsVendorName = cpsVendor.getName().toString();
						}
					}
				}

				if (StringUtils.isNotBlank(mtsCollaborationList.get(i).getCollaborationStatus())) {
					mtsCollaborationStatus = mtsCollaborationList.get(i).getCollaborationStatus().toString();
				}

				if (StringUtils.isNotBlank(mtsCollaborationList.get(i).getCollaborationDescribe())) {
					mtsCollaborationDescribe = mtsCollaborationList.get(i).getCollaborationDescribe().toString();
				}

				rtnMtsCollaborationString = mtsCollaborationSysid + "$*$" + mtsCreationDate + "$*$" + mtsCollaborationNo
						+ "$*$" + mtsVendorName + "$*$" + mtsCollaborationStatus + "$*$" + mtsCollaborationDescribe;
				rtnMtsCollaborationList.add(rtnMtsCollaborationString);

			}

			session.put("rtnMtsCollaborationList", rtnMtsCollaborationList);

		} else {
			session.put("rtnMtsCollaborationList", null);
		}

		// 抓MtsAppointment單
		String memberSysid = getUserData2().getAccount().getSysid();
		logger.debug("initMtsAppointmentData-memberSysid:" + memberSysid);

		List<MtsAppointment> mtsAppointmentlist = cloudDao.queryTable(sf(), MtsAppointment.class,
				new QueryGroup(new QueryRule("siteMemberSysid", EQ, memberSysid)),
				new QueryOrder[] { new QueryOrder("creationDate", DESC) }, null, null);

		logger.debug("initMtsAppointmentData-mtsAppointmentlist.size():" + mtsAppointmentlist.size());
		if (mtsAppointmentlist.size() > 0) {

			List<Map> mtsAppointmentMapList = formatListToMap(mtsAppointmentlist); // 將list轉為map

			for (Map mtsAppointment : mtsAppointmentMapList) {

				String[] dateArr = mtsAppointment.get("creationDate").toString().split(" ");
				String appointmentDate = dateArr[0];
				mtsAppointment.put("appointmentDate", appointmentDate); // 拿掉日期後面的毫秒
				// logger.debug("initMtsAppointmentData-appointmentDate:" +
				// appointmentDate);

				// 預約團隊名稱
				String vendorName = findVendorName(mtsAppointment.get("appointmentVendor").toString());
				if ("".equals(vendorName)) {
					vendorName = "&emsp;";
				} else {
					mtsAppointment.put("vendorName", vendorName);
				}

				mtsAppointment.put("vendorName", vendorName); // 預約團隊名稱
				// logger.debug("initMtsAppointmentData-vendorName:" +
				// vendorName);

				String[] productsNameArr = mtsAppointment.get("mtsProductsSysid").toString().split("，");
				String productsName = "";
				for (int i = 0; i < productsNameArr.length; i++) {
					productsName = findProductsName(productsNameArr[i].toString()) + "</br>" + productsName;
				}
				mtsAppointment.put("productsName", productsName); // 醫療服務
				// logger.debug("initMtsAppointmentData-productsName:" +
				// productsName);

				// appointmentStatus 狀態
				String[] appointmentStatus = mtsAppointment.get("appointmentStatus").toString().split(" ");
				String status = appointmentStatus[0];
				mtsAppointment.put("appointmentStatus", status);
			}
			request.setAttribute("rtnMtsAppointmentList", mtsAppointmentMapList);

		} else {
			request.setAttribute("rtnMtsAppointmentList", null);
		}

		return SUCCESS;
	} // end initCollaborationData

	/**
	 * 取得團隊名稱 輸入：insourceId:vendorSysid 輸出：rtnVendorName:團隊名稱
	 */
	public String findVendorName(String insourceId) {
		String rtnVendorName = "";
		CpsVendor cpsVendor = getDataCpsVendorTable().get(insourceId);
		if (cpsVendor != null) {
			rtnVendorName = StringUtils.defaultString(cpsVendor.getName());
		}
		return rtnVendorName;
	}

	/**
	 * 取得醫療服務名稱 輸入：inProductsName:productsSysid 輸出：rtnProductsName:醫療服務名稱
	 */
	protected String findProductsName(String inProductsName) {
		String rtnProductsName = StringUtils.defaultString(getDataMtsProductsTable().get(inProductsName).getName());
		return rtnProductsName;
	}

	/**
	 * 合作需求單內頁-判斷
	 */
	public String collaborationSub() {
		String result = SUCCESS;
		String collaborationSysid = request.getParameter("collaborationSysid");
		// logger.debug("collaborationSub-collaborationSysid:" +
		// collaborationSysid);

		if (StringUtils.isNotBlank(collaborationSysid)) {
			String entityType = collaborationSysid.substring(0, 3).toUpperCase(); // 取館別
			// logger.debug("collaborationSub-entityType:" + entityType);

			if ("BHS".equals(entityType)) {
				doBhsCollaborationList(collaborationSysid);
				result = "bhs";
			} else if ("MTS".equals(entityType)) {
				doMtsCollaborationList(collaborationSysid);
			}
		}
		return result;
	}

	/**
	 * BHS前台頁面顯示-合作需求單內頁
	 */
	protected void doBhsCollaborationList(String inCollaborationSysid) {

		List<BhsCollaboration> bhsCollaborationList = cloudDao.queryTable(sf(), BhsCollaboration.class,
				new QueryGroup(new QueryRule(PK, EQ, inCollaborationSysid)), new QueryOrder[0], null, null);

		if (bhsCollaborationList.size() > 0) {
			// 處理國別
			List<CpsCountry> countryPut = cloudDao.queryTable(sf(), CpsCountry.class,
					new QueryGroup(new QueryRule(PK, bhsCollaborationList.get(0).getCollaborationCountry())),
					new QueryOrder[0], null, null);
			if (countryPut.size() > 0) {
				request.setAttribute("countryName", countryPut.get(0).getName());
			}

			if (bhsCollaborationList.get(0).getSourceId().equals("BHS")) {

				request.setAttribute("cpsVendorName", getText("web.node.bhs"));
				request.setAttribute("cpsVendorEmail", getCpsConfig().getBioCompaniesManagerEmail());
			} else {
				CpsVendor cpsVendor = getDataCpsVendorTable().get(bhsCollaborationList.get(0).getSourceId());
				if (cpsVendor != null) {
					request.setAttribute("cpsVendorName", cpsVendor.getName().toString());
					// 寄給所有帳號
					String cpsVendorEmail = "";
					List<String> emailList = (List<String>) cloudDao.findProperty(sf(), CpsMemberForVendor.class,
							new QueryGroup(new QueryRule(FK, bhsCollaborationList.get(0).getSourceId()),
									new QueryRule("email", NE, "")),
							null, true, "email");
					if (emailList.size() > 0) {
						StringBuilder sb = new StringBuilder();
						for (String email : emailList) {
							sb.append("," + email);
						}
						cpsVendorEmail = sb.toString().substring(1);
					}
					request.setAttribute("cpsVendorEmail", cpsVendorEmail);
				}
			}

			// 處理時間顯示
			DateFormat sdfor = new SimpleDateFormat("yyyy-MM-dd");

			if (bhsCollaborationList.get(0).getCollaborationVideoOneLocal() != null) {
				String oneLocalSp = sdfor.format(bhsCollaborationList.get(0).getCollaborationVideoOneLocal());
				request.setAttribute("collaborationVideoOneLocalSp1", oneLocalSp);
			}

			if (bhsCollaborationList.get(0).getCollaborationVideoOneTaiwan() != null) {
				String oneTaiwanSp = sdfor.format(bhsCollaborationList.get(0).getCollaborationVideoOneTaiwan());
				request.setAttribute("collaborationVideoOneTaiwanSp1", oneTaiwanSp);
			}

			if (bhsCollaborationList.get(0).getCollaborationVideoTwoLocal() != null) {
				String twoLocalSp = sdfor.format(bhsCollaborationList.get(0).getCollaborationVideoTwoLocal());
				request.setAttribute("collaborationVideoTwoLocalSp2", twoLocalSp);
			}
			if (bhsCollaborationList.get(0).getCollaborationVideoTwoTaiwan() != null) {
				String twoTaiwanSp = sdfor.format(bhsCollaborationList.get(0).getCollaborationVideoTwoTaiwan());
				request.setAttribute("collaborationVideoTwoTaiwanSp2", twoTaiwanSp);
			}

			request.setAttribute("itemBean", bhsCollaborationList.get(0));

		}

		List<BhsCollaborationFeedback> bhsCollaborationFeedbackList = cloudDao.queryTable(sf(),
				BhsCollaborationFeedback.class, new QueryGroup(new QueryRule("parentSysid", EQ, inCollaborationSysid)),
				new QueryOrder[] { new QueryOrder("creationDate", ASC) }, null, null);

		if (bhsCollaborationFeedbackList.size() > 0) {

			List<Map> bhsCollaborationFeedbackMap = formatListToMap(bhsCollaborationFeedbackList); // 將list
																									// 轉為
																									// map
			for (Map map : bhsCollaborationFeedbackMap) {
				if (StringUtils.isNotBlank((String) map.get("backMemberSysid"))) {
					// 後台會員
					String memberSysid = (String) map.get("backMemberSysid");

					List<CpsMember> cpsMemberList = cloudDao.queryTable(sf(), CpsMember.class,
							new QueryGroup(new QueryRule(PK, EQ, memberSysid)), new QueryOrder[0], null, null);

					if (cpsMemberList.size() > 0) {
						if (StringUtils.isNotBlank(cpsMemberList.get(0).getMemberName())) {
							String memberName = cpsMemberList.get(0).getMemberName().toString().trim();
							map.put("memberName", memberName);
						} else {
							map.put("memberName", "");
						}
					} else {
						map.put("memberName", "");
					}
				} else if (StringUtils.isNotBlank((String) map.get("memberSysid"))) {
					// 前台會員
					String memberSysid = (String) map.get("memberSysid");
					// logger.debug("getCollaborationSub-memberSysid:" +
					// memberSysid);

					List<CpsSiteMember> cpsSiteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class,
							new QueryGroup(new QueryRule(PK, EQ, memberSysid)), new QueryOrder[0], null, null);

					// logger.debug("getCollaborationSub-cpsSiteMemberList.size():"
					// + cpsSiteMemberList.size());
					if (cpsSiteMemberList.size() > 0) {
						if (StringUtils.isNotBlank(cpsSiteMemberList.get(0).getMemberName())) {
							String memberName = cpsSiteMemberList.get(0).getMemberName().toString().trim();
							map.put("memberName", memberName);
						} else {
							map.put("memberName", "");
						}
					} else {
						map.put("memberName", "");
					}
				}
			} // end for

			request.setAttribute("CollaborationFeedbackMap", bhsCollaborationFeedbackMap);
		}
	} // end

	/**
	 * MTS前台頁面顯示-合作需求單內頁
	 */
	protected void doMtsCollaborationList(String inCollaborationSysid) {

		List<MtsCollaboration> mtsCollaborationList = cloudDao.queryTable(sf(), MtsCollaboration.class,
				new QueryGroup(new QueryRule(PK, EQ, inCollaborationSysid)), new QueryOrder[0], null, null);

		if (mtsCollaborationList.size() > 0) {
			// 處理國別
			List<CpsCountry> countryPut = cloudDao.queryTable(sf(), CpsCountry.class,
					new QueryGroup(new QueryRule(PK, mtsCollaborationList.get(0).getCollaborationCountry())),
					new QueryOrder[0], null, null);
			if (countryPut.size() > 0) {
				request.setAttribute("countryName", countryPut.get(0).getName());
			}

			if (mtsCollaborationList.get(0).getSourceId().equals("MTS")) {
				request.setAttribute("cpsVendorName", getText("web.node.mts"));
				request.setAttribute("cpsVendorEmail", getCpsConfig().getMedicalManagerEmail());
			} else {
				CpsVendor cpsVendor = getDataCpsVendorTable().get(mtsCollaborationList.get(0).getSourceId());
				if (cpsVendor != null) {
					request.setAttribute("cpsVendorName", cpsVendor.getName().toString());
					String cpsVendorEmail = "";
					List<String> emailList = (List<String>) cloudDao.findProperty(sf(), CpsMemberForVendor.class,
							new QueryGroup(new QueryRule(FK, mtsCollaborationList.get(0).getSourceId()),
									new QueryRule("email", NE, "")),
							null, true, "email");
					if (emailList.size() > 0) {
						StringBuilder sb = new StringBuilder();
						for (String email : emailList) {
							sb.append("," + email);
						}
						cpsVendorEmail = sb.toString().substring(1);
					}
					request.setAttribute("cpsVendorEmail", cpsVendorEmail);
				}
			}

			// 處理合作項目
			String ptValue = "";
			String cnPsSubstring = "";
			String orValue = "";
			String cnPs = StringUtils.defaultString(mtsCollaborationList.get(0).getCollaborationProducts());
			int intOr = cnPs.indexOf("OR:");
			if (intOr != -1) {
				cnPsSubstring = cnPs.substring(0, intOr);
				orValue = cnPs.substring(intOr + 3, cnPs.length());
			} else {
				cnPsSubstring = cnPs;
			}

			List<MtsProducts> mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class,
					new QueryGroup(new QueryRule(PK, IN, cnPsSubstring)), new QueryOrder[0], null, null);
			addMultiLan(mtsProductsList, sf(), MtsProducts.class);

			List<MtsCooperation> mtsCooperationList = cloudDao.queryTable(sf(), MtsCooperation.class,
					new QueryGroup(new QueryRule(PK, IN, cnPsSubstring)), new QueryOrder[0], null, null);
			addMultiLan(mtsCooperationList, sf(), MtsCooperation.class);

			if (mtsProductsList.size() > 0) {
				for (MtsProducts mtsProducts : mtsProductsList) {
					ptValue += "," + mtsProducts.getName();
				}
			}

			if (mtsCooperationList.size() > 0) {
				for (MtsCooperation mtsCooperation : mtsCooperationList) {
					ptValue += "," + mtsCooperation.getCooperationName();
				}
			}

			if (StringUtils.isNotBlank(ptValue)) {
				ptValue = ptValue.substring(1);
			}

			if (StringUtils.isNotBlank(ptValue) && StringUtils.isNotBlank(orValue)) {
				ptValue = ptValue + "," + orValue;
			} else {
				ptValue = orValue;
			}
			request.setAttribute("ptValue", ptValue);

			// 處理時間顯示
			DateFormat sdfor = new SimpleDateFormat("yyyy-MM-dd");

			if (mtsCollaborationList.get(0).getCollaborationVideoOneLocal() != null) {
				String oneLocalSp = sdfor.format(mtsCollaborationList.get(0).getCollaborationVideoOneLocal());
				request.setAttribute("collaborationVideoOneLocalSp1", oneLocalSp);
			}

			if (mtsCollaborationList.get(0).getCollaborationVideoOneTaiwan() != null) {
				String oneTaiwanSp = sdfor.format(mtsCollaborationList.get(0).getCollaborationVideoOneTaiwan());
				request.setAttribute("collaborationVideoOneTaiwanSp1", oneTaiwanSp);
			}

			if (mtsCollaborationList.get(0).getCollaborationVideoTwoLocal() != null) {
				String twoLocalSp = sdfor.format(mtsCollaborationList.get(0).getCollaborationVideoTwoLocal());
				request.setAttribute("collaborationVideoTwoLocalSp2", twoLocalSp);
			}
			if (mtsCollaborationList.get(0).getCollaborationVideoTwoTaiwan() != null) {
				String twoTaiwanSp = sdfor.format(mtsCollaborationList.get(0).getCollaborationVideoTwoTaiwan());
				request.setAttribute("collaborationVideoTwoTaiwanSp2", twoTaiwanSp);
			}

			// 基本資源
			request.setAttribute("itemBean", mtsCollaborationList.get(0));

		}

		List<MtsCollaborationFeedback> mtsCollaborationFeedbackList = cloudDao.queryTable(sf(),
				MtsCollaborationFeedback.class, new QueryGroup(new QueryRule("parentSysid", EQ, inCollaborationSysid)),
				new QueryOrder[] { new QueryOrder("creationDate", ASC) }, null, null);

		// logger.debug("getCollaborationSub-mtsCollaborationFeedbackList.size():"
		// + mtsCollaborationFeedbackList.size());
		if (mtsCollaborationFeedbackList.size() > 0) {

			List<Map> mtsCollaborationFeedbackMap = formatListToMap(mtsCollaborationFeedbackList); // 將list
																									// 轉為
																									// map
			for (Map map : mtsCollaborationFeedbackMap) {
				// logger.debug("getCollaborationSub-map.get(backMemberSysid):"
				// + map.get("backMemberSysid"));

				if (StringUtils.isNotBlank((String) map.get("backMemberSysid"))) {
					// 後台會員
					String memberSysid = (String) map.get("backMemberSysid");

					List<CpsMember> cpsMemberList = cloudDao.queryTable(sf(), CpsMember.class,
							new QueryGroup(new QueryRule(PK, EQ, memberSysid)), new QueryOrder[0], null, null);

					// logger.debug("getCollaborationSub-cpsMemberList.size():"
					// + cpsMemberList.size());
					if (cpsMemberList.size() > 0) {
						if (StringUtils.isNotBlank(cpsMemberList.get(0).getMemberName())) {
							String memberName = cpsMemberList.get(0).getMemberName().toString().trim();
							// logger.debug("getCollaborationSub-memberName:" +
							// memberName);

							map.put("memberName", memberName);
						} else {
							map.put("memberName", "");
						}
					} else {
						map.put("memberName", "");
					}
				} else if (StringUtils.isNotBlank((String) map.get("memberSysid"))) {
					// 前台會員
					String memberSysid = (String) map.get("memberSysid");
					// logger.debug("getCollaborationSub-memberSysid:" +
					// memberSysid);

					List<CpsSiteMember> cpsSiteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class,
							new QueryGroup(new QueryRule(PK, EQ, memberSysid)), new QueryOrder[0], null, null);

					// logger.debug("getCollaborationSub-cpsSiteMemberList.size():"
					// + cpsSiteMemberList.size());
					if (cpsSiteMemberList.size() > 0) {
						if (StringUtils.isNotBlank(cpsSiteMemberList.get(0).getMemberName())) {
							String memberName = cpsSiteMemberList.get(0).getMemberName().toString().trim();
							map.put("memberName", memberName);
						} else {
							map.put("memberName", "");
						}
					} else {
						map.put("memberName", "");
					}
				}
			} // end for

			request.setAttribute("CollaborationFeedbackMap", mtsCollaborationFeedbackMap);
		}
	} // end

	// doMtsCollaborationList

	/** 新增留言訊息 */
	public String ajaxDoAddContent() {
		resultMap = new HashMap();
		String siteMemberSysid = getUserData2().getAccount().getSysid();
		String feedBackDesc = request.getParameter("addContent").trim();
		String collaborationSysid = request.getParameter("collaborationSysid");
		String entityType = collaborationSysid.substring(0, 3).toUpperCase(); // 取館別
		String collaborationNo = request.getParameter("collaborationNo");
		String collaborationName = request.getParameter("collaborationName");
		String cpsVendorName = request.getParameter("cpsVendorName");
		String cpsVendorEmail = request.getParameter("cpsVendorEmail");

		// logger.debug("$$$$$$$-siteMemberSysid:" + siteMemberSysid);
		// logger.debug("$$$$$$$-feedBackDesc:" + feedBackDesc);
		// logger.debug("$$$$$$$-collaborationSysid:" + collaborationSysid);

		String doResult = "";
		try {
			List saveList = new ArrayList();
			String daoMsg = "";

			if ("BHS".equals(entityType)) {
				BhsCollaborationFeedback bf = new BhsCollaborationFeedback();
				Util.defaultPK(bf);
				defaultValue(bf);
				bf.setParentSysid(collaborationSysid);
				bf.setFeedbackDetail(feedBackDesc);
				bf.setMemberSysid(siteMemberSysid);

				// 取得目前日期時間
				Date nowdate = new Date();
				bf.setFeedbackTime(nowdate);

				saveList.add(bf);
				daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");

			} else if ("MTS".equals(entityType)) {
				MtsCollaborationFeedback mf = new MtsCollaborationFeedback();
				Util.defaultPK(mf);
				defaultValue(mf);
				mf.setParentSysid(collaborationSysid);
				mf.setFeedbackDetail(feedBackDesc);
				mf.setMemberSysid(siteMemberSysid);

				// 取得目前日期時間
				Date nowdate = new Date();
				mf.setFeedbackTime(nowdate);

				saveList.add(mf);
				daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
			}

			logger.debug("daoMsg-新增CollaborationFeedback:" + daoMsg);
			resultString = daoMsg;

			if (StringUtils.equals(SUCCESS, daoMsg)) {

				// vendor to client
				List<CpsEmailTemplate> emailTemplate = cloudDao.queryTable(sf(), CpsEmailTemplate.class,
						new QueryGroup(new QueryRule("emailId", "collaboration_C2V")), null, null, null);
				if (emailTemplate.size() == 0) {
					resultMap.put("msg", "查無範本信件可以寄出，請告知管理員沒有範本信件");
				} else {
					CpsEmailTemplate emailClientEmailTemplate = emailTemplate.get(0);

					/** 標題部分 */
					String emailTitle = emailClientEmailTemplate.getEmailTitle();
					//單號
					emailTitle = emailTitle.replace("$BILLNO$", collaborationNo);

					/** 內文部分 */
					String emailContent = emailClientEmailTemplate.getEmailContent();
					// 填寫人名稱
					emailContent = emailContent.replace("$CLIENT_NAME$", collaborationName);

					// 單號
					emailContent = emailContent.replace("$BILLNO$", collaborationNo);

					// 廠商名稱
					emailContent = emailContent.replace("$VENDOR_NAME$", cpsVendorName);

					// 回覆內容
					emailContent = emailContent.replace("$FEEDBACK$", feedBackDesc);

					List<String> emailClientList = new ArrayList<String>();
					emailClientList.add(emailContent);

					try {
						new MailThread(new MailBean(cpsVendorEmail, emailTitle, emailClientList), getSendMailSetting())
								.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("msg", doResult);
			return JSON_RESULT;
		}

		return JSON_RESULT;
	}

	/**
	 * PDF功能
	 */
	public String print() {
		String subResult = collaborationSub();

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("logo", FileUtil.createInputStream("medical_index_logo.jpg"));
		Object itemBeanObj = request.getAttribute("itemBean");
		parameter.put("itemBean", itemBeanObj);

		String country = (String) request.getAttribute("countryName");
		parameter.put("country", country);

		String ptValue = (String) request.getAttribute("ptValue");
		parameter.put("ptValue", ptValue);

		if (itemBeanObj instanceof BhsCollaboration) {
			BhsCollaboration itemBean = (BhsCollaboration) itemBeanObj;
			parameter.put("itemvalue", interestForPrint(itemBean));
			// parameter.put("interestTechnologySysid",
			// multiSysidToName(BhsTechnology.class,
			// itemBean.getInterestTechnologySysid(), NAME));
			// parameter.put("technologyMenuSysid",
			// multiSysidToName(BhsMenu.class,
			// itemBean.getProposeTechnologyMenuSysid(), NAME));
			// parameter.put("interestProductsSysid",
			// multiSysidToName(BhsProducts.class,
			// itemBean.getInterestProductsSysid(), NAME));
			// parameter.put("productsMenuSysid",
			// multiSysidToName(BhsMenu.class,
			// itemBean.getProposeProductsMenuSysid(), NAME));
			// parameter.put("interestHighlightSysid",
			// multiSysidToName(BhsHighlight.class,
			// itemBean.getInterestHighlightSysid(), NAME));
		}

		String cpsVendorName = (String) request.getAttribute("cpsVendorName");
		parameter.put("vendorName", cpsVendorName);

		try {
			String creationDate = (String) PropertyUtils.getProperty(itemBeanObj, "creationDate");
			parameter.put("creationDate", creationDate.substring(0, 10).replace("/", "-"));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		parameter.put(JRParameter.REPORT_LOCALE, ActionContext.getContext().getLocale());

		// ResourceBundle rb = ResourceBundle.getBundle("globalFields",
		// ActionContext.getContext().getLocale());

		String reportTarget = "colabo";
		if (StringUtils.equals("bhs", subResult))
			reportTarget = "colabo_bhs";

		byte[] rawData = FileUtil.export(null, parameter, reportTarget);
		downInputStream = new ByteArrayInputStream(rawData);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		downFileName = df.format(systemDate) + "-" + getText("web.cooperationNeedsReceipt") + ".pdf";
		return DOWN_STREAM;
	}
}