package tw.com.mitac.thp.action.app;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.FileUtil;

public class VendorUploadAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "vendorUploadInit";
	}

	protected List<File> upload;
	protected List<String> uploadFileName;

	public final List<File> getUpload() {
		return upload;
	}

	public final void setUpload(List<File> upload) {
		this.upload = upload;
	}

	public final List<String> getUploadFileName() {
		return uploadFileName;
	}

	public final void setUploadFileName(List<String> uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	protected String vendorSysid;

	public String getVendorSysid() {
		if (StringUtils.isBlank(vendorSysid)) {
			List<CpsMember> memberList = cloudDao.queryTable(sf(), CpsMember.class, new QueryGroup(new QueryRule(
					"uuid", getUserID())), new QueryOrder[0], null, null);
			CpsMember bean = memberList.get(0);
			if (CpsEntity.class.getSimpleName().equals(bean.getSourceType())) {
				vendorSysid = bean.getSourceSysid();
			} else if (CpsVendor.class.getSimpleName().equals(bean.getSourceType())) {
				vendorSysid = bean.getSourceSysid();
			} else {
				vendorSysid = bean.getSysid();
			}
		}
		return vendorSysid;
	}

	/**
	 * server.xml add <code>
	 * <Context docBase="/opt/app/thp/image/vendorUpload" path="/vu" />
	 * </code>
	 * 
	 * @return
	 */
	protected String getSavePath() {
		return getSettingResource().get("file.vendorUpload") + getVendorSysid() // +File.separator
		;
	}

	public String vendorUploadInit() {
		File floder = new File(getSavePath());
		if (!floder.isDirectory())
			floder.mkdirs();

		File[] pictureExtentionArr = floder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return FileUtil.validateExtention(pictureExtention, fileName);
			}
		});
		List<String> vendorUploadList = new ArrayList<String>();
		for (File file : pictureExtentionArr)
			vendorUploadList.add(file.getName());
		request.setAttribute("vendorUploadList", vendorUploadList);

		return SUCCESS;
	}

	protected List<Object[]> executeVendorUpload() {
		List<Object[]> l = new ArrayList<Object[]>();
		if (upload != null && upload.size() > 0) {
			String subMainFilePath = getSavePath() + File.separator;
			File dirFile = new File(subMainFilePath);
			if (!dirFile.exists())
				dirFile.mkdirs();// create document
			for (int fileIndex = 0; fileIndex < uploadFileName.size(); fileIndex++) {
				String finalFileName = uploadFileName.get(fileIndex);

				if (!FileUtil.validateExtention(pictureExtention, finalFileName)) {
					String errMsg = "[" + finalFileName + "]"
							+ getText("errMsg.fileFormatWrong", new String[] { FileUtil.getExtention(finalFileName) });
					l.add(new Object[] { false, errMsg });
					continue;
				}

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
						finalFileName = _finalFileName + " (" + i + ")" + extention;
						saveFilePath = subMainFilePath + finalFileName;
						fileLocation = new File(saveFilePath);
						if (fileLocation.exists())
							continue;
						break;
					}
				}
				// logger.debug("測試 itemPicture儲存路徑:" + saveFilePath);

				boolean isSuccess = FileUtil.moveFile(upload.get(fileIndex), fileLocation);
				if (!isSuccess) {
					String errMsg = "[" + finalFileName + "]" + "上傳失敗";
					l.add(new Object[] { false, errMsg });
					continue;
				}
				l.add(new Object[] { true, finalFileName });
			}
		}

		return l;
	}

	public String vendorUpload() {
		List<Object[]> l = executeVendorUpload();
		for (int fileIndex = 0; fileIndex < uploadFileName.size(); fileIndex++) {
			boolean isSuccess = (boolean) l.get(fileIndex)[0];
			String msg = (String) l.get(fileIndex)[1];
			if (!isSuccess)
				addActionError(msg);
		}
		return vendorUploadInit();
	}

	public String vendorUploadRemove() {
		final String targetNmae = request.getParameter("fileNmae");

		File floder = new File(getSavePath());
		if (!floder.isDirectory())
			floder.mkdirs();

		File[] targetArr = floder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.equals(targetNmae);
			}
		});
		if (targetArr.length != 1)
			addActionError("查無檔案");
		else
			for (File file : targetArr) {
				boolean isSuccess = file.delete();
				if (!isSuccess)
					addActionError("刪除失敗");
			}

		return vendorUploadInit();
	}

	public String vendorUploadFile() {
		String callback = request.getParameter("CKEditorFuncNum");
		String filePath = "";
		String successMsg = "";

		resultString = "";

		List<Object[]> l = executeVendorUpload();
		for (int fileIndex = 0; fileIndex < uploadFileName.size(); fileIndex++) {
			boolean isSuccess = (boolean) l.get(fileIndex)[0];
			String msg = (String) l.get(fileIndex)[1];
			if (!isSuccess) {
				logger.warn(msg);
				successMsg = msg;
			} else {
				// String filePath = "/vu/" + getVendorSysid() + "/" + msg;
				filePath = "\\/vu\\/" + getVendorSysid() + "\\/" + msg;

				// String successMsg="上載成功";
				// resultString += "<html><body>";//
				// resultString += "<script>";
				resultString += "<script type=\"text/javascript\">" + "\n";
				resultString += "    window.parent.CKEDITOR.tools.callFunction("
				// + "\""
						+ callback
						// + "\""
						+ ",\"" + filePath + "\",\"" + successMsg + "\");" + "\n";
				resultString += "</script>" + "\n";
				// resultString += "</body></html>";//
			}
		}

		// case 1.
		// return JSON_RESULT;

		// case 2.
		// return renderText(resultString);

		// case 3.
		request.setAttribute("callback", callback);
		request.setAttribute("filePath", filePath);
		request.setAttribute("successMsg", successMsg);
		return SUCCESS;
	}

	public String vendorUploadBrowse() {
		String callback = request.getParameter("CKEditorFuncNum");
		request.setAttribute("callback", callback);
		return vendorUploadInit();
	}
}