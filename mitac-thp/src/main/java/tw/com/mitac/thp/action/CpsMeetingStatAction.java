package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMeetingStat;

public class CpsMeetingStatAction extends BasisCrudAction<CpsMeetingStat> {
	
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = null;
		try {
			rules = new ArrayList<QueryRule>();
			if (StringUtils.isNotBlank(beaninfo.get("dateStatS"))) {
				rules.add(new QueryRule("dateStat", GE, sdfYMD.parse(beaninfo.get("dateStatS"))));
			}
			if (StringUtils.isNotBlank(beaninfo.get("dateStatE"))) {
				rules.add(new QueryRule("dateStat", LE, sdfYMD.parse(beaninfo.get("dateStatE"))));
			}
			
			if (rules.size() == 0) {
				rules = new ArrayList<QueryRule>();
				String dateStr = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
				rules.add(new QueryRule("dateStat", GE, sdfYMD.parse(dateStr)));
				rules.add(new QueryRule("dateStat", LE, sdfYMD.parse(dateStr)));
			}			
		} catch (Exception e) {
			
		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}
	
	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		if (queryGroup.getGroups().length == 0) { //未給條件時不帶資料
			List<CpsMeetingStat> statList = new ArrayList<CpsMeetingStat>();
			return new Object[]{statList.size(), statList};
		}
		List<CpsMeetingStat> results = cloudDao.queryTable(sf(), CpsMeetingStat.class, queryGroup, orders, from, length);
		List<CpsMeetingStat> statResult = statResults(results);
		if (getIsDataChangeLocale())
			addMultiLan(statResult, sf(), getPersistentClass());
		return new Object[] { statResult.size(), statResult };		
	}
	
	/**
	 * 以日統計表的bean形成統計表區間
	 * @param list
	 * @return
	 */
	private List<CpsMeetingStat> statResults(List<CpsMeetingStat> list) {
		
		Map<String, CpsMeetingStat> map = new HashMap<String, CpsMeetingStat>();
		for(CpsMeetingStat cms : list) { 
			String key = cms.getEntitySysid() + "-" + cms.getMeetingType(); //重新歸類, key=entitySysid-meetingType
			if (map.containsKey(key)) {
				CpsMeetingStat statCms = map.get(key);
				statCms.setTotalMeeting(statCms.getTotalMeeting() + cms.getTotalMeeting());
				statCms.setTotalMeetingMinutes(statCms.getTotalMeetingMinutes() + cms.getTotalMeetingMinutes());
				statCms.setTotalParticipants(statCms.getTotalParticipants() + cms.getTotalParticipants());
			} else {
				CpsMeetingStat statCms = new CpsMeetingStat();
				statCms.setEntitySysid(cms.getEntitySysid());
				statCms.setMeetingType(cms.getMeetingType());
				statCms.setTotalMeeting(cms.getTotalMeeting());
				statCms.setTotalMeetingMinutes(cms.getTotalMeetingMinutes());
				statCms.setTotalParticipants(cms.getTotalParticipants());
				map.put(key, statCms);
			}
		}
		return Arrays.asList(map.values().toArray(new CpsMeetingStat[map.size()]));
	}
	
}