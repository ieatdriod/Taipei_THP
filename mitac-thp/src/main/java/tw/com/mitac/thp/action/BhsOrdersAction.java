package tw.com.mitac.thp.action;

import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import tw.com.mitac.thp.bean.BhsOrders;
import tw.com.mitac.thp.bean.BhsOrdersFeedback;
import tw.com.mitac.thp.util.Util;

public class BhsOrdersAction extends DetailController<BhsOrders> {
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

	public String outerOrdersSingle() {
		String sysid = request.getParameter("sysid");

		if (StringUtils.isBlank(sysid)) {
			addActionError(getText("msg.itemLost"));
			return ERROR;
		} else {
			bean = cloudDao.get(sf(), getPersistentClass(), sysid);
		}
		return SUCCESS;
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
			bof.setSourceType("SM");//
			snGenerator(dataSet, bof, SN);
			String daoMsg = cloudDao.save(sf(), new Object[] { bof }, true, "INSERT");
			if (!SUCCESS.equals(daoMsg)) {
				resultString = daoMsg;
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultString = e.getMessage();
		}

		return JSON_RESULT;
	}
}