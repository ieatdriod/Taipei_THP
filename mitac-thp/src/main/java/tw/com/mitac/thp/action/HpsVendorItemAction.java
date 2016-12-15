package tw.com.mitac.thp.action;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreDeliveryWay;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.bean.HpsVendorItem;
import tw.com.mitac.thp.bean.HpsVendorItemDetailType;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings("unchecked")
public class HpsVendorItemAction extends BasisCrudAction<HpsVendorItem> {

	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid()));
		} else
		// if
		// (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
		{
			return QueryGroup.DEFAULT;
		}
	}

	@Override
	public String main() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			addActionMessage("管理者可以查詢所有項目");
		}
		return super.main();
	}

	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		if (StringUtils.isNotBlank(beaninfo.get("vendorItemType"))) {
			List<String> typeList = (List<String>) cloudDao.findProperty(sf(), HpsCoreItemType.class, new QueryGroup(
					OR, new QueryRule[] { new QueryRule(ID, BW, beaninfo.get("vendorItemType")),
							new QueryRule(NAME, CN, beaninfo.get("vendorItemType")) }, null), new QueryOrder[0], false,
					PK);
			if (typeList.size() == 0) {
				rules.add(new QueryRule(PK, "x"));
			} else {
				List<String> detailList = (List<String>) cloudDao.findProperty(sf(), HpsVendorItemDetailType.class,
						new QueryGroup(new QueryRule("itemTypeSysid", IN, typeList)), new QueryOrder[0], false,
						"itemSysid");
				if (detailList.size() == 0) {
					rules.add(new QueryRule(PK, "x"));
				} else {
					rules.add(new QueryRule(PK, IN, detailList));
				}
			}
		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

	/**
	 * [jqgrid]
	 */
	protected Map<String, Map> getJqgridColModelMap() {
		Map<String, Map> jqgridColModelMap = super.getJqgridColModelMap();
		Map<String, Object> colModelMap = new HashMap<String, Object>();
		jqgridColModelMap.put("vendorItemType", colModelMap);
		colModelMap.put("name", "vendorItemType");
		colModelMap.put("index", "vendorItemType");
		colModelMap.put("align", getJqgridTextAlign());
		colModelMap.put("sortable", true); // sql order
		colModelMap.put("editable", true);
		colModelMap.put("label", "店家商品類別");
		colModelMap.put("width", 150);
		return jqgridColModelMap;
	}

	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] arr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<HpsVendorItem> oldResults = (List<HpsVendorItem>) arr[1];
		List<Map> newResults = new ArrayList<Map>();
		for (HpsVendorItem hpsVendorItem : oldResults) {
			Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(hpsVendorItem);
			newResults.add(map);

			List<String> typeSysidList = (List<String>) cloudDao.findProperty(sf(), HpsVendorItemDetailType.class,
					new QueryGroup(new QueryRule("itemSysid", hpsVendorItem.getSysid())), new QueryOrder[0], false,
					"itemTypeSysid");
			if (typeSysidList.size() > 0) {
				String text = "";
				for (String string : typeSysidList) {
					text += "," + createDataDisplay(HpsCoreItemType.class).get(string);
				}
				text = text.substring(1);
				map.put("vendorItemType", text);
			}
		}
		return new Object[] { arr[0], newResults };
	}

	protected final String MAIN_FILE_PATH() {
		return getSettingResource().get("file.hpsVendorItemPic");
	}

	private List<File> itemPictures;
	private List<String> itemPicturesFileName;

	public List<File> getItemPictures() {
		return itemPictures;
	}

	public void setItemPictures(List<File> itemPictures) {
		this.itemPictures = itemPictures;
	}

	public List<String> getItemPicturesFileName() {
		return itemPicturesFileName;
	}

	public void setItemPicturesFileName(List<String> itemPicturesFileName) {
		this.itemPicturesFileName = itemPicturesFileName;
	}

	@Override
	protected boolean executeSave() {
		// 檢查商品類別設定
		if (!SUCCESS.equals(checkItemType(false))) {
			addActionError("商品類別設定錯誤，不可互為上下階關係");
			return false;
		}
		// 檢查 可送達地區/國家 需在 HpsCoreDeliveryWay（運費設定）中是否存在
		QueryGroup queryGroup = new QueryGroup(new QueryRule("area", bean.getDeliveryArea()), new QueryRule(
				"vendorSysid", bean.getVendorSysid()));
		int count = cloudDao.queryTableCount(sf(), HpsCoreDeliveryWay.class, queryGroup);
		if (StringUtils.isNotBlank(bean.getDeliveryArea()) && count == 0) {
			addActionError("可送達地區/國家：需在「運費設定」中設定該區域");
			return false;
		}

		if (itemPictures != null && itemPictures.size() > 0) {
			String subMainFilePath = MAIN_FILE_PATH() + bean.getSysid() + File.separator;
			File dirFile = new File(subMainFilePath);
			if (!dirFile.exists())
				dirFile.mkdirs();// create document
			for (String fileName : itemPicturesFileName)
				if (!FileUtil.validateExtention(pictureExtention, fileName)) {
					addActionError(getText("errMsg.fileFormatWrong", new String[] { FileUtil.getExtention(fileName) }));
					return false;
				}
			for (int fileIndex = 0; fileIndex < itemPicturesFileName.size(); fileIndex++) {
				String finalFileName = itemPicturesFileName.get(fileIndex);
				String saveFilePath = subMainFilePath + finalFileName;
				logger.debug("測試 itemPicture儲存路徑:" + saveFilePath);
				File fileLocation = new File(saveFilePath);
				FileUtil.moveFile(itemPictures.get(fileIndex), fileLocation);
				if (fileIndex == 0 && StringUtils.isBlank(bean.getMainPictureFilePath()))
					bean.setMainPictureFilePath(finalFileName);// 預設主圖
			}
		}

		String assignMainFileName = request.getParameter("mainFileName");
		if (StringUtils.isNotBlank(assignMainFileName))
			bean.setMainPictureFilePath(assignMainFileName);
		String deleteFileName = request.getParameter("deleteFileName");
		if (StringUtils.isNotBlank(deleteFileName)) {
			String deleteFilePath = MAIN_FILE_PATH() + bean.getSysid() + File.separator + deleteFileName;
			logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
			File deleteLocation = new File(deleteFilePath);
			deleteLocation.delete();
			if (deleteFileName.equals(bean.getMainPictureFilePath()))
				bean.setMainPictureFilePath(null);
		}
		String changeFileName = request.getParameter("changeFileName");
		String changeSn = request.getParameter("changeFileTargetSn");
		if (StringUtils.isNotBlank(changeFileName)) {
			String targetFilePath = MAIN_FILE_PATH() + bean.getSysid() + File.separator + changeFileName;
			String newFileName = changeSn + FileUtil.getExtention(changeFileName);
			String newFilePath = MAIN_FILE_PATH() + bean.getSysid() + File.separator + newFileName;
			logger.debug("測試 變更檔案的路徑target:" + targetFilePath);
			logger.debug("測試 變更檔案的路徑new:" + newFilePath);
			File targetLocation = new File(targetFilePath);
			File newLocation = new File(newFilePath);
			FileUtil.moveFile(targetLocation, newLocation);
			if (changeFileName.equals(bean.getMainPictureFilePath()))
				bean.setMainPictureFilePath(newFileName);
		}
		buildPictureBillMap();
		return super.executeSave();
	}

	@Override
	public String edit() {
		buildPictureBillMap();
		return super.edit();
	}

	/** 找所有圖片路徑 */
	protected void buildPictureBillMap() {
		if (StringUtils.isNotBlank(bean.getSysid())) {
			String subMainFilePath = MAIN_FILE_PATH() + bean.getSysid() + File.separator;
			File targetDir = new File(subMainFilePath);
			Map<String, List<String>> fileMap = new LinkedHashMap<String, List<String>>();
			if (targetDir.isDirectory()) {
				String[] fileNames = targetDir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return FileUtil.validateExtention(pictureExtention, name);
					}
				});
				List<String> fileList = null;
				for (int i = 0, key = 0; i < fileNames.length; i++) {
					if (i % 4 == 0) {
						fileList = new ArrayList<String>();
						fileMap.put("" + key, fileList);
						key++;
					}
					fileList.add(fileNames[i]);
				}
			}
			sessionSet("pictureFileMap", fileMap);
		} else {
			sessionSet("pictureFileMap", null);
		}
	}

	// ---------- ---------- ---------- ---------- ----------
	public String saveItemTypeSysid() {
		resultString = SUCCESS;
		try {
			String itemSysid = request.getParameter("itemSysid");
			String itemTypeSysid = request.getParameter("itemTypeSysid");
			QueryGroup queryGroup = new QueryGroup(new QueryRule("itemSysid", itemSysid), new QueryRule(
					"itemTypeSysid", itemTypeSysid));
			int count = cloudDao.queryTableCount(sf(), HpsVendorItemDetailType.class, queryGroup);
			if (count > 0) {
				resultString = "重複的商品類別!!";
			} else if (!SUCCESS.equals(checkItemType(true))) {
				resultString = "商品類別設定錯誤，不可互為上下階關係";
			} else {
				// 更新資料庫
				HpsVendorItemDetailType dt = new HpsVendorItemDetailType();
				Util.defaultPK(dt);
				defaultValue(dt);
				dt.setItemSysid(itemSysid);
				dt.setItemTypeSysid(itemTypeSysid);

				String daoMsg = cloudDao.save(sf(), new Object[] { dt }, true, "INSERT");
				if (!SUCCESS.equals(daoMsg)) {
					resultString = daoMsg;
				}
			}
		} catch (Exception e) {
			resultString = e.getMessage();
		}

		return JSON_RESULT;
	}

	public String delItemTypeSysid() {
		resultString = SUCCESS;
		try {
			String itemSysid = request.getParameter("itemSysid");
			String itemTypeSysid = request.getParameter("itemTypeSysid");
			QueryGroup queryGroup = new QueryGroup(new QueryRule("itemSysid", itemSysid), new QueryRule(
					"itemTypeSysid", itemTypeSysid));
			int count = cloudDao.queryTableCount(sf(), HpsVendorItemDetailType.class, queryGroup);
			if (count == 0) {
				resultString = "查無商品類別!!";
			} else {
				// 更新資料庫
				String daoMsg = cloudDao
						.save(sf(), new Object[] { new DeleteStatement(HpsVendorItemDetailType.class.getSimpleName(),
								queryGroup) }, true, "");
				if (!SUCCESS.equals(daoMsg)) {
					resultString = daoMsg;
				}
			}
		} catch (Exception e) {
			resultString = e.getMessage();
		}

		return JSON_RESULT;
	}

	public String findItemType() {
		resultList = new ArrayList();

		String itemSysid = request.getParameter("itemSysid");
		List<String> itemTypeSysidList = (List<String>) cloudDao.findProperty(sf(), HpsVendorItemDetailType.class,
				new QueryGroup(new QueryRule("itemSysid", itemSysid)), new QueryOrder[0], false, "itemTypeSysid");
		if (itemTypeSysidList.size() > 0)
			resultList = cloudDao.queryTable(sf(), HpsCoreItemType.class, new QueryGroup(new QueryRule(PK, IN,
					itemTypeSysidList)), new QueryOrder[] { new QueryOrder(ID) }, null, null);

		return JSON_RESULT;
	}

	// 檢測商品類別不可互為上下階
	protected Set<String> itemTypeSysidSet;
	protected Set<String> parentItemTypeSysidSet;
	protected Map<String, HpsCoreItemType> itemTypeMap;

	/**
	 * 檢測：商品類別不可互為上下階
	 * 
	 * @param isVendor
	 *            true:廠商 false:平台
	 * @return SUCCESS/ERROR
	 */
	protected String checkItemType(Boolean isVendor) {
		// 比較組
		itemTypeSysidSet = new HashSet<String>();
		if (isVendor) {
			String itemSysid = request.getParameter("itemSysid");
			String itemTypeSysid = request.getParameter("itemTypeSysid");
			itemTypeSysidSet.add(itemTypeSysid);
			List<String> itemTypeSysidList = (List<String>) cloudDao.findProperty(sf(), HpsVendorItemDetailType.class,
					new QueryGroup(new QueryRule("itemSysid", itemSysid)), new QueryOrder[0], false, "itemTypeSysid");
			itemTypeSysidSet.addAll(itemTypeSysidList);
		} else {
			itemTypeSysidSet.add(bean.getItemTypeSysid());
			if (StringUtils.isNotBlank(bean.getItemType2Sysid()))
				itemTypeSysidSet.add(bean.getItemType2Sysid());
			if (StringUtils.isNotBlank(bean.getItemType3Sysid()))
				itemTypeSysidSet.add(bean.getItemType3Sysid());
		}
		// 所有ItemType -> Map
		itemTypeMap = new LinkedHashMap<String, HpsCoreItemType>();
		String queryRule = isVendor ? NE : EQ; // 平台＝＊，廠商＝其他
		List<HpsCoreItemType> itemTypeList = cloudDao.queryTable(sf(), HpsCoreItemType.class, new QueryGroup(
				new QueryRule("vendorSysid", queryRule, "*")), new QueryOrder[] { new QueryOrder(ID) }, null, null);
		for (HpsCoreItemType itemType : itemTypeList)
			itemTypeMap.put(itemType.getSysid(), itemType);

		// 將比較組的sysid往上階搜尋，找是否有其他比較組Sysid，有則代表為上下階關係，返回ERROR
		parentItemTypeSysidSet = new HashSet<String>();
		for (String itemTypeSysid : itemTypeSysidSet) {
			HpsCoreItemType thisItemType = itemTypeMap.get(itemTypeSysid);
			String parentItemTypeSysid = thisItemType.getParentItemTypeSysid();
			// 找上階直到為空
			while (StringUtils.isNotBlank(parentItemTypeSysid)) {
				// 將上階放入Set
				parentItemTypeSysidSet.add(parentItemTypeSysid);
				HpsCoreItemType newItemType = itemTypeMap.get(parentItemTypeSysid);
				parentItemTypeSysid = newItemType.getParentItemTypeSysid();
			}
		}
		// 所有上階
		int allParentSize = parentItemTypeSysidSet.size();
		for (String itemTypeSysid : itemTypeSysidSet) {
			parentItemTypeSysidSet.add(itemTypeSysid);
			allParentSize += 1;
			// 若加入後set長度不變＝已包含該類別＝上下階關係，返回ERROR
			if (allParentSize != parentItemTypeSysidSet.size())
				return ERROR;
		}

		return SUCCESS;
	}

	// 尋找上階：停用（遞回太麻煩了...
	protected String findParentItemType(String thisItemTypeSysid, String parentItemTypeSysid) {
		String result = SUCCESS;
		for (String itemTypeSysid : itemTypeSysidSet) {
			// 其他比較組（不含本身）
			if (!itemTypeSysid.equals(thisItemTypeSysid)) {
				if (itemTypeSysid.equals(parentItemTypeSysid)) {
					// this的上階在比較組中
					return ERROR;
				} else {
					// 不在比較組中，再往上階找直到沒有上階
					HpsCoreItemType thisItemType = itemTypeMap.get(parentItemTypeSysid);
					String parentParentItemTypeSysid = thisItemType.getParentItemTypeSysid();
					if (StringUtils.isNotBlank(parentParentItemTypeSysid)) {
						// 上上階
						result = findParentItemType(thisItemTypeSysid, parentParentItemTypeSysid);
					} else {
						// 已無上階
					}
				}
			}
		}
		return result;
	}

}