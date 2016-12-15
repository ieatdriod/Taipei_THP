package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.Util;

public class FrontCpsSiteMemberAction extends BasisFrontLoginAction {
	
	protected CpsSiteMember bean;
	
	public CpsSiteMember getBean() {
		return bean;
	}

	public void setBean(CpsSiteMember bean) {
		this.bean = bean;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}
	
	public String initAccountInfo() {
		String sysid = getUserData2().getAccount().getSysid();
		logger.debug("modifyPassword-initAccountInfo-sysid:" + sysid);		
		bean = cloudDao.get(sf(), CpsSiteMember.class, sysid);
		return SUCCESS;
	}
	
	//會員中心-修改密碼
	public String ajaxDoModifyPassword() {
		
		String sysid = getUserData2().getAccount().getSysid();
		String newPassword = request.getParameter("newPassword");  //抓畫面上輸入的密碼
		String beanSysid = request.getParameter("beanSysid");  //抓畫面上輸入的sysid
		String encodePassword = "";
		logger.debug("modifyPassword-sysid:" + sysid);
		logger.debug("modifyPassword-bean sysid:" + beanSysid);
		logger.debug("modifyPassword-newPassword:" + newPassword);
		
		if (!sysid.equals(beanSysid)) {
			resultString = "資訊異常，請重新整理";
		} else {
			//先找該筆member sysid是否存在
			if (StringUtils.isNotBlank(sysid)) {
			
				List<CpsSiteMember> SiteMemberList = cloudDao.queryTable(sf(), CpsSiteMember.class, 
						new QueryGroup(new QueryRule(PK, sysid)), 
						new QueryOrder[0], null, null);
			
				logger.debug("modifyPassword-Sysidcount:" + SiteMemberList.size());
				if (SiteMemberList.size() > 0) {
					//有查到，進行密碼修改
					Map<String, Object> setMap = getUpdatePropertyMap();
				
					encodePassword = Util.encode(newPassword); //編碼密碼
					logger.debug("修改密碼畫面編碼過的密碼:" + encodePassword);
					setMap.put("password", encodePassword);

					resultString = cloudDao.save(sf(), new UpdateStatement(CpsSiteMember.class.getSimpleName(), 
								   new QueryGroup(new QueryRule(PK, sysid)), setMap));
				} else {
					//沒有查到
					resultMap.put("msg", "很抱歉，系統無法辨識此會員資料，請與客服中心連絡!");
				}
			}
		}
		logger.debug("modifyPassword-resultString:" + resultString);
		
		return JSON_RESULT;
	}

	//會員中心-修改密碼--比對畫面新舊密碼
	public String ajaxDoComparisonPassword() {
		String oldPassword = request.getParameter("oldPassword");  //抓畫面上舊密碼
		String newPassword = request.getParameter("newPassword");  //抓畫面上新密碼

		logger.debug("modifyPassword-oldPassword:" + oldPassword);
		logger.debug("modifyPassword-newPassword:" + newPassword);
		
		if (!Util.encode(newPassword).equals(oldPassword)) {
			resultString = "success";
		} else {
			resultString = "新舊密碼相同，請另外設定新密碼!";
		}
		logger.debug("ajaxDoComparisonPassword-resultString:" + resultString);
		
		return JSON_RESULT;
	}

	
}