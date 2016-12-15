package tw.com.mitac.thp.job;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import tw.com.mitac.thp.util.Util;

/** 排程控制模組 */
public class JobController {
	protected static Logger logger = Logger.getLogger(JobController.class);
	protected static SchedulerFactory sf;
	public static Scheduler sched;
	protected static CronTrigger trigger;
	protected static JobDetail jobDetail;
	public static Map<String, JobKey> jobKeyMap;
	public final static String ONE_TIME_EXCUTE_MAP = "oneTimeExcuteMap";
	public final static String SPRING_BEANS = "springBeans";

	static {
		jobKeyMap = new HashMap<String, JobKey>();
		sf = new StdSchedulerFactory();

		try {
			sched = sf.getScheduler();

			sched.getListenerManager().addJobListener(new BaseJobListener(),
					org.quartz.impl.matchers.EverythingMatcher.allJobs());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public static void setSpringBeans(ApplicationContext springBeans) throws SchedulerException {
		sched.getContext().put(SPRING_BEANS, springBeans);
	}

	/**
	 * 
	 * @param jobName
	 *            工作名稱
	 * @param jobGroup
	 *            排程群組，一個群組可以設定多個工作
	 * @param className
	 *            要執行的class
	 * @param triggerStr
	 * @param cronTime
	 *            時間，"秒 分 時 日 月 周 年"
	 * @param isOneTimeExcute
	 * @param startOnRemoteHost
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SchedulerException
	 * @throws UnknownHostException
	 */
	public static Map<String, Object> jobStart(String jobName, String jobGroup, String className, String triggerStr,
			String cronTime, Boolean isOneTimeExcute, String startOnRemoteHost) throws ClassNotFoundException,
			SchedulerException, UnknownHostException {
		logger.debug("---------開始job start--------");
		// String jobKeyStr = jobName;
		String jobKeyStr = jobGroup + "." + jobName;

		Map<String, Object> resultMap = new HashMap<String, Object>();

		JobKey jobKey = new JobKey(jobName, jobGroup);
		Class c = Class.forName(className);
		jobDetail = JobBuilder.newJob(c).withIdentity(jobName, jobGroup).build();
		getTrigger(jobGroup, triggerStr, cronTime);
		// 將現有job移除
		if (jobKeyMap.containsKey(jobKeyStr))
			sched.deleteJob(jobKeyMap.get(jobKeyStr));

		// 加入前後執行方法
		// sched.getListenerManager().addJobListener(new BaseJobListener(),
		// org.quartz.impl.matchers.KeyMatcher.keyEquals(jobKey));
		// 將是否一次性執行加入 schedule context
		Map<String, Boolean> map = (Map<String, Boolean>) sched.getContext().get(ONE_TIME_EXCUTE_MAP);
		if (CollectionUtils.isEmpty(map))
			map = new LinkedHashMap<String, Boolean>();
		map.put(jobDetail.getKey().toString(), isOneTimeExcute);
		sched.getContext().put(ONE_TIME_EXCUTE_MAP, map);
		// 啟動排程
		// InetAddress hostLoc = InetAddress.getLocalHost();
		String message = jobKeyStr + "須啟動於此ＩＰ位址上：" + startOnRemoteHost;
		// + "，目前ＩＰ位址為：" + hostLoc.getHostAddress();
		if (StringUtils.isNotBlank(startOnRemoteHost) && Util.ipValidate(startOnRemoteHost)) {
			// hostLoc.getHostAddress().equals(jobScheduleSetting.getStartOnRemoteHost())){
			Date dt = sched.scheduleJob(jobDetail, trigger);
			jobKeyMap.put(jobKeyStr, jobDetail.getKey());
			message = jobKeyStr + " 排程於時間：" + dt + " 啟動";// +"於此ＩＰ位址主機："+hostLoc.getHostAddress();
			logger.debug(message);
		}
		resultMap.put("successInfo", true);
		resultMap.put("message", message);
		logger.debug("---------結束job start--------");
		return resultMap;
	}

	private static void getTrigger(String jobGroup, String triggerStr, String CronTime) {
		trigger = (CronTrigger) TriggerBuilder.newTrigger().withIdentity(triggerStr, jobGroup)
				.withSchedule(CronScheduleBuilder.cronSchedule(CronTime)).startNow().build();
	}

	public static Map<String, Object> jobStop(String jobName, String jobGroup) throws SchedulerException {
		logger.debug("---------開始job stop--------");

		// String jobKeyStr = jobName;
		String jobKeyStr = jobGroup + "." + jobName;

		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean succes_delete = false;
		if (jobKeyMap.containsKey(jobKeyStr))
			succes_delete = sched.deleteJob(jobKeyMap.get(jobKeyStr));
		String message = jobKeyStr + " 排程已停止... " + new Date();
		logger.debug(message);
		resultMap.put("successInfo", succes_delete);
		resultMap.put("message", message);
		logger.debug("---------結束job stop--------");
		return resultMap;
	}
}