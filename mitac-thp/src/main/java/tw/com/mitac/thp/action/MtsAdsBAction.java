package tw.com.mitac.thp.action;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.thp.bean.MtsAdsB;

public class MtsAdsBAction extends BasisCrudAction<MtsAdsB> {
	@Override
	public String[] getImgCols() {
		return new String[] { "adsImage" };
	}

	@Override
	protected boolean executeSave() {
		// 檢核
		String bannerType = bean.getAdsType();

		if (StringUtils.isBlank(bean.getAdsUrl())) {
			addActionError("請輸入連結網址");
			return false;
		} else if ("G".equals(bannerType)) {

			if (StringUtils.isBlank(bean.getForumSysid())) {
				addActionError("請選擇一項展覽館資料");
				return false;

			}

		}

		boolean result = super.executeSave();

		// 此檢核不能寫在存檔前，否則上傳圖片時會出錯，僅顯示訊息給USER看
		if ("S".equals(bannerType)) {
			if (StringUtils.isBlank(bean.getAdsImage())) {
				addActionError("貼心小提醒:您未上傳圖片");
			}
		}
		return result;
	}
}