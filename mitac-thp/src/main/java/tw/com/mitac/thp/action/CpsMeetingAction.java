package tw.com.mitac.thp.action;

import java.io.File;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import sun.misc.Cleaner;
import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.meeting.CrudZoomMeeting;
import tw.com.mitac.thp.meeting.meeting.ParamMeetingData;
import tw.com.mitac.thp.meeting.meeting.ReturnMeetingData;
import tw.com.mitac.thp.meeting.meeting.ReturnUpdateData;

public class CpsMeetingAction extends CpsMeetingController {

	/**
	 * MAIN頁面篩選功能-舊版
	 */
	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			CpsEntity entity = getDataCpsEntityETable().get(getUserAccount().getSourceSysid());
			if (entity.getDataId().equals("mts")) {
				return new QueryGroup(new QueryRule("sourceId", "MTS"));
			} else if (entity.getDataId().equals("bhs")) {
				return new QueryGroup(new QueryRule("sourceId", "BHS"));
			} else if (entity.getDataId().equals("cps")) {
				return new QueryGroup(new QueryRule("sourceId", IN, "ADMIN,BHS,MTS"));
			} else {
				return new QueryGroup(new QueryRule(PK, "x"));
				// return QueryGroup.DEFAULT;
			}
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			CpsMember user = getUserAccount();
			return new QueryGroup(new QueryRule("sourceId", user.getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	/**
	 * MAIN頁面篩選功能-新版(先藏著)
	 */

	// @Override
	// protected QueryGroup getQueryRestrict() {
	// List<QueryGroup> queryGroupList = new ArrayList<>();
	// List<QueryRule> queryRuleList = new ArrayList<>();
	//
	// /** 處理觀看人 */
	// String sourceId = "";
	// if
	// (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
	// {
	// CpsEntity entity =
	// getDataCpsEntityETable().get(getUserAccount().getSourceSysid());
	// if (entity.getDataId().equals("mts")) {
	// sourceId = "MTS";
	// } else if (entity.getDataId().equals("bhs")) {
	// sourceId = "BHS";
	// } else if (entity.getDataId().equals("cps")) {
	// sourceId = "ADMIN";
	// }
	// } else if
	// (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType()))
	// {
	// sourceId = getUserAccount().getSourceSysid();
	// }
	// queryRuleList.add(new QueryRule("sourceId", sourceId));
	// logger.debug("觀看歷史人是:" + sourceId);
	//
	// /** 處理時間計算 */
	// DateFormat formatTime = new SimpleDateFormat("HH:mm");
	// DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
	// DateFormat formatOffsideDate = new SimpleDateFormat("yyyy/MM/dd");
	//
	// Calendar testOffsideCr = Calendar.getInstance();
	// testOffsideCr.setTime(systemDatetime);
	// testOffsideCr.add(Calendar.MINUTE, +15);
	// Date newDateOffside = testOffsideCr.getTime();
	//
	// Date A = null;
	// try {
	// A = formatOffsideDate.parse(formatOffsideDate.format(systemDatetime));
	// } catch (ParseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// Date B = null;
	// try {
	// B = formatOffsideDate.parse(formatOffsideDate.format(newDateOffside));
	// } catch (ParseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// Boolean offside;
	// if (B.compareTo(A) > 0) {
	// offside = true;
	// } else {
	// offside = false;
	// }
	//
	// if (!offside) {
	// logger.debug("沒有超過於24小時");
	// /** 處理時間 +10分 */
	// Calendar calendarT = Calendar.getInstance();
	// calendarT.setTime(systemDatetime);
	// calendarT.add(Calendar.MINUTE, +15);
	// Date newT = calendarT.getTime();
	// String sT = formatTime.format(newT);
	// logger.debug("今天時間:" + sT);
	//
	// /** 處理日期+0 */
	// String sD = formatDate.format(systemDate);
	// logger.debug("今天日期:" + sD);
	//
	// queryGroupList.add(new QueryGroup(OR, new QueryRule[] { new
	// QueryRule("meetingDate", GT, sD) },
	// new QueryGroup[] { new QueryGroup(new QueryRule("meetingDate", EQ, sD),
	// new QueryRule("meetingStartTime", GE, sT)) }));
	//
	// /*
	// * return new QueryGroup(OR, new QueryRule[] {}, new QueryGroup[] {
	// * new QueryGroup(new QueryRule("meetingDate", LT, sD)), new
	// * QueryGroup(new QueryRule("meetingDate", EQ, sD), new
	// * QueryRule("meetingStartTime", LE, sT)) });
	// *
	// * return new QueryGroup(OR, new QueryRule[] { new
	// * QueryRule("meetingDate", LT, sD) }, new QueryGroup[] { new
	// * QueryGroup(new QueryRule("meetingDate", EQ, sD), new
	// * QueryRule("meetingStartTime", LE, sT)) });
	// */
	//
	// } else {
	// logger.debug("超過於24小時");
	// /** 處理時間 +10分 */
	// Calendar calendarT = Calendar.getInstance();
	// calendarT.setTime(systemDatetime);
	// calendarT.add(Calendar.MINUTE, +15);
	// Date newT = calendarT.getTime();
	// String sT = formatTime.format(newT);
	// logger.debug("今天時間:" + sT);
	//
	// /** 處理日期 +1 */
	// Calendar calendarD = Calendar.getInstance();
	// calendarD.setTime(systemDatetime);
	// calendarD.add(Calendar.DATE, +1);
	// Date newD = calendarD.getTime();
	// String sD = formatTime.format(newD);
	// logger.debug("今天時間:" + sD);
	//
	// queryGroupList.add(new QueryGroup(OR, new QueryRule[] { new
	// QueryRule("meetingDate", GT, sD) },
	// new QueryGroup[] { new QueryGroup(new QueryRule("meetingDate", EQ, sD),
	// new QueryRule("meetingStartTime", GE, sT)) }));
	// }
	//
	// return new QueryGroup(AND, queryRuleList.toArray(new QueryRule[0]),
	// queryGroupList.toArray(new QueryGroup[0]));
	//
	// }

	/**
	 * 儲存功能追加
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see tw.com.mitac.thp.action.BasisCrudAction#executeSave()
	 */
	@Override
	protected boolean executeSave() {

		try {

			// 確認至少一人主席
			Set<CpsMeetingAttendance> set = (Set<CpsMeetingAttendance>) findDetailSetWhenEdit("detailSet");
			if (set == null || set.size() == 0) {
				addActionError(getText("error.message1"));
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

			// .doc檔案刪除部分
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

			/**
			 * 新增會議室部分-正式介接網址https://zoomnow.net/API/zntw_api.php 連結經由程式開啟後
			 * A.會議功能主持人能隨意進入會議室，除非剩下1人+在會議室40分鐘 (自動踢除)
			 * B.{後面會議主持人}可以強制剔除{前面會議主持人} C.與會者進入與否可以
			 * Option_jbh(false)-主持人進入會議室才能進入
			 */
			if (StringUtils.isBlank(bean.getCreator())) {

				bean.setMeetingStatus(false);

				ParamMeetingData pmd = new ParamMeetingData();

				// 寫入開始時間({必填}-格式 ISOJavaDate "yyyy-MM-dd'T'HH:mmZ" )
				String d = DateFormatUtils.format(bean.getMeetingDate(), "yyyyMMdd") + bean.getMeetingStartTime(); // yyyyMMddHH:mm:ss
				logger.debug("會議日期設定A" + d);
				pmd.setStart_time(DateUtils.parseDate(d, "yyyyMMddHH:mm:ss"));
				logger.debug("會議日期設定B" + DateUtils.parseDate(d, "yyyyMMddHH:mm:ss"));

				// 寫入會議類型({必填:預設1}-1:即時會議,2:日程會議,3:定期會議)
				pmd.setType(2);
				// 會議時間({非必填}-僅輸入分鐘,僅日程會議使用)
				pmd.setDuration(Integer.valueOf(bean.getMeetingSession()));
				logger.debug("會議時間" + Integer.valueOf(bean.getMeetingSession()));
				// 會議名稱({必填}-最多300字)
				pmd.setTopic(bean.getMeetingSubject());
				// 會議時區({非必填}-僅日程會議使用)
				pmd.setTimezone("Asia/Taipei");
				// 加入會議({非必填 預設:false}-主持人進會議前是否可先加入會議)
				pmd.setOption_jbh(true);
				// 會議類型({非必填}-video:視訊,screen_share:螢幕分享)
				pmd.setOption_start_type("video");
				// 主持人加入會議是否開啟視訊 ({非必填})
				pmd.setOption_host_video(true);
				// 與會者加入會議是否開啟視訊 ({非必填})
				pmd.setOption_participants_video(true);
				// 會議音訊({非必填}-both:皆可,telephony:電話,voip:網路電話)
				pmd.setOption_audio("both");
				// logger.debug("PMD內容:" + pmd);

				// 取得會議室 KEY
				CpsMeetingCfg cmc = getUsableZoomUserId(bean.getMeetingDate(), bean.getMeetingStartTime(),
						bean.getMeetingSession());
				logger.debug("CpsMeetingCfg會議室KEY" + cmc);

				if (cmc == null) {
					throw new Exception("該時段無會議室可使用!");
				}

				// 帶入資訊
				CrudZoomMeeting curdzm = new CrudZoomMeeting(cmc);
				logger.debug("CrudZoomMeeting(cmc):" + curdzm);
				// .createMeeting帶入API_Key api host_id
				Response<ReturnMeetingData> rzm = curdzm.createMeeting(pmd);
				logger.debug("產生會議參數值Zoom Return=" + rzm);

				// 回傳參數成功是0
				if (rzm.getCode() == 0) {
					// 寫入開啟會議
					bean.setStartUrl(rzm.getData().getStart_url());
					// 寫入加入會議
					bean.setVideoConferenceAddress(rzm.getData().getJoin_url());
					// 寫入會議室ID
					bean.setVideoConferenceId(rzm.getData().getHost_id());
					// 會議室ID
					bean.setMeetingRoomId(rzm.getData().getId());
				} else {
					// throw new
					// Exception(String.format("Get meeting
					// error:[%d]%s",rzm.getCode(),
					// rzm.getMessage()));
					throw new Exception(String.format("沒有會議室可以用:" + rzm.getMessage(), rzm.getCode(), rzm.getMessage()));
				}

			} else {

				// udate 視訊會議
				// CpsMeetingCfg cmc =
				getCpsMeetingCfg(bean.getVideoConferenceId());
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
						addActionError(String.format("Update meeting error:[%d]%s", zr.getData().getError_code(),
								zr.getData().getError_message()));
					}
				}
			}
			boolean result = super.executeSave();
			newDataForEmail();
			oldDataForEmail();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			addActionError(e.getMessage());
		}
		return false;

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
		// 1.取得db中所有該館及共用且人數大於等於開會人數的的視訊帳號
		List<CpsMeetingCfg> cfgList = cloudDao.queryTable(sf(), CpsMeetingCfg.class,
				new QueryGroup(new QueryRule("usageEntitySysid", CN, "cps")
				// ,new QueryRule("attendance", GE, bean.getAttendanceNumber())
				), new QueryOrder[0], null, null);
		for (CpsMeetingCfg cmc : cfgList) {
			System.out.println(cmc.toString());
		}
		// 2.取得db中當天(會議日期, >=會議時間)中目前有多少會議在
		List<CpsMeeting> meetingList = cloudDao.queryTable(sf(), CpsMeeting.class,
				new QueryGroup(new QueryRule("meetingDate", date), new QueryRule(PK, NE, bean.getSysid())) // 排除自己
				, new QueryOrder[0], null, null);
		CpsMeetingCfg cmc = getUnusedAccount(cfgList, meetingList);
		if (cmc != null) {
			return cmc;
		}
		return getIdleTimeAccount(cfgList, meetingList, time, duration);
		// 如果連這個都null,就是沒有可用的帳號了
	}

	/**
	 * -------------------------------------------------------------------------
	 */

	@Override
	public String edit() {


		/**
		 * 新建單寫入SourceId
		 */
		if (StringUtils.isBlank(bean.getSourceId())) {
			CpsMember user = getUserAccount();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {

				CpsEntity entity = getDataCpsEntityETable().get(getUserAccount().getSourceSysid());
				if (entity.getDataId().equals("mts")) {
					bean.setSourceId("MTS");
				} else if (entity.getDataId().equals("bhs")) {
					bean.setSourceId("BHS");
				} else if (entity.getDataId().equals("cps")) {
					bean.setSourceId("ADMIN");
				}

			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setSourceId(user.getSourceSysid());
			}
		}

		if (StringUtils.isBlank(bean.getEntitySysid())) {
			CpsMember user = getUserAccount();

			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setEntitySysid(user.getSourceSysid());
			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {

				Map<String, String> cpsvMap = new HashMap<>();
				for (CpsVendor cpsv : getDataCpsVendorTable().values()) {
					cpsvMap.put(cpsv.getSysid(), cpsv.getEntitySysid());
				}
				String id = cpsvMap.get(user.getSourceSysid());
				bean.setEntitySysid(id);
			}
		}

		String result = super.edit();
		return result;
	}

	/** 顯示 刪除尾檔功能按鈕 + 操作按鈕功能(單筆寄信) 用來歷史資訊 隱藏 */
	public Boolean getShowSubtle() {

		return true;
	}

	public String init() {

		return SUCCESS;

	}

	/**
	 * 寫入會議室名單功能
	 */
	public String importOfficer() {
		String msg = "";
		String name = request.getParameter("importOfficerName");
		String email = request.getParameter("importOfficerEmail");
		String type = request.getParameter("importOfficerType");

		if (StringUtils.isBlank(name)) {
			msg = "請輸入姓名";
		} else if (StringUtils.isBlank(email)) {
			msg = "請輸入信箱";
		} else if (!type.equals("1") && !type.equals("3")) {
			msg = "角色不正確";
		}

		if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(email) && StringUtils.isNotBlank(type)) {

			Set<CpsMeetingAttendance> dataSet = (Set<CpsMeetingAttendance>) findDetailSetWhenEdit(DETAIL_SET);
			CpsMeetingAttendance item = getDefaultDMO(CpsMeetingAttendance.class);

			Map<String, String> map = new HashMap<>();

			List<CpsSiteMember> siteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(),
					new QueryOrder[0], null, null);

			if (siteMemberList.size() > 0) {
				for (CpsSiteMember cpsSiteMember : siteMemberList) {
					map.put(cpsSiteMember.getEmail(), cpsSiteMember.getSysid());
				}
			}

			String emailSysid = map.get(email);
			if (StringUtils.isNotBlank(emailSysid)) {
				item.setAttendanceCpsSitememberSysid(emailSysid);
			}

			item.setAttendanceParticipantsName(name);
			item.setAttendanceParticipantsEmail(email);
			item.setAttendanceType(type);
			defaultValue(item);
			tw.com.mitac.thp.util.Util.defaultPK(item);
			dataSet.add(item);


			logger.debug("name:" + name + "-email:" + email + "-type:" + type);
			msg = SUCCESS;
		}

