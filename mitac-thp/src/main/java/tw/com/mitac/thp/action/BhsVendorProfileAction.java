package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsAdsC;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;

public class BhsVendorProfileAction extends DetailController<BhsVendorProfile> {
	/** 框架按鈕處理 */
	public boolean getWithoutSaveBtn() {
		return false;
	}

	public boolean getWithoutSaveAndNew() {
		return true;
	}

	public boolean getWithoutSaveAndReturnMain() {
		return true;
	}

	/** 尾檔 */
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();

		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", BhsAdsC.class));

		return detailClassMap;
	}

	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("bhsVendorProfile_pic");

	protected String getSavePath() {
		return PATH;
	}

	protected CpsVendor cpsVendor;

	public CpsVendor getCpsVendor() {
		return cpsVendor;
	}

	public void setCpsVendor(CpsVendor cpsVendor) {
		this.cpsVendor = cpsVendor;
	}

	@Override
	public String[] getImgCols() {
		return new String[] { "vendorImageSummary" };
	}

	// // 假欄位 vendorShortName
	// protected Map<String, Map> getJqgridColModelMap() {
	// Map<String, Map> jqgridColModelMap = super.getJqgridColModelMap();
	// Map<String, Map> newMap = new LinkedHashMap<String, Map>();
	// for (String key : jqgridColModelMap.keySet()) {
	// if ("vendorSysid".equals(key))
	// //緊鄰vendorSysid前面
	// newMap.put("vendorShortName", fakeColModel("vendorShortName"));
	// newMap.put(key, jqgridColModelMap.get(key));
	// }
	// return newMap;
	// }

	// @Override
	// protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup,
	// QueryOrder[] orders, Integer from,
	// Integer length) {
	// Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from,
	// length);
	// List<BhsVendorProfile> list = (List<BhsVendorProfile>) oldArr[1];
	// List<Map> newResults = new ArrayList<Map>();
	// for (BhsVendorProfile bean : list) {
	// Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(bean);
	// newResults.add(map);
	// CpsVendor source =
	// createDataTable(CpsVendor.class).get(bean.getVendorSysid());
	// String vendorShortName = "";
	// if (source != null &&
	// StringUtils.isNotBlank(source.getVendorShortName()))
	// vendorShortName = source.getVendorShortName();
	// map.put("vendorShortName", vendorShortName);
	// }
	// Object[] newArr = new Object[] { oldArr[0], newResults };
	// return newArr;
	// }

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
	public String edit() {
		String result = super.edit();
		cpsVendor = createDataTable(CpsVendor.class).get(bean.getVendorSysid());
		if (cpsVendor == null)
			cpsVendor = new CpsVendor();

		menuSel = getBhsMenuSel();
		return result;
	}

	@Override
	protected boolean executeSave() {

		List<BhsVendorProfile> bhsVendorProfiles = (List<BhsVendorProfile>) cloudDao.query(sf(),
				BhsVendorProfile.class, new QueryGroup(new QueryRule(PK, NE, bean.getSysid()), new QueryRule(
						"vendorSysid", bean.getVendorSysid())), new QueryOrder[0], null, null);
		if (bhsVendorProfiles.size() > 0) {
			addActionError("企業不可重複");
			return false;
		}

		bean.setVendorName(createDataDisplay(CpsVendor.class).get(bean.getVendorSysid()));

		String msg = saveBhsMenuSel();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}

		// BhsAdsC-SaveActionStart
		boolean isBannerSuccess = bannerImgExecute(PATH, getDetailInfoMap().get(""));
		if (!isBannerSuccess)
			return false;
		// BhsAdsC-SaveActionEnd

		return super.executeSave();
	}

	// BhsAdsC-bannerDeleteStart
	public final String bannerDelete() {
		return bannerDelete(PATH);
	}

	// BhsAdsC-bannerDeleteEnd

	// 跳轉掠過MAIN頁面功能
	@Override
	public String main() {

		String result = super.main();
		session.remove("userVendorProfiles");
		CpsMember user = getUserAccount();
		if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
			List<BhsVendorProfile> vendorProfiles = cloudDao.queryTable(sf(), BhsVendorProfile.class, new QueryGroup(
					new QueryRule("vendorSysid", user.getSourceSysid())), new QueryOrder[0], null, null);
			if (vendorProfiles.size() > 0) {
				String userVendorProfilesSysid = vendorProfiles.get(0).getSysid();
				String url = getActionKey() + "_edit?bean.sysid=" + userVendorProfilesSysid;

				redirectPage = url;
				return REDIRECT_PAGE;
			}
		}
		return result;
	}
	// 跳轉掠過MAIN頁面功能
}