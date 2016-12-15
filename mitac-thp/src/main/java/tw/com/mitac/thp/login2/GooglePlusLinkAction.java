package tw.com.mitac.thp.login2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.action.BasisFrontLoginAction;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.OauthUtil;

public class GooglePlusLinkAction extends BasisFrontLoginAction {
	protected final String client_id() {
		return getSettingResource().get("google.oauth.id");
	}

	protected final String client_secret() {
		return getSettingResource().get("google.oauth.secret");
	}

	protected String code;

	public final String getCode() {
		return code;
	}

	public final void setCode(String code) {
		this.code = code;
	}

	protected String getRedirectUri() {
		String redirect_uri = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath() + "/"
				+ OauthUtil.OAUTH_PATH + "/linkRedirect2";
		return redirect_uri;
	}

	public String linkRedirect1() throws UnsupportedEncodingException {
		String referer = request.getHeader("referer");
		logger.debug("referer:" + referer);
		if (StringUtils.isNotBlank(referer))
			sessionSet("tempPage", referer);

		// Step1. code
		redirectPage = "https://accounts.google.com/o/oauth2/auth";
		redirectPage += "?scope=" + URLEncoder.encode("profile", "UTF-8");
		redirectPage += "&response_type=" + URLEncoder.encode("code", "UTF-8");
		redirectPage += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");
		redirectPage += "&access_type=" + URLEncoder.encode("offline", "UTF-8");
		redirectPage += "&approval_prompt=" + URLEncoder.encode("force", "UTF-8");
		redirectPage += "&client_id=" + URLEncoder.encode(client_id(), "UTF-8");
		return REDIRECT_PAGE;
	}

	public String linkRedirect2() throws UnsupportedEncodingException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step2. token
			String TOKEN_URL = "https://www.googleapis.com" + "/oauth2/v4/token";
			String tokenUrl = TOKEN_URL;
			tokenUrl += "?code=" + URLEncoder.encode(code, "UTF-8");
			tokenUrl += "&client_id=" + URLEncoder.encode(client_id(), "UTF-8");
			tokenUrl += "&client_secret=" + URLEncoder.encode(client_secret(), "UTF-8");
			tokenUrl += "&grant_type=" + URLEncoder.encode("authorization_code", "UTF-8");
			tokenUrl += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");

			HttpPost httpPost = new HttpPost(tokenUrl);

			HttpResponse httpResponse1 = closeableHttpClient.execute(httpPost);
			if (httpResponse1.getStatusLine().getStatusCode() == 200 && httpResponse1.getEntity() != null) {
				String tokenRespText = EntityUtils.toString(httpResponse1.getEntity());
				// logger.debug("tokenRespText:" + tokenRespText);
				String access_token = "";
				Integer expires = 0;
				JSONObject tokenResp = new JSONObject(tokenRespText);
				access_token = tokenResp.getString("access_token");
				expires = tokenResp.getInt("expires_in");

				// Step3. data
				String dataUrl = "https://www.googleapis.com/plus/v1/people/me?access_token=" + access_token;

				HttpGet httpGet = new HttpGet(dataUrl);
				System.out.println(httpGet.getRequestLine());

				HttpResponse httpResponse2 = closeableHttpClient.execute(httpGet);
				if (httpResponse2.getStatusLine().getStatusCode() == 200 && httpResponse2.getEntity() != null) {
					String entity = EntityUtils.toString(httpResponse2.getEntity());
					JSONObject dataResp = new JSONObject(entity);
					// System.out.println(dataResp);

					String oauthId = dataResp.getString("id");
					List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(
							new QueryRule("oauthId", oauthId)), new QueryOrder[0], null, null);
					if (memberList.size() > 0) {
						// 已註冊
						addActionError("綁定失敗:此Google+帳號已綁定在其他帳號!");
						return SUCCESS;
					} else {
						Map<String, Object> setMap = getUpdatePropertyMap();
						setMap.put("oauthId", oauthId);
						setMap.put("oauthType", OauthUtil.OAUTH_TYPE_G);
						String daoMsg = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(),
								new QueryGroup(new QueryRule(PK, getUserData2().getAccount().getSysid())), setMap));
						if (!SUCCESS.equals(daoMsg)) {
							addActionError(daoMsg);
							return SUCCESS;
						}
						redirectPage = (String) sessionGet("tempPage");
						logger.debug("redirectPage:" + redirectPage);
						return REDIRECT_PAGE;
					}
				}
			}
		} catch (IOException e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				// 关闭流并释放资源
				closeableHttpClient.close();
			} catch (IOException e) {
				addActionError(e.getClass() + ":" + e.getMessage());
				e.printStackTrace();
			}
		}
		return SUCCESS;
	}
}