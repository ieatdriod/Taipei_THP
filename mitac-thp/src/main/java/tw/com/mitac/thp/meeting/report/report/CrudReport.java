package tw.com.mitac.thp.meeting.report.report;

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

public class CrudReport {
	private ZoomMeetingApi zma = new ZoomMeetingApi();
	private CpsMeetingCfg cmc;

	public CrudReport(CpsMeetingCfg cmc) {
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
	public Response<DailyReport> getDailyReport(String year, String month) throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("API_Key", cmc.getKeyStr()));
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("api", "report_getdailyreport");
		paramMap.put("year", year);
		paramMap.put("month", month);
		for(String key : paramMap.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));	
		}
		urlParameters.add(new BasicNameValuePair("check_value", cmc.genCheckValue(paramMap)));
		Map<String, String> resultMap = zma.post(urlParameters);
		if (HttpStatus.SC_OK == Integer.parseInt(resultMap.get("Code"))) {
			return new Response<DailyReport>(resultMap.get("Entity")){};
		} else { //http error
			//TODO
			throw new Exception(String.format("Get account notified error! http[code:%s, message:%s", resultMap.get("Code"), resultMap.get("Phrase")));
		}		
	}
	
	public static void main(String[] args) throws Exception {
		
//		CrudReport cr = new CrudReport(new ZoomAccount());
//		Response<DailyReport> dr = cr.getDailyReport("2016", "03");
//		System.out.println(dr);

	}
}
