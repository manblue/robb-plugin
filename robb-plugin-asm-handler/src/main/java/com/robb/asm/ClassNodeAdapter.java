package com.robb.asm;


import java.util.Iterator;

import org.apache.commons.collections4.CollectionUtils;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class ClassNodeAdapter extends ClassNode {

	public ClassNodeAdapter() {
		super(Opcodes.ASM5);
	}
	
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

}
