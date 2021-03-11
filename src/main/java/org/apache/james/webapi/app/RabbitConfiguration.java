package org.apache.james.webapi.app;

import java.net.URISyntaxException;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.james.backends.rabbitmq.RabbitMQConfiguration;
import org.apache.james.backends.rabbitmq.RabbitMQConnectionFactory;
import org.apache.james.backends.rabbitmq.ReceiverProvider;
import org.apache.james.backends.rabbitmq.SimpleConnectionPool;
import org.apache.james.event.json.EventSerializer;
import org.apache.james.mailbox.events.MailboxIdRegistrationKey;
import org.apache.james.mailbox.events.MemoryEventDeadLetters;
import org.apache.james.mailbox.events.RabbitMQEventBus;
import org.apache.james.mailbox.events.RetryBackoffConfiguration;
import org.apache.james.mailbox.events.RoutingKeyConverter;
import org.apache.james.mailbox.jpa.JPAId;
import org.apache.james.mailbox.quota.QuotaRootDeserializer;
import org.apache.james.mailbox.store.mail.model.DefaultMessageId;
import org.apache.james.metrics.logger.DefaultMetricFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Sets;

import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;

@Configuration
//@Conditional
public class RabbitConfiguration {

	@Bean
	SimpleConnectionPool simpleConnectionPool(WebApiConfiguration conf) throws URISyntaxException {
		PropertiesConfiguration propertiesConfiguration = new org.apache.commons.configuration2.PropertiesConfiguration();
		propertiesConfiguration.addProperty("uri", conf.getAmqpUri());
		propertiesConfiguration.addProperty("management.uri", conf.getAmqpManagementUri());
		propertiesConfiguration.addProperty("management.user", conf.getAmqpManagementUser());
		propertiesConfiguration.addProperty("management.password", conf.getAmqpManagementPass());
		RabbitMQConfiguration rabbitMQConfiguration = RabbitMQConfiguration.from(propertiesConfiguration);
		RabbitMQConnectionFactory factory = new RabbitMQConnectionFactory(rabbitMQConfiguration);
		return new SimpleConnectionPool(factory);
	}

	@Bean
	public ReceiverProvider provideRabbitMQReceiver(SimpleConnectionPool simpleConnectionPool) {
		return () -> RabbitFlux
				.createReceiver(new ReceiverOptions().connectionMono(simpleConnectionPool.getResilientConnection()));
	}

	@Bean
	EventSerializer eventSerializer(QuotaRootDeserializer quotaResolver) {
		return new EventSerializer(new JPAId.Factory(), new DefaultMessageId.Factory(), quotaResolver);
	}

	@Bean
	RabbitMQEventBus rabbitMQEventBus(ReceiverProvider receiverProvider, EventSerializer eventSerializer) {
		RoutingKeyConverter rkc = new RoutingKeyConverter(
				Sets.newHashSet(new MailboxIdRegistrationKey.Factory(new JPAId.Factory())));
		return new RabbitMQEventBus(new Sender(), receiverProvider, eventSerializer, RetryBackoffConfiguration.DEFAULT,
				rkc, new MemoryEventDeadLetters(), new DefaultMetricFactory());
	}

}
