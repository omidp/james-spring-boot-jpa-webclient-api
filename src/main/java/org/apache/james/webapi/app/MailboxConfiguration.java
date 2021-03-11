package org.apache.james.webapi.app;

import javax.persistence.EntityManagerFactory;

import org.apache.james.adapter.mailbox.store.UserRepositoryAuthenticator;
import org.apache.james.adapter.mailbox.store.UserRepositoryAuthorizator;
import org.apache.james.dnsservice.dnsjava.DNSJavaService;
import org.apache.james.domainlist.jpa.JPADomainList;
import org.apache.james.mailbox.acl.SimpleGroupMembershipResolver;
import org.apache.james.mailbox.acl.UnionMailboxACLResolver;
import org.apache.james.mailbox.events.EventBus;
import org.apache.james.mailbox.events.RabbitMQEventBus;
import org.apache.james.mailbox.jpa.JPAMailboxSessionMapperFactory;
import org.apache.james.mailbox.jpa.mail.JPAMailboxMapper;
import org.apache.james.mailbox.jpa.mail.JPAMessageMapper;
import org.apache.james.mailbox.jpa.mail.JPAModSeqProvider;
import org.apache.james.mailbox.jpa.mail.JPAUidProvider;
import org.apache.james.mailbox.jpa.openjpa.OpenJPAMailboxManager;
import org.apache.james.mailbox.jpa.quota.JPAPerUserMaxQuotaDAO;
import org.apache.james.mailbox.jpa.quota.JPAPerUserMaxQuotaManager;
import org.apache.james.mailbox.jpa.quota.JpaCurrentQuotaManager;
import org.apache.james.mailbox.quota.CurrentQuotaManager;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.SessionProviderImpl;
import org.apache.james.mailbox.store.StoreMailboxAnnotationManager;
import org.apache.james.mailbox.store.StoreRightManager;
import org.apache.james.mailbox.store.extractor.JsoupTextExtractor;
import org.apache.james.mailbox.store.mail.MailboxMapperFactory;
import org.apache.james.mailbox.store.mail.MessageMapperFactory;
import org.apache.james.mailbox.store.mail.model.DefaultMessageId;
import org.apache.james.mailbox.store.mail.model.impl.MessageParser;
import org.apache.james.mailbox.store.quota.DefaultUserQuotaRootResolver;
import org.apache.james.mailbox.store.quota.QuotaComponents;
import org.apache.james.mailbox.store.quota.StoreQuotaManager;
import org.apache.james.mailbox.store.search.SimpleMessageSearchIndex;
import org.apache.james.metrics.logger.DefaultMetricFactory;
import org.apache.james.user.jpa.JPAUsersRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailboxConfiguration {

	@Bean
	JPAMailboxMapper jpaMailboxMapper(EntityManagerFactory entityManagerFactory) {
		return new JPAMailboxMapper(entityManagerFactory);
	}

	@Bean
	JPAMessageMapper jpaMessageMapper(EntityManagerFactory entityManagerFactory, JPAUidProvider p,
			JPAModSeqProvider seq) {
		return new JPAMessageMapper(p, seq, entityManagerFactory);
	}

	@Bean
	JPAUidProvider jpaUidProvider(EntityManagerFactory entityManagerFactory) {

		return new JPAUidProvider(entityManagerFactory);
	}

	@Bean
	JPAModSeqProvider jpaModSeqProvider(EntityManagerFactory entityManagerFactory) {
		return new JPAModSeqProvider(entityManagerFactory);
	}

	@Bean
	JPAMailboxSessionMapperFactory mailboxSessionMapperFactory(EntityManagerFactory entityManagerFactory,
			JPAUidProvider p, JPAModSeqProvider seq) {
		return new JPAMailboxSessionMapperFactory(entityManagerFactory, p, seq);
	}

	@Bean
	SessionProviderImpl sessionProviderImpl(UserRepositoryAuthenticator authenticator,
			UserRepositoryAuthorizator authorizator) {
		return new SessionProviderImpl(authenticator, authorizator);
	}

//    @Bean
//    InVMEventBus inVMEventBus() {
//
//        return new InVMEventBus(new InVmEventDelivery(new DefaultMetricFactory()), RetryBackoffConfiguration.DEFAULT,
//                new MemoryEventDeadLetters());
//    }

	@Bean
	StoreMailboxAnnotationManager storeMailboxAnnotationManager(MailboxSessionMapperFactory mailboxSessionMapperFactory,
			StoreRightManager rightManager) {
		return new StoreMailboxAnnotationManager(mailboxSessionMapperFactory, rightManager);
	}

	@Bean
	StoreRightManager storeRightManager(MailboxSessionMapperFactory mailboxSessionMapperFactory, EventBus eventBus) {
		return new StoreRightManager(mailboxSessionMapperFactory, new UnionMailboxACLResolver(),
				new SimpleGroupMembershipResolver(), eventBus);
	}

	@Bean
	QuotaComponents quotaComponents(JPAPerUserMaxQuotaManager maxQuotaManager, StoreQuotaManager quotaManager,
			DefaultUserQuotaRootResolver quotaRootResolver) {
		return new QuotaComponents(maxQuotaManager, quotaManager, quotaRootResolver);
	}

	@Bean
	DefaultUserQuotaRootResolver defaultUserQuotaRootResolver(SessionProviderImpl sessionProvider,
			MailboxSessionMapperFactory factory) {
		return new DefaultUserQuotaRootResolver(sessionProvider, factory);
	}

	@Bean
	JpaCurrentQuotaManager jpaCurrentQuotaManager(EntityManagerFactory entityManagerFactory) {
		return new JpaCurrentQuotaManager(entityManagerFactory);
	}

	@Bean
	StoreQuotaManager storeQuotaManager(JPAPerUserMaxQuotaManager maxQuotaManager,
			CurrentQuotaManager currentQuotaManager) {
		return new StoreQuotaManager(currentQuotaManager, maxQuotaManager);
	}

	@Bean
	JPAPerUserMaxQuotaManager jpaPerUserMaxQuotaManager(EntityManagerFactory entityManagerFactory) {
		return new JPAPerUserMaxQuotaManager(new JPAPerUserMaxQuotaDAO(entityManagerFactory));
	}

	@Bean
	OpenJPAMailboxManager openJPAMailboxManager(JPAMailboxSessionMapperFactory mapperFactory,
			SessionProviderImpl sessionProvider, RabbitMQEventBus eventBus,
			StoreMailboxAnnotationManager annotationManager, StoreRightManager storeRightManager,
			QuotaComponents quotaComponents, SimpleMessageSearchIndex index) {

		return new OpenJPAMailboxManager(mapperFactory, sessionProvider, new MessageParser(),
				new DefaultMessageId.Factory(), eventBus, annotationManager, storeRightManager, quotaComponents, index);

	}

	@Bean
	SimpleMessageSearchIndex simpleMessageSearchIndex(MessageMapperFactory messageMapperFactory,
			MailboxMapperFactory mailboxMapperFactory) {
		return new SimpleMessageSearchIndex(messageMapperFactory, mailboxMapperFactory, new JsoupTextExtractor());
	}

	@Bean
	UserRepositoryAuthenticator ura(JPAUsersRepository usersRepository) {
		return new UserRepositoryAuthenticator(usersRepository);
	}

	@Bean
	UserRepositoryAuthorizator userRepositoryAuthorizator(JPAUsersRepository usersRepository) {
		return new UserRepositoryAuthorizator(usersRepository);
	}

	@Bean
	JPAUsersRepository usersRepository(JPADomainList domainList) {
		return new JPAUsersRepository(domainList);
	}

	@Bean
	JPADomainList domailList(EntityManagerFactory entityManagerFactory) {

		return new JPADomainList(new DNSJavaService(new DefaultMetricFactory()), entityManagerFactory);
	}

}
