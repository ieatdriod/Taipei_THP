package tw.com.mitac.thp.meeting.meeting;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.mitac.thp.meeting.base.ResponseType;

public class ReturnAllMeetingData implements ResponseType {
	private Integer page_count;
	private Integer total_records;
	private Integer page_number;
	private Integer page_size;
	private List<ReturnMeetingData> meetings;
	
	public ReturnAllMeetingData(String src) throws JSONException, ParseException, UnsupportedEncodingException {
		JSONObject json = new JSONObject(src);
		this.page_count = json.getInt("page_count");
		this.total_records = json.getInt("total_records");
		this.page_number = json.getInt("page_number");
		this.page_size = json.getInt("page_size");	
		if (json.has("meetings")) {
			meetings = new ArrayList<ReturnMeetingData>();
			JSONArray arr = json.getJSONArray("meetings");
			ReturnMeetingData meeting = null;
			for(int i=0; i<arr.length(); i++) {
				meeting = new ReturnMeetingData(arr.get(i).toString());
				meetings.add(meeting);
			}				
		}		
		
	}

	public Integer getPage_count() {
		return page_count;
	}

	public void setPage_count(Integer page_count) {
		this.page_count = page_count;
	}

	public Integer getTotal_records() {
		return total_records;
	}

	public void setTotal_records(Integer total_records) {
		this.total_records = total_records;
	}

	public Integer getPage_number() {
		return page_number;
	}

	public void setPage_number(Integer page_number) {
		this.page_number = page_number;
	}

	public Integer getPage_size() {
		return page_size;
	}

	public void setPage_size(Integer page_size) {
		this.page_size = page_size;
	}

	public List<ReturnMeetingData> getMeetings() {
		return meetings;
	}

	public void setMeetings(List<ReturnMeetingData> meetings) {
		this.meetings = meetings;
	}

	@Override
	public String toString() {
		return "ReturnAllMeetingData [page_count=" + page_count + ", total_records=" + total_records + ", page_number="
				+ page_number + ", page_size=" + page_size + ", meetings=" + meetings + "]";
	}
	
	
}
