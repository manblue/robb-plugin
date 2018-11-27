package com.robb.asm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.robb.config.AutoControConfig;
import com.robb.config.AutoServerConfig;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

/**
 * 根据业务serverImpl  class 自动生成给dubbo调用的业务接口层
 * */
public class Server2DubboServer {
	static String SUPER_NAME = "java/lang/Object";
	/**业务server class文件全路径名称 com/ml/example/service/RobbService */
	final static String SERVER_CLASS_FULL_NAME = "serverClassFullName";
	/**业务serverImpl class文件全路径名称*/
	final static String SERVER_IMPL_CLASS_FULL_NAME = "serverImplClassName";
	/**dubbo server clas文件全路径名称 com/ml/example/service/impl/RobbServiceImpl */
	final static String D_SERVER_CLASS_FULL_NAME = "dServerClassFullName";
	/**dubbo serverImpl class文件全路径名称 */
	final static String D_SERVER_IMPL_CLASS_FULL_NAME = "dServerImplClassName";
	/**旧class描述 Lcom/robb/manager/RobbManager;*/
	final static String O_CLASS_DESC = "oldClassDesc";
	/**新class描述 Lcom/robb/manager/RobbManager;*/
	final static String N_CLASS_DESC = "newClassDesc";
	
	final static String separator = "/";//FileSystems.getDefault().getSeparator();
	private Map<String, Object> paramCache = new HashMap<String, Object>();
	private static Logger logger = LoggerFactory.getLogger(Server2DubboServer.class);
	
	private Server2DubboServer() {
		// TODO Auto-generated constructor stub
	}
	
	public static Server2DubboServer getHandler() {
		return new Server2DubboServer();
	}
		
