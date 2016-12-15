package tw.com.mitac.thp.action;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.HqlStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAppointment;

public class FrontControlOfVideoConferencingAction extends IndexMtsPageSpecialAction {

	/**
	 * 管控客戶信件點連結後管控是否會議開始
	 */
	public String outerVideoConferencing() {

		/** 準備資源 */

		logger.debug("取得網址資源");
		String cpsMeetingSysid = request.getParameter("cpsMeetingSysid");
		logger.debug("會議室" + cpsMeetingSysid);

		String participantsSysid = request.getParameter("participantsSysid");
		logger.debug("與會者或是主席" + participantsSysid);

		String vendorSysid = request.getParameter("vendorSysid");
		logger.debug("廠商" + vendorSysid);

		// 預先準備如果出現錯誤就跳轉的網址
		request.setAttribute("reurl",
				request.getScheme() + "://" + request.getHeader("host") + request.getContextPath());

		List<CpsMeeting> cpsMeetingList = cloudDao.queryTable(sf(), CpsMeeting.class,
				new QueryGroup(new QueryRule(PK, cpsMeetingSysid)), new QueryOrder[0], null, null);

		if (cpsMeetingList.size() > 0) {

			// 準備資源
			Date tDate = null;// 前10分鐘
			Date sDate = null;// 開始時間
			Date eDate = null;// 結束時間
			Date nDate = new Date();// 今天時間
			// 取得時間
			String meetingDate = new SimpleDateFormat("yyyy-MM-dd").format(cpsMeetingList.get(0).getMeetingDate());

			String meetingTime = cpsMeetingList.get(0).getMeetingStartTime().toString();

			String meetingDateAndTime = meetingDate + " " + meetingTime;

			try {
				sDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(meetingDateAndTime);
				logger.debug("開始時間：" + sDate);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sDate);
				calendar.add(Calendar.MINUTE, -10);
				tDate = calendar.getTime();
				logger.debug("開始前10分鐘時間：" + tDate);
				int rTime = Integer.parseInt(cpsMeetingList.get(0).getMeetingSession());
				calendar.add(Calendar.MINUTE, +10);// +回10分鐘
				calendar.add(Calendar.MINUTE, +rTime);
				eDate = calendar.getTime();
				logger.debug("結束時間" + eDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// 判斷時間可不可以進去
			// 回傳值等於0代表兩日曆等價
			// 回傳值小於0代表目前日曆小於指定日曆
			// 回傳值大於0代表目前日曆大於指定日曆

			if (nDate.compareTo(tDate) < 0) {
				// "會議尚未開始
				request.setAttribute("controlOfVideoConferencingMsg", getText("web.meeting.notStarted"));
			} else if (nDate.compareTo(eDate) > 0) {
				// "會議已經結束
				request.setAttribute("controlOfVideoConferencingMsg", getText("web.meeting.isClosed"));
			} else if (nDate.compareTo(tDate) >= 0 && nDate.compareTo(eDate) <= 0) {

				if (StringUtils.isNotBlank(cpsMeetingSysid) && StringUtils.isNotBlank(participantsSysid)
						&& StringUtils.isBlank(vendorSysid)) {

					logger.debug("檢查會議室步驟完程-客戶區");
					logger.debug("會議室" + cpsMeetingSysid);
					logger.debug("與會者或是主席" + participantsSysid);
					logger.debug("廠商" + vendorSysid);

					String userJoinUrl = userJoin(cpsMeetingList.get(0), participantsSysid);
					logger.debug("回傳連結" + userJoinUrl);
					if (!userJoinUrl.equals(SUCCESS)) {
						redirectPage = userJoinUrl;
						return REDIRECT_PAGE;
					}

				} else if (StringUtils.isNotBlank(cpsMeetingSysid) && StringUtils.isNotBlank(vendorSysid)
						&& StringUtils.isBlank(participantsSysid)) {

					logger.debug("檢查會議室步驟完程-廠商區");
					logger.debug("會議室" + cpsMeetingSysid);
					logger.debug("與會者或是主席" + participantsSysid);
					logger.debug("廠商" + vendorSysid);

					String vendorJoinUrl = vendorJoin(cpsMeetingList.get(0), vendorSysid);
					logger.debug("回傳連結" + vendorJoinUrl);

					if (!vendorJoinUrl.equals(SUCCESS)) {
						redirectPage = vendorJoinUrl;
						return REDIRECT_PAGE;
					}
				} else {
					request.setAttribute("controlOfVideoConferencingMsg", "您的連結不正確請聯繫該會議開啟人");
					return SUCCESS;
				}

			}

		} else {
			// 沒有該會議室
			request.setAttribute("controlOfVideoConferencingMsg", getText("web.meeting.notExist"));
			return SUCCESS;
		}

		return SUCCESS;
	}

	/** 與會者與主席進入會議室 */
	protected String userJoin(CpsMeeting cpsMeeting, String participantsSysid) {

		List<CpsMeetingAttendance> cpsMeetingAttendanceList = cloudDao.queryTable(sf(), CpsMeetingAttendance.class,
				new QueryGroup(new QueryRule(FK, cpsMeeting.getSysid()), new QueryRule(PK, participantsSysid)),
				new QueryOrder[0], null, null);

		if (cpsMeetingAttendanceList.size() > 0) {
			CpsMeetingAttendance cpsMeetingAttendance = cpsMeetingAttendanceList.get(0);

			String joinUserType = cpsMeetingAttendance.getAttendanceType();// 加入會議室人員狀態(1:主持人,3:與會者)
			Boolean confirmOpen = cpsMeeting.getMeetingStatus();

			if (joinUserType.equals("1")) {

				logger.debug("主席進入");

				if (!cpsMeeting.getMeetingStatus()) {
					logger.debug("檢查會議室未開啟，修正為開啟");
					Map<String, Object> setMap = getUpdatePropertyMap();
					setMap.put("meetingStatus", true);
					HqlStatement hql = new UpdateStatement(CpsMeeting.class.getSimpleName(),
							new QueryGroup(new QueryRule(PK, cpsMeeting.getSysid())), setMap);
					cloudDao.save(sf(), hql);
				}

				String i = cpsMeeting.getStartUrl();
				return i;

			} else if (joinUserType.equals("3")) {
				logger.debug("與會者進入");

				if (confirmOpen) {

					/** 處理網址沒有編碼問題 */
					String name = cpsMeetingAttendance.getAttendanceParticipantsName();

					try {
						name = java.net.URLEncoder.encode(name, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String i = cpsMeeting.getVideoConferenceAddress() + "?uname=" + name;

					return i;

				} else {

					logger.debug("會議尚未由主席或館主開啟");
					request.setAttribute("controlOfVideoConferencingMsg", "會議尚未由主席或者供應商開啟，請通知會議主席或供應商");

				}

			}
		} else {
			request.setAttribute("controlOfVideoConferencingMsg", getText("web.meeting.notListPersons"));
		}

		return SUCCESS;
	}

	/** 供應商進入會議室 */
	protected String vendorJoin(CpsMeeting cpsMeeting, String vendorSysid) {

		if (cpsMeeting.getSourceId().equals(vendorSysid)) {

			/** 檢查會議室 */
			if (!cpsMeeting.getMeetingStatus()) {
				logger.debug("檢查會議室未開啟，修正為開啟");
				Map<String, Object> setMap = getUpdatePropertyMap();
				setMap.put("meetingStatus", true);
				HqlStatement hql = new UpdateStatement(CpsMeeting.class.getSimpleName(),
						new QueryGroup(new QueryRule(PK, cpsMeeting.getSysid())), setMap);
				cloudDao.save(sf(), hql);
			}

			/** 檢查進入階級 */
			if (vendorSysid.equals("BHS") || vendorSysid.equals("MTS")) {

				logger.debug("館主階級進入");

				/** 處理網址沒有編碼問題 */
				String name = "THP館主";

				try {
					name = java.net.URLEncoder.encode(name, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String i = cpsMeeting.getVideoConferenceAddress() + "?uname=" + name;

				return i;

			} else {

				List<CpsVendor> vendorList = cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);

				if (vendorList.size() > 0) {

					logger.debug("廠商階級進入");

					/** 處理網址沒有編碼問題 */
					String name = "供應商-" + vendorList.get(0).getName();

					try {
						name = java.net.URLEncoder.encode(name, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String i = cpsMeeting.getVideoConferenceAddress() + "?uname=" + name;
					return i;

				} else {

					request.setAttribute("controlOfVideoConferencingMsg", "您不是該會議廠商#2");
				}

			}

		} else {
			logger.debug("不是該會議廠商或館主");
			request.setAttribute("controlOfVideoConferencingMsg", "您不是該會議廠商#1");
		}

		return SUCCESS;
	}

	/**
	 * 預計擴充用，目前暫定跳轉頁面用，回傳SUCCESS就跳轉頁面由前台處理
	 */
	public String removeControlOfVideoConferencingMsg() {

		resultString = SUCCESS;
		return JSON_RESULT;
	}

}