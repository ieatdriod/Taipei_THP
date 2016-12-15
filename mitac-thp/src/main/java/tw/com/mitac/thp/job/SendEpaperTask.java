package tw.com.mitac.thp.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.HqlStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.bean.CpsEpaper;
import tw.com.mitac.thp.bean.CpsEpaperDetail;
import tw.com.mitac.thp.bean.CpsSiteMember;

/**
 * <pre>
 * 寄送電子報
 * </pre>
 * 
 * B_HPS_300_303_002
 * 
 * 每天凌晨4點執行
 */
@SuppressWarnings("unchecked")
public class SendEpaperTask extends BasisJob {
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Date newDate = calendar.getTime();
//		DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		String formatDateToString = formatDate.format(newDate);

		/** 檢查未發送 */
		logger.debug("epaper檢查未發送-start");
		List<CpsEpaper> epNormalList = cloudDao.queryTable(sessionFactory, CpsEpaper.class,
				new QueryGroup(new QueryRule("publishDate", LE, newDate),
						new QueryRule("sendStatus", IN, "N,L")),
				new QueryOrder[0], null, null);

		for (CpsEpaper cpsEpaper : epNormalList) {
			logger.debug("SYSID" + cpsEpaper.getSysid());
			if (cpsEpaper.getSelectObjects().equals("A")) {

				// 全部
				logger.debug("epaper檢查未發送-開始寄送全部");
				String rtn = sendAllEmail(cpsEpaper.getSysid());
				logger.debug("傳送狀態" + rtn);
				if (rtn.equals("success")) {
					changeEpaperY(cpsEpaper.getSysid());
				} else {
					changeEpaperL(cpsEpaper.getSysid());
				}

			} else if (cpsEpaper.getSelectObjects().equals("E")) {

				// 特定
				logger.debug("epaper檢查未發送-開始寄送特定");
				String rtn = sendExceptionEmail(cpsEpaper.getSysid());
				logger.debug("傳送狀態" + rtn);
				if (rtn.equals("success")) {
					changeEpaperY(cpsEpaper.getSysid());
				} else {
					changeEpaperL(cpsEpaper.getSysid());
				}

			}

		}
		logger.debug("epaper檢查未發送-end");

	}

	/** 寄送全部前台全部客戶功能 -僅限有啟用+有開通 */
	public String sendAllEmail(String epaperSysid) {

		List<CpsEpaper> epList = cloudDao.queryTable(sessionFactory, CpsEpaper.class,
				new QueryGroup(new QueryRule(PK, EQ, epaperSysid)), new QueryOrder[0], null, null);
		if (epList.size() > 0) {

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add(epList.get(0).getContent());

			List<CpsSiteMember> memList = cloudDao.queryTable(sessionFactory, CpsSiteMember.class,
					new QueryGroup(new QueryRule(IS_ENABLED, true), new QueryRule("isActivate", true)),
					new QueryOrder[0], null, null);
			if (memList.size() > 0) {
				for (CpsSiteMember cpsSiteMember : memList) {
					try {
						new MailThread(
								new MailBean(cpsSiteMember.getEmail(), epList.get(0).getTitle(), contentStringList),
								getSendMailSetting()).start();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "error";
					}
				}
			}

		}

		return "success";
	}

	/** 寄送全部前台特定客戶功能 */
	public String sendExceptionEmail(String epaperSysid) {

		List<CpsEpaper> epList = cloudDao.queryTable(sessionFactory, CpsEpaper.class,
				new QueryGroup(new QueryRule(PK, EQ, epaperSysid)), new QueryOrder[0], null, null);
		if (epList.size() > 0) {

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add(epList.get(0).getContent());

			Set<String> emailAry = new HashSet<>();

			String[] status = epList.get(0).getExceptionObjects().split(",");
			for (int i = 0; i < status.length; i++) {

				if (status[i].equals("B")) {
					List<CpsSiteMember> memList = cloudDao.queryTable(sessionFactory,
							CpsSiteMember.class, new QueryGroup(new QueryRule(IS_ENABLED, true),
									new QueryRule("isActivate", true), new QueryRule("isBhsEpaper", true)),
							new QueryOrder[0], null, null);
					if (memList.size() > 0) {
						for (CpsSiteMember cpsSiteMember : memList) {
							emailAry.add(cpsSiteMember.getEmail());
						}
					}
				}

				if (status[i].equals("M")) {
					List<CpsSiteMember> memList = cloudDao.queryTable(sessionFactory,
							CpsSiteMember.class, new QueryGroup(new QueryRule(IS_ENABLED, true),
									new QueryRule("isActivate", true), new QueryRule("isMtsEpaper", true)),
							new QueryOrder[0], null, null);
					if (memList.size() > 0) {
						for (CpsSiteMember cpsSiteMember : memList) {
							emailAry.add(cpsSiteMember.getEmail());
						}
					}
				}

				if (status[i].equals("D")) {
					List<CpsEpaperDetail> cedList = cloudDao.queryTable(sessionFactory, CpsEpaperDetail.class,
							new QueryGroup(new QueryRule(FK, epaperSysid)), new QueryOrder[0], null, null);
					if (cedList.size() > 0) {
						for (CpsEpaperDetail cpsEpaperDetail : cedList) {
							emailAry.add(cpsEpaperDetail.getEmail());
						}

					}
				}

			}

			if (emailAry.size() > 0) {
				for (String string : emailAry) {

					try {
						new MailThread(new MailBean(string, epList.get(0).getTitle(), contentStringList),
								getSendMailSetting()).start();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "error";
					}
				}

			}

		}

		return "success";
	}

	/** 發送成功處理 */
	public String changeEpaperY(String epaperSysid) {

		Map<String, Object> setMap = getUpdatePropertyMap();
		setMap.put("sendStatus", "Y");
		setMap.put("publishDateActual", systemDatetime);

		HqlStatement hql = new UpdateStatement(CpsEpaper.class.getSimpleName(),
				new QueryGroup(new QueryRule(PK, epaperSysid)), setMap);
		cloudDao.save(sessionFactory, hql);

		return "success";
	}

	/** 失敗發送處理 */
	public String changeEpaperL(String epaperSysid) {

		Map<String, Object> setMap = getUpdatePropertyMap();
		setMap.put("sendStatus", "L");
		setMap.put("publishDateActual", systemDatetime);

		HqlStatement hql = new UpdateStatement(CpsEpaper.class.getSimpleName(),
				new QueryGroup(new QueryRule(PK, epaperSysid)), setMap);
		cloudDao.save(sessionFactory, hql);

		return "success";
	}
}