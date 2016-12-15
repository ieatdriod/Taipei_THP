package tw.com.mitac.thp.action;

// Generated Mon Apr 25 14:31:10 CST 2016 by GenCode.java

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsSmsHistory;
import tw.com.mitac.thp.bean.CpsSmsSendDetail;
import tw.com.mitac.thp.bean.CpsSmsTemplate;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsVendorMember;
import tw.com.mitac.thp.util.Util;

/**
 * CpsSmsTemplateAction generated by GenCode.java
 */
public class CpsSmsTemplateAction extends BasisCrudAction<CpsSmsTemplate> {

	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return QueryGroup.DEFAULT;
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	public String main() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			addActionMessage("管理者可以查詢所有項目");
		}
		return super.main();
	}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSysid())) {
			CpsMember user = getUserAccount();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setEntitySysid(user.getSourceSysid());
				bean.setVendorSysid("*");
			} else if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				List<CpsEntity> entitys = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(new QueryRule(
						"dataId", "hps")), new QueryOrder[0], null, null);
				if (entitys.size() > 0)
					bean.setEntitySysid(entitys.get(0).getSysid());
				bean.setVendorSysid(user.getSourceSysid());
			}

		}

		String result = super.edit();
		return result;
	}

	@Override
	protected boolean executeSave() {

		// 一封簡訊長度上限：純英數160個字,中英混和70個字
		int l = bean.getSmsMessage().length();
		int l2 = bean.getSmsMessage().getBytes().length;
		int maxLength = 160;
		if (l != l2)
			maxLength = 70;
		if (l > maxLength) {
			addActionError("超過一封簡訊的長度（純英數160個字，中英混和70個字）");
			return false;
		}

		return super.executeSave();
	}

	// 寄送sms
	public String sendSms() {
		String result = SUCCESS;
		try {
			// 先存檔,避免user更改後未存檔直接按發送
			if (EDIT_ERROR.equals(save())) {
				addActionError("儲存時發生錯誤");
				return EDIT_ERROR;
			}

			// 找出所有會員
			CpsMember user = getUserAccount();
			List<Map> mMap = new ArrayList<Map>();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType()))
				mMap = (List<Map>) cloudDao.findProperty(sf(), CpsMember.class, new QueryGroup(new QueryRule(
						"isEnabled", true)), new QueryOrder[0], false, PK, "mobilePhone");
			if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				mMap = (List<Map>) cloudDao.findProperty(sf(), CpsMember.class, new QueryGroup(new QueryRule(
						"isEnabled", true), new QueryRule(PK, IN, getMemberIn())), new QueryOrder[0], false, PK,
						"mobilePhone");
			}
			Set<CpsSmsSendDetail> dSet = new LinkedHashSet<CpsSmsSendDetail>();
			for (Map m : mMap) {
				String num = (String) m.get("mobilePhone");
				if (StringUtils.isNotBlank(num)) {
					CpsSmsSendDetail d = new CpsSmsSendDetail();
					Util.defaultPK(d);
					// defaultValue(d);
					// d.setParentSysid(bean.getSysid());
					d.setMemberSysid((String) m.get(PK));
					d.setSmsTel(num);
					dSet.add(d);
				}
			}

			// TODO 發送簡訊（等介接）
			// ...
			if (!SUCCESS.equals(result)) {
				addActionError("發送簡訊時發生錯誤");
				return EDIT_ERROR;
			}

			// 每一筆尾檔都寫入記錄
			for (CpsSmsSendDetail detail : dSet) {
				CpsSmsHistory smsHistory = new CpsSmsHistory();
				Util.defaultPK(smsHistory);
				defaultValue(smsHistory);
				smsHistory.setEntitySysid(bean.getEntitySysid());
				smsHistory.setVendorSysid(bean.getVendorSysid());
				smsHistory.setSmsTel(detail.getSmsTel());
				smsHistory.setSmsMessage(bean.getSmsMessage());
				smsHistory.setSmsDateTime(systemDatetime);
				saveList.add(smsHistory);
			}

			result = super.save();
			if (EDIT_ERROR.equals(result)) {
				addActionError("儲存記錄時發生錯誤");
				return EDIT_ERROR;
			}
			clearMessages();
			addActionMessage("系統發送成功，寄送" + dSet.size() + "封簡訊");
		} catch (Exception e) {
			e.printStackTrace();
			addActionError("發生例外狀況");
			return EDIT_ERROR;
		}
		return result;
	}

	public String getMemberIn() {
		String result = (String) sessionGet("memberIN");
		if (result == null) {
			CpsMember user = getUserAccount();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				result = "";
			} else {
				List<String> vm = (List<String>) cloudDao.findProperty(sf(), HpsVendorMember.class, new QueryGroup(
						new QueryRule("vendorSysid", user.getSourceSysid())), new QueryOrder[0], false, "memberSysid");
				result = "x";
				for (String s : vm)
					result += "," + s;
			}
			sessionSet("memberIN", result);
		}
		return result;
	}
}