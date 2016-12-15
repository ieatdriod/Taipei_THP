/**
 * 日報表,每天執行一次，建議於凌晨，將前一天會議的明細表統計後放入前一天的日報表中
 * @author Administrator
 *
 */
package tw.com.mitac.thp.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMeetingStat;
import tw.com.mitac.thp.util.Util;

public class MeetingDaily extends BasisJob {
	protected static int Days = -1; //搜尋日期往回找
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub

		try {
			logger.debug("execute MeetingDaily start..........");
			doJob();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.debug("execute MeetingDaily finish..........");
		}
		
	}
	
	private void doJob() throws Exception {
		//取得前一天的MeetingDetail資料
		Date backDate = DateUtils.addDays(new Date(), Days);
		String backDateStr = DateFormatUtils.format(backDate, "yyyyMMdd");
		StringBuffer sb = new StringBuffer();
		sb.append("select entity_sysid, meeting_type, meeting_date, count(1) as total_meeting, sum(meeting_session) as total_meeting_minutes, sum(attendance_number) as total_participants ")
		  .append("from cps_meeting_report_details ")
		  .append("where MEETING_DATE=").append(backDateStr).append("");
		
		Session session = null;
		try {
			session = sessionFactory.openSession();;
			Query query = session.createSQLQuery(sb.toString());
			List<?> resList = query.list();
			//System.out.println(resList);
			if (resList != null && resList.size() > 0) {
				CpsMeetingStat cms = null;
				for (int i=0; i<resList.size(); i++) {
					cms = new CpsMeetingStat();
					Object[] o = (Object[])resList.get(i);
					
					if (!chkStatExist(o)) {
						Util.defaultPK(cms);
						defaultValue(cms);
						cms.setEntitySysid(o[0].toString());
						cms.setMeetingType(o[1].toString());
						cms.setDateStat((Date)o[2]);						
						cms.setTotalMeeting(Integer.valueOf(o[3].toString()));
						cms.setTotalMeetingMinutes(Integer.valueOf(o[4].toString()));
						cms.setTotalParticipants(Integer.valueOf(o[5].toString()));
						cloudDao.save(sessionFactory, cms);						
					}
				}				
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}
		
	}
	
	private boolean chkStatExist(Object[] o) {
		List<CpsMeetingStat> cmsList = cloudDao.queryTable(sessionFactory, CpsMeetingStat.class
				, new QueryGroup(new QueryRule("dateStat", EQ, new Date())
								,new QueryRule("entitySysid", EQ, o[0])
								,new QueryRule("meetingType", EQ, o[1])
								,new QueryRule("meetingDate", EQ, (Date)o[2]))
				, new QueryOrder[0]
				, null, null);
		
		if (cmsList != null && cmsList.size() > 0) {
			return true;
		}
		return false;
	}
	private Map<String, CpsMeetingStat> getCpsMeetingStatMapForEntity() throws Exception {
		
		Map<String, CpsMeetingStat> cmsMap = null;
		List<CpsEntity> ceList = cloudDao.queryTable(sessionFactory, CpsEntity.class
				, new QueryGroup()
				, new QueryOrder[0]
				, null, null);
		if (ceList != null && ceList.size() > 0) {
			cmsMap = new HashMap<String, CpsMeetingStat>();
			for(CpsEntity ce : ceList) {
				cmsMap.put(ce.getSysid(), new CpsMeetingStat());
			}
		}
		return cmsMap;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ApplicationContext applicationContext;
		applicationContext = new org.springframework.context.support.ClassPathXmlApplicationContext(
		"applicationContext.xml");		
		SessionFactory sessionFactory = applicationContext.getBean(SessionFactory.class);		
		MeetingDaily m = new MeetingDaily();
		m.setSessionFactory(sessionFactory);
		m.doJob();
	}

}
