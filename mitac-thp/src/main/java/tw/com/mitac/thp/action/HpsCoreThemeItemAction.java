package tw.com.mitac.thp.action;

import java.sql.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsCoreThemeItem;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

public class HpsCoreThemeItemAction extends BasisCrudAction<HpsCoreThemeItem> {

	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return QueryGroup.DEFAULT;
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule("vendorSysid", getUserAccount().getSourceSysid()));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	public String main() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			addActionMessage("管理者可以查詢所有項目");
		}
		return super.main();
	}

	@Override
	public String edit() {
		// 預設值
		if (StringUtils.isBlank(bean.getSysid())) {
			if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
				bean.setVendorSysid(getUserAccount().getSourceSysid());
			}
		}
		String result = super.edit();
		return result;
	}

	// 確認
	@Override
	public String turnToConfirmed() {
		bean.setApplyDate(systemDate);
		bean.setApproveDate(null);
		String result = super.turnToConfirmed();
		if (EDIT_ERROR.equals(result))
			bean.setApplyDate(null);
		return result;
	}

	// 取消確認/退回開立
	@Override
	public String cancelConfirmed() {
		Date temp = new Date(bean.getApplyDate().getTime());
		bean.setApplyDate(null);
		String result = super.cancelConfirmed();
		if (EDIT_ERROR.equals(result))
			bean.setApplyDate(temp);
		return result;
	}

	// 審核
	@Override
	public String turnToApproved() {
		bean.setApproveDate(systemDate);
		String result = super.turnToApproved();
		if (EDIT_ERROR.equals(result))
			bean.setApproveDate(null);
		return result;
	}

	// 退件
	@Override
	public String turnToUnapproved() {
		bean.setApproveDate(systemDate);
		String result = super.turnToUnapproved();
		if (EDIT_ERROR.equals(result))
			bean.setApproveDate(null);
		return result;
	}

	// 取消審核
	@Override
	public String cancelApproved() {
		Date temp = new Date(bean.getApproveDate().getTime());
		bean.setApproveDate(null);
		String result = super.cancelApproved();
		if (EDIT_ERROR.equals(result))
			bean.setApproveDate(temp);
		return result;
	}

}