	public Class buildDubboServerClass(InputStream is) {
		paramCache.clear();
		Class dServerImpl = null;
		try {
			//遍历class信息
			ClassReader classReader = new ClassReader(is);
			//业务serverImpl
			ClassNodeAdapter serverImplClassNode = new ClassNodeAdapter();
			classReader.accept(serverImplClassNode, ClassReader.EXPAND_FRAMES);
			//校验注解org.springframework.stereotype.Service
			checkAnnotion(serverImplClassNode);
			
			
			AsmHandler handler = new AsmHandler();
			//dubbo server class 【原业务server class】
			Class dServerClass = handler.getInterfaceClass(serverImplClassNode);
			paramCache.put(D_SERVER_CLASS_FULL_NAME, dServerClass.getName().replace('.', '/'));

			classReader = new ClassReader(dServerClass.getName());
			//业务server
			ClassNodeAdapter serverClassNode = new ClassNodeAdapter();
			classReader.accept(serverClassNode, ClassReader.EXPAND_FRAMES);
			String serverClassName = serverClassNode.name.concat("4Busi");
			paramCache.put(SERVER_CLASS_FULL_NAME, serverClassName);
			//修改类全路径名 业务server
			serverClassNode.changeClassName(serverClassName);			
			
			ClassApdater apdater = new ClassApdater();
			serverClassNode.accept(apdater);
			byte[] bb = apdater.getClassWriter().toByteArray();
			outFile(serverClassNode.name, bb);
			//新业务server class
			Class  serverClass = handler.defineClazz(serverClassNode.name.replace('/', '.'), bb, 0, bb.length, ClassUtils.getDefaultClassLoader());
			
			//dubbo serverImpl class
			dServerImpl = null;
			
			apdater = new ClassApdater();
			//修改业务serverImpl接口名称 及注解名称
			serverImplClassNode.changeClassInterfaceName(dServerClass.getName().replace('.', '/'), serverClassName);
			serverImplClassNode.accept(apdater);
			bb = apdater.getClassWriter().toByteArray();
			outFile(serverImplClassNode.name, bb);
			//业务serverImpl class
			Class  serverImplClass = handler.defineClazz(serverImplClassNode.name.replace('/', '.'), bb, 0, bb.length, ClassUtils.getDefaultClassLoader());
			AutoServerConfig.addCache(serverImplClassNode.name.replace('/', '.'), serverImplClass);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dServerImpl;
	}
	
	
	
	/**
	 * 校验注解org.springframework.stereotype.Service
	 * */
	private static void checkAnnotion(ClassNode classNode) throws IllegalAccessException {
		if (CollectionUtils.isEmpty(classNode.visibleAnnotations)) {
			throw new IllegalAccessException("class["+classNode.name+"] must declared annotation[org.springframework.stereotype.Service]");
		}
		boolean ok = true;
		for(AnnotationNode node : classNode.visibleAnnotations){
			if ("Lorg/springframework/stereotype/Service;".equals(node.desc)) {
				ok = false;
				if (StringUtils.isBlank((CharSequence) node.values.get(1))) {
					throw new IllegalAccessException("class["+classNode.name+"] annotation[org.springframework.stereotype.Service] must set 'name'");
				}
			}
		}
		if (ok) {
			throw new IllegalAccessException("class["+classNode.name+"] must declared annotation[org.springframework.stereotype.Service]");
		}
	}
	
	private static void outFile(String name,byte[] code) {
		String basePath = "D:\\maolong\\DEV\\spring\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\robb-soa\\WEB-INF\\classes\\";
		String outFile = basePath+name.replace('.',	'/' )+".class";
		System.out.println("outFile====="+outFile);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outFile);
			fos.write(code);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
	
	
	
	
	
	
	
	
	
	
	class AsmHandler implements Opcodes{
		
		/**
		 * 获取接口class
		 * @param classNode
		 * 		业务serverImpl
		 * */
		public Class getInterfaceClass(ClassNode classNode) {
			String serverImplName = StringUtils.substringAfterLast(classNode.name, "/");
			String serverName = serverImplName.replace("Impl", "");
			String serverFullName = null;
			if (CollectionUtils.isEmpty(classNode.interfaces)) {
				throw new IllegalArgumentException("getInterfaceClass from "+classNode.name+" err class["+classNode.name+"]must implements interface");
			}else {
				for (String interfaceName : classNode.interfaces) {
					if (interfaceName.endsWith(serverName)) {
						serverFullName = interfaceName;
						break;
					}
				}
				if (StringUtils.isNotBlank(serverFullName)) {
					try {
						return ClassUtils.forName(serverFullName.replace('/', '.'), ClassUtils.getDefaultClassLoader());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						throw new IllegalArgumentException("getInterfaceClass from "+classNode.name+" err:"+e.getMessage());
					} catch (LinkageError e) {
						e.printStackTrace();
						throw new IllegalArgumentException("getInterfaceClass from "+classNode.name+" err:"+e.getMessage());
					}
				}
			}
			throw new IllegalArgumentException("getInterfaceClass from "+classNode.name+" err: please change interface name to "+serverName);

		}
		/**
		 * 拼装class头信息
		 * @param classNode
		 * 		业务serverImpl
		 * */
		public ClassWriter buildClassHead(ClassNode classNode) {
			//classNode.name eg:com/robb/manager/RobbManager
			String dubboServerImplName = classNode.name.concat("4Dubbo");//java.lang.String
			String simpleName =  StringUtils.substringAfterLast(dubboServerImplName, separator);
			
			paramCache.put(SERVER_IMPL_CLASS_FULL_NAME, classNode.name);
			paramCache.put(D_SERVER_IMPL_CLASS_FULL_NAME, dubboServerImplName);
//			paramCache.put(N_CLASS_DESC, new StringBuilder("L").append(controName).append(';').toString());
//			paramCache.put(O_CLASS_FULL_NAME, classNode.name);
//			paramCache.put(O_CLASS_NAME, StringUtils.substringAfterLast(classNode.name, separator));
//			paramCache.put(O_CLASS_DESC, new StringBuilder("L").append(classNode.name).append(';').toString());
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			cw.visit(classNode.version, 
					classNode.access,
					dubboServerImplName, null, SUPER_NAME,
					null);
			//处理注解
			if (CollectionUtils.isNotEmpty(classNode.visibleAnnotations)) {
				for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
					//TODO 注解过滤逻辑
//					if (AutoControConfig.checkClassAnntoFilter(annotationNode.desc)) {
//						continue;
//					}
					if (annotationNode.desc.equals(Type.getDescriptor(Service.class))) {
						System.out.println("---annotationNode.desc:"+annotationNode.desc);
						AnnotationVisitor visitor = cw.visitAnnotation(annotationNode.desc, true);
						annotationNode.accept(visitor);
						visitor.visitEnd();
						break;
					}
				}
			}
			//默认初始化方法
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
					"<init>", "()V", null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, SUPER_NAME, "<init>", "()V",false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			return cw;
	}
		
		public final Class<?> defineClazz(String name, byte[] b, int off, int len,ClassLoader classLoader)
	            throws ClassFormatError
	        {
				try {
					Method cc = ClassLoader.class.getDeclaredMethod("defineClass", String.class,byte[].class,int.class,int.class);
					cc.setAccessible(true);
					return (Class<?>)cc.invoke(classLoader, new Object[]{name,b,off,len});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            return null;
	        }
	}


}
