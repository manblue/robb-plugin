package com.robb.asm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ValueConstants;

public class DefaultManager2Controller {

	final static String superName = "java/lang/Object";
	/**接口类全名 com/ml/example/service/RobbService */
	final static String kFullInterfaceName = "fullInterfaceName";
	/**接口类名*/
	final static String kInterfaceName = "interfaceName";
	/**接口IMPL类全名 com/ml/example/service/impl/RobbServiceImpl */
	final static String kFullImplName = "fullImplName";
	/**接口IMPL类名 */
	final static String kImplName = "implName";
	/**字段名称*/
	final static String kFiedlName = "fieldName";
	/**字段描述*/
	final static String kFieldDesc = "fieldDesc";
	
	final static String separator = "/";//FileSystems.getDefault().getSeparator();
	
	private final static Logger LOGGER = LoggerFactory.getLogger(DefaultManager2Controller.class);
	
	public static Class buildControClass(Class managerClass,InputStream is) {
		Class implClass = null;
		try {

			if (!managerClass.getSimpleName().endsWith("Manager")) {
				throw new IllegalArgumentException(" this plugin only support Manager layer!");
			}
			InterfaceHandler4Asm handler4Asm = new InterfaceHandler4Asm(managerClass.getClassLoader());
			//获取相关签名信息
			ClassPrinter printer = ClassPrinter.getNewPrinter(true);
//			InputStream is = managerClass.getClassLoader().getSystemResourceAsStream(managerClass.getName().replace(".", "/")+".class");
			ClassReader cReader = new ClassReader(is);
			cReader.accept(printer, 0);			
			
			Map<String, Object> outPutParams = new HashMap<String, Object>();
			outPutParams.put(kInterfaceName, managerClass.getSimpleName());
			outPutParams.put(kFullInterfaceName, managerClass.getName().replace('.', '/'));
			outPutParams.put(kImplName, managerClass.getSimpleName()+"Impl");
			
			Type.getDescriptor(managerClass);
			ClassWriter cw = handler4Asm.buildClassHead(managerClass,outPutParams);
			FieldVisitor fv = handler4Asm.buildClassField(cw, managerClass,outPutParams);
			handler4Asm.buildClassMethod(cw,managerClass, printer.getVisitMethods(),outPutParams);
			cw.visitEnd();
			//写文件
			byte[] code = cw.toByteArray();
			FileOutputStream fos = null;
			String outFile = managerClass.getResource("/").getPath()+(String) outPutParams.get(kFullImplName)+".class";
			System.out.println("outFile====="+outFile);
				fos = new FileOutputStream(outFile);
				fos.write(code);
				fos.close();


			implClass = handler4Asm.defineClazz(((String) outPutParams.get(kFullImplName)).replace('/', '.'), code, 0, code.length);
//			implClass = interfaceClass.getClassLoader().loadClass(((String) outPutParams.get(kFullImplName)).replace('/', '.'));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return implClass;
	}

	
	static class InterfaceHandler4Asm extends ClassLoader implements Opcodes {

		public InterfaceHandler4Asm(ClassLoader parent) {
			super(parent);
		}
		//拼装类头信息，构造方法
		public ClassWriter buildClassHead(Class clazz, Map<String, Object> outPutParams) {
			String nameImpl = clazz.getName().replace(".", separator).replace("Manager", "Controller1").replace("manager", "controller");//java.lang.String
			String simpleName =  clazz.getSimpleName().replace("Manager", "Controller1");
			
			outPutParams.put(kFullImplName, nameImpl);
			outPutParams.put(kImplName, simpleName);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			cw.visit(V1_7, 
					ACC_PUBLIC + ACC_SUPER,
					nameImpl, null, superName,
					null);
			cw.visitAnnotation("Lorg/springframework/web/bind/annotation/RestController;", true);
			RequestMapping requestMapping = (RequestMapping)clazz.getAnnotation(RequestMapping.class);
			AnnotationVisitor annotationVisitor = cw.visitAnnotation("Lorg/springframework/web/bind/annotation/RequestMapping;", true);
			buildAnnotation(annotationVisitor, requestMapping);
			annotationVisitor.visitEnd();
			
			//空构造
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
					"<init>", "()V", null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			return cw;
		}
		
		//拼装字段信息
		public FieldVisitor buildClassField(ClassWriter cw,Class clazz, Map<String, Object> outPutParams) {
			String sname = clazz.getSimpleName();
			String fieldName = StringUtils.lowerCase(StringUtils.substring(sname, 0, 1))+StringUtils.substring(sname, 1);
			String fieldDesc = "L"+clazz.getName().replace(".", separator)+";";
			outPutParams.put(kFiedlName, fieldName);
			outPutParams.put(kFieldDesc, fieldDesc);
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName, fieldDesc, null, null);
			
			fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true);
			fv.visitEnd();
			return fv;
		}
		
