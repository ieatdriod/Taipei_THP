package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsMember;

public class CpsMeetingListAction extends BasisCrudAction<CpsMeetingAttendance> {
	public boolean getWithoutClickBtnEdit() {
		return true;
	}

	@Override
	protected QueryGroup getQueryRestrict() {
		List<QueryRule> rules = new ArrayList<QueryRule>();
		String memberSysid = getMemberSysid(getUserID());
		System.out.println("MemberSysid=" + memberSysid);
		rules.add(new QueryRule("attendanceCpsMemberSysid", memberSysid));
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] rtnArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<CpsMeetingAttendance> list = (List<CpsMeetingAttendance>) rtnArr[1];

		Map<String, CpsMeetingAttendance> newMap = new HashMap<String, CpsMeetingAttendance>();

		for (CpsMeetingAttendance bean : list) {

			String sysid = bean.getParentSysid();
			if (sysid == null || "".equals(sysid))
				continue;

			List<CpsMeeting> cmList = cloudDao.queryTable(sf(), CpsMeeting.class, new QueryGroup(new QueryRule("sysid",
					EQ, sysid)), new QueryOrder[0], null, null);

			// CpsMeeting cm = null;
			// if (cmList != null && cmList.size() > 0) {
			// cm = cmList.get(0);
			// }
			// if (cm != null && !"2".equals(cm.getMeetingStatus())) {
			// if (!newMap.containsKey(sysid)) {
			// newMap.put(sysid, bean);
			// }
			// }
		}
		List<CpsMeetingAttendance> newList = new ArrayList<CpsMeetingAttendance>();
		if (newMap.size() > 0) {
			newList = Arrays.asList(newMap.values().toArray(new CpsMeetingAttendance[newMap.size()]));
		}

		Object[] newArr = new Object[] { rtnArr[0], newList };
		return newArr;
	}

	private String getMemberSysid(String uuid) {
		List<CpsMember> l = cloudDao.queryTable(sf(), CpsMember.class, new QueryGroup(
				new QueryRule("uuid", getUserID())), new QueryOrder[0], null, null);
		return l.get(0).getSysid();
	}
}
