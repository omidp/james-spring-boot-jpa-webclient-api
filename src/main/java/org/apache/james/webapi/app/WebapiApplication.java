package org.apache.james.webapi.app;

import java.util.List;

import org.apache.james.adapter.mailbox.store.UserRepositoryAuthenticator;
import org.apache.james.core.Username;
import org.apache.james.mailbox.jpa.mail.JPAMailboxMapper;
import org.apache.james.mailbox.jpa.mail.JPAMessageMapper;
import org.apache.james.mailbox.jpa.openjpa.OpenJPAMailboxManager;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.search.ExactName;
import org.apache.james.mailbox.model.search.MailboxQuery;
import org.apache.james.mailbox.model.search.Wildcard;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;

@SpringBootApplication
@EnableConfigurationProperties(value = { WebApiConfiguration.class })
public class WebapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebapiApplication.class, args);
	}

	@Bean
	CommandLineRunner cmd(UserRepositoryAuthenticator ura, OpenJPAMailboxManager openJPAMailboxManager,
			JPAMessageMapper jpaMessageMapper, JPAMailboxMapper mailMapper) {
		return args -> {
			
		};
	}

}
