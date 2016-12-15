package tw.com.mitac.thp.action;

import java.text.SimpleDateFormat;

// Generated Tue Mar 15 14:23:28 CST 2016 by GenCode.java

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsQa;
import tw.com.mitac.thp.bean.CpsVendor;

/**
 * CpsQaAction generated by GenCode.java
 */
public class CpsQaAction extends DetailController<CpsQa> {

	/** MAIN頁面假欄位處理 */
	@Override
	protected Map<String, Map> getJqgridColModelMap() {
		Map<String, Map> jqgridColModelMap = super.getJqgridColModelMap();
		Map<String, Map> newMap = new LinkedHashMap<String, Map>();
		newMap.put("creationDateShow", fakeColModel("creationDateShow"));
		for (String key : jqgridColModelMap.keySet()) {
			newMap.put(key, jqgridColModelMap.get(key));
		}
		return newMap;
	}

	/** 處理MAIN頁面假欄位數值 */
	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<CpsQa> list = (List<CpsQa>) oldArr[1];
		List<Map> newResults = new ArrayList<Map>();
		for (CpsQa bean : list) {
			Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(bean);
			newResults.add(map);

			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date;
			try {
				date = sdFormat.parse(bean.getCreationDate());
				map.put("creationDateShow", date);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Object[] newArr = new Object[] { oldArr[0], newResults };
		return newArr;
	}

	/** 搜尋功能處理 */
	@Override
	public String main() {
		String result = super.main();
		if (getQueryCondition() == null) {
			beaninfo = new HashMap<String, String>();
			beaninfo.put("uc", "A");
			find();
		}
		return result;
	}

	protected QueryGroup createQueryCondition() {

		List<QueryRule> rules = new ArrayList<QueryRule>();

		logger.debug("開始時間：" + beaninfo.get("displayDateGE"));
		if (StringUtils.isNotBlank(beaninfo.get("displayDateGE"))) {
			Date displayDateGE = DateTypeConverter.convertFromString(beaninfo.get("displayDateGE"));
			if (displayDateGE != null) {
				rules.add(new QueryRule(CD, GE, sdf.format(displayDateGE)));
			}
		}

		logger.debug("結束時間" + beaninfo.get("displayDateLE"));
		if (StringUtils.isNotBlank(beaninfo.get("displayDateLE"))) {

			Date displayDateLE = DateTypeConverter.convertFromString(beaninfo.get("displayDateLE"));
			if (displayDateLE != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(displayDateLE);
				cal.add(Calendar.DATE, 1);
				displayDateLE = cal.getTime();
				rules.add(new QueryRule(CD, LE, sdf.format(displayDateLE)));
			}
		}

		// 狀態
		String uc = beaninfo.get("uc");
		if (StringUtils.isNotBlank(uc)) {
			if (beaninfo.get("uc").equals("A")) {
			} else {
				rules.add(new QueryRule("qaType", uc));
			}
		}

		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

	/** 框架權限顯示處理 */
	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("qaDepartment", IN, "CPS,BHS,MTS"));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	/** 自動填入回覆者資訊處理 */
	@Override
	public String edit() {
		String result = super.edit();
		if (StringUtils.isBlank(bean.getBackMemberSysid())) {
			CpsMember user = getUserAccount();
			bean.setBackMemberSysid(user.getSysid());
			beaninfo.put("backMemberSysidShow", user.getMemberName());
		}

		if (bean.getQaRedate() == null) {
			bean.setQaRedate(systemDatetime);
		}

		return result;
	}

	@Override
	protected boolean executeSave() {

		/**
		 * 回覆內容後狀態處理並且寄信回覆內容
		 */
		if (StringUtils.isNotBlank(bean.getQaRetext())) {

			bean.setQaType("C");

			List<CpsEmailTemplate> emailTemplateList = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(),
					CpsEmailTemplate.class, new QueryGroup(new QueryRule("emailId", "cpsQa_Retext")), new QueryOrder[0],
					null, null);

			if (emailTemplateList.size() == 1) {
				CpsEmailTemplate emailTemplate = emailTemplateList.get(0);
				String content = emailTemplate.getEmailContent();

				/** 客戶名稱 */
				if (StringUtils.isNotBlank(bean.getCreateName())) {
					content = content.replace("$CLIENT_NAME$", bean.getCreateName());
				}
				/** 客戶提問內容 */
				if (StringUtils.isNotBlank(bean.getQaText())) {
					content = content.replace("$CLIENT_QUESTIONS$", bean.getQaText());
				}

				/** 廠商回覆內容 */
				if (StringUtils.isNotBlank(bean.getQaRetext())) {
					content = content.replace("$REPLY$", bean.getQaRetext());
				}

				/** 廠商名稱 */
				if (bean.getQaDepartment().equals("CPS")) {
					content = content.replace("$VENDOR_NAME$", getText("web.node.cps"));
				} else if (bean.getQaDepartment().equals("MTS")) {
					content = content.replace("$VENDOR_NAME$", getText("web.node.mts"));
				} else if (bean.getQaDepartment().equals("BHS")) {
					content = content.replace("$VENDOR_NAME$", getText("web.node.bhs"));
				} else {
					List<CpsVendor> vendorList = (List<CpsVendor>) cloudDao.queryTable(sf(), CpsVendor.class,
							new QueryGroup(new QueryRule(PK, bean.getQaDepartment())), new QueryOrder[0], null, null);
					if (vendorList.size() > 0) {
						content = content.replace("$VENDOR_NAME$", vendorList.get(0).getName());
					}

				}

				List<String> cpsQa_qaRetextArray = new ArrayList<String>();
				cpsQa_qaRetextArray.add(content);

				try {
					new MailThread(new MailBean(bean.getEmail(), emailTemplate.getEmailTitle(), cpsQa_qaRetextArray),
							getSendMailSetting()).start();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		return super.executeSave();
	}
}