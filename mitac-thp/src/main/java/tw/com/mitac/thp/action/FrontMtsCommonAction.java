package tw.com.mitac.thp.action;

public class FrontMtsCommonAction extends BasisTenancyAction {

	private int sub_pages = 10;

	/**
	 * 分頁 當前第1/453頁 [首頁] [上頁] 1 2 3 4 5 6 7 8 9 10 [下頁] [尾頁] nowShowItemPage
	 * 目前頁數 pageNums 總頁數 subPage_link 頁碼連結
	 */
	public String subPageCss2(int nowShowItemPage, int pageNums, String subPage_link) {
		String subPageCss2Str = "";
		// subPageCss2Str += "當前第" + nowShowItemPage + "/" + pageNums + "頁 ";

		if (nowShowItemPage > 1) {
			String firstPageUrl = subPage_link + "1";
			String prewPageUrl = subPage_link + (nowShowItemPage - 1);
			// subPageCss2Str += "[<a href='" + firstPageUrl + "'>首頁</a>] ";
			subPageCss2Str += "<a href='" + prewPageUrl + "'>Previous</a> ";
		} else {
			// subPageCss2Str += "[首頁] ";
			subPageCss2Str += "Previous ";
		}

		java.util.ArrayList<Integer> a = construct_num_Page(pageNums, sub_pages, nowShowItemPage);
		int s;
		for (int i = 0; i < a.size(); i++) {
			s = a.get(i);
			if (s == nowShowItemPage) {
				subPageCss2Str += "<span style='color:red;font-weight:bold;'>&nbsp" + s + "&nbsp</span>";
			} else {
				String url = subPage_link + s;
				subPageCss2Str += "<a href='" + url + "'>&nbsp" + s + "&nbsp</a>";
			}
		}

		if (nowShowItemPage < pageNums) {
			String lastPageUrl = subPage_link + pageNums;
			String nextPageUrl = subPage_link + (nowShowItemPage + 1);
			subPageCss2Str += " <a href='" + nextPageUrl + "'>Next</a> ";
			// subPageCss2Str += "[<a href='" + lastPageUrl + "'>尾頁</a>] ";
		} else {
			subPageCss2Str += "Next ";
			// subPageCss2Str += "[尾頁] ";
		}
		return subPageCss2Str;
	}

	/**
	 * construct_num_Page該函數使用來構造顯示的條目 即使：[1][2][3][4][5][6][7][8][9][10]
	 * pageNums 總頁數 sub_pages nowShowItemPage 目前頁數
	 */
	public java.util.ArrayList<Integer> construct_num_Page(int pageNums, int sub_pages, int nowShowItemPage) {
		java.util.ArrayList<Integer> current_array = new java.util.ArrayList<Integer>();
		if (pageNums < sub_pages) {
			// current_array=array();
			for (int i = 0; i < pageNums; i++) {
				current_array.add(i, i + 1);
			}
		} else {
			current_array = this.initArray(sub_pages);
			if (nowShowItemPage <= 3) {
				for (int i = 0; i < current_array.size(); i++) {
					current_array.set(i, i + 1);
				}
			} else if (nowShowItemPage <= pageNums && nowShowItemPage > pageNums - sub_pages + 1) {
				for (int i = 0; i < current_array.size(); i++) {
					current_array.set(i, (pageNums) - (sub_pages) + 1 + i);
				}
			} else {
				for (int i = 0; i < current_array.size(); i++) {
					current_array.set(i, nowShowItemPage - 2 + i);
				}
			}
		}
		for (int i = 0; i < current_array.size(); i++)
			System.out.print(current_array.get(i) + " ");
		System.out.println();
		return current_array;
	}

	/**
	 * 用來給建立分頁的數組初始化的函數。1,2,3,4,5,6.....
	 */
	public java.util.ArrayList<Integer> initArray(int sub_pages) {
		java.util.ArrayList<Integer> page_array = new java.util.ArrayList<Integer>();
		for (int i = 0; i < sub_pages; i++) {
			page_array.add(i, i);
			;
		}
		return page_array;
	}

}