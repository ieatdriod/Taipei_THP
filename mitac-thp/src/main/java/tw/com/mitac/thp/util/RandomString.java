package tw.com.mitac.thp.util;

import java.util.ArrayList;
import java.util.List;

public class RandomString {
	public static void main(String[] args) {
		for (int i = 0; i < 20; i++) {
			int strLen = (int) Math.round(Math.random() * 10) + 5;
			System.out.println(getRandomString(strLen));
		}
	}

	protected static final Character[] seeds;
	static {
		List<Character> l = new ArrayList<Character>();
		for (int i = 48; i <= 57; i++)
			l.add((char) i);
		for (int i = 65; i <= 90; i++)
			l.add((char) i);
		for (int i = 97; i <= 122; i++)
			l.add((char) i);
		seeds = l.toArray(new Character[0]);
	}

	public static String getRandomString() {
		int strLen = (int) Math.round(Math.random() * 5) + 40;
		return getRandomString(strLen);
	}

	public static String getRandomString(int strLen) {
		char randStr[] = new char[strLen];
		for (int i = 0; i < randStr.length; i++) {
			randStr[i] = seeds[(int) Math.round(Math.random() * (seeds.length - 1))];
		}
		String returnStr = new String(randStr);
		return returnStr;
	}
}