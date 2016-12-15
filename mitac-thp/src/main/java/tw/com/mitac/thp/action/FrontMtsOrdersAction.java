package tw.com.mitac.thp.action;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.MtsItems;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontMtsOrdersAction extends BasisFrontLoginAction {

	protected MtsOrders bean;
	protected List<MtsOrdersProducts> mtsOrdersProductsList;
	protected List<MtsOrdersItems> mtsOrdersItemsList;
	protected List<MtsProducts> mtsProductsList;
	protected List<MtsItems> mtsItemsList;
	protected List<CpsCountry> cpsCountryList;
	protected CpsSiteMember cpsMember;

	protected String treatment;
	protected String service;
	protected String title;
	protected String lname;
	protected String mname;
	protected String fname;
	protected String gender;
	protected String birthday;
	protected String country;
	protected String countrySelect;
	protected String address;
	protected String phonecode;
	protected String phone;
	protected String email;
	protected String requesttext;
	protected String fd;
	protected String sd;
	protected String needvalidate;

	public final MtsOrders getBean() {
		return bean;
	}

	public final void setBean(MtsOrders bean) {
		this.bean = bean;
	}

	public List<MtsOrdersProducts> getMtsOrdersProductsList() {
		return mtsOrdersProductsList;
	}

	public void setMtsOrdersProductsList(List<MtsOrdersProducts> mtsOrdersProductsList) {
		this.mtsOrdersProductsList = mtsOrdersProductsList;
	}

	public List<MtsOrdersItems> getMtsOrdersItemsList() {
		return mtsOrdersItemsList;
	}

	public void setMtsOrdersItemsList(List<MtsOrdersItems> mtsOrdersItemsList) {
		this.mtsOrdersItemsList = mtsOrdersItemsList;
	}

	public List<MtsProducts> getMtsProductsList() {
		return mtsProductsList;
	}

	public void setMtsProductsList(List<MtsProducts> mtsProductsList) {
		this.mtsProductsList = mtsProductsList;
	}

	public List<MtsItems> getMtsItemsList() {
		return mtsItemsList;
	}

	public void setMtsItemsList(List<MtsItems> mtsItemsList) {
		this.mtsItemsList = mtsItemsList;
	}

	public CpsSiteMember getCpsMember() {
		return cpsMember;
	}

	public void setCpsMember(CpsSiteMember cpsMember) {
		this.cpsMember = cpsMember;
	}

	public String getTreatment() {
		return treatment;
	}

	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getMname() {
		return mname;
	}

	public void setMname(String mname) {
		this.mname = mname;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhonecode() {
		return phonecode;
	}

	public void setPhonecode(String phonecode) {
		this.phonecode = phonecode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRequesttext() {
		return requesttext;
	}

	public void setRequesttext(String requesttext) {
		this.requesttext = requesttext;
	}

	public String getFd() {
		return fd;
	}

	public void setFd(String fd) {
		this.fd = fd;
	}

	public String getSd() {
		return sd;
	}

	public void setSd(String sd) {
		this.sd = sd;
	}

	public String getNeedvalidate() {
		return needvalidate;
	}

	public void setNeedvalidate(String needvalidate) {
		this.needvalidate = needvalidate;
	}

	// ========== 第二醫療 ==========
	protected String r00;
	protected String r01;
	protected String r02;
	protected String r10;
	protected String r11;
	protected String r12;
	protected String needLanguage;
	protected String language;
	protected String fdt1;
	protected String fdt2;
	protected String sdt1;
	protected String sdt2;
	protected String td;
	protected String tdt1;
	protected String tdt2;
	protected String cid;
	// Upload
	protected String cb1;
	protected String cbOther;
	// checkBox
	protected String cb2;
	protected String requesttext1;
	protected String requesttext2;
	protected String requesttext3;

	protected String wcp01;
	protected String wcp02;
	protected String wcp03;
	protected String vcp01;
	protected String vcp02;
	protected String vcp03;

	public String getR00() {
		return r00;
	}

	public void setR00(String r00) {
		this.r00 = r00;
	}

	public String getR01() {
		return r01;
	}

	public void setR01(String r01) {
		this.r01 = r01;
	}

	public String getR02() {
		return r02;
	}

	public void setR02(String r02) {
		this.r02 = r02;
	}

	public String getR10() {
		return r10;
	}

	public void setR10(String r10) {
		this.r10 = r10;
	}

	public String getR11() {
		return r11;
	}

	public void setR11(String r11) {
		this.r11 = r11;
	}

	public String getR12() {
		return r12;
	}

	public void setR12(String r12) {
		this.r12 = r12;
	}

	public String getNeedLanguage() {
		return needLanguage;
	}

	public void setNeedLanguage(String needLanguage) {
		this.needLanguage = needLanguage;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getCb1() {
		return cb1;
	}

	public void setCb1(String cb1) {
		this.cb1 = cb1;
	}

	public String getCbOther() {
		return cbOther;
	}

	public void setCbOther(String cbOther) {
		this.cbOther = cbOther;
	}

	public String getCb2() {
		return cb2;
	}

	public void setCb2(String cb2) {
		this.cb2 = cb2;
	}

	public String getRequesttext1() {
		return requesttext1;
	}

	public void setRequesttext1(String requesttext1) {
		this.requesttext1 = requesttext1;
	}

	public String getRequesttext2() {
		return requesttext2;
	}

	public void setRequesttext2(String requesttext2) {
		this.requesttext2 = requesttext2;
	}

	public String getRequesttext3() {
		return requesttext3;
	}

	public void setRequesttext3(String requesttext3) {
		this.requesttext3 = requesttext3;
	}

	public String getWcp01() {
		return wcp01;
	}

	public void setWcp01(String wcp01) {
		this.wcp01 = wcp01;
	}

	public String getWcp02() {
		return wcp02;
	}

	public void setWcp02(String wcp02) {
		this.wcp02 = wcp02;
	}

	public String getWcp03() {
		return wcp03;
	}

	public void setWcp03(String wcp03) {
		this.wcp03 = wcp03;
	}

	public String getVcp01() {
		return vcp01;
	}

	public void setVcp01(String vcp01) {
		this.vcp01 = vcp01;
	}

	public String getVcp02() {
		return vcp02;
	}

	public void setVcp02(String vcp02) {
		this.vcp02 = vcp02;
	}

	public String getVcp03() {
		return vcp03;
	}

	public void setVcp03(String vcp03) {
		this.vcp03 = vcp03;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	protected Map<String, String> cpscountryMap;

	public Map<String, String> getCpscountryMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = cpscountryMap;
		return map;
	}

	public String medicalAppointment1() {
		String memberSysid = getUserData2().getAccount().getSysid();

		/*
		 * String sysid = request.getParameter("sysid"); if
		 * (StringUtils.isBlank(sysid)) { addActionError(getText("msg.noItem"));
		 * return ERROR; } else { bean = cloudDao.get(sf(), MtsOrders.class,
		 * sysid); }
		 */

		bean = new MtsOrders();
		Util.defaultPK(bean);
		String vendorProfileSysid = request.getParameter("vendorSysid");
		String vendorSysid = "";
		if (StringUtils.isBlank(vendorProfileSysid)) {
			return ERROR;
		} else {
			MtsVendorProfile vp = cloudDao.get(sf(), MtsVendorProfile.class, vendorProfileSysid);
			if (vp == null)
				return ERROR;
			vendorSysid = vp.getVendorSysid();
		}
		if (StringUtils.isBlank(vendorSysid))
			return ERROR;

		String ordersType = request.getParameter("ordersType");
		// if ("MA".equals(ordersType)) {
		mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(new QueryRule("vendorSysid",
				vendorSysid), new QueryRule("productsType", "S")), new QueryOrder[0], null, null);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (MtsProducts mp : mtsProductsList)
			map.put(mp.getSysid(), mp.getName());
		treatmentMap = map;
		cpsMember = cloudDao.get(sf(), CpsSiteMember.class, memberSysid);

		bean.setMemberSysid(memberSysid);
		bean.setBillStatus(BillStatusUtil.MTS_NEW);
		bean.setIsOrdersToVendor(true);
		bean.setIssueDate(systemDatetime);
		bean.setTransactionAmount(BigDecimal.ZERO);
		bean.setVendorSysid(vendorSysid);
		bean.setOrdersType("MA");
		// } else {
		// return ERROR;
		// }

		CpsCountry memberCountry = getAllCountry().get(getUserData2().getAccount().getCountrySysid());
		if (memberCountry != null)
			countrySelect = getUserData2().getAccount().getCountrySysid() + "#" + memberCountry.getIsForeign();

		// 取電話國際區碼
		cpsCountryList = cloudDao.queryTable(sf(), CpsCountry.class, new QueryGroup(), new QueryOrder[0], null, null);
		Map<String, String> mapcallingcode = new LinkedHashMap<String, String>();
		for (CpsCountry mpcallcode : cpsCountryList) {
			if (StringUtils.isNotBlank(mpcallcode.getCountryCallingCode())) {
				mapcallingcode.put(mpcallcode.getCountryCallingCode(), mpcallcode.getCountryCallingCode());
			}
		}

		cpscountryMap = mapcallingcode;

		sessionSet("tempBean", bean);
		sessionSet("treatmentMap", treatmentMap);
		sessionSet("cpscountryMap", cpscountryMap);
		return SUCCESS;
	}

	public String medicalAppointmentConfirm() {
		// 產生訂單
		boolean isSuccess = saveOrder();
		if (!isSuccess)
			return ERROR;

		logger.debug("產生訂單:" + bean.getSysid());
		needvalidate = "true";
		return SUCCESS;
	}

	public String medicalAppointment2() {
		// 檢核訂單
		String sysid = request.getParameter("sysid");
		MtsOrders order = cloudDao.get(sf(), MtsOrders.class, sysid);
		if (order == null)
			return ERROR;
		if (BillStatusUtil.MTS_NEW.equals(order.getBillStatus())) {
			needvalidate = "true";
		} else if (BillStatusUtil.MTS_CANCEL_ORDER.equals(order.getBillStatus())) {
			needvalidate = "訂單已取消";
		} else {
			needvalidate = "訂單已驗證";
		}
		bean = order;
		sessionSet("tempBean", bean);
		return SUCCESS;
	}

	public String medicalAppointmentValidate() {
		bean = (MtsOrders) sessionGet("tempBean");
		if (BillStatusUtil.MTS_NEW.equals(bean.getBillStatus())) {
			// TODO 驗證信用卡（暫無）
			// ...
			// === 驗證結束 ===
			boolean isSuccess = updateOrder("MA");
			if (!isSuccess)
				return ERROR;
			needvalidate = "Success！";
		} else {
			needvalidate = "訂單狀態錯誤，無法驗證";
		}
		return SUCCESS;
	}

	protected boolean saveOrder() {
		try {
			List saveList = new ArrayList();
			bean = (MtsOrders) sessionGet("tempBean");
			treatmentMap = (Map) sessionGet("treatmentMap");
			defaultValue(bean);
			bean.setCreator(getUserData2().getAccount().getMemberName());
			bean.setOperator(getUserData2().getAccount().getMemberName());
			defaultBillno(bean);

			String ordersDetail = "";
			if (StringUtils.isBlank(treatment)) {
				treatment = "";
			}
			String[] treatmentArr = treatment.split(", ");
			String treatmentStr = "";
			for (String key : treatmentArr)
				treatmentStr += ", " + treatmentMap.get(key);
			treatmentStr = treatmentStr.substring(2);
			ordersDetail += textFormat("Selected Treatment", treatmentStr);
			ordersDetail += textFormat("I am requesting this medical service for", service);
			ordersDetail += textFormat("Salutation", title);
			ordersDetail += textFormat("Last Name", lname);
			ordersDetail += textFormat("Middle Name", mname);
			ordersDetail += textFormat("First name", fname);
			ordersDetail += textFormat("Gender", gender);
			ordersDetail += textFormat("Date of Birth", birthday);
			if (StringUtils.isNotBlank(countrySelect))
				country = getAllCountry().get(countrySelect.split("#")[0]).getName();
			ordersDetail += textFormat("Nationality", country);
			ordersDetail += textFormat("Address", address);
			ordersDetail += textFormat("Phone Number ", phonecode + " - " + phone);
			ordersDetail += textFormat("E-mail", email);
			ordersDetail += textFormat("Appointment request", "\n" + requesttext);
			ordersDetail += textFormat("Appointment date 1st", fd);
			ordersDetail += textFormat("Appointment date 2nd", sd);
			bean.setOrdersDetail(ordersDetail);

			// 尾檔
			Set<MtsOrdersProducts> detailSet2 = new LinkedHashSet<MtsOrdersProducts>();
			if ("MA".equals(bean.getOrdersType())) {
				for (String key : treatmentArr) {
					if (StringUtils.isNotBlank(key)) {
						MtsOrdersProducts mtsOrdersProducts = new MtsOrdersProducts();
						// mtsOrdersProducts = (MtsOrdersProducts)
						// BeanUtils.cloneBean(data);
						Util.defaultPK(mtsOrdersProducts);
						defaultValue(mtsOrdersProducts);
						mtsOrdersProducts.setCreator(getUserData2().getAccount().getMemberName());
						mtsOrdersProducts.setOperator(getUserData2().getAccount().getMemberName());
						mtsOrdersProducts.setRemark("");
						mtsOrdersProducts.setParentSysid(bean.getSysid());
						mtsOrdersProducts.setMtsProductsSysid(key);
						// saveList.add(mtsOrdersProducts);
						detailSet2.add(mtsOrdersProducts);
					}
				}
				bean.setDetailSet2(detailSet2);
			} else {
				return false;
			}

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
			bean = (MtsOrders) sessionGet("tempBean");
			if ("MA".equals(orderType))
				bean.setBillStatus(BillStatusUtil.MTS_WAIT_ORDER);
			else if ("SO".equals(orderType))
				bean.setBillStatus(BillStatusUtil.MTS_WAIT_APPROVED);
			defaultValue(bean);
			// bean.setCreator(getUserData2().getAccount().getMemberName());
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

	// ========== 第二醫療 ==========

	public String medicalConsultant1() {
		String memberSysid = getUserData2().getAccount().getSysid();

		bean = new MtsOrders();
		Util.defaultPK(bean);

		String ordersType = request.getParameter("ordersType");

		mtsProductsList = cloudDao.queryTable(sf(), MtsProducts.class, new QueryGroup(
				new QueryRule("productsType", "S")), new QueryOrder[0], null, null);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (MtsProducts mp : mtsProductsList)
			map.put(mp.getSysid(), mp.getName());
		treatmentMap = map;
		cpsMember = cloudDao.get(sf(), CpsSiteMember.class, memberSysid);

		bean.setMemberSysid(memberSysid);
		bean.setBillStatus(BillStatusUtil.MTS_NEW);
		bean.setIsOrdersToVendor(false);
		bean.setIssueDate(systemDatetime);
		bean.setTransactionAmount(BigDecimal.ZERO);
		bean.setVendorSysid("*");
		bean.setOrdersType("SO");

		CpsCountry memberCountry = getAllCountry().get(getUserData2().getAccount().getCountrySysid());
		if (memberCountry != null)
			countrySelect = getUserData2().getAccount().getCountrySysid() + "#" + memberCountry.getIsForeign();

		// 預設金額
		wcp01 = bigDecimalFormat(findItems("WC001", null));
		wcp02 = bigDecimalFormat(findItems("WC002", null));
		wcp03 = bigDecimalFormat(findItems("WC003", null));
		vcp01 = bigDecimalFormat(findItems("VC001", null));
		vcp02 = bigDecimalFormat(findItems("VC002", null));
		vcp03 = bigDecimalFormat(findItems("VC003", null));

		sessionSet("tempBean", bean);
		sessionSet("treatmentMap", treatmentMap);
		return SUCCESS;
	}

	public String medicalConsultantEdit() {

		return SUCCESS;
	}

	public String medicalConsultantReview() {

		return SUCCESS;
	}

	public String medicalConsultantConfirm() {
		// 產生訂單
		boolean isSuccess = saveOrder2();
		if (!isSuccess)
			return ERROR;

		logger.debug("產生訂單:" + bean.getSysid());
		needvalidate = "true";
		return SUCCESS;
	}

	public String medicalConsultant() {
		// 檢核訂單
		String sysid = request.getParameter("sysid");
		MtsOrders order = cloudDao.get(sf(), MtsOrders.class, sysid);
		if (order == null)
			return ERROR;
		if (BillStatusUtil.MTS_NEW.equals(order.getBillStatus())) {
			needvalidate = "true";
		} else if (BillStatusUtil.MTS_CANCEL_ORDER.equals(order.getBillStatus())) {
			needvalidate = "訂單已取消";
		} else {
			needvalidate = "訂單已付款";
		}
		bean = order;
		sessionSet("tempBean", bean);
		return SUCCESS;
	}

	public String medicalConsultantValidate() {
		bean = (MtsOrders) sessionGet("tempBean");
		if (BillStatusUtil.MTS_NEW.equals(bean.getBillStatus())) {
			// TODO 信用卡付費（暫無）
			// ...
			// === 結束 ===
			boolean isSuccess = updateOrder("SO");
			if (!isSuccess)
				return ERROR;
			needvalidate = "Success！";
		} else {
			needvalidate = "訂單狀態錯誤，付款失敗";
		}
		return SUCCESS;
	}

	protected int flag = 0;

	protected BigDecimal findItems(String id, Set<MtsItems> rSet) {
		if (StringUtils.isBlank(id))
			return BigDecimal.ZERO;
		List<MtsItems> items = cloudDao.queryTable(sf(), MtsItems.class, new QueryGroup(new QueryRule(Util.ID, id)),
				new QueryOrder[0], null, null);
		if (items.size() < 1)
			return BigDecimal.ZERO;
		if (rSet == null)
			return items.get(0).getItemsPrice();
		if (flag == 1)
			items.get(0).setRemark("Interpreter Costs");
		rSet.add(items.get(0));
		return items.get(0).getItemsPrice();
	}

	protected String bigDecimalFormat(BigDecimal d) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		df.setGroupingUsed(false);
		BigDecimal bd = d.setScale(2, BigDecimal.ROUND_HALF_UP);
		return df.format(bd);
	}

	protected boolean saveOrder2() {
		try {
			List saveList = new ArrayList();
			bean = (MtsOrders) sessionGet("tempBean");
			treatmentMap = (Map) sessionGet("treatmentMap");
			defaultValue(bean);
			bean.setCreator(getUserData2().getAccount().getMemberName());
			bean.setOperator(getUserData2().getAccount().getMemberName());
			defaultBillno(bean);

			String ordersDetail = "";

			BigDecimal p = new BigDecimal(0);
			Set<MtsItems> rSet = new LinkedHashSet<MtsItems>();
			String rStr = "";
			flag = 0;
			p = p.add(findItems(r00, rSet));
			p = p.add(findItems(r01, rSet));
			p = p.add(findItems(r02, rSet));
			p = p.add(findItems(r10, rSet));
			p = p.add(findItems(r11, rSet));
			p = p.add(findItems(r12, rSet));
			if ("Yes".equals(needLanguage)) {
				flag = 1;
				p = p.add(findItems(r10, rSet));
				p = p.add(findItems(r11, rSet));
				p = p.add(findItems(r12, rSet));
			}
			for (MtsItems i : rSet) {
				rStr += ", " + i.getName();
				if (StringUtils.isNotBlank(i.getRemark()))
					rStr += "(" + i.getRemark() + ")";
			}
			rStr = rStr.substring(2);
			ordersDetail += textFormat("Your oeder service", rStr);
			if ("Yes".equals(needLanguage))
				ordersDetail += textFormat("Interpreter", language);

			String ttp = "$" + bigDecimalFormat(p);
			ordersDetail += textFormat("Total Price", ttp);
			bean.setTransactionAmount(p);

			ordersDetail += textFormat("1st.Date", fd + " " + fdt1 + " ~ " + fdt2);
			ordersDetail += textFormat("2nd.Date", sd + " " + sdt1 + " ~ " + sdt2);
			ordersDetail += textFormat("3rd.Date", td + " " + tdt1 + " ~ " + tdt2);

			if (StringUtils.isBlank(treatment)) {
				treatment = "";
			}
			String[] treatmentArr = treatment.split(", ");
			String treatmentStr = "";
			for (String key : treatmentArr)
				treatmentStr += ", " + treatmentMap.get(key);
			treatmentStr = treatmentStr.substring(2);
			ordersDetail += textFormat("Selected Treatment", treatmentStr);
			ordersDetail += textFormat("I am requesting this medical service for", service);
			ordersDetail += textFormat("Salutation", title);
			ordersDetail += textFormat("Last Name", lname);
			ordersDetail += textFormat("Middle Name", mname);
			ordersDetail += textFormat("First name", fname);
			ordersDetail += textFormat("Gender", gender);
			ordersDetail += textFormat("Date of Birth", birthday);
			if (StringUtils.isNotBlank(countrySelect))
				country = getAllCountry().get(countrySelect.split("#")[0]).getName();
			ordersDetail += textFormat("Nationality", country);
			ordersDetail += textFormat("Address", address);
			ordersDetail += textFormat("Phone Number ", phonecode + " - " + phone);
			ordersDetail += textFormat("E-mail", email);
			// ordersDetail += textFormat("Appointment request", "\n" +
			// requesttext);
			// ordersDetail += textFormat("Appointment date 1st", fd);
			// ordersDetail += textFormat("Appointment date 2nd", sd);
			ordersDetail += textFormat("Facebook/LINE/ICQ/QQ/skype ID", cid);

			ordersDetail += textFormat("The reason for your appointment request", "\n" + requesttext);

			ordersDetail += textFormat("You upload medical report", cb1);
			ordersDetail += textFormat("You have the following conditions", cb2);

			ordersDetail += textFormat("Describe in more detail above conditions", "\n" + requesttext1);
			ordersDetail += textFormat(
					"Do you have a strong family history of any serious illnesses (cancer, heart disease etc.)?", "\n"
							+ requesttext2);
			ordersDetail += textFormat("Special request", "\n" + requesttext3);

			bean.setOrdersDetail(ordersDetail);

			// 尾檔
			Set<MtsOrdersProducts> detailSet2 = new LinkedHashSet<MtsOrdersProducts>();
			for (String key : treatmentArr) {
				if (StringUtils.isNotBlank(key)) {
					MtsOrdersProducts mtsOrdersProducts = new MtsOrdersProducts();
					Util.defaultPK(mtsOrdersProducts);
					defaultValue(mtsOrdersProducts);
					mtsOrdersProducts.setCreator(getUserData2().getAccount().getMemberName());
					mtsOrdersProducts.setOperator(getUserData2().getAccount().getMemberName());
					mtsOrdersProducts.setRemark("");
					mtsOrdersProducts.setParentSysid(bean.getSysid());
					mtsOrdersProducts.setMtsProductsSysid(key);
					detailSet2.add(mtsOrdersProducts);
				}
			}

			Set<MtsOrdersItems> detailSet3 = new LinkedHashSet<MtsOrdersItems>();
			for (MtsItems i : rSet) {
				MtsOrdersItems items = new MtsOrdersItems();
				Util.defaultPK(items);
				defaultValue(items);
				items.setCreator(getUserData2().getAccount().getMemberName());
				items.setOperator(getUserData2().getAccount().getMemberName());
				items.setRemark(i.getRemark());
				items.setParentSysid(bean.getSysid());
				items.setMtsItemsSysid(i.getSysid());
				items.setItemsPrice(i.getItemsPrice());
				detailSet3.add(items);
			}

			bean.setDetailSet2(detailSet2);
			bean.setDetailSet3(detailSet3);

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

}