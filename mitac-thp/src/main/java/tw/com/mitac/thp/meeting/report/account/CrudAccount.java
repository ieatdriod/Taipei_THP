package tw.com.mitac.thp.meeting.report.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.meeting.ZoomMeetingApi;
import tw.com.mitac.thp.meeting.base.Response;

public class CrudAccount {
	private ZoomMeetingApi zma = new ZoomMeetingApi();
	private CpsMeetingCfg cmc;

	public CrudAccount(CpsMeetingCfg cmc) {
		this.cmc = cmc;
	}
	
	public CpsMeetingCfg getCmc() {
		return cmc;
	}

	public void setCmc(CpsMeetingCfg cmc) {
		this.cmc = cmc;
	}

	/**
	 * Get account notified
	 * @return AccountNotified
	 * @throws Exception
	 */
	public Response<ReturnAccountNotified> getAccountNotified() throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("api", "report_getaccountnotified");
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));
		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<ReturnAccountNotified>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Get account notified error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	/**
	 * Get account report
	 * request : ZoomAccount 
	 * @param from 起始日期 yyyyMMdd, yyyy-MM-dd
	 * @param to 結束日期 yyyyMMdd, yyyy-MM-dd
	 * @param page_size
	 * @param page_number
	 * @return
	 * @throws Exception
	 */
	public Response<ReturnAccountReport> getAccountReport(String from, String to, int page_size, int page_number) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put("api", "report_getaccountreport");
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
			return new Response<ReturnAccountReport>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Get account report error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	

	
	public static void main(String[] args) throws Exception {
//		CrudAccount ca = new CrudAccount(new ZoomAccount());
//		Response<ReturnAccountReport> ar = ca.getAccountReport("20160101", "20160331", -1, -1);
//		System.out.println(ar);
	}
}
