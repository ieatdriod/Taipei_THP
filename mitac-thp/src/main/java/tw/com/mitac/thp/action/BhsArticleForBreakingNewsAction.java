package tw.com.mitac.thp.action;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;

public class BhsArticleForBreakingNewsAction extends BhsArticleAction {

	/** 框架MAIN頁面篩選 A:Breaking News - B:Taiwan Insight */
	@Override
	protected QueryGroup getQueryRestrict() {
		return new QueryGroup(new QueryRule("bhsArticleType", "A"));
	}
	
	/** 預設EDIT就是固定 */
	@Override
	public String edit() {
		String result = super.edit();
		bean.setBhsArticleType("A");
		return result;
	}

}