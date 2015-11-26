package com;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

public class MainImage {

	static ReadImage ri = new ReadImage();

	public static void main(String[] args) {
		// File f = new File("test.jpg");

		File f = new File("result.jpg");
		File bmp = new File("temp.bmp");
		if (!bmp.exists()) {
			try {
				bmp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// bmp.deleteOnExit();
		}
		String result = "";
		try {
			BufferedImage bf = ImageIO.read(f);
			ImageIO.write(removeBack(bf), "bmp", bmp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			List<BufferedImage> list = splitImage(ImageIO.read(bmp));
			System.out.println("find out " + list.size());
			Map<BufferedImage, String> map = ri.loadTrainData();
			for (BufferedImage bufferedImage : list) {
				result += getSingleCharOcr(bufferedImage, map);
			}
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 按照空列分割图片
	 */
	public static List<BufferedImage> splitImage(BufferedImage img) throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int left = firstBlackLine(img, 0);
		int right = firstWhiteLine(img, 50) - left + 1;
		BufferedImage newImage = img.getSubimage(left, 5, right, 13);
		int imgWidth = newImage.getWidth();
		int imgHeight = newImage.getHeight();
		int pointer = 0;
		int firstWhite = 0;
		int firstBlack = 0;
		for (int i = 0; i < 5; i++) {
			firstWhite = firstWhiteLine(newImage, pointer);
			firstBlack = firstBlackLine(newImage, pointer);
			System.out.println(firstWhite + "," + firstBlack);
			if (firstBlack == imgWidth || firstWhite == imgWidth) {
				BufferedImage temp = newImage.getSubimage(pointer, 0, imgWidth - pointer, imgHeight);
				boolean a = subImgs.add(temp);
				System.out.println(a + "-------------" + pointer + " " + imgWidth);
				break;
			}
			subImgs.add(newImage.getSubimage(pointer, 0, firstWhite - pointer + 1, imgHeight));
			pointer = firstBlack;
		}
		return subImgs;
	}

	/**
	 * 将分割后的单字图片与图片库对比，找出最相似的。
	 */
	public static String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) {
		String result = " ";
		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;
		Map<BufferedImage, String> newMap = new HashMap<BufferedImage, String>();
		if (width > 15) {
			Map<BufferedImage, String> map2 = deepClone(map);
			Iterator<Entry<BufferedImage, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Iterator<Entry<BufferedImage, String>> iterator2 = map2.entrySet().iterator();
				Entry<BufferedImage, String> entry = iterator.next();
				while (iterator2.hasNext()) {
					Entry<BufferedImage, String> entry2 = iterator2.next();
					String a = entry.getValue() + entry2.getValue() + "";
					newMap.put(plusImage(entry.getKey(), entry2.getKey()), a);
				}
			}
			map = newMap;
		}
		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			if (bi.getWidth() != width) {
				continue;
			}
			Label: for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					if (ri.isWhite(img.getRGB(x, y)) != ri.isWhite(bi.getRGB(x, y))) {
						count++;
						if (count >= min)
							break Label;
					}
				}
			}
			if (count < min) {
				min = count;
				result = map.get(bi);// 找出最相似的图片
			}
		}
		if (result.equals("wf")) {
			try {
				int i = 0;
				ImageIO.write(img, "bmp", new File("unknown" + i + ".bmp"));
				i++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("结果" + result);
		return result;
	}

	/**
	 * 如果有粘连的字母，则将图片相加形成新的图片库与粘连的对比
	 */
	static BufferedImage plusImage(BufferedImage b1, BufferedImage b2) {
		try {
			// 读取第一张图片
			BufferedImage ImageOne = b1;
			int width = ImageOne.getWidth();// 图片宽度
			int height = ImageOne.getHeight();// 图片高度
			// 从图片中读取RGB
			int[] ImageArrayOne = new int[width * height];
			ImageArrayOne = ImageOne.getRGB(0, 0, width, height, ImageArrayOne, 0, width);
			// 对第二张图片做相同的处理
			BufferedImage ImageTwo = b2;
			int widthTwo = ImageTwo.getWidth();// 图片宽度
			int heightTwo = ImageTwo.getHeight();// 图片高度
			int[] ImageArrayTwo = new int[widthTwo * heightTwo];
			ImageArrayTwo = ImageTwo.getRGB(0, 0, widthTwo, heightTwo, ImageArrayTwo, 0, widthTwo);
			// 生成新图片
			BufferedImage ImageNew = new BufferedImage(width + widthTwo, height, BufferedImage.TYPE_INT_RGB);
			ImageNew.setRGB(0, 0, width, height, ImageArrayOne, 0, width);// 设置左半部分的RGB
			ImageNew.setRGB(width, 0, widthTwo, heightTwo, ImageArrayTwo, 0, widthTwo);// 设置右半部分的RGB
			return ImageNew;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 判断该列是否是空白
	 */
	static private boolean isWhiteLine(BufferedImage image, int lineNumber) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (lineNumber >= width || lineNumber < 0) {
			System.out.println("列数不符合规则");
			return true;
		}
		for (int j = 0; j < height; j++) {
			if (!isWhite(image.getRGB(lineNumber, j))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 从左至右扫描，找到第一列左黑右白的列，返回黑列的序号
	 */
	static int firstWhiteLine(BufferedImage image, int startIndex) {
		int width = image.getWidth();
		if (startIndex >= width || startIndex < 0) {
			System.out.println(startIndex + "越界");
			return width;
		}
		for (int i = startIndex; i < width; i++) {
			if ((!isWhiteLine(image, i)) && isWhiteLine(image, i + 1)) {
				int result = (i + 1) > width ? width : i;
				if (result == startIndex) {
					continue;// 如果该列是第一列，则跳过
				}
				return result;
				// return i + 1;
			}
		}
		return width;

	}

	/**
	 * 从左至右扫描，找到第一列左白右黑的列，返回黑列的序号
	 */
	static int firstBlackLine(BufferedImage image, int startIndex) {
		int width = image.getWidth();
		if (startIndex > width || startIndex < 0) {
			System.out.println(startIndex + "越界");
			return 0;
		}
		for (int i = startIndex; i < width; i++) {
			if ((isWhiteLine(image, i)) && !isWhiteLine(image, i + 1)) {
				int result = i + 1 > width ? i : (i + 1);
				if (result == startIndex + 1) {
					continue;// 如果该列是第一列，则跳过
				}
				return result;
			}
		}
		return width;
	}

	static void configImage() {

	}

	/**
	 * 去除背景噪点
	 */
	static BufferedImage removeBack(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (ri.isWhite(img.getRGB(x, y))) {
					img.setRGB(x, y, -1);
				} else {
					img.setRGB(x, y, -16777216);
				}
			}
		}
		return img;
	}

	private static boolean isWhite(int i) {
		return i > -10000000;
	}

	static void configPort() {
		// String imgPath = "D:\\workspace\\ImageConfig\\1111.bmp";
		// try {
		// String result = getAllOcr(imgPath);
		// System.out.println(result);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// String imgPath = "D:\\workspace\\ImageConfig\\7.bmp";
		// try {
		// BufferedImage image = removeBackgroud(imgPath);
		// ImageIO.write(image, "bmp", new File(imgPath));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		Map<String, String> map = FetchIps.getIps();// ip and image url
		Map<String, String> finalResult = new HashMap<>();// ip and port number
		System.out.println("map`s size = " + map.size());
		Iterator<Map.Entry<String, String>> iterater = map.entrySet().iterator();
		while (iterater.hasNext()) {
			Entry<String, String> s = iterater.next();
			try {
				finalResult.put(s.getKey(), ri.getAllOcr(s.getValue().replaceAll(" ", "%20")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("finalResult`s size = " + finalResult.size());
		Iterator<Map.Entry<String, String>> entry1 = finalResult.entrySet().iterator();
		while (entry1.hasNext()) {
			Entry<String, String> s1 = entry1.next();
			System.out.println(s1.getKey() + "      " + s1.getValue());
		}
	}

	private static Map<BufferedImage, String> deepClone(Map<BufferedImage, String> map) {
		HashMap<BufferedImage, String> result = new HashMap<BufferedImage, String>();
		Iterator<Entry<BufferedImage, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<BufferedImage, String> entry = iterator.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
