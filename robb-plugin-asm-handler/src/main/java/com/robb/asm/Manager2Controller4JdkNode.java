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





import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ValueConstants;

import com.robb.asm.DefaultManager2Controller.InterfaceHandler4Asm;
import com.robb.config.AutoControConfig;

//import jdk.internal.org.objectweb.asm.AnnotationVisitor;
//import jdk.internal.org.objectweb.asm.ClassReader;
//import jdk.internal.org.objectweb.asm.ClassWriter;
//import jdk.internal.org.objectweb.asm.FieldVisitor;
//import jdk.internal.org.objectweb.asm.Label;
//import jdk.internal.org.objectweb.asm.MethodVisitor;
//import jdk.internal.org.objectweb.asm.Opcodes;
//import jdk.internal.org.objectweb.asm.Type;
//import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
//import jdk.internal.org.objectweb.asm.tree.ClassNode;
//import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
//import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class Manager2Controller4JdkNode {

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
	
	private final static Logger LOGGER = LoggerFactory.getLogger(Manager2Controller4JdkNode.class);

	public static Class buildControClass(InputStream is) {
		Class controClass = null;

		try {
			if (AutoControConfig.getBaseController() != null) {
				SUPER_NAME = AutoControConfig.getBaseController().replace('.', '/');
			}
			//遍历class信息
			ClassReader classReader = new ClassReader(is);
			ClassNodeAdapter classNode = new ClassNodeAdapter();
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			
			InterfaceHandler4Asm handler4Asm = new InterfaceHandler4Asm(ClassUtils.getDefaultClassLoader());
			Map<String, Object> outPutParams = new HashMap<String, Object>();
			ClassWriter cw = handler4Asm.buildClassHead(classNode,outPutParams);
			handler4Asm.buildClassField(cw, classNode, outPutParams);
			handler4Asm.buildClassMethod(cw, classNode,  outPutParams);
			cw.visitEnd();
			
			//输出class文件
			byte[] code = cw.toByteArray();
//			FileOutputStream fos = null;
//			String basePath = "D:\\maolong\\DEV\\spring\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\robb-web\\WEB-INF\\classes\\";
//			String outFile = basePath+(String) outPutParams.get(N_CLASS_FULL_NAME)+".class";
//			System.out.println("outFile====="+outFile);
//				fos = new FileOutputStream(outFile);
//				fos.write(code);
//				fos.close();


				controClass = handler4Asm.defineClazz(((String) outPutParams.get(N_CLASS_FULL_NAME)).replace('/', '.'), code, 0, code.length);

				//移除原class部分注解
				for (String  annotationDesc: AutoControConfig.getOClassAnntoFilter()) {
					classNode.removeAnnotation(annotationDesc);
				}
				for (String annotationDesc : AutoControConfig.getOMethodAnntoFilter()) {
					classNode.removeMethodAnnotation(annotationDesc);
				}
				
				ClassApdater classApdater = new ClassApdater();
				classNode.accept(classApdater);
//				classReader.accept(classApdater, ClassReader.EXPAND_FRAMES);
//				code = classApdater.getClassWriter().toByteArray();
//				Class nManagerClass = handler4Asm.defineClazz(classNode.name.replace('/', '.'), code, 0, code.length);
				AutoControConfig.addCache(classNode.name.replace('/', '.'), classApdater.getClassWriter());
				
//				FileOutputStream fos = null;
//				 outFile = basePath+classNode.name.replace('.', '/')+"1.class";
//				LOGGER.info("outFile====={}", outFile);
//					fos = new FileOutputStream(outFile);
//					fos.write(classApdater.getClassWriter().toByteArray());
//					fos.close();
				
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return controClass;
	}
	
	
	
	static class InterfaceHandler4Asm extends ClassLoader implements Opcodes {

		public InterfaceHandler4Asm(ClassLoader parent) {
			super(parent);
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
		
		//拼装字段信息
		public FieldVisitor buildClassField(ClassWriter cw,  ClassNode classNode,Map<String, Object> outPutParams) {
			String oldClassName = (String) outPutParams.get(O_CLASS_NAME);
			String fieldName = new StringBuilder(StringUtils.lowerCase(oldClassName.substring(0, 1))).append(oldClassName.substring(1)).toString();
			String fieldDesc = (String) outPutParams.get(O_CLASS_DESC);
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName,fieldDesc, classNode.signature, null);
			
			fv.visitAnnotation("Lorg/springframework/beans/factory/annotation/Autowired;", true);
			fv.visitEnd();
			return fv;
		}
		
		//拼装方法信息
		public void buildClassMethod(ClassWriter cw, ClassNode classNode, Map<String, Object> outPutParams) {
			String oldClassName = (String) outPutParams.get(O_CLASS_NAME);
			String fieldName = new StringBuilder(StringUtils.lowerCase(oldClassName.substring(0, 1))).append(oldClassName.substring(1)).toString();
			String fieldDesc = (String) outPutParams.get(O_CLASS_DESC);
			String oldClassFullName = (String) outPutParams.get(O_CLASS_FULL_NAME);
			String newClassName = (String) outPutParams.get(N_CLASS_FULL_NAME);
			String nClassDesc = (String) outPutParams.get(N_CLASS_DESC);
			
			for (MethodNode methodNode : classNode.methods) {
				System.out.println("---method.name:"+methodNode.name+"-desc:"+methodNode.desc);
				String mName = methodNode.name;
				if (mName.contains("<") || mName.equals(oldClassName) ||
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
				
				if (CollectionUtils.isEmpty(methodNode.visibleAnnotations)) {
					continue;
				}
				
				//方法头信息
				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + (returnFlag ? ACC_VARARGS:0), 
						mName, 
						mDesc, 
						methodNode.signature, 
						methodNode.exceptions == null?null:methodNode.exceptions.toArray(new String[methodNode.exceptions.size()] ));
				
				//拼装方法注解
				buildMethodAnnotation(mv, methodNode);
				mv.visitCode();
				//亚栈this
				Label startLabel = new Label();//本地变量 作用域-开始
				mv.visitLabel(startLabel);
				mv.visitLineNumber(0, startLabel);
				mv.visitVarInsn(ALOAD, 0);//this
				mv.visitFieldInsn(GETFIELD, newClassName, fieldName, fieldDesc);//fieldName
					//亚栈入参
				Label endLabel = buildMethodVisitorArgs(mv, methodNode, startLabel);
				mv.visitLocalVariable("this",nClassDesc,nClassDesc, startLabel, endLabel, 0);
				//TODO 判断是接口调用还是类调用
				mv.visitMethodInsn(INVOKEVIRTUAL, oldClassFullName, mName, mDesc, false);//mName


				buildParameterAnnotation(mv, methodNode);
				mv.visitInsn(buildMethodReturnCode(methodNode.desc));
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
		private static  List<Integer> buildMethodArgsLoads(String methodArgsDesc) {
			String argsDesc = StringUtils.substringBefore(StringUtils.substringAfter(methodArgsDesc, "("), ")");
			
			String[] args = StringUtils.split(argsDesc,';');
			List<Integer> loadList = new LinkedList<Integer>();
			boolean skip = false;//遇到[后续跳过
			for (int i = 0; i < args.length; i++) {
				for (char ch : args[i].toCharArray()) {
					LOGGER.info("-------------"+ch+"---"+skip);
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
		private static int buildMethodReturnCode(String methodDesc) {
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
		private static  void buildMethodAnnotation(MethodVisitor mv, MethodNode methodNode) {
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
		private static  void buildParameterAnnotation(MethodVisitor mv, MethodNode methodNode) {
			
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
		private static void buildAnnotation4Node(AnnotationVisitor annotationVisitor,AnnotationNode annotationNode) {

			//annotationObj 为代理对象 class com.sun.proxy.$Proxy11
			   //annotationObj.annotationType()为真实注解
			System.out.println("---buildAnnotation4Node:desc:"+annotationNode.desc+"-getClass:"+annotationNode.getClass());
			
		
		}

		private static  Label buildMethodVisitorArgs(MethodVisitor mv,MethodNode methodNode,Label startLable) {
			Label endLable = new Label();//本地变量 作用域-结束
			mv.visitLabel(endLable);
			mv.visitLineNumber(10, endLable);
			String methodArgsDesc = methodNode.desc;
			if (methodArgsDesc.startsWith("()")) {//无参数
				return endLable;
			}
			List<Integer> loadList = buildMethodArgsLoads(methodNode.desc);
			
			int paramNum = methodNode.visibleParameterAnnotations.length;
			List<LocalVariableNode> localVariables = methodNode.localVariables;
			//亚栈入参
			int line = 1;
			int loadNum = 1;
			for (Integer loadOpcode : loadList) {
				System.out.println(loadOpcode+"-----------"+loadNum);
//				if (loadNum == 8) {
//					loadNum++;
//				}
				if (loadNum < 5) {
					line = loadNum + 3;
				}else {
					line = 7 + (loadNum - 4) * 2;
				}
				mv.visitVarInsn(loadOpcode, loadNum);
				loadNum++;
			}
			
			mv.visitLineNumber(line, endLable);
			for (int i = 1; i <= paramNum; i++) {
				LocalVariableNode node = localVariables.get(i);
				System.out.println("--name:"+node.name+"--desc:"+node.desc+"--signature:"+node.signature);

				mv.visitLocalVariable(node.name, node.desc, node.signature, startLable, endLable, i);
			}
			return endLable;
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
