package tw.com.mitac.thp.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.ssh.util.DateTypeConverter;
import tw.com.mitac.thp.bean.CpsKeywords;


public class CpsKeywords2Action extends BasisCrudAction<CpsKeywords> {

	//加入常用查詢條件
	protected QueryGroup createQueryCondition() {
		List<QueryRule> rules = null;
		try {
			
			//logger.debug("doCpsMeetingData-operationDateS:" + beaninfo.get("operationDateS"));
			//logger.debug("doCpsMeetingData-operationDateE:" + beaninfo.get("operationDateE"));
			
			rules = new ArrayList<QueryRule>();
			if (StringUtils.isNotBlank(beaninfo.get("operationDateS"))) {
				if (StringUtils.isNotBlank(beaninfo.get("operationDateE"))) {
					if (beaninfo.get("operationDateS").equals(beaninfo.get("operationDateE"))) {
						rules.add(new QueryRule("operationDate", GE, beaninfo.get("operationDateS")));
						
						Date operationDate = DateTypeConverter.convertFromString(beaninfo.get("operationDateS"));
						Calendar cal = Calendar.getInstance();
						cal.setTime(operationDate);
						cal.add(Calendar.DATE, 1); //日期加一天
						Date displayDate2=cal.getTime();
						
						//logger.debug("doCpsMeetingData-displayDate2:" + displayDate2);
						SimpleDateFormat formatter=new SimpleDateFormat("yyyy/MM/dd");
						//logger.debug("doCpsMeetingData-displayDate3:" + formatter.format(displayDate2));
						
						rules.add(new QueryRule("operationDate", LT, formatter.format(displayDate2)));
						
					}
				}
			}
			
			if (rules.size() == 0) {
				rules = new ArrayList<QueryRule>();
				String dateStr = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
				rules.add(new QueryRule("operationDate", GE, dateStr));
				rules.add(new QueryRule("operationDate", LT, dateStr));
			}			
			
			
			
			
			
//			if (StringUtils.isNotBlank(beaninfo.get("operationDateS"))) {
//				rules.add(new QueryRule("operationDate", GE, beaninfo.get("operationDateS")));
//			                                           //大於
//			}
//			if (StringUtils.isNotBlank(beaninfo.get("operationDateE"))) {
//				rules.add(new QueryRule("operationDate", LT, beaninfo.get("operationDateE")));
//                                                      //小於
//			}
//			
//			if (rules.size() == 0) {
//				rules = new ArrayList<QueryRule>();
//				String dateStr = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
//				rules.add(new QueryRule("operationDate", GE, dateStr));
//				rules.add(new QueryRule("operationDate", LT, dateStr));
//			}			
		} catch (Exception e) {
			
		}
		return new QueryGroup(rules.toArray(new QueryRule[0]));
	}	
	
	
}
