package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersFeedback;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

public class MtsOrdersForVendor2Action extends DetailController<MtsOrders> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsOrdersFeedback.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", MtsOrdersProducts.class));
		detailClassMap.put("3", new DetailInfo("3", DETAIL_SET3, "detail3", MtsOrdersItems.class));
		return detailClassMap;
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		List<MtsOrders> list = new ArrayList<MtsOrders>();
		// 僅顯示登入者所屬供應商之單據
		if (!CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType()))
			return new Object[] { 0, list };
		// 以轉單後供應商才可見
		QueryGroup newQueryGroup = new QueryGroup(AND, new QueryRule[] {
				new QueryRule("vendorSysid", getUserAccount().getSourceSysid()),
				new QueryRule("isOrdersToVendor", true), new QueryRule("ordersType", "SO") }, queryGroup.getGroups());
		Object[] oldArr = super.jqgridList(clazz, newQueryGroup, orders, from, length);
		list = (List<MtsOrders>) oldArr[1];
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	// ============================= 流程1 =============================

	// ============================= 流程2 =============================

	// TODO 結案(應由系統自動結案)
	public String turnToFo() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_CONFIRM_ORDER.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_FINISH);
			} else {
				addActionError("狀態錯誤，無法結案");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// ---------- ---------- ---------- ---------- ----------
	public String saveFeedbackDetail() {
		resultString = SUCCESS;
		try {
			String feedbackDetail = request.getParameter("feedbackDetail");
			String parentSysid = request.getParameter(FK);
			Set dataSet = findDetailSetWhenEdit(DETAIL_SET);

			// 更新資料庫
			MtsOrdersFeedback mof = getDefaultDMO(MtsOrdersFeedback.class);
			Util.defaultPK(mof);
			dataSet.add(mof);
			mof.setParentSysid(parentSysid);
			defaultValue(mof);
			mof.setFeedbackDetail(feedbackDetail);
			mof.setMemberSysid(getUserAccount().getSysid());
			mof.setSourceType("V");//
			snGenerator(dataSet, mof, SN);
			String daoMsg = cloudDao.save(sf(), new Object[] { mof }, true, "INSERT");
			if (!SUCCESS.equals(daoMsg)) {
				resultString = daoMsg;
			}
		} catch (Exception e) {
			resultString = e.getMessage();
		}

		return JSON_RESULT;
	}
}