		resultString = msg;
		return JSON_RESULT;

	}

	/**
	 * 會議室新建單自動寄信功能(全部名單寄)
	 */
	protected String newDataForEmail() {
		String newOrOld = request.getParameter("newOrOld");
		logger.debug("newOrOld:" + newOrOld);
		if (newOrOld.equals("NEW")) {

			/** 寄信給主席 */
			emailChairman();

			/** 寄信給與會者 */
			emailAttendees();

			/** 寄信給廠商 */
			emailVendor();

			addActionMessage("貼心小提醒:已將信寄送到客戶填寫的信箱!，告知客戶預約的時間");
			addActionMessage("如果客戶輸入信箱不正確則會收不到，請確認客戶信箱狀態是否正常");
		}

		return SUCCESS;
	}

	/**
	 * 新增預約-寄信給廠商階級
	 */
	public String emailVendor() {
		logger.debug("寄信於廠商");
		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "video_Conferencing_Vendor")),
				new QueryOrder[0], null, null);

		logger.debug("範例信件筆數：" + emailTemplate.size());

		if (emailTemplate.size() > 0) {

			String vendorName = "";
			if (bean.getSourceId().equals("ADMIN")) {
				vendorName = getText("web.node.cps");
			} else if (bean.getSourceId().equals("MTS")) {
				vendorName = getText("web.node.mts");
			} else if (bean.getSourceId().equals("BHS")) {
				vendorName = getText("web.node.bhs");
			} else {
				List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, bean.getSourceId())), new QueryOrder[0], null, null);
				if (vendorList.size() > 0) {
					vendorName = vendorList.get(0).getName();
				}
			}

			String attendance = "";
			List<CpsMeetingAttendance> attendanceList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
					CpsMeetingAttendance.class, new QueryGroup(new QueryRule(FK, bean.getSourceId())),
					new QueryOrder[0], null, null);
			if (attendanceList.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : attendanceList) {
					attendance += "，" + cpsMeetingAttendance.getAttendanceParticipantsName();
				}
				attendance = attendance.substring(1);
			}

			if (bean.getSourceId().equals("MTS") || bean.getSourceId().equals("BHS")
					|| bean.getSourceId().equals("ADMIN")) {

				String entityType = "";
				if (bean.getSourceId().equals("MTS")) {
					entityType = "mts";
				} else if (bean.getSourceId().equals("BHS")) {
					entityType = "bhs";
				} else if (bean.getSourceId().equals("ADMIN")) {
					entityType = "cps";
				}

				List<CpsEntity> entityList = (List<CpsEntity>) cloudDao.queryTable(sf(), CpsEntity.class,
						new QueryGroup(new QueryRule("dataId", entityType)), new QueryOrder[0], null, null);
				if (entityList.size() > 0) {
					/** 寄信給館主 */
					List<CpsMember> memberList = (List<CpsMember>) cloudDao.queryTable(sf(), CpsMember.class,
							new QueryGroup(new QueryRule("sourceSysid", entityList.get(0).getSysid())),
							new QueryOrder[0], null, null);
					logger.debug("使用者數量：" + memberList.size());

					if (memberList.size() > 0) {
						for (CpsMember cpsMember : memberList) {

							CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);
							String cpsMeetingSysid = bean.getSysid();
							String participantsSysid = bean.getSourceId();
							String participantsEmail = cpsMember.getEmail();

							String url = "<a href='" + request.getScheme() + "://" + request.getHeader("host")
									+ request.getContextPath() + "/pages2/ControlOfVideoConferencing?cpsMeetingSysid="
									+ cpsMeetingSysid + "&vendorSysid=" + participantsSysid + "'>點我由此去</a>";

							logger.debug("自動寄信資訊(會議室SYSID)：" + cpsMeetingSysid);
							logger.debug("自動寄信資訊(會議人員SYSID)：" + participantsSysid);
							logger.debug("自動寄信資訊(會議人員EMAIL):" + participantsEmail);
							logger.debug("自動寄信資訊(URL連結)" + url);

							/** 標題部分 */
							String emailTitle = appointmentEmailTemplate.getEmailTitle();
							// 館主名稱
							emailTitle = emailTitle.replace("$VENDOR_NAME$", vendorName);

							/** 內容部分 */
							String emailContent = appointmentEmailTemplate.getEmailContent();
							// 館主名稱
							emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

							// 與會者
							emailContent = emailContent.replace("$CANCLE_PARTICIPANTS$", attendance);

							// 會議室連結
							emailContent = emailContent.replace("$INVITE_LINK$", url);

							// 會議室名稱
							emailContent = emailContent.replace("$CANCLE_NAME$", bean.getMeetingSubject());

							// 會議室開始結束時間
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(bean.getMeetingStartTime());

							// 開始時間+會議時間
							if (bean.getMeetingSession().equals("30")) {
								calendar.add(Calendar.MINUTE, +30);
							} else if (bean.getMeetingSession().equals("60")) {
								calendar.add(Calendar.MINUTE, +60);
							} else if (bean.getMeetingSession().equals("90")) {
								calendar.add(Calendar.MINUTE, +90);
							} else if (bean.getMeetingSession().equals("120")) {
								calendar.add(Calendar.MINUTE, +120);
							}

							Date newEndDate = calendar.getTime();

							// 設定日期格式
							DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
							// 進行轉換
							String formatEndDateToString = formatDate.format(newEndDate);
							logger.debug("結束時間:" + formatEndDateToString);

							SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
							String meetDate = sdFormat.format(bean.getMeetingDate());

							String start = meetDate + "-" + bean.getMeetingStartTime();
							String end = meetDate + "-" + formatEndDateToString;

							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

							// 比較時間用
							Date dateStart = null;
							try {
								dateStart = sdf.parse(start);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							Date dateEnd = null;
							try {
								dateEnd = sdf.parse(end);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							if (dateStart.compareTo(dateEnd) < 0) {
								emailContent = emailContent.replace("$CANCLE_TIME$", start);
								logger.debug("開始時間:" + start);
								emailContent = emailContent.replace("$CANCLE_TIME_END$", end);
								logger.debug("結束時間:" + end);
							} else {
								emailContent = emailContent.replace("$CANCLE_TIME$", start);
								logger.debug("開始時間:" + start);
								// 時間超過24小時處理
								Calendar calendarE = Calendar.getInstance();
								logger.debug("@@@@" + end);
								calendarE.setTime(dateEnd);
								calendarE.add(Calendar.DATE, +1);
								Date endUpdate1 = calendarE.getTime();
								String endUpdate1Sdf = sdf.format(endUpdate1);
								logger.debug("日期超過24小時處理:" + endUpdate1Sdf);
								emailContent = emailContent.replace("$CANCLE_TIME_END$", endUpdate1Sdf);
							}

							List<String> contentStringList = new ArrayList<String>();
							contentStringList.add(emailContent);

							new MailThread(new MailBean(participantsEmail, emailTitle, contentStringList),
									getSendMailSetting()).start();

						}
					}
				}
			} else {
				/** 寄信給供應商 */
				List<CpsMember> memberList = (List<CpsMember>) cloudDao.queryTable(sf(), CpsMember.class,
						new QueryGroup(new QueryRule("sourceSysid", bean.getSourceId())), new QueryOrder[0], null,
						null);
				logger.debug("使用者數量：" + memberList.size());

				if (memberList.size() > 0) {
					for (CpsMember cpsMember : memberList) {

						CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);
						String cpsMeetingSysid = bean.getSysid();
						String participantsSysid = bean.getSourceId();
						String participantsEmail = cpsMember.getEmail();

						String url = "<a href='" + request.getScheme() + "://" + request.getHeader("host")
								+ request.getContextPath() + "/pages2/ControlOfVideoConferencing?cpsMeetingSysid="
								+ cpsMeetingSysid + "&vendorSysid=" + participantsSysid + "'>點我由此去</a>";

						logger.debug("自動寄信資訊(會議室SYSID)：" + cpsMeetingSysid);
						logger.debug("自動寄信資訊(會議人員SYSID)：" + participantsSysid);
						logger.debug("自動寄信資訊(會議人員EMAIL):" + participantsEmail);
						logger.debug("自動寄信資訊(URL連結)" + url);

						/** 標題部分 */
						String emailTitle = appointmentEmailTemplate.getEmailTitle();

						// 廠商名稱
						emailTitle = emailTitle.replace("$VENDOR_NAME$", vendorName);

						/** 內文部分 */
						String emailContent = appointmentEmailTemplate.getEmailContent();
						// 會議室連結
						emailContent = emailContent.replace("$INVITE_LINK$", url);

						// 供應商名稱
						emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

						// 與會者
						emailContent = emailContent.replace("$CANCLE_PARTICIPANTS$", attendance);

						// 會議室名稱
						emailContent = emailContent.replace("$CANCLE_NAME$", bean.getMeetingSubject());

						// 會議室開始結束時間
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(bean.getMeetingStartTime());

						// 開始時間+會議時間
						if (bean.getMeetingSession().equals("30")) {
							calendar.add(Calendar.MINUTE, +30);
						} else if (bean.getMeetingSession().equals("60")) {
							calendar.add(Calendar.MINUTE, +60);
						} else if (bean.getMeetingSession().equals("90")) {
							calendar.add(Calendar.MINUTE, +90);
						} else if (bean.getMeetingSession().equals("120")) {
							calendar.add(Calendar.MINUTE, +120);
						}

						Date newEndDate = calendar.getTime();

						// 設定日期格式
						DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
						// 進行轉換
						String formatEndDateToString = formatDate.format(newEndDate);
						logger.debug("結束時間:" + formatEndDateToString);

						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
						String meetDate = sdFormat.format(bean.getMeetingDate());

						String start = meetDate + "-" + bean.getMeetingStartTime();
						String end = meetDate + "-" + formatEndDateToString;

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

						// 比較時間用
						Date dateStart = null;
						try {
							dateStart = sdf.parse(start);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Date dateEnd = null;
						try {
							dateEnd = sdf.parse(end);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (dateStart.compareTo(dateEnd) < 0) {
							emailContent = emailContent.replace("$CANCLE_TIME$", start);
							logger.debug("開始時間:" + start);
							emailContent = emailContent.replace("$CANCLE_TIME_END$", end);
							logger.debug("結束時間:" + end);
						} else {
							emailContent = emailContent.replace("$CANCLE_TIME$", start);
							logger.debug("開始時間:" + start);
							// 時間超過24小時處理
							Calendar calendarE = Calendar.getInstance();
							logger.debug("@@@@" + end);
							calendarE.setTime(dateEnd);
							calendarE.add(Calendar.DATE, +1);
							Date endUpdate1 = calendarE.getTime();
							String endUpdate1Sdf = sdf.format(endUpdate1);
							logger.debug("日期超過24小時處理:" + endUpdate1Sdf);
							emailContent = emailContent.replace("$CANCLE_TIME_END$", endUpdate1Sdf);
						}

						List<String> contentStringList = new ArrayList<String>();
						contentStringList.add(emailContent);

						new MailThread(new MailBean(participantsEmail, emailTitle, contentStringList),
								getSendMailSetting()).start();

					}
				}
			}
		}
		return SUCCESS;
	}

	/**
	 * 新增預約-寄信給主席
	 */
	public String emailChairman() {
		logger.debug("寄信給主席");
		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "video_Conferencing_Chairman")),
				new QueryOrder[0], null, null);

		logger.debug("範例信件筆數：" + emailTemplate.size());

		if (emailTemplate.size() > 0) {

			String vendorName = "";
			if (bean.getSourceId().equals("ADMIN")) {
				vendorName = getText("web.node.cps");
			} else if (bean.getSourceId().equals("MTS")) {
				vendorName = getText("web.node.mts");
			} else if (bean.getSourceId().equals("BHS")) {
				vendorName = getText("web.node.bhs");
			} else {
				List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, bean.getSourceId())), new QueryOrder[0], null, null);
				if (vendorList.size() > 0) {
					vendorName = vendorList.get(0).getName();
				}
			}

			String attendance = "";
			List<CpsMeetingAttendance> attendanceList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
					CpsMeetingAttendance.class, new QueryGroup(new QueryRule(FK, bean.getSourceId())),
					new QueryOrder[0], null, null);
			if (attendanceList.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : attendanceList) {
					attendance += "，" + cpsMeetingAttendance.getAttendanceParticipantsName();
				}
				attendance = attendance.substring(1);
			}

			List<CpsMeetingAttendance> userEmailList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
					CpsMeetingAttendance.class,
					new QueryGroup(new QueryRule(FK, bean.getSysid()), new QueryRule("attendanceType", "1")),
					new QueryOrder[0], null, null);

			logger.debug("使用者數量：" + userEmailList.size());
			if (userEmailList.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : userEmailList) {

					CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);
					String cpsMeetingSysid = bean.getSysid();
					String participantsSysid = cpsMeetingAttendance.getSysid();
					String participantsEmail = cpsMeetingAttendance.getAttendanceParticipantsEmail();

					String url = "<a href='" + request.getScheme() + "://" + request.getHeader("host")
							+ request.getContextPath() + "/pages2/ControlOfVideoConferencing?cpsMeetingSysid="
							+ cpsMeetingSysid + "&participantsSysid=" + participantsSysid + "'>點我由此去</a>";

					logger.debug("自動寄信資訊(會議室SYSID)：" + cpsMeetingSysid);
					logger.debug("自動寄信資訊(會議人員SYSID)：" + participantsSysid);
					logger.debug("自動寄信資訊(會議人員EMAIL):" + participantsEmail);
					logger.debug("自動寄信資訊(URL連結)" + url);

					/** 標題部分 */
					String emailTitle = appointmentEmailTemplate.getEmailTitle();
					// 廠商名稱
					emailTitle = emailTitle.replace("$VENDOR_NAME$", vendorName);

					/** 內文部分 */
					String emailContent = appointmentEmailTemplate.getEmailContent();
					// 廠商名稱
					emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

					// 會議室連結
					emailContent = emailContent.replace("$INVITE_LINK$", url);

					// 會議室名稱
					emailContent = emailContent.replace("$CANCLE_NAME$", bean.getMeetingSubject());

					// 與會人員
					emailContent = emailContent.replace("$CANCLE_PARTICIPANTS$", attendance);

					// 會議室開始結束時間
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(bean.getMeetingStartTime());

					// 開始時間+會議時間
					if (bean.getMeetingSession().equals("30")) {
						calendar.add(Calendar.MINUTE, +30);
					} else if (bean.getMeetingSession().equals("60")) {
						calendar.add(Calendar.MINUTE, +60);
					} else if (bean.getMeetingSession().equals("90")) {
						calendar.add(Calendar.MINUTE, +90);
					} else if (bean.getMeetingSession().equals("120")) {
						calendar.add(Calendar.MINUTE, +120);
					}

					Date newEndDate = calendar.getTime();

					// 設定日期格式
					DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
					// 進行轉換
					String formatEndDateToString = formatDate.format(newEndDate);
					logger.debug("結束時間:" + formatEndDateToString);

					SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					String meetDate = sdFormat.format(bean.getMeetingDate());

					String start = meetDate + "-" + bean.getMeetingStartTime();
					String end = meetDate + "-" + formatEndDateToString;

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

					// 比較時間用
					Date dateStart = null;
					try {
						dateStart = sdf.parse(start);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Date dateEnd = null;
					try {
						dateEnd = sdf.parse(end);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (dateStart.compareTo(dateEnd) < 0) {
						emailContent = emailContent.replace("$CANCLE_TIME$", start);
						logger.debug("開始時間:" + start);
						emailContent = emailContent.replace("$CANCLE_TIME_END$", end);
						logger.debug("結束時間:" + end);
					} else {
						emailContent = emailContent.replace("$CANCLE_TIME$", start);
						logger.debug("開始時間:" + start);
						// 時間超過24小時處理
						Calendar calendarE = Calendar.getInstance();
						logger.debug("@@@@" + end);
						calendarE.setTime(dateEnd);
						calendarE.add(Calendar.DATE, +1);
						Date endUpdate1 = calendarE.getTime();
						String endUpdate1Sdf = sdf.format(endUpdate1);
						logger.debug("日期超過24小時處理:" + endUpdate1Sdf);
						emailContent = emailContent.replace("$CANCLE_TIME_END$", endUpdate1Sdf);
					}

					List<String> contentStringList = new ArrayList<String>();
					contentStringList.add(emailContent);

					new MailThread(new MailBean(participantsEmail, emailTitle, contentStringList), getSendMailSetting())
							.start();

				}
			}
		}
		return SUCCESS;
	}

	/**
	 * 新增預約-寄信給與會者
	 */
	public String emailAttendees() {
		logger.debug("寄信給與會者");
		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "video_Conferencing_Invite")),
				new QueryOrder[0], null, null);

		logger.debug("範例信件筆數：" + emailTemplate.size());

		if (emailTemplate.size() > 0) {

			String vendorName = "";

			if (bean.getSourceId().equals("ADMIN")) {
				vendorName = getText("web.node.cps");
			} else if (bean.getSourceId().equals("MTS")) {
				vendorName = getText("web.node.mts");
			} else if (bean.getSourceId().equals("BHS")) {
				vendorName = getText("web.node.bhs");
			} else {
				List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, bean.getSourceId())), new QueryOrder[0], null, null);
				if (vendorList.size() > 0) {
					vendorName = vendorList.get(0).getName();
				}
			}

			String attendance = "";
			List<CpsMeetingAttendance> attendanceList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
					CpsMeetingAttendance.class, new QueryGroup(new QueryRule(FK, bean.getSourceId())),
					new QueryOrder[0], null, null);
			if (attendanceList.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : attendanceList) {
					attendance += "，" + cpsMeetingAttendance.getAttendanceParticipantsName();
				}
				attendance = attendance.substring(1);
			}

			List<CpsMeetingAttendance> userEmailList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
					CpsMeetingAttendance.class,
					new QueryGroup(new QueryRule(FK, bean.getSysid()), new QueryRule("attendanceType", "3")),
					new QueryOrder[0], null, null);

			logger.debug("使用者數量：" + userEmailList.size());
			if (userEmailList.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : userEmailList) {

					CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);
					String emailContent = appointmentEmailTemplate.getEmailContent();
					String cpsMeetingSysid = bean.getSysid();
					String participantsSysid = cpsMeetingAttendance.getSysid();
					String participantsEmail = cpsMeetingAttendance.getAttendanceParticipantsEmail();

					String url = "<a href='" + request.getScheme() + "://" + request.getHeader("host")
							+ request.getContextPath() + "/pages2/ControlOfVideoConferencing?cpsMeetingSysid="
							+ cpsMeetingSysid + "&participantsSysid=" + participantsSysid + "'>點我由此去</a>";

					logger.debug("自動寄信資訊(會議室SYSID)：" + cpsMeetingSysid);
					logger.debug("自動寄信資訊(會議人員SYSID)：" + participantsSysid);
					logger.debug("自動寄信資訊(會議人員EMAIL):" + participantsEmail);
					logger.debug("自動寄信資訊(URL連結)" + url);

					/** 標題部分 */
					String emailTitle = appointmentEmailTemplate.getEmailTitle();
					// 廠商名稱
					emailTitle = emailTitle.replace("$VENDOR_NAME$", vendorName);

					/** 內容部分 */
					// 會議室連結
					emailContent = emailContent.replace("$INVITE_LINK$", url);
					// 廠商名稱
					emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

					// 與會人員
					emailContent = emailContent.replace("$CANCLE_PARTICIPANTS$", attendance);

					// 會議室名稱
					emailContent = emailContent.replace("$CANCLE_NAME$", bean.getMeetingSubject());

					// 會議室開始結束時間
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(bean.getMeetingStartTime());

					// 開始時間+會議時間
					if (bean.getMeetingSession().equals("30")) {
						calendar.add(Calendar.MINUTE, +30);
					} else if (bean.getMeetingSession().equals("60")) {
						calendar.add(Calendar.MINUTE, +60);
					} else if (bean.getMeetingSession().equals("90")) {
						calendar.add(Calendar.MINUTE, +90);
					} else if (bean.getMeetingSession().equals("120")) {
						calendar.add(Calendar.MINUTE, +120);
					}

					Date newEndDate = calendar.getTime();

					// 設定日期格式
					DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
					// 進行轉換
					String formatEndDateToString = formatDate.format(newEndDate);
					logger.debug("結束時間:" + formatEndDateToString);

					SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					String meetDate = sdFormat.format(bean.getMeetingDate());

					String start = meetDate + "-" + bean.getMeetingStartTime();
					String end = meetDate + "-" + formatEndDateToString;

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

					// 比較時間用
					Date dateStart = null;
					try {
						dateStart = sdf.parse(start);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Date dateEnd = null;
					try {
						dateEnd = sdf.parse(end);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (dateStart.compareTo(dateEnd) < 0) {
						emailContent = emailContent.replace("$CANCLE_TIME$", start);
						logger.debug("開始時間:" + start);
						emailContent = emailContent.replace("$CANCLE_TIME_END$", end);
						logger.debug("結束時間:" + end);
					} else {
						emailContent = emailContent.replace("$CANCLE_TIME$", start);
						logger.debug("開始時間:" + start);
						// 時間超過24小時處理
						Calendar calendarE = Calendar.getInstance();
						logger.debug("@@@@" + end);
						calendarE.setTime(dateEnd);
						calendarE.add(Calendar.DATE, +1);
						Date endUpdate1 = calendarE.getTime();
						String endUpdate1Sdf = sdf.format(endUpdate1);
						logger.debug("日期超過24小時處理:" + endUpdate1Sdf);
						emailContent = emailContent.replace("$CANCLE_TIME_END$", endUpdate1Sdf);
					}

					List<String> contentStringList = new ArrayList<String>();
					contentStringList.add(emailContent);

					new MailThread(new MailBean(participantsEmail, emailTitle, contentStringList), getSendMailSetting())
							.start();

				}
			}
		}

		return SUCCESS;
	}

	/**
	 * 會議室異動自動寄信功能(全部名單寄)
	 */

	protected String oldDataForEmail() {
		String newOrOld = request.getParameter("newOrOld");
		logger.debug("newOrOld:" + newOrOld);
		if (newOrOld.equals("OLD")) {
			List<CpsMeetingAttendance> userEmailList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
					CpsMeetingAttendance.class, new QueryGroup(new QueryRule(FK, bean.getSysid())), new QueryOrder[0],
					null, null);
			String attendance = "";
			if (userEmailList.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : userEmailList) {
					attendance += "，" + cpsMeetingAttendance.getAttendanceParticipantsName();
				}
				attendance = attendance.substring(1);
			}
			
			String vendorName = "";

			if (bean.getSourceId().equals("ADMIN")) {
				vendorName = getText("web.node.cps");
			} else if (bean.getSourceId().equals("MTS")) {
				vendorName = getText("web.node.mts");
			} else if (bean.getSourceId().equals("BHS")) {
				vendorName = getText("web.node.bhs");
			} else {
				List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
						new QueryGroup(new QueryRule(PK, bean.getSourceId())), new QueryOrder[0], null, null);
				if (vendorList.size() > 0) {
					vendorName = vendorList.get(0).getName();
				}
			}
			
			
			
			


			List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
					CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "video_Conferencing_Change")),
					new QueryOrder[0], null, null);

			logger.debug("範例信件筆數：" + emailTemplate.size());
			logger.debug("使用者數量：" + userEmailList.size());

			if (userEmailList.size() > 0 && emailTemplate.size() > 0) {
				for (CpsMeetingAttendance cpsMeetingAttendance : userEmailList) {

					CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);
					String cpsMeetingSysid = bean.getSysid();
					String participantsSysid = cpsMeetingAttendance.getSysid();
					String participantsEmail = cpsMeetingAttendance.getAttendanceParticipantsEmail();

					String url = "<a href='" + request.getScheme() + "://" + request.getHeader("host")
							+ request.getContextPath() + "/pages2/ControlOfVideoConferencing?cpsMeetingSysid="
							+ cpsMeetingSysid + "&participantsSysid=" + participantsSysid + "'>點我由此去</a>";

					logger.debug("自動寄信資訊(會議室SYSID)：" + cpsMeetingSysid);
					logger.debug("自動寄信資訊(會議人員SYSID)：" + participantsSysid);
					logger.debug("自動寄信資訊(會議人員EMAIL):" + participantsEmail);
					logger.debug("自動寄信資訊(URL連結)" + url);

					/**標題部分*/
					String emailTitle = appointmentEmailTemplate.getEmailTitle();
					
					//廠商名稱
					emailTitle = emailTitle.replace("$VENDOR_NAME$", vendorName);
					
					/**內文部分*/
					String emailContent = appointmentEmailTemplate.getEmailContent();					
					//廠商名稱
					emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);
					
					//與會者
					emailContent = emailContent.replace("$CANCLE_PARTICIPANTS$", attendance);
					
					// 會議室連結 
					emailContent = emailContent.replace("$INVITE_LINK$", url);

					// 會議室名稱 
					emailContent = emailContent.replace("$CANCLE_TITLE$", bean.getMeetingSubject());

					// 會議室開始結束時間 
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(bean.getMeetingStartTime());

					// 開始時間+會議時間
					if (bean.getMeetingSession().equals("30")) {
						calendar.add(Calendar.MINUTE, +30);
					} else if (bean.getMeetingSession().equals("60")) {
						calendar.add(Calendar.MINUTE, +60);
					} else if (bean.getMeetingSession().equals("90")) {
						calendar.add(Calendar.MINUTE, +90);
					} else if (bean.getMeetingSession().equals("120")) {
						calendar.add(Calendar.MINUTE, +120);
					}

					Date newEndDate = calendar.getTime();

					// 設定日期格式
					DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
					// 進行轉換
					String formatEndDateToString = formatDate.format(newEndDate);
					logger.debug("結束時間:" + formatEndDateToString);

					SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					String meetDate = sdFormat.format(bean.getMeetingDate());

					String start = meetDate + "-" + bean.getMeetingStartTime();
					String end = meetDate + "-" + formatEndDateToString;

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

					// 比較時間用
					Date dateStart = null;
					try {
						dateStart = sdf.parse(start);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Date dateEnd = null;
					try {
						dateEnd = sdf.parse(end);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (dateStart.compareTo(dateEnd) < 0) {
						emailContent = emailContent.replace("$CANCLE_TIME$", start);
						logger.debug("開始時間:" + start);
						emailContent = emailContent.replace("$CANCLE_TIME_END$", end);
						logger.debug("結束時間:" + end);
					} else {
						emailContent = emailContent.replace("$CANCLE_TIME$", start);
						logger.debug("開始時間:" + start);
						// 時間超過24小時處理
						Calendar calendarE = Calendar.getInstance();
						logger.debug("@@@@" + end);
						calendarE.setTime(dateEnd);
						calendarE.add(Calendar.DATE, +1);
						Date endUpdate1 = calendarE.getTime();
						String endUpdate1Sdf = sdf.format(endUpdate1);
						logger.debug("日期超過24小時處理:" + endUpdate1Sdf);
						emailContent = emailContent.replace("$CANCLE_TIME_END$", endUpdate1Sdf);
					}
					
					List<String> contentStringList = new ArrayList<String>();
					contentStringList.add(emailContent);

					new MailThread(new MailBean(participantsEmail, emailTitle,
							contentStringList), getSendMailSetting()).start();

				}

			}
			addActionMessage("貼心小提醒:您以儲存修改資訊，已將信寄送到客戶填寫的信箱!，告知客戶修正預約的時間");
		}

		return SUCCESS;
	}

	/**
	 * 手動寄送會議室信件功能(單筆寄信)
	 */
	public String sendSingleEmail() {
		/**資源準備*/		
		String msg = SUCCESS;
		String cpsMeetingSysid = request.getParameter("cpsMeetingSysid");
		String participantsSysid = request.getParameter("participantsSysid");
		String participantsEmail = request.getParameter("participantsEmail");
		String vendorSysid = request.getParameter("vendorSysid");
		
		
		String attendance = "";
		List<CpsMeetingAttendance> attendanceList = (List<CpsMeetingAttendance>) cloudDao.queryTable(sf(),
				CpsMeetingAttendance.class, new QueryGroup(new QueryRule(FK, cpsMeetingSysid)),
				new QueryOrder[0], null, null);
		if (attendanceList.size() > 0) {
			for (CpsMeetingAttendance cpsMeetingAttendance : attendanceList) {
				attendance += "，" + cpsMeetingAttendance.getAttendanceParticipantsName();
			}
			attendance = attendance.substring(1);
		}
		
		String vendorName = "";

		if (vendorSysid.equals("ADMIN")) {
			vendorName = getText("web.node.cps");
		} else if (vendorSysid.equals("MTS")) {
			vendorName = getText("web.node.mts");
		} else if (vendorSysid.equals("BHS")) {
			vendorName = getText("web.node.bhs");
		} else {
			
			List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
					new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);
			if (vendorList.size() > 0) {
				vendorName = vendorList.get(0).getName();
			}
		}
		

		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "video_Conferencing_Invite")),
				new QueryOrder[0], null, null);
		if (emailTemplate.size() > 0) {

			CpsEmailTemplate appointmentEmailTemplate = emailTemplate.get(0);

			String url = "<a href='" + request.getScheme() + "://" + request.getHeader("host")
					+ request.getContextPath() + "/pages2/ControlOfVideoConferencing?cpsMeetingSysid=" + cpsMeetingSysid
					+ "&participantsSysid=" + participantsSysid + "'>點我由此去</a>";

			logger.debug("單筆寄信資訊(會議室SYSID)：" + cpsMeetingSysid);
			logger.debug("單筆寄信資訊(會議人員SYSID)：" + participantsSysid);
			logger.debug("單筆寄信資訊(會議人員EMAIL)：" + participantsEmail);
			logger.debug("單筆寄信資訊(URL連結)：" + url);

			List<CpsMeeting> cpsMeetingList = (List<CpsMeeting>) cloudDao.queryTable(sf(), CpsMeeting.class,
					new QueryGroup(new QueryRule(PK, cpsMeetingSysid)), new QueryOrder[0], null, null);
			if (cpsMeetingList.size() > 0) {
				
				/**標題部分*/
				String emailTitle = appointmentEmailTemplate.getEmailTitle();
				//供應商名稱
				emailTitle =emailTitle.replace("$VENDOR_NAME$ ", vendorName);
				
				/**內文部分*/
				String emailContent = appointmentEmailTemplate.getEmailContent();
				
				emailContent = emailContent.replace("$VENDOR_NAME$ ", vendorName);
				//與會者
				emailContent = emailContent.replace("$CANCLE_PARTICIPANTS$", attendance);				
				
				// 會議室名稱 
				emailContent = emailContent.replace("$CANCLE_NAME$", cpsMeetingList.get(0).getMeetingSubject());

				// 會議室時間 
				emailContent = emailContent.replace("$CANCLE_TIME$",
						cpsMeetingList.get(0).getMeetingDate() + "-" + cpsMeetingList.get(0).getMeetingStartTime());
				
				// 結束時間
				DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
				Date endDate = null;
				try {
					endDate = formatDate.parse(cpsMeetingList.get(0).getMeetingDate() + "-" + cpsMeetingList.get(0).getMeetingStartTime());

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(endDate);
					int num = Integer.parseInt(cpsMeetingList.get(0).getMeetingSession());
					calendar.add(Calendar.MINUTE, num);
					Date newEndDate = calendar.getTime();
					// 進行轉換
					String formatDateToString = formatDate.format(newEndDate);
					logger.debug("轉換出結束時間：" + formatDateToString);

					emailContent = emailContent.replace("$CANCLE_TIME_END$", formatDateToString);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
								
				// 會議室連結 
				emailContent = emailContent.replace("$INVITE_LINK$", url);
				
				List<String> contentStringList = new ArrayList<String>();
				contentStringList.add(emailContent);
				
				new MailThread(new MailBean(participantsEmail, appointmentEmailTemplate.getEmailTitle(), contentStringList),
						getSendMailSetting()).start();

			}

		}
		resultString = msg;
		return JSON_RESULT;
	}

	/**
	 * 取消該參與人加入會議功能(單筆或多筆)
	 */
	public String cancelParticipant() {

		/** 資源處理 */
		String msg = SUCCESS;
		String deleteSysid = request.getParameter("deleteSysid");
		String deleteEmail = request.getParameter("deleteEmail");
		String deleteTitle = request.getParameter("deleteTitle");
		String deleteTimeStart = request.getParameter("deleteTimeStart");
		String endMinute = request.getParameter("deleteTimeEndMinute");
		String vendorSysid = request.getParameter("vendorSysid");
		logger.debug("刪除會議人員SYSID:" + deleteSysid);
		logger.debug("刪除會議人員Email:" + deleteEmail);

		/** 取消該參與人加入 */

		List<CpsMeetingAttendance> attendanceList = cloudDao.queryTable(sf(), CpsMeetingAttendance.class,
				new QueryGroup(new QueryRule(PK, IN, deleteSysid)), new QueryOrder[0], null, null);
		logger.debug("筆數:" + attendanceList.size());

		

		if (attendanceList.size() > 0) {
			for (CpsMeetingAttendance cpsMeetingAttendance : attendanceList) {			
				cloudDao.delete(sf(), cpsMeetingAttendance);
			}
		}

		Set<CpsMeetingAttendance> oldSet = (Set<CpsMeetingAttendance>) findDetailSetWhenEdit(DETAIL_SET);
		if (oldSet != null) {
			logger.debug("刪除檔案資料 dataSet2.size() =" + oldSet.size());
		}
		String oldSetSysid[] = deleteSysid.split(",");

		for (int c = 0; c < oldSetSysid.length; c++) {
			CpsMeetingAttendance b = null;
			for (CpsMeetingAttendance object : oldSet) {
				if (object.getSysid().equals(oldSetSysid[c])) {
					b = object;
					break;
				}
			}
			if (b != null) {
				boolean aa = oldSet.remove(b);

				logger.debug(aa);
			}

		}

		/** 寄信通知資源準備 */
		List<CpsEmailTemplate> emailTemplate = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
				CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "video_Conferencing_Cancel")),
				new QueryOrder[0], null, null);

		CpsEmailTemplate cancelParticipantEmailTemplate = emailTemplate.get(0);

		String vendorName = "";

		if (vendorSysid.equals("ADMIN")) {
			vendorName = getText("web.node.cps");
		} else if (vendorSysid.equals("MTS")) {
			vendorName = getText("web.node.mts");
		} else if (vendorSysid.equals("BHS")) {
			vendorName = getText("web.node.bhs");
		} else {
			List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
					new QueryGroup(new QueryRule(PK, vendorSysid)), new QueryOrder[0], null, null);

			if (vendorList.size() > 0) {
				vendorName = vendorList.get(0).getName();
			}
		}

		/** 標題部分 */
		String emailTitle = cancelParticipantEmailTemplate.getEmailTitle();
		// 廠商名稱
		emailTitle = emailTitle.replace("$VENDOR_NAME$", vendorName);

		/** 內容部分 */
		String emailContent = cancelParticipantEmailTemplate.getEmailContent();
		// 會議名稱
		emailContent = emailContent.replace("$CANCLE_TITLE$", deleteTitle);

		// 廠商名稱
		emailContent = emailContent.replace("$VENDOR_NAME$", vendorName);

		// 會議時間
		emailContent = emailContent.replace("$CANCLE_TIME$", deleteTimeStart);

		// 結束時間
		DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		Date endDate = null;
		try {
			endDate = formatDate.parse(deleteTimeStart);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(endDate);
			int num = Integer.parseInt(endMinute);
			calendar.add(Calendar.MINUTE, num);
			Date newEndDate = calendar.getTime();
			// 進行轉換
			String formatDateToString = formatDate.format(newEndDate);
			logger.debug("轉換出結束時間：" + formatDateToString);

			emailContent = emailContent.replace("$CANCLE_TIME_END$", formatDateToString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> contentStringList = new ArrayList<String>();
		contentStringList.add(emailContent);
		String email[] = deleteEmail.split(",");

		for (int i = 0; i < email.length; i++) {
			logger.debug("EMAIL:" + email[i]);

			new MailThread(new MailBean(email[i], emailTitle, contentStringList), getSendMailSetting()).start();

		}

		resultString = msg;
		return JSON_RESULT;
	}

}