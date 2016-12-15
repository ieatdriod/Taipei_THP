package tw.com.mitac.thp.util;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class MeetingApi {
	private static String serverUrl = "https://administrator:admin123@211.78.240.180:8181";
	private static String url = serverUrl + "/api/v2/rest/service/accounts";
	private static String accessToken = "?access_token=997d347d2a299b33";
	private static volatile int accessTokenCnt = 995;
	
	private static String managerAc = "administrator";
	private static String managerPw = "admin123";
	
	
	public Map<String, Object> getMeetingRoom(Map<String, String> params) {
		//TODO
		return null;
	}
	
	public Map<String, Object> getMeetingAccount(Map<String, String> params) {
		//TODO
		return null;
	}
	
	public Map<String, String> getMeetingAccounts() {
		return execVideoServerGet(url + getAccessToken());
	}	
	
	public Map<String, String> modifyMeetingRoom(String roomId, Map<String, String> params) {
		return modifyMeetingAccount(roomId, params);
	}
	
	public Map<String, String> addMeetingRoom(Map<String, String> params) {
		params.put("type", "Meeting");
		params.put("usageType", "Personal");
		params.put("enabled", "true");
		filterNovalue(params);
		System.out.println("params=" + params);
		return execVideoServerPost(url + getAccessToken(), params);
		
	}
	
	public Map<String, String> deleteMeetingRoom(String roomId) {
		return deleteMeetingAccount(roomId);
	}
	
	public Map<String, String> addMeetingAccount(Map<String, String> params) {
		//TODO
		return null;
	}
	
	public Map<String, String> modifyMeetingAccount(String userId, Map<String, String> params) {
		filterNovalue(params);
		System.out.println("params=" + params);
		return execVideoServerPost(url + "/" + userId + getAccessToken(), params);
	}
	
	public Map<String, String> deleteMeetingAccount(String accountId) {
		String dUrl = url + "/" + accountId + getAccessToken();
		return execVideoServerDelete(dUrl);
	}
	
	private String getAccessToken() {
		
		try {
			if (accessTokenCnt >= 995) {
				
				synchronized(this) {
					if (accessTokenCnt >= 995) {
						System.out.println("Reset accessTokenCnt...");
						Map<String, String> map = execVideoServerGet(serverUrl + String.format("/api/v1/access-token/?grant_type=password&username=%s&password=%s", managerAc, managerPw));
						System.out.println(map);
						JSONObject json = new JSONObject(map.get("R"));
						accessToken = "?access_token=" + json.get("access_token").toString();
						accessTokenCnt = 1;						
					} else {
						accessTokenCnt++;
					}
				}
			} else {
				accessTokenCnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}	
	
	private Map<String, String> execVideoServerPost(String vUrl, Map<String, String> params) {
		HttpClient client = null;
		StringEntity entity = null;
		Map<String, String> rtnMap = new HashMap<String, String>();
		try {
			client = createHttpClient_SSL();
			HttpPost post = new HttpPost(vUrl);
			
			post.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
			post.addHeader("Content-type", "application/json");
			
			JSONObject json = new JSONObject(params);

			entity = new StringEntity(json.toString(), "UTF-8");
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			
			int responseCode = response.getStatusLine().getStatusCode();
			
			if (responseCode == HttpStatus.SC_CREATED) {
				String msg = String.format("ResponseCode:%d; ReasonPhrase:%s",
						responseCode,
						response.getStatusLine().getReasonPhrase());
				rtnMap.put("S", msg);
			} else {
				String msg = String.format("ResponseCode:%d; ReasonPhrase:%s; Content:%s",
						responseCode,
						response.getStatusLine().getReasonPhrase(),
						EntityUtils.toString(response.getEntity()));
				rtnMap.put("F", msg);
			}

		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			rtnMap.put("F", ue.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			rtnMap.put("F", e.getMessage());
		} finally {
			try {
				if (entity != null) {
					entity.getContent().close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rtnMap;		
		
	}
	
	private Map<String, String> execVideoServerGet(String vUrl) {

		Map<String, String> rtnMap = new HashMap<String, String>();
		try {
			HttpClient client = createHttpClient_SSL();
			
			HttpGet get = new HttpGet(vUrl);

			get.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
			get.addHeader("Content-type", "application/json");
			
			HttpResponse response = client.execute(get);
			
			int responseCode = response.getStatusLine().getStatusCode();
			
			if (responseCode == HttpStatus.SC_OK) {
				String msg = String.format("ResponseCode:%d; ReasonPhrase:%s",
						responseCode,
						response.getStatusLine().getReasonPhrase());
				//System.out.println("response=" + EntityUtils.toString(response.getEntity()));
				//System.out.println("msg=" + msg);
				
				rtnMap.put("S", msg);
				rtnMap.put("R", EntityUtils.toString(response.getEntity()));
			} else {
				String msg = String.format("ResponseCode:%d; ReasonPhrase:%s; Content:%s",
						responseCode,
						response.getStatusLine().getReasonPhrase(),
						EntityUtils.toString(response.getEntity()));
				rtnMap.put("F", msg);
			}

		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			rtnMap.put("F", ue.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			rtnMap.put("F", e.getMessage());
		} finally {

		}
		
		return rtnMap;				
		
	}
	
	private Map<String, String> execVideoServerDelete(String vUrl) {
		
		HttpClient client = null;
		Map<String, String> rtnMap = new HashMap<String, String>();
		try {
			client = createHttpClient_SSL();
			
			HttpDelete del = new HttpDelete(vUrl);
			
			del.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
			del.addHeader("Content-type", "application/json");
			

			HttpResponse response = client.execute(del);
			
			int responseCode = response.getStatusLine().getStatusCode();
			
			System.out.println("ResponseCode:" + responseCode);
			System.out.println("ReasonPhrase:" + response.getStatusLine().getReasonPhrase());
			
			if (responseCode == HttpStatus.SC_NO_CONTENT) {
				String msg = String.format("ResponseCode:%d; ReasonPhrase:%s",
						responseCode,
						response.getStatusLine().getReasonPhrase());
				rtnMap.put("S", msg);
			} else {
				String msg = String.format("ResponseCode:%d; ReasonPhrase:%s; Content:%s",
						responseCode,
						response.getStatusLine().getReasonPhrase(),
						EntityUtils.toString(response.getEntity()));
				rtnMap.put("F", msg);
			}

		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			rtnMap.put("F", ue.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			rtnMap.put("F", e.getMessage());
		} finally {

		}
		
		return rtnMap;
	}	
	
	private void filterNovalue(Map<String, String> map) {
		
		Iterator<String> it = map.keySet().iterator();
		
		while (it.hasNext()) {
			String key = it.next();
			if (map.get(key) == null || "".equals(map.get(key))) {
				it.remove();
			}			
		}
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
