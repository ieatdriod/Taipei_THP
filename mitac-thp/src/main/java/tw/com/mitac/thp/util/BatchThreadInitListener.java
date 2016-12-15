package tw.com.mitac.thp.util;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.tenancy.bean.MtMultiTenancy;
import tw.com.mitac.tenancy.util.TenancySessionFactoryUtil;
import tw.com.mitac.thp.bean.SysJobScheduleSetting;
import tw.com.mitac.thp.job.JobController;
import tw.com.mitac.thp.job.MtJobModule;
import tw.com.mitac.tool.dao.CloudTableDAO;

/**
 * 執行排程初始化
 */
public class BatchThreadInitListener implements ServletContextListener {
	protected Logger logger = Logger.getLogger(this.getClass());
	protected static final boolean isMultiTenancy = new Boolean(Util.globalSetting().getString("app.isMultiTenancy"));

	protected ApplicationContext getApplicationContext(ServletContextEvent event) {
		logger.info("SPRING IOC");
		return WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		final ApplicationContext applicationContext = getApplicationContext(event);
		final CloudTableDAO cloudDAO = applicationContext.getBean(CloudTableDAO.class);
		if (isMultiTenancy) {
			final List<MtMultiTenancy> tenancyList = buildTenancy();

			try {
				Thread jobThread = new Thread() {
					@Override
					public void run() {
						logger.debug("排程停止段落 起點");

						for (MtMultiTenancy tenancy : tenancyList) {
							final org.hibernate.SessionFactory sessionFactory = TenancySessionFactoryUtil
									.createTenancySessionFactory(tenancy);
							List<SysJobScheduleSetting> jobList = cloudDAO.queryTable(sessionFactory,
									SysJobScheduleSetting.class, new QueryGroup(new QueryRule("jobenable", true),
											new QueryRule("isOneTimeExcute", false)), new QueryOrder[0], null, null);
							for (SysJobScheduleSetting jobBean : jobList)
								try {
									logger.info("--------正在停止排程：" + jobBean.getJobName() + "--------");
									MtJobModule.jobStop(jobBean, tenancy);
								} catch (Exception e) {
									logger.error("排程停止失敗:" + e);
									e.printStackTrace();
								}
						}
						try {
							JobController.sched.shutdown();
						} catch (SchedulerException e) {
							logger.error("排程停止失敗:" + e);
							e.printStackTrace();
						}
						logger.debug("排程停止段落 終點");
						super.run();
					}
				};
				jobThread.run();
			} catch (Exception e) {
				logger.error("排程啟動失敗:" + e);
				e.printStackTrace();
			}
		} else {
			final MtMultiTenancy tenancy = Util.defaultTenancy;
			final org.hibernate.SessionFactory sessionFactory = applicationContext
					.getBean(org.hibernate.SessionFactory.class);
			try {
				Thread jobThread = new Thread() {
					@Override
					public void run() {
						logger.debug("排程停止段落 起點");

						List<SysJobScheduleSetting> jobList = getJobList(cloudDAO, sessionFactory);
						for (SysJobScheduleSetting jobBean : jobList)
							try {
								logger.info("--------正在停止排程：" + jobBean.getJobName() + "--------");
								MtJobModule.jobStop(jobBean, tenancy);
							} catch (Exception e) {
								logger.error("排程停止失敗:" + e);
								e.printStackTrace();
							}
						try {
							JobController.sched.shutdown();
						} catch (SchedulerException e) {
							logger.error("排程停止失敗:" + e);
							e.printStackTrace();
						}
						logger.debug("排程停止段落 終點");
						super.run();
					}
				};
				jobThread.run();
			} catch (Exception e) {
				logger.error("排程啟動失敗:" + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ApplicationContext applicationContext = getApplicationContext(event);
		final CloudTableDAO cloudDAO = applicationContext.getBean(CloudTableDAO.class);
		try {
			JobController.setSpringBeans(applicationContext);
		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}
		if (isMultiTenancy) {
			final List<MtMultiTenancy> tenancyList = buildTenancy();

			try {
				Thread jobThread = new Thread() {
					@Override
					public void run() {
						for (MtMultiTenancy tenancy : tenancyList) {
							final org.hibernate.SessionFactory sessionFactory = TenancySessionFactoryUtil
									.createTenancySessionFactory(tenancy);
							runJob(cloudDAO, sessionFactory, tenancy);
						}
						try {
							JobController.sched.start();
						} catch (SchedulerException e) {
							logger.error("排程啟動失敗:" + e);
							e.printStackTrace();
						}
						super.run();
					}
				};
				jobThread.run();
			} catch (Exception e) {
				logger.error("排程啟動失敗:" + e);
				e.printStackTrace();
			}
		} else {
			final MtMultiTenancy tenancy = Util.defaultTenancy;
			final org.hibernate.SessionFactory sessionFactory = applicationContext
					.getBean(org.hibernate.SessionFactory.class);
			try {
				Thread jobThread = new Thread() {
					@Override
					public void run() {
						runJob(cloudDAO, sessionFactory, tenancy);
						try {
							JobController.sched.start();
						} catch (SchedulerException e) {
							logger.error("排程啟動失敗:" + e);
							e.printStackTrace();
						}
						super.run();
					}
				};
				jobThread.run();
			} catch (Exception e) {
				logger.error("排程啟動失敗:" + e);
				e.printStackTrace();
			}
		}
	}

	private void runJob(CloudTableDAO cloudDAO, SessionFactory sessionFactory, MtMultiTenancy tenancy) {
		List<SysJobScheduleSetting> jobList = getJobList(cloudDAO, sessionFactory);
		for (SysJobScheduleSetting jobBean : jobList)
			try {
				// 第一次啟動時應該沒有任何排程，後期重啟時，ＤＢ可能有舊資料，包含一次性排程，但重啟加入排程時不應該有一次性排程在內，
				// 若欲再次啟動一次性排程，請到排程設定將啟用再次開啟，操作完畢後系統會再次將其設定為關閉
				// （因為一次性排程save後會，啟用停用會被自動鎖定為停用）
				logger.info("--------排程名稱：" + jobBean.getJobName() + "--------");
				MtJobModule.jobStart(jobBean, tenancy);
			} catch (Exception e) {
				logger.error("排程啟動失敗:" + e);
				e.printStackTrace();
			}
	}

	private List<SysJobScheduleSetting> getJobList(CloudTableDAO cloudDAO, SessionFactory sessionFacotry) {
		List<SysJobScheduleSetting> jobList = cloudDAO.query(sessionFacotry, SysJobScheduleSetting.class,
				new QueryGroup(new QueryRule("jobenable", true), new QueryRule("isOneTimeExcute", false)),
				new QueryOrder[] { new QueryOrder("triggerName", "desc") }, null, null);
		return jobList;
	}

	private List<MtMultiTenancy> buildTenancy() {
		List<MtMultiTenancy> tenancyList = TenancySessionFactoryUtil.allTenancy();
		for (MtMultiTenancy tenancy : tenancyList)
			TenancySessionFactoryUtil.createTenancySessionFactory(tenancy);
		return tenancyList;
	}
}