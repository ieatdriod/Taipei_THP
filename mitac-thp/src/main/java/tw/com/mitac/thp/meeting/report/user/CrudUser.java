package tw.com.mitac.thp.meeting.report.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;

public class CrudUser {
	private ZoomMeetingApi zma = new ZoomMeetingApi();
	private CpsMeetingCfg cmc;

	public CrudUser(CpsMeetingCfg cmc) {
		this.cmc = cmc;
	}
	
	public CpsMeetingCfg getCmc() {
		return cmc;
	}

	public void setCmc(CpsMeetingCfg cmc) {
		this.cmc = cmc;
	}

	/**
	 * Get UserReport
	 * @return UserReport
	 * @throws Exception
	 */
	public Response<UserReport> getUserReport(String from, String to, int page_size, int page_number) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put("api", "report_getuserreport");
		paramMap.put("from", from);
		paramMap.put("to", to);
		paramMap.put("user_id", cmc.getAccountStr());
		if (page_size > 0) {
			paramMap.put("page_size", String.valueOf(page_size));
			paramMap.put("page_number", String.valueOf(page_number));			
		}
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}			
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));

		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<UserReport>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Get account report error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}			
	}
	
	/**
	 * Get all UserReport
	 * request : ZoomAccount
	 * @param from : GMT 例如2016-02-01, 20160201 
	 * @param to : GMT 例如2016-02-01, 20160201
	 * @param pageSize 一頁幾筆資料
	 * @param pageNumber 第幾頁
	 * @return
	 * @throws Exception
	 */
	public Response<UserReport> getAllUserReport(String from, String to, int page_size, int page_number) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put("api", "report_getalluserreport");
		paramMap.put("from", from);
		paramMap.put("to", to);
		if (page_size > 0) {
			paramMap.put("page_size", String.valueOf(page_size));
			paramMap.put("page_number", String.valueOf(page_number));			
		}
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}			
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));

		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<UserReport>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Get account report error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	

	
	public static void main(String[] args) throws Exception {
		CpsMeetingCfg cmc = new CpsMeetingCfg();
		cmc.setTestData();
		CrudUser cu = new CrudUser(cmc);
		Response<UserReport> ur = cu.getUserReport("2016-03-01", "2016-04-01", -1, -1);
		System.out.println(ur.toString());
//		System.out.println(Arrays.toString(TimeZone.getAvailableIDs()));
//		System.out.println(TimeZone.getDefault().getDisplayName());
//		System.out.println(TimeZone.getDefault().getID());
	}
}
