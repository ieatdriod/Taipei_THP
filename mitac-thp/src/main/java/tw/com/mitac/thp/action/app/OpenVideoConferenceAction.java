package tw.com.mitac.thp.action.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsVendor;

public class OpenVideoConferenceAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "app/OpenVideoConference";
	}

	public String init() {

		String user = getUserAccount().getSysid();

		List<CpsMeetingAttendance> cpsMeetingAttendanceList = (List<CpsMeetingAttendance>) session
				.get("cpsMeetingAttendanceList");
		if (cpsMeetingAttendanceList == null) {
			cpsMeetingAttendanceList = cloudDao.queryTable(sf(), CpsMeetingAttendance.class, new QueryGroup(
					new QueryRule("attendanceCpsMemberSysid", user)), new QueryOrder[] { new QueryOrder(PK, DESC) },
					null, null);

			// List<Object> meetdata = new ArrayList<>();

			// for (CpsMeetingAttendance cpsMeetingAttendance :
			// cpsMeetingAttendanceList) {
			//
			// meetdata.add(cpsMeetingAttendance.getAttendanceType());
			// }
			// request.setAttribute("meetdata", meetdata);
		}

		if (cpsMeetingAttendanceList.size() > 0) {
			List<Map> cpsMeetingMapList = new ArrayList<>();

			// for-start
			for (CpsMeetingAttendance cpsMeetingAttendance : cpsMeetingAttendanceList) {
				List<CpsMeeting> cpsMeetingList = cloudDao.queryTable(sf(), CpsMeeting.class, new QueryGroup(
						new QueryRule(PK, cpsMeetingAttendance.getParentSysid())), new QueryOrder[0], null, null);
				addMultiLan(cpsMeetingList, sf(), CpsMeeting.class);
				// 轉換 LIST -> MAP

				if (cpsMeetingList.size() > 0) {
					Map<String, Object> mtsAppointmentMap = tw.com.mitac.ssh.util.Util.formatToMap(cpsMeetingList
							.get(0));

					// 發起人名稱取得
					if (cpsMeetingList.get(0).getSourceId().equals("BHS")) {
						mtsAppointmentMap.put("sponsor", "BHS");
					} else if (cpsMeetingList.get(0).getSourceId().equals("MTS")) {
						mtsAppointmentMap.put("sponsor", "MTS");
					} else if (cpsMeetingList.get(0).getSourceId().equals("ADMIN")) {
						mtsAppointmentMap.put("sponsor", "ADMIN");
					} else {
						List<CpsVendor> cpsVendorList = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(
								new QueryRule(PK, cpsMeetingList.get(0).getSourceId())), new QueryOrder[0], null, null);

						if (cpsVendorList.size() > 0) {
							logger.debug("cpsMemberList.size():" + cpsVendorList.size());
							logger.debug("cpsMemberList.get(0).getMemberName():" + cpsVendorList.get(0).getName());
							mtsAppointmentMap.put("sponsor", cpsVendorList.get(0).getName());
						}
					}

					// 結束時間顯示處理
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					String dateString = sdf.format(cpsMeetingList.get(0).getMeetingStartTime());
					String etime = doMeetingEndTime(dateString, cpsMeetingList.get(0).getMeetingSession());

					// 按鈕顯示處理

					Date nDate = new Date();
					String eDate = cpsMeetingList.get(0).getMeetingDate() + " " + etime + ":00";

					String sDate = cpsMeetingList.get(0).getMeetingDate() + " " + dateString;

					// 啟動時間
					Date sDatef = null;
					try {
						sDatef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// 前10分
					Date sDatef10 = null;
					try {
						sDatef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate);
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(sDatef);
						calendar.add(Calendar.MINUTE, -10);
						sDatef10 = calendar.getTime();
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// 結束時間
					Date eDatef = null;
					try {
						eDatef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(eDate);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("開始時間" + sDatef10);
					System.out.println("開始時間" + sDate);
					System.out.println("結束時間" + eDate);
					System.out.println("現在時間" + nDate);

					// 回傳值等於0代表兩日曆等價
					// 回傳值小於0代表目前日曆小於指定日曆
					// 回傳值大於0代表目前日曆大於指定日曆

					// 按鈕要不要顯示
					if (nDate.compareTo(eDatef) < 0) {
						mtsAppointmentMap.put("isShow", true);
					}

					if (nDate.compareTo(sDatef10) < 0) {
						mtsAppointmentMap.put("startBlocking", true);
					}

					// 放入
					mtsAppointmentMap.put("userName", getUserAccount().getMemberName());
					mtsAppointmentMap.put("etime", etime);
					mtsAppointmentMap.put("attendanceType", cpsMeetingAttendance.getAttendanceType());
					cpsMeetingMapList.add(mtsAppointmentMap);
				}
			}
			// for-end

			request.setAttribute("cpsMeetingActionList", cpsMeetingMapList);

		}

		return SUCCESS;
	}

	public String doMeetingEndTime(String inStartTime, String inMeetingSession) {
		String outEndTime = "";
		String[] hourArr = inStartTime.split(":");
		int hh = Integer.parseInt(hourArr[0]); // 小時
		int mm = Integer.parseInt(hourArr[1]); // 分

		int sum1 = Integer.parseInt(inMeetingSession) / 60; // 除數
		int sum2 = Integer.parseInt(inMeetingSession) % 60; // 餘數
		int tmphh = 0;
		int tmpmm = 0;

		if ((mm + sum2) >= 60) {
			tmphh = (mm + sum2) / 60;
			tmpmm = (mm + sum2) % 60;
		} else {
			tmpmm = mm + sum2;
		}

		outEndTime = String.format("%02d", (hh + sum1 + tmphh)) + ":" + String.format("%02d", tmpmm);

		return outEndTime;
	}
}