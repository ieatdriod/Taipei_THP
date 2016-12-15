package tw.com.mitac.thp.action;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.meeting.CrudZoomMeeting;
import tw.com.mitac.thp.meeting.meeting.ReturnDeleteData;
import tw.com.mitac.thp.util.FileUtil;
import tw.com.mitac.thp.util.MeetingApi;

public class CpsMeetingController extends DetailController<CpsMeeting> {
	private static final long serialVersionUID = 1L;
	protected List<File> doc;
	protected List<String> docFileName;
	protected static final String PATH = ResourceBundle.getBundle("FilePathSetting").getString("cpsMeeting");

	public final List<File> getDoc() {
		return doc;
	}

	public final void setDoc(List<File> doc) {
		this.doc = doc;
	}

	public final List<String> getDocFileName() {
		return docFileName;
	}

	public final void setDocFileName(List<String> docFileName) {
		this.docFileName = docFileName;
	}

	@Override
	public final LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", CpsMeetingAttendance.class));
		return detailClassMap;
	}

	@Override
	public final String delete() {
		try {
			CpsMeetingCfg cmc = getCpsMeetingCfg(bean.getVideoConferenceId());
			CrudZoomMeeting crudzm = new CrudZoomMeeting(cmc);
			Response<ReturnDeleteData> zr = crudzm.deleteMeeting(bean.getMeetingRoomId());
			logger.debug("Zoom Return=" + zr);
			if (zr.getCode() != 0) {
				addActionError(String.format("Delete meeting error:[%d]%s", zr.getCode(), zr.getMessage()));
			} else {
				if (zr.getData().getError_code() != null) {
					addActionError(String.format("Update meeting error:[%d]%s", zr.getData().getError_code(), zr
							.getData().getError_message()));
				}
			}
			return super.delete();
		} catch (Exception e) {
			addActionError(e.getMessage());
		}
		return EDIT_ERROR;
	}

	/**
	 * 創建房間
	 * */
	public final String startRoom() {

		try {
			String meetingRoomId = request.getParameter("meetingRoomId");
			String attendanceNumber = request.getParameter("attendanceNumber");
			String meetingSubject = request.getParameter("meetingSubject");
			String meetingDescription = request.getParameter("meetingDescription");
			Map<String, String> map = new HashMap<String, String>();
			map.put("userID", meetingRoomId);
			map.put("displayName", meetingSubject);
			map.put("description", meetingDescription);
			map.put("maxParticipants", attendanceNumber);
			logger.debug("startRoom創建房間" + map);
			MeetingApi mapi = new MeetingApi();
			Map<String, String> rtnMap = mapi.addMeetingRoom(map);
			logger.debug("startRoom創建房間API輸出值" + rtnMap);
			JSONObject json = new JSONObject(rtnMap);
			logger.debug("startRoom創建房間API輸出值JSON" + json);
			resultString = json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSON_RESULT;
	}

	protected CpsMeetingCfg getCpsMeetingCfg(String id) {
		List<CpsMeetingCfg> accountList = cloudDao.queryTable(sf(), CpsMeetingCfg.class, new QueryGroup(new QueryRule(
				"accountStr", id)), new QueryOrder[0], null, null);

		if (accountList != null && accountList.size() > 0) {
			return accountList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 取得當天還沒使用到的視訊帳號
	 * 
	 * @param allUserList
	 * @param cmList
	 * @return
	 */
	protected final CpsMeetingCfg getUnusedAccount(List<CpsMeetingCfg> allUserList, List<CpsMeeting> cmList) {

		CpsMeetingCfg tmpCmc = null;
		for (CpsMeetingCfg cmc : allUserList) {
			String userId = cmc.getAccountStr();
			boolean isExist = false;
			for (CpsMeeting cm : cmList) {
				if (userId.equals(cm.getVideoConferenceId())) {
					isExist = true;
					break;
				}
			}

			if (!isExist) { // 不存在表示當天該帳號尚未有時段被使用

				// 未找到會議室 or 有會議室但是目前這個會議室人數比之前的少(夠用就好)
				if (tmpCmc == null || (tmpCmc != null && (tmpCmc.getAttendance() > cmc.getAttendance()))) {
					tmpCmc = cmc;
				}
			}
		}
		return tmpCmc;
	}

	// .doc上傳檔案
	protected final String uploadData(List<File> data, List<String> dataFileName, String name) {
		if (data != null && data.size() > 0) {
			String subMainFilePath = PATH + bean.getSysid() + File.separator;
			File dirFile = new File(subMainFilePath);
			if (!dirFile.exists())
				dirFile.mkdirs();// create document

			for (int fileIndex = 0; fileIndex < dataFileName.size(); fileIndex++) {
				String finalFileName = dataFileName.get(fileIndex);
				String saveFilePath = subMainFilePath + finalFileName;
				logger.debug("簡報檔儲存路徑:" + saveFilePath);
				File fileLocation = new File(saveFilePath);
				FileUtil.moveFile(data.get(fileIndex), fileLocation);
				if (fileIndex == 0 /*
									 * && StringUtils.isBlank(bean.
									 * getVendorImageSummary())
									 */) {
					// bean.setVendorImageSummary(finalFileName);
					try {
						PropertyUtils.setProperty(bean, name, finalFileName);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return SUCCESS;
	}

	protected final long[] getTimeRange(Time time, String duration) {
		long start = time.getTime();
		Date d = DateUtils.addMinutes(time, Integer.parseInt(duration));
		long end = d.getTime();
		return new long[] { start, end };
	}

	/**
	 * 判斷時間是否重疊
	 * 
	 * @param t1
	 *            [0]start, [1]end
	 * @param t2
	 *            [0]start, [1]end
	 * @return true表示重疊
	 */
	protected final boolean isOverlaps(long[] t1, long[] t2) {
		return !((t2[1] < t1[0]) || (t1[1] < t2[0]));
	}

	/**
	 * 取得當天要求的時間裡有足夠閒置時間的視訊帳號
	 * 
	 * @param allUserList
	 * @param cmList
	 * @return
	 */
	protected final CpsMeetingCfg getIdleTimeAccount(List<CpsMeetingCfg> allUserList, List<CpsMeeting> cmList,
			Time time, String duration) {
		CpsMeetingCfg rtnCmc = null;
		// 先排除cmList不在allUserList裡的視訊帳號(表示該帳號不適用), 因為看不懂QueryRule
		// in的用法,在這裡先filter
		List<CpsMeeting> newCmList = new ArrayList<CpsMeeting>();
		for (CpsMeetingCfg cmc : allUserList) {
			for (CpsMeeting cm : cmList) {
				if (cmc.getAccountStr().equals(cm.getVideoConferenceId())) {
					newCmList.add(cm);
					break;
				}
			}
		}

		// 表示有足夠閒置時間的視訊帳號都沒有符合條件(館別, 參與人數)
		if (newCmList.size() == 0) {
			return null;
		}

		// 將可用的id放入set
		Set<String> idSet = new HashSet<String>();
		for (CpsMeeting cm : cmList) {
			idSet.add(cm.getVideoConferenceId());
		}

		Time mTime = bean.getMeetingStartTime();
		String mDuration = bean.getMeetingSession();

		long[] t1 = getTimeRange(mTime, mDuration);

		// 刪除時間重疊的id
		for (CpsMeeting cm : cmList) {
			Time cTime = cm.getMeetingStartTime();
			String cDuration = cm.getMeetingSession();
			long[] t2 = getTimeRange(cTime, cDuration);
			if (isOverlaps(t1, t2)) {
				idSet.remove(cm.getVideoConferenceId());
			}
		}

		if (idSet.size() > 0) { // 有可用的id, 取第一個來用
			String[] idArr = idSet.toArray(new String[idSet.size()]);
			for (CpsMeetingCfg cmc : allUserList) {
				if (cmc.getAccountStr().equals(idArr[0])) {
					rtnCmc = cmc;
					break;
				}

			}
		}
		return rtnCmc;
	}
}