package tw.com.mitac.thp.login2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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

public class WeiboLinkAction extends BasisFrontLoginAction {
	protected final String client_id() {
		return getSettingResource().get("weibo.oauth.id");
	}

	protected final String client_secret() {
		return getSettingResource().get("weibo.oauth.secret");
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
				+ OauthUtil.OAUTH_PATH + "/wbLinkRedirect2";
		return redirect_uri;
	}

	public String linkRedirect1() throws UnsupportedEncodingException {
		String referer = request.getHeader("referer");
		logger.debug("referer:" + referer);
		if (StringUtils.isNotBlank(referer))
			sessionSet("tempPage", referer);

		// Step1. code
		redirectPage = "https://api.weibo.com/oauth2/authorize";
		redirectPage += "?response_type=" + URLEncoder.encode("code", "UTF-8");
		redirectPage += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");
		redirectPage += "&client_id=" + URLEncoder.encode(client_id(), "UTF-8");

		return REDIRECT_PAGE;
	}

	public String linkRedirect2() throws UnsupportedEncodingException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step2. token
			String TOKEN_URL = "https://api.weibo.com" + "/oauth2/access_token";
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
				String access_token = "", uid = "";
				Integer expires = 0;
				JSONObject tokenResp = new JSONObject(tokenRespText);
				access_token = tokenResp.getString("access_token");
				expires = tokenResp.getInt("expires_in");
				uid = tokenResp.getString("uid");

				return linkRedirect2(access_token, uid);
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

	public String linkRedirect2(String access_token, String uid) throws ClientProtocolException {
		String oauthId = uid;
		List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(new QueryRule(
				"oauthId", oauthId)), new QueryOrder[0], null, null);
		if (memberList.size() > 0) {
			// 已註冊
			addActionError("綁定失敗:此微博帳號已綁定在其他帳號!");
			return SUCCESS;
		} else {
			Map<String, Object> setMap = getUpdatePropertyMap();
			setMap.put("oauthId", oauthId);
			setMap.put("oauthType", OauthUtil.OAUTH_TYPE_W);
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