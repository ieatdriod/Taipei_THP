package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.MtsItems;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersFeedback;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;
import tw.com.mitac.thp.util.Util;

//public class MtsOrders3Action extends DetailController<MtsOrders> {
public class MtsOrders3Action extends DetailAction<MtsOrders> {	
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsOrdersFeedback.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", MtsOrdersProducts.class));
		detailClassMap.put("3", new DetailInfo("3", DETAIL_SET3, "detail3", MtsOrdersItems.class));
		return detailClassMap;
	}
	
//	/**
//	 * [jqgrid]
//	 */
//	protected Map<String, Map> getJqgridColModelMap() {
//		Map<String, Map> jqgridColModelMap = super.getJqgridColModelMap();
//		Map<String, Object> colModelMap = new HashMap<String, Object>();
//		jqgridColModelMap.put("mtsItemName", colModelMap);
//		colModelMap.put("name", "mtsItemName");
//		colModelMap.put("index", "mtsItemName");
//		colModelMap.put("align", getJqgridTextAlign());
//		colModelMap.put("sortable", true); // sql order
//		colModelMap.put("editable", true);
//		colModelMap.put("label", "*訂單品項*");
//		colModelMap.put("width", 150);
//		return jqgridColModelMap;
//	}	
	
//	//jqgrid再加上過濾條件
//	@Override
//	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
//			Integer length) {
//  		Object[] arr = super.jqgridList(clazz, queryGroup, orders, from, length);
//  		
//  		List<MtsOrders> oldResults = (List<MtsOrders>) arr[1];
//  		List<Map> newResults = new ArrayList<Map>();
//
//System.out.println("@@@@@@@@@@@@@@@@-aaa[" + oldResults.size() + "]");  		
//  		
//  		for (MtsOrders mtsOrders : oldResults) {
//			//Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(mtsOrders);
//			//newResults.add(map);
//
//			List<String> mtsItemsSysidList = (List<String>) cloudDao.findProperty(sf(), MtsOrdersItems.class,
//					new QueryGroup(new QueryRule(FK, mtsOrders.getSysid())), new QueryOrder[0], 
//					false, "mtsItemsSysid");
//
//System.out.println("@@@@@@@@@@@@@@@@-bbb[" + mtsItemsSysidList.size() + "]");			
//			
//			if (mtsItemsSysidList.size() > 0) {
//				for (String string : mtsItemsSysidList) {
//					Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(mtsOrders);
//					newResults.add(map);
//					String text = createDataDisplay(MtsItems.class).get(string);
//					map.put("mtsItemName", text);
//				}
//			}//else{
//			//	//mts_orders_item沒有資料
//			//	Map<String, Object> map = tw.com.mitac.ssh.util.Util.formatToMap(mtsOrders);
//			//	newResults.add(map);
//			//	String text = "";
//			//	map.put("mtsItemName", text);
//			//}
//  		}
//  		return new Object[] { arr[0], newResults };
//	}
	
	
	
	
	
	@Override
	protected boolean escapeJoin() {
		return false;
	}

	@Override
	protected DetailInfo getJoinDetailInfo() {
		return getDetailInfoMap().get("3");
	}

	@Override
	protected QueryGroup getQueryRestrict() {
		// 以轉單後供應商才可見
		if (escapeJoin()) {
			QueryGroup newQueryGroup = new QueryGroup(new QueryRule("ordersType", "SO"));
			return newQueryGroup;
		} else {
			String as1 = Util.buildJoinTableFrontKey(getPersistentClass());
			QueryGroup newQueryGroup = new QueryGroup(new QueryRule(as1 + "_" + "ordersType", "SO"));
			return newQueryGroup;
		}
	}
	
	
	
	
	
//	@Override
//	protected QueryGroup getQueryRestrict() {
//		// 以轉單後供應商才可見
//		QueryGroup newQueryGroup = new QueryGroup(
//		// new QueryRule(BILL_STATUS, "mts90"),
//				new QueryRule("ordersType", "SO"));
//		return newQueryGroup;
//	}	
	
	//加入常用查詢條件
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = null;
		try {
			rules = new ArrayList<QueryRule>();
			if (StringUtils.isNotBlank(beaninfo.get("issueDateS"))) {
				rules.add(new QueryRule("issueDate", GE, sdfYMD.parse(beaninfo.get("issueDateS"))));
			                                       //大於
			}
			if (StringUtils.isNotBlank(beaninfo.get("issueDateE"))) {
				rules.add(new QueryRule("issueDate", LE, sdfYMD.parse(beaninfo.get("issueDateE"))));
                                                   //小於等於
			}
			
			if (rules.size() == 0) {
				rules = new ArrayList<QueryRule>();
				String dateStr = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
				rules.add(new QueryRule("issueDate", GE, sdfYMD.parse(dateStr)));
				rules.add(new QueryRule("issueDate", LE, sdfYMD.parse(dateStr)));
			}			
		} catch (Exception e) {
			
		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}

}