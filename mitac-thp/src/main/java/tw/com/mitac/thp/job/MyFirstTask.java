package tw.com.mitac.thp.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.thp.bean.SysJobScheduleSetting;

public class MyFirstTask extends BasisJob {
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("start " + getClass().getSimpleName());
		logger.debug("cloudDAO:" + cloudDao);
		logger.debug("sessionFactory:" + sessionFactory);
		int count = cloudDao.queryTableCount(sessionFactory, SysJobScheduleSetting.class, QueryGroup.DEFAULT);
		logger.debug("count:" + count);
		logger.debug("end");
	}
}