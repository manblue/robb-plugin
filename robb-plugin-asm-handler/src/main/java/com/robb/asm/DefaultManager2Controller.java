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

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.ParameterNode;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ValueConstants;

public class DefaultManager2Controller {

	final static String superName = "java/lang/Object";
	/**接口全路径名称 com/ml/example/service/RobbService */
	final static String kFullInterfaceName = "fullInterfaceName";
	/**接口名称*/
	final static String kInterfaceName = "interfaceName";
	/**实现类IMPL全路径名称 com/ml/example/service/impl/RobbServiceImpl */
	final static String kFullImplName = "fullImplName";
	/**实现类IMPL名称 */
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
			ClassPrinter printer = ClassPrinter.getNewPrinter(true);
//			InputStream is = managerClass.getClassLoader().getSystemResourceAsStream(managerClass.getName().replace(".", "/")+".class");
			ClassReader cReader = new ClassReader(is);
			cReader.accept(printer, 0);			

			ClassNode node = new ClassNode();
			cReader.accept(node, ClassReader.EXPAND_FRAMES);
			//-------------------------------test------------------------------------------
			List<MethodNode> list = node.methods;
			System.out.println("-------------------------name:"+node.name+"-signature:"+node.signature);

			for (MethodNode methodNode : list) {
				List<ParameterNode> pNodes = methodNode.parameters;
				for (LocalVariableNode lvNode : methodNode.localVariables) {
					System.out.println("-methodName:"+methodNode.name+"-index:"+lvNode.index+"-name:"+lvNode.name+"-desc:"+lvNode.desc);
				}
				List<AnnotationNode> list2 = methodNode.visibleAnnotations;
				if (CollectionUtils.isEmpty(list2)) {
					continue;
				}
				for (AnnotationNode annotationNode : list2) {
					System.out.println("---buildAnnotation4Node:desc:"+annotationNode.desc+"-getClass:"+annotationNode.getClass());
				}
				System.out.println("-------------------------");
			}
			//-----------------------------test end----------------------------------------
			Map<String, Object> outPutParams = new HashMap<String, Object>();
			outPutParams.put(kInterfaceName, managerClass.getSimpleName());
			outPutParams.put(kFullInterfaceName, managerClass.getName().replace('.', '/'));
			outPutParams.put(kImplName, managerClass.getSimpleName()+"Impl");
			
			ClassWriter cw = handler4Asm.buildClassHead(managerClass,outPutParams);
//			handler4Asm.buildClassField(cw, managerClass,outPutParams);
//			handler4Asm.buildClassMethod(cw,managerClass, printer.getVisitMethods(),outPutParams);
			
			handler4Asm.buildClassField4Node(cw, managerClass, outPutParams, node);
			handler4Asm.buildClassMethod4Node(cw, managerClass, printer.getVisitMethods(), outPutParams, node);
			
			cw.visitEnd();
			
			//输出class文件
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
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		//拼装class头信息
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
			
			//默认初始化方法
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
				if (mName.contains("<") || mName.equals(clazz.getSimpleName()) ||
						mName.startsWith("set") || 
						mName.equals("get")) {//初始化方法，构造方法，static,setter,getter
					continue;
				}
				
				String mDesc = (String)classMethod.get(ClassPrinter.pdesc);
				boolean returnFlag = mDesc.contains(")V") ? false : true;//是否有返回
				Method method = methodMap.get(mName+":"+mDesc);
				if (!Modifier.isPublic(method.getModifiers()) ||
						Modifier.isStatic(method.getModifiers())) {//非public,static
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
				//亚栈this
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitVarInsn(ALOAD, 0);//this
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLocalVariable("this", Type.getDescriptor(clazz), Type.getDescriptor(clazz), l0, l1, 0);
//				if (mName.startsWith("set")) {
//					mv.visitVarInsn(ALOAD, 1);
//					mv.visitFieldInsn(PUTFIELD, fullImplName, fieldName, fieldDesc);
//				}else {
					mv.visitFieldInsn(GETFIELD, fullImplName, fieldName, fieldDesc);//fieldName
					//亚栈入参
					buildMethodVisitorArgs(mv, mDesc);
					mv.visitMethodInsn(INVOKEINTERFACE, fullInterfaceName, mName, mDesc, true);//mName
//				}

//				mv.visitVarInsn(ALOAD, 1);//���1
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
				mv.visitInsn(buildMethodReturnCode(Type.getMethodDescriptor(method)));
				mv.visitMaxs(3, 1);
				mv.visitEnd();
			}
		}
		
		  /**
		   * 
		   Java对象---------------JVM标识------------------jvm指令</p>
		   boolean----------------Z--------------------------iload</p>
		   char---------------------C--------------------------iload</p>
		   byte---------------------B--------------------------iload</p>
		   short--------------------S--------------------------iload</p>
		   int------------------------I--------------------------iload</p>
		   float---------------------F--------------------------fload</p>
		   long---------------------J--------------------------lload</p>
		   double------------- ----D-------------------------dload</p>
		   Object-----------------Ljava/lang/Object;------aload</p>
		   int[]----------------------[I------------------------  -aload</p>
		   Object[][]-----------------[[Ljava/lang/Object;----aload</p>
       */
		private static  void buildMethodVisitorArgs(MethodVisitor mv,String methodArgsDesc) {
//			methodArgsDesc = Type.getMethodDescriptor(method);
			if (methodArgsDesc.startsWith("()")) {//无参数
				return ;
			}

			List<Integer> loadList = buildMethodArgsLoads(methodArgsDesc);
			
			//亚栈入参
			int loadNum = 1;
			Label bL = new Label();
			Label eL = null;
			for (Integer loadOpcode : loadList) {
				System.out.println(loadOpcode+"-----------"+loadNum);
//				if (loadNum == 8) {
//					loadNum++;
//				}
				mv.visitLabel(bL);
				mv.visitVarInsn(loadOpcode, loadNum++);
				eL = new Label();
				mv.visitLabel(eL);
//				mv.visitLocalVariable(arg0, arg1, arg2, bL, eL, loadNum);
				bL = eL;
			}
			
		}
		
		  /**
		   * 
		   Java对象---------------JVM标识------------------jvm指令</p>
		   boolean----------------Z--------------------------iload</p>
		   char---------------------C--------------------------iload</p>
		   byte---------------------B--------------------------iload</p>
		   short--------------------S--------------------------iload</p>
		   int------------------------I--------------------------iload</p>
		   float---------------------F--------------------------fload</p>
		   long---------------------J--------------------------lload</p>
		   double------------- ----D-------------------------dload</p>
		   Object-----------------Ljava/lang/Object;------aload</p>
		   int[]----------------------[I------------------------  -aload</p>
		   Object[][]-----------------[[Ljava/lang/Object;----aload</p>
     */
		private static  List<Integer> buildMethodArgsLoads(String methodArgsDesc) {
			String argsDesc = StringUtils.substringBefore(StringUtils.substringAfter(methodArgsDesc, "("), ")");
			
			String[] args = StringUtils.split(argsDesc,';');
			List<Integer> loadList = new LinkedList<Integer>();
			boolean skip = false;//遇到[后续跳过
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
			return loadList;
		}
		//方法返回code
		private static int buildMethodReturnCode(String methodArgsDesc) {
//			String methodArgsDesc = Type.getMethodDescriptor(method);
			if (methodArgsDesc.contains(")V")) {//无参数
				return Opcodes.RETURN;
			}
			char key = StringUtils.substringAfter(methodArgsDesc, ")").charAt(0);
			switch (key) {
			case 'Z':
			case 'C':
			case 'B':
			case 'S':
			case 'I':
				return Opcodes.IRETURN;
			case 'F':
				return Opcodes.FRETURN;
			case 'J':
				return Opcodes.LRETURN;
			case 'D':
				return Opcodes.DRETURN;
			case 'L':
			case '[':
				return Opcodes.ARETURN;
			default:
				throw new IllegalArgumentException("deal methodArgsDesc["+methodArgsDesc+"] err:"+key);
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
				//annotationObj 为代理对象 class com.sun.proxy.$Proxy11
				   //annotationObj.annotationType()为真实注解
					Class orgAnnotation = methodAnnotation.annotationType();
					AnnotationVisitor aVisitor = mv.visitAnnotation(Type.getDescriptor(orgAnnotation), true);
					buildAnnotation(aVisitor, methodAnnotation);
					aVisitor.visitEnd();
			}
		}
		/**
		 * 拼装参数注解
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
				//参数名称
				//mv.visitParameter(parameter.getName(), Opcodes.ACC_MANDATED);
				
				Annotation[] paramAnnotations = parameter.getAnnotations();
				if (ArrayUtils.isNotEmpty(paramAnnotations)) {
					for (Annotation paramAnnotation : paramAnnotations) {
						//annotationObj 为代理对象 class com.sun.proxy.$Proxy11
						   //annotationObj.annotationType()为真实注解
						Class orgAnnotation = paramAnnotation.annotationType();
						AnnotationVisitor paramVisitor = mv.visitParameterAnnotation(pindex, Type.getDescriptor(orgAnnotation), true);
						buildAnnotation(paramVisitor, paramAnnotation);
						paramVisitor.visitEnd();
					}
				}
			}
		}
		/**
		 * 構建註解對象
		 * */
		private static void buildAnnotation(AnnotationVisitor annotationVisitor,Annotation annotationObj) {

			//annotationObj 为代理对象 class com.sun.proxy.$Proxy11
			   //annotationObj.annotationType()为真实注解
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
		
		/**添加註解字段*/
		private static void buildAnnotationAddField(AnnotationVisitor annotationVisitor,String field, Object val) {
			if (val.getClass().isEnum()) {
				annotationVisitor.visitEnum(field, Type.getDescriptor(val.getClass()), val.toString());
			}else if (val instanceof String && 
					(StringUtils.isBlank((String)val) ||
							StringUtils.equals(ValueConstants.DEFAULT_NONE, (CharSequence) val))) {
				//string 為空不處理
			}else {
				annotationVisitor.visit(field, val);
			}
		}
		
		//拼装字段信息
		public FieldVisitor buildClassField4Node(ClassWriter cw,Class clazz, Map<String, Object> outPutParams,  ClassNode classNode) {
			String sname = StringUtils.substringAfterLast(classNode.name, separator);
			String fieldName = StringUtils.lowerCase(StringUtils.substring(sname, 0, 1))+StringUtils.substring(sname, 1);
			String fieldDesc = "L"+classNode.name+";";
			outPutParams.put(kFiedlName, fieldName);
			outPutParams.put(kFieldDesc, fieldDesc);
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName,fieldDesc, classNode.signature, null);
			
			fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true);
			fv.visitEnd();
			return fv;
		}
		
		//拼装方法信息
		public void buildClassMethod4Node(ClassWriter cw, Class clazz,List<Map<String, Object>> classMethods, Map<String, Object> outPutParams,ClassNode classNode) {
			String fieldName = (String) outPutParams.get(kFiedlName);
			String fieldDesc = (String) outPutParams.get(kFieldDesc);
			String fullInterfaceName = (String) outPutParams.get(kFullInterfaceName);
			String fullImplName = (String) outPutParams.get(kFullImplName);
			for (MethodNode methodNode : classNode.methods) {
				System.out.println("---method.name:"+methodNode.name+"-desc:"+methodNode.desc);
				String mName = methodNode.name;
				if (mName.contains("<") || mName.equals(classNode.name) ||
						mName.startsWith("set") || 
						mName.equals("get")) {//初始化方法，构造方法，static,setter,getter
					continue;
				}		
				
				String mDesc = methodNode.desc;
				boolean returnFlag = mDesc.contains(")V") ? false : true;//是否有返回
				if (!Modifier.isPublic(methodNode.access) ||
						Modifier.isStatic(methodNode.access)) {//非public,static
					continue;
				}
				
				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + (returnFlag ? ACC_VARARGS:0), 
						mName, 
						mDesc, 
						methodNode.signature, 
						methodNode.exceptions == null?null:methodNode.exceptions.toArray(new String[methodNode.exceptions.size()] ));
				
				//TODO
				buildMethodAnnotation4Node(mv, methodNode);
				mv.visitCode();
				//亚栈this
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(0, l0);
				mv.visitVarInsn(ALOAD, 0);//this
				mv.visitFieldInsn(GETFIELD, fullImplName, fieldName, "L"+classNode.name+";");//fieldName
					//亚栈入参
				//buildMethodVisitorArgs(mv, mDesc);
				Label l1 = buildMethodVisitorArgs4Node(mv, methodNode, l0);
				mv.visitLocalVariable("this",Type.getDescriptor(clazz) , Type.getDescriptor(clazz), l0, l1, 0);
				mv.visitMethodInsn(INVOKEINTERFACE, fullInterfaceName, mName, mDesc, true);//mName


				buildParameterAnnotation4Node(mv, methodNode);
				mv.visitInsn(buildMethodReturnCode(methodNode.desc));
				mv.visitMaxs(3, 1);
				mv.visitEnd();
			}
		}
		
		/**
		 * 拼装方法注解
		 * */
		private static  void buildMethodAnnotation4Node(MethodVisitor mv, MethodNode methodNode) {
			List<AnnotationNode> methodAnnNodes = methodNode.visibleAnnotations;
			if (CollectionUtils.isEmpty(methodAnnNodes)) {
				return;
			}
			
			for (AnnotationNode annotationNode : methodAnnNodes) {
				AnnotationVisitor aVisitor = mv.visitAnnotation(annotationNode.desc, true);
				buildAnnotation4Node(aVisitor, annotationNode);
				annotationNode.accept(aVisitor);
				aVisitor.visitEnd();
			}
		}
		
		/**
		 * 拼装参数注解
		 * */
		private static  void buildParameterAnnotation4Node(MethodVisitor mv, MethodNode methodNode) {
			
			//methodNode.parameters 不可用
//			List<ParameterNode> parameters = methodNode.parameters;
//			if (CollectionUtils.isEmpty(parameters)) {
//				return ;
//			}
			
			List<AnnotationNode>[] parameterAnnotations = methodNode.visibleParameterAnnotations;
			if (ArrayUtils.isEmpty(parameterAnnotations)) {
				return ;
			}
			
			int pindex = 0;
			for (List<AnnotationNode> list : parameterAnnotations) {
				if (CollectionUtils.isEmpty(list)) {
					continue;
				}
				for (AnnotationNode annotationNode : list) {
					AnnotationVisitor paramVisitor = mv.visitParameterAnnotation(pindex, annotationNode.desc, true);
					annotationNode.accept(paramVisitor);
//					buildAnnotation(paramVisitor, paramAnnotation);
					paramVisitor.visitEnd();
				}
				pindex ++;
			}
			
		}
		
		/**
		 * 構建註解對象
		 * */
		private static void buildAnnotation4Node(AnnotationVisitor annotationVisitor,AnnotationNode annotationNode) {

			//annotationObj 为代理对象 class com.sun.proxy.$Proxy11
			   //annotationObj.annotationType()为真实注解
			System.out.println("---buildAnnotation4Node:desc:"+annotationNode.desc+"-getClass:"+annotationNode.getClass());
			
		
		}

		private static  Label buildMethodVisitorArgs4Node(MethodVisitor mv,MethodNode methodNode,Label lable) {
//			buildMethodVisitorArgs(mv, methodNode.desc);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(10, l1);
			String methodArgsDesc = methodNode.desc;
			if (methodArgsDesc.startsWith("()")) {//无参数
				return l1;
			}
			List<Integer> loadList = buildMethodArgsLoads(methodNode.desc);
			
			int paramNum = methodNode.visibleParameterAnnotations.length;
			List<LocalVariableNode> localVariables = methodNode.localVariables;
			//亚栈入参
			int line = 1;
			int loadNum = 1;
//			Label bL = new Label();
//			Label eL = null;
			for (Integer loadOpcode : loadList) {
				System.out.println(loadOpcode+"-----------"+loadNum);
//				if (loadNum == 8) {
//					loadNum++;
//				}
//				mv.visitLabel(bL);
				if (loadNum < 5) {
					line = loadNum + 3;
				}else {
					line = 7 + (loadNum - 4) * 2;
				}
//				mv.visitLineNumber(line, bL);
				mv.visitVarInsn(loadOpcode, loadNum);
//				eL = new Label();
//				mv.visitLabel(eL);
//				LocalVariableNode node = localVariables.get(loadNum);
//				System.out.println("--name:"+node.name+"--desc:"+node.desc+"--signature:"+node.signature);
//
//				mv.visitLocalVariable(node.name, node.desc, node.signature, bL, eL, loadNum);
//				bL = eL;
				loadNum++;
			}
			

//			mv.visitLocalVariable("this",Type.getDescriptor(clazz) , Type.getDescriptor(clazz), l0, l1, 0);
			for (int i = 1; i <= paramNum; i++) {
				LocalVariableNode node = localVariables.get(i);
				System.out.println("--name:"+node.name+"--desc:"+node.desc+"--signature:"+node.signature);

				mv.visitLocalVariable(node.name, node.desc, node.signature, lable, l1, i);
			}
			return l1;
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
