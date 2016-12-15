package tw.com.mitac.thp.job;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RemoveOldFileTask extends BasisJob {
	protected static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("file.defaultCreate");

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("start " + getClass().getSimpleName());
		Calendar cal = Calendar.getInstance(), calB = Calendar.getInstance();
		calB.add(Calendar.DATE, -1);
		File f = new File(PATH);
		if (f.exists()) {
			if (!f.isDirectory()) {
				logger.error("error happen:the PATH isn't folder");
				return;
			}
			for (File file : f.listFiles()) {
				cal.setTime(new Date(file.lastModified()));
				// logger.debug("lastModified:" + new
				// Date(file.lastModified()));
				if (cal.before(calB)) {
					// logger.debug("delete:" + file);
					file.delete();
				}
			}
		} else {
			logger.warn("PATH not EXISTS.");
		}
		logger.debug("end");
	}
}