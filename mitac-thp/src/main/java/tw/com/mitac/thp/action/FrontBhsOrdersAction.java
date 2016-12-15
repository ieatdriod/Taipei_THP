package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.thp.bean.BhsOrders;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontBhsOrdersAction extends BasisFrontLoginAction {

	protected BhsOrders bean;
	protected CpsSiteMember cpsMember;

	protected String user;
	protected String userName;
	protected String country;
	protected String countrySelect;
	protected String vendorName;
	protected String position;
	protected String duns;
	protected String epaper;
	protected String item;
	protected String cooperationType;
	protected String cooperationName;
	protected String cooperation;
	protected String requesttext;
	protected String video;
	protected String videoDatetime;
	protected String fd;
	protected String fdt1;
	protected String fdt2;
	protected String sd;
	protected String sdt1;
	protected String sdt2;
	protected String td;
	protected String tdt1;
	protected String tdt2;
	protected String needvalidate;
	protected String epaperOo;

	public BhsOrders getBean() {
		return bean;
	}

	public void setBean(BhsOrders bean) {
		this.bean = bean;
	}

	public CpsSiteMember getCpsMember() {
		return cpsMember;
	}

	public void setCpsMember(CpsSiteMember cpsMember) {
		this.cpsMember = cpsMember;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountrySelect() {
		return countrySelect;
	}

	public void setCountrySelect(String countrySelect) {
		this.countrySelect = countrySelect;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getDuns() {
		return duns;
	}

	public void setDuns(String duns) {
		this.duns = duns;
	}

	public String getEpaper() {
		return epaper;
	}

	public void setEpaper(String epaper) {
		this.epaper = epaper;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getCooperationType() {
		return cooperationType;
	}

	public void setCooperationType(String cooperationType) {
		this.cooperationType = cooperationType;
	}

	public String getCooperationName() {
		return cooperationName;
	}

	public void setCooperationName(String cooperationName) {
		this.cooperationName = cooperationName;
	}

	public String getCooperation() {
		return cooperation;
	}

	public void setCooperation(String cooperation) {
		this.cooperation = cooperation;
	}

	public String getRequesttext() {
		return requesttext;
	}

	public void setRequesttext(String requesttext) {
		this.requesttext = requesttext;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getVideoDatetime() {
		return videoDatetime;
	}

	public void setVideoDatetime(String videoDatetime) {
		this.videoDatetime = videoDatetime;
	}

	public String getFd() {
		return fd;
	}

	public void setFd(String fd) {
		this.fd = fd;
	}

	public String getFdt1() {
		return fdt1;
	}

	public void setFdt1(String fdt1) {
		this.fdt1 = fdt1;
	}

	public String getFdt2() {
		return fdt2;
	}

	public void setFdt2(String fdt2) {
		this.fdt2 = fdt2;
	}

	public String getSd() {
		return sd;
	}

	public void setSd(String sd) {
		this.sd = sd;
	}

	public String getSdt1() {
		return sdt1;
	}

	public void setSdt1(String sdt1) {
		this.sdt1 = sdt1;
	}

	public String getSdt2() {
		return sdt2;
	}

	public void setSdt2(String sdt2) {
		this.sdt2 = sdt2;
	}

	public String getTd() {
		return td;
	}

	public void setTd(String td) {
		this.td = td;
	}

	public String getTdt1() {
		return tdt1;
	}

	public void setTdt1(String tdt1) {
		this.tdt1 = tdt1;
	}

	public String getTdt2() {
		return tdt2;
	}

	public void setTdt2(String tdt2) {
		this.tdt2 = tdt2;
	}

	public String getNeedvalidate() {
		return needvalidate;
	}

	public void setNeedvalidate(String needvalidate) {
		this.needvalidate = needvalidate;
	}

	public String getEpaperOo() {
		return epaperOo;
	}

	public void setEpaperOo(String epaperOo) {
		this.epaperOo = epaperOo;
	}

	protected Map<String, String> orderTypeMap;

	public Map<String, String> getOrderTypeMap() {
		return orderTypeMap;
	}

	protected Map<String, String> businessTypeMap;

	public Map<String, String> getBusinessTypeMap() {
		return businessTypeMap;
	}

	// ========== 合作需求單 ==========

	public String enterpriseData() {
		return SUCCESS;
	}

	public String enterpriseData2() {
		String memberSysid = getUserData2().getAccount().getSysid();

		bean = new BhsOrders();
		Util.defaultPK(bean);
		String vendorProfileSysid = request.getParameter("vendorSysid");
		String vendorSysid = "";
		if (StringUtils.isBlank(vendorProfileSysid))
			return ERROR;
		BhsVendorProfile vp = cloudDao.get(sf(), BhsVendorProfile.class, vendorProfileSysid);
		if (vp == null)
			return ERROR;
		vendorSysid = vp.getVendorSysid();

		cpsMember = cloudDao.get(sf(), CpsSiteMember.class, memberSysid);
		String memberVendorSysid = "*";
		// if (!"CpsVendor".equals(cpsMember.getSourceType())) {
		// return ERROR;
		// } else {
		// BhsVendorProfile vp = cloudDao.get(sf(), BhsVendorProfile.class,
		// cpsMember.getSourceSysid());
		// if (vp == null)
		// return ERROR;
		// memberVendorSysid = vp.getVendorSysid();
		// }
		// TODO 登入者身份是否為供應商 - > 只會是一般會員
		// BhsVendorProfile vp2 = cloudDao.get(sf(), BhsVendorProfile.class,
		// "BhsVendorProfile20160112163756345653522");
		// if (vp2 == null)
		// return ERROR;
		// memberVendorSysid = vp2.getVendorSysid();

		// 預設
		bean.setBillStatus(BillStatusUtil.PAYSTATUS_S);
		bean.setVendorSysid(vendorSysid);
		bean.setMemberSysid(memberSysid);
		// bean.setRemark("");

		CpsCountry memberCountry = getAllCountry().get(getUserData2().getAccount().getCountrySysid());
		if (memberCountry != null)
			countrySelect = getUserData2().getAccount().getCountrySysid() + "#" + memberCountry.getIsForeign();

		Map<String, Map> bhsOrdersConstantMap = getConstantMenu(BhsOrders.class);
		orderTypeMap = bhsOrdersConstantMap.get("orderType");
		businessTypeMap = bhsOrdersConstantMap.get("businessType");

		sessionSet("tempBean", bean);
		sessionSet("orderTypeMap", orderTypeMap);
		sessionSet("businessTypeMap", businessTypeMap);

		return SUCCESS;
	}

	public String enterpriseData2Submit() {
		// 產生訂單
		boolean isSuccess = saveOrder();
		if (!isSuccess)
			return ERROR;

		logger.debug("產生訂單:" + bean.getSysid());
		needvalidate = "true";
		return SUCCESS;
	}

	protected boolean saveOrder() {
		try {
			List saveList = new ArrayList();
			bean = (BhsOrders) sessionGet("tempBean");
			orderTypeMap = (Map) sessionGet("orderTypeMap");
			businessTypeMap = (Map) sessionGet("businessTypeMap");

			defaultValue(bean);
			bean.setCreator(getUserData2().getAccount().getMemberName());
			bean.setOperator(getUserData2().getAccount().getMemberName());
			defaultBillno(bean);

			bean.setDuns(duns);
			bean.setIssueDate(systemDatetime);
			bean.setMemberVendorSysid(vendorName);
			bean.setMemberPositionSysid(position);
			bean.setOrdersItem(item);
			bean.setBusinessType(epaper);
			bean.setOrderType(cooperation);
			String ordersDetail = "";
			String businessTypeStr = "";
			if (StringUtils.isNotBlank(epaper)) {
				String[] businessTypeArr = epaper.split(", ");
				for (String key : businessTypeArr) {
					businessTypeStr += ", " + businessTypeMap.get(key);
					if ("O".equals(key))
						businessTypeStr += "(" + epaperOo + ")";
				}
				if (businessTypeStr.length() >= 2)
					businessTypeStr = businessTypeStr.substring(2);
			}
			String[] orderTypeArr = cooperation.split(", ");
			String orderTypeStr = "";
			for (String key : orderTypeArr)
				orderTypeStr += ", " + orderTypeMap.get(key);
			orderTypeStr = orderTypeStr.substring(2);

			ordersDetail += textFormat("姓名", userName);
			if (StringUtils.isNotBlank(countrySelect))
				country = getAllCountry().get(countrySelect.split("#")[0]).getName();
			ordersDetail += textFormat("國籍", country);
			ordersDetail += textFormat("公司名", vendorName);
			ordersDetail += textFormat("職稱", position);
			ordersDetail += textFormat("鄧白氏編號", duns);
			ordersDetail += textFormat("營業別", businessTypeStr);
			ordersDetail += textFormat("主要產品項目", item);
			// ordersDetail += textFormat("合作項目類別", cooperationType);
			ordersDetail += textFormat("合作項目類別", orderTypeStr);
			ordersDetail += textFormat("合作項目名稱", cooperationName);
			// ordersDetail += textFormat("合作方式", orderTypeStr);
			ordersDetail += textFormat("合作內容說明", "\n" + requesttext);
			Boolean isVideo = "Y".equals(video);
			String videoStr = "";
			if (!isVideo) {
				videoStr += "否";
			} else {
				videoStr += "是";
				if (StringUtils.isNotBlank(videoDatetime))
					videoStr += "，預期視訊所需洽談時間(" + videoDatetime + ")";
				if (StringUtils.isNotBlank(fd)) {
					videoStr += "\n可進行視訊洽談的時間區間(Taiwan GMT +8)";
					videoStr += "\n" + fd + " " + fdt1 + " ~ " + fdt2;
				}
				if (StringUtils.isNotBlank(sd))
					videoStr += "\n" + sd + " " + sdt1 + " ~ " + sdt2;
				if (StringUtils.isNotBlank(td))
					videoStr += "\n" + td + " " + tdt1 + " ~ " + tdt2;
			}
			ordersDetail += textFormat("視訊洽談合作", videoStr);
			bean.setOrdersDetail(ordersDetail);

			saveList.add(bean);

			String daoMsg = "";
			if (StringUtils.isBlank(daoMsg)) {
				daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
			}

			if (!daoMsg.equals(SUCCESS)) {
				addActionError(daoMsg);
				return false;
			}
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		addActionMessage(getText(I18N_SAVE_SUCCESS));

		return true;
	}

	protected boolean updateOrder(String orderType) {
		try {
			List saveList = new ArrayList();
			bean = (BhsOrders) sessionGet("tempBean");
			if ("MA".equals(orderType))
				bean.setBillStatus(BillStatusUtil.MTS_WAIT_ORDER);
			else if ("SO".equals(orderType))
				bean.setBillStatus(BillStatusUtil.MTS_WAIT_APPROVED);
			defaultValue(bean);
			bean.setOperator(getUserData2().getAccount().getMemberName());
			saveList.add(bean);
			String daoMsg = "";
			if (StringUtils.isBlank(daoMsg)) {
				daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "UPDATE");
			}
			if (!daoMsg.equals(SUCCESS)) {
				addActionError(daoMsg);
				return false;
			}
		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		addActionMessage(getText(I18N_SAVE_SUCCESS));

		return true;
	}

	protected String textFormat(String name, String value) {
		if (StringUtils.isBlank(value))
			value = "";
		return name + "：" + value + "\n";
	}
}