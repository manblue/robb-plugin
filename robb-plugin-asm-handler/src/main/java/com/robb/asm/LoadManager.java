package com.robb.asm;

import java.lang.reflect.Method;

import com.robb.config.AutoControConfig;

import jdk.internal.org.objectweb.asm.ClassWriter;

public class LoadManager {

	private LoadManager() {
		// TODO Auto-generated constructor stub
	}
	
	public static Class loadManagerByCache(String className,ClassLoader classLoader) {
		try {
				ClassWriter cw = AutoControConfig.getCache(className);
				byte[] b = cw.toByteArray();
				Method cc = ClassLoader.class.getDeclaredMethod("defineClass", String.class,byte[].class,int.class,int.class);
				cc.setAccessible(true);
				return (Class<?>)cc.invoke(classLoader, new Object[]{className.replace('.', '/'),cw.toByteArray(),0,b.length});
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
}
