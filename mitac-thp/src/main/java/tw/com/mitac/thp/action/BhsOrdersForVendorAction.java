package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsOrders;
import tw.com.mitac.thp.bean.BhsOrdersFeedback;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

public class BhsOrdersForVendorAction extends DetailController<BhsOrders> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", BhsOrdersFeedback.class));
		return detailClassMap;
	}

	@Override
	public String getActionTitle() {
		String result = super.getActionTitle();
		// 假欄位用MAP
		JSONObject jsonObject = new JSONObject(getConstantMenu().get("orderType"));
		session.put("fakeMap", jsonObject.toString());
		JSONObject jsonObject2 = new JSONObject(getConstantMenu().get("businessType"));
		session.put("fakeMap2", jsonObject2.toString());
		return result;
	}

	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		List<BhsOrders> list = new ArrayList<BhsOrders>();
		// 僅顯示登入者所屬供應商之單據
		if (!CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType()))
			return new Object[] { 0, list };
		QueryGroup newQueryGroup = new QueryGroup(AND, new QueryRule[] { new QueryRule("vendorSysid", getUserAccount()
				.getSourceSysid()) }, queryGroup.getGroups());
		Object[] oldArr = super.jqgridList(clazz, newQueryGroup, orders, from, length);
		list = (List<BhsOrders>) oldArr[1];
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	// 拒絕
	public String turnToR() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.PAYSTATUS_S.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.PAYSTATUS_R);
			} else {
				addActionError("狀態錯誤，無法拒絕");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// 結案
	public String turnToF() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.PAYSTATUS_S.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.PAYSTATUS_F);
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
			BhsOrdersFeedback bof = getDefaultDMO(BhsOrdersFeedback.class);
			Util.defaultPK(bof);
			dataSet.add(bof);
			bof.setParentSysid(parentSysid);
			defaultValue(bof);
			bof.setFeedbackDetail(feedbackDetail);
			bof.setMemberSysid(getUserAccount().getSysid());
			bof.setSourceType("V");//
			snGenerator(dataSet, bof, SN);
			String daoMsg = cloudDao.save(sf(), new Object[] { bof }, true, "INSERT");
			if (!SUCCESS.equals(daoMsg)) {
				resultString = daoMsg;
			}
		} catch (Exception e) {
			resultString = e.getMessage();
		}

		return JSON_RESULT;
	}
}