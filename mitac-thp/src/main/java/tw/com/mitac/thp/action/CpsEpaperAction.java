package tw.com.mitac.thp.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import tw.com.mitac.thp.bean.CpsEpaper;
import tw.com.mitac.thp.bean.CpsEpaperDetail;

public class CpsEpaperAction extends DetailController<CpsEpaper> {

	private File fileExl;

	public File getFileExl() {
		return fileExl;
	}

	public void setFileExl(File fileExl) {
		this.fileExl = fileExl;
	}

	/** 框架尾檔 */
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", CpsEpaperDetail.class));
		return detailClassMap;
	}

	/** 框架儲存功能處理 */
	@Override
	protected boolean executeSave() {

		sObjects();

		boolean result = super.executeSave();

		return result;
	}

	/** 處理發送對象-E特定對象(+上處理對象來源),A全站會員 */
	protected String sObjects() {
		String sObjects = request.getParameter("sObjects");
		logger.debug("發送對象來源：" + sObjects);
		if (sObjects.equals("E")) {
			logger.debug("USER選擇特定對象寄信");
			String[] ckbv = request.getParameterValues("chkv");
			String enValue = "";
			logger.debug("比數:" + ckbv.length);
			for (int i = 0; i < ckbv.length; i++) {
				if (i == 0) {
					enValue += ckbv[i];
				} else {
					enValue += "," + ckbv[i];
				}
				// 處理EXL 需要勾選自訂執行
				if (ckbv[i].equals("D") && fileExl != null) {
					exlRead(fileExl);
				}
			}

			logger.debug("checkbox有勾選的欄位:" + enValue);
			bean.setExceptionObjects(enValue);
			bean.setSelectObjects(sObjects);
			bean.setPublishDateActual(null);
		} else {
			logger.debug("USER選擇全站會員寄信");
			bean.setExceptionObjects("");
			bean.setSelectObjects(sObjects);
			bean.setPublishDateActual(null);
		}

		return SUCCESS;
	}

	/** 讀取exl文件 */
	protected String exlRead(File file) {

		XSSFWorkbook readWorkbook = null;
		try {
			// readWorkbook = new XSSFWorkbook(new
			// FileInputStream("C:\\test.xlsx"));
			readWorkbook = new XSSFWorkbook(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (readWorkbook != null) {
			String wbv = "";
			// 取得Sheet 可指定sheet的名稱, 參數為sheet名稱
			XSSFSheet readSheet = readWorkbook.getSheetAt(0);
			int rNo = readSheet.getPhysicalNumberOfRows(); // 取得列總數
			logger.debug("載入 XLSX 資訊-Start");
			for (int i = 0; i < rNo; i++) {
				XSSFRow r = readSheet.getRow(i);// 先取出列

				// int cNO = r.getPhysicalNumberOfCells();
				// for (int j = 0; j < cNO; j++) {
				// // System.out.print(r.getCell(j) + ",");// 再取出欄
				// if (j == 0) {
				// wbv += r.getCell(j);
				// } else {
				// wbv += "," + r.getCell(j);
				// }
				// } // System.out.println();//換行
				// logger.debug("載入檔案XLSX資訊 - 第" + i + "行 - 資訊：" + wbv);
				// wbv = "";
				
				if (i == 0) {
					wbv += r.getCell(0);
				} else {
					wbv += "," + r.getCell(0);
				}
			}
			// 匯入
			emailImport(wbv);
			logger.debug("載入 XLSX 資訊-End");
		}

		return SUCCESS;
	}

	/** 匯入尾檔儲存 */
	protected String emailImport(String email) {

		Set<CpsEpaperDetail> dataSet = (Set<CpsEpaperDetail>) findDetailSetWhenEdit(DETAIL_SET);

		if (StringUtils.isNotBlank(email)) {

			String[] emailAry = email.split(",");

			for (int i = 1; i < emailAry.length; i++) {
				CpsEpaperDetail item = getDefaultDMO(CpsEpaperDetail.class);
				item.setEmail(emailAry[i]);
				defaultValue(item);
				tw.com.mitac.thp.util.Util.defaultPK(item);
				dataSet.add(item);
				logger.debug("信箱匯入第" + i + "筆" + emailAry[i] + "-資訊:" + item);
			}

		}

		return SUCCESS;
	}

	// @Override
	// protected QueryGroup getQueryRestrict() {
	// List<String> entitySysidList = getEntitySysidList();
	// if (entitySysidList.size() == 0)
	// return new QueryGroup(new QueryRule("entitySysid", "x"));
	// else
	// return new QueryGroup(new QueryRule("entitySysid", IN, entitySysidList));
	// }
	//
	// public List<String> getEntitySysidList() {
	// // List<String> entitySysidList = new ArrayList<String>();
	// // for (CpsEntity cpsEntity : getDataCpsEntityETable().values()) {
	// // Boolean entitySysid_ = (Boolean) request.getAttribute("entitySysid_"
	// // + cpsEntity.getDataId());
	// // if (entitySysid_ == null)
	// // entitySysid_ = false;
	// // if (entitySysid_)
	// // entitySysidList.add(cpsEntity.getSysid());
	// // }
	// // return entitySysidList;
	// logger.debug("getUserAccount().getSourceType():" +
	// getUserAccount().getSourceType());
	// if
	// (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
	// {
	// if (getCpsEntitySysid().equals(getUserAccount().getSourceSysid())) {
	// List<String> l = new ArrayList<String>();
	// for (CpsEntity cpsEntity : getDataCpsEntityETable().values()) {
	// l.add(cpsEntity.getSysid());
	// }
	// return l;
	// } else {
	// List<String> l = new ArrayList<String>();
	// l.add(getUserAccount().getSourceSysid());
	// return l;
	// }
	// } else {
	// return new ArrayList<String>();
	// }
	// }
	//
	// @Override
	// public String edit() {
	// String result = super.edit();
	// if (StringUtils.isBlank(bean.getEntitySysid())
	// &&
	// CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType()))
	// {
	// String entitySysid = getUserAccount().getSourceSysid();
	// bean.setEntitySysid(entitySysid);
	// beaninfo.put("entitySysid" + "Show",
	// createDataDisplay(CpsEntity.class).get(entitySysid));
	// }
	// return result;
	// }
}