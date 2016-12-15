package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;

import tw.com.mitac.thp.bean.HpsBillSalesOrder;
import tw.com.mitac.thp.bean.HpsBillSalesOrderItem;

public class HpsBillSalesOrderAction extends DetailController<HpsBillSalesOrder> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", HpsBillSalesOrderItem.class));
		return detailClassMap;
	}
}