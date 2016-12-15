package tw.com.mitac.thp.action.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.email.MailBean;
import tw.com.mitac.email.MailThread;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsEmailHistory;
import tw.com.mitac.thp.bean.CpsEmailTemplate;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.tree.BeanTreeNode;
import tw.com.mitac.thp.tree.TreeUtil;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HpsInviteVendorAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "app/hpsInviteVendor";
	}

	protected String itemMsg() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<ul>");

			Map treeObj = new HashMap();
			treeObj.put("request", getDataHpsCoreItemTypeTable().values());
			List<BeanTreeNode> rootList = TreeUtil.treeMachine(treeObj, "parentItemTypeSysid", PK);

			Map<String, Integer> levelRecords = (Map<String, Integer>) treeObj.get("levelRecords");
			Map<String, BeanTreeNode> beanTreeNodeMap = (Map<String, BeanTreeNode>) treeObj.get("beanTreeNodeMap");
			for (BeanTreeNode beanTreeNode : rootList) {
				itemMsg(treeObj, beanTreeNode, sb);
			}

			sb.append("</ul>");
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	protected void itemMsg(Map treeObj, BeanTreeNode<HpsCoreItemType> beanTreeNode, StringBuilder sb) {
		HpsCoreItemType bean = beanTreeNode.getBean();
		sb.append("<li>");
		sb.append(bean.getName() + ":" + bean.getAllotRate());

		sb.append("<ul>");
		for (BeanTreeNode<HpsCoreItemType> subBeanTreeNode : beanTreeNode.getSub()) {
			itemMsg(treeObj, subBeanTreeNode, sb);
		}
		sb.append("</ul>");

		sb.append("</li>");
	}

	public String hpsInviteVendor() {
		List<CpsEmailTemplate> l = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(), CpsEmailTemplate.class,
				new QueryGroup(new QueryRule("emailId", "hpsInviteVendor")), new QueryOrder[0], null, null);
		if (l.size() > 0) {
			CpsEmailTemplate cpsEmailTemplate = l.get(0);
			request.setAttribute("cpsEmailTemplate", cpsEmailTemplate);
			String emailContent = cpsEmailTemplate.getEmailContent();
			emailContent = emailContent.replace("$ITEM_MSG$", itemMsg());
			request.setAttribute("emailContent", emailContent);
		}
		return SUCCESS;
	}

	public String hpsInviteVendorSend1() {
		logger.info("通知既有廠商(所有廠商)");
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");
		List<CpsEmailTemplate> l = cloudDao.queryTable(sf(), CpsEmailTemplate.class, new QueryGroup(new QueryRule(
				"emailId", "hpsInviteVendor")), new QueryOrder[0], null, null);
		if (l.size() == 0) {
			resultMap.put("msg", "查無範本");
			return JSON_RESULT;
		}
		CpsEmailTemplate cpsEmailTemplate = l.get(0);
		String emailContent = cpsEmailTemplate.getEmailContent();
		emailContent = emailContent.replace("$ITEM_MSG$", itemMsg());

		int count = 0;
		for (CpsVendor cpsVendor : getDataCpsVendorTable().values()) {
			// String vendorEmail = cpsVendor.getVendorEmail();
			String vendorEmail = "raichu@mitac.com.tw";// FIXME
			String name = cpsVendor.getName();
			if (StringUtils.isBlank(vendorEmail))
				continue;
			logger.info("email:" + vendorEmail);
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
			history.setEmailTitle(cpsEmailTemplate.getEmailTitle());
			history.setEmailContent(content);
			history.setRemark(history.getCreationDate());
			String daoMsg = cloudDao.save(sf(), history);
			if (!SUCCESS.equals(daoMsg)) {
				resultMap.put("msg", daoMsg);
				return JSON_RESULT;
			}

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add(content);
			new MailThread(new MailBean(vendorEmail, cpsEmailTemplate.getEmailTitle(), contentStringList),
					getSendMailSetting()).start();
			count++;
		}
		resultMap.put("isSuccess", true);
		resultMap.put("msg", count + "封信件已發送");
		return JSON_RESULT;
	}

	public String hpsInviteVendorSend2() {
		logger.info("通知外部廠商");
		resultMap = new HashMap();
		resultMap.put("isSuccess", false);
		resultMap.put("msg", "尚未執行");
		List<CpsEmailTemplate> l = (List<CpsEmailTemplate>) cloudDao.queryTable(sf(), CpsEmailTemplate.class,
				new QueryGroup(new QueryRule("emailId", "hpsInviteVendor")), new QueryOrder[0], null, null);
		if (l.size() == 0) {
			resultMap.put("msg", "查無範本");
			return JSON_RESULT;
		}
		CpsEmailTemplate cpsEmailTemplate = l.get(0);
		String emailContent = cpsEmailTemplate.getEmailContent();
		emailContent = emailContent.replace("$ITEM_MSG$", itemMsg());

		String emailStr = request.getParameter("email");
		logger.debug("emailStr:" + emailStr);
		String[] emailArr = emailStr.split(",");

		int count = 0;
		for (String email : emailArr) {
			String vendorEmail = email.trim();
			String name = "";
			if (StringUtils.isBlank(vendorEmail))
				continue;
			logger.info("email:" + vendorEmail);
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
			history.setEmailTitle(cpsEmailTemplate.getEmailTitle());
			history.setEmailContent(content);
			history.setRemark(history.getCreationDate());
			String daoMsg = cloudDao.save(sf(), history);
			if (!SUCCESS.equals(daoMsg)) {
				resultMap.put("msg", daoMsg);
				return JSON_RESULT;
			}

			List<String> contentStringList = new ArrayList<String>();
			contentStringList.add(content);
			new MailThread(new MailBean(vendorEmail, cpsEmailTemplate.getEmailTitle(), contentStringList),
					getSendMailSetting()).start();
			count++;
		}
		resultMap.put("isSuccess", true);
		resultMap.put("msg", count + "封信件已發送");
		return JSON_RESULT;
	}
}