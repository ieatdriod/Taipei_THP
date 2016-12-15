package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsArticle;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsRegistration;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Deprecated
public class BhsOnlineGalleryRegistrationAction extends BasisFrontLoginAction {
	private static final long serialVersionUID = 1L;

	CpsRegistration bean;

	public CpsRegistration getBean() {
		return bean;
	}

	public void setBean(CpsRegistration bean) {
		this.bean = bean;
	}

	public String main() {
		String articleSysid = request.getParameter("articleSysid");
		BhsArticle bhsArticle = createDataTable(BhsArticle.class).get(articleSysid);
		if (StringUtils.isBlank(articleSysid) || bhsArticle == null) {
			request.setAttribute("isSuccess", false);
			return SUCCESS;
		}
		session.put("bhsArticle", bhsArticle);

		CpsSiteMember member = getUserData2().getAccount();
		bean = new CpsRegistration();
		Util.defaultPK(bean);
		bean.setRegistrationName(member.getFirstName());
		bean.setLastName(member.getLastName());
		bean.setRegistrationCall(member.getMemberCall());
		bean.setRegistrationTitle(member.getMemberTitle());
		bean.setRegistrationUnit(member.getMemberUnit());
		bean.setRegistrationTel(member.getPhone());
		bean.setRegistrationTel2(member.getPhone2());
		bean.setCountryCode(member.getCountryCode());
		bean.setCountryCode2(member.getCountryCode2());
		bean.setRegistrationMail(member.getEmail());
		bean.setRegistrationCompany(member.getMemberCompany());
		String countryName = createDataDisplay(CpsCountry.class).get(member.getCountrySysid());
		bean.setRegistrationCountry(countryName);
		bean.setRegistrationAddr(member.getAddress());
		bean.setRegistrationWebsite(member.getMemberWebsite());
		bean.setRegistrationBus(member.getMemberBus());
		bean.setSiteMemberSysid(member.getSysid());
		bean.setSourceSysid(articleSysid);
		String entityId = articleSysid.substring(0, 3).toLowerCase();
		List<CpsEntity> cpsEntityList = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(new QueryRule(ID,
				entityId)), new QueryOrder[0], null, null);
		if (cpsEntityList.size() > 0)
			bean.setEntitySysid(cpsEntityList.get(0).getSysid());
		sessionSet("tempBean", bean);
		// request.setAttribute("cpsCountryList", findCpsCountryList());
		return SUCCESS;
	}

	public String toSubmit() {
		Boolean isSuccess = saveRegistration();
		request.setAttribute("isSuccess", isSuccess);
		if (isSuccess)
			sessionSet("tempBean", null);
		logger.debug("isSuccess:" + isSuccess + ",報名表:" + bean.getSysid());
		return SUCCESS;
	}

	protected boolean saveRegistration() {
		try {
			CpsRegistration tempBean = (CpsRegistration) sessionGet("tempBean");
			bean.setSysid(tempBean.getSysid());
			bean.setSiteMemberSysid(tempBean.getSiteMemberSysid());
			bean.setSourceSysid(tempBean.getSourceSysid());
			bean.setEntitySysid(tempBean.getEntitySysid());
			bean.setIsPay(false);
			bean.setRegistrationType("E");

			defaultValue(bean);

			List saveList = new ArrayList();
			saveList.add(bean);
			String daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
			if (!daoMsg.equals(SUCCESS)) {
				logger.debug(daoMsg);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}