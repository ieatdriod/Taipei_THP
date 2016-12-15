package tw.com.mitac.thp.action;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsAdvertisementRent;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsEpaper;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreItemType;
import tw.com.mitac.thp.bean.HpsVendorItem;
import tw.com.mitac.thp.util.BillStatusUtil;

public class CpsAdvertisementRent3Action extends BasisCrudAction<CpsAdvertisementRent> {

	protected List<File> img1;
	protected List<String> img1FileName;

	public List<File> getImg1() {
		return img1;
	}

	public void setImg1(List<File> img1) {
		this.img1 = img1;
	}

	public List<String> getImg1FileName() {
		return img1FileName;
	}

	public void setImg1FileName(List<String> img1FileName) {
		this.img1FileName = img1FileName;
	}

	private static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("cpsAd_pic");

	@Override
	protected QueryGroup getQueryRestrict() {
		// 狀態＝結案(同等於付款狀態＝已付款), 日期>系統日期
		QueryRule qRule1 = new QueryRule(BILL_STATUS, BillStatusUtil.FINISH);
		QueryRule qRule2 = new QueryRule("startDate", GT, systemDate);
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(qRule1, qRule2);// QueryGroup.DEFAULT;
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(qRule1, qRule2, new QueryRule("vendorSysid", getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	public String main() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			addActionMessage("僅顯示即將上架的項目，管理者可以查詢所有項目");
		}
		return super.main();
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<CpsAdvertisementRent> list = (List<CpsAdvertisementRent>) oldArr[1];
		for (CpsAdvertisementRent bean : list) {
			// 資料表名稱，並決定比對欄位
			if (StringUtils.isBlank(bean.getSourceSysid())) {
				bean.setSourceSysid("");
			} else if (CpsEpaper.class.getSimpleName().equals(bean.getSourceType())) {
				CpsEpaper source = createDataTable(CpsEpaper.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getCreator() + getSplitChar() + source.getTitle());
			} else if (HpsVendorItem.class.getSimpleName().equals(bean.getSourceType())) {
				HpsVendorItem source = createDataTable(HpsVendorItem.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getDataId() + getSplitChar() + source.getName());
			} else if (HpsCoreItemType.class.getSimpleName().equals(bean.getSourceType())) {
				HpsCoreItemType source = createDataTable(HpsCoreItemType.class).get(bean.getSourceSysid());
				if (source != null)
					bean.setSourceSysid(source.getDataId() + getSplitChar() + source.getName());
			} else {
				bean.setSourceSysid("");
			}
		}
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSysid())) {
			CpsMember user = getUserAccount();
			String userSysid = (String) user.getSysid();
			bean.setIssueMemberSysid(userSysid);
			List<CpsEntity> entitys = cloudDao.queryTable(sf(), CpsEntity.class, new QueryGroup(new QueryRule("dataId",
					"hps")), new QueryOrder[0], null, null);
			if (entitys.size() > 0) {
				bean.setEntitySysid(entitys.get(0).getSysid());
			}
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid("*");
			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setVendorSysid(user.getSourceSysid());
			}
			bean.setFilePath("*");
		}

		String result = super.edit();
		if (StringUtils.isBlank(bean.getSourceSysid())) {
			bean.setSourceSysid("");
		} else if (CpsEpaper.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("epaperSysid", bean.getSourceSysid());
			CpsEpaper source = createDataTable(CpsEpaper.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("epaperSysidShow", source.getCreator() + getSplitChar() + source.getTitle());
		} else if (HpsVendorItem.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("vendorItemSysid", bean.getSourceSysid());
			HpsVendorItem source = createDataTable(HpsVendorItem.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("vendorItemSysidShow", source.getDataId() + getSplitChar() + source.getName());
		} else if (HpsCoreItemType.class.getSimpleName().equals(bean.getSourceType())) {
			beaninfo.put("vendorItemSysid", bean.getSourceSysid());
			HpsCoreItemType source = createDataTable(HpsCoreItemType.class).get(bean.getSourceSysid());
			if (source != null)
				beaninfo.put("vendorItemSysidShow", source.getDataId() + getSplitChar() + source.getName());
		} else {
			bean.setSourceSysid("");
		}
		return result;
	}
}