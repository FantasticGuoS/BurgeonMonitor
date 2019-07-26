package cn.burgeon.bos.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

	@RequestMapping("/welcome")
	public String index() {
		return "这是伯俊自动通知插件，当前运行状态正常";
	}

}
