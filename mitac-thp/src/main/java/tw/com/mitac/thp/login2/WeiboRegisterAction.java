package tw.com.mitac.thp.login2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.OauthUtil;
import tw.com.mitac.thp.util.Util;

public class WeiboRegisterAction extends LoginAction {
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

	protected CpsSiteMember bean;

	public final CpsSiteMember getBean() {
		return bean;
	}

	public final void setBean(CpsSiteMember bean) {
		this.bean = bean;
	}

	protected String getRedirectUri() {
		String redirect_uri = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath() + "/"
				+ OauthUtil.OAUTH_PATH + "/wbRegisterRedirect2";
		return redirect_uri;
	}

	public String registerRedirect1() throws UnsupportedEncodingException {
		// Step1. code
		redirectPage = "https://api.weibo.com/oauth2/authorize";
		redirectPage += "?response_type=" + URLEncoder.encode("code", "UTF-8");
		redirectPage += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");
		redirectPage += "&client_id=" + URLEncoder.encode(client_id(), "UTF-8");

		return REDIRECT_PAGE;
	}

	public String registerRedirect2() throws UnsupportedEncodingException {
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

				return registerRedirect2(access_token, uid);
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

	public String registerRedirect2(String access_token, String uid) throws ClientProtocolException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step3. data
			String dataUrl = "https://api.weibo.com/2/users/show.json";
			dataUrl += "?uid=" + uid;

			HttpGet httpGet = new HttpGet(dataUrl);
			httpGet.setHeader("Authorization", ("OAuth2 " + access_token));
			String ipStr = request.getHeader("host");
			logger.debug("API-RemoteIP:" + ipStr);
			httpGet.setHeader("API-RemoteIP", ipStr);
			logger.debug("RequestLine:" + httpGet.getRequestLine());

			HttpResponse httpResponse2 = closeableHttpClient.execute(httpGet);
			// logger.debug("StatusCode:" +
			// httpResponse2.getStatusLine().getStatusCode());
			if (httpResponse2.getStatusLine().getStatusCode() == 200 && httpResponse2.getEntity() != null) {
				String entity = EntityUtils.toString(httpResponse2.getEntity());
				JSONObject dataResp = new JSONObject(entity);
				logger.debug(dataResp);

				String oauthId = dataResp.getString("idstr");// XXX 使用uid更快
				List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(
						new QueryRule("oauthId", oauthId)), new QueryOrder[0], null, null);
				if (memberList.size() == 0) {
					bean = new CpsSiteMember();
					bean.setOauthId(oauthId);
					bean.setOauthType(OauthUtil.OAUTH_TYPE_W);

					bean.setMemberName(dataResp.getString("name"));
					bean.setFirstName(dataResp.getString("name"));

					String gender = "";
					if (dataResp.has("gender")) {
						String _gender = dataResp.getString("gender");
						if ("m".equalsIgnoreCase(_gender))
							gender = "MAN";
						else if ("f".equalsIgnoreCase(_gender))
							gender = "WOMAN";
					}
					bean.setGender(gender);

					if (dataResp.has("profile_image_url")) {
						String oauthImg = dataResp.getString("profile_image_url");
						request.setAttribute("oauthImg", oauthImg);
					}

					{
						// email
						String dataUrl_email = "https://api.weibo.com/2/account/profile/email.json";
						dataUrl_email += "?access_token=" + access_token;
						httpGet = new HttpGet(dataUrl_email);

						httpGet.setHeader("Authorization", ("OAuth2 " + access_token));
						httpGet.setHeader("API-RemoteIP", ipStr);
						logger.debug("RequestLine:" + httpGet.getRequestLine());

						httpResponse2 = closeableHttpClient.execute(httpGet);
						logger.debug("httpResponse2.getStatusLine().getStatusCode():"
								+ httpResponse2.getStatusLine().getStatusCode());
						if (httpResponse2.getStatusLine().getStatusCode() == 200 && httpResponse2.getEntity() != null) {
							String entity_email = EntityUtils.toString(httpResponse2.getEntity());
							logger.debug("entity_email:" + entity_email);
							JSONArray dataResp_email = new JSONArray(entity_email);
							logger.debug(dataResp_email);

							for (int i = 0; i < dataResp_email.length(); i++) {
								JSONObject obj = dataResp_email.getJSONObject(i);
								String email = obj.getString("email");
								if (StringUtils.isNotBlank(email)) {
									logger.debug("email:" + email);
									bean.setEmail(email);
									bean.setUuid(bean.getEmail());
									break;
								}
							}
						} else {
							String entity_email = EntityUtils.toString(httpResponse2.getEntity());
							logger.warn("entity_email:" + entity_email);
						}
					}

					logger.debug("bean:" + ReflectionToStringBuilder.toString(bean, ToStringStyle.MULTI_LINE_STYLE));
					if (StringUtils.isBlank(bean.getUuid()))
						return "register";

					{
						Util.defaultPK(bean);
						defaultValue(bean);
						bean.setPassword("");
						bean.setIsEnabled(true);
						if (StringUtils.isBlank(bean.getGender()))
							bean.setGender("NONE");
						bean.setRegisterDate(systemDate);
						bean.setIsMtsEpaper(true);
						bean.setIsBhsEpaper(true);
						bean.setIsHpsEpaper(false);
						bean.setIsActivate(true);

						String birthdayStr = bean.getBirthYear() + "/" + bean.getBirthMonth() + "/"
								+ bean.getBirthDate();
						try {
							Date birthday = sdfYMD.parse(birthdayStr);
							bean.setBirthday(birthday);
						} catch (ParseException e) {
							// e.printStackTrace();
							// addActionError("出生年月日格式輸入錯誤，請以數字方式輸入");
							// return ERROR;
						}

						List<QueryRule> ruleList = new ArrayList<QueryRule>();
						ruleList.add(new QueryRule("uuid", bean.getUuid()));

						int count = cloudDao.queryTableCount(sf(), CpsSiteMember.class,
								new QueryGroup(ruleList.toArray(new QueryRule[0])));
						if (count > 0) {
							String uuid = bean.getUuid();
							bean.setUuid("");

							// for (int i = 1; i <= 9; i++) {
							// String uuid2 = uuid + i;
							// count = cloudDao.queryTableCount(sf(),
							// CpsSiteMember.class, new QueryGroup(
							// new QueryRule[] { new QueryRule("uuid", uuid2)
							// }));
							// if (count == 0) {
							// bean.setUuid(uuid2);
							// break;
							// }
							// }
						}
						if (StringUtils.isBlank(bean.getUuid())) {
							addActionError("此電子信箱已註冊，請登入後於會員中心進行綁定．");
							return SUCCESS;
						}

						logger.debug("bean:" + ReflectionToStringBuilder.toString(bean, ToStringStyle.MULTI_LINE_STYLE));
						String daoMsg = cloudDao.save(sf(), bean);
						if (!SUCCESS.equals(daoMsg)) {
							addActionError(daoMsg);
							return SUCCESS;
						}
						memberList.add(bean);
					}
				}

				if (memberList.size() > 0) {
					// 已註冊 繼續進行登入
					CpsSiteMember member = memberList.get(0);
					this.uid = member.getUuid();
					return login(true, 0);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流并释放资源
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return SUCCESS;
	}
}