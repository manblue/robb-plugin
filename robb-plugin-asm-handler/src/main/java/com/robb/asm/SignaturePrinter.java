package com.robb.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class SignaturePrinter extends SignatureVisitor {

	public SignaturePrinter(int api) {
		super(Opcodes.ASM7);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SignatureVisitor visitArrayType() {
		// TODO Auto-generated method stub
		return super.visitArrayType();
	}
	
	@Override
	public SignatureVisitor visitParameterType() {
		// TODO Auto-generated method stub
		return super.visitParameterType();
	}
	
	
}
