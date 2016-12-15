package tw.com.mitac.thp.meeting.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;

import org.apache.commons.beanutils.ConstructorUtils;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Response<E> {
	private Integer code;
	private String message;
	private E data;
	public final static String[] datePattern = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
	
	public Response(String src) throws JSONException, ParseException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		JSONObject json = new JSONObject(src);
		this.code = json.getInt("code");
		this.message = json.getString("message");
		if (json.has("data")) {
			Class<E> t = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	    	data = (E)ConstructorUtils.invokeConstructor(t, json.get("data").toString());
		}
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public E getData() {
		return data;
	}

	public void setData(E data) {
		this.data = data;
	}


	@Override
	public String toString() {
		return "Response [code=" + code + ", message=" + message + ", data=" + data + "]";
	}
	
	

}
