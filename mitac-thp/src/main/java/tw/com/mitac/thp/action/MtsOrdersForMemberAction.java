package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersFeedback;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

public class MtsOrdersForMemberAction extends DetailController<MtsOrders> {
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
		// 僅顯示登入者所屬之單據
		// List<String> qList = (List<String>)
		// cloudDao.findProperty(tenancySessionFactory, CpsMember.class,
		// new QueryGroup(new QueryRule("uuid", getUserData().getUid())),
		// new QueryOrder[0], false, "sysid");
		// if (qList.size() == 0)
		// return new Object[] { 0, list };
		QueryGroup newQueryGroup = new QueryGroup(AND, new QueryRule[] { new QueryRule(CR, getUserData().getUid()) },
				queryGroup.getGroups());
		Object[] oldArr = super.jqgridList(clazz, newQueryGroup, orders, from, length);
		list = (List<MtsOrders>) oldArr[1];
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}

	@Override
	protected boolean executeSave() {
		if (StringUtils.isBlank(bean.getBillStatus())) {
			bean.setBillStatus(BillStatusUtil.MTS_NEW);
		}
		return super.executeSave();
	}

	// ============================= 流程1 =============================
	// 驗證
	public String turnToWo() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_NEW.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_WAIT_ORDER);
			} else {
				addActionError("狀態錯誤，無法驗證");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// ============================= 流程2 =============================
	// 付費
	public String payToWa() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_NEW.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_WAIT_APPROVED);
			} else {
				addActionError("狀態錯誤，無法付費");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// 確認
	public String turnToWc() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_WAIT_CONFIRM.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_CONFIRM_ORDER);
			} else {
				addActionError("狀態錯誤，無法確認");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	// 再次審核
	public String turnToWa() {
		try {
			if (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
				addActionError("沒有狀態欄位");
				return EDIT_ERROR;
			}
			String billStatus = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS);
			if (BillStatusUtil.MTS_WAIT_CONFIRM.equals(billStatus)) {
				PropertyUtils.setProperty(bean, Util.BILL_STATUS, BillStatusUtil.MTS_WAIT_APPROVED);
			} else {
				addActionError("狀態錯誤，無法再次審核");
				return EDIT_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return save();
	}

	/*
	 * / 取消 public String turnToZ() { try { if
	 * (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
	 * addActionError("沒有狀態欄位"); return EDIT_ERROR; } String billStatus =
	 * (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS); if
	 * (BillStatusUtil.M.equals(billStatus)) { PropertyUtils.setProperty(bean,
	 * Util.BILL_STATUS, BillStatusUtil.PAYSTATUS_Z); } else {
	 * addActionError("狀態錯誤，無法取消"); return EDIT_ERROR; } } catch (Exception e) {
	 * e.printStackTrace(); } return save(); }
	 * 
	 * // 付款 public String turnToP() { try { if
	 * (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
	 * addActionError("沒有狀態欄位"); return EDIT_ERROR; } String billStatus =
	 * (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS); if
	 * (BillStatusUtil.PAYSTATUS_Q.equals(billStatus)) {
	 * PropertyUtils.setProperty(bean, Util.BILL_STATUS,
	 * BillStatusUtil.PAYSTATUS_P); } else { addActionError("狀態錯誤，無法付款"); return
	 * EDIT_ERROR; } } catch (Exception e) { e.printStackTrace(); } return
	 * save(); }
	 * 
	 * // 取消 public String cancelQ() { try { if
	 * (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
	 * addActionError("沒有狀態欄位，不適用取消功能"); return EDIT_ERROR; } String billStatus
	 * = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS); if
	 * (BillStatusUtil.PAYSTATUS_Q.equals(billStatus)) {
	 * PropertyUtils.setProperty(bean, Util.BILL_STATUS,
	 * BillStatusUtil.PAYSTATUS_Z); } else { addActionError("狀態錯誤，無法取消"); return
	 * EDIT_ERROR; } } catch (Exception e) { e.printStackTrace(); } return
	 * save(); }
	 * 
	 * // 退款 public String cancelP() { try { if
	 * (PropertyUtils.getPropertyDescriptor(bean, Util.BILL_STATUS) == null) {
	 * addActionError("沒有狀態欄位，不適用退款功能"); return EDIT_ERROR; } String billStatus
	 * = (String) PropertyUtils.getProperty(bean, Util.BILL_STATUS); if
	 * (BillStatusUtil.PAYSTATUS_P.equals(billStatus)) {
	 * PropertyUtils.setProperty(bean, Util.BILL_STATUS,
	 * BillStatusUtil.PAYSTATUS_B); } else { addActionError("狀態錯誤，無法退款"); return
	 * EDIT_ERROR; } } catch (Exception e) { e.printStackTrace(); } return
	 * save(); }
	 */
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
			mof.setSourceType("M");//
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