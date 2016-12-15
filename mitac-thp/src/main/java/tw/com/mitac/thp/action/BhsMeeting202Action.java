package tw.com.mitac.thp.action;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.meeting.CrudZoomMeeting;
import tw.com.mitac.thp.meeting.meeting.ParamMeetingData;
import tw.com.mitac.thp.meeting.meeting.ReturnMeetingData;
import tw.com.mitac.thp.meeting.meeting.ReturnUpdateData;

public class BhsMeeting202Action extends CpsMeetingController {
	private static final String entityId = "bhs";

	private static String entSysid = "";
	private static String entName = "";

	@Override
	protected boolean executeSave() {
		try {
			// Set<CpsMeetingAttendance> set = bean.getDetailSet();
			Set<CpsMeetingAttendance> set = (Set<CpsMeetingAttendance>) findDetailSetWhenEdit("detailSet");
			if (set == null || set.size() == 0) {
				addActionError(getText("error.message1")); // 至少需有一出席人員身份為主席!
			}

			int cnt = 0;
			for (CpsMeetingAttendance cma : set) {
				if (cma.getAttendanceType() != null && "1".equals(cma.getAttendanceType())) {
					cnt++;
				}
			}

			if (cnt == 0 || cnt > 1) {
				addActionError(getText("error.message2"));
			}

			if (!SUCCESS.equals(uploadData(doc, docFileName, "meetingPpt")))
				return false;

			String assignMainFileName = request.getParameter("mainFileName");
			if (StringUtils.isNotBlank(assignMainFileName))
				bean.setMeetingPpt(assignMainFileName);
			String deleteFileName = request.getParameter("deleteFileName");
			if (StringUtils.isNotBlank(deleteFileName)) {
				String deleteFilePath = PATH + bean.getSysid() + "/" + deleteFileName;
				logger.debug("測試 刪除檔案的路徑:" + deleteFilePath);
				File deleteLocation = new File(deleteFilePath);
				deleteLocation.delete();
				if (deleteFileName.equals(bean.getMeetingPpt()))
					bean.setMeetingPpt(null);
			}
			System.out.println("meetingPpt:" + bean.getMeetingPpt());

			// create zoom meeting, for add

			if (null == bean.getCreator() || "".equals(bean.getCreator())) {

				ParamMeetingData pmd = new ParamMeetingData();
				String d = DateFormatUtils.format(bean.getMeetingDate(), "yyyyMMdd") + bean.getMeetingStartTime(); // yyyyMMddHH:mm:ss
				pmd.setStart_time(DateUtils.parseDate(d, "yyyyMMddHH:mm:ss"));

				pmd.setType(2);
				pmd.setDuration(Integer.valueOf(bean.getMeetingSession()));
				pmd.setTopic(bean.getMeetingSubject());
				pmd.setTimezone("Asia/Taipei");
				CpsMeetingCfg cmc = getUsableZoomUserId(bean.getMeetingDate(), bean.getMeetingStartTime(),
						bean.getMeetingSession());
				if (cmc == null) {
					throw new Exception("該時段無會議室可使用!");
				}
				CrudZoomMeeting curdzm = new CrudZoomMeeting(cmc);
				Response<ReturnMeetingData> rzm = curdzm.createMeeting(pmd);

				logger.debug("Zoom Return=" + rzm);

				if (rzm.getCode() == 0) {
					bean.setStartUrl(rzm.getData().getStart_url());
					bean.setVideoConferenceAddress(rzm.getData().getJoin_url());
					bean.setVideoConferenceId(rzm.getData().getHost_id());
					bean.setMeetingRoomId(rzm.getData().getId());
				} else {
					throw new Exception(String.format("Get meeting error:[%d]%s", rzm.getCode(), rzm.getMessage()));
				}
			} else { // udate 視訊會議

				ParamMeetingData pmd = new ParamMeetingData();

				String d = DateFormatUtils.format(bean.getMeetingDate(), "yyyyMMdd") + bean.getMeetingStartTime(); // yyyyMMddHH:mm:ss
				pmd.setStart_time(DateUtils.parseDate(d, "yyyyMMddHH:mm:ss"));

				pmd.setType(2);
				pmd.setDuration(Integer.valueOf(bean.getMeetingSession()));
				pmd.setTopic(bean.getMeetingSubject());
				pmd.setTimezone("Asia/Taipei");

				CpsMeetingCfg cmc = getUsableZoomUserId(bean.getMeetingDate(), bean.getMeetingStartTime(),
						bean.getMeetingSession());
				if (cmc == null) {
					throw new Exception("該時段無會議室可使用!");
				}
				CrudZoomMeeting curdzm = new CrudZoomMeeting(cmc);
				Response<ReturnUpdateData> zr = curdzm.updateMeeting(bean.getMeetingRoomId(), pmd);
				logger.debug("Zoom Return=" + zr);
				if (zr.getCode() != 0) {
					addActionError(String.format("Update meeting error:[%d]%s", zr.getCode(), zr.getMessage()));
				} else {
					if (zr.getData().getError_code() != null) {
						addActionError(String.format("Update meeting error:[%d]%s", zr.getData().getError_code(), zr
								.getData().getError_message()));
					}
				}
			}
			return super.executeSave();
		} catch (Exception e) {
			addActionError(e.getMessage());
			// throw new RuntimeException(e);
			// e.printStackTrace();
		}

		return false;
		// return super.save();
	}

