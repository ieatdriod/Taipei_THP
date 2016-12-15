package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsArticle;
import tw.com.mitac.thp.bean.BhsArticleType;
import tw.com.mitac.thp.bean.MtsArticleType;
import tw.com.mitac.thp.bean.SysConstant;

public class BhsArticleAction extends BasisCrudAction<BhsArticle> {
	
	/**框架圖片功能*/
	@Override
	public String[] getImgCols() {
		return new String[] { "articleImageSummary", "articleImage" };
	}

	
	@Override
	protected boolean executeSave() {
		
		/**儲存時檢核*/
		if ("F".equals(bean.getArticleCategory()) && (bean.getForumCost() == null)) {
			addActionError("新知類型為論壇時，請輸入論壇費用。");
			return false;
		}

		return super.executeSave();
	}

	/** 因依分類拆開繼承-暫時註解 */
	/*
	 * protected QueryGroup createQueryCondition() { List<QueryRule> rules = new
	 * ArrayList<QueryRule>(); if
	 * (StringUtils.isNotBlank(beaninfo.get("articleType"))) rules.add(new
	 * QueryRule("bhsArticleType", EQ, beaninfo.get("articleType")));
	 * 
	 * return new QueryGroup(rules.toArray(new QueryRule[0])); }
	 * 
	 * @Override public String main() { // 下拉選單資源 List<SysConstant>
	 * sysConstantList = cloudDao.queryTable(sf(), SysConstant.class, new
	 * QueryGroup(new QueryRule("constantId", "bhsArticleType")), new
	 * QueryOrder[0], null, null); addMultiLan(sysConstantList, sf(),
	 * MtsArticleType.class);
	 * 
	 * String result = super.main(); if (getQueryCondition() == null ) { String
	 * i = "A"; beaninfo = new HashMap<String, String>();
	 * beaninfo.put("articleType", i); find(); } return result; }
	 */

}