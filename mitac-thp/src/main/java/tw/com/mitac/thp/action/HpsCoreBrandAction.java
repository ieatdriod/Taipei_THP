package tw.com.mitac.thp.action;

import tw.com.mitac.thp.bean.HpsCoreBrand;

public class HpsCoreBrandAction extends BasisCrudAction<HpsCoreBrand> {
	@Override
	public String[] getImgCols() {
		return new String[] { "filePath" };
	}
}