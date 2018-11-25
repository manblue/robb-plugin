package com.robb.asm;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server2DubboServer {
	static String SUPER_NAME = "java/lang/Object";
	/**旧class文件全路径名称 com/ml/example/service/RobbService */
	final static String O_CLASS_FULL_NAME = "oldClassFullName";
	/**旧class文件名称*/
	final static String O_CLASS_NAME = "oldClassName";
	/**新class文件全路径名称 com/ml/example/service/impl/RobbServiceImpl */
	final static String N_CLASS_FULL_NAME = "newClassFullName";
	/**新class文件名称 */
	final static String N_CLASS_NAME = "newClassName";
	/**旧class描述 Lcom/robb/manager/RobbManager;*/
	final static String O_CLASS_DESC = "oldClassDesc";
	/**新class描述 Lcom/robb/manager/RobbManager;*/
	final static String N_CLASS_DESC = "newClassDesc";
	
	final static String separator = "/";//FileSystems.getDefault().getSeparator();
	
	private static Logger logger = LoggerFactory.getLogger(Server2DubboServer.class);
	
	public static Class buildDubboServerClass(InputStream is) {
		Class dubboServerClass = null;
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dubboServerClass;
	}
	
}
