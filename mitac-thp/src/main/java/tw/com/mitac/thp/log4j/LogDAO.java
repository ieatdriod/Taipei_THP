package tw.com.mitac.thp.log4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * <pre>
 * 查詢產生的日誌檔
 * </pre>
 */
public class LogDAO {
	private Logger logger = Logger.getLogger(this.getClass());
	private String logFolderPath;
	private static DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

	public void setLogFolderPath(String logFolderPath) {
		this.logFolderPath = logFolderPath;
	}

	public LogDAO(String logFolderPath) {
		this.logFolderPath = logFolderPath;
	}

	private static String[] debugLevelArr = new String[] { "DEBUG", "INFO ", "WARN ", "ERROR", "FATAL" };
	private static String[] infoLevelArr = new String[] { "INFO ", "WARN ", "ERROR", "FATAL" };
	private static String[] warnLevelArr = new String[] { "WARN ", "ERROR", "FATAL" };
	private static String[] errorLevelArr = new String[] { "ERROR", "FATAL" };
	private static String[] fatalLevelArr = new String[] { "FATAL" };

	public List<LogDataBean> queryLog(String logLevel, String msgContent, String className, String methodName,
			Date logBeginTime, Date logEndTime) {
		// 增加讀取檔案速度
		TimeZone tz = TimeZone.getTimeZone("GMT+8");
		GregorianCalendar cal = new GregorianCalendar(tz);
		GregorianCalendar calB = new GregorianCalendar(tz);
		GregorianCalendar calF = new GregorianCalendar(tz);
		calB.setTime(logBeginTime);
		calB.add(Calendar.DATE, -3);
		calF.setTime(logEndTime);
		calF.add(Calendar.DATE, 3);

		String[] logLevelArr = getLevelArr(logLevel);
		File f = new File(logFolderPath);
		if (!f.isDirectory()) {
			logger.error("error happen:the logFolderPath isn't folder");
			return null;
		}

		InputStreamReader isr = null;
		InputStream ins = null;
		BufferedInputStream bis = null;
		String logLine = null;
		LogDataBean dataBean = null;
		List<LogDataBean> dataBeanList = new ArrayList<LogDataBean>();

		/**
		 * 針對log資料夾下依年份增加額外資料夾
		 */
		List<File> logFileList = new ArrayList<File>();
		for (File file : f.listFiles()) {
			if (file.isFile()) {
				logFileList.add(file);
			} else if (file.isDirectory()) {
				for (File file2 : file.listFiles())
					if (file2.isFile()) {
						logFileList.add(file2);
					}
			}
		}
		for (File file : logFileList) {
			cal.setTime(new Date(file.lastModified()));
			if (cal.after(calB) && cal.before(calF))
				try {

					// Here BufferedInputStream is added for fast reading
					ins = new FileInputStream(file);
					isr = new InputStreamReader(ins);
					BufferedReader br = new BufferedReader(isr);

					while ((logLine = br.readLine()) != null) {
						dataBean = getDataBean(logLine, logLevelArr, msgContent, className, methodName, logBeginTime,
								logEndTime);
						if (dataBean != null) {
							dataBeanList.add(dataBean);
						}
					}

					// dispose all the resources after using them.
					br.close();
					isr.close();
					ins.close();
					System.gc();
				} catch (Exception e) {
					logger.error(e.getClass() + ":" + e.getMessage());
					e.printStackTrace();
				}
		}
		return dataBeanList;
	}

	private String[] getLevelArr(String logLevel) {
		if (logLevel.equalsIgnoreCase("debug")) {
			return debugLevelArr;
		}
		if (logLevel.equalsIgnoreCase("info")) {
			return infoLevelArr;
		}
		if (logLevel.equalsIgnoreCase("warn")) {
			return warnLevelArr;
		}
		if (logLevel.equalsIgnoreCase("error")) {
			return errorLevelArr;
		}
		return fatalLevelArr;
	}

	/**
	 * 取得符合的log資料
	 * 
	 * @param logLine
	 * @param logLevelArr
	 * @param msgContent
	 *            log key word
	 * @param className
	 * @param methodName
	 * @param logBeginTime
	 * @param logEndTime
	 * @return
	 */
	private LogDataBean getDataBean(String logLine, String[] logLevelArr, String msgContent, String className,
			String methodName, Date logBeginTime, Date logEndTime) {
		LogDataBean dataBean = new LogDataBean();
		boolean isFitLogLevel = false;
		boolean isFitMsgContent = false;
		boolean isInLogTime = false;
		boolean isFitClass = false;
		boolean isFitMethod = false;

		try {
			// check fit logLevel
			for (int i = 0; i < logLevelArr.length; i++) {
				if (logLine.indexOf("[" + logLevelArr[i] + "]") >= 0) {
					dataBean.setLogLevel(logLevelArr[i]);
					isFitLogLevel = true;
					break;
				}
			}
			// check msgContent
			if (isFitLogLevel) {
				if (StringUtils.isBlank(msgContent) || logLine.indexOf(msgContent) >= 0) {
					dataBean.setMsgContent(logLine.substring(logLine.indexOf("] - ") + 4));
					isFitMsgContent = true;
				}
			} else {
				return null;
			}
			// check time
			if (isFitMsgContent && logLine.indexOf("[") == 0) {
				String logDateStr = logLine.substring(1, 24);
				Date logDate = sdf.parse(logDateStr);
				if (logDate.after(logBeginTime) && logDate.before(logEndTime)) {
					dataBean.setLogTime(logDateStr);
					isInLogTime = true;
				}
			} else {
				return null;
			}
			// check class
			if (isInLogTime) {
				String classMsg = logLine.substring(47, 73);
				if (classMsg.indexOf(className) >= 0) {
					dataBean.setClassMsg(classMsg.trim());
					isFitClass = true;
				}
			} else {
				return null;
			}
			// check method
			if (isFitClass) {
				String methodMsg = logLine.substring(76, 96);
				if (methodMsg.indexOf(methodName) >= 0) {
					dataBean.setMethodMsg(methodMsg.trim());
					isFitMethod = true;
				}
			} else {
				return null;
			}
			// end
			if (isFitMethod) {
				return dataBean;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// logger.error(e.getClass() + ":" + e.getMessage() + " logLine:" +
			// logLine);
			return null;
		}
	}
}