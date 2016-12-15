package tw.com.mitac.thp.action;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsAdvertisement;
import tw.com.mitac.thp.bean.CpsAdvertisementRent;
import tw.com.mitac.thp.bean.CpsEmailHistory;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsEpaper;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.bean.HpsVendorItem;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

public class CpsAdvertisementRentAction extends BasisCrudAction<CpsAdvertisementRent> {

	protected List<File> img1;
	protected List<String> img1FileName;

	public List<File> getImg1() {
		return img1;
	}

	public void setImg1(List<File> img1) {
		this.img1 = img1;
	}

	public List<String> getImg1FileName() {
		return img1FileName;
	}

	public void setImg1FileName(List<String> img1FileName) {
		this.img1FileName = img1FileName;
	}

	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("cpsAd_pic");

	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return QueryGroup.DEFAULT;
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	public String main() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			addActionMessage("管理者可以查詢所有項目");
		}
		return super.main();
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<CpsAdvertisementRent> list = (List<CpsAdvertisementRent>) oldArr[1];
		for (CpsAdvertisementRent bean : list) {
			// 資料表名稱，並決定比對欄位
			if (StringUtils.isBlank(bean.getSourceSysid())) {
				bean.setSourceSysid("");
			} else if (CpsEpaper.class.getSimpleName().equals(bean.getSourceType())) {
				CpsEpaper source = createDataTable(CpsEpaper.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getCreator() + getSplitChar() + source.getTitle());
			} else if (HpsVendorItem.class.getSimpleName().equals(bean.getSourceType())) {
				HpsVendorItem source = createDataTable(HpsVendorItem.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getDataId() + getSplitChar() + source.getName());
			} else if (HpsCoreItemType.class.getSimpleName().equals(bean.getSourceType())) {
				HpsCoreItemType source = createDataTable(HpsCoreItemType.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getDataId() + getSplitChar() + source.getName());
			} else {
				bean.setSourceSysid("");
			}
		}
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSysid())) {
			CpsMember user = getUserAccount();
			String userSysid = (String) user.getSysid();
			bean.setIssueMemberSysid(userSysid);
			List<CpsEntity> entitys = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(new QueryRule("dataId",
					"hps")), new QueryOrder[0], null, null);
			if (entitys.size() > 0) {
				bean.setEntitySysid(entitys.get(0).getSysid());
			}
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid("*");
			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid(user.getSourceSysid());
			}
			bean.setFilePath("*");
		}

		String result = super.edit();
		if (StringUtils.isBlank(bean.getSourceSysid())) {
			bean.setSourceSysid("");
		} else if (CpsEpaper.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("epaperSysid", bean.getSourceSysid());
			CpsEpaper source = createDataTable(CpsEpaper.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("epaperSysidShow", source.getCreator() + getSplitChar() + source.getTitle());
		} else if (HpsVendorItem.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("vendorItemSysid", bean.getSourceSysid());
			HpsVendorItem source = createDataTable(HpsVendorItem.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("vendorItemSysidShow", source.getDataId() + getSplitChar() + source.getName());
		} else if (HpsCoreItemType.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("vendorItemSysid", bean.getSourceSysid());
			HpsCoreItemType source = createDataTable(HpsCoreItemType.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("vendorItemSysidShow", source.getDataId() + getSplitChar() + source.getName());
		} else {
			bean.setSourceSysid("");
		}
		return result;
	}

	@Override
	protected boolean executeSave() {
		// 新增/開立時
		if (StringUtils.isBlank(bean.getBillStatus()) || BillStatusUtil.NEW.equals(bean.getBillStatus())) {
			// 檢核
			String msg = preCheck();
			if (!SUCCESS.equals(msg)) {
				addActionError(msg);
				return false;
			}
			// 上傳
			if (!SUCCESS.equals(uploadData(img1, img1FileName, "filePath")))
				return false;
			// 刪除
			String deleteFileName = request.getParameter("deleteFileName");
			if (StringUtils.isNotBlank(deleteFileName)) {
				String deleteFilePath = PATH + bean.getSysid() + "/" + deleteFileName;
				logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
				File deleteLocation = new File(deleteFilePath);
				deleteLocation.delete();
				bean.setFilePath("*");
			}
		}

		return super.executeSave();
	}

	@Override
	public String turnToConfirmed() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位，不適用確認功能");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (StringUtils.isBlank(billStatus) || BillStatusUtil.NEW.equals(billStatus)) {
				// 檢核
				String msg = preCheck();
				if (!SUCCESS.equals(msg)) {
					addActionError(msg);
					return EDIT_ERROR;
				}
				if ("*".equals(bean.getFilePath())) {
					addActionError("尚未上傳圖片");
					return EDIT_ERROR;
				}
				// 檢查日期是否可用
				msg = checkAd();
				if (!SUCCESS.equals(msg)) {
					addActionError(msg);
					return EDIT_ERROR;
				}
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.CONFIRM);
			} else {
				addActionError("狀態錯誤，無法確認");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return save();
	}

	@Override
	public String turnToApproved() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位，不適用審核功能");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.CONFIRM.equals(billStatus)) {
				// 審核通過，寄送Email給店家
				List<CpsEmailTemplate> l = (List<CpsEmailTemplate>) cloudDao
						.queryTable(sf(), CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId",
								"cpsAdvertisementRent")), new QueryOrder[0], null, null);
				if (l.size() == 0) {
					addActionError("查無Email範本");
					return EDIT_ERROR;
				}
				CpsEmailTemplate cpsEmailTemplate = l.get(0);
				String emailContent = cpsEmailTemplate.getEmailContent();

				CpsVendor cpsVendor = createDataTable(CpsVendor.class).get(bean.getVendorSysid());
				// TODO 以後健康館會切開
				String vendorEmail = "";// cpsVendor.getVendorEmail();
				String name = cpsVendor.getName();
				logger.info("email:" + vendorEmail);
				String content = emailContent.replace("$VENDOR_NAME$", name);

				CpsEmailHistory history = new CpsEmailHistory();
				Util.defaultPK(history);
				defaultValue(history);
				if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
					history.setEntitySysid(getUserAccount().getSourceSysid());
				} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
					history.setVendorSysid(getUserAccount().getSourceSysid());
				}
				history.setEmailAddress(vendorEmail);
				history.setEmailTitle(cpsEmailTemplate.getEmailTitle());
				history.setEmailContent(content);
				history.setRemark(history.getCreationDate());
				saveList.add(history);

				List<String> contentStringList = new ArrayList<String>();
				contentStringList.add(content);
				new MailThread(new MailBean(vendorEmail, cpsEmailTemplate.getEmailTitle(), contentStringList),
						getSendMailSetting()).start();
				addActionMessage("信件已發送");
				// 寄送End

				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.APPROVED);
			} else {
				addActionError("狀態錯誤，無法審核");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// 檢核
	protected String preCheck() {
		if (bean.getStartDate().compareTo(systemDatetime) < 0)
			return "「廣告開始日期」不可小於系統日期";
		if (bean.getEndDate().compareTo(bean.getStartDate()) < 0)
			return "「廣告結束日期」不可小於「廣告開始日期」";
		// 差距天數 + 1(當天)
		Long day = (bean.getEndDate().getTime() - bean.getStartDate().getTime()) / (24 * 60 * 60 * 1000) + 1L;
		CpsAdvertisement ad = cloudDao.get(sf(), CpsAdvertisement.class, bean.getAdvertisementSysid());
		if (day.compareTo(ad.getEachMaxRentDay()) > 0)
			return "廣告日期起迄區間(" + day + "天)不可大於「廣告區塊」設定的「每次最長租用天數」(" + ad.getEachMaxRentDay() + "天)";

		// 重新計算金額
		bean.setUnitPrice(ad.getUnitPrice());
		bean.setTotalAmount(ad.getUnitPrice().multiply(new BigDecimal(day)));
		return SUCCESS;
	}

	// 檢查該日期廣告區塊是否達到上限
	protected String checkAd() {
		String msg = "";
		CpsAdvertisement ad = cloudDao.get(sf(), CpsAdvertisement.class, bean.getAdvertisementSysid());
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		// 差距天數 + 1(當天)
		Long day = (bean.getEndDate().getTime() - bean.getStartDate().getTime()) / (24 * 60 * 60 * 1000) + 1L;
		for (int i = 0; i < day; i++) {
			Date thisDay = new Date(bean.getStartDate().getTime() + i * (1000 * 60 * 60 * 24)); // 第1+i天
			// 條件：廣告日期包含當天＆狀態＝確認or審核or結案
			int count = cloudDao.queryCount(sf(), CpsAdvertisementRent.class, new QueryGroup(new QueryRule(
					"advertisementSysid", EQ, bean.getAdvertisementSysid()), new QueryRule("startDate", LE, thisDay),
					new QueryRule("endDate", GE, thisDay), new QueryRule(BILL_STATUS, IN, BillStatusUtil.CONFIRM + ","
							+ BillStatusUtil.APPROVED + "," + BillStatusUtil.FINISH)));
			// 記錄超過MAX的日期
			if (count >= ad.getMaxAmount())
				msg += "," + df.format(thisDay);
		}
		if (msg.length() > 0)
			msg = msg.substring(1) + "等日期的申請已達到上限，請選擇其他日期。";
		else
			msg = SUCCESS;

		return msg;
	}

	// 上傳檔案
	public String uploadData(List<File> data, List<String> dataFileName, String name) {
		if (data != null && data.size() > 0) {
			String subMainFilePath = PATH + bean.getSysid() + File.separator;
			File dirFile = new File(subMainFilePath);
			if (!dirFile.exists())
				dirFile.mkdirs();// create document
			for (String fileName : dataFileName)
				if (!FileUtil.validateExtention(pictureExtention, fileName)) {
					addActionError(getText("errMsg.fileFormatWrong", new String[] { FileUtil.getExtention(fileName) }));
					return EDIT_ERROR;
				}
			for (int fileIndex = 0; fileIndex < dataFileName.size(); fileIndex++) {
				String finalFileName = dataFileName.get(fileIndex);
				String saveFilePath = subMainFilePath + finalFileName;
				logger.debug("測試 儲存路徑:" + saveFilePath);
				File fileLocation = new File(saveFilePath);
				FileUtil.moveFile(data.get(fileIndex), fileLocation);
				if (fileIndex == 0) {
					try {
						PropertyUtils.setProperty(bean, name, finalFileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return SUCCESS;
	}
}