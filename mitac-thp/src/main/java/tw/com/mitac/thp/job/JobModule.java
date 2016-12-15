package tw.com.mitac.thp.job;

import java.net.UnknownHostException;
import java.util.Map;

import org.quartz.SchedulerException;

import tw.com.mitac.thp.bean.SysJobScheduleSetting;

/**
 * 排程控制模組
 * 
 * @author Shan
 *
 */
public class JobModule {
	public static Map<String, Object> jobStart(SysJobScheduleSetting jobScheduleSetting) throws ClassNotFoundException,
			SchedulerException, UnknownHostException {
		return JobController.jobStart(jobScheduleSetting.getJobName(), jobScheduleSetting.getGroupName(),
				jobScheduleSetting.getClassName(), jobScheduleSetting.getTriggerName(),
				jobScheduleSetting.getCronTime(), jobScheduleSetting.getIsOneTimeExcute(),
				jobScheduleSetting.getStartOnRemoteHost());
	}

	public static Map<String, Object> jobStop(SysJobScheduleSetting jobScheduleSetting) throws SchedulerException {
		return JobController.jobStop(jobScheduleSetting.getJobName(), jobScheduleSetting.getGroupName());
	}
}