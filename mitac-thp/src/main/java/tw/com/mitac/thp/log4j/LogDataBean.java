package tw.com.mitac.thp.log4j;

public class LogDataBean {
	protected String logLevel;
	protected String msgContent;
	protected String classMsg;
	protected String methodMsg;
	protected String logTime;

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getClassMsg() {
		return classMsg;
	}

	public void setClassMsg(String classMsg) {
		this.classMsg = classMsg;
	}

	public String getMethodMsg() {
		return methodMsg;
	}

	public void setMethodMsg(String methodMsg) {
		this.methodMsg = methodMsg;
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}
}