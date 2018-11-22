package com.robb.asm;

import com.robb.config.AutoControConfig;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

public class MethodAdapter extends MethodVisitor {


	public MethodAdapter(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		if (AutoControConfig.checkOMethodAnntoFilter(arg0)) {
			return null;
		}
		return super.visitAnnotation(arg0, arg1);
	}
}
