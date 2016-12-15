package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.ssh.util.BeanComparator;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.thp.log4j.LogDAO;
import tw.com.mitac.thp.log4j.LogDataBean;

public class SysLog4jAction extends BasisCrudAction<LogDataBean> {
	@Override
	public String init() {
		session.remove("logList");
		return super.init();
	}

	@Override
	public String main() {
		String result = super.main();
		if (beaninfo == null)
			beaninfo = new HashMap<String, String>();
		if (StringUtils.isBlank(beaninfo.get("logBeginTime"))) {
			beaninfo.put("logBeginTime", DateTypeConverter.convertToString(systemDate));
		}
		if (StringUtils.isBlank(beaninfo.get("logEndTime"))) {
			beaninfo.put("logEndTime", DateTypeConverter.convertToString(systemDatetime));
		}
		return result;
	}

	@Override
	public String find() {
		String priority = beaninfo.get("logLevel");
		String message = beaninfo.get("msgContent");
		String clazz = beaninfo.get("classMsg");
		String method = beaninfo.get("methodMsg");
		Date logBeginTime = DateTypeConverter.convertFromString(beaninfo.get("logBeginTime"));
		Date logEndTime = DateTypeConverter.convertFromString(beaninfo.get("logEndTime"));

		List<LogDataBean> logList = new LogDAO("/opt/app/thp/log").queryLog(priority, message, clazz, method,
				logBeginTime, logEndTime);
		session.put("logList", logList);
		return super.find();
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		List results = Collections.emptyList();
		List<LogDataBean> logList = (List<LogDataBean>) session.get("logList");
		if (logList == null || logList.size() == 0) {
			return new Object[] { 0, results };
		}

		List<LogDataBean> targetList = new ArrayList<LogDataBean>();
		if (orders != null && orders.length > 0) {
			Comparator<LogDataBean> comparator = new BeanComparator<LogDataBean>(orders);

			TreeSet<LogDataBean> treeSet = new TreeSet<LogDataBean>(comparator);
			treeSet.addAll(logList);
			targetList.addAll(treeSet);
		} else {
			targetList = logList;
		}

		try {
			results = new ArrayList(targetList.subList(from, from + length));
		} catch (IndexOutOfBoundsException e) {
			results = new ArrayList(targetList.subList(from, targetList.size()));
		}
		return new Object[] { logList.size(), results };
	}
}