package tw.com.mitac.thp.action;

// Generated Wed Mar 30 11:43:31 CST 2016 by GenCode.java

import java.util.LinkedHashMap;

import tw.com.mitac.thp.bean.HpsCoreAp;
import tw.com.mitac.thp.bean.HpsCoreApDetail;

/**
 * HpsCoreApAction generated by GenCode.java
 */
public class HpsCoreApAction extends DetailController<HpsCoreAp> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", HpsCoreApDetail.class));
		return detailClassMap;
	}
	
}