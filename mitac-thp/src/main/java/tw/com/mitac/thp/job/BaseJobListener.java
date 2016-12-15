package tw.com.mitac.thp.job;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.tenancy.util.TenancySessionFactoryUtil;
import tw.com.mitac.thp.util.Util;

public class BaseJobListener implements JobListener {
	protected Logger logger = Logger.getLogger(this.getClass());
	protected static final boolean isMultiTenancy = new Boolean(Util.globalSetting().getString("app.isMultiTenancy"));

	@Override
	public String getName() {
		// must return a name
		return this.getClass().getSimpleName();
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		org.quartz.JobKey jobKey = context.getJobDetail().getKey();
		String jobName = context.getJobDetail().getKey().toString();
		logger.debug("jobToBeExecuted where isMultiTenancy:" + isMultiTenancy);
		logger.debug("Job : " + jobName + " is going to start...");

		Scheduler scheduler = context.getScheduler();
		try {
			SchedulerContext schedulerContext = scheduler.getContext();
			ApplicationContext springBeans = (ApplicationContext) schedulerContext.get(JobController.SPRING_BEANS);
			PropertyUtils.setProperty(context.getJobInstance(), "applicationContext", springBeans);
			if (isMultiTenancy) {
				String[] arr = jobKey.getGroup().split("_");
				String tenancyId = arr[0];
				logger.info("tenancyId : " + tenancyId);
				Object[] arr1 = TenancySessionFactoryUtil.queryTenancy(tenancyId);
				if (arr1[0] != null) {
					SessionFactory sessionFactory = TenancySessionFactoryUtil
							.createTenancySessionFactory((MtMultiTenancy) arr1[0]);
					PropertyUtils.setProperty(context.getJobInstance(), "sessionFactory", sessionFactory);
				} else {
					logger.warn(arr1[1]);
				}
			} else {
				SessionFactory sessionFactory = springBeans.getBean(SessionFactory.class);
				PropertyUtils.setProperty(context.getJobInstance(), "sessionFactory", sessionFactory);
			}

		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.debug("jobExecutionVetoed");

	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		String jobName = context.getJobDetail().getKey().toString();
		Scheduler scheduler = context.getScheduler();
		try {
			Map<String, Boolean> oneTimeExcuteMap = (Map<String, Boolean>) scheduler.getContext().get(
					JobController.ONE_TIME_EXCUTE_MAP);
			// 一次性任務 刪除排程任務
			if (!CollectionUtils.isEmpty(oneTimeExcuteMap) && oneTimeExcuteMap.get(jobName)) {
				boolean succes_delete = scheduler.deleteJob(context.getJobDetail().getKey());
				logger.debug("一次性任務  " + jobName + " 排程已停止... " + new Date());
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
			logger.debug(jobName + "  排程 jobListener ...error ");
		}
		if (jobException != null && !jobException.getMessage().equals("")) {
			logger.debug("Exception thrown by: " + jobName + " Exception: " + jobException.getMessage());
		}
	}
}