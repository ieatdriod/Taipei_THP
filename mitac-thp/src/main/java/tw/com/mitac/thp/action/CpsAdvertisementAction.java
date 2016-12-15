package tw.com.mitac.thp.action;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsAdvertisement;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.util.FileUtil;

public class CpsAdvertisementAction extends BasisCrudAction<CpsAdvertisement> {

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
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSysid())) {
			List<CpsEntity> entitys = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(new QueryRule("dataId",
					"hps")), new QueryOrder[0], null, null);
			if (entitys.size() > 0)
				bean.setEntitySysid(entitys.get(0).getSysid());
		}

		String result = super.edit();
		return result;
	}

	@Override
	protected boolean executeSave() {
		// 檢核? 先依前端需求寫死
		String aid = bean.getDataId();
		if ("1".equals(aid) || "2".equals(aid) || "3".equals(aid) || "4".equals(aid) || "5".equals(aid)
				|| "6".equals(aid)) {
			if ("1".equals(aid)) {
				if (bean.getMaxAmount().compareTo(6L) > 0) {
					addActionError("「數量限制」不可超過6");
					return false;
				}
			} else {
				if (bean.getMaxAmount().compareTo(1L) > 0) {
					addActionError("「數量限制」不可超過1");
					return false;
				}
			}
		} else {
			addActionError("廣告區塊代號應為1~6");
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
			// bean.setFilePath("*");
		}

		return super.executeSave();
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