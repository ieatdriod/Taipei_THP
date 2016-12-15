package tw.com.mitac.thp.action.app;

import java.util.List;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.thp.action.BasisLoginAction;
import tw.com.mitac.thp.bean.CpsTopmarquee;

public class TopMarqueeAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "app/TopMarquee1";
	}

	private String topmarqueeText;

	public String getTopmarqueeText() {
		return topmarqueeText;
	}

	public void setTopmarqueeText(String topmarqueeText) {
		this.topmarqueeText = topmarqueeText;
	}

	public String init() {
		// TODO 把Config資料撈出來放在Session裡

		// 取得該集合的session
		List<CpsTopmarquee> CpsTopmarqueeKeyword = (List<CpsTopmarquee>) session.get("KeywordList");
		// 判斷是否為null，如果是就產生新的值，不是就回傳
		if (CpsTopmarqueeKeyword == null) {
			CpsTopmarqueeKeyword = cloudDao.queryTable(sf(), CpsTopmarquee.class, new QueryGroup(
			// GT:> LT:< GE:>= LE:<= EQ=
			// new QueryRule("startDate",LE,systemDate),
					// new QueryRule("endDate",GE,systemDate)
					), new QueryOrder[0],
					// 起始點預設null
					0,
					// 顯示筆數null
					1);
			// logger.debug(hpsCoreSearchKeyword);
			session.put("KeywordList", CpsTopmarqueeKeyword);
		}
		return SUCCESS;
	}

	public String save() {
		// TODO 執行存檔 從頁面收到資料存進資料庫
		logger.debug(topmarqueeText);

		// 1.在查出來一次
		List<CpsTopmarquee> CpsTopmarqueeKeyword = (List<CpsTopmarquee>) session.get("KeywordList");
		if (CpsTopmarqueeKeyword == null) {
			CpsTopmarqueeKeyword = cloudDao.queryTable(sf(), CpsTopmarquee.class, new QueryGroup(),

			new QueryOrder[0],
			// 起始點預設null
					0,
					// 顯示筆數null
					1);
			session.put("KeywordList", CpsTopmarqueeKeyword);
		}

		// 2.執行SET
		for (CpsTopmarquee bean : CpsTopmarqueeKeyword) {

			bean.setTopmarqueeText(topmarqueeText);

		}

		// 3.執行SAVE
		cloudDao.save(sf(), CpsTopmarqueeKeyword);

		return SUCCESS;
	}
}