package tw.com.mitac.thp.action;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.thp.bean.HpsPromoteLimit;
import tw.com.mitac.thp.util.FileUtil;

public class HpsPromoteLimit2Action extends BasisCrudAction<HpsPromoteLimit> {
	protected List<File> img1;
	protected List<String> img1FileName;
	protected List<File> img2;
	protected List<String> img2FileName;

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

	public List<File> getImg2() {
		return img2;
	}

	public void setImg2(List<File> img2) {
		this.img2 = img2;
	}

	public List<String> getImg2FileName() {
		return img2FileName;
	}

	public void setImg2FileName(List<String> img2FileName) {
		this.img2FileName = img2FileName;
	}

	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("hpsPromoteLimit_pic");

	@Override
	protected boolean executeSave() {
		if (!SUCCESS.equals(uploadData(img1, img1FileName, "limitTimeFilePath")))
			return false;
		if (!SUCCESS.equals(uploadData(img2, img2FileName, "limitQuantityFilePath")))
			return false;

		String deleteFileName = request.getParameter("deleteFileName");
		if (StringUtils.isNotBlank(deleteFileName)) {
			String deleteFilePath = PATH + bean.getSysid() + "/" + deleteFileName;
			logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
			File deleteLocation = new File(deleteFilePath);
			deleteLocation.delete();
		}

		return super.executeSave();
	}

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