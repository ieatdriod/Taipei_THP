package tw.com.mitac.thp.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import tw.com.mitac.tenancy.util.TenancySessionFactoryUtil;

/**
 * 執行系統初始化
 */
public class ApplicationContextListener implements ServletContextListener {
	protected Logger logger = Logger.getLogger(this.getClass());
	protected static final String PROJECT_NAME = Util.globalSetting().getString("app.projectName");
	protected static final boolean isMultiTenancy = new Boolean(Util.globalSetting().getString("app.isMultiTenancy"));

	protected ApplicationContext getApplicationContext(ServletContextEvent event) {
		logger.info("SPRING IOC");
		return WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		System.gc();
		logger.info(PROJECT_NAME + " SEE YOU");
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		event.getServletContext().setAttribute("editFrame", "_self");
		tw.com.mitac.ssh.util.BigDecimalTypeConverter.setPattern("#,##0.########");
		tw.com.mitac.ssh.util.LongTypeConverter.setPattern("#,##0");
		event.getServletContext().removeAttribute("tenancyList");
		List<String> errorMsg = new ArrayList<String>();

		if (isMultiTenancy) {
			logger.debug("MtMultiTenancy start");
			TenancySessionFactoryUtil
					.setMtApplicationContext(new org.springframework.context.support.ClassPathXmlApplicationContext(
							"classpath:" + "MtMultiTenancy.xml"));
			// TenancySessionFactoryUtil
			// .setMtApplicationContext(new
			// org.springframework.context.support.FileSystemXmlApplicationContext(
			// "file:" + path));
			logger.debug("MtMultiTenancy end");
		}
		// ---------- ---------- ---------- ---------- ----------

		if (errorMsg.size() == 0) {
			logger.info("WELCOME TO " + PROJECT_NAME);
		} else {
			logger.info(PROJECT_NAME + " start with error:");
			for (String message : errorMsg)
				logger.error(message);
		}
	}
}