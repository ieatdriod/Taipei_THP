package tw.com.mitac.thp.login2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
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

public class GooglePlusRegisterAction extends LoginAction {
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

	protected CpsSiteMember bean;

	public final CpsSiteMember getBean() {
		return bean;
	}

	public final void setBean(CpsSiteMember bean) {
		this.bean = bean;
	}

	protected String getRedirectUri() {
		String redirect_uri = request.getScheme() + "://" + request.getHeader("host") + request.getContextPath() + "/"
				+ OauthUtil.OAUTH_PATH + "/registerRedirect2";
		return redirect_uri;
	}

	public String registerRedirect1() throws UnsupportedEncodingException {
		// Step1. code
		redirectPage = "https://accounts.google.com/o/oauth2/auth";
		redirectPage += "?scope=" + URLEncoder.encode("email profile", "UTF-8");
		redirectPage += "&response_type=" + URLEncoder.encode("code", "UTF-8");
		redirectPage += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");
		redirectPage += "&access_type=" + URLEncoder.encode("offline", "UTF-8");
		redirectPage += "&approval_prompt=" + URLEncoder.encode("force", "UTF-8");
		redirectPage += "&client_id=" + URLEncoder.encode(client_id(), "UTF-8");
		return REDIRECT_PAGE;
	}

	public String registerRedirect2() throws UnsupportedEncodingException {
		logger.debug("registerRedirect2()");
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
			// logger.debug("requestLine:" + httpPost.getRequestLine());

			// 执行post请求
			HttpResponse httpResponse1 = closeableHttpClient.execute(httpPost);
			// // 获取响应消息实体
			// HttpEntity entity = httpResponse.getEntity();
			// // 响应状态
			// logger.debug("status:" + httpResponse1.getStatusLine());
			// // 判断响应实体是否为空
			// if (entity != null) {
			// System.out.println("contentEncoding:" +
			// entity.getContentEncoding());
			// System.out.println("response content:" +
			// EntityUtils.toString(entity));
			// }
			if (httpResponse1.getStatusLine().getStatusCode() == 200 && httpResponse1.getEntity() != null) {
				String tokenRespText = EntityUtils.toString(httpResponse1.getEntity());
				// logger.debug("tokenRespText:" + tokenRespText);
				String access_token = "";
				Integer expires = 0;
				JSONObject tokenResp = new JSONObject(tokenRespText);
				access_token = String.valueOf(tokenResp.get("access_token"));
				expires = tokenResp.getInt("expires_in");

				return registerRedirect2(access_token);
			} else {
				String entity = EntityUtils.toString(httpResponse1.getEntity());
				logger.warn("entity:" + entity);
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
		if (!hasActionErrors())
			addActionError("也請檢查secret是否正確");
		return SUCCESS;
	}

	protected String registerRedirect2(String access_token) throws UnsupportedEncodingException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step3. data
			String dataUrl = "https://www.googleapis.com/plus/v1/people/me" + "?access_token=" + access_token;

			HttpGet httpGet = new HttpGet(dataUrl);
			// logger.debug("requestLine:" + httpGet.getRequestLine());

			HttpResponse httpResponse2 = closeableHttpClient.execute(httpGet);
			// logger.debug("status:" + httpResponse2.getStatusLine());
			if (httpResponse2.getStatusLine().getStatusCode() == 200 && httpResponse2.getEntity() != null) {
				String entity = EntityUtils.toString(httpResponse2.getEntity());
				JSONObject dataResp = new JSONObject(entity);
				// logger.debug(dataResp);

				String oauthId = dataResp.getString("id");
				List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(
						new QueryRule("oauthId", oauthId)), new QueryOrder[0], null, null);
				if (memberList.size() == 0) {
					bean = new CpsSiteMember();
					bean.setOauthId(oauthId);
					bean.setOauthType(OauthUtil.OAUTH_TYPE_G);

					bean.setMemberName(dataResp.getString("displayName"));
					bean.setFirstName(dataResp.getJSONObject("name").getString("givenName"));
					bean.setLastName(dataResp.getJSONObject("name").getString("familyName"));

					JSONArray emails = dataResp.getJSONArray("emails");
					for (int i = 0; i < emails.length(); i++) {
						JSONObject email = emails.getJSONObject(i);
						if ("account".equals(email.getString("type")))
							bean.setEmail(email.getString("value"));
					}

					bean.setUuid(bean.getEmail());

					String gender = "";
					if (dataResp.has("gender")) {
						String _gender = dataResp.getString("gender");
						if ("male".equalsIgnoreCase(_gender))
							gender = "MAN";
						else if ("female".equalsIgnoreCase(_gender))
							gender = "WOMAN";
					}
					bean.setGender(gender);

					String oauthImg = dataResp.getJSONObject("image").getString("url");
					request.setAttribute("oauthImg", oauthImg);

					// logger.debug("bean:" +
					// ReflectionToStringBuilder.toString(bean,
					// ToStringStyle.MULTI_LINE_STYLE));
					// return "register";
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

						// logger.debug("bean:" +
						// ReflectionToStringBuilder.toString(bean,
						// ToStringStyle.MULTI_LINE_STYLE));
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
			} else {
				String entity = EntityUtils.toString(httpResponse2.getEntity());
				logger.warn("entity:" + entity);
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
		if (!hasActionErrors())
			addActionError("也請檢查Google+ API是否啟用");
		return SUCCESS;
	}
}