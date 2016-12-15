package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEmailHistory;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreTheme;
import tw.com.mitac.thp.bean.HpsCoreThemeItem;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

public class HpsCoreThemeAction extends BasisCrudAction<HpsCoreTheme> {

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSysid())) {
			List<CpsEntity> entitys = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(new QueryRule("dataId",
					"hps")), new QueryOrder[0], null, null);
			if (entitys.size() > 0)
				bean.setEntitySysid(entitys.get(0).getSysid());
		}

		String result = super.edit();
		return result;
	}

	@Override
	protected boolean executeSave() {
		// 檢核
		String msg = preCheck();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}

		return super.executeSave();
	}

	// 檢核
	protected String preCheck() {
		if (bean.getEndDate().compareTo(bean.getStartDate()) < 0)
			return "「結束日期」不可小於「開始日期」";
		if (bean.getApplyEndDate().compareTo(bean.getApplyStartDate()) < 0)
			return "「廠商申請結束日期」不可小於「廠商申請起始日期」";
		if (bean.getApplyEndDate().compareTo(bean.getEndDate()) > 0)
			return "「廠商申請結束日期」不可大於「結束日期」";
		return SUCCESS;
	}

	public String sendEmail() {
		logger.info("寄送EMAIL");
		String emailTemplateSysid = beaninfo.get("emailTemplateSysid");
		CpsEmailTemplate emailTemplate = createDataTable(CpsEmailTemplate.class).get(emailTemplateSysid);
		if (emailTemplate == null) {
			addActionError("電子郵件範本錯誤");
			return EDIT_ERROR;
		}

		// 寄送EMAIL
		String emailContent = emailTemplate.getEmailContent();
		int count = 0;
		for (CpsVendor cpsVendor : getDataCpsVendorTable().values()) {
			// TODO 以後健康館會切開
			String vendorEmail = "";// cpsVendor.getVendorEmail();
			String name = cpsVendor.getName();
			if (StringUtils.isBlank(vendorEmail))
				continue;
			logger.info("email:" + vendorEmail);
			// 替換字符
			String content = emailContent.replace("$VENDOR_NAME$", name);

			CpsEmailHistory history = new CpsEmailHistory();
			Util.defaultPK(history);
			defaultValue(history);
			if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				history.setEntitySysid(getUserAccount().getSourceSysid());
			} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				history.setVendorSysid(getUserAccount().getSourceSysid());
			}
			history.setEmailAddress(vendorEmail);
			history.setEmailTitle(emailTemplate.getEmailTitle());
			history.setEmailContent(content);
			history.setRemark(history.getCreationDate());
			String daoMsg = cloudDao.save(sf(), history);
			if (!SUCCESS.equals(daoMsg)) {
				addActionError(daoMsg);
				return EDIT_ERROR;
			}

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add(content);
			new MailThread(new MailBean(vendorEmail, emailTemplate.getEmailTitle(), contentStringList),
					getSendMailSetting()).start();
			count++;
		}

		addActionMessage(count + "封信件已發送");
		return EDIT;
	}

	public String sendEmail2() {
		logger.info("寄送EMAIL2");
		String emailTemplateSysid = beaninfo.get("emailTemplateSysid");
		CpsEmailTemplate emailTemplate = createDataTable(CpsEmailTemplate.class).get(emailTemplateSysid);
		if (emailTemplate == null) {
			addActionError("電子郵件範本錯誤");
			return EDIT_ERROR;
		}
		List<HpsCoreThemeItem> items = cloudDao.queryTable(sf(), HpsCoreThemeItem.class, new QueryGroup(new QueryRule(
				"themeSysid", bean.getSysid()), new QueryRule(BILL_STATUS, BillStatusUtil.UNAPPROVED)),
				new QueryOrder[0], null, null);
		if (items.size() == 0) {
			addActionError("尚無可發送EMAIL資料");
			return EDIT_ERROR;
		}

		// 寄送EMAIL
		String emailContent = emailTemplate.getEmailContent();
		int count = 0;
		for (HpsCoreThemeItem item : items) {
			CpsVendor cpsVendor = createDataTable(CpsVendor.class).get(item.getVendorSysid());
			// TODO 以後健康館會切開
			String vendorEmail = "";// cpsVendor.getVendorEmail();
			String name = cpsVendor.getName();
			if (StringUtils.isBlank(vendorEmail))
				continue;
			logger.info("email:" + vendorEmail);
			// 替換字符
			String content = emailContent.replace("$VENDOR_NAME$", name);

			CpsEmailHistory history = new CpsEmailHistory();
			Util.defaultPK(history);
			defaultValue(history);
			if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				history.setEntitySysid(getUserAccount().getSourceSysid());
			} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				history.setVendorSysid(getUserAccount().getSourceSysid());
			}
			history.setEmailAddress(vendorEmail);
			history.setEmailTitle(emailTemplate.getEmailTitle());
			history.setEmailContent(content);
			history.setRemark(history.getCreationDate());
			String daoMsg = cloudDao.save(sf(), history);
			if (!SUCCESS.equals(daoMsg)) {
				addActionError(daoMsg);
				return EDIT_ERROR;
			}

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add(content);
			new MailThread(new MailBean(vendorEmail, emailTemplate.getEmailTitle(), contentStringList),
					getSendMailSetting()).start();
			count++;
		}

		addActionMessage(count + "封信件已發送");
		return EDIT;
	}
}