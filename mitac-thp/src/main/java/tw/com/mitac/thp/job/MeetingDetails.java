/**
 * 明細表,建議每10~20分鐘執行一次，程式往回找兩天前至今尚未結束的會議，
 * 與zoom報表比對後寫入明細表，同時回寫會議已結束狀態至cpsMeeting
 * ps:之後可考慮另外寫一隻排程專門更新會議狀態(目前因已結束的會議zoom還是回傳尚未開始,故暫時以此方式取代)
 *
 * @author Administrator
 *
 */
package tw.com.mitac.thp.job;


import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.bean.CpsMeetingReportDetails;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.report.user.CrudUser;
import tw.com.mitac.thp.meeting.report.user.Meeting;
import tw.com.mitac.thp.meeting.report.user.UserReport;
import tw.com.mitac.thp.util.Util;

public class MeetingDetails extends BasisJob {
	protected static int Days = -2; //搜尋日期往回找 
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		
		try {
			logger.debug("execute MeetingDetails start..........");
			doJob();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.debug("execute MeetingDetails finish..........");
		}
		
	}
	
	private void doJob() throws Exception {

		//1.由cps_meeting回朔到2天前的會議
		Date backDate = DateUtils.addDays(new Date(), Days);
		String backDateStr = DateFormatUtils.format(backDate, "yyyy-MM-dd");
		List<CpsMeeting> cmList = cloudDao.query(sessionFactory, CpsMeeting.class
			, new QueryGroup(new QueryRule("meetingDate", GE, backDate)
							,new QueryRule("meetingStatus", NE, "2"))
			,new QueryOrder[0]
			, null, null);
		
		logger.debug("CpsMeeting list = " + cmList);

		if (cmList == null || cmList.size() == 0) {
			return;
		}
		//2.由zoom讀取2天內所有的帳號的會議report
		Map<String, List<Meeting>> userReportMap = getAllZoomAccountReport(backDateStr, DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
		logger.debug("zoom userReport Map=" + userReportMap);

		if (userReportMap == null || userReportMap.size() == 0) {
			return;
		}

		//3的步驟考慮由4來取代，紀錄details時順便將狀態改為已結束
		//3.由cps_meeting list的會議代碼到zoom讀取該會議的status
		//3-1若cps_meeting status 不等於zoom 的 status, 則將cps_meeting status更新
		
		//4.比對cps_meeting list 與 report，將有資料且不存在於cps_meeting_report_details的資料寫入該表中
		for(CpsMeeting cm : cmList) {
			//1. 取得帳號所屬meeting list
			if (!userReportMap.containsKey(cm.getVideoConferenceId())) {
				continue;
			}
			List<Meeting> meetingList = userReportMap.get(cm.getVideoConferenceId());

			//2-1.有資料就到cps_meeting_report_details找看看有沒有資料, 沒有就insert, 有就丟棄
			if (meetingList != null && meetingList.size() > 0) {
				for(Meeting meeting : meetingList) {
					if (meeting.getNumber().equals(cm.getMeetingRoomId())) {
						if (!queryCpsMeetingReportDetails(cm.getSysid())) { //sysid不存在cps_meeting_report_details, 新增一筆report_details寫入
							CpsMeetingReportDetails cmrd = genCmsMeetingReportDetail(cm, meeting);
							cloudDao.save(sessionFactory, cmrd);
//							cm.setMeetingStatus("2");
							defaultValue(cm);
							cloudDao.save(sessionFactory, cm);
						}
					}
				}
			}
		}
	}
	
	private CpsMeetingReportDetails genCmsMeetingReportDetail(CpsMeeting cm, Meeting meeting) {
		CpsMeetingReportDetails cmrd = new CpsMeetingReportDetails();
		Util.defaultPK(cmrd);
		defaultValue(cmrd);
		
		cmrd.setCpsMeetingSysid(cm.getSysid());
		cmrd.setAttendanceNumber(meeting.getParticipants().size());
		cmrd.setEntitySysid(cm.getEntitySysid());
//		cmrd.setInitiatorCpsMemberSysid(cm.getInitiatorCpsMemberSysid());
		cmrd.setMeetingDate(cm.getMeetingDate());
		cmrd.setMeetingRoomId(cm.getMeetingRoomId());
		cmrd.setMeetingSession(meeting.getDuration());
		cmrd.setMeetingStartTime(new Time(meeting.getStart_time().getTime()));
		cmrd.setMeetingStatus("2");
		cmrd.setMeetingSubject(cm.getMeetingSubject());
		cmrd.setMeetingType(cm.getMeetingType());

		return cmrd;
	}
	
	private boolean queryCpsMeetingReportDetails(String sysid) {
	
		List<CpsMeetingReportDetails> cmrdList = cloudDao.queryTable(sessionFactory, CpsMeetingReportDetails.class
				, new QueryGroup(new QueryRule("cpsMeetingSysid", EQ, sysid))
				,new QueryOrder[0]
				, null, null);
		
		if (cmrdList != null && cmrdList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private Map<String, List<Meeting>> getAllZoomAccountReport(String from, String to) throws Exception {
		
		Map<String, List<Meeting>> rtnMap = null;
		CpsMeetingCfg cmc = getOneZoomAccount();
		CrudUser cu = new CrudUser(cmc);
		Response<UserReport> ur =  cu.getAllUserReport(from, to, 300, 1);
		if (ur.getCode() == 0) {
			if (Integer.parseInt(ur.getData().getTotal_records()) > 0) {
				rtnMap = new HashMap<String, List<Meeting>>();
				List<Meeting> list = ur.getData().getMeetings();
				for(Meeting meeting : list) {
					if (rtnMap.containsKey(meeting.getHost_id())) {
						rtnMap.get(meeting.getHost_id()).add(meeting);
					} else {
						List<Meeting> mList = new ArrayList<Meeting>();
						mList.add(meeting);
						rtnMap.put(meeting.getHost_id(), mList);
					}
				}
			}
		}
		
		return rtnMap;
	}
	
	private CpsMeetingCfg getOneZoomAccount() {
		List<CpsMeetingCfg> cmcList = cloudDao.queryTable(sessionFactory, CpsMeetingCfg.class
				, new QueryGroup()
				,new QueryOrder[0]
				, null, null);
		return cmcList.get(0);
	}
	

	public static void main(String[] args) throws Exception{
		ApplicationContext applicationContext;
//		applicationContext = new org.springframework.context.support.ClassPathXmlApplicationContext(
//				"applicationContext.xml");		
//		SessionFactory sessionFactory = applicationContext.getBean(SessionFactory.class);		
//		MeetingDetails m = new MeetingDetails();
//		m.setSessionFactory(sessionFactory);
//		m.doJob();
	}
}
