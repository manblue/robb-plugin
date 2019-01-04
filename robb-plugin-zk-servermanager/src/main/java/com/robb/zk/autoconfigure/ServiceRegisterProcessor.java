package com.robb.zk.autoconfigure;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

import com.robb.zk.ServiceRegister;

public class ServiceRegisterProcessor implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			try {
				ServiceRegister.register(EnvironmentAutoConfiguration.getEnv());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
