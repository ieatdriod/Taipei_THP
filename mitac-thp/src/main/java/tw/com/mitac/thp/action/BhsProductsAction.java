package tw.com.mitac.thp.action;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsAdsC;
import tw.com.mitac.thp.bean.BhsInfoLink;
import tw.com.mitac.thp.bean.BhsMenuLink;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsProductsCategoryLink;
import tw.com.mitac.thp.bean.BhsRecommandItem;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.Util;

public class BhsProductsAction extends DetailController<BhsProducts> {
	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("bhsProducts_pic");

	protected String productsCategory;

	public String getProductsCategory() {
		return productsCategory;
	}

	public void setProductsCategory(String productsCategory) {
		this.productsCategory = productsCategory;
	}

	protected String getProductsCategorySel() {
		List<String> list = (List<String>) cloudDao.findProperty(sf(), BhsProductsCategoryLink.class, new QueryGroup(
				new QueryRule(FK, bean.getSysid())), new QueryOrder[0], false, "categorySysid");
		String str = "";
		for (String s : list)
			str += ", " + s;
		return StringUtils.isBlank(str) ? "" : str.substring(2);
	}

	protected String saveProductsCategory() {
		if (StringUtils.isNotBlank(productsCategory)) {
			// 1.刪除
			String sysid = bean.getSysid();
			String daoMsg = cloudDao.save(sf(), new DeleteStatement(BhsProductsCategoryLink.class.getSimpleName(),
					new QueryGroup(new QueryRule(FK, sysid))));
			if (!SUCCESS.equals(daoMsg))
				return daoMsg;

			// 2.新增勾選的選單分類
			String[] arr = productsCategory.split(", ");
			for (int i = 0; i < arr.length; i++) {
				if (StringUtils.isNotBlank(arr[i])) {
					BhsProductsCategoryLink pcl = new BhsProductsCategoryLink();
					Util.defaultPK(pcl);
					defaultValue(pcl);
					pcl.setCategorySysid(arr[i].toString());
					pcl.setParentSysid(sysid);
					saveList.add(pcl);
				}
			}
		}
		return SUCCESS;
	}

	@Override
	public String[] getImgCols() {
		return new String[] { "productsImageSummary1" };
	}

	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", BhsInfoLink.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", BhsAdsC.class));
		return detailClassMap;
	}

	// 此功能館主可看全部單,關閉排序功能
	@Override
	public Boolean getJqgridDefaultSoab() {
		if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType()))
			return true;
		return false;
	}

	@Override
	public String getJqgridDefaultSidx() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
			return "vendorSysid";
		return super.getJqgridDefaultSidx();
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

	@Override
	public String edit() {
		if (StringUtils.isBlank(bean.getSysid())
				&& CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType()))
			bean.setVendorSysid(getUserAccount().getSourceSysid());

		String result = super.edit();

		menuSel = getBhsMenuSel();
		productsCategory = getProductsCategorySel();
		return result;
	}

	@Override
	protected boolean executeSave() {
		// 判斷中文字數
		String p1 = bean.getProductsSummary1();
		String p2 = bean.getProductsSummary2();
		String p3 = bean.getProductsSummary3();

		if (StringUtils.isNotBlank(p1) && p1.getBytes(Charset.forName("BIG5")).length != p1.length()) {
			if (p1.length() > 10) {
				addActionError("字數過長");
				return false;
			}
		} else if (StringUtils.isNotBlank(p2) && p2.getBytes(Charset.forName("BIG5")).length != p2.length()) {
			if (p2.length() > 10) {
				addActionError("字數過長");
				return false;
			}
		} else if (StringUtils.isNotBlank(p3) && p3.getBytes(Charset.forName("BIG5")).length != p3.length()) {
			if (p3.length() > 10) {
				addActionError("字數過長");
				return false;
			}
		}

		String msg = saveProductsCategory();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}

		msg = saveBhsMenuSel();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}

		msg = updateBhsInfoLinkPic();
		if (!SUCCESS.equals(msg)) {
			addActionError(msg);
			return false;
		}

		// MtsAdsC-SaveActionStart
		boolean isBannerSuccess = bannerImgExecute(PATH, getDetailInfoMap().get("2"));
		if (!isBannerSuccess)
			return false;
		// MtsAdsC-SaveActionEnd

		return super.executeSave();
	}

	@Override
	public String delete() {
		String deletePk = bean.getSysid();
		if (StringUtils.isNotBlank(deletePk)) {
			int c = cloudDao.queryTableCount(sf(), BhsRecommandItem.class, new QueryGroup(new QueryRule("sourceSysid",
					deletePk)));
			if (c > 0) {
				addActionError(getText("delete.error.recommand"));
				return EDIT_ERROR;
			}
			saveList.add(new DeleteStatement(BhsMenuLink.class.getSimpleName(), new QueryGroup(new QueryRule(FK,
					deletePk))));
		}
		return super.delete();
	}

	public final String bannerDelete() {
		return bannerDelete(PATH);
	}
}