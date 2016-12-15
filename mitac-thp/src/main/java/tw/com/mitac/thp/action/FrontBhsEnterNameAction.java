package tw.com.mitac.thp.action;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.time.DateUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.Util;

public class FrontBhsEnterNameAction extends BasisFrontLoginAction {

	private CpsSiteMember cpsMember;
	private String meetingDate;
	private String meetingTime;
	private String meetingSubject;
	private String cpsMeetingSysid;
	private CpsMeeting cpsMeeting;

	@Override
	public String execute() {
		cpsMeetingSysid = request.getParameter("sysid");
		return SUCCESS;
	}

	public String review() {
		cpsMeetingSysid = request.getParameter("cpsMeetingSysid");
		cpsMember = getUserData2().getAccount();

		List<CpsMeeting> list = cloudDao.queryTable(sf(), CpsMeeting.class, new QueryGroup(new QueryRule(IS_ENABLED,
				true), new QueryRule("sysid", cpsMeetingSysid)), new QueryOrder[] {}, null, null);

		if (list != null && list.size() > 0) {
			cpsMeeting = list.get(0);
			meetingDate = changeDateFormat(cpsMeeting.getMeetingDate());

			meetingTime = cpsMeeting.getMeetingStartTime().toString() + "起 共" + cpsMeeting.getMeetingSession() + "分鐘";
			meetingSubject = cpsMeeting.getMeetingSubject();
		}

		return SUCCESS;
	}

	public String submit() {

		String cpsMeetingSysid = request.getParameter("cpsMeetingSysid").toString();
		String cpsMemberSysid = request.getParameter("cpsMemberSysid").toString();
		String cpsMeetingStartTime = request.getParameter("cpsMeetingStartTime").toString();
		String cpsMeetingSession = request.getParameter("cpsMeetingSession").toString();

		CpsMeetingAttendance cma = new CpsMeetingAttendance();
		try {
			Util.defaultPK(cma);
			defaultValue(cma);

			cma.setParentSysid(cpsMeetingSysid);
			cma.setAttendanceCpsMemberSysid(cpsMemberSysid);

			cma.setAttendanceType("3");
			System.out.println(cma);
			resultString = cloudDao.save(sf(), new Object[] { cma }, false, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return JSON_RESULT;
	}

	public CpsSiteMember getCpsMember() {
		return cpsMember;
	}

	public void setCpsMember(CpsSiteMember cpsMember) {
		this.cpsMember = cpsMember;
	}

	public String getMeetingDate() {
		return meetingDate;
	}

	public void setMeetingDate(String meetingDate) {
		this.meetingDate = meetingDate;
	}

	public String getMeetingTime() {
		return meetingTime;
	}

	public void setMeetingTime(String meetingTime) {
		this.meetingTime = meetingTime;
	}

	public String getMeetingSubject() {
		return meetingSubject;
	}

	public void setMeetingSubject(String meetingSubject) {
		this.meetingSubject = meetingSubject;
	}

	public String getCpsMeetingSysid() {
		return cpsMeetingSysid;
	}

	public void setCpsMeetingSysid(String cpsMeetingSysid) {
		this.cpsMeetingSysid = cpsMeetingSysid;
	}

	public CpsMeeting getCpsMeeting() {
		return cpsMeeting;
	}

	public void setCpsMeeting(CpsMeeting cpsMeeting) {
		this.cpsMeeting = cpsMeeting;
	}

	private Date StringToDate(String d, String format) {
		Date date = null;
		try {
			DateFormat df = new SimpleDateFormat(format);
			date = df.parse(d);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return date;
	}

	private Time StringToTime(String d, String format) {
		Time time = null;
		try {
			DateFormat df = new SimpleDateFormat(format);
			long ms = df.parse(d).getTime();
			time = new Time(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}

	private String changeDateFormat(Date date) {

		String[] week = { "", "(一)", "(二)", "(三)", "(四)", "(五)", "(六)", "(日)" };

		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		String dateStr = df.format(date);

		SimpleDateFormat date2Day = new SimpleDateFormat("u");
		String days = week[Integer.parseInt(date2Day.format(date))];
		return dateStr + days;
	}
}