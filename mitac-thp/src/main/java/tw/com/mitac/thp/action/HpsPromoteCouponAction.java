package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;

import tw.com.mitac.thp.bean.HpsPromoteCoupon;
import tw.com.mitac.thp.bean.HpsPromoteCouponMember;


public class HpsPromoteCouponAction extends DetailController<HpsPromoteCoupon>{
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", HpsPromoteCouponMember.class));
		return detailClassMap;
	}
	
}

