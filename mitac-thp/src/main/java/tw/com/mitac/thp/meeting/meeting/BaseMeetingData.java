package tw.com.mitac.thp.meeting.meeting;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BaseMeetingData {
	private String topic;
	private Integer type;
	private Date start_time; //請永遠設定為Asia/Taipei時間
	private Integer duration;
	private String timezone;
	private String password;
	private Boolean option_jbh;
	private String option_start_type;
	private Boolean option_host_video;
	private Boolean option_participants_video;
	private String option_audio;
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Boolean getOption_jbh() {
		return option_jbh;
	}
	public void setOption_jbh(Boolean option_jbh) {
		this.option_jbh = option_jbh;
	}
	public String getOption_start_type() {
		return option_start_type;
	}
	public void setOption_start_type(String option_start_type) {
		this.option_start_type = option_start_type;
	}
	public Boolean getOption_host_video() {
		return option_host_video;
	}
	public void setOption_host_video(Boolean option_host_video) {
		this.option_host_video = option_host_video;
	}
	public Boolean getOption_participants_video() {
		return option_participants_video;
	}
	public void setOption_participants_video(Boolean option_participants_video) {
		this.option_participants_video = option_participants_video;
	}
	public String getOption_audio() {
		return option_audio;
	}
	public void setOption_audio(String option_audio) {
		this.option_audio = option_audio;
	}
	
	protected Map<String, String> getParams() throws Exception {
		Map<String, String> paramMap = new HashMap<String, String>();
		
		Field[] fields = BaseMeetingData.class.getDeclaredFields();
		for(Field field : fields) {
			if(field.get(this) != null) {
				paramMap.put(field.getName(), String.valueOf(field.get(this)));
			}
		}
		
		return paramMap;
	}	
	
	@Override
	public String toString() {
		return "BaseMeetingData [topic=" + topic + ", type=" + type + ", start_time=" + start_time + ", duration="
				+ duration + ", timezone=" + timezone + ", password=" + password + ", option_jbh=" + option_jbh
				+ ", option_start_type=" + option_start_type + ", option_host_video=" + option_host_video
				+ ", option_participants_video=" + option_participants_video + ", option_audio=" + option_audio + "]";
	}
	
	public static void main(String[] args) throws Exception {
		BaseMeetingData bmd = new BaseMeetingData();
		bmd.setTopic("sddsdfsdff");
		System.out.println(bmd.getParams());
		
	}
}
