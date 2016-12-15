package tw.com.mitac.thp.action;

import java.net.SocketException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.SysJobScheduleSetting;
import tw.com.mitac.thp.job.MtJobModule;
import tw.com.mitac.thp.util.Util;

public class SysJobScheduleSettingAction extends BasisCrudAction<SysJobScheduleSetting> {
	@Override
	public String main() {
		try {
			addActionMessage("CHECK IP:" + Util.allIpList());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return super.main();
	}

	@Override
	public String edit() {
		String pk = editPK();
		String result = super.edit();
		if (StringUtils.isBlank(pk)) {
			// 給預設值
			bean.setGroupName("group1");

			// 預設 trigger0010
			String defaultId = "0010";
			List<String> idList = (List<String>) cloudDao.findProperty(sf(), SysJobScheduleSetting.class,
					new QueryGroup(new QueryRule("fileId", BW, "trigger")), new QueryOrder[] { new QueryOrder(
							"triggerName", DESC) }, false, "triggerName");
			for (String idString : idList) {
				String idStr = idString.substring(7);
				if (!StringUtils.isNumeric(idStr))
					continue;
				try {
					int idInt = Integer.parseInt(idStr);// max
					defaultId = String.valueOf(idInt + 10);
					defaultId = StringUtils.leftPad(defaultId, 4, "0");
					break;
				} catch (NumberFormatException e) {
					logger.debug("[none int]" + idStr);
				}
			}
			bean.setTriggerName("trigger" + defaultId);

			bean.setSecond("0");
			bean.setMinute("*");
			bean.setHour("*");
			bean.setDay("*");
			bean.setMonth("*");
			bean.setWeek("?");
			bean.setYear("*");
		}
		return result;
	}

	@Override
	protected boolean executeSave() {
		int count = cloudDao.queryTableCount(sf(), SysJobScheduleSetting.class,
				new QueryGroup(new QueryRule(PK, NE, bean.getSysid()), new QueryRule("groupName", bean.getGroupName()),
						new QueryRule("jobName", bean.getJobName())));
		if (count > 0) {
			addActionError(getText("bean.groupName") + "-" + getText("bean.jobName") + "重複");
			return false;
		}

		count = cloudDao.queryTableCount(sf(), SysJobScheduleSetting.class,
				new QueryGroup(new QueryRule(PK, NE, bean.getSysid()), new QueryRule("groupName", bean.getGroupName()),
						new QueryRule("triggerName", bean.getTriggerName())));
		if (count > 0) {
			addActionError(getText("bean.groupName") + "-" + getText("bean.triggerName") + "重複");
			return false;
		}

		if (bean.getJobenable()) {
			try {
				Class<?> targetClass = Class.forName(bean.getClassName());
			} catch (ClassNotFoundException e) {
				addActionError("類別不存在");
				bean.setJobenable(false);
			}
		}

		try {
			// InetAddress hostLoc = InetAddress.getLocalHost();
			if (!Util.ipValidate(bean.getStartOnRemoteHost())) {
				// !hostLoc.getHostAddress().equals(bean.getStartOnRemoteHost())){
				addActionError("當操作機器之ＩＰ與設定不符"// +"為："+hostLoc.getHostAddress()
						+ "，目前設定使本排程於本地設定為停用，請您前往您所設定的ＩＰ主機，再次設定此排程，方能啟動此排程之功能。");
				bean.setJobenable(false);
				// return EDIT_ERROR;
			} else if (StringUtils.isNotBlank(bean.getSysid())) {
				SysJobScheduleSetting oldBean = cloudDao.get(sf(), getPersistentClass(), bean.getSysid());
				if (oldBean != null && !Util.ipValidate(oldBean.getStartOnRemoteHost())// !hostLoc.getHostAddress().equals(oldBean.getStartOnRemoteHost())
						&& Util.ipValidate(bean.getStartOnRemoteHost()))// hostLoc.getHostAddress().equals(bean.getStartOnRemoteHost()))
					addActionMessage(// "當操作機器之ＩＰ為："+hostLoc.getHostAddress()+"，"+
					"此操作變更排程後，將使此排程列為本機器之執行排程，" + "但原執行機器:" + oldBean.getStartOnRemoteHost()
							+ "上之此排程並不會停止執行，若欲停止該機器排程，請至該機器同樣進行此操作");
			}

			conbineCronTime();
			startOrStopJob();
			if (hasActionErrors())
				return false;
			// 執行完任務後 如果為一次性任務 將啟用變為false;
			checkIsOneTimeExcute();
		} catch (Exception e) {
			e.printStackTrace();
			addActionError(e.getMessage());
			return false;
		}
		return super.executeSave();
	}

	private void checkIsOneTimeExcute() {
		// 只要是一次性執行的排程，是否啟用必定為false，若故意對ＤＢ設定成兩個都是true，會造成判定錯誤（在下次重啟時會再次被讀進來啟用，失去了“一次性”的原則）
		try {
			boolean isOneTimeExcute = (Boolean) PropertyUtils.getProperty(bean, "isOneTimeExcute");
			boolean jobenable = (Boolean) PropertyUtils.getProperty(bean, "jobenable");
			if (isOneTimeExcute && jobenable) {
				PropertyUtils.setProperty(bean, "jobenable", false);
				addActionMessage("此任務為一次性任務");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 組合真正使用的的時間格式 */
	private void conbineCronTime() {
		String year = bean.getYear();
		String month = bean.getMonth();
		String week = bean.getWeek();
		String day = bean.getDay();
		String hour = bean.getHour();
		String minute = bean.getMinute();
		String second = bean.getSecond();

		// [秒] [分] [小時] [日] [月] [星期] [年]
		String cronTime = second + " ";
		cronTime += minute + " ";
		cronTime += hour + " ";
		cronTime += day + " ";
		cronTime += month + " ";
		cronTime += week + " ";
		if (StringUtils.isNotBlank(year))
			cronTime += year + " ";
		bean.setCronTime(cronTime);
	}

	/**
	 * 開啟關閉排程
	 * 
	 * @throws Exception
	 * 
	 */
	private void startOrStopJob() throws Exception {
		if (bean.getJobenable())
			schedule_start();
		else
			schedule_stop();
	}

	/**
	 * 啟動排程
	 * 
	 * @return
	 * @throws Exception
	 */
	public void schedule_start() throws Exception {
		String cronTime = bean.getCronTime();
		String[] cronStrArr = cronTime.split(" ");

		// 時間錯誤或是沒有設定 回傳失敗
		if (cronStrArr == null || cronStrArr.length == 0) {
			throw new Exception("時間錯誤或是沒有設定 回傳失敗");
		}
		Map<String, Object> resultMap = MtJobModule.jobStart(bean, getTenancyData().getTenancy());
		addActionMessage(resultMap);
	}

	private void addActionMessage(Map<String, Object> resultMap) {
		boolean success = (Boolean) resultMap.get(SUCCESS_INFO);
		if (success)
			addActionMessage((String) resultMap.get(MESSAGE));
	}

	private static String SUCCESS_INFO = "successInfo";
	private static String MESSAGE = "message";

	/**
	 * 停止排程
	 * 
	 * @return
	 * @throws SchedulerException
	 */
	public void schedule_stop() throws SchedulerException {
		Map<String, Object> resultMap = MtJobModule.jobStop(bean, getTenancyData().getTenancy());
		addActionMessage(resultMap);
	}
}