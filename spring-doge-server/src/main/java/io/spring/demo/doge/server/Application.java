/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.demo.doge.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import io.spring.demo.doge.photo.manipulate.DogePhotoManipulator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Application configuration and main method.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultiPartConfigFactory factory = new MultiPartConfigFactory();
        factory.setMaxFileSize("10Mb");
        return factory.createMultipartConfig();
    }

    @Bean
    public DogePhotoManipulator dogePhotoManipulator() {
        return new DogePhotoManipulator();
    }

    @Bean
    public GridFsTemplate gridFsTemplate(MongoDbFactory mongoDbFactory, MongoTemplate mongoTemplate) {
        return new GridFsTemplate(mongoDbFactory,
                mongoTemplate.getConverter());
    }

/*

    @Bean
    public MongoDbFactory mongoDbFactory(Mongo mongo) {
        return new SimpleMongoDbFactory(mongo, "doge");
    }
    @Bean
    MongoClient mongoClient (  Environment environment ) throws IOException
    {
         // VCAP_SERVICES={"mongolab":[{"name":"doge-mongodb","label":"mongolab","tags":["document","mongodb","Data Store"],"plan":"sandbox","credentials":{"uri":"mongodb://CloudFoundry_brfvsjks_m6ga9qrt_bfom35f8:8n41doAsVbviQulOVPAdGKTm8HaX7Vm1@ds043047.mongolab.com:43047/CloudFoundry_brfvsjks_m6ga9qrt"}}]}



        String creds =   environment.getProperty( "vcap.services.doge-mongodb.credentials" );

        System.out.println( "credentials for MongoLab MongoDB client: " + creds) ;
        URI uri  = URI.create( creds) ;

        ServerAddress serverAddress = new ServerAddress( uri.getHost(), uri.getPort()) ;
        MongoClient mongoClient =new MongoClient( serverAddress, Arrays.asList(  MongoCredential.createMongoCRCredential( uri.getUserInfo(), ur.getUser ))) ;

    }*/

/*
    @Configuration
    @Profile("default")
    public static class DefaultConfiguration {


    }

    @Configuration
    @Profile("cloud")
    //@EnableConfigurationProperties(MongoProperties.class)
    public static class CloudConfiguration {

  *//*      @Bean
        public Cloud cloud() {
            return new CloudFactory().getCloud();
        }*//*

        {

        }
        String mongoUri =
                "mongodb://CloudFoundry_brfvsjks_m6ga9qrt_lppt1ejo:jeTuhiPKR7H7JWvFJbjy2monmwaEF5GG@ds043047.mongolab.com:43047/CloudFoundry_brfvsjks_m6ga9qrt";

        @Bean
        public Mongo mongo() throws Exception {
            List<MongoCredential> mongoCredentials = Arrays.asList(MongoCredential.createMongoCRCredential(
                    "CloudFoundry_brfvsjks_m6ga9qrt_lppt1ejo", "CloudFoundry_brfvsjks_m6ga9qrt", "jeTuhiPKR7H7JWvFJbjy2monmwaEF5GG".toCharArray()));
            return new MongoClient(new ServerAddress("ds043047.mongolab.com", 43047), mongoCredentials);
        }


    }*/

    @Configuration
    @EnableScheduling
    @EnableWebSocketMessageBroker
    public static class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer
            implements SchedulingConfigurer {

        @Bean
        public ThreadPoolTaskScheduler reservationPool() {
            return new ThreadPoolTaskScheduler();
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/doge").withSockJS();
        }

        @Override
        public void configureClientOutboundChannel(ChannelRegistration registration) {
            registration.taskExecutor().corePoolSize(4).maxPoolSize(10);
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/queue/", "/topic/");
            registry.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            taskRegistrar.setTaskScheduler(reservationPool());
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
