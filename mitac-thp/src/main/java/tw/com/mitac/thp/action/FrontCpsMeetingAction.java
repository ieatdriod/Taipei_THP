package tw.com.mitac.thp.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsVendor;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FrontCpsMeetingAction extends BasisFrontLoginAction {

	private String entityId;

	private static final long serialVersionUID = 1L;

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	/**
	 * 
	 */
	public String doCpsMeetingData() {
		String memberSysid = getUserData2().getAccount().getSysid();

		logger.debug("doCpsMeetingData-uid:" + getUserData2().getUid());
		logger.debug("doCpsMeetingData-memberSysid:" + memberSysid);

		List<Map> list = (List<Map>) cloudDao.findProperty(sf(), CpsMeetingAttendance.class,
				new QueryGroup(new QueryRule("attendanceCpsSitememberSysid", EQ, memberSysid)), new QueryOrder[0],
				false, FK, "attendanceType"); // findProperty可以找出多個欄位，只能放最後面參數

		if (list.size() > 0) {
			// <FK,attendanceType>
			Map<String, String> attendanceTypeMap = new HashMap<String, String>();

			for (Map<String, String> row : list) {
				String fk = row.get(FK);
				String type = row.get("attendanceType");

				attendanceTypeMap.put(fk, type);
			}

			List<CpsMeeting> 
			cpsMeetinglist = cloudDao.queryTable(sf(), CpsMeeting.class,
					new QueryGroup(new QueryRule(PK, IN, attendanceTypeMap.keySet())),
					new QueryOrder[] { new QueryOrder("meetingDate", DESC) }, null, null); // keySet()取出list所有的key
			addMultiLan(cpsMeetinglist, sf(), CpsMeeting.class);
				
			logger.debug("doCpsMeetingData-cpsMeetinglist.size():" + cpsMeetinglist.size());

			List<Map> cpsMeetingMapList = formatListToMap(cpsMeetinglist); // 將list轉為map
			
//			使過期會議取消啟動會議按鈕
			for (int i = 0; i < cpsMeetingMapList.size(); i++) {
				Map cpsMeeting = cpsMeetingMapList.get(i);
				CpsMeeting a=cpsMeetinglist.get(i);
				
				 cpsMeeting.put("isShow", false);
//				boolean isNotShow = false;
//
//				 Calendar d1 = Calendar.getInstance();
//				 Calendar d2 = Calendar.getInstance();
//				
//				 d1.setTime(systemDate);
//				 System.out.println(systemDate);
//				 System.out.println(d1);
//				 
//				 d2.setTime(a.getMeetingDate());
//				 System.out.println(a.getMeetingDate());
//				 System.out.println(d2);
//				 
				 
				 
				
//			for (Map cpsMeeting : cpsMeetingMapList) {
				logger.debug("obj:" + ReflectionToStringBuilder.toString(cpsMeeting, ToStringStyle.MULTI_LINE_STYLE)); // 把list內容丟出來

				// 發起人
				String sourceId = cpsMeeting.get("sourceId").toString();
				String showSourceIdName = "";
				if ("BHS".equals(sourceId)) {
					showSourceIdName = getText("web.node.bhs");
				} else if ("MTS".equals(sourceId)) {
					showSourceIdName = getText("web.node.mts");
				} else if ("ADMIN".equals(sourceId)) {
					showSourceIdName = getText("web.admin");
				} else {
					showSourceIdName = findVendorName(sourceId);
				}
				cpsMeeting.put("showSourceIdName", showSourceIdName);
				
				// 計算結束時間
				if (StringUtils.isNotBlank(cpsMeeting.get("meetingStartTime").toString())) {
					String[] StartTimeArr = cpsMeeting.get("meetingStartTime").toString().split(":");
					String newStartTime = StartTimeArr[0] + ":" + StartTimeArr[1];
					logger.debug("doCpsMeetingData-newStartTime:" + newStartTime);

					String endTime = doMeetingEndTime(newStartTime, cpsMeeting.get("meetingSession").toString());
					
					cpsMeeting.put("endTime", endTime);
					
					
					//判別是否大於當日
					
						
						 Date nDate = new Date();
						 String eDate =a.getMeetingDate() +" "+ endTime+":00";
						 
						 
						 String sDate =a.getMeetingDate() +" "+ newStartTime+":00";
						 
						Date sDatef = null;
						try {
							sDatef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate);
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						
						//前10分
						Date sDatef10 = null;
						try {
							sDatef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(sDatef);
							calendar.add(Calendar.MINUTE, -10);
							sDatef10 = 	calendar.getTime();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						Date eDatef = null;
						try {
							eDatef = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(eDate);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							
						 
						
						 logger.debug("開始時間"+sDate);
						 logger.debug("結束時間"+eDate);
						 logger.debug("現在時間"+nDate);
						 logger.debug("阻擋前10分鐘"+sDatef10);
						
						 //按鈕要不要顯示
						 if(nDate.compareTo(eDatef)<0){
						 cpsMeeting.put("isShow", true);
						 }
						 logger.debug(cpsMeeting.get("isShow"));
						 
						 //阻擋提早啟動現有時間小於開始時間-10分就鎖起來
						 if(nDate.compareTo(sDatef10)<0){
							 cpsMeeting.put("startBlocking", true);
						 }
						 
						 
						 
				}
				
				
				 

				String type = attendanceTypeMap.get(cpsMeeting.get(PK));
				cpsMeeting.put("type", type);
			}

			
			
			// session.put("cpsMeetinglist", cpsMeetinglist); //用這個前端會接不到list
			request.setAttribute("rtncpsMeetingMapList", cpsMeetingMapList);
		} else {
			request.setAttribute("rtncpsMeetingMapList", null);
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

		outEndTime = String.format("%02d", (hh + sum1 + tmphh) )+ ":" + String.format("%02d", tmpmm);

		return outEndTime;
	}

	public String findVendorName(String insourceId) {
		String rtnVendorName = "";

		List<CpsVendor> cpsVendorList = cloudDao.queryTable(sf(), CpsVendor.class,
				new QueryGroup(new QueryRule(PK, EQ, insourceId)), new QueryOrder[0], null, null);
		addMultiLan(cpsVendorList, sf(), CpsVendor.class);
		
		if (cpsVendorList.size() > 0) {
			if (StringUtils.isNotBlank(cpsVendorList.get(0).getName())) {
				rtnVendorName = cpsVendorList.get(0).getName().toString();
			}
		}

		return rtnVendorName;
	}

	private String getEntitySysid(String entityId) {
		List<CpsEntity> list = cloudDao.queryTable(sf(), CpsEntity.class,
				new QueryGroup(new QueryRule(IS_ENABLED, true), new QueryRule("dataId", entityId)), new QueryOrder[0],
				null, null);
		addMultiLan(list, sf(), CpsEntity.class);
		if (list != null && list.size() > 0) {
			return list.get(0).getSysid();
		}
		return null;
	}

	// public String getMemberName() {
	//
	// try {
	// // Map<String, String[]> map = request.getParameterMap();
	// // for (String s : map.keySet()) {
	// // String[] ss = map.get(s);
	// // System.out.println(s + ":" + Arrays.toString(ss));
	// // }
	//
	// // System.out.println(request.getParameter("sysid"));
	// String sysid = request.getParameter("sysid");
	// Map<String, String> jsonMap = new LinkedHashMap<String, String>();
	// List<CpsSiteMember> list = cloudDao.queryTable(sf(),
	// CpsSiteMember.class, new QueryGroup(new QueryRule("sysid",
	// sysid)), new QueryOrder[0], null, null);
	//
	// if (list != null && list.size() > 0) {
	// CpsSiteMember cm = list.get(0);
	// jsonMap.put("memberName", cm.getMemberName());
	// }
	// JSONObject jsonObject = new JSONObject(jsonMap);
	// resultString = jsonObject.toString();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return JSON_RESULT;
	// }
	//
	// // 用來驗證該帳號是否可取得會議室代碼的判斷, 目前無會員帳號可判斷, 未完成
	// public String getJoinRoom() {
	// try {
	// Map<String, String[]> map = request.getParameterMap();
	// for (String s : map.keySet()) {
	// String[] ss = map.get(s);
	// System.out.println(s + ":" + Arrays.toString(ss));
	// }
	//
	// System.out.println(request.getParameter("roomId"));
	//
	// Map<String, String> jsonMap = new LinkedHashMap<String, String>();
	// jsonMap.put("Key1", "Value1");
	// JSONObject jsonObject = new JSONObject(jsonMap);
	// resultString = jsonObject.toString();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return JSON_RESULT;
	// }

}