		//拼装方法信息
		public void buildClassMethod(ClassWriter cw, Class clazz,List<Map<String, Object>> classMethods, Map<String, Object> outPutParams) {
			String fieldName = (String) outPutParams.get(kFiedlName);
			String fieldDesc = (String) outPutParams.get(kFieldDesc);
			String fullInterfaceName = (String) outPutParams.get(kFullInterfaceName);
			String fullImplName = (String) outPutParams.get(kFullImplName);
			
			Map<String, Method> methodMap = new HashMap<String, Method>();
			for (Method method : clazz.getDeclaredMethods()) {
				methodMap.put(method.getName()+":"+Type.getMethodDescriptor(method), method);
			}
			
			for (Map<String, Object> classMethod : classMethods) {
				String mName = (String)classMethod.get(ClassPrinter.pname);
				if (mName.contains("<") || mName.equals(clazz.getSimpleName())) {//初始化函数 构造函数
					continue;
				}
				
				String mDesc = (String)classMethod.get(ClassPrinter.pdesc);
				boolean returnFlag = mDesc.contains(")V") ? false : true;//是否有返回
				Method method = methodMap.get(mName+":"+mDesc);
				if (!Modifier.isPublic(method.getModifiers())) {//非public方法，不处理
					continue;
				}
				
				
				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + (returnFlag ? ACC_VARARGS:0), 
						mName, 
						mDesc, 
						(String)classMethod.get(ClassPrinter.psignature), 
						(String[])classMethod.get(ClassPrinter.pexceptions));
				
//					RequestMapping requestMapping = (RequestMapping)method.getAnnotation(RequestMapping.class);
//					AnnotationVisitor aVisitor = mv.visitAnnotation("Lorg/springframework/web/bind/annotation/RequestMapping;", true);
//					AnnotationVisitor visitor1 = aVisitor.visitArray("value");
//					visitor1.visit("value", requestMapping.value()[0]);
//					visitor1.visitEnd();
////					aVisitor.visit("method", requestMapping.method()[0]);
//					visitor1 = aVisitor.visitArray("method");
//					visitor1.visitEnum("method", "Lorg.springframework.web.bind.annotation.RequestMethod;", requestMapping.method()[0].name());
//					visitor1.visitEnd();
//					aVisitor.visitEnd();

					buildMethodAnnotation(mv, method);
//				for (Parameter parameter : method.getParameters()) {
//					System.out.println("---"+mName+":-"+parameter.getName());
//					for (Annotation pAnnotation : parameter.getAnnotations()) {
//						System.out.println("---"+mName+":-"+parameter.getName()+"-"+pAnnotation.annotationType());
//						for (Method field : pAnnotation.annotationType().getDeclaredMethods()) {
//							try {
//								try {
//									System.out.println("---"+mName+":-"+parameter.getName()+"-"+pAnnotation.annotationType()+"-"+field.getName());
//									System.out.println("----------"+field.invoke(pAnnotation, null));
//								} catch (InvocationTargetException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//							} catch (IllegalArgumentException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (IllegalAccessException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//
//						}
//					}
//				}
					
					
				mv.visitCode();
				//字段调用逻辑
				mv.visitVarInsn(ALOAD, 0);//this
				if (mName.startsWith("set")) {
					mv.visitVarInsn(ALOAD, 1);
					mv.visitFieldInsn(PUTFIELD, fullImplName, fieldName, fieldDesc);
				}else {
					mv.visitFieldInsn(GETFIELD, fullImplName, fieldName, fieldDesc);//fieldName
					//入参描述符分割
					buildMethodVisitorArgs(mv, mDesc,method);
					mv.visitMethodInsn(INVOKEINTERFACE, fullInterfaceName, mName, mDesc, true);//mName
				}

//				mv.visitVarInsn(ALOAD, 1);//入参1
				/**
				for (Method mh : RobbService.class.getDeclaredMethods()) {
					System.out.println("--"+Type.getMethodDescriptor(mh));
					
					for (Type type : Type.getArgumentTypes(mh)) {
						System.out.println("-----"+type.getDescriptor());
					}
				}
				*/
//				mv.visitLocalVariable("name", "Ljava/lang/String;", null, start, end, 1);
				buildParameterAnnotation(mv, method);
				mv.visitInsn(returnFlag?ARETURN:RETURN);
				mv.visitMaxs(3, 1);
				mv.visitEnd();
			}
		}
		
		  /**
		   * 入参描述符分割
		   *  我们知道JAVA类型分为基本类型和引用类型，在JVM中对每一种类型都有与之相对应的类型描述，如下表：
		   Java类型---------------JVM中的描述
		   boolean----------------Z--------------------------iload
		   char---------------------C--------------------------iload
		   byte---------------------B--------------------------iload
		   short--------------------S--------------------------iload
		   int------------------------I--------------------------iload
		   float---------------------F--------------------------fload
		   long---------------------J--------------------------lload
		   double------------- ----D-------------------------dload
		   Object-----------------Ljava/lang/Object;------aload
		   int----------------------[I------------------------  -aload
		   Object-----------------[[Ljava/lang/Object;----aload
       */
		private static  void buildMethodVisitorArgs(MethodVisitor mv,String methodArgsDesc,Method method) {
			methodArgsDesc = Type.getMethodDescriptor(method);
			if (methodArgsDesc.startsWith("()")) {//无参数
				return ;
			}
			String argsDesc = StringUtils.substringBefore(StringUtils.substringAfter(methodArgsDesc, "("), ")");
			
			String[] args = StringUtils.split(argsDesc,';');
			List<Integer> loadList = new LinkedList<Integer>();
			boolean skip = false;//跳过标识
			for (int i = 0; i < args.length; i++) {
				for (char ch : args[i].toCharArray()) {
					System.out.println("-------------"+ch+"---"+skip);
					if (ch == 'Z') {
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(ILOAD);
					}else if(ch == 'C'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(ILOAD);
					}else if(ch == 'B'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(ILOAD);
					}else if(ch == 'S'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(ILOAD);
					}else if(ch == 'I'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(ILOAD);
					}else if(ch == 'F'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(FLOAD);
					}else if(ch == 'J'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(LLOAD);
					}else if(ch == 'D'){
						if (skip) {
							skip = false;
							continue;
						}
						loadList.add(DLOAD);
					}else if(ch == 'L'){
						if (skip) {
							skip = false;
							break;
						}
						loadList.add(ALOAD);
						break;
						
					}else if(ch == '['){
						if (!skip) {
							loadList.add(ALOAD);
						}
						skip = true;
					}else if (ch == '(' || ch == ')') {
						continue;
					}else {
						throw new IllegalArgumentException("deal methodArgsDesc["+methodArgsDesc+"] err:"+ch);
					}
				}
			}
			
			//亚栈入参指令
			int loadNum = 1;
			for (Integer loadOpcode : loadList) {
				System.out.println(loadOpcode+"-----------"+loadNum);
//				if (loadNum == 8) {
//					loadNum++;
//				}
				mv.visitVarInsn(loadOpcode, loadNum++);
			}
			
		}
		
		/**
		 * 拼装方法注解
		 * */
		private static  void buildMethodAnnotation(MethodVisitor mv, Method method) {
			Annotation[] annotations = method.getAnnotations();
			if (ArrayUtils.isEmpty(annotations)) {
				return;
			}
			
			for (Annotation methodAnnotation : annotations) {
				//paramAnnotation 为注解代理 class com.sun.proxy.$Proxy11
				   //paramAnnotation.annotationType() 为原始注解
					Class orgAnnotation = methodAnnotation.annotationType();
					AnnotationVisitor aVisitor = mv.visitAnnotation(Type.getDescriptor(orgAnnotation), true);
					buildAnnotation(aVisitor, methodAnnotation);
					aVisitor.visitEnd();
			}
		}
		/**
		 * 拼装方法参数名称，参数注解，方法注解
		 * */
		private static  void buildParameterAnnotation(MethodVisitor mv, Method method) {
			Parameter[] parameters = method.getParameters();
			if (ArrayUtils.isEmpty(parameters)) {
				return ;
			}
			
			int pindex = 0;
			for (Parameter parameter : parameters) {
				if (!parameter.isNamePresent()) {
					throw new IllegalArgumentException("please use JDK8 compile [-parameters]");
				}
				//添加参数名称
				mv.visitParameter(parameter.getName(), Opcodes.ACC_MANDATED);
				
				//添加参数注解 默认只有一个注解
//				AnnotatedType paramAnnotated = parameter.getAnnotatedType();
				Annotation[] paramAnnotations = parameter.getAnnotations();
				if (ArrayUtils.isNotEmpty(paramAnnotations)) {
					for (Annotation paramAnnotation : paramAnnotations) {
						//paramAnnotation 为注解代理 class com.sun.proxy.$Proxy11
					   //paramAnnotation.annotationType() 为原始注解
						Class orgAnnotation = paramAnnotation.annotationType();
						AnnotationVisitor paramVisitor = mv.visitParameterAnnotation(pindex, Type.getDescriptor(orgAnnotation), true);
						buildAnnotation(paramVisitor, paramAnnotation);
						paramVisitor.visitEnd();
					}
				}
			}
		}
		/**
		 * 拼装注解对象
		 * */
		private static void buildAnnotation(AnnotationVisitor annotationVisitor,Annotation annotationObj) {

			//paramAnnotation 为注解代理 class com.sun.proxy.$Proxy11
			   //paramAnnotation.annotationType() 为原始注解
				Class orgAnnotation = annotationObj.annotationType();
				for (Method paramAnnMethod : orgAnnotation.getDeclaredMethods()) {
					try {
						LOGGER.info("----- AnnotatedType:{} field:{}", orgAnnotation,paramAnnMethod);
						Object val = paramAnnMethod.invoke(annotationObj, null);
						if (val == null) {
							continue;
						}
						
						if (val.getClass().isArray()) {
							if (ArrayUtils.isEmpty((Object[]) val)) {
								continue;
							}
							AnnotationVisitor ava = annotationVisitor.visitArray(paramAnnMethod.getName());
							for (Object ov : (Object[])val) {
								buildAnnotationAddField(ava, paramAnnMethod.getName(), ov);
							}
							ava.visitEnd();
						}else {
							buildAnnotationAddField(annotationVisitor, paramAnnMethod.getName(), val);
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new IllegalArgumentException(DefaultManager2Controller.class+":deal parameter annotated err:"+e.getMessage());
					} 
				}
		}
		
		/**添加注解字段*/
		private static void buildAnnotationAddField(AnnotationVisitor annotationVisitor,String field, Object val) {
			if (val.getClass().isEnum()) {
				annotationVisitor.visitEnum(field, Type.getDescriptor(val.getClass()), val.toString());
			}else if (val instanceof String && 
					(StringUtils.isBlank((String)val) ||
							StringUtils.equals(ValueConstants.DEFAULT_NONE, (CharSequence) val))) {
				//string 不处理情况
			}else {
				annotationVisitor.visit(field, val);
			}
		}
		
		public final Class<?> defineClazz(String name, byte[] b, int off, int len)
	            throws ClassFormatError
	        {
				try {
					Method cc = ClassLoader.class.getDeclaredMethod("defineClass", String.class,byte[].class,int.class,int.class);
					cc.setAccessible(true);
					return (Class<?>)cc.invoke(getParent(), new Object[]{name,b,off,len});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            return super.defineClass(name, b, off, len, null);
	        }
	}
}
