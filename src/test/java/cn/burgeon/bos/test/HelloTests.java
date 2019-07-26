package cn.burgeon.bos.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cn.burgeon.bos.controller.WelcomeController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloTests {

	private static final Logger log = LoggerFactory.getLogger(HelloTests.class);
	private MockMvc mvc;

	@Before
	public void setUp() throws Exception {
		log.info("=====TEST=====");
		mvc = MockMvcBuilders.standaloneSetup(new WelcomeController()).build();
	}

	@Test
	public void getHello() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/welcome").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect((ResultMatcher) content().string(equalTo("Hello World")));
	}

}
