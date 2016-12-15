package tw.com.mitac.thp.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAppointment;
import tw.com.mitac.thp.bean.MtsAppointmentCase;
import tw.com.mitac.thp.bean.MtsAppointmentCaseLink;
import tw.com.mitac.thp.bean.MtsAppointmentFeedback;
import tw.com.mitac.thp.bean.MtsAppointmentFileLink;
import tw.com.mitac.thp.bean.MtsDoctor;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

import com.opensymphony.xwork2.ActionContext;

/**
 * 預約來台就醫
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FrontMtsAppointmentListAction extends BasisFrontLoginAction {
	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("mtsAppointment_file");

	protected List<File> uploadItems1;
	protected List<String> uploadItems1FileName;

	public List<File> getUploadItems1() {
		return uploadItems1;
	}

	public void setUploadItems1(List<File> uploadItems1) {
		this.uploadItems1 = uploadItems1;
	}

	public List<String> getUploadItems1FileName() {
		return uploadItems1FileName;
	}

	public void setUploadItems1FileName(List<String> uploadItems1FileName) {
		this.uploadItems1FileName = uploadItems1FileName;
	}

	protected Map<String, String> pageParam;

	public Map<String, String> getPageParam() {
		return pageParam;
	}

	public void setPageParam(Map<String, String> pageParam) {
		this.pageParam = pageParam;
	}

	// private String entityId;
	//
	// private static final long serialVersionUID = 1L;
	//
	// public String getEntityId() {
	// return entityId;
	// }
	//
	// public void setEntityId(String entityId) {
	// this.entityId = entityId;
	// }

	/** 上傳檔案 */
	protected String caseFileUpload(String inParentSysid) {
		logger.debug("start");
		logger.debug("aaa-PATH:" + PATH);
		String subMainFilePath = PATH + inParentSysid + File.separator; // 正式路徑
		File dirFile = new File(subMainFilePath);
		if (!dirFile.exists())
			dirFile.mkdirs();// create document

		List<String> fileNameList = new ArrayList<String>(getUploadFileMap().keySet());
		for (String finalFileName : fileNameList) {
			File file = getUploadFileMap().remove(finalFileName);
			if (file != null) {
				String saveFilePath = subMainFilePath + finalFileName;
				File fileLocation = new File(saveFilePath);
				if (fileLocation.exists()) {
					String extention = FileUtil.getExtention(finalFileName);
					String _finalFileName = finalFileName.substring(0, finalFileName.lastIndexOf(extention));
					logger.debug("finalFileName:" + finalFileName);
					logger.debug("extention:" + extention);
					logger.debug("_finalFileName:" + _finalFileName);
					for (int i = 1;; i++) {
						if (i % 100 == 0)
							logger.warn("the same fileName count:" + i);
						saveFilePath = subMainFilePath + _finalFileName + " (" + i + ")" + extention;
						fileLocation = new File(saveFilePath);
						if (fileLocation.exists())
							continue;
						break;
					}
				}

				boolean isSuccess = FileUtil.moveFile(file, fileLocation);

				if (!isSuccess) {
					addActionError("上傳失敗");
				} else {
					// 新增 mts_appointment_file_link
					List saveFileList = new ArrayList();

					MtsAppointmentFileLink maf = new MtsAppointmentFileLink();
					Util.defaultPK(maf);
					defaultValue(maf);
					maf.setParentSysid(inParentSysid);
					maf.setFileName(finalFileName); // 病歷資料表SYSID

					saveFileList.add(maf);
					String daoFileMsg = cloudDao.save(sf(), saveFileList.toArray(), false, "INSERT");

					if (!"success".equals(daoFileMsg)) {
						addActionError("上傳失敗");
					}

				}
			}
		}
		logger.debug("end");
		return SUCCESS;
		// return vendorUploadInit();
	}

	/**
	 * 預約來台就醫單列表
	 */
	public String initMtsAppointmentData() {
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

			}
			request.setAttribute("rtnMtsAppointmentList", mtsAppointmentMapList);

		} else {
			request.setAttribute("rtnMtsAppointmentList", null);
		}

		return SUCCESS;
	}

	/** 預約來台就醫單內頁 */
	public String appointmentSub() {
		String memberSysid = getUserData2().getAccount().getSysid();
		String appointmentSysid = request.getParameter("appointmentSysid");
		logger.debug("appointmentSub-memberSysid:" + memberSysid);
		logger.debug("appointmentSub-appointmentSysid:" + appointmentSysid);

		if (StringUtils.isBlank(appointmentSysid)) {
			return ERROR;
		}

		List<MtsAppointment> mtsAppointmentlist = cloudDao.queryTable(sf(), MtsAppointment.class,
				new QueryGroup(new QueryRule(PK, appointmentSysid), new QueryRule("siteMemberSysid", memberSysid)),
				null, null, null);

		logger.debug("appointmentSub-mtsAppointmentlist.size():" + mtsAppointmentlist.size());
		if (mtsAppointmentlist.size() > 0) {
			MtsAppointment mtsAppointmentBean = mtsAppointmentlist.get(0);
			// request.setAttribute("itemBean",mtsAppointmentBean);
			// //這樣前端就不用 s:iterator，可直接取用

			findAppointmentFileLink(mtsAppointmentBean.getSysid()); // 取得病歷資料
			findAppointmentCaseLink(mtsAppointmentBean.getSysid()); // 取得病患個人病史與家族史

			Map<String, Object> mtsAppointmentMap = tw.com.mitac.ssh.util.Util.formatToMap(mtsAppointmentBean);
			logger.debug("mtsAppointmentlistAppointmentVendor():" + mtsAppointmentBean.getAppointmentVendor());

			logger.debug("Front其他服務PDF:" + mtsAppointmentBean.getOtherHelps());
			if (StringUtils.isNotBlank(mtsAppointmentBean.getOtherHelps())) {
				String otherHelps = mtsAppointmentBean.getOtherHelps();
				int tn = otherHelps.indexOf("TN");
				if (tn != -1) {
					otherHelps = otherHelps.replace("TN", getText("web.mts.interpreter"));
				}

				int vn = otherHelps.indexOf("VN");
				if (vn != -1) {
					otherHelps = otherHelps.replace("VN", getText("web.mts.visaApplication"));
				}

				int an = otherHelps.indexOf("AN");
				if (an != -1) {
					otherHelps = otherHelps.replace("AN", getText("web.mts.accommodationArrangement"));
				}

				int tr = otherHelps.indexOf("TR");
				if (tr != -1) {
					otherHelps = otherHelps.replace("TR", getText("web.mts.travelArrangement"));
				}

				int ts = otherHelps.indexOf("TS");
				if (ts != -1) {
					otherHelps = otherHelps.replace("TS", getText("web.mts.transportationArrangement"));
				}

				int oe = otherHelps.indexOf("OE");
				if (oe != -1) {
					otherHelps = otherHelps.replace("OE", getText("web.mts.otherServices"));
				}

				mtsAppointmentMap.put("otherHelps", otherHelps);
			}

			if ("MTS".equals(mtsAppointmentBean.getAppointmentVendor())) {
				mtsAppointmentMap.put("subVendorName", getText("web.node.mts"));
			} else {
				String vendorName = findVendorName(mtsAppointmentBean.getAppointmentVendor());
				mtsAppointmentMap.put("subVendorName", vendorName);
			}

			// 拿掉日期後面的毫秒
			String[] dateArr = mtsAppointmentMap.get("creationDate").toString().split(" ");
			String appointmentDate = dateArr[0]; // + " " + dateArr[1];
			mtsAppointmentMap.put("appointmentDate", appointmentDate);

			// 取醫療服務名稱
			String[] productsNameArr = StringUtils.defaultString(mtsAppointmentBean.getMtsProductsSysid()).split(",");
			String productsName = "";
			for (int i = 0; i < productsNameArr.length; i++) {
				productsName = findProductsName(productsNameArr[i].toString()) + "," + productsName;
				// productsName =
				// findProductsName(productsNameArr[i].toString()) + "</br>"
				// + productsName; //配合jsp改成textarea改回用,連接
			}

			// logger.debug("appointmentSub-productsName-1:" +
			// productsName);
			// logger.debug("appointmentSub-productsName-2:" +
			// StringUtils.substringBeforeLast(productsName , ","));
			mtsAppointmentMap.put("productsName", StringUtils.substringBeforeLast(productsName, ","));

			// 取得醫師名稱
			String doctorSysid = mtsAppointmentBean.getAppointmentDoctor();
			if (StringUtils.isNotBlank(doctorSysid)) {
				MtsDoctor doctor = getDataMtsDoctorTable().get(doctorSysid);
				mtsAppointmentMap.put("doctorName", doctor.getDoctorName());
			}

			// 取國別名稱
			String countryName = findCountryName(mtsAppointmentBean.getSuffererCountry());
			mtsAppointmentMap.put("countryName", countryName);

			request.setAttribute("isAcceptTaiwan", mtsAppointmentlist.get(0).getIsAcceptMedicalAtTaiwan());
			request.setAttribute("isAcceptTaiwanMechanism", mtsAppointmentlist.get(0).getTreatmentMechanism());

			// 取feedback
			findAppointFeedback(appointmentSysid);

			request.setAttribute("itemBean", mtsAppointmentMap);

		}

		return SUCCESS;
	}

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
	 * 取得國別名稱 輸入：inCountrySysid:CountrySysid 輸出：rtnCountryName:團隊名稱
	 */
	public String findCountryName(String inCountrySysid) {
		String rtnCountryName = "";
		CpsCountry cpsCountry = createDataTable(CpsCountry.class).get(inCountrySysid);
		if (cpsCountry != null) {
			rtnCountryName = StringUtils.defaultString(cpsCountry.getName());
		}
		return rtnCountryName;
	}

	/**
	 * 取得病歷資料 輸入：inAppointmentSysid：AppointmentSysid 輸出：病歷資料名稱
	 */
	public void findAppointmentFileLink(String inAppointmentSysid) {

		List<MtsAppointmentFileLink> mtsAppointmentFileLinkList = cloudDao.queryTable(sf(),
				MtsAppointmentFileLink.class, new QueryGroup(new QueryRule("parentSysid", EQ, inAppointmentSysid)),
				new QueryOrder[0], null, null);

		logger.debug("findAppointmentFileLink-mtsAppointmentFileLinkList.size():" + mtsAppointmentFileLinkList.size());
		if (mtsAppointmentFileLinkList.size() > 0) {

			// 配合前端套版，改成回傳list
			// String mtsAppointmentFiles = "";
			// for (int i=0;i<mtsAppointmentFileLinkList.size();i++) {
			// mtsAppointmentFiles =
			// mtsAppointmentFileLinkList.get(i).getFileName().toString() +
			// "</br>" + mtsAppointmentFiles;
			// }
			// logger.debug("findAppointmentFileLink-mtsAppointmentFiles:" +
			// mtsAppointmentFiles);
			// request.setAttribute("mtsAppointmentFiles", mtsAppointmentFiles);

			request.setAttribute("mtsAppointmentFilesList", mtsAppointmentFileLinkList);
		}
	}

	/*
	 * 取得病患個人病史與家族史 輸入：inAppointmentSysid：AppointmentSysid 輸出：病患個人病史與家族史
	 */
	public void findAppointmentCaseLink(String inAppointmentSysid) {
		session.remove("mtsAppointmentCaseLinkList");
		List<MtsAppointmentCaseLink> mtsAppointmentCaseLinkList = cloudDao.queryTable(sf(),
				MtsAppointmentCaseLink.class, new QueryGroup(new QueryRule("parentSysid", EQ, inAppointmentSysid)),
				new QueryOrder[0], null, null);
		addMultiLan(mtsAppointmentCaseLinkList, sf(), MtsAppointmentCaseLink.class);

		if (mtsAppointmentCaseLinkList.size() > 0) {
			List<Map> mtsAppointmentCaseMapList = formatListToMap(mtsAppointmentCaseLinkList);

			for (Map<String, Object> mtsAppointmentCase : mtsAppointmentCaseMapList) {
				String caseName = findCaseName(mtsAppointmentCase.get("appointmentCaseSysid").toString());
				mtsAppointmentCase.put("caseName", caseName);
			}

			request.setAttribute("mtsAppointmentCases", mtsAppointmentCaseMapList);
		} else {
			request.setAttribute("mtsAppointmentCases", null);
		}
	}

	public void findAppointFeedback(String inAppointmentSysid) {

		List<MtsAppointmentFeedback> mtsAppointmentFeedbackList = cloudDao.queryTable(sf(),
				MtsAppointmentFeedback.class, new QueryGroup(new QueryRule("parentSysid", EQ, inAppointmentSysid)),
				new QueryOrder[] { new QueryOrder("creationDate", ASC) }, null, null);

		// logger.debug("findAppointFeedback-mtsAppointmentFeedbackList.size():"
		// + mtsAppointmentFeedbackList.size());
		if (mtsAppointmentFeedbackList.size() > 0) {

			List<Map> mtsAppointmentFeedbackMap = formatListToMap(mtsAppointmentFeedbackList);
			// 將 bean list 轉為 map list
			for (Map map : mtsAppointmentFeedbackMap) {
				if (StringUtils.isNotBlank((String) map.get("memberSysid"))) {

					String memberSysid = (String) map.get("memberSysid");
					// logger.debug("findAppointFeedback-memberSysid:" +
					// memberSysid);

					List<CpsSiteMember> cpsSiteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class,
							new QueryGroup(new QueryRule(PK, EQ, memberSysid)), new QueryOrder[0], null, null);

					// logger.debug("findAppointFeedback-cpsSiteMemberList.size():"
					// + cpsSiteMemberList.size());
					if (cpsSiteMemberList.size() > 0) {
						if (StringUtils.isNotBlank(cpsSiteMemberList.get(0).getMemberName())) {
							String memberName = cpsSiteMemberList.get(0).getMemberName().toString().trim();
							// logger.debug("findAppointFeedback-memberName:" +
							// memberName);
							map.put("memberName", memberName);
						} else {
							map.put("memberName", "");
						}
					} else {
						map.put("memberName", "");
					}

				}

				if (StringUtils.isNotBlank((String) map.get("backMemberSysid"))) {

					String backMemberSysid = (String) map.get("backMemberSysid");
					// logger.debug("findAppointFeedback-memberSysid:" +
					// memberSysid);

					List<CpsMember> cpsMemberList = cloudDao.queryTable(sf(), CpsMember.class,
							new QueryGroup(new QueryRule(PK, EQ, backMemberSysid)), new QueryOrder[0], null, null);

					// logger.debug("findAppointFeedback-cpsSiteMemberList.size():"
					// + cpsSiteMemberList.size());
					if (cpsMemberList.size() > 0) {
						if (StringUtils.isNotBlank(cpsMemberList.get(0).getMemberName())) {
							String backMemberName = cpsMemberList.get(0).getMemberName().toString().trim();
							// logger.debug("findAppointFeedback-memberName:" +
							// memberName);
							map.put("backMemberName", backMemberName);
						} else {
							map.put("backMemberName", "");
						}
					} else {
						map.put("backMemberName", "");
					}

				}
			}

			request.setAttribute("mtsAppointmentFeedbackList", mtsAppointmentFeedbackMap);
		} else {
			request.setAttribute("mtsAppointmentFeedbackList", null);
		}
	}

	/**
	 * 取得疾病名稱 輸入：inAppointmentSysid：AppointmentSysid 輸出：病歷資料名稱
	 */
	public String findCaseName(String inCaseSysid) {
		String rtnCaseName = "";
		MtsAppointmentCase mtsAppointmentCase = getMtsAppointmentCaseTable().get(inCaseSysid);
		if (mtsAppointmentCase != null) {
			rtnCaseName = StringUtils.defaultString(mtsAppointmentCase.getItemName());
		}
		return rtnCaseName;
	}

	/** 新增留言訊息 */
	public String ajaxDoAddMtsContent() {
		resultMap = new HashMap();

		String siteMemberSysid = getUserData2().getAccount().getSysid();
		String addContent = request.getParameter("addContent");
		String appointmentSysid = request.getParameter("appointmentSysid");

		// logger.debug("$$$$$$$-siteMemberSysid:" + siteMemberSysid);
		// logger.debug("$$$$$$$-feedBackDesc:" + addContent);
		// logger.debug("$$$$$$$-collaborationSysid:" + appointmentSysid);

		String doResult = "";

		try {
			List saveList = new ArrayList();
			String daoMsg = "";

			MtsAppointmentFeedback mf = new MtsAppointmentFeedback();
			Util.defaultPK(mf);
			defaultValue(mf);
			mf.setParentSysid(appointmentSysid);
			mf.setFeedbackDetail(addContent);
			mf.setMemberSysid(siteMemberSysid);

			// 取得目前日期時間
			Date nowdate = new Date();
			mf.setFeedbackTime(nowdate);

			saveList.add(mf);
			daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");

			logger.debug("daoMsg-新增AppointmentFeedback:" + daoMsg);
			resultString = daoMsg;

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("msg", doResult);
			return JSON_RESULT;
		}

		// 處理寄信
		if (doResult.equals(SUCCESS)) {

			List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
					CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "appointment_C2V")),
					new QueryOrder[0], null, null);

			if (emailTemplate.size() > 0) {

				CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);
				String emailContent = appointmentEmailTemplate.getEmailContent();
				String emailTitle = appointmentEmailTemplate.getEmailTitle();

				List<MtsAppointment> mtsAppointmentList = (List<MtsAppointment>) cloudDao.queryTable(sf(),
						MtsAppointment.class, new QueryGroup(new QueryRule(PK, appointmentSysid)), new QueryOrder[0],
						null, null);

				if (mtsAppointmentList.size() > 0) {

					MtsAppointment mtsAp = mtsAppointmentList.get(0);

					/** 標題部分 */

					// $BILLNO$ 單號
					emailTitle = emailTitle.replace("$BILLNO$", mtsAp.getAppointmentNo());

					/** 內文部分 */

					// $BILLNO$ 單號
					emailContent = emailContent.replace("$BILLNO$", mtsAp.getAppointmentNo());

					// $CLIENT_NAME$ 前台會員名稱
					String cancleName = mtsAp.getSuffererSurname() + mtsAp.getSuffererName();
					emailContent = emailContent.replace("$CANCLE_NAME$", cancleName);

					// $FEEDBACK$ 留言內容
					emailContent = emailContent.replace("$FEEDBACK$", addContent);

					// $VENDOR_NAME$ 供應商名稱
					String vendorName = getDataCpsVendorTable().get(mtsAp.getAppointmentVendor()).getName();
					emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

					List<String> contentStringList = new ArrayList<String>();
					contentStringList.add(emailContent);

					// 寄信給廠商
					List<CpsMember> cpsMemberList = (List<CpsMember>) cloudDao.queryTable(sf(), CpsMember.class,
							new QueryGroup(new QueryRule("sourceSysid", mtsAp.getAppointmentVendor())),
							new QueryOrder[0], null, null);

					if (cpsMemberList.size() > 0) {
						for (CpsMember cpsMember : cpsMemberList) {
							new MailThread(new MailBean(cpsMember.getEmail(), emailTitle, contentStringList),
									getSendMailSetting()).start();
						}
					}
				}
			}
		}

		return JSON_RESULT;
	}

	/**
	 * PDF功能
	 */
	public String print() {
		appointmentSub();

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("logo", FileUtil.createInputStream("medical_index_logo.jpg"));
		Map<String, Object> itemBeanMap = (Map<String, Object>) request.getAttribute("itemBean");
		parameter.put("itemBean", itemBeanMap);
		List<MtsAppointmentFileLink> mtsAppointmentFileLinkList = (List<MtsAppointmentFileLink>) request
				.getAttribute("mtsAppointmentFilesList");
		parameter.put("mtsAppointmentFilesList", mtsAppointmentFileLinkList);
		if (mtsAppointmentFileLinkList != null) {
			parameter.put("item", getText("web.mts.item"));
			parameter.put("fileName", getText("web.mts.fileName"));
			parameter.put("noItem", "");
		} else {
			parameter.put("noItem", getText("web.mts.noItem"));
			parameter.put("item", "");
			parameter.put("fileName", "");
		}
		List<Map> mtsAppointmentCaseMapList = (List<Map>) request.getAttribute("mtsAppointmentCases");

		String doctorName = (String) itemBeanMap.get("doctorName");
		parameter.put("doctorName", doctorName);

		// request.setAttribute("isAcceptTaiwan",
		// mtsAppointmentlist.get(0).getIsAcceptMedicalAtTaiwan());
		// request.setAttribute("isAcceptTaiwanMechanism",
		// mtsAppointmentlist.get(0).getTreatmentMechanism());

		String accept = "";
		boolean isAcceptTaiwan = (boolean) request.getAttribute("isAcceptTaiwan");
		if (isAcceptTaiwan) {
			accept = getText("web.yes");
			String isAcceptTaiwanMechanism = (String) request.getAttribute("isAcceptTaiwanMechanism");
			if (StringUtils.isNotBlank(isAcceptTaiwanMechanism)) {
				accept += "," + isAcceptTaiwanMechanism;
			}
		} else {
			accept = getText("web.no");
		}
		parameter.put("isAcceptTaiwan", accept);

		parameter.put(JRParameter.REPORT_LOCALE, ActionContext.getContext().getLocale());

		// ResourceBundle rb = ResourceBundle.getBundle("globalFields",
		// ActionContext.getContext().getLocale());

		byte[] rawData = FileUtil.export(mtsAppointmentCaseMapList, parameter, "map");
		downInputStream = new ByteArrayInputStream(rawData);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");

		logger.debug("itemBeanMap:" + itemBeanMap);
		downFileName = df.format(systemDate) + "-" + itemBeanMap.get("appointmentNo") + "-"
				+ getText("web.mts.makeAppointmentOrder") + ".pdf";

		return DOWN_STREAM;
	}

	/**
	 * 客服中心-預約來台就醫單
	 */
	public String centerAppointmentPage() {
		String sysid = request.getParameter("v");

		Boolean isEntity = true;
		if (StringUtils.isNotBlank(sysid)) {
			isEntity = false;
			// MtsProducts mtsProducts = cloudDao.get(sf(), MtsProducts.class,
			// sysid);
			// addMultiLan(new Object[] { mtsProducts }, sf(), CpsVendor.class);
			// request.setAttribute("mtsProducts", mtsProducts);
			CpsVendor cpsVendor = cloudDao.get(sf(), CpsVendor.class, sysid);
			addMultiLan(new Object[] { cpsVendor }, sf(), CpsVendor.class);
			request.setAttribute("cpsVendor", cpsVendor);

			// 查詢醫師列表
			List<MtsDoctor> mtsDoctorList = cloudDao.queryTable(sf(), MtsDoctor.class,
					new QueryGroup(new QueryRule("vendorSysid", sysid)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(mtsDoctorList, sf(), MtsDoctor.class);
			session.put("mtsDoctorList", mtsDoctorList);
			// List<String> doctorMenu = (List<String>)
			// cloudDao.findProperty(sf(), MtsDoctor.class, new QueryGroup(
			// new QueryRule("vendorSysid", sysid)), new QueryOrder[] { new
			// QueryOrder(DATA_ORDER) }, false,
			// "doctorName");
			// session.put("doctorMenu", doctorMenu);
		}
		request.setAttribute("isEntity", isEntity);

		// 取得前台登入者資料
		findCpsSiteMember();

		return SUCCESS;
	}

	/** 取得前台登入者資料 */
	protected void findCpsSiteMember() {
		pageParam = (Map<String, String>) session.remove("pageParam");

		logger.debug("-----取得帳號資料START-------");
		logger.debug(pageParam);
		logger.debug("------取得帳號資料END--------");
		if (pageParam == null) {

			pageParam = new HashMap();
			logger.debug("姓氏:" + getUserData2().getAccount().getFirstName());
			pageParam.put("firstName", getUserData2().getAccount().getFirstName());
			pageParam.put("lastName", getUserData2().getAccount().getLastName());
			pageParam.put("gender", getUserData2().getAccount().getGender());
			if (getUserData2().getAccount().getBirthday() != null) {
				DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				String dateString = sdf.format(getUserData2().getAccount().getBirthday());
				logger.debug("生日轉換dateString:" + dateString);

				if (dateString != null) {
					pageParam.put("birthday", dateString);
				}
			}
			pageParam.put("suffererCountry", getUserData2().getAccount().getCountrySysid());
			pageParam.put("suffererAreaCode", getUserData2().getAccount().getCompanyAreaCode());
			pageParam.put("suffererAddress", getUserData2().getAccount().getAddress());
			pageParam.put("suffererEmail", getUserData2().getAccount().getEmail());
			pageParam.put("countryCode", getUserData2().getAccount().getCountryCode());
			pageParam.put("countryCode2", getUserData2().getAccount().getCountryCode2());
			pageParam.put("phone", getUserData2().getAccount().getPhone());
			pageParam.put("suffererReservePhone", getUserData2().getAccount().getPhone2());

		}
		String con = getUserData2().getAccount().getCountrySysid();
		if (con != null) {

			logger.debug("取得國家名稱的SYSID:" + con);
			findCpsCountryName(con);
		}

	}

	// 取國別名稱
	public void findCpsCountryName(String inCountrySysid) {

		List<CpsCountry> cpsCountryNmList = cloudDao.queryTable(sf(), CpsCountry.class,
				new QueryGroup(new QueryRule(PK, EQ, inCountrySysid)), new QueryOrder[0], null, null);
		addMultiLan(cpsCountryNmList, sf(), CpsCountry.class);
		logger.debug("findCpsCountryName-cpsCountryNmList.size():" + cpsCountryNmList.size());
		if (cpsCountryNmList.size() > 0) {
			session.put("cpsCountryNmList", cpsCountryNmList.get(0));

		} else {
			session.put("cpsCountryNmList", null);

		}
	}

	/**
	 * 預覽頁面
	 */
	public String previewWithValue() {
		session.put("pageParam", pageParam);
		request.setAttribute("bean", exchangeFromParam());
		return SUCCESS;
	}

	public String appointmentSubmit() {

		// logger.debug("appointmentSubmit-1:" +
		// pageParam.get("medicalSymptomDescribe"));
		// logger.debug("appointmentSubmit-2:" +
		// pageParam.get("selOtherHelps"));

		// 新增mts_appointment
		Boolean isAddMtsAppointment = addMtsAppointment();
		logger.debug("isSuccess=" + isAddMtsAppointment);
		request.setAttribute("isSuccess", isAddMtsAppointment);

		pageParam = (Map<String, String>) session.get("pageParam");

		String v = pageParam.get("vendorKey");
		request.setAttribute("cpsv", v);
		session.remove("pageParam");
		return SUCCESS;
	}

	protected MtsAppointment exchangeFromParam() {
		Map<String, String> pageParam = (Map<String, String>) session.get("pageParam");
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MtsAppointment mf = new MtsAppointment();
		Util.defaultPK(mf);
		defaultValue(mf);

		mf.setSiteMemberSysid(getUserData2().getAccount().getSysid());
		mf.setAppointmentStatus("Pending"); // 狀態：待處理
		mf.setIsRead(false);

		String vendorKey = pageParam.get("vendorKey");// 直接接收key值
		if (StringUtils.isNotBlank(vendorKey)) {
			mf.setAppointmentVendor(vendorKey);
		} else {
			mf.setAppointmentVendor("MTS"); // 從客服中心進來固定[MTS]
		}

		defaultBillno(mf); // 預約單號
		mf.setMtsProductsSysid(pageParam.get("selServices")); // 醫療服務

		// 寫入
		mf.setSuffererName(pageParam.get("firstName")); // 患者名字
		mf.setSuffererSurname(pageParam.get("lastName")); // 患者姓氏
		mf.setSuffererGender(pageParam.get("gender")); // 患者性別
		if (StringUtils.isNotBlank(pageParam.get("birthday").toString())) {
			try {
				Date birthday = sdf.parse(pageParam.get("birthday").toString() + " " + "00:00:00");
				mf.setSuffererBirthday(birthday); // 患者生日
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		mf.setSuffererCountry(pageParam.get("suffererCountry")); // 患者國別
		mf.setSuffererAreaCode(pageParam.get("suffererAreaCode"));// 患者國別區碼
		mf.setSuffererAddress(pageParam.get("suffererAddress")); // 患者地址
		mf.setSuffererEmail(pageParam.get("suffererEmail")); // 患者EMAIL
		mf.setCountryCode(pageParam.get("countryCode"));
		mf.setSuffererPhone(pageParam.get("phone")); // 患者電話
		// 患者備用電話
		mf.setCountryCode2(pageParam.get("countryCode2"));
		mf.setSuffererReservePhone(pageParam.get("suffererReservePhone"));

		mf.setMedicalSymptomDescribe(pageParam.get("medicalSymptomDescribe")); // 醫療症狀簡述
		if (StringUtils.isNotBlank(pageParam.get("localTime1"))) {
			// Date localdate1 = sdf.parse(.toString() + " " + "00:00:00");
			Date localdate1 = DateTypeConverter.convertFromString(pageParam.get("localTime1"));

			mf.setAppointmentDateOneLocal(localdate1); // 就診日期一(local)
		}
		if (StringUtils.isNotBlank(pageParam.get("localTime2"))) {
			// Date localdate2 =
			// sdf.parse(pageParam.get("localTime2").toString() + " " +
			// "00:00:00");
			Date localdate2 = DateTypeConverter.convertFromString(pageParam.get("localTime2"));
			mf.setAppointmentDateTwoLocal(localdate2); // 就診日期二(local)
		}
		if (StringUtils.isNotBlank(pageParam.get("taiwanTime1"))) {
			// Date taiwandate1 =
			// sdf.parse(pageParam.get("taiwanTime1").toString() + " " +
			// "00:00:00");
			Date taiwandate1 = DateTypeConverter.convertFromString(pageParam.get("taiwanTime1"));

			mf.setAppointmentDateOneTaiwan(taiwandate1); // 就診日期一(taiwan)
		}
		if (StringUtils.isNotBlank(pageParam.get("taiwanTime2"))) {
			// Date taiwandate2 =
			// sdf.parse(pageParam.get("taiwanTime2").toString() + " " +
			// "00:00:00");
			Date taiwandate2 = DateTypeConverter.convertFromString(pageParam.get("taiwanTime2"));

			mf.setAppointmentDateTwoTaiwan(taiwandate2); // 就診日期二(taiwan)
		}

		mf.setAppointmentDoctor(pageParam.get("appointmentDoctor")); // 預約醫師

		// 是否在台灣接受過醫療
		if (pageParam.get("isAcceptMedicalAtTaiwan").equals("true")) {
			mf.setIsAcceptMedicalAtTaiwan(true);
			mf.setTreatmentMechanism(pageParam.get("treatmentMechanism"));
		} else {
			mf.setIsAcceptMedicalAtTaiwan(false);
		}

		// 其他協助
		if (StringUtils.isNotBlank(pageParam.get("selOtherHelps"))) {
			String otherHelps = pageParam.get("selOtherHelps").toString();
			int tt = pageParam.get("selOtherHelps").indexOf("TN");
			logger.debug("ttttt:" + tt);
			logger.debug("newotherHelps:" + otherHelps.replace("TN", "TN:" + pageParam.get("needlanguage").toString()));
			if (tt != -1) {
				otherHelps = otherHelps.replace("TN", "TN:" + pageParam.get("needlanguage").toString());
			}
			mf.setOtherHelps(otherHelps);
		}

		// 重大手術
		if (StringUtils.isNotBlank(pageParam.get("greatOperation"))) {
			mf.setGreatOperation(pageParam.get("greatOperation").toString().trim());
		}

		Set<MtsAppointmentCaseLink> detailSet = new HashSet<MtsAppointmentCaseLink>();
		// 處理尾檔 mts_appointment_case_link 新增資料
		for (MtsAppointmentCase mp : getMtsAppointmentCaseTable().values()) {
			logger.debug("addMtsAppointmentCaseLink-caseSysid:" + mp.getSysid());
			// List saveCaseList = new ArrayList();

			// 先判斷有沒有勾選
			String isSelfId = "caseSelf_" + mp.getSysid();
			String selfRemark = "caseSelfRemark_" + mp.getSysid();
			String isParentsId = "caseParents_" + mp.getSysid();
			String parentsRemark = "caseParentsRemark_" + mp.getSysid();
			String isBrothersId = "caseBrothers_" + mp.getSysid();
			String brothersRemark = "caseBrothersRemark_" + mp.getSysid();
			String isGrandparentsId = "caseGrandparents_" + mp.getSysid();
			String grandparentsRemark = "caseGrandparentsRemark_" + mp.getSysid();
			Boolean doCheck = false;

			if (StringUtils.isNotBlank(pageParam.get(isSelfId)) || StringUtils.isNotBlank(pageParam.get(isParentsId))
					|| StringUtils.isNotBlank(pageParam.get(isBrothersId))
					|| StringUtils.isNotBlank(pageParam.get(isGrandparentsId))) {
				doCheck = true;
			}

			if (doCheck) {
				MtsAppointmentCaseLink maL = new MtsAppointmentCaseLink();
				Util.defaultPK(maL);
				defaultValue(maL);
				maL.setParentSysid(mf.getSysid());
				maL.setAppointmentCaseSysid(mp.getSysid()); // 病歷資料表SYSID

				// String isSelfId = "caseSelf_" + mp.getSysid();
				// String selfRemark = "caseSelfRemark_" + mp.getSysid();
				// String isParentsId = "caseParents_" + mp.getSysid();
				// String parentsRemark = "caseParentsRemark_" +
				// mp.getSysid();
				// String isBrothersId = "caseBrothers_" + mp.getSysid();
				// String brothersRemark = "caseBrothersRemark_" +
				// mp.getSysid();
				// String isGrandparentsId = "caseGrandparents_" +
				// mp.getSysid();
				// String grandparentsRemark = "caseGrandparentsRemark_" +
				// mp.getSysid();

				logger.debug("addMtsAppointmentCaseLink-idname:" + pageParam.get(isSelfId));
				// 本人
				if (StringUtils.isNotBlank(pageParam.get(isSelfId))) {
					if ("on".equals(pageParam.get(isSelfId).toString())) {
						logger.debug("addMtsAppointmentCaseLink-本人:" + mp.getSysid());
						maL.setIsSelf(true);

						if (StringUtils.isNotBlank(pageParam.get(selfRemark))) {
							maL.setSelfRemark(pageParam.get(selfRemark).toString());
						}
					} else {
						maL.setIsSelf(false);
					}
				} else {
					maL.setIsSelf(false);
				}

				// 父母
				if (StringUtils.isNotBlank(pageParam.get(isParentsId))) {
					if ("on".equals(pageParam.get(isParentsId).toString())) {
						logger.debug("addMtsAppointmentCaseLink-父母:" + mp.getSysid());
						maL.setIsParents(true);

						if (StringUtils.isNotBlank(pageParam.get(parentsRemark))) {
							maL.setParentsRemark(pageParam.get(parentsRemark).toString());
						}
					} else {
						maL.setIsParents(false);
					}
				} else {
					maL.setIsParents(false);
				}

				// 兄弟姊妹
				if (StringUtils.isNotBlank(pageParam.get(isBrothersId))) {
					if ("on".equals(pageParam.get(isBrothersId).toString())) {
						logger.debug("addMtsAppointmentCaseLink-兄弟姊妹:" + mp.getSysid());
						maL.setIsBrothers(true);

						if (StringUtils.isNotBlank(pageParam.get(brothersRemark))) {
							maL.setBrothersRemark(pageParam.get(brothersRemark).toString());
						}
					} else {
						maL.setIsBrothers(false);
					}
				} else {
					maL.setIsBrothers(false);
				}

				// (外)祖父母
				if (StringUtils.isNotBlank(pageParam.get(isGrandparentsId))) {
					if ("on".equals(pageParam.get(isGrandparentsId).toString())) {
						logger.debug("addMtsAppointmentCaseLink-兄弟姊妹:" + mp.getSysid());
						maL.setIsGrandparents(true);

						if (StringUtils.isNotBlank(pageParam.get(grandparentsRemark))) {
							maL.setGrandparentsRemark(pageParam.get(grandparentsRemark).toString());
						}
					} else {
						maL.setIsGrandparents(false);
					}
				} else {
					maL.setIsGrandparents(false);
				}

				detailSet.add(maL);
			}
		}
		mf.setDetailSet(detailSet);

		return mf;
	}

	/** 儲存mts_appointment */
	protected boolean addMtsAppointment() {
		try {
			List saveList = new ArrayList();
			String daoMsg = "";

			MtsAppointment mf = exchangeFromParam();

			saveList.add(mf);
			daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");

			/** 儲存後寄信 */
			if (daoMsg.equals(SUCCESS)) {
				String clientName = mf.getSuffererSurname() + " " + mf.getSuffererName();

				logger.debug("寄信客戶名稱：" + clientName);
				logger.debug("寄信客戶EMAIL：" + mf.getSuffererEmail());
				logger.debug("寄信廠商SYSID：" + mf.getAppointmentVendor());

				String vendorName = "";
				if (StringUtils.isNotBlank(mf.getAppointmentVendor())) {
					List<CpsVendor> vendorList = cloudDao.queryTable(sf(), CpsVendor.class,
							new QueryGroup(new QueryRule(PK, mf.getAppointmentVendor())), new QueryOrder[0], null,
							null);
					if (vendorList.size() > 0) {
						vendorName = vendorList.get(0).getName();
					}
				}
				logger.debug("(寄信)客戶單號" + mf.getAppointmentNo());
				emailClient(clientName, mf.getSuffererEmail(), vendorName, mf.getAppointmentNo());
				emailVendor(clientName, mf.getAppointmentVendor(), mf.getAppointmentNo());
			}

			// 上傳檔案
			String uploadStatus = caseFileUpload(mf.getSysid());
			logger.debug("daoMsg-uploadStatus:" + uploadStatus);

			logger.debug("daoMsg-新增Appointment:" + daoMsg);
			resultString = daoMsg;

		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("errMsg", e.toString());
			return false;
		}
		return true;
	}

	protected Map<String, File> getUploadFileMap() {
		Map<String, File> uploadFileMap = (Map<String, File>) sessionGet("uploadFileMap");
		if (uploadFileMap == null) {
			uploadFileMap = new LinkedHashMap<String, File>();
			sessionSet("uploadFileMap", uploadFileMap);
		}
		return uploadFileMap;
	}

	public String uploadFile() {
		if (uploadItems1 != null && uploadItems1.size() > 0) {
			String subMainFilePath = FILE_DEFAULT_CREATE();
			File dirFile = new File(subMainFilePath);
			if (!dirFile.exists())
				dirFile.mkdirs();
			for (String fileName : uploadItems1FileName)
				if (!FileUtil.validateExtention(pictureExtention, fileName)) {
					resultString = "[" + fileName + "]"
							+ getText("errMsg.fileFormatWrong", new String[] { FileUtil.getExtention(fileName) });
					return JSON_RESULT;
				}
			for (int fileIndex = 0; fileIndex < uploadItems1FileName.size(); fileIndex++) {
				String finalFileName = uploadItems1.get(fileIndex).getName();
				// logger.debug("getName:"+finalFileName);
				String saveFilePath = subMainFilePath + finalFileName;
				File fileLocation = new File(saveFilePath);
				if (fileLocation.exists()) {
					String extention = FileUtil.getExtention(finalFileName);
					String _finalFileName = finalFileName.substring(0, finalFileName.lastIndexOf(extention));
					logger.debug("finalFileName:" + finalFileName);
					logger.debug("extention:" + extention);
					logger.debug("_finalFileName:" + _finalFileName);
					for (int i = 1;; i++) {
						if (i % 100 == 0)
							logger.warn("the same fileName count:" + i);
						saveFilePath = subMainFilePath + _finalFileName + " (" + i + ")" + extention;
						fileLocation = new File(saveFilePath);
						if (fileLocation.exists())
							continue;
						break;
					}
				}
				// logger.debug("測試 itemPicture儲存路徑:" + saveFilePath);

				boolean isSuccess = FileUtil.moveFile(uploadItems1.get(fileIndex), fileLocation);
				if (!isSuccess) {
					addActionError("上傳失敗");
				} else {
					String fileName = uploadItems1FileName.get(fileIndex);
					fileName = fileName.replace(",", "");
					if (getUploadFileMap().containsKey(fileName)) {
						String extention = FileUtil.getExtention(fileName);
						String _finalFileName = fileName.substring(0, fileName.lastIndexOf(extention));
						logger.debug("fileName:" + fileName);
						logger.debug("extention:" + extention);
						logger.debug("_finalFileName:" + _finalFileName);
						for (int i = 1;; i++) {
							if (i % 100 == 0)
								logger.warn("the same fileName count:" + i);
							fileName = _finalFileName + " (" + i + ")" + extention;
							if (getUploadFileMap().containsKey(fileName))
								continue;
							break;
						}
					}
					getUploadFileMap().put(fileName, fileLocation);
					// logger.info("fileName:" + fileName);
					// logger.info("fileLocation:" + fileLocation);
				}
			}
		} else {
			logger.warn("uploadItems1 NOT FOUND!");
		}

		resultString = SUCCESS;
		return JSON_RESULT;
	}

	public String deleteFile() {
		String fileName = request.getParameter("fileName");
		File file = getUploadFileMap().remove(fileName);
		if (file != null) {
			file.delete();
			resultString = SUCCESS;
		} else {
			resultString = ERROR;
		}
		return JSON_RESULT;
	}

	public String showFile() {
		resultObject = getUploadFileMap().keySet();
		return JSON_RESULT;
	}

	public String emailClient(String clientName, String clientEmail, String vendorName, String billNo) {

		logger.debug("寄信客戶資訊:" + clientName + "," + clientEmail);

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "appointment_Confirmation")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() == 1) {
			CpsEmailTemplate emailClientEmailTemplate = emailTemplate.get(0);

			String emailContent = emailClientEmailTemplate.getEmailContent();
			String emailTitle = emailClientEmailTemplate.getEmailTitle();

			/** 標題部分 */

			// 單號
			if (StringUtils.isNotBlank(billNo)) {
				emailTitle = emailTitle.replace("$BILLNO$", billNo);
			}

			/** 內文部分 */

			// 前台會員名稱
			if (StringUtils.isNotBlank(clientName)) {
				emailContent = emailContent.replace("$CLIENT_NAME$", clientName);
			}

			// 供應商名稱
			if (StringUtils.isNotBlank(vendorName)) {
				emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);
			}

			// 單號
			if (StringUtils.isNotBlank(billNo)) {
				emailContent = emailContent.replace("$BILLNO$", billNo);
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

	public String emailVendor(String clientName, String vendorSysid, String billNo) {
		logger.debug("寄信廠商資訊:" + vendorSysid);

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "appointment_Notice_Vendor")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() == 1) {
			CpsEmailTemplate emailVendorEmailTemplate = emailTemplate.get(0);

			String emailContent = emailVendorEmailTemplate.getEmailContent();
			String emailTitle = emailVendorEmailTemplate.getEmailTitle();

			/** 標題部分 */

			// 單號
			if (StringUtils.isNotBlank(billNo)) {
				emailTitle = emailTitle.replace("$BILLNO$", billNo);
			}

			/** 內文部分 */
			// 單號
			if (StringUtils.isNotBlank(billNo)) {
				emailContent = emailContent.replace("$BILLNO$", billNo);
			}

			// 填寫人名稱
			if (StringUtils.isNotBlank(clientName)) {
				emailContent = emailContent.replace("$CLIENT_NAME$", clientName);
			}

			// 廠商名稱
			List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
					new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);
			if (vendorList.size() > 0) {
				emailContent = emailContent.replace("$VENDOR_NAME$", vendorList.get(0).getName());
			}

			List<String> emailVendorList = new ArrayList<String>();
			emailVendorList.add(emailContent);

			if (vendorSysid.equals("MTS")) {
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