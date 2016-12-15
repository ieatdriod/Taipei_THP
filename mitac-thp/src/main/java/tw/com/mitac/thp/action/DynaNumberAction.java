package tw.com.mitac.thp.action;

import java.util.Map;

import tw.com.mitac.thp.util.ImageUtils;

public class DynaNumberAction extends BasisAction {
	private java.io.InputStream imageStream;

	public java.io.InputStream getImageStream() {
		return imageStream;
	}

	public void setImageStream(java.io.InputStream imageStream) {
		this.imageStream = imageStream;
	}

	public String execute() throws Exception {
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();

		try {
			Map<String, Object> map = ImageUtils.doDynaNumber();
			String number = (String) map.get("NUMBER");
			java.awt.image.BufferedImage dstImage = (java.awt.image.BufferedImage) map.get("IMAGE");
			session.put(DYNA_NUMBER, number);

			javax.imageio.ImageIO.write(dstImage, "JPEG", bos);
			imageStream = new java.io.ByteArrayInputStream(bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return SUCCESS;
	}
}