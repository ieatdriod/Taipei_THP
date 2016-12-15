package tw.com.mitac.thp.action;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsAdsC;
import tw.com.mitac.thp.bean.MtsVendorCategoryLink;
import tw.com.mitac.thp.bean.MtsVendorProfile;
import tw.com.mitac.thp.util.Util;

public class MtsVendorProfileAction extends DetailController<MtsVendorProfile> {
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

		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsAdsC.class));

		return detailClassMap;
	}

	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("mtsVendorProfile_pic");

	protected String getSavePath() {
		return PATH;
	}

	@Override
	public String[] getImgCols() {
		return new String[] { "vendorImageSummary" };
	}

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

	// 20160715修正模仿BhsMenu版本-start
	// mts_vendor_category_link用
	protected String menuTeamCertificationSel;

	public String getMenuTeamCertificationSel() {
		return menuTeamCertificationSel;
	}

	public void setMenuTeamCertificationSel(String menuTeamCertificationSel) {
		this.menuTeamCertificationSel = menuTeamCertificationSel;
	}

	protected String getMtsTeamCertificationSel() {
		String menuTeamCertificationSel = "";
		try {
			String teamCertificationSysid = (String) PropertyUtils.getProperty(bean, PK);
			List<String> teamCertificationList = (List<String>) cloudDao.findProperty(sf(),
					MtsVendorCategoryLink.class, new QueryGroup(new QueryRule("sourceSysid", teamCertificationSysid)),
					new QueryOrder[0], false, "categorySysid");
			for (String tTeam : teamCertificationList)
				menuTeamCertificationSel += ", " + tTeam;
			menuTeamCertificationSel = StringUtils.isBlank(menuTeamCertificationSel) ? "" : menuTeamCertificationSel
					.substring(2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return menuTeamCertificationSel;
	}

	protected String saveMtsTeamCertificationSel() {
		try {
			// 1.刪除
			String teamCertificationSysid = (String) PropertyUtils.getProperty(bean, PK);
			String daoMsgToTeam = cloudDao.save(sf(), new DeleteStatement(MtsVendorCategoryLink.class.getSimpleName(),
					new QueryGroup(new QueryRule("sourceSysid", teamCertificationSysid))));
			if (!SUCCESS.equals(daoMsgToTeam))
				return daoMsgToTeam;

			// 2.新增勾選的選單分類
			if (StringUtils.isNotBlank(menuTeamCertificationSel)) {
				String[] arrToTeam = menuTeamCertificationSel.split(", ");
				for (String string : arrToTeam)
					if (StringUtils.isNotBlank(string)) {
						MtsVendorCategoryLink mlToTeam = new MtsVendorCategoryLink();
						Util.defaultPK(mlToTeam);
						defaultValue(mlToTeam);
						mlToTeam.setCategorySysid(string);
						mlToTeam.setSourceSysid(teamCertificationSysid);
						saveList.add(mlToTeam);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	// 20160715修正模仿BhsMenu版本-end

	@Override
	protected boolean executeSave() {

		// 顯示名簡易稱
		bean.setVendorName(createDataDisplay(CpsVendor.class).get(bean.getVendorSysid()));

		// 20160715修正模仿BhsMenu版本-start-TeamCertification
		String msg2 = saveMtsTeamCertificationSel();
		if (!SUCCESS.equals(msg2)) {
			addActionError(msg2);
			return false;
		}
		// 20160715修正模仿BhsMenu版本-end

		// 20160715修正模仿BhsMenu版本-start
		String msg = saveMtsMenuSel();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}
		// 20160715修正模仿BhsMenu版本-end

		// MtsAdsC-SaveActionStart
		boolean isBannerSuccess = bannerImgExecute(PATH, getDetailInfoMap().get(""));
		if (!isBannerSuccess)
			return false;
		// MtsAdsC-SaveActionEnd

		return super.executeSave();
	}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getVendorSysid())) {
			CpsMember user = getUserAccount();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid("MTS");
			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid(user.getSourceSysid());
			}
		}
		String result = super.edit();

		// 20160715修正模仿BhsMenu版本-start
		menuSel = getMtsMenuSel();
		// 20160715修正模仿BhsMenu版本-end

		// 20160715修正模仿BhsMenu版本-start-TeamCertification
		menuTeamCertificationSel = getMtsTeamCertificationSel();
		// 20160715修正模仿BhsMenu版本-end

		return result;
	}

	// MtsAdsC-bannerDeleteStart
	public final String bannerDelete() {
		return bannerDelete(PATH);
	}

	// MtsAdsC-bannerDeleteEnd

	// 跳轉掠過MAIN頁面功能

	@Override
	public String main() {

		String result = super.main();
		session.remove("userVendorProfiles");
		CpsMember user = getUserAccount();
		if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
			List<MtsVendorProfile> vendorProfiles = cloudDao.queryTable(sf(), MtsVendorProfile.class, new QueryGroup(
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