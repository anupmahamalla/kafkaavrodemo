package com.ssnc.kafkaavrodemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaavrodemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaavrodemoApplication.class, args);
		System.out.println("Kafka Avro Demo Application Started Successfully");
	}

}
