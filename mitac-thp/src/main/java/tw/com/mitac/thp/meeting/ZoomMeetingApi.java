package tw.com.mitac.thp.meeting;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class ZoomMeetingApi {

	private static String postUrl = "https://zoomnow.net/API/zntw_api.php";
	
//	public Map<String, Object> getUserList() {
//		
//		Map<String, Object> rtnMap = new HashMap<String, Object>();
//		try {
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("api", "user_list");
//			Map<String, String> postMap = post(map);
//			if (HttpStatus.SC_OK == Integer.parseInt(postMap.get("Code"))) {
//				
//				JSONObject json = new JSONObject(postMap.get("Entity"));
//				
//				rtnMap.put("code", json.get("code"));
//				rtnMap.put("message", json.get("message"));
//				JSONObject data = new JSONObject(json.get("data").toString());
//				Map<String, Object> dataMap = new HashMap<String, Object>();
//				dataMap.put("page_count", data.get("page_count"));
//				dataMap.put("total_records", data.get("total_records"));
//				dataMap.put("page_number", data.get("page_number"));
//				dataMap.put("page_size", data.get("page_size"));
//				
//				Map<String, Object> usersMap = new HashMap<String, Object>();
//				JSONArray users = data.getJSONArray("users");
//				
//				Map<String, String> userMap = null;
//				for(int i=0; i<users.length(); i++) {
//					System.out.println(users.get(i));					
//					JSONObject user = new JSONObject(users.get(i).toString());
//					
//					userMap = new HashMap<String, String>();
//					Iterator<String> it = user.keys();
//					while(it.hasNext()) {
//						String key = it.next();
//						userMap.put(key, user.getString(key));
//					}
//					usersMap.put(user.getString("id"), userMap);					
//				}
//				dataMap.put("users", usersMap);
//				rtnMap.put("data", dataMap);
//			} else {
//				//TODO 尚未處理失敗狀況
//			}
//		} catch ( JSONException je) {
//			je.printStackTrace();
//		}
//		return rtnMap;		
//	}
	
	
	public void toURLEncode(Map<String, String> map) throws Exception{
		
		for(String key : map.keySet()) {
			
			String value = map.get(key);
			if (value.length() != value.getBytes().length) {
				map.put(key, URLEncoder.encode(value, "UTF-8"));
			}
		}
	}
	
//	public void toURLDecode(Map<String, String> map) throws Exception {
//		
//		for(String key : map.keySet()) {
//			String value = map.get(key);
//			System.out.println(value);
//			map.put(key, URLDecoder.decode(value, "UTF-8"));
//		}
//		
//	}
	
	public static String toURLDecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
	}
	
	public Map<String, String> post(List<NameValuePair> urlParameters) {
		HttpClient client = null;
		Map<String, String> rtnMap = new HashMap<String, String>();
		try {
			client = createHttpClient_SSL();
			HttpPost post = new HttpPost(postUrl);
			
			post.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
			//post.addHeader("Content-type", "application/json");
			post.addHeader("Content-type", "application/x-www-form-urlencoded");			
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			HttpResponse response = client.execute(post);
			int responseCode = response.getStatusLine().getStatusCode();
			String responsePhrase = response.getStatusLine().getReasonPhrase();
			System.out.println("RESPONSE CODE:" + responseCode);
			System.out.println("RESPONSE PHRASE:" + responsePhrase);
			String entity = EntityUtils.toString(response.getEntity());
			System.out.println("Entity:" + entity);
			rtnMap.put("Code", String.valueOf(responseCode));
			rtnMap.put("Phrase", String.valueOf(responsePhrase));
			rtnMap.put("Entity", entity);
			//toURLDecode(rtnMap);
			
		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			rtnMap.put("Code", "-990");
			rtnMap.put("Phrase", ue.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			rtnMap.put("Code", "-991");
			rtnMap.put("Phrase", e.getMessage());
		} finally {

		}
		
		return rtnMap;
		
	}	
	
	
	/**
	 * getISODateTime("20150304 1430", "yyyyMMdd HHmm", "Asia/Taipei") 
	 * @param s 日期字串
	 * @param pattern  日期字串的格式
	 * @param timezone 時區
	 * @return
	 * @throws Exception
	 */
	public static String getISODateTime(String s, String pattern, String timezone) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date dd = sdf.parse(s);
		return getISODateTime(dd, timezone);
	}
	
	public static String getISODateTime(Date date, String timezone) throws Exception {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf1.setTimeZone(TimeZone.getTimeZone(timezone));
		String rtnStr = sdf1.format(date);
		return rtnStr;		
	}
	
	private HttpClient createHttpClient_SSL() {
		HttpClient client = null;

		try {
			HttpClientBuilder b = HttpClientBuilder.create();
			
			// setup a Trust Strategy that allows all certificates.
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy(){
				
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}
				
			}).build();
			b.setSslcontext(sslContext);
			
			// don't check Hostnames, either.
		    //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
			HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			
		    // here's the special part:
		    //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
		    //      -- and create a Registry, to register it.
		    //			
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
		    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
		            .register("http", PlainConnectionSocketFactory.getSocketFactory())
		            .register("https", sslSocketFactory)
		            .build();
		    
		    // now, we create connection-manager using our Registry.
		    //      -- allows multi-threaded use		    
		    PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
		    b.setConnectionManager( connMgr);
		    client = b.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return client;
	}
	
	/**
	 * 轉換時區
	 * @param dateTime 例如：2016-02-18 06:03:59
	 * @param pattern 對應dateTime的格式 例如：yyyy-MM-dd HH:mm:ss
	 * @param fromTimeZone 例如 GMT
	 * @param toTimeZone 例如 Asia/Taipei
	 * @return
	 */
	public static String TimeZoneConver(String dateTime, String[] pattern, String fromTimeZone, String toTimeZone) throws ParseException {
		if (dateTime == null || "".equals(dateTime)) {
			return null;
		}
		
		for(String paStr : pattern) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(paStr);
				sdf.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
				Date d = sdf.parse(dateTime);
				sdf.setTimeZone(TimeZone.getTimeZone(toTimeZone));
				return sdf.format(d);						
			} catch (Exception e) {
				
			}			
		}
		return null;
	}
	
	public static Date TimeZoneConverDate(String dateTime, String[] pattern, String fromTimeZone, String toTimeZone) throws ParseException {
		if (dateTime == null || "".equals(dateTime)) {
			return null;
		}
		for(String paStr : pattern) {
			
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(paStr);
				sdf.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
				Date d = sdf.parse(dateTime);
				sdf.setTimeZone(TimeZone.getTimeZone(toTimeZone));
				return sdf.parse(sdf.format(d));				
			} catch (Exception e) {
			}
		}
		
		throw new ParseException("Unable to match the date format:" + Arrays.toString(pattern),0);
		
	}
	
	public static boolean parseDate(String dateTime, String... pattern) {
		try {
			DateUtils.parseDateStrictly(dateTime, pattern);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
		
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(parseDate("2016-03-01T08:44:59Z", "yyyy-MM-dd HH:mm:ss","yyyy-MM-dd'T'HH:mm:ss'Z'"));
	}

}
