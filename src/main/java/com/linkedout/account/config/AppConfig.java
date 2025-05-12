package com.linkedout.account.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.linkedout.common.messaging.ServiceIdentifier;

@Configuration
public class AppConfig {
	@Value("${service.name}")
	private String serviceName;

	@Bean
	public ServiceIdentifier serviceIdentifier() {
		return new ServiceIdentifier(serviceName);
	}

//	@Bean
//	public ModelMapper modelMapper() {
//		ModelMapper modelMapper = new ModelMapper();
//
//		modelMapper.getConfiguration()
//			.setMatchingStrategy(MatchingStrategies.STRICT)
//			.setFieldMatchingEnabled(true)
//			.setSkipNullEnabled(true)
//			.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
//
//		return modelMapper;
//	}
}
