package com.ql.webrtc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class QMediaApplication {

	public static void main(String[] args) throws UnknownHostException {
		ConfigurableApplicationContext run = SpringApplication.run(QMediaApplication.class, args);
		Environment env = run.getEnvironment();
		String ip = InetAddress.getLocalHost().getHostAddress();
		String port = env.getProperty("server.port");
		String path = env.getProperty("server.servlet.context-path");
		log.info("\n----------------------------------------------------------\n\t" +
				"Application QMediaApplication is running! Access URL:\n\t" +
				"Local: \t\thttp://localhost:" + port + path + "\n\t" +
				"NetWork: \t"+ip+":" + port + path + "\n\t" +
				"Author: \tqinlei\n\t"+
				"email: \t\tqlanto_147@163.com\n\t"+
				"QQ : \t\t429679876\n\t"+
				"----------------------------------------------------------");
	}
}