	private void getEntitySysid() {
		System.out.println("execute get EntitySysid.....................................................");
		List<CpsEntity> list = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(
				new QueryRule(IS_ENABLED, true), new QueryRule("dataId", entityId)), new QueryOrder[0], null, null);

		if (list != null && list.size() > 0) {
			entSysid = list.get(0).getSysid();
			entName = list.get(0).getDataId() + "：" + list.get(0).getName();
		}
	}

	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {

		if (entityId == null || "".equals(entSysid)) {
			getEntitySysid();
		}

		queryGroup = new QueryGroup(new QueryRule("entitySysid", entSysid), new QueryRule("meetingType", "1"),
				new QueryRule("creator", getUserID()));

		return super.jqgridList(clazz, queryGroup, orders, from, length);
	}

	@Override
	public String edit() {
		if (entityId == null || "".equals(entSysid)) {
			getEntitySysid();
		}

		String result = super.edit();
		bean.setMeetingType("1");
		bean.setEntitySysid(entSysid);
		beaninfo.put("entitySysidShow", entName);

		return result;
	}

	/**
	 * 取得該時段可用的user_id
	 * 
	 * @param date
	 *            會議日期
	 * @param time
	 *            會議起始時間
	 * @param duration
	 *            會議使用時間
	 */
	public CpsMeetingCfg getUsableZoomUserId(Date date, Time time, String duration) {

		long[] newMeetingRange = getTimeRange(time, duration);

		// 1.取得db中所有該館及共用且人數大於等於開會人數的的視訊帳號
		List<CpsMeetingCfg> accountList = cloudDao.queryTable(sf(), CpsMeetingCfg.class, new QueryGroup(new QueryRule(
				"usageEntitySysid", CN, "bhs")
		// ,new QueryRule("attendance", GE, bean.getAttendanceNumber())
				), new QueryOrder[0], null, null);
		// 2.取得db中當天(會議日期, >=會議時間)中目前有多少會議在
		String dateStr = DateFormatUtils.format(date, "yyyyMMdd");

		List<CpsMeeting> meetingList = cloudDao.queryTable(sf(), CpsMeeting.class, new QueryGroup(new QueryRule(
				"meetingDate", dateStr), new QueryRule(PK, NE, bean.getSysid())) // 排除自己
				, new QueryOrder[0], null, null);

		for (CpsMeetingCfg cmc : accountList) {
			System.out.println(cmc.toString());
		}
		CpsMeetingCfg cmc = getUnusedAccount(accountList, meetingList);
		if (cmc != null) {
			return cmc;
		}
		return getIdleTimeAccount(accountList, meetingList, time, duration);
		// 如果連這個都null, 就是沒有可用的帳號了
	}
}