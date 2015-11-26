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
	 * ���տ��зָ�ͼƬ
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
	 * ���ָ��ĵ���ͼƬ��ͼƬ��Աȣ��ҳ������Ƶġ�
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
				result = map.get(bi);// �ҳ������Ƶ�ͼƬ
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
		System.out.println("���" + result);
		return result;
	}

	/**
	 * �����ճ������ĸ����ͼƬ����γ��µ�ͼƬ����ճ���ĶԱ�
	 */
	static BufferedImage plusImage(BufferedImage b1, BufferedImage b2) {
		try {
			// ��ȡ��һ��ͼƬ
			BufferedImage ImageOne = b1;
			int width = ImageOne.getWidth();// ͼƬ���
			int height = ImageOne.getHeight();// ͼƬ�߶�
			// ��ͼƬ�ж�ȡRGB
			int[] ImageArrayOne = new int[width * height];
			ImageArrayOne = ImageOne.getRGB(0, 0, width, height, ImageArrayOne, 0, width);
			// �Եڶ���ͼƬ����ͬ�Ĵ���
			BufferedImage ImageTwo = b2;
			int widthTwo = ImageTwo.getWidth();// ͼƬ���
			int heightTwo = ImageTwo.getHeight();// ͼƬ�߶�
			int[] ImageArrayTwo = new int[widthTwo * heightTwo];
			ImageArrayTwo = ImageTwo.getRGB(0, 0, widthTwo, heightTwo, ImageArrayTwo, 0, widthTwo);
			// ������ͼƬ
			BufferedImage ImageNew = new BufferedImage(width + widthTwo, height, BufferedImage.TYPE_INT_RGB);
			ImageNew.setRGB(0, 0, width, height, ImageArrayOne, 0, width);// ������벿�ֵ�RGB
			ImageNew.setRGB(width, 0, widthTwo, heightTwo, ImageArrayTwo, 0, widthTwo);// �����Ұ벿�ֵ�RGB
			return ImageNew;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * �жϸ����Ƿ��ǿհ�
	 */
	static private boolean isWhiteLine(BufferedImage image, int lineNumber) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (lineNumber >= width || lineNumber < 0) {
			System.out.println("���������Ϲ���");
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
	 * ��������ɨ�裬�ҵ���һ������Ұ׵��У����غ��е����
	 */
	static int firstWhiteLine(BufferedImage image, int startIndex) {
		int width = image.getWidth();
		if (startIndex >= width || startIndex < 0) {
			System.out.println(startIndex + "Խ��");
			return width;
		}
		for (int i = startIndex; i < width; i++) {
			if ((!isWhiteLine(image, i)) && isWhiteLine(image, i + 1)) {
				int result = (i + 1) > width ? width : i;
				if (result == startIndex) {
					continue;// ��������ǵ�һ�У�������
				}
				return result;
				// return i + 1;
			}
		}
		return width;

	}

	/**
	 * ��������ɨ�裬�ҵ���һ������Һڵ��У����غ��е����
	 */
	static int firstBlackLine(BufferedImage image, int startIndex) {
		int width = image.getWidth();
		if (startIndex > width || startIndex < 0) {
			System.out.println(startIndex + "Խ��");
			return 0;
		}
		for (int i = startIndex; i < width; i++) {
			if ((isWhiteLine(image, i)) && !isWhiteLine(image, i + 1)) {
				int result = i + 1 > width ? i : (i + 1);
				if (result == startIndex + 1) {
					continue;// ��������ǵ�һ�У�������
				}
				return result;
			}
		}
		return width;
	}

	static void configImage() {

	}

	/**
	 * ȥ���������
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
