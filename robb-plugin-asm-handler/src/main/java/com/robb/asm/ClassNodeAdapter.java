package com.robb.asm;


import java.util.Iterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

//import jdk.internal.org.objectweb.asm.Opcodes;
//import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
//import jdk.internal.org.objectweb.asm.tree.ClassNode;
//import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class ClassNodeAdapter extends ClassNode { 

	public ClassNodeAdapter() {
		super(Opcodes.ASM6);
	}
	
	/**
	 * 移除类注解
	 * @param annotationDesc
	 * */
	public void removeAnnotation(String annotationDesc) {
		if (CollectionUtils.isEmpty(visibleAnnotations)) {
			return;
		}
		
		
		for (Iterator<AnnotationNode> iterator = visibleAnnotations.iterator(); iterator.hasNext();) {
			if (iterator.next().desc.equals(annotationDesc)) {
				iterator.remove();
			}			
		}
	}
	
	/**
	 * 移除方法注解
	 * @param annotationDesc
	 * */
	public void removeMethodAnnotation(String annotationDesc) {
		for (MethodNode methodNode : methods) {
			//移除参数注解
			methodNode.visibleParameterAnnotations = null;

			if (CollectionUtils.isEmpty(methodNode.visibleAnnotations)) {
				continue;
			}
			for (Iterator<AnnotationNode> iterator = methodNode.visibleAnnotations.iterator(); iterator.hasNext();) {
				if (iterator.next().desc.equals(annotationDesc)) {
					iterator.remove();
				}			
			}
		}
	}
	
	/**
	 * 修改类全路径名
	 * @param nClassName
	 * 		eg:java/lang/string
	 * */
	public void changeClassName(String nClassName) {
		name = nClassName;
	}
	
	/**
	 * 修改serverImpl接口名称 及注解service名称
	 * @param oInterfaceName
	 * 		eg:java/lang/string
	 * @param nInterfaceName
	 * 		eg:java/lang/string
	 * */
	public void changeClassInterfaceName(String oInterfaceName,String nInterfaceName) {
		if (CollectionUtils.isEmpty(interfaces)) {
			return;
		}
		int index = 0;
		for (Iterator iterator = interfaces.iterator(); iterator.hasNext();) {
			String attribute = (String) iterator.next();
			if (attribute.equals(oInterfaceName)) {
				iterator.remove();
				break;
			}		
			index++;
		}
		interfaces.add(index, nInterfaceName);
		
		String annoValue = StringUtils.substringAfterLast(nInterfaceName, "/");
		annoValue = annoValue.substring(0, 1).toLowerCase().concat(annoValue.substring(1));
		for (AnnotationNode node : visibleAnnotations) {
			if ("Lorg/springframework/stereotype/Service;".equals(node.desc)) {
				index = 0;
				for (Iterator iterator = node.values.iterator(); iterator.hasNext();) {
					if (index % 2 == 1) {
						continue;
					}
					String attribute = (String) iterator.next();
					if ("value".equals(attribute)) {
						break;
					}
					index++;
				}
				node.values.remove(index+1);
				node.values.add(index+1, annoValue);
			}
		}
	}

}
