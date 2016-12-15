package tw.com.mitac.thp.meeting.meeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;


public class CrudZoomMeeting {
	private ZoomMeetingApi zma = new ZoomMeetingApi();
	private CpsMeetingCfg cmc;
	public CrudZoomMeeting(CpsMeetingCfg cmc) {
		this.cmc = cmc;
	}
	
	public CpsMeetingCfg getCmc() {
		return cmc;
	}

	public void setCmc(CpsMeetingCfg cmc) {
		this.cmc = cmc;
	}

	/**
	 * Craete a Meeting
	 * request : ZoomAccount, ZoomMeeting
	 * @return ReturnZoomMeeting
	 * @throws Exception
	 */
	public Response<ReturnMeetingData> createMeeting(ParamMeetingData pmd) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = pmd.getParams();
		paramMap.put("api", "meeting_create");
		paramMap.put("host_id", cmc.getAccountStr());
		zma.toURLEncode(paramMap);
		System.out.println(paramMap);
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));
		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<ReturnMeetingData>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Create Meeting error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	/**
	 * Get Zoom meetings list
	 * request : ZoomAccount 
	 * @param pageSize 一頁幾筆資料
	 * @param pageNumber 第幾頁
	 * @return
	 * @throws Exception
	 */
	public Response<ReturnAllMeetingData> queryMeetings(int pageSize, int pageNumber) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put("api", "meeting_list");
		paramMap.put("host_id", cmc.getAccountStr());
		if (pageSize > 0) {
			paramMap.put("page_size", String.valueOf(pageSize));
			paramMap.put("page_number", String.valueOf(pageNumber));			
		}
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}			
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));

		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<ReturnAllMeetingData>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Query Meetings error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	/**
	 * Get a Zoom meeting
	 * request : ZoomAccount
	 * @return
	 * @throws Exception
	 */
	public Response<ReturnMeetingData> queryMeeting(String meeting_id) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("api", "meeting_get");
		paramMap.put("host_id", cmc.getAccountStr());
		paramMap.put("id", meeting_id);
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}		
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));
		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<ReturnMeetingData>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Query Meeting error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	/**
	 * Update zoom Meeting
	 * request : ZoomAccount, ZoomMeeting
	 * @return
	 * @throws Exception
	 */
	public Response<ReturnUpdateData> updateMeeting(String meeting_id, ParamMeetingData pmd) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = pmd.getParams();
		paramMap.put("api", "meeting_update");
		paramMap.put("host_id", cmc.getAccountStr());
		paramMap.put("id", meeting_id);
		zma.toURLEncode(paramMap);
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));
		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<ReturnUpdateData>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Update Meeting error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	/**
	 * delete a Meeting
	 * request : ZoomAccount
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Response<ReturnDeleteData> deleteMeeting(String meeting_id) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("api", "meeting_delete");
		paramMap.put("host_id", cmc.getAccountStr());
		paramMap.put("id", meeting_id);
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}		
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));
		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<ReturnDeleteData>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Delete Meeting error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		CpsMeetingCfg cmc = new CpsMeetingCfg();
		cmc.setTestData();
		CrudZoomMeeting czm = new CrudZoomMeeting(cmc);
		//Create new meeting
		
//		ParamMeetingData pmd = new ParamMeetingData();
//
//		pmd.setTopic("全新日程會議室測試1");
//		pmd.setType(2);
//		pmd.setStart_time(DateUtils.parseDate("20160305 1400", "yyyyMMdd HHmm"));
//		pmd.setDuration(120);
//		pmd.setTimezone("Asia/Taipei");
		
//		System.out.println(pmd.getParams());
//		CreateMeeting cm = czm.createMeeting(pmd);
//		System.out.println(cm);
		
		//QueryAllMeeting qam = czm.queryMeetings(-1, -1);
		//System.out.println(qam);
		
		//DeleteMeeting dm = czm.deleteMeeting("494810978");
		//System.out.println(dm);
//		Response<ReturnMeetingData> qm = czm.createMeeting(pmd);
//		System.out.println(qm);
		
		
		//query meetings
		Response<ReturnAllMeetingData> rzms = czm.queryMeetings(-1, -1);
		System.out.println(rzms);
//		System.out.println(rzms.getZp());
//		System.out.println(rzms.getZms());
//		List<ZoomMeeting> zmList = rzms.getZms();
	
		
		//query a meeting
//		String num = "385789187";
//		Response<ReturnMeetingData> qm = czm.queryMeeting(num);
//		System.out.println(num + "：" + qm.getData().getStatus());
		
		//delete
//		ZoomReturn zr = czm.deleteMeeting("768729269");
//		System.out.println(zr);
		
		//update a meeting
//		ZoomMeeting zm = new ZoomMeeting();
//		zm.setTopic("會議室更新測試");
//		zm.setPassword("123456");
//		zm.setId("828746356");
//		czm.setZm(zm);
//		ZoomReturn zr = czm.updateMeeting();
//		System.out.println(zr);
	}
}
