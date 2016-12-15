package tw.com.mitac.thp.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.quartz.Job;
import org.springframework.context.ApplicationContext;

import tw.com.mitac.email.SendMailSetting;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRuleArea;
import tw.com.mitac.thp.bean.CpsConfig;
import tw.com.mitac.thp.util.ProjectArea;
import tw.com.mitac.thp.util.Util;
import tw.com.mitac.tool.dao.CloudTableDAO;
import tw.com.mitac.tool.dao.impl.CloudDAOImpl;

public abstract class BasisJob implements Job, QueryRuleArea, ProjectArea {
	/** yyyy/MM/dd HH:mm:ss SSS */
	protected final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss SSS");
	protected final Date systemDatetime = new Date();
	protected final String systemDatetimeStr = sdf.format(systemDatetime);
	// protected final String reportName = sdfr.format(systemDatetime);
	protected final Date systemDate;
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		systemDate = cal.getTime();
	}

	protected Logger logger = Logger.getLogger(this.getClass());
	protected CloudTableDAO cloudDao = new CloudDAOImpl();
	protected ApplicationContext applicationContext;
	protected SessionFactory sessionFactory;

	public final void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private CpsConfig cpsConfig = null;

	public final CpsConfig getCpsConfig() {
		if (cpsConfig == null) {
			List<CpsConfig> list = cloudDao.queryTable(sessionFactory, CpsConfig.class, QueryGroup.DEFAULT,
					new QueryOrder[] { new QueryOrder(PK) }, 0, 1);
			if (list.size() > 0)
				cpsConfig = list.get(0);
			else
				cpsConfig = new CpsConfig();
		}
		return cpsConfig;
	}

	private SendMailSetting sendMailSetting;

	/**
	 * Mail info
	 */
	public final SendMailSetting getSendMailSetting() {
		if (sendMailSetting == null) {
			sendMailSetting = new SendMailSetting();
			sendMailSetting.setSmtpHostName(getCpsConfig().getSmtpServer());
			sendMailSetting.setSmtpPort(String.valueOf(getCpsConfig().getSmtpPort()));
			sendMailSetting.setMailAccountId(getCpsConfig().getSmtpAuthUsername());
			sendMailSetting.setMailAccountPassword(getCpsConfig().getSmtpAuthPassword());
		}
		return sendMailSetting;
	}

	protected String createOperatorValue() {
		return getClass().getSimpleName();
	}

	/**
	 * 產生dataLog:creationDate,operationDate,creator,operator
	 * 
	 * @param bean
	 */
	protected final void defaultValue(Object bean) {
		try {
			String creator = (String) PropertyUtils.getProperty(bean, CR);
			if (StringUtils.isBlank(creator))
				PropertyUtils.setProperty(bean, CR, createOperatorValue());
			PropertyUtils.setProperty(bean, OP, createOperatorValue());

			if (String.class.equals(Util.timestampClass)) {
				String creationdate = (String) PropertyUtils.getProperty(bean, CD);
				if (StringUtils.isBlank(creationdate))
					PropertyUtils.setProperty(bean, CD, systemDatetimeStr);
				PropertyUtils.setProperty(bean, OD, systemDatetimeStr);
			} else if (Date.class.equals(Util.timestampClass)) {
				Date creationdate = (Date) PropertyUtils.getProperty(bean, CD);
				if (creationdate == null)
					PropertyUtils.setProperty(bean, CD, systemDatetime);
				PropertyUtils.setProperty(bean, OD, systemDatetime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 產生dataLog:operationDate,operator
	 * 
	 * @return setMap
	 */
	protected final Map<String, Object> getUpdatePropertyMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		if (String.class.equals(Util.timestampClass)) {
			map.put(OD, systemDatetimeStr);
		} else if (Date.class.equals(Util.timestampClass)) {
			map.put(OD, systemDatetime);
		}
		map.put(OP, createOperatorValue());
		return map;
	}
}