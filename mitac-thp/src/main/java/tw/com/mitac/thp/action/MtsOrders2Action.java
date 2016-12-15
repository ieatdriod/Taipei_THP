package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersFeedback;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

public class MtsOrders2Action extends DetailAction<MtsOrders> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", MtsOrdersFeedback.class));
		detailClassMap.put("2", new DetailInfo("2", DETAIL_SET2, "detail2", MtsOrdersProducts.class));
		detailClassMap.put("3", new DetailInfo("3", DETAIL_SET3, "detail3", MtsOrdersItems.class));
		return detailClassMap;
	}

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

	// ============================= 流程1 =============================

	// ============================= 流程2 =============================
	// 審核
	public String turnToWc() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_WAIT_APPROVED.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_WAIT_CONFIRM);
			} else {
				addActionError("狀態錯誤，無法審覆");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// 取消訂單
	public String turnToCa() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_WAIT_APPROVED.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_CANCEL_ORDER);

				// TODO 信用卡退款模組
				// ...

				// TODO 寄出道歉信函
				// ...
			} else {
				addActionError("狀態錯誤，無法取消");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// 轉單供應商
	public String turnToVd() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_CONFIRM_ORDER.equals(billStatus)) {
				if (StringUtils.isBlank(bean.getVendorSysid()) || "*".equals(bean.getVendorSysid())) {
					addActionError("未選擇供應商");
					return EDIT_ERROR;
				}
				PropertyUtils.setProperty(bean, "isOrdersToVendor", true);
			} else {
				addActionError("狀態錯誤，無法轉單");
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
			mof.setSourceType("SM");//
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