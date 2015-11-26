package com;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FetchIps {
	static BasicCookieStore cookieStore = new BasicCookieStore();
	static CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	static int i = 0;

	public static Map<String, String> getIps() {
		Map<String, String> list = new HashMap<>();
		HttpGet get = new HttpGet("http://ip.zdaye.com/");
		try {
			HttpResponse response = client.execute(get);
			String html = EntityUtils.toString(response.getEntity());
			Document doc = Jsoup.parse(html);
			Elements elements = doc.select("td[v]");
			for (Element element : elements) {
				if (!element.hasAttr("v2")) {
					String imageUrl = "http://ip.zdaye.com"
							+ element.nextElementSibling().nextElementSibling().children().first().attr("src");
					list.put(element.text(), imageUrl);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static BufferedImage getImage(String url) {
		HttpGet get1 = new HttpGet(url);
		get1.setHeader("Referer", "http://ip.zdaye.com");
		get1.setHeader("Accept", "image/webp,image/*,*/*;q=0.8");
		get1.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
		BufferedImage image;
		try {
			HttpResponse response = client.execute(get1);
			InputStream in = response.getEntity().getContent();
			// File ff = new File(i + "_a.bmp");
			// i++;
			// if (!ff.exists()) {
			// System.out.println(ff.createNewFile());
			// }
			// FileOutputStream fo = new FileOutputStream(ff);
			// byte[] tmpBuf = new byte[1024];
			// int bufLen = 0;
			// // long downloadedSize = 0;
			// while ((bufLen = in.read(tmpBuf)) > 0) {
			// fo.write(tmpBuf, 0, bufLen);
			// // downloadedSize += bufLen;
			// }
			// fo.close();
			image = ImageIO.read(in);
			in.close();
			return image;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get1.releaseConnection();
		}
		return null;
	}

	public static void main(String[] args) {
		getIps();
		HttpGet get = new HttpGet("http://ip.zdaye.com/");
		get.setHeader("contentType", "application/x-www-form-urlencoded; charset=UTF-8");
		get.setHeader("Accept", "text/plain,text/html,application/xml, text/xml,application/json, text/javascript");
		get.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
		try {
			HttpResponse response = client.execute(get);
			String str = EntityUtils.toString(response.getEntity(), "gb2312");
			System.out.println(str);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
