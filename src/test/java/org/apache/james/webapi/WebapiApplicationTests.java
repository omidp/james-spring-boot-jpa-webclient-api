package org.apache.james.webapi;

import org.apache.james.mailbox.jpa.mail.JPAMailboxMapper;
import org.apache.james.mailbox.jpa.mail.JPAMessageMapper;
import org.apache.james.mailbox.jpa.openjpa.OpenJPAMailboxManager;
import org.apache.james.webapi.app.DBConfiguration;
import org.apache.james.webapi.app.MailboxConfiguration;
import org.apache.james.webapi.app.RabbitConfiguration;
import org.apache.james.webapi.app.WebApiConfiguration;
import org.apache.james.webapi.app.service.MailboxService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@Slf4j
@Disabled
class WebapiApplicationTests {

	@Autowired
	MailboxService mbService;

	@Test
	@Disabled
	void contextLoads() {
		Assert.notNull(mbService);
	}

	@TestConfiguration
	@Import({ MailboxConfiguration.class, DBConfiguration.class, RabbitConfiguration.class })
//	@ComponentScan(basePackageClasses = MailboxService.class)
	@EnableConfigurationProperties(value = { WebApiConfiguration.class })
	public static class ApiTestConfig {

		@Bean
		public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
			PropertySourcesPlaceholderConfigurer ps = new PropertySourcesPlaceholderConfigurer();
			ps.setLocations(new ClassPathResource("app.properties"));
			ps.setIgnoreResourceNotFound(true);
			ps.setIgnoreUnresolvablePlaceholders(true);
			return ps;
		}

		@Bean
		MailboxService mailboxService(JPAMailboxMapper jpaMailboxMapper, OpenJPAMailboxManager openJPAMailboxManager,
				JPAMessageMapper jpaMessageMapper) {
			return new MailboxService(jpaMailboxMapper, openJPAMailboxManager, jpaMessageMapper);
		}

	}

}
