package dev.jackelyj.spring_agent;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"chat.memory.type=in-memory"
})
@Tag("integration")
class SpringAgentApplicationTests {

	@Test
	void contextLoads() {
	}

}
