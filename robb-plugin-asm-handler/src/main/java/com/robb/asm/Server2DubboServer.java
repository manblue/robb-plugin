package com.robb.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.robb.config.AutoControConfig;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

/**
 * 根据server interface  class 自动生成给dubbo调用的业务接口层
 * */
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
			//遍历class信息
			ClassReader classReader = new ClassReader(is);
			ClassNodeAdapter classNode = new ClassNodeAdapter();
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			
			
			
			
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
	
	static class ClassHandler implements Opcodes{
		
		public Class getInterfaceClass(ClassNode classNode,Map<String, Object> outPutParams) {
			String serverImplName = StringUtils.substringAfterLast(classNode.name, ".");
			String serverName = serverImplName.replace("Impl", "");
			String serverFullName = null;
			if (classNode.interfaces.size() == 1) {
				serverFullName = classNode.interfaces.get(0);
				try {
					return ClassUtils.forName(serverFullName, ClassUtils.getDefaultClassLoader());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new IllegalArgumentException("getInterfaceClass from "+classNode.name+" err:"+e.getMessage());
				} catch (LinkageError e) {
					e.printStackTrace();
					throw new IllegalArgumentException("getInterfaceClass from "+classNode.name+" err:"+e.getMessage());
				}
			}
			
			return null;
		}
		//拼装class头信息
		public ClassWriter buildClassHead(ClassNode classNode,Map<String, Object> outPutParams) {
			//classNode.name eg:com/robb/manager/RobbManager
			String controName = classNode.name.replace("Manager", "Controller").replace("manager", "controller");//java.lang.String
			String simpleName =  StringUtils.substringAfterLast(controName, separator);
			
			outPutParams.put(N_CLASS_FULL_NAME, controName);
			outPutParams.put(N_CLASS_NAME, simpleName);
			outPutParams.put(N_CLASS_DESC, new StringBuilder("L").append(controName).append(';').toString());
			outPutParams.put(O_CLASS_FULL_NAME, classNode.name);
			outPutParams.put(O_CLASS_NAME, StringUtils.substringAfterLast(classNode.name, separator));
			outPutParams.put(O_CLASS_DESC, new StringBuilder("L").append(classNode.name).append(';').toString());
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			cw.visit(classNode.version, 
					ACC_PUBLIC + ACC_SUPER,
					controName, null, SUPER_NAME,
					null);
			//处理注解
			cw.visitAnnotation("Lorg/springframework/web/bind/annotation/RestController;", true).visitEnd();
			if (CollectionUtils.isNotEmpty(classNode.visibleAnnotations)) {
				for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
					//TODO 注解过滤逻辑
					if (AutoControConfig.checkClassAnntoFilter(annotationNode.desc)) {
						continue;
					}
					System.out.println("---annotationNode.desc:"+annotationNode.desc);
					AnnotationVisitor visitor = cw.visitAnnotation(annotationNode.desc, true);
					annotationNode.accept(visitor);
					visitor.visitEnd();
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
	}

	
}
