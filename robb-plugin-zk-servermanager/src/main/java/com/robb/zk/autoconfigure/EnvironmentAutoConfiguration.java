package com.robb.zk.autoconfigure;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

//@Configuration
public class EnvironmentAutoConfiguration implements EnvironmentAware {

	private static Environment env;
	
	public static Environment getEnv() {
		return env;
	}
	
	@Override
	public void setEnvironment(Environment environment) {
		EnvironmentAutoConfiguration.env = environment;
	}

}
