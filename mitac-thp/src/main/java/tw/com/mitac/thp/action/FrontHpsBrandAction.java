package tw.com.mitac.thp.action;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.HpsCoreBrandType;
import tw.com.mitac.thp.bean.HpsCoreBrandTypeBrand;
import tw.com.mitac.thp.bean.HpsCoreSearchKeyword;

public class FrontHpsBrandAction extends BasisTenancyAction {
	public String execute() {
		
		//取得該集合的session
			List<HpsCoreBrandTypeBrand>	hpsCoreBrandTypeBrandList=(List<HpsCoreBrandTypeBrand>) session.get("hpsCoreBrandTypeBrandList");
			//判斷是否為null，如果是就產生新的值，不是就回傳
		if(hpsCoreBrandTypeBrandList==null){
			hpsCoreBrandTypeBrandList= cloudDao.queryTable(
					sf(), 
					HpsCoreBrandTypeBrand.class,
					new QueryGroup(
							//GT:> LT:< GE:>= LE:<= EQ=  
							), 
					
					new QueryOrder[0]
							//ASC 順時針  DESC逆時針
							,
					// 起始點預設
					null,
					// 顯示筆數
					null);
			session.put("hpsCoreBrandTypeBrandList", hpsCoreBrandTypeBrandList);
		}

		return SUCCESS;
	}
}