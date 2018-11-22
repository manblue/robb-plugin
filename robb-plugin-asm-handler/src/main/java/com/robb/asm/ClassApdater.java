package com.robb.asm;


import com.robb.config.AutoControConfig;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.TypePath;

public class ClassApdater extends ClassVisitor {

	public ClassApdater() {
		super(Opcodes.ASM5, new ClassWriter(ClassWriter.COMPUTE_FRAMES));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		System.out.println("---arg0:"+arg0+"-arg1:"+arg1);
		if(AutoControConfig.checkOClassAnntoFilter(arg0)) {
			return null;
		}
		return super.visitAnnotation(arg0, arg1);
	}
	

	@Override
	public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
			String arg3, String[] arg4) {
		// TODO Auto-generated method stub		
		return super.visitMethod(arg0, arg1, arg2, arg3, arg4);
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int arg0, TypePath arg1,
			String arg2, boolean arg3) {
		System.out.println("---arg0:"+arg0+"-arg1:"+arg1+"-arg2:"+arg2+"-arg3:"+arg3);

		// TODO Auto-generated method stub
		return super.visitTypeAnnotation(arg0, arg1, arg2, arg3);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
	}
		
	public ClassWriter getClassWriter() {
		return (ClassWriter)cv;
	}
	
	
}
