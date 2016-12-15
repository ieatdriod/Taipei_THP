package tw.com.mitac.thp.meeting.meeting;

import java.util.Map;

import tw.com.mitac.thp.meeting.ZoomMeetingApi;

public class ParamMeetingData extends BaseMeetingData {


	/**
	 * 覆寫getParams, 將start_time轉換為ISO格式
	 */
	@Override
	public Map<String, String >getParams() throws Exception {
		Map<String, String> params = super.getParams();
		if (this.getStart_time() != null) {
			params.put("start_time", ZoomMeetingApi.getISODateTime(this.getStart_time(), "Asia/Taipei"));
		}
		return params;
	}
	
	@Override
	public String toString() {
		return "ParamMeetingData [getTopic()=" + getTopic() + ", getType()=" + getType() + ", getStart_time()="
				+ getStart_time() + ", getDuration()=" + getDuration() + ", getTimezone()=" + getTimezone()
				+ ", getPassword()=" + getPassword() + ", getOption_jbh()=" + getOption_jbh()
				+ ", getOption_start_type()=" + getOption_start_type() + ", getOption_host_video()="
				+ getOption_host_video() + ", getOption_participants_video()=" + getOption_participants_video()
				+ ", getOption_audio()=" + getOption_audio() + ", toString()=" + super.toString() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + "]";
	}
	
	public static void main(String[] args) throws Exception {
//		ParamMeetingData pmd = new ParamMeetingData();
//		pmd.setTopic("adfdfsdfsdffff");
//		pmd.setType(3);
//		Map<String, String> map = pmd.getParams();
//		System.out.println(map);
		
	}
	
}
