package org.joolzminer.fpc;

import org.joolzminer.fpc.ApplicationConfig;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EnvironmentTest {

	@Test
	public void testBootstrapFromJavaConfig() {
		ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
		assertThat(context, is(notNullValue()));
	}
}
