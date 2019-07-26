package cn.burgeon.bos.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.context.Context;

import com.alibaba.fastjson.JSONObject;

import cn.burgeon.bos.util.MailUtil;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Component
public class NoticeTask {

	private static final Logger log = LoggerFactory.getLogger(NoticeTask.class);
	private static boolean isLock = false;

	@Autowired
	private MailUtil mailUtil;

	@Value("${ne.server.url}")
	private String url;
	@Value("${ne.server.email}")
	private String email;
	@Value("${ne.email.from}")
	private String from;
	@Value("${ne.email.from.name}")
	private String fromName;
	@Value("${ne.email.tos}")
	private String tos;
	@Value("${ne.email.ccs}")
	private String ccs;

	@Scheduled(cron = "${ne.cron.sendemail}")
	public void sendEmailNotice() {
		if (!isReachable("www.baidu.com")) {
			log.info("当前网络不通，请连接网络才可以启用");
			return;
		}

		long begin = System.currentTimeMillis();
		String serverURL = url + "/verify.jsp?email=" + email;
		JSONObject resp = httpGet(serverURL);
		resp.remove("ph");
		long end = System.currentTimeMillis();
		long diff = end - begin;
		resp.fluentPut("responseTime", diff + "ms");

		if (!isLock) {
			if (!resp.getBooleanValue("isSuccess")) {
				// 系统崩掉——发送邮件
				sendWarnEmail(resp);
				isLock = true;
			} else {
				log.info(resp.toString());
				return;
			}
		} else {
			if (resp.getBooleanValue("isSuccess")) {
				// 系统恢复——发送邮件
				sendNormalEmail(resp);
				isLock = false;
			} else {
				log.info(resp.toString());
				return;
			}
		}
	}

	private void sendWarnEmail(JSONObject resp) {
		try {
			String[] tosTo = tos.split(";");
			String[] ccsTo = ccs.trim().length() == 0 ? null : ccs.split(";");

			Context context = new Context();
			context.setVariable("serverURL", url);
			context.setVariable("email", email);
			context.setVariable("userName", "/");
			context.setVariable("serverTime", "/");
			context.setVariable("message", resp.getString("message"));
			context.setVariable("responseTime", resp.getString("responseTime"));

			Map<String, File> maps = new HashMap<String, File>();
			File file = ResourceUtils.getFile("classpath:image/BOS-Service.png");
			maps.put("img-warn", file);

			mailUtil.sendTemplateMail(from, fromName, tosTo, ccsTo, "【伯俊警告】当前BOS系统处于异常状态", "burgeon-noties-warn.html",
					context, maps);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
	}

	private void sendNormalEmail(JSONObject resp) {
		try {
			String[] tosTo = tos.split(";");
			String[] ccsTo = ccs.trim().length() == 0 ? null : ccs.split(";");

			Context context = new Context();
			context.setVariable("serverURL", url);
			context.setVariable("email", email);
			context.setVariable("userName", resp.getString("name"));
			context.setVariable("serverTime", resp.getString("serverTime"));
			context.setVariable("message", resp.getString("message"));
			context.setVariable("responseTime", resp.getString("responseTime"));

			Map<String, File> maps = new HashMap<String, File>();
			File file = ResourceUtils.getFile("classpath:image/BOS-Login.png");
			maps.put("img-normal", file);

			mailUtil.sendTemplateMail(from, fromName, tosTo, ccsTo, "【伯俊通知】当前BOS系统已恢复", "burgeon-noties-normal.html",
					context, maps);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
	}

	private boolean isReachable(String remoteInetAddress) {
		boolean reachable = false;
		try {
			InetAddress address = InetAddress.getByName(remoteInetAddress);
			reachable = address.isReachable(5000);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return reachable;
	}

	private JSONObject httpGet(String requestUrl) {
		HttpURLConnection http = null;
		JSONObject resp = new JSONObject();
		try {
			URL url = new URL(requestUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setDoOutput(true);
			http.setDoInput(true);
			http.setUseCaches(false);
			// 设置请求方式（GET/POST）
			http.setRequestMethod("GET");
			// 设置连接超时时间：3S
			http.setConnectTimeout(3000);
			// 设置读取数据超时时间：30S
			http.setReadTimeout(30000);
			http.connect();

			StringBuffer buffer = new StringBuffer();
			// HTTP状态码大于400（包含400）是没有返回值的
			if (http.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
				// 获取服务端的返回值
				InputStream inputStream = http.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

				String str = null;
				while ((str = bufferedReader.readLine()) != null) {
					buffer.append(str);
				}
				// 释放资源
				bufferedReader.close();
				inputStreamReader.close();
				inputStream.close();
				inputStream = null;
			}
			return JSONObject.parseObject(buffer.toString());
		} catch (ConnectException e) {
			// TODO: handle exception
			log.error("http连接错误：" + e.getMessage(), e);
			resp.fluentPut("isSuccess", false);
			resp.fluentPut("message", "http连接错误：" + e.getMessage());
		} catch (UnknownHostException e) {
			// TODO: handle exception
			log.error("未知域名：" + e.getMessage(), e);
			resp.fluentPut("isSuccess", false);
			resp.fluentPut("message", "未知域名：" + e.getMessage());
		} catch (SocketTimeoutException e) {
			// TODO: handle exception
			log.error("http请求超时：" + e.getMessage(), e);
			resp.fluentPut("isSuccess", false);
			resp.fluentPut("message", "http请求超时：" + e.getMessage());
		} catch (IOException e) {
			// TODO: handle exception
			log.error("http请求失败：" + e.getMessage(), e);
			resp.fluentPut("isSuccess", false);
			resp.fluentPut("message", "http请求失败：" + e.getMessage());
		} finally {
			if (http != null)
				http.disconnect();
		}
		return resp;
	}

}
