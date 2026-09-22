package com.add2numweb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class Add2NumWebApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		assertDoesNotThrow(() -> Add2NumWebApplication.main(new String[]{"--server.port=0"}));
	}

}
