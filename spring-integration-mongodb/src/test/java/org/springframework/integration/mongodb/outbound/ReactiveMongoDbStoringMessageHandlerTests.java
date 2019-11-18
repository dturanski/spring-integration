/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.mongodb.outbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.mongodb.rules.MongoDbAvailable;
import org.springframework.integration.mongodb.rules.MongoDbAvailableTests;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import org.bson.conversions.Bson;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

/**
 * @author Amol Nayak
 * @author Oleg Zhurakousky
 * @author Gary Russell
 * @author David Turanski
 *
 * @since 5.2.2
 */
public class ReactiveMongoDbStoringMessageHandlerTests extends MongoDbAvailableTests {

	@Test(expected = IllegalArgumentException.class)
	public void withNullMongoDBFactory() {
		new ReactiveMongoDbStoringMessageHandler((ReactiveMongoDatabaseFactory) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withNullMongoTemplate() {
		new ReactiveMongoDbStoringMessageHandler((ReactiveMongoOperations) null);
	}


	@Test
	@MongoDbAvailable
	public void validateMessageHandlingWithDefaultCollection() throws Exception {

		ReactiveMongoDatabaseFactory mongoDbFactory = this.prepareReactiveMongoFactory();
		ReactiveMongoDbStoringMessageHandler handler = new ReactiveMongoDbStoringMessageHandler(mongoDbFactory);
		handler.setBeanFactory(mock(BeanFactory.class));
		handler.afterPropertiesSet();
		Message<Person> message = MessageBuilder.withPayload(this.createPerson("Bob")).build();
		handler.handleRequestMessage(message).block(Duration.ofSeconds(1));

		ReactiveMongoTemplate template = new ReactiveMongoTemplate(mongoDbFactory);
		Query query = new BasicQuery("{'name' : 'Bob'}");
		Person person = template.findOne(query, Person.class, "data").block(Duration.ofSeconds(1));
		assertThat(person.getName()).isEqualTo("Bob");
		assertThat(person.getAddress().getState()).isEqualTo("PA");

	}

	@Test
	@MongoDbAvailable
	public void validateMessageHandlingWithNamedCollection() throws Exception {

		ReactiveMongoDatabaseFactory mongoDbFactory = this.prepareReactiveMongoFactory();
		ReactiveMongoDbStoringMessageHandler handler = new ReactiveMongoDbStoringMessageHandler(mongoDbFactory);
		handler.setCollectionNameExpression(new LiteralExpression("foo"));
		handler.setBeanFactory(mock(BeanFactory.class));
		handler.setRequiresReply(false);
		handler.afterPropertiesSet();
		Message<Person> message = MessageBuilder.withPayload(this.createPerson("Bob")).build();
		handler.handleRequestMessage(message).block(Duration.ofSeconds(1));

		ReactiveMongoTemplate template = new ReactiveMongoTemplate(mongoDbFactory);
		Query query = new BasicQuery("{'name' : 'Bob'}");
		Person person = template.findOne(query, Person.class, "foo").block(Duration.ofSeconds(1));

		assertThat(person.getName()).isEqualTo("Bob");
		assertThat(person.getAddress().getState()).isEqualTo("PA");
	}

	@Test
	@MongoDbAvailable
	public void validateMessageHandlingWithMongoConverter() throws Exception {

		ReactiveMongoDatabaseFactory mongoDbFactory = this.prepareReactiveMongoFactory();
		ReactiveMongoDbStoringMessageHandler handler = new ReactiveMongoDbStoringMessageHandler(mongoDbFactory);
		handler.setCollectionNameExpression(new LiteralExpression("foo"));
		MappingMongoConverter converter = new ReactiveTestMongoConverter(mongoDbFactory, new MongoMappingContext());
		converter.afterPropertiesSet();
		converter = spy(converter);
		handler.setMongoConverter(converter);
		handler.setBeanFactory(mock(BeanFactory.class));
		handler.afterPropertiesSet();
		Message<Person> message = MessageBuilder.withPayload(this.createPerson("Bob")).build();
		handler.handleRequestMessage(message).block(Duration.ofSeconds(1));

		ReactiveMongoTemplate template = new ReactiveMongoTemplate(mongoDbFactory);
		Query query = new BasicQuery("{'name' : 'Bob'}");
		Person person = template.findOne(query, Person.class, "foo").block(Duration.ofSeconds(1));

		assertThat(person.getName()).isEqualTo("Bob");
		assertThat(person.getAddress().getState()).isEqualTo("PA");
		verify(converter, times(1)).write(Mockito.any(), Mockito.any(Bson.class));
	}

	@Test
	@MongoDbAvailable
	public void validateMessageHandlingWithMongoTemplate() throws Exception {
		ReactiveMongoDatabaseFactory mongoDbFactory = this.prepareReactiveMongoFactory();
		MappingMongoConverter converter = new ReactiveTestMongoConverter(mongoDbFactory, new MongoMappingContext());
		converter.afterPropertiesSet();
		converter = spy(converter);
		ReactiveMongoTemplate template = new ReactiveMongoTemplate(mongoDbFactory, converter);


		ReactiveMongoDbStoringMessageHandler handler = new ReactiveMongoDbStoringMessageHandler(mongoDbFactory);
		handler.setCollectionNameExpression(new LiteralExpression("foo"));
		handler.setBeanFactory(mock(BeanFactory.class));
		handler.afterPropertiesSet();
		Message<Person> message = MessageBuilder.withPayload(this.createPerson("Bob")).build();
		handler.handleRequestMessage(message).block(Duration.ofSeconds(1));

		ReactiveMongoTemplate readingTemplate = new ReactiveMongoTemplate(mongoDbFactory);
		Query query = new BasicQuery("{'name' : 'Bob'}");
		Person person = readingTemplate.findOne(query, Person.class, "foo").block();

		assertThat(person.getName()).isEqualTo("Bob");
		assertThat(person.getAddress().getState()).isEqualTo("PA");
		verify(converter, times(1)).write(Mockito.any(), Mockito.any(Bson.class));
	}
}
