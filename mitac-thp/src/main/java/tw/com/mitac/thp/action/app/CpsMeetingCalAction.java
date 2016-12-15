package tw.com.mitac.thp.action.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.bean.CpsVendor;

public class CpsMeetingCalAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "WeekDemo/CpsMeetingCal";
	}

	/**
	 * 時間表搜尋
	 */
	public List<Map> getDayAndTimeList() {

		// 依序定義會議室的編號
		Map<String, Integer> cfgMap = new HashMap<>();

		int i = 1;
		for (CpsMeetingCfg cfg : getDataCpsMeetingCfgTable().values()) {
			cfgMap.put(cfg.getAccountStr(), i++);
		}

		List<Map> cpsMeetingDayList2 = (List<Map>) session.get("DayAndTime");
		if (cpsMeetingDayList2 == null) {
			List<CpsMeeting> cpsMeetingDayList = cloudDao.queryTable(sf(), CpsMeeting.class, new QueryGroup(),
					new QueryOrder[0], null, null);

			cpsMeetingDayList2 = formatListToMap(cpsMeetingDayList);

			for (Map<String, Object> cpsMeeting : cpsMeetingDayList2) {

				/**
				 * 發起人處理- 1.僅限館主可以看全部 -2.USER只能看見自己的，非自己的只會顯示{回傳back-已預定}
				 */
				if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
					if (cpsMeeting.get("sourceId").equals("MTS")) {
						cpsMeeting.put("meetingUser", getText("web.mts"));
					} else if (cpsMeeting.get("sourceId").equals("BHS")) {
						cpsMeeting.put("meetingUser", getText("web.bhs"));
					} else if (cpsMeeting.get("sourceId").equals("ADMIN")) {
						cpsMeeting.put("meetingUser", getText("web.admin"));
					} else {
						List<CpsVendor> cpsMemberList = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(
								new QueryRule(PK, cpsMeeting.get("sourceId"))), new QueryOrder[0], null, null);
						if (cpsMemberList.size() > 0) {
							cpsMeeting.put("meetingUser", cpsMemberList.get(0).getName());
						} else {
							cpsMeeting.put("meetingUser", "back");
						}
					}
				} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
					if (cpsMeeting.get("sourceId").equals(getUserAccount().getSourceSysid())) {
						List<CpsVendor> cpsMemberList = cloudDao.queryTable(sf(), CpsVendor.class, new QueryGroup(
								new QueryRule(PK, cpsMeeting.get("sourceId"))), new QueryOrder[0], null, null);
						cpsMeeting.put("meetingUser", cpsMemberList.get(0).getName());
					} else {
						cpsMeeting.put("meetingUser", "back");
					}

				} else {
					cpsMeeting.put("meetingUser", "back");
				}

				/**
				 * 依照會議部分代碼轉換為會議室編號
				 */
				// 會議室名稱
				String cp9 = (String) cpsMeeting.get("videoConferenceId");
				Integer room = cfgMap.get(cp9);
				cpsMeeting.put("room", room);

			}
			request.setAttribute("DayAndTime", cpsMeetingDayList2);
		}
		return cpsMeetingDayList2;
	}

	public String init() {

		return SUCCESS;

	}
}