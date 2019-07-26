package cn.burgeon.bos.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.context.Context;

import cn.burgeon.bos.util.MailUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JunitTestMail {

	@Autowired
	private MailUtil mailUtil;
	@Value("${ne.server.url}")
	private String url;
	@Value("${ne.email.from}")
	private String from;
	@Value("${ne.email.from.name}")
	private String fromName;
	@Value("${ne.email.tos}")
	private String tos;
	@Value("${ne.email.ccs}")
	private String ccs;

	@Test
	public void testSimpleMail() throws Exception {
		mailUtil.sendSimpleMail(from, "516987289@qq.com", "Simple邮件", " hello this is simple mail");
	}

	@Test
	public void testHtmlMail() throws Exception {
		String content = "<html>\n<body>\n<h3 style='color:red'>hello world ! 这是一封Html邮件!</h3>\n</body>\n</html>";
		mailUtil.sendHtmlMail(from, "516987289@qq.com", "Html邮件", content);
	}

	@Test
	public void sendAttachmentsMail() {
		String filePath = "D:/Document/Image/logo.jpg";
		mailUtil.sendAttachmentsMail(from, "516987289@qq.com", "带附件的邮件", "有附件，请查收！", filePath);
	}

	@Test
	public void sendInlineResourceMail() throws Exception {
		String rscId = "neo006";
		String content = "<html><body>这是有图片的邮件：<img src=\'cid:" + rscId + "\' ></body></html>";
		File file = ResourceUtils.getFile("classpath:image/BOS-Service.png");

		List<String> tos = new ArrayList<String>();
		tos.add("516987289@qq.com");

		Map<String, File> maps = new HashMap<String, File>();
		maps.put(rscId, file);

		mailUtil.sendInlineResourceMail(from, tos, null, "内容有图片的邮件", content, maps);
	}

	@Test
	public void sendTemplateMail() throws Exception {
		// 创建邮件正文
		File file = ResourceUtils.getFile("classpath:image/BOS-Service.png");

		Context context = new Context();
		context.setVariable("serverURL", url);

		Map<String, File> maps = new HashMap<String, File>();
		maps.put("img-warn", file);

		mailUtil.sendTemplateMail(from, fromName, tos.split(";"), ccs.trim().length() == 0 ? null : ccs.split(";"),
				"【伯俊警告】当前BOS系统处于异常状态", "burgeon-noties-warn.html", context, maps);
	}

	@Test
	public void sendTemplateMailNormal() throws Exception {
		// 创建邮件正文
		File file = ResourceUtils.getFile("classpath:image/BOS-Login.png");

		Context context = new Context();
		context.setVariable("serverURL", url);
		context.setVariable("email", "nea@burgeon.com.cn");
		context.setVariable("userName", "root");
		context.setVariable("serverTime", "2019-07-22 17:55:25");
		context.setVariable("message", "BOS运行正常");

		Map<String, File> maps = new HashMap<String, File>();
		maps.put("img-normal", file);

		mailUtil.sendTemplateMail(from, fromName, tos.split(";"), ccs.trim().length() == 0 ? null : ccs.split(";"),
				"【伯俊通知】当前BOS系统已恢复", "burgeon-noties-normal.html", context, maps);
	}

}
