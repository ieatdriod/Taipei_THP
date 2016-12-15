package tw.com.mitac.thp.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;

public class CpsMeetingHistoricalRecordAction extends CpsMeetingAction {

	/**框架按鈕處理*/
	
	public boolean getWithoutSaveBtn() {
		return true;
	}

	public boolean getWithoutSaveAndNew() {
		return true;
	}

	public boolean getWithoutSaveAndReturnMain() {
		return true;
	}
	
	/**顯示 刪除尾檔功能按鈕 + 操作按鈕功能(單筆寄信) 用來歷史資訊 隱藏 */
	public Boolean getShowSubtle() {
		
		return false;
	}
	
	/**
	 * MAIN頁面篩選功能-篩選開始前15分鐘後:現有時間+15分鐘 比對 會議開始時間 = 會議開始前-15分鐘 比對 現有時間
	 */

	@Override
	protected QueryGroup getQueryRestrict() {
		List<QueryGroup> queryGroupList = new ArrayList<>();
		List<QueryRule> queryRuleList = new ArrayList<>();

		/** 處理觀看人 */
		String sourceId = "";
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			CpsEntity entity = getDataCpsEntityETable().get(getUserAccount().getSourceSysid());
			if (entity.getDataId().equals("mts")) {
				sourceId = "MTS";
			} else if (entity.getDataId().equals("bhs")) {
				sourceId = "BHS";
			} else if (entity.getDataId().equals("cps")) {
				sourceId = "ADMIN";
			}
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			sourceId = getUserAccount().getSourceSysid();
		}
		queryRuleList.add(new QueryRule("sourceId", sourceId));
		logger.debug("觀看歷史人是:" + sourceId);

		/** 處理時間計算 */
		DateFormat formatTime = new SimpleDateFormat("HH:mm");
		DateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
		DateFormat formatOffsideDate = new SimpleDateFormat("yyyy/MM/dd");

		Calendar testOffsideCr = Calendar.getInstance();
		testOffsideCr.setTime(systemDatetime);
		testOffsideCr.add(Calendar.MINUTE, +15);
		Date newDateOffside = testOffsideCr.getTime();

		Date A = null;
		try {
			A = formatOffsideDate.parse(formatOffsideDate.format(systemDatetime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Date B = null;
		try {
			B = formatOffsideDate.parse(formatOffsideDate.format(newDateOffside));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Boolean offside;
		if (B.compareTo(A) > 0) {
			offside = true;
		} else {
			offside = false;
		}

		if (!offside) {
			logger.debug("沒有超過於24小時");
			/** 處理時間 +10分 */
			Calendar calendarT = Calendar.getInstance();
			calendarT.setTime(systemDatetime);
			calendarT.add(Calendar.MINUTE, +15);
			Date newT = calendarT.getTime();
			String sT = formatTime.format(newT);
			logger.debug("今天時間:" + sT);

			/** 處理日期+0 */
			String sD = formatDate.format(systemDate);
			logger.debug("今天日期:" + sD);

			queryGroupList.add(new QueryGroup(OR, new QueryRule[] { new QueryRule("meetingDate", LT, sD) },
					new QueryGroup[] { new QueryGroup(new QueryRule("meetingDate", EQ, sD),
							new QueryRule("meetingStartTime", LE, sT)) }));

			/*
			 * return new QueryGroup(OR, new QueryRule[] {}, new QueryGroup[] {
			 * new QueryGroup(new QueryRule("meetingDate", LT, sD)), new
			 * QueryGroup(new QueryRule("meetingDate", EQ, sD), new
			 * QueryRule("meetingStartTime", LE, sT)) });
			 * 
			 * return new QueryGroup(OR, new QueryRule[] { new
			 * QueryRule("meetingDate", LT, sD) }, new QueryGroup[] { new
			 * QueryGroup(new QueryRule("meetingDate", EQ, sD), new
			 * QueryRule("meetingStartTime", LE, sT)) });
			 */

		} else {
			logger.debug("超過於24小時");
			/** 處理時間 +10分 */
			Calendar calendarT = Calendar.getInstance();
			calendarT.setTime(systemDatetime);
			calendarT.add(Calendar.MINUTE, +15);
			Date newT = calendarT.getTime();
			String sT = formatTime.format(newT);
			logger.debug("今天時間:" + sT);

			/** 處理日期 +1 */
			Calendar calendarD = Calendar.getInstance();
			calendarD.setTime(systemDatetime);
			calendarD.add(Calendar.DATE, +1);
			Date newD = calendarD.getTime();
			String sD = formatTime.format(newD);
			logger.debug("今天時間:" + sD);

			queryGroupList.add(new QueryGroup(OR, new QueryRule[] { new QueryRule("meetingDate", LT, sD) },
					new QueryGroup[] { new QueryGroup(new QueryRule("meetingDate", EQ, sD),
							new QueryRule("meetingStartTime", LE, sT)) }));
		}
		
		return new QueryGroup(AND, queryRuleList.toArray(new QueryRule[0]), queryGroupList.toArray(new QueryGroup[0]));

	}

}