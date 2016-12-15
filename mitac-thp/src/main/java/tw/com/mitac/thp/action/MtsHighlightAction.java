package tw.com.mitac.thp.action;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.DeleteStatement;
// Generated Sat May 14 10:58:38 CST 2016 by GenCode.javaimport tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAdsC;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsInfoLink;
import tw.com.mitac.thp.bean.MtsMenuLink;
import tw.com.mitac.thp.bean.MtsRecommandItem;
import tw.com.mitac.thp.util.FileUtil;

/**
 * MtsHighlightAction generated by GenCode.java
 */
public class MtsHighlightAction extends DetailController<MtsHighlight> {
	// public class MtsHighlightAction<MO> extends DetailController<MO> {

	@Override
	public String[] getImgCols() {
		return new String[] { "highlightSummaryImg" };
	}

	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("mtsHighlight_pic");

	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsInfoLink.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", MtsAdsC.class));

		return detailClassMap;
	}

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

	@Override
	protected boolean executeSave() {
		// 20160715修正模仿BhsMenu版本-start
		String msg = saveMtsMenuSel();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}
		// 20160715修正模仿BhsMenu版本-end

		Set dataSet = findDetailSetWhenEdit(DETAIL_SET);
		// 上傳
		if (img1 != null && img1.size() > 0) {
			String subMainFilePath = PATH + bean.getSysid() + File.separator;
			File dirFile = new File(subMainFilePath);
			// 如果我這個資料夾沒有就創建
			if (!dirFile.exists())
				dirFile.mkdirs();// create document

			for (String fileName : img1FileName)
				// 驗證圖片附檔名
				if (!FileUtil.validateExtention(resultFileExtention, fileName)) {
					addActionError(getText("errMsg.fileFormatWrong", new String[] { FileUtil.getExtention(fileName) }));
					return false;
				}

			for (int fileIndex = 0; fileIndex < img1FileName.size(); fileIndex++) {

				String finalFileName = img1FileName.get(fileIndex);
				String saveFilePath = subMainFilePath + finalFileName;
				logger.debug("測試 儲存路徑:" + saveFilePath);
				File fileLocation = new File(saveFilePath);

				// 判斷檔案存不存在
				if (fileLocation.exists()) {
					// 如果存在的話應該使用不同名稱

					// 前面+日期
					finalFileName = reportName + finalFileName;
					saveFilePath = subMainFilePath + finalFileName;
					logger.debug("測試 儲存路徑:" + saveFilePath);
					fileLocation = new File(saveFilePath);

				}

				FileUtil.moveFile(img1.get(fileIndex), fileLocation);

				MtsInfoLink f = getDefaultDMO(MtsInfoLink.class);
				f.setParentSysid(bean.getSysid());
				f.setFileName(finalFileName);

				// 去掉附檔名
				String ext = FileUtil.getExtention(finalFileName);
				String linktxt = finalFileName.replace(ext, "");
				f.setLinktxt(linktxt);

				defaultValue(f);
				tw.com.mitac.thp.util.Util.defaultPK(f);
				dataSet.add(f);

			}
		}

		// MtsAdsC-SaveActionStart
		boolean isBannerSuccess = bannerImgExecute(PATH, getDetailInfoMap().get("2"));
		if (!isBannerSuccess)
			return false;
		// MtsAdsC-SaveActionEnd

		return super.executeSave();
	}

	@Override
	public String delete() {
		String deletePk = bean.getSysid();
		if (StringUtils.isNotBlank(deletePk)) {
			int c = cloudDao.queryTableCount(sf(), MtsRecommandItem.class, new QueryGroup(new QueryRule("sourceSysid",
					deletePk)));
			if (c > 0) {
				addActionError(getText("delete.error.recommand"));
				return EDIT_ERROR;
			}
			saveList.add(new DeleteStatement(MtsMenuLink.class.getSimpleName(), new QueryGroup(new QueryRule(FK,
					deletePk))));
		}
		return super.delete();
	}

	public String picDelete() {
		String msg = SUCCESS;

		JSONObject jo = new JSONObject();
		String deletesysid = request.getParameter("sysid");
		String deletename = request.getParameter("name");
		String deleteddsysid = request.getParameter("dsysid");
		logger.debug(deletesysid);
		// 刪除
		if (StringUtils.isNotBlank(deletesysid)) {
			String deleteFilePath = PATH + deleteddsysid + "/" + deletename;
			logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
			File deleteLocation = new File(deleteFilePath);

			List<Object> deleteDetailList = new ArrayList<Object>();
			deleteDetailList.add(new DeleteStatement(MtsInfoLink.class.getSimpleName(), new QueryGroup(new QueryRule(
					PK, EQ, deletesysid))));
			msg = cloudDao.save(sf(), deleteDetailList.toArray(), false, null);

			deleteLocation.delete();

			Set<MtsInfoLink> dataSet = (Set<MtsInfoLink>) findDetailSetWhenEdit(DETAIL_SET);
			MtsInfoLink z = null;
			for (MtsInfoLink object : dataSet) {
				logger.debug(object.getSysid());
				if (object.getSysid().equals(deletesysid)) {
					z = object;
					break;
				}
			}
			if (z != null) {
				boolean aa = dataSet.remove(z);

				logger.debug(aa);

			}
		}
		resultString = msg;
		return JSON_RESULT;

	}

	// MtsAdsC-bannerDeleteStart
	public final String bannerDelete() {
		return bannerDelete(PATH);
	}

	// MtsAdsC-bannerDeleteEnd

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getVendorSysid())) {
			CpsMember user = getUserAccount();
			if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid(user.getSourceSysid());
			}
		}
		String result = super.edit();
		// 20160715修正模仿BhsMenu版本-start
		menuSel = getMtsMenuSel();
		// 20160715修正模仿BhsMenu版本-end
		return result;
	}

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

	/*
	 * / 尾檔完成（替代刪除事件 public String ajaxLoadComplete() { String msg = SUCCESS;
	 * try { String parentSysid = request.getParameter("parentSysid"); if
	 * (StringUtils.isBlank(parentSysid)) { resultString = "參數錯誤"; return
	 * JSON_RESULT; } int total = cloudDao.queryCount(sf(), MtsAdsC.class, new
	 * QueryGroup(new QueryRule(FK, parentSysid)));
	 * 
	 * // remap order Set<MtsAdsC> detailSet2 = (Set<MtsAdsC>)
	 * sessionGet(DETAIL_SET2); if (detailSet2 == null) detailSet2 = new
	 * LinkedHashSet<MtsAdsC>(); if (detailSet2.size() != total) { List<MtsAdsC>
	 * list = new ArrayList<MtsAdsC>(detailSet2); Collections.sort(list, new
	 * Comparator<MtsAdsC>() { public int compare(MtsAdsC ent1, MtsAdsC ent2) {
	 * return (ent1.getDataOrder().compareTo(ent2.getDataOrder())); } }); int
	 * order = 1; for (MtsAdsC detail : list) { detail.setDataOrder(order);
	 * order++; } detailSet2 = new LinkedHashSet<MtsAdsC>(list);
	 * sessionSet(DETAIL_SET, detailSet2);
	 * 
	 * MtsHighlight newBean = (MtsHighlight) cloudDao.get(sf(),
	 * getPersistentClass(), parentSysid); PropertyUtils.setProperty(newBean,
	 * DETAIL_SET2, detailSet2); List<Object> saveDetailList = new
	 * ArrayList<Object>(); saveDetailList.add(newBean); msg =
	 * cloudDao.save(sf(), saveDetailList.toArray(), false, null); if
	 * (!SUCCESS.equals(msg)) { resultString = msg; return JSON_RESULT; } else {
	 * List<Object> deleteDetailList = new ArrayList<Object>();
	 * deleteDetailList.add(new DeleteStatement(MtsAdsC.class.getSimpleName(),
	 * new QueryGroup( new QueryRule(FK, NU, "")))); msg = cloudDao.save(sf(),
	 * deleteDetailList.toArray(), false, null); if (!SUCCESS.equals(msg)) {
	 * resultString = msg; return JSON_RESULT; } else { msg = "reloadGrid"; } }
	 * } } catch (Exception e) { e.printStackTrace(); resultString = "尾檔排序發生錯誤";
	 * return JSON_RESULT; } resultString = msg; return JSON_RESULT; }
	 * 
	 * // 尾檔新增or更新 public String ajaxDetailSave() { String msg = SUCCESS; try {
	 * String sysid = request.getParameter("sysid"); String newIds =
	 * request.getParameter("dataOrder"); String parentSysid =
	 * request.getParameter("parentSysid"); // String sourceSysid =
	 * request.getParameter("sourceSysid"); // String remark =
	 * request.getParameter("remark"); if (sysid == null ||
	 * StringUtils.isBlank(newIds) || StringUtils.isBlank(parentSysid)) {
	 * resultString = "尾檔排序參數錯誤"; return JSON_RESULT; } int newOrder =
	 * Integer.parseInt(newIds);
	 * 
	 * Boolean isNew = true; MtsAdsC detailBean; if
	 * (StringUtils.isNotBlank(sysid)) { detailBean = (MtsAdsC)
	 * cloudDao.get(sf(), MtsAdsC.class, sysid); isNew = false; } else {
	 * detailBean = new MtsAdsC(); Util.defaultPK(detailBean);
	 * detailBean.setParentSysid(parentSysid);
	 * detailBean.setDataOrder(newOrder); //
	 * detailBean.setSourceSysid(sourceSysid); // detailBean.setRemark(remark);
	 * }
	 * 
	 * int total = cloudDao.queryCount(sf(), MtsAdsC.class, new QueryGroup(new
	 * QueryRule(FK, parentSysid))); if (isNew) { if (newOrder < (total + 1)) {
	 * // upper msg = shiftDetailOrder(parentSysid, newOrder, total, 1); } else
	 * if (newOrder > (total + 1)) { detailBean.setDataOrder(total + 1); } }
	 * else { int oldOrder = detailBean.getDataOrder();
	 * detailBean.setDataOrder(newOrder); if (newOrder < oldOrder) { // upper
	 * msg = shiftDetailOrder(parentSysid, newOrder, oldOrder - 1, 1); } else if
	 * (newOrder > oldOrder) { // downer msg = shiftDetailOrder(parentSysid,
	 * oldOrder + 1, newOrder, -1); if (newOrder > total) {
	 * detailBean.setDataOrder(total); } } }
	 * 
	 * defaultValue(detailBean); List<Object> saveDetailList = new
	 * ArrayList<Object>(); saveDetailList.add(detailBean); msg =
	 * cloudDao.save(sf(), saveDetailList.toArray(), false, null); if
	 * (!SUCCESS.equals(msg)) { resultString = msg; return JSON_RESULT; }
	 * 
	 * MtsHighlight newBean2 = (MtsHighlight) cloudDao.get(sf(),
	 * getPersistentClass(), parentSysid); sessionSet(DETAIL_SET2,
	 * PropertyUtils.getProperty(newBean2, DETAIL_SET2)); } catch (Exception e)
	 * { e.printStackTrace(); resultString = "尾檔排序發生錯誤"; return JSON_RESULT; }
	 * resultString = msg; return JSON_RESULT; }
	 * 
	 * // 尾檔拖拉排序 public String ajaxDetailSoab() { String msg = SUCCESS; try {
	 * String sysid = request.getParameter("sysid"); String newIds =
	 * request.getParameter("newIds"); String parentSysid =
	 * request.getParameter("parentSysid"); if (StringUtils.isBlank(sysid) ||
	 * StringUtils.isBlank(newIds) || StringUtils.isBlank(parentSysid)) {
	 * resultString = "尾檔排序參數錯誤"; return JSON_RESULT; } int newOrder =
	 * Integer.parseInt(newIds);
	 * 
	 * MtsAdsC deatilbean = (MtsAdsC) cloudDao.get(sf(), MtsAdsC.class, sysid);
	 * int oldOrder = (int) PropertyUtils.getProperty(deatilbean, DATA_ORDER);
	 * if (newOrder < oldOrder) { // upper msg = shiftDetailOrder(parentSysid,
	 * newOrder, oldOrder - 1, 1); } else if (newOrder > oldOrder) { // downer
	 * msg = shiftDetailOrder(parentSysid, oldOrder + 1, newOrder, -1); } if
	 * (!SUCCESS.equals(msg)) { resultString = msg; return JSON_RESULT; }
	 * 
	 * PropertyUtils.setProperty(deatilbean, DATA_ORDER, newOrder);
	 * defaultValue(deatilbean); List<Object> saveDetailList = new
	 * ArrayList<Object>(); saveDetailList.add(deatilbean); msg =
	 * cloudDao.save(sf(), saveDetailList.toArray(), false, "UPDATE"); if
	 * (!SUCCESS.equals(msg)) { resultString = msg; return JSON_RESULT; }
	 * 
	 * MtsHighlight newBean2 = (MtsHighlight) cloudDao.get(sf(),
	 * getPersistentClass(), parentSysid); sessionSet(DETAIL_SET2,
	 * PropertyUtils.getProperty(newBean2, DETAIL_SET2)); } catch (Exception e)
	 * { e.printStackTrace(); resultString = "尾檔排序發生錯誤"; return JSON_RESULT; }
	 * resultString = msg; return JSON_RESULT; }
	 * 
	 * protected String shiftDetailOrder(String parentSysid, int startIdx, int
	 * endIdx, int shiftNum) { try { // HQL Session session =
	 * sf().openSession(); Transaction tx = session.beginTransaction(); String
	 * tableName = MtsAdsC.class.getSimpleName(); String columnName =
	 * "dataOrder"; String qStr = "UPDATE " + tableName + " "; qStr += "SET " +
	 * columnName + " = " + columnName + " "; if (shiftNum >= 0) qStr += "+ ";
	 * qStr += shiftNum + " "; qStr += "WHERE " + columnName + " >= " + startIdx
	 * + " "; qStr += "AND " + columnName + " <= " + endIdx + " "; qStr +=
	 * "AND " + FK + " = '" + parentSysid + "' "; Query query =
	 * session.createQuery(qStr); query.executeUpdate(); tx.commit();
	 * session.close(); } catch (Exception e) { e.printStackTrace(); return
	 * "排序發生錯誤"; } return SUCCESS; }
	 */

}