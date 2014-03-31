package org.joolzminer.fpc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource("classpath:META-INF/maven/org.joolzminer/folder-playlist-creator-swing-ui/pom.properties")
public class ApplicationConfig {

}
