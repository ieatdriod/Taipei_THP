package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;

import tw.com.mitac.thp.bean.BhsVendorMore;
import tw.com.mitac.thp.bean.BhsVendorMoreItem;

public class BhsVendorMoreAction extends DetailController<BhsVendorMore> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", BhsVendorMoreItem.class));
		return detailClassMap;
	}

	@Override
	protected boolean executeSave() {
		session.remove("bhsVendorMoreList");
		return super.executeSave();
	}
}