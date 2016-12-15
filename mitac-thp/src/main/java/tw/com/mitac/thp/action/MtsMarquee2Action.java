package tw.com.mitac.thp.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsMarquee;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsMarquee;

public class MtsMarquee2Action extends BasisCrudAction<MtsMarquee> {

	// 此功能館主可看全部單,關閉排序功能
		@Override
		public Boolean getJqgridDefaultSoab() {
			if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType()))
				return true;
			return false;
		}
		
		@Override
		public String getJqgridDefaultSidx() {
			if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
				return SOURCE_ID;
			return super.getJqgridDefaultSidx();
		}	
	
	
		@Override
		protected QueryGroup getQueryRestrict() {
			if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				return QueryGroup.DEFAULT;
			} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				return new QueryGroup(new QueryRule(SOURCE_ID, getUserAccount().getSourceSysid()));
			} else {
				return new QueryGroup(new QueryRule(PK, "x"));
			}
		}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSourceId())) {
			CpsMember user = getUserAccount();
			if (CpsEntity.class.getSimpleName().equals(user.getSourceType())) {
				bean.setSourceId("MTS");
			} else if (CpsVendor.class.getSimpleName().equals(user.getSourceType())) {
				bean.setSourceId(user.getSourceSysid());
			}
		}
		String result = super.edit();
		return result;
	}
	
	
	@Override
	protected Object[] jqgridList(Class<?> clazz, QueryGroup queryGroup, QueryOrder[] orders, Integer from,
			Integer length) {
		Object[] oldArr = super.jqgridList(clazz, queryGroup, orders, from, length);
		List<MtsMarquee> list = (List<MtsMarquee>) oldArr[1];
		for (MtsMarquee bean : list) {
			String sourceId = bean.getSourceId();
			if (!"MTS".equals(sourceId)) {
				CpsVendor source = createDataTable(CpsVendor.class).get(sourceId);
				if (source != null)
					bean.setSourceId(source.getName());
			}
		}
		Object[] newArr = new Object[] { oldArr[0], list };
		return newArr;
	}
	
	
	
	
	
	
	
	// 以下功能已併入basisCrudAction
	
//	@Override
//	public Boolean getJqgridDefaultSoab() {
//		return true;
//	}
//
//	@Override
//	public String getJqgridDefaultSidx() {
//		return "dataOrder";
//	}
//
//	@Override
//	public String getJqgridDefaultSord() {
//		return "asc";
//	}

//	@Override
//	public String edit() {
//		// 預設值
//		if (StringUtils.isBlank(bean.getSysid())) {
//			bean.setDataOrder(1);
//		}
//		String result = super.edit();
//		return result;
//	}

//	@Override
//	public String save() {
//		// 檢核
//		if (bean.getDataOrder() == null || bean.getDataOrder() < 1) {
//			addActionError("排序請輸入正整數（從1開始）");
//			return EDIT_ERROR;
//		}
//
//		MtsMarquee marquee = cloudDao.get(sf(), MtsMarquee.class, bean.getSysid());
//		int total = cloudDao.queryCount(sf(), MtsMarquee.class, new QueryGroup(new QueryRule[0]));
//		String msg = SUCCESS;
//		int newOrder = bean.getDataOrder();
//		if (marquee == null) {
//			if (newOrder < (total + 1)) {
//				// upper
//				msg = shiftOrder(newOrder, total, 1);
//			} else if (newOrder > (total + 1)) {
//				addActionMessage("排序調整:" + bean.getDataOrder() + "->" + (total + 1) + "(最後的序號)");
//				bean.setDataOrder(total + 1);
//			}
//		} else {
//			int oldOrder = marquee.getDataOrder();
//			if (newOrder < oldOrder) {
//				// upper
//				msg = shiftOrder(newOrder, oldOrder - 1, 1);
//			} else if (newOrder > oldOrder) {
//				// downer
//				msg = shiftOrder(oldOrder + 1, newOrder, -1);
//				if (newOrder > total) {
//					addActionMessage("排序調整:" + bean.getDataOrder() + "->" + total + "(最後的序號)");
//					bean.setDataOrder(total);
//				}
//			}
//		}
//		String msg = preSave();
//		if (!SUCCESS.equals(msg)) {
//			addActionError(msg);
//			return EDIT_ERROR;
//		}
//		String result = super.save();
//		return result;
//	}

//	public String ajaxSoab() {
//		String msg = SUCCESS;
//		try {
//			String sysid = request.getParameter("sysid");
//			String newIds = request.getParameter("newIds");
//			int newOrder = Integer.parseInt(newIds);
//
//			MtsMarquee marquee = cloudDao.get(sf(), MtsMarquee.class, sysid);
//			int oldOrder = marquee.getDataOrder();
//			if (newOrder < oldOrder) {
//				// upper
//				msg = shiftOrder(newOrder, oldOrder - 1, 1);
//			} else if (newOrder > oldOrder) {
//				// downer
//				msg = shiftOrder(oldOrder + 1, newOrder, -1);
//			}
//			if (!SUCCESS.equals(msg)) {
//				resultString = msg;
//				return JSON_RESULT;
//			}
//
//			marquee.setDataOrder(newOrder);
//			defaultValue(marquee);
//			saveList.add(marquee);
//			msg = cloudDao.save(sf(), saveList.toArray(), false, "UPDATE");
//			if (!SUCCESS.equals(msg)) {
//				resultString = msg;
//				return JSON_RESULT;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			resultString = "排序發生錯誤";
//			return JSON_RESULT;
//		}
//		resultString = msg;
//		return JSON_RESULT;
//	}
//
//	/**
//	 * 排序位移
//	 * 
//	 * @param startIdx
//	 * @param endIdx
//	 * @param shiftNum
//	 * @return
//	 */
//	protected String shiftOrder(int startIdx, int endIdx, int shiftNum) {
//		try {
//			// HQL
//			Session session = sf().openSession();
//			Transaction tx = session.beginTransaction();
//			String tableName = MtsMarquee.class.getSimpleName();
//			String columnName = "dataOrder";
//			String qStr = "UPDATE " + tableName + " ";
//			qStr += "SET " + columnName + " = " + columnName + " ";
//			if (shiftNum >= 0)
//				qStr += "+ ";
//			qStr += shiftNum + " ";
//			qStr += "WHERE " + columnName + " >= " + startIdx + " ";
//			qStr += "AND " + columnName + " <= " + endIdx + " ";
//			Query query = session.createQuery(qStr);
//			query.executeUpdate();
//			tx.commit();
//			session.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "排序發生錯誤";
//		}
//		return SUCCESS;
//	}
}