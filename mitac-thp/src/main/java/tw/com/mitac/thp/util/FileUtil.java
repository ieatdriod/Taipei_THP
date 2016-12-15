package tw.com.mitac.thp.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class FileUtil {
	private static Logger logger = Logger.getLogger(FileUtil.class);

	/**
	 * 產生報表
	 * 
	 * @param source
	 *            資料來源
	 * @param parameter
	 *            報表參數
	 * @param report
	 *            報表名稱
	 * @return
	 */
	public static byte[] export(List<?> source, Map<String, Object> parameter, String report) {
		byte[] rawData = null;
		try {
			InputStream jasperFile = createInputStream(report + ".jasper");
			if (parameter == null)
				parameter = new HashMap<String, Object>();
			JRDataSource dataSource = new JREmptyDataSource();
			if (source != null && source.size() > 0)
				dataSource = new JRBeanCollectionDataSource(source);

			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFile, parameter, dataSource);
			rawData = JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (JRException e) {
			logger.error(e);
		}
		return rawData;
	}

	/**
	 * 報表所需串流
	 * 
	 * @param fileName
	 * @return
	 */
	public static InputStream createInputStream(String fileName) throws RuntimeException {
		return Util.class.getResourceAsStream(fileName);
	}

	/**
	 * 存檔動作，將檔案自暫存區取出並寫入存檔區； 第一個參數收檔案來源，第二個參數指定存檔位置
	 */
	private static final int BUFFER_SIZE = 2 * 1024;

	public static void copyRealFile(File src, File dst) {
		try {
			InputStream inputStream = null;
			OutputStream outputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(src), BUFFER_SIZE);
				outputStream = new BufferedOutputStream(new FileOutputStream(dst), BUFFER_SIZE);
				byte[] buffer = new byte[BUFFER_SIZE];
				while (inputStream.read(buffer) > 0) {
					outputStream.write(buffer);
				}
			} finally {
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != outputStream) {
					outputStream.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取得副檔名
	 * 
	 * @param fileName
	 * @return .副檔名
	 */
	public static String getExtention(String fileName) {
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(pos);
	}

	/**
	 * 檢查副檔名是否符合上傳規則，true表示符合，false表示不符合
	 * 
	 * @param extentionList
	 * @param fileName
	 * @return
	 */
	public static boolean validateExtention(List<String> extentionList, String fileName) {
		String fileExtention = getExtention(fileName);
		boolean validatePass = false;
		for (String extention : extentionList)
			if (StringUtils.equalsIgnoreCase(extention, fileExtention))
				validatePass = true;
		return validatePass;
	}

	/**
	 * 檢查檔明是否已存在，若不存在，則回傳原檔名+副檔名，若已存在，則回傳原檔名+(數字)+副檔名
	 * 
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	private static String validateFileExistAndChangeName(String filePath, String fileName) {
		File validateFile = new File(filePath + fileName);
		if (!validateFile.exists()) {
			return fileName;
		} else {
			String extention = getExtention(fileName);
			String mainFileName = fileName.substring(0, fileName.indexOf(extention));
			logger.debug("測試 mainFileName:" + mainFileName);
			for (int counter = 1; counter > 0; counter++) {
				fileName = mainFileName + "(" + counter + ")" + extention;
				validateFile = new File(filePath + fileName);
				if (!validateFile.exists())
					break;
			}
			return fileName;
		}
	}

	/**
	 * 統一控管專案的檔案移動方式
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 */
	public static boolean moveFile(File srcFile, File destFile) {
		// boolean isRenameToSuccess = srcFile.renameTo(destFile);
		boolean isRenameToSuccess = false;
		try {
			FileUtils.moveFile(srcFile, destFile);
			isRenameToSuccess = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!isRenameToSuccess)
			logger.fatal("無法移動檔案:" + srcFile + "->" + destFile);
		return isRenameToSuccess;
	}
}