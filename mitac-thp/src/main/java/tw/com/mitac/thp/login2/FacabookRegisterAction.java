package tw.com.mitac.thp.login2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.OauthUtil;
import tw.com.mitac.thp.util.Util;

public class FacabookRegisterAction extends LoginAction {
	protected final String client_id() {
		return getSettingResource().get("facabook.oauth.id");
	}

	protected final String client_secret() {
		return getSettingResource().get("facabook.oauth.secret");
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
				+ OauthUtil.OAUTH_PATH + "/fbRegisterRedirect2";
		return redirect_uri;
	}

	// ---------- ---------- ---------- ---------- ----------
	public String registerRedirect1() throws UnsupportedEncodingException {
		session.put("oauthCase", "A");
		// Step1. code

		redirectPage = "https://www.facebook.com/dialog/oauth";
		redirectPage += "?client_id=" + URLEncoder.encode(client_id(), "UTF-8");
		redirectPage += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");
		redirectPage += "&scope=" + URLEncoder.encode("email public_profile user_birthday", "UTF-8");
		return REDIRECT_PAGE;
	}

	public String linkRedirect1() throws UnsupportedEncodingException {
		String referer = request.getHeader("referer");
		logger.debug("referer:" + referer);
		if (StringUtils.isNotBlank(referer))
			sessionSet("tempPage", referer);

		session.put("oauthCase", "B");
		// Step1. code

		redirectPage = "https://www.facebook.com/dialog/oauth";
		redirectPage += "?client_id=" + URLEncoder.encode(client_id(), "UTF-8");
		redirectPage += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");
		// redirectPage += "&scope=" +
		// URLEncoder.encode("email public_profile user_birthday", "UTF-8");
		return REDIRECT_PAGE;
	}

	// ---------- ---------- ---------- ---------- ----------
	public String registerRedirect2() throws UnsupportedEncodingException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step2. token
			String TOKEN_URL = "https://graph.facebook.com" + "/oauth/access_token";
			String tokenUrl = TOKEN_URL;
			tokenUrl += "?code=" + URLEncoder.encode(code, "UTF-8");
			tokenUrl += "&client_id=" + URLEncoder.encode(client_id(), "UTF-8");
			tokenUrl += "&client_secret=" + URLEncoder.encode(client_secret(), "UTF-8");
			tokenUrl += "&redirect_uri=" + URLEncoder.encode(getRedirectUri(), "UTF-8");

			HttpPost httpPost = new HttpPost(tokenUrl);

			HttpResponse httpResponse1 = closeableHttpClient.execute(httpPost);
			if (httpResponse1.getStatusLine().getStatusCode() == 200 && httpResponse1.getEntity() != null) {
				String tokenRespText = EntityUtils.toString(httpResponse1.getEntity());
				String access_token = "", expires = "";
				String[] arr = tokenRespText.split("&");
				for (String string : arr) {
					if (string.startsWith("access_token="))
						access_token = string.replace("access_token=", "");
					else if (string.startsWith("expires="))
						// access_token 可以存活的秒數
						expires = string.replace("expires=", "");
				}

				String oauthCase = (String) session.remove("oauthCase");
				if ("A".equalsIgnoreCase(oauthCase))
					return registerRedirect2(access_token);
				if ("B".equalsIgnoreCase(oauthCase))
					return linkRedirect2(access_token);
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

	public String registerRedirect2(String access_token) throws ClientProtocolException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step3. data
			String dataUrl = "https://graph.facebook.com/me" + "?access_token=" + access_token;

			dataUrl += "&fields=id,name,first_name,last_name,email,locale,picture,gender" + ",birthday";

			HttpGet httpGet = new HttpGet(dataUrl);

			HttpResponse httpResponse2 = closeableHttpClient.execute(httpGet);
			if (httpResponse2.getStatusLine().getStatusCode() == 200 && httpResponse2.getEntity() != null) {
				String entity = EntityUtils.toString(httpResponse2.getEntity());
				JSONObject dataResp = new JSONObject(entity);

				String oauthId = dataResp.getString("id");
				List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(
						new QueryRule("oauthId", oauthId)), new QueryOrder[0], null, null);
				if (memberList.size() == 0) {
					bean = new CpsSiteMember();
					bean.setOauthId(oauthId);
					bean.setOauthType(OauthUtil.OAUTH_TYPE_F);

					bean.setMemberName(dataResp.getString("name"));
					bean.setFirstName(dataResp.getString("first_name"));
					bean.setLastName(dataResp.getString("last_name"));

					bean.setEmail(dataResp.getString("email"));

					// String language = dataResp.getString("locale");
					// if (languageTypeMap.get(language) == null)
					// language = "en_US";
					// Cookie cookie = new Cookie("language",
					// StringUtils.defaultString(language));
					// cookie.setMaxAge(60 * 60 * 24 * 365);
					// response.addCookie(cookie);
					// bean.setLanguageType(language);

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

					// facebook可以拿生日
					try {
						String birthdayStr = dataResp.getString("birthday");
						DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
						Date birthday = df.parse(birthdayStr);
						bean.setBirthYear(new SimpleDateFormat("yyyy").format(birthday));
						bean.setBirthMonth(new SimpleDateFormat("MM").format(birthday));
						bean.setBirthDate(new SimpleDateFormat("dd").format(birthday));
					} catch (Exception e) {
						e.printStackTrace();
					}

					String oauthImg = dataResp.getJSONObject("picture").getJSONObject("data").getString("url");
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

	public String linkRedirect2(String access_token) throws ClientProtocolException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			// Step3. data
			String dataUrl = "https://graph.facebook.com/me" + "?access_token=" + access_token;

			// dataUrl +=
			// "&fields=id,name,first_name,last_name,email,locale,picture,gender"
			// + ",birthday";

			HttpGet httpGet = new HttpGet(dataUrl);

			HttpResponse httpResponse2 = closeableHttpClient.execute(httpGet);
			if (httpResponse2.getStatusLine().getStatusCode() == 200 && httpResponse2.getEntity() != null) {
				String entity = EntityUtils.toString(httpResponse2.getEntity());
				JSONObject dataResp = new JSONObject(entity);

				String oauthId = dataResp.getString("id");
				List<CpsSiteMember> memberList = cloudDao.queryTable(sf(), CpsSiteMember.class, new QueryGroup(
						new QueryRule("oauthId", oauthId)), new QueryOrder[0], null, null);
				if (memberList.size() > 0) {
					// 已註冊
					addActionError("綁定失敗:此Facebook帳號已綁定在其他帳號!");
					return SUCCESS;
				} else {
					Map<String, Object> setMap = getUpdatePropertyMap();
					setMap.put("oauthId", oauthId);
					setMap.put("oauthType", OauthUtil.OAUTH_TYPE_F);
					String daoMsg = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(),
							new QueryGroup(new QueryRule(PK, ((UserData2) session.get("userData2")).getAccount()
									.getSysid())), setMap));
					if (!SUCCESS.equals(daoMsg)) {
						addActionError(daoMsg);
						return SUCCESS;
					}
					redirectPage = (String) sessionGet("tempPage");
					logger.debug("redirectPage:" + redirectPage);
					return REDIRECT_PAGE;
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