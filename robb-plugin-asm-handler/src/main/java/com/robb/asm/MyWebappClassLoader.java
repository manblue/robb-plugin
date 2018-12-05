package com.robb.asm;

import org.apache.catalina.loader.WebappClassLoader;

public class MyWebappClassLoader extends WebappClassLoader {

	public MyWebappClassLoader() {
		super();
	}
	
	public MyWebappClassLoader(ClassLoader parent){
		super(parent);
	}
	
	@Override
	public synchronized Class<?> loadClass(String arg0, boolean arg1)
			throws ClassNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("===============:"+arg0+",===:"+arg1);
		return super.loadClass(arg0, arg1);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("===============:"+name);
		return super.loadClass(name);
	}
	
}
