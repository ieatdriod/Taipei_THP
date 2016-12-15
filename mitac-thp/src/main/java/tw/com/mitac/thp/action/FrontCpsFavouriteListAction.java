package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsHighlight;
import tw.com.mitac.thp.bean.BhsMarquee;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsFavouriteList;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsCooperation;
import tw.com.mitac.thp.bean.MtsHighlight;
import tw.com.mitac.thp.bean.MtsMarquee;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.bean.MtsVendorProfile;

public class FrontCpsFavouriteListAction extends BasisFrontLoginAction {
	List<String> mtsVendorList = new ArrayList<String>();
	List<String> mtsProductsList = new ArrayList<String>();
	List<String> mtsCooperationList = new ArrayList<String>();
	List<String> mtsHighlightList = new ArrayList<String>();
	List<String> bhsVendorList = new ArrayList<String>();
	List<String> bhsProductsList = new ArrayList<String>();
	List<String> bhsTechnologyList = new ArrayList<String>();
	List<String> bhsHighlightList = new ArrayList<String>();

	protected CpsFavouriteList bean;

	public CpsFavouriteList getBean() {
		return bean;
	}

	public void setBean(CpsFavouriteList bean) {
		this.bean = bean;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	public void getClear() {
		mtsVendorList.clear();
		mtsProductsList.clear();
		mtsCooperationList.clear();
		mtsHighlightList.clear();
		bhsVendorList.clear();
		bhsProductsList.clear();
		bhsTechnologyList.clear();
		bhsHighlightList.clear();
	}

	/**
	 * 無團隊名稱不回傳我的最愛 取得登入者我的最愛資料
	 */
	public String initFavouriteData() {
		String sysid = getUserData2().getAccount().getSysid();
		List<CpsFavouriteList> cpsFavouriteListList = cloudDao.queryTable(sf(), CpsFavouriteList.class,
				new QueryGroup(new QueryRule("memberSysid", sysid)), new QueryOrder[] { new QueryOrder(PK, DESC) },
				null, null);
		
		
		
		// logger.debug("$$$$$$$-CpsFavouriteListList.size():" +
		// CpsFavouriteListList.size());
		//
		getClear();
		if (cpsFavouriteListList.size() > 0) {
			for (CpsFavouriteList cpsFavouriteList : cpsFavouriteListList) {
				String entityType = cpsFavouriteList.getEntityType().toString();
				String favouriteSysid = cpsFavouriteList.getSysid().toString();
				String vendorSysid = cpsFavouriteList.getVendorSysid().toString();
				
				
				if (StringUtils.isNotBlank(cpsFavouriteList.getFavouriteType())
						&& StringUtils.isNotBlank(cpsFavouriteList.getSourceSysid())) {
					String sourceSysid = cpsFavouriteList.getSourceSysid().toString();
					// logger.debug("$$$$$$$-sourceSysid:" + sourceSysid);
		
					// 醫療館
					if ("MTS".equals(entityType)) {
						if ("T".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for 團隊頁籤
							String vednorlistString = getMtsVendorList(favouriteSysid, vendorSysid);
							if (vednorlistString != null)
								mtsVendorList.add(vednorlistString);
						} else if ("S".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for 特色服務頁籤
							String productslistString = getMtsProductsList(favouriteSysid, vendorSysid, sourceSysid);
							if (productslistString != null)
								mtsProductsList.add(productslistString);
						} else if ("I".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for 國際合作頁籤
							String cooperationlistString = getMtsCooperationList(favouriteSysid, vendorSysid,
									sourceSysid);
							if (cooperationlistString != null)
								mtsCooperationList.add(cooperationlistString);
						} else if ("H".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for Highlight頁籤
							String highlightlistString = getMtsHighlightList(favouriteSysid, vendorSysid, sourceSysid);
							if (highlightlistString != null)
								mtsHighlightList.add(highlightlistString);
						}
					}
					// 生技館
					if ("BHS".equals(entityType)) {
						if ("B".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for 企業頁籤
							String bhsVednorlistString = getBhsVendorList(favouriteSysid, vendorSysid);
							if (bhsVednorlistString != null)
								bhsVendorList.add(bhsVednorlistString);

						} else if ("P".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for 特色產品頁籤
							String productslistString = getBhsProductsList(favouriteSysid, vendorSysid, sourceSysid);
							if (productslistString != null)
								bhsProductsList.add(productslistString);
							logger.debug("bhsProductsList:"+bhsProductsList);
							
						} else if ("T".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for 核心技術頁籤
							String technologylistString = getBhsTechnologyList(favouriteSysid, vendorSysid,
									sourceSysid);
							if (technologylistString != null)
								bhsTechnologyList.add(technologylistString);
						} else if ("H".equals(cpsFavouriteList.getFavouriteType().toString())) {
							// for Highlight頁籤
							String highlightlistString = getBhsHighlightList(favouriteSysid, vendorSysid, sourceSysid);
							if (highlightlistString != null)
								bhsHighlightList.add(highlightlistString);
						}
					}
					
				
				}
			}
			session.put("mtsVendorList", mtsVendorList);
			session.put("mtsProductsList", mtsProductsList);
			session.put("mtsCooperationList", mtsCooperationList);
			session.put("mtsHighlightList", mtsHighlightList);

			session.put("bhsVendorList", bhsVendorList);
			logger.debug("++++++" + bhsVendorList);

			session.put("bhsProductsList", bhsProductsList);
			session.put("bhsTechnologyList", bhsTechnologyList);
			session.put("bhsHighlightList", bhsHighlightList);
		} else {
			session.put("mtsVendorList", null);
			session.put("mtsProductsList", null);
			session.put("mtsCooperationList", null);
			session.put("mtsHighlightList", null);

			session.put("bhsVendorList", null);
			session.put("bhsProductsList", null);
			session.put("bhsTechnologyList", null);
			session.put("bhsHighlightList", null);
		}
		return SUCCESS;
	}

	private Map<String, String> mtsVendorNewsMap = new HashMap<String, String>();

	/** 醫療館-團隊頁籤 */
	protected String getMtsVendorList(String infavouriteSysid, String inVendorSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找團隊最新消息
		String vendorNews = mtsVendorNewsMap.get(inVendorSysid);
		if (StringUtils.isBlank(vendorNews)) {
			vendorNews = "<目前無團隊訊息>";
			List<MtsMarquee> mtsMarqueeList = cloudDao.queryTable(sf(), MtsMarquee.class,
					new QueryGroup(new QueryRule("sourceId", inVendorSysid)), null, 0, 1);
			addMultiLan(mtsMarqueeList, sf(), MtsMarquee.class);
			if (mtsMarqueeList.size() > 0) {
				vendorNews = mtsMarqueeList.get(0).getMarqueeText().toString();
			}

			mtsVendorNewsMap.put(inVendorSysid, vendorNews);
		}
		//找vendorProfileSysid
		List<MtsVendorProfile> mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
		String mtsVendorProfileSysid = "";
		if (mtsVendorProfileList.size() > 0) {

			mtsVendorProfileSysid = mtsVendorProfileList.get(0).getSysid();
		}
		
		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + vendorNews + "$*$" + inVendorSysid;
		return rtnlistString;
	}

	/** 醫療館-服務頁籤 */
	protected String getMtsProductsList(String infavouriteSysid, String inVendorSysid, String inSourceSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找服務名稱
		MtsProducts data = getDataMtsProductsTable().get(inSourceSysid);
		String productsName = "<目前無服務名稱>";
		if (data != null) {
			productsName = data.getName().toString();
		}
		//找vendorProfileSysid
		List<MtsVendorProfile> mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
		String mtsVendorProfileSysid = "";
		if (mtsVendorProfileList.size() > 0) {

			mtsVendorProfileSysid = mtsVendorProfileList.get(0).getSysid();
		}
		
		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + productsName + "$*$" + inSourceSysid+ "$*$"
				+ inVendorSysid;
		return rtnlistString;
	}

	/** 醫療館-國際合作頁籤 */
	protected String getMtsCooperationList(String infavouriteSysid, String inVendorSysid, String inSourceSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找國際合作名稱
		MtsCooperation data = getDataMtsCooperationTable().get(inSourceSysid);
		String cooperationName = "<目前無國際合作名稱>";
		if (data != null) {
			cooperationName = data.getCooperationName().toString();
		}
		//找vendorProfileSysid
		List<MtsVendorProfile> mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
		String mtsVendorProfileSysid = "";
		if (mtsVendorProfileList.size() > 0) {

			mtsVendorProfileSysid = mtsVendorProfileList.get(0).getSysid();
		}
		
		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + cooperationName + "$*$" + inSourceSysid+ "$*$"
				+ inVendorSysid;
		return rtnlistString;
	}

	/** 醫療館-Highlight頁籤 */
	protected String getMtsHighlightList(String infavouriteSysid, String inVendorSysid, String inSourceSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找服務名稱
		MtsHighlight data = getDataMtsHighlightTable().get(inSourceSysid);
		String highlightName = "<目前無Highlight名稱>";
		if (data != null) {
			highlightName = data.getName().toString();
		}
		//找vendorProfileSysid
		List<MtsVendorProfile> mtsVendorProfileList = cloudDao.queryTable(sf(), MtsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
		String mtsVendorProfileSysid = "";
		if (mtsVendorProfileList.size() > 0) {

			mtsVendorProfileSysid = mtsVendorProfileList.get(0).getSysid();
		}
		
		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + highlightName + "$*$" + inSourceSysid+ "$*$"
				+ inVendorSysid;
		return rtnlistString;
	}

	private Map<String, String> bhsVendorNewsMap = new HashMap<String, String>();

	/** 生技館-企業頁籤 */
	protected String getBhsVendorList(String infavouriteSysid, String inVendorSysid) {
		// 找企業名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找企業最新消息
		String vendorNews = bhsVendorNewsMap.get(inVendorSysid);
		if (StringUtils.isBlank(vendorNews)) {
			vendorNews = "<目前無企業訊息>";
			List<BhsMarquee> bhsMarqueeList = cloudDao.queryTable(sf(), BhsMarquee.class,
					new QueryGroup(new QueryRule("sourceId", inVendorSysid)), null, 0, 1);
			addMultiLan(bhsMarqueeList, sf(), BhsMarquee.class);
			if (bhsMarqueeList.size() > 0) {
				vendorNews = bhsMarqueeList.get(0).getMarqueeText().toString();
			}

			bhsVendorNewsMap.put(inVendorSysid, vendorNews);
		}

		List<BhsVendorProfile> bhsVendorProfileList = cloudDao.queryTable(sf(), BhsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
		String bhsVendorProfileSysid = "";
		if (bhsVendorProfileList.size() > 0) {

			bhsVendorProfileSysid = bhsVendorProfileList.get(0).getSysid();
		}

		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + vendorNews + "$*$" + bhsVendorProfileSysid;
		return rtnlistString;
	}

	/** 生技館-產品頁籤 */
	protected String getBhsProductsList(String infavouriteSysid, String inVendorSysid, String inSourceSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找產品名稱
		BhsProducts data = getDataBhsProductsTable().get(inSourceSysid);
		String productsName = "<目前無產品名稱>";
		if (data != null) {
			productsName = data.getName().toString();
		}
		//找vendorProfileSysid
		List<BhsVendorProfile> bhsVendorProfileList = cloudDao.queryTable(sf(), BhsVendorProfile.class,
				new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
		String bhsVendorProfileSysid = "";
		if (bhsVendorProfileList.size() > 0) {

			bhsVendorProfileSysid = bhsVendorProfileList.get(0).getSysid();
		}

		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + productsName + "$*$" + inSourceSysid+ "$*$"
				+ bhsVendorProfileSysid;
		return rtnlistString;
	}

	/** 生技館-技術頁籤 */
	protected String getBhsTechnologyList(String infavouriteSysid, String inVendorSysid, String inSourceSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找技術名稱
		BhsTechnology data = getDataBhsTechnologyTable().get(inSourceSysid);
		String technologyName = "<目前無技術名稱>";
		if (data != null) {
			technologyName = data.getName().toString();
		}
		
		//找vendorProfileSysid
				List<BhsVendorProfile> bhsVendorProfileList = cloudDao.queryTable(sf(), BhsVendorProfile.class,
						new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
				String bhsVendorProfileSysid = "";
				if (bhsVendorProfileList.size() > 0) {

					bhsVendorProfileSysid = bhsVendorProfileList.get(0).getSysid();
				}
		
		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + technologyName + "$*$" + inSourceSysid + "$*$"
		+ bhsVendorProfileSysid;
		return rtnlistString;
	}

	/** 生技館-Highlight頁籤 */
	protected String getBhsHighlightList(String infavouriteSysid, String inVendorSysid, String inSourceSysid) {
		// 找團隊名稱
		CpsVendor cpsVendor = getDataCpsVendorTable().get(inVendorSysid);
		if (cpsVendor == null)
			return null;
		String vendorName = cpsVendor.getName();
		// 找Highlight名稱
		BhsHighlight data = getDataBhsHighlightTable().get(inSourceSysid);
		String highlightName = "<目前無Highlight名稱>";
		if (data != null) {
			highlightName = data.getName().toString();
		}
		
		//找vendorProfileSysid
				List<BhsVendorProfile> bhsVendorProfileList = cloudDao.queryTable(sf(), BhsVendorProfile.class,
						new QueryGroup(new QueryRule("vendorSysid", inVendorSysid)), null, 0, 1);
				String bhsVendorProfileSysid = "";
				if (bhsVendorProfileList.size() > 0) {

					bhsVendorProfileSysid = bhsVendorProfileList.get(0).getSysid();
				}
		
		String rtnlistString = infavouriteSysid + "$*$" + vendorName + "$*$" + highlightName + "$*$" + inSourceSysid+ "$*$"
				+ bhsVendorProfileSysid;
		return rtnlistString;
	}

	// 刪除選擇的我的最愛項目
	public String ajaxDoDeleteFavourite() {
		resultMap = new HashMap();
		String selSysid = request.getParameter("selSysid").trim();
		logger.debug("$$$$$$$-selSysid:" + selSysid);

		String doResult = "";
		try {
			if (StringUtils.isNotBlank(selSysid)) {
				String[] sysidArr = selSysid.split(",");

				for (String delFavouriteSysid : sysidArr) {
					delFavouriteSysid = delFavouriteSysid.trim();
					logger.debug("$$$$$$$-AAASysid:" + delFavouriteSysid);
					if (!"on".equals(delFavouriteSysid)) {
						doResult = delFavourite(delFavouriteSysid);
					}
				}
				logger.debug("$$$$$$$-doResult:" + doResult);
				resultMap.put("msg", doResult);
			}

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("msg", doResult);
			return JSON_RESULT;
		}

		return JSON_RESULT;
	}

	public String delFavourite(String delSysid) {
		String daoMsg = cloudDao.save(sf(), new DeleteStatement(CpsFavouriteList.class.getSimpleName(),
				new QueryGroup(new QueryRule(PK, delSysid)))); // 要加這行才會真正執行

		logger.debug("$$$$$$$-daoMsg:" + daoMsg);

		return daoMsg;
	}

}