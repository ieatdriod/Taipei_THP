package tw.com.mitac.thp.action;

import java.util.List;

public class QueryResults {
	/** 一頁資料比數 */
	protected Integer rows = 1;
	/** 目前頁數 */
	protected Integer page = 1;
	/** 最大頁碼 */
	protected Integer total = 1;
	/** 資料比數 */
	protected Integer record = 1;
	/** 一頁資料比數 */
	protected List<?> gridModel;

	public final Integer getRows() {
		return rows;
	}

	public final void setRows(Integer rows) {
		this.rows = rows;
	}

	public final Integer getPage() {
		return page;
	}

	public final void setPage(Integer page) {
		this.page = page;
	}

	public final Integer getTotal() {
		return total;
	}

	public final void setTotal(Integer total) {
		this.total = total;
	}

	public final Integer getRecord() {
		return record;
	}

	public final void setRecord(Integer record) {
		this.record = record;
	}

	public final List<?> getGridModel() {
		return gridModel;
	}

	public final void setGridModel(List<?> gridModel) {
		this.gridModel = gridModel;
	}
}