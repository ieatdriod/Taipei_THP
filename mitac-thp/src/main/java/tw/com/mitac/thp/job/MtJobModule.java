package tw.com.mitac.thp.job;

import java.net.UnknownHostException;
import java.util.Map;

import org.quartz.SchedulerException;

import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.thp.bean.SysJobScheduleSetting;

/** 多承租戶版排程控制模組 */
public class MtJobModule {
	public static Map<String, Object> jobStart(SysJobScheduleSetting jobScheduleSetting, MtMultiTenancy tenancy)
			throws ClassNotFoundException, SchedulerException, UnknownHostException {
		String name =
		// tenancy.getTenancyId() + "_" +
		jobScheduleSetting.getJobName();
		// 識別承租戶
		String group = tenancy.getTenancyId() + "_" + jobScheduleSetting.getGroupName();
		String trigger =
		// tenancy.getTenancyId() + "_" +
		jobScheduleSetting.getTriggerName();
		return JobController.jobStart(name, group, jobScheduleSetting.getClassName(), trigger,
				jobScheduleSetting.getCronTime(), jobScheduleSetting.getIsOneTimeExcute(),
				jobScheduleSetting.getStartOnRemoteHost());
	}

	public static Map<String, Object> jobStop(SysJobScheduleSetting jobScheduleSetting, MtMultiTenancy tenancy)
			throws SchedulerException {
		String name =
		// tenancy.getTenancyId() + "_" +
		jobScheduleSetting.getJobName();
		String group = tenancy.getTenancyId() + "_" + jobScheduleSetting.getGroupName();
		return JobController.jobStop(name, group);
	}
}