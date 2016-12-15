package tw.com.mitac.thp.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageUtils {

	private static Rectangle clip;
	private final static int reSizePer = 10000;

	// http://sanjaal.com/java/395/java-graphics/cropping-an-image-in-java-sampletutorial-with-source-code/
	// public static void main(String[] args) {
	//
	// String inputFilelocation = "C:/Users/Robin/Desktop/0011154-original.jpg";
	// String outputFilelocation =
	// "C:/Users/Robin/Desktop/0011154-original-cropped.jpg";
	//
	// // 裁減參數
	// int cropHeight = 150;
	// int cropWidth = 150;
	// int cropStartX = 300;
	// int cropStartY = 300;
	//
	// ImageUtils.doCrop(inputFilelocation, outputFilelocation , cropWidth,
	// cropHeight, cropStartX, cropStartY);
	// }
	//
	// public static void main(String[] args) throws Exception {
	//
	// int re_width = 100;
	// int re_height = 100;
	//
	// String inputFilelocation = "C:/Users/Robin/Desktop/0011154-original.jpg";
	// String outputFilelocation =
	// "C:/Users/Robin/Desktop/0011154-original-resize.jpg";
	//
	// ImageUtils.doResize(inputFilelocation, outputFilelocation, re_width,
	// re_height);
	// }

	public static void main(String[] args) {

		// String file1 = "C:/Users/Robin/Desktop/0011154-original.jpg";
		// String file2 = "C:/Users/Robin/Desktop/fuck.jpg";
		// String file3 = "C:/Users/Robin/Desktop/result.jpg";
		//
		// ImageUtils.doCombine(new File(file1), new File(file2), 99, 97, new
		// File(file3));

		doDynaNumber("C:/Users/Robin/Desktop/result.jpg", "jpg");
	}

	public static String doDynaNumber(String location, String extName) {
		Map map = dynamicNumber();
		writeImage((BufferedImage) map.get("IMAGE"), location, extName);
		return (String) map.get("NUMBER");
	}

	public static Map<String, Object> doDynaNumber() {
		return dynamicNumber();
	}

	private static Map<String, Object> dynamicNumber() {
		BufferedImage bufferedImage = new BufferedImage(50, 30, BufferedImage.TYPE_INT_RGB);
		Graphics bufferedGraphics = bufferedImage.getGraphics();
		bufferedGraphics.setColor(Color.WHITE);
		bufferedGraphics.fillRect(0, 0, 50, 30);
		bufferedGraphics.setColor(Color.BLACK);
		bufferedGraphics.drawRect(5, 5, 40, 20);

		String number = Integer.toString((int) (Math.random() * 10000));
		while (number.length() < 4) {
			number = "0" + number;
		}

		bufferedGraphics.setFont(new Font("Times New Roman", Font.BOLD, 13));
		bufferedGraphics.drawString(number, 10, 20);

		for (int i = 0; i < 20; i++) {
			int x = (int) (Math.random() * 40) + 5;
			int y = (int) (Math.random() * 20) + 5;

			bufferedGraphics.fillOval(x, y, 1, 1);
		}

		bufferedGraphics.dispose();

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("NUMBER", number);
		result.put("IMAGE", bufferedImage);

		return result;
	}

	public static void doCombine(File back, File front, int x, int y, File combine) {

		BufferedImage image_bg = readImage(back);
		BufferedImage image_fe = readImage(front);

		BufferedImage canvas = new BufferedImage(image_bg.getWidth(), image_bg.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics graphic = canvas.getGraphics();

		try {
			graphic.drawImage(image_bg, 0, 0, null);
			// graphic.drawImage(image_fe, 99, 97, null);
			graphic.drawImage(image_fe, x, y, null);
		} finally {
			graphic.dispose();
		}

		String target = combine.getName();
		String postfix = target.substring(target.lastIndexOf(".") + 1, target.length()).toUpperCase();
		writeImage(canvas, combine, postfix);
	}

	// http://goo.gl/moza6
	public static String doResize(File source, String target, int width, int height) throws Exception {

		BufferedImage image = ImageIO.read(source);
		int org_width = image.getWidth();
		int org_height = image.getHeight();

		int re_width = org_width;
		int re_height = org_height;

		if (org_width > width || org_height > height) {

			int tmp = 0;

			// 要縮小幾% 才會到350pix以下
			int w = (width * reSizePer) / org_width;
			int h = (height * reSizePer) / org_height;

			if (w > reSizePer && h > reSizePer)
				tmp = reSizePer;
			else if (w > reSizePer && h < reSizePer)
				tmp = h;
			else if (h > reSizePer && w < reSizePer)
				tmp = w;
			else
				tmp = w < h ? w : h;

			if (tmp == 0)
				tmp = reSizePer;

			re_width = Math.round(org_width * tmp / reSizePer);
			re_height = Math.round(org_height * tmp / reSizePer);
		}

		resize(image, target, re_width, re_height, target.substring(target.lastIndexOf(".") + 1, target.length())
				.toUpperCase());
		return target;
	}

	public static String doResize(String source, String target, int width, int height) throws Exception {

		BufferedImage image = ImageIO.read(new File(source));
		int org_width = image.getWidth();
		int org_height = image.getHeight();

		int re_width = org_width;
		int re_height = org_height;

		if (org_width > width || org_height > height) {

			int tmp = 0;

			// 要縮小幾% 才會到350pix以下
			int w = (width * reSizePer) / org_width;
			int h = (height * reSizePer) / org_height;

			if (w > reSizePer && h > reSizePer)
				tmp = reSizePer;
			else if (w > reSizePer && h < reSizePer)
				tmp = h;
			else if (h > reSizePer && w < reSizePer)
				tmp = w;
			else
				tmp = w < h ? w : h;

			if (tmp == 0)
				tmp = reSizePer;

			re_width = Math.round(org_width * tmp / reSizePer);
			re_height = Math.round(org_height * tmp / reSizePer);
		}

		resize(image, target, re_width, re_height, target.substring(target.lastIndexOf(".") + 1, target.length())
				.toUpperCase());
		return target;
	}

	public static BufferedImage doResize(File source, int width, int height) throws Exception {

		BufferedImage image = ImageIO.read(source);
		// int org_width = image.getWidth();
		// int org_height = image.getHeight();
		//
		// int re_width = org_width;
		// int re_height = org_height;
		//
		// if(org_width > width || org_height > height) {
		//
		// int tmp = 0;
		//
		// // 要縮小幾% 才會到350pix以下
		// int w = (width*reSizePer) / org_width;
		// int h = (height*reSizePer) / org_height;
		//
		// if(w>reSizePer && h>reSizePer) tmp=reSizePer;
		// else if(w>reSizePer && h<reSizePer) tmp=h;
		// else if(h>reSizePer && w<reSizePer) tmp=w;
		// else tmp = w<h? w:h;
		//
		// if(tmp==0) tmp=reSizePer;
		//
		// re_width = Math.round(org_width * tmp/reSizePer);
		// re_height = Math.round(org_height * tmp/reSizePer);
		// }
		//
		// return resize(image, re_width, re_height);
		return resize(image, width, height);
	}

	private static void resize(BufferedImage source, String toLocation, int new_w, int new_h, String extName)
			throws Exception {

		BufferedImage target = new BufferedImage(new_w, new_h, BufferedImage.TYPE_INT_RGB);
		target.getGraphics().drawImage(source, 0, 0, new_w, new_h, null); // 繪製縮小的圖
		writeImage(target, toLocation, extName);
	}

	private static BufferedImage resize(BufferedImage source, int new_w, int new_h) throws Exception {

		BufferedImage target = new BufferedImage(new_w, new_h, BufferedImage.TYPE_INT_RGB);
		target.getGraphics().drawImage(source, 0, 0, new_w, new_h, null); // 繪製縮小的圖
		return target;
	}

	public static void doCrop(File inputFile, String outputFile, int cropWidth, int cropHeight, int cropStartX,
			int cropStartY) {

		BufferedImage originalImage = readImage(inputFile);
		BufferedImage processedImage = cropImage(originalImage, cropWidth, cropHeight, cropStartX, cropStartY);
		writeImage(processedImage, outputFile, "jpg");
		System.out.println("...Done");
	}

	public static void doCrop(String inputFilelocation, String outputFilelocation, int cropWidth, int cropHeight,
			int cropStartX, int cropStartY) {

		System.out.println("input file location: " + inputFilelocation);
		BufferedImage originalImage = readImage(inputFilelocation);

		BufferedImage processedImage = cropImage(originalImage, cropWidth, cropHeight, cropStartX, cropStartY);
		System.out.println("output file location: " + outputFilelocation);

		writeImage(processedImage, outputFilelocation, "jpg");
		System.out.println("...Done");
	}

	public static BufferedImage readImage(String location) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(location));
			System.out.println("image height: " + img.getHeight() + ", width " + img.getWidth());

		} catch (Exception e) {
			e.printStackTrace();

		}
		return img;
	}

	public static BufferedImage readImage(File file) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
			System.out.println("image height: " + img.getHeight() + ", width " + img.getWidth());
		} catch (Exception e) {
			e.printStackTrace();

		}
		return img;
	}

	public static BufferedImage cropImage(BufferedImage img, int cropWidth, int cropHeight, int cropStartX,
			int cropStartY) {

		BufferedImage clipped = null;
		Dimension size = new Dimension(cropWidth, cropHeight);

		createClip(img, size, cropStartX, cropStartY);

		try {

			int w = clip.width;
			int h = clip.height;

			System.out.println("Crop Width " + w);
			System.out.println("Crop Height " + h);
			System.out.println("Crop Location " + "(" + clip.x + "," + clip.y + ")");

			clipped = img.getSubimage(clip.x, clip.y, w, h);

			System.out.println("Image Cropped. New Image Dimension: " + clipped.getWidth() + "w X "
					+ clipped.getHeight() + "h");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return clipped;
	}

	private static void createClip(BufferedImage img, Dimension size, int clipX, int clipY) {

		boolean isClipAreaAdjusted = false;

		if (clipX < 0) {
			clipX = 0;
			isClipAreaAdjusted = true;
		}

		if (clipY < 0) {
			clipY = 0;
			isClipAreaAdjusted = true;
		}

		int maginX = size.width + clipX;
		int maginY = size.height + clipY;

		if (maginX <= img.getWidth() && maginY <= img.getHeight()) {

			clip = new Rectangle(size);
			clip.x = clipX;
			clip.y = clipY;
		} else {

			if (maginX > img.getWidth())
				size.width = img.getWidth() - clipX;
			if (maginY > img.getHeight())
				size.height = img.getHeight() - clipY;

			clip = new Rectangle(size);
			clip.x = clipX;
			clip.y = clipY;

			isClipAreaAdjusted = true;
		}

		if (isClipAreaAdjusted) {
			System.out.println("Crop Area Lied Outside The Image.  Adjusted The Clip Rectangle");
		}
	}

	public static void writeImage(BufferedImage img, String fileLocation, String extension) {
		try {
			BufferedImage bi = img;
			ImageIO.write(bi, extension, new File(fileLocation));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeImage(BufferedImage img, File file, String extension) {
		try {
			BufferedImage bi = img;
			ImageIO.write(bi, extension, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
