package com.robb.asm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.robb.config.AutoControConfig;
import com.robb.config.AutoServerConfig;

//import jdk.internal.org.objectweb.asm.AnnotationVisitor;
//import jdk.internal.org.objectweb.asm.ClassReader;
//import jdk.internal.org.objectweb.asm.ClassWriter;
//import jdk.internal.org.objectweb.asm.FieldVisitor;
//import jdk.internal.org.objectweb.asm.Label;
//import jdk.internal.org.objectweb.asm.MethodVisitor;
//import jdk.internal.org.objectweb.asm.Opcodes;
//import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
//import jdk.internal.org.objectweb.asm.tree.ClassNode;
//import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
//import jdk.internal.org.objectweb.asm.tree.MethodNode;

/**
 * 根据业务serverImpl  class 自动生成给dubbo调用的业务接口层
 * */
public class Server2DubboServer {
	static String SUPER_NAME = "java/lang/Object";
	/**业务server class文件 Signature*/
	final static String SERVER_CLASS_SIGNATURE = "serverClassSignature";
	/**业务server class文件全路径名称 com/ml/example/service/RobbService */
	final static String SERVER_CLASS_FULL_NAME = "serverClassFullName";
	/**业务serverImpl class文件全路径名称*/
	final static String SERVER_IMPL_CLASS_FULL_NAME = "serverImplClassName";
	/**dubbo server clas文件全路径名称 com/ml/example/service/impl/RobbServiceImpl */
	final static String D_SERVER_CLASS_FULL_NAME = "dServerClassFullName";
	/**dubbo serverImpl class文件全路径名称 */
	final static String D_SERVER_IMPL_CLASS_FULL_NAME = "dServerImplClassName";
	/**变量描述 Lcom/robb/manager/RobbManager;*/
	final static String FIELD_DESC = "fieldDesc";
	/**变量名称*/
	final static String FIELD_NAME = "fieldName";
	
	final static String separator = "/";//FileSystems.getDefault().getSeparator();
	private Map<String, Object> paramCache = new HashMap<String, Object>();
	private Map<String, String> doneServerClassCache = new HashMap<String, String>();
	private static Logger logger = LoggerFactory.getLogger(Server2DubboServer.class);
	private static boolean outFile = false;
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
			
//			System.out.println(Server2DubboServer.class.getClassLoader());
//			ClassPrinter printer = ClassPrinter.getNewPrinter(true);
//			classReader.accept(printer, ClassReader.EXPAND_FRAMES);

			AsmHandler handler = new AsmHandler(ClassUtils.getDefaultClassLoader());
			ClassApdater apdater = null;
			byte[] code;
			//dubbo server class 【原业务server class】
			Class dServerClass = handler.getInterfaceClass(serverImplClassNode);
//			Method[] method = dServerClass.getDeclaredMethods();
			paramCache.put(D_SERVER_CLASS_FULL_NAME, dServerClass.getName().replace('.', '/'));
			String serverClassName = doneServerClassCache.get(dServerClass.getName());
			if (StringUtils.isBlank(serverClassName)) {
				
				Resource resource = AutoServerConfig.removeServiceResourceCache("/"+dServerClass.getName().replace(".", "/")+".class");
				InputStream in = resource.getInputStream();
				classReader = new ClassReader(in);
				in.close();
				//业务server
				ClassNodeAdapter serverClassNode = new ClassNodeAdapter();
				classReader.accept(serverClassNode, ClassReader.EXPAND_FRAMES);
				serverClassName = serverClassNode.name.concat("4Busi");
				//修改类全路径名 业务server
				serverClassNode.changeClassName(serverClassName);			
				paramCache.put(SERVER_CLASS_SIGNATURE, serverClassNode.signature);
				doneServerClassCache.put(dServerClass.getName(), serverClassName);

				apdater = new ClassApdater();
				serverClassNode.accept(apdater);
				code = apdater.getClassWriter().toByteArray();
				outFile(serverClassNode.name, code);
				//新业务server class
				Class  serverClass = handler.defineClazz(serverClassNode.name.replace('/', '.'), code, 0, code.length);
//				method = serverClass.getDeclaredMethods();
//				System.out.println(serverClass+"-"+method);
			}
			paramCache.put(SERVER_CLASS_FULL_NAME, serverClassName);
			
			Object object1 = ClassNodeAdapter.class.newInstance();
			
			//dubbo serverImpl class
			dServerImpl = buildDServerImplClass(handler, serverImplClassNode, dServerClass);
//			object1 = dServerImpl.newInstance();
			
//			Method[] methods = dServerImpl.getDeclaredMethods();
			apdater = new ClassApdater();
			//修改业务serverImpl接口名称 及注解名称
			serverImplClassNode.changeClassInterfaceName(dServerClass.getName().replace('.', '/'), serverClassName);
			serverImplClassNode.accept(apdater);
			code = apdater.getClassWriter().toByteArray();
			outFile(serverImplClassNode.name, code);
			//业务serverImpl class
			Class  serverImplClass = handler.defineClazz(serverImplClassNode.name.replace('/', '.'), code, 0, code.length);
//			Method[] methods = serverImplClass.getDeclaredMethods();

//			Object object = serverImplClass.newInstance();
//			System.out.println("object---"+object);
			AutoServerConfig.addServiceImplClassCache(serverImplClassNode.name.replace('/', '.'), serverImplClass);
			
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
	
	private Class buildDServerImplClass(AsmHandler handler,ClassNode serverImplClassNode,Class dServerClass) {
		ClassWriter cw = handler.buildClassHead(serverImplClassNode);
		handler.buildClassField(cw, serverImplClassNode);
		handler.buildClassMethod(cw, serverImplClassNode, dServerClass);
		cw.visitEnd();
		
		//输出class文件
		byte[] code = cw.toByteArray();
		String dServerImplFullName = (String) paramCache.get(D_SERVER_IMPL_CLASS_FULL_NAME);
		outFile(dServerImplFullName, code);
		Class  dServerImplClass = handler.defineClazz(dServerImplFullName.replace('/', '.'), code	, 0, code.length);
		return dServerImplClass;
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
//		String basePath = "D:\\maolong\\DEV\\spring\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\robb-soa\\WEB-INF\\classes\\";
		if (!outFile) {
			return;
		}
		String basePath = Server2DubboServer.class.getResource("/").getPath();
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
	
	
	
	
	
	
	
	
	
	
	/**
	 * 字节码处理类
	 * */
	class AsmHandler extends ClassLoader implements Opcodes{
		
		/**
		 * 获取接口class
		 * @param classNode
		 * 		业务serverImpl
		 * */
		public Class getInterfaceClass(ClassNode classNode) {
			String serverImplName = StringUtils.substringAfterLast(classNode.name, "/");
			String serverName = StringUtils.substringBefore(serverImplName, "Impl");
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
		 * 		业务serverImplClassNode
		 * */
		public ClassWriter buildClassHead(ClassNode serverImplClassNode) {
			//classNode.name eg:com/robb/manager/RobbManager
			String dubboServerImplName = serverImplClassNode.name.concat("4Dubbo");//java.lang.String
			String simpleName =  StringUtils.substringAfterLast(dubboServerImplName, separator);
			
			paramCache.put(SERVER_IMPL_CLASS_FULL_NAME, serverImplClassNode.name);
			paramCache.put(D_SERVER_IMPL_CLASS_FULL_NAME, dubboServerImplName);
//			paramCache.put(N_CLASS_DESC, new StringBuilder("L").append(controName).append(';').toString());
//			paramCache.put(O_CLASS_FULL_NAME, classNode.name);
//			paramCache.put(O_CLASS_NAME, StringUtils.substringAfterLast(classNode.name, separator));
//			paramCache.put(O_CLASS_DESC, new StringBuilder("L").append(classNode.name).append(';').toString());
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			cw.visit(serverImplClassNode.version, 
					serverImplClassNode.access,
					dubboServerImplName, null, SUPER_NAME,
					serverImplClassNode.interfaces.toArray(new String[serverImplClassNode.interfaces.size()]));
//			null);
			//处理复制注解
			if (CollectionUtils.isNotEmpty(serverImplClassNode.visibleAnnotations)) {
				for (AnnotationNode annotationNode : serverImplClassNode.visibleAnnotations) {
					//TODO 注解过滤逻辑
//					if (AutoServerConfig.checkClassAnntoFilter(annotationNode.desc)) {
//						continue;
//					}
					if (annotationNode.desc.equals(Type.getDescriptor(Service.class))) {
//						System.out.println("---annotationNode.desc:"+annotationNode.desc);
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
		
		/**
		 * 拼装字段信息
		 * @param cw
		 * @param classNode
		 * 		业务serverImplClassNode
		 * */
		public FieldVisitor buildClassField(ClassWriter cw,  ClassNode serverImplClassNode) {
			StringBuilder sb = new StringBuilder();
			String serverClassFullName = (String) paramCache.get(SERVER_CLASS_FULL_NAME);
			String serverClassName = StringUtils.substringAfterLast(serverClassFullName, separator);
			String fieldName = sb.append(StringUtils.lowerCase(serverClassName.substring(0, 1))).append(serverClassName.substring(1)).toString();
			paramCache.put(FIELD_NAME, fieldName);
			sb.setLength(0);
			String fieldDesc = sb.append("L").append(serverClassFullName).append(";").toString();
			paramCache.put(FIELD_DESC, fieldDesc);
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName,fieldDesc, (String) paramCache.get(SERVER_CLASS_SIGNATURE), null);
			
			fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true).visitEnd();;
			fv.visitEnd();
			return fv;
		}
		
		/**
		 * 拼装方法信息
		 * @param cw
		 * @param classNode
		 * 		业务serverImplClassNode
		 * 
		 * */
		public void buildClassMethod(ClassWriter cw, ClassNode serverImplClassNode,Class dServerClass) {
			String fieldName = (String) paramCache.get(FIELD_NAME);
			String fieldDesc = (String) paramCache.get(FIELD_DESC);
			String serverClassFullName = (String) paramCache.get(SERVER_CLASS_FULL_NAME);

			String dServerImplClassFullName = (String) paramCache.get(D_SERVER_IMPL_CLASS_FULL_NAME);
//			String dServerImplClassName = StringUtils.substringAfterLast(dServerImplClassFullName, separator);
			
			for (MethodNode methodNode : serverImplClassNode.methods) {
//				System.out.println("---method.name:"+methodNode.name+"-desc:"+methodNode.desc);
				String mName = methodNode.name;
				if (mName.contains("<") || mName.equals(StringUtils.substringAfterLast(serverImplClassNode.name, separator)) ||
						mName.startsWith("set") || 
						mName.equals("get")) {//初始化方法，构造方法，static,setter,getter
					continue;
				}		

				String mDesc = methodNode.desc;
				boolean returnFlag = mDesc.contains(")V") ? false : true;//是否有返回
				if (methodNode.access > ACC_PUBLIC) {//非public,static,接口的方法
					continue;
				}
				
				//方法头信息
				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + (returnFlag ? ACC_VARARGS:0), 
						mName, 
						mDesc, 
						methodNode.signature, 
						methodNode.exceptions == null?null:methodNode.exceptions.toArray(new String[methodNode.exceptions.size()] ));
				
				//拼装方法注解
//				buildMethodAnnotation(mv, methodNode);
				mv.visitCode();
				mv.visitEnd();
				Label startLabel = new Label();//本地变量 作用域-开始
				mv.visitLabel(startLabel);
				mv.visitLineNumber(0, startLabel);
				//亚栈this
				int line = 0;//行号
				mv.visitVarInsn(ALOAD, 0);//this
				line ++;
				mv.visitFieldInsn(GETFIELD, dServerImplClassFullName, fieldName, fieldDesc);//fieldName
				line +=3;
					//亚栈入参
				int paramNum = buildMethodVisitorArgs(mv, methodNode, line);
				//TODO 判断是接口调用还是类调用
				mv.visitMethodInsn(INVOKEINTERFACE, serverClassFullName, mName, mDesc, true);//mName
				line +=5;
				
//				buildParameterAnnotation(mv, methodNode);
				buildMethodLocalVariables(mv, methodNode, paramNum, startLabel,line++);
//				int retCode = buildMethodReturnCode(methodNode.desc);
//				if (retCode == RETURN) {
//					mv.visitInsn(POP);
//				}
				mv.visitInsn(buildMethodReturnCode(methodNode.desc));

				mv.visitMaxs(3, paramNum+1);
				mv.visitEnd();
			}
		}
		
		/**
		 * 方法本地变量表
		 * @param mv
		 * @param methodNode
		 * @param paramNum
		 * @param endLine
		 * */
		private void buildMethodLocalVariables(MethodVisitor mv,MethodNode methodNode,int paramNum,Label startLabel,int endLine) {
			
			Label endLabel = new Label();
			mv.visitLabel(endLabel);
			mv.visitLineNumber(endLine, endLabel);
			String dServerImplClassDesc = new StringBuffer("L").append(paramCache.get(D_SERVER_IMPL_CLASS_FULL_NAME)).append(";").toString();
			mv.visitLocalVariable("this",dServerImplClassDesc,dServerImplClassDesc, startLabel, endLabel, 0);
			
			for (int i = 1; i <= paramNum; i++) {
				LocalVariableNode node = methodNode.localVariables.get(i);
//				System.out.println("--name:"+node.name+"--desc:"+node.desc+"--signature:"+node.signature);

				mv.visitLocalVariable(node.name, node.desc, node.signature, startLabel, endLabel, i);
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
		private   List<Integer> buildMethodArgsLoads(String methodArgsDesc) {
			String argsDesc = StringUtils.substringBefore(StringUtils.substringAfter(methodArgsDesc, "("), ")");
			
			String[] args = StringUtils.split(argsDesc,';');
			List<Integer> loadList = new LinkedList<Integer>();
			boolean skip = false;//遇到[后续跳过
			for (int i = 0; i < args.length; i++) {
				for (char ch : args[i].toCharArray()) {
//					logger.info("-------------"+ch+"---"+skip);
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
		
		
			  /**
			   * 方法返回code
		      * @Description:对象指令对照表</p>
		      Java对象---------------JVM标识-------------------jvm指令</p>
			   boolean----------------Z--------------------------iload</p>
			   char---------------------C--------------------------iload</p>
			   byte---------------------B--------------------------iload</p>
			   short--------------------S--------------------------iload</p>
			   int------------------------I--------------------------iload</p>
			   float---------------------F--------------------------fload</p>
			   long---------------------J---------------------------lload</p>
			   double------------- ----D--------------------------dload</p>
			   Object------------------Ljava/lang/Object;------aload</p>
			   int[]---------------------[I---------------------------aload</p>
			   Object[][]---------------[[Ljava/lang/Object;----aload</p>
		 * @param methodDesc 方法描述
		 * @return  Opcodes.*RETURN 
		 * @author maolong
		 * @date 2018年11月19日下午6:00:19
	     */
		private  int buildMethodReturnCode(String methodDesc) {
			if (methodDesc.contains(")V")) {//无参数
				return Opcodes.RETURN;
			}
			char key = StringUtils.substringAfter(methodDesc, ")").charAt(0);
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
				throw new IllegalArgumentException("deal methodArgsDesc["+methodDesc+"] err:"+key);
			}
		}

		

		/**
		 * 拼装方法注解
		 * */
		private   void buildMethodAnnotation(MethodVisitor mv, MethodNode methodNode) {
			List<AnnotationNode> methodAnnNodes = methodNode.visibleAnnotations;
			if (CollectionUtils.isEmpty(methodAnnNodes)) {
				return;
			}
			
			for (AnnotationNode annotationNode : methodAnnNodes) {
				//TODO 注解过滤
				if (AutoControConfig.checkMethodAnntoFilter(annotationNode.desc)) {
					continue;
				}
				AnnotationVisitor aVisitor = mv.visitAnnotation(annotationNode.desc, true);
				buildAnnotation4Node(aVisitor, annotationNode);
				annotationNode.accept(aVisitor);
				aVisitor.visitEnd();
			}
		}
		
		/**
		 * 拼装参数注解
		 * */
		private   void buildParameterAnnotation(MethodVisitor mv, MethodNode methodNode) {
			
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
					paramVisitor.visitEnd();
				}
				pindex ++;
			}
			
		}
		
		/**
		 * 構建註解對象
		 * */
		private  void buildAnnotation4Node(AnnotationVisitor annotationVisitor,AnnotationNode annotationNode) {

			//annotationObj 为代理对象 class com.sun.proxy.$Proxy11
			   //annotationObj.annotationType()为真实注解
			System.out.println("---buildAnnotation4Node:desc:"+annotationNode.desc+"-getClass:"+annotationNode.getClass());
			
		
		}

		/**
		 * 参数亚栈
		 * @param mv
		 * @param methodNode
		 * @param line 行号
		 * @return int 
		 * 		参数个数
		 * */
		private   int buildMethodVisitorArgs(MethodVisitor mv,MethodNode methodNode,int line) {
			String methodArgsDesc = methodNode.desc;
			if (methodArgsDesc.startsWith("()")) {//无参数
				return 0;
			}
			List<Integer> loadList = buildMethodArgsLoads(methodNode.desc);
			
			int paramNum = loadList.size();
			List<LocalVariableNode> localVariables = methodNode.localVariables;
			//亚栈入参
			int loadNum = 1;
			for (Integer loadOpcode : loadList) {
//				System.out.println(loadOpcode+"-----------"+loadNum);
				mv.visitVarInsn(loadOpcode, loadNum);
				line++;
				loadNum++;
			}

			return paramNum;
		}
		
		public AsmHandler(ClassLoader parent) {
			super(parent);
		}
		
//		@Override
//		public Class<?> findClass(String name) throws ClassNotFoundException {
//			// TODO Auto-generated method stub
//			System.out.println("=========="+name);
//			Class clazz = getParent().findClass(name);
//			return clazz;
//		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			// TODO Auto-generated method stub
//			AutoServerConfig.removeServiceImplClassCache(name);
			Class clazz = null;
			if (name.equals("RobbServiceImpl4Dubbo")) {
				clazz = cache.get("com.robb.service.impl.RobbServiceImpl4Dubbo");
			}else {
				clazz = getParent().loadClass(name);
			}
			return clazz;
		}
		
		public final Class<?> defineClazz(String name, byte[] b, int off, int len)
	            throws ClassFormatError
	        {
				try {
//					Class clazz = this.defineClass(name, b, off, len);
//					cache.put(name, clazz);
//					return clazz;
					Method cc = ClassLoader.class.getDeclaredMethod("defineClass", String.class,byte[].class,int.class,int.class);
					cc.setAccessible(true);
					return (Class<?>)cc.invoke(getParent(), new Object[]{name,b,off,len});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            return null;
	        }
		
		private ConcurrentMap<String, Class> cache = new ConcurrentHashMap<String, Class>();
	}


}
