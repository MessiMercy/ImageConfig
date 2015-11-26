package com;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class ReadImage {

	public boolean isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 400) {
			return true;
		}
		return false;
		// return colorInt > -1644827 ? 1 : 0;
	}

	public BufferedImage removeBackgroud(BufferedImage img) throws Exception {
		int width = img.getWidth();
		int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (isWhite(img.getRGB(x, y))) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					img.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return img;
	}

	public List<BufferedImage> splitImage(BufferedImage img) throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int imgWidth = img.getWidth();
		int imgNums = imgWidth / 8;
		// subImgs.add(img.getSubimage(0, 0, 8, 10));
		// subImgs.add(img.getSubimage(8, 0, 8, 10));
		// subImgs.add(img.getSubimage(16, 0, 8, 10));
		// subImgs.add(img.getSubimage(24, 0, 8, 10));
		for (int i = 0; i < imgNums; i++) {
			subImgs.add(img.getSubimage(i * 8, 0, 8, 10));
		}
		return subImgs;
	}

	public Map<BufferedImage, String> loadTrainData() throws Exception {
		Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
		File dir = new File("imageLib");
		File[] files = dir.listFiles();
		for (File file : files) {
			map.put(ImageIO.read(file), file.getName().charAt(0) + "");
		}
		return map;
	}

	public String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) {
		String result = "";
		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;
		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			Label: for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {
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
		return result;
	}

	public String getAllOcr(String url) throws Exception {
		BufferedImage img = removeBackgroud(FetchIps.getImage(url));
		List<BufferedImage> listImg = splitImage(img);
		Map<BufferedImage, String> map = loadTrainData();
		String result = "";
		for (BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		return result;
	}

}
