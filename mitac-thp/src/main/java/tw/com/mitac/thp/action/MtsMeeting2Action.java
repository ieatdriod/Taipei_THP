package tw.com.mitac.thp.action;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;

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

public class MtsMeeting2Action extends CpsMeetingController {
	private static final String entId = "mts";
	private static String entSysid = "";
	private static String entName = "";
	private String orderNo;
	private String orderService;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderService() {
		return orderService;
	}

	public void setOrderService(String orderService) {
		this.orderService = orderService;
	}

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
					throw new Exception(getText("error.message3"));
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
					throw new Exception(getText("error.message3"));
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
			boolean result = super.executeSave();
			return result;
		} catch (Exception e) {
			addActionError(e.getMessage());
		}
		return false;

	}

	private void getEntitySysid() {
		List<CpsEntity> list = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(
				new QueryRule(IS_ENABLED, true), new QueryRule("dataId", entId)), new QueryOrder[0], null, null);

		if (list != null && list.size() > 0) {
			entSysid = list.get(0).getSysid();
			entName = list.get(0).getDataId() + "：" + list.get(0).getName();
		}
	}

	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		if (entId == null || "".equals(entSysid)) {
			getEntitySysid();
		}
		queryGroup = new QueryGroup(new QueryRule("entitySysid", entSysid));
		return super.jqgridList(clazz, queryGroup, orders, from, length);
	}

	public String orderData() {
		String orderItemSysid = request.getParameter("orderItemSysid");
		System.out.println("orderItemSysid=" + orderItemSysid);
		Map<String, String> map = getOrderData(orderItemSysid);
		if (map != null) {
			JSONObject json = new JSONObject(map);
			resultString = json.toString();
		}
		return JSON_RESULT;
	}

	private Map<String, String> getOrderData(String orderItemSysid) {
		Session session = null;
		Map<String, String> rtnMap = null;
		try {
			session = sf().openSession();
			StringBuffer sb = new StringBuffer();
			sb.append("select (select odno from mts_orders where sysid=a.orders_sysid) as odno, ")
					.append("(select items_name from mts_items where sysid=a.mts_items_sysid) as itemsname ")
					.append("from mts_orders_items a ").append("where a.sysid='").append(orderItemSysid).append("'");

			Query query = session.createSQLQuery(sb.toString());
			List<?> list = query.list();
			if (list != null && list.size() > 0) {
				rtnMap = new HashMap<String, String>();
				for (int i = 0; i < list.size(); i++) {
					Object[] o = (Object[]) list.get(i);
					rtnMap.put("orderNo", o[0].toString());
					rtnMap.put("orderService", o[1].toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return rtnMap;
	}

	@Override
	public String edit() {

		String result = super.edit();
		if (entId == null || "".equals(entSysid)) {
			getEntitySysid();
		}
		bean.setEntitySysid(entSysid);
		beaninfo.put("entitySysidShow", entName);
		// if (StringUtils.isBlank(bean.getInitiatorCpsMemberSysid())) {
		// bean.setInitiatorCpsMemberSysid(getUserAccount().getSysid());
		// beaninfo.put("initiatorCpsMemberSysidShow", getUserID());
		// }

		// orderItemSysid欄位已經刪除(根據需求已經不需要)
		// String orderItemSysid = request.getParameter("orderItemSysid");
		// if (orderItemSysid == null || "".equals(orderItemSysid)) {
		// orderItemSysid = bean.getOrderItemSysid();
		// }
		// if (orderItemSysid != null && !"".equals(orderItemSysid)) {
		// Map<String, String> map = getOrderData(orderItemSysid);
		// if (map != null) {
		// orderNo = map.get("orderNo");
		// orderService = map.get("orderService");
		// }
		// }
		//
		// if (bean.getOrderItemSysid() == null ||
		// "".equals(bean.getOrderItemSysid())) {
		// bean.setOrderItemSysid(orderItemSysid);
		// }
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
				"usageEntitySysid", CN, "mts")
		// , new QueryRule("attendance", GE, bean.getAttendanceNumber())
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