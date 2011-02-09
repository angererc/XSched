package xsched.instrumentation;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import xsched.Task;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class ScheduleSiteRewriter implements ClassFileTransformer {
	
	final ClassPool classPool;
	public ScheduleSiteRewriter() {
		classPool = ClassPool.getDefault();	
	}
	
	private void instrumentMethod(CtMethod method) throws CannotCompileException {
		method.instrument(new ExprEditor() {

			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				if(m.getMethodName().startsWith(Task.NormalTaskMethodPrefix)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String statement = "new xsched.runtime.RTTask(this, \"" + m.getMethodName() + "\", null);";
					System.out.println("found schedule site: " + m + "; replacing it with " + statement);
					m.replace(statement);
				}
			}

			@Override
			public void edit(NewExpr e) throws CannotCompileException {
				// TODO Auto-generated method stub
				super.edit(e);
			}
			
		});
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		System.out.println("about to really instrument class " + className);
		CtClass cc = null;
		
		String javaClassName = className.replace('/', '.');
		
		try {
			cc = classPool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
			System.out.println("did insert class in class pool");
			//cc = classPool.get(className);
			System.out.println("got class from class pool ");
			//Thread.sleep(1000);
			CtMethod[] methods = cc.getMethods();
			for (int k=0; k<methods.length; k++) {				
				if (methods[k].getLongName().startsWith(javaClassName)) {
					System.out.println("instrumenting method " + methods[k]);
					instrumentMethod(methods[k]);					
				} else {
					System.out.println("no body; cannot instrument method " + methods[k].getLongName() + " (class name = " + javaClassName + ")");
				}
			}
			
			// return the new bytecode array:
			byte[] newClassfileBuffer = cc.toBytecode();
			return newClassfileBuffer;
		} catch (CannotCompileException e) {
			System.err.println("cannot compile: " + e.getMessage() + " transforming class " + className + "; returning uninstrumented class");
		} catch (IOException e) {
			System.err.println("IO exception: " + e.getMessage() + " transforming class " + className + "; returning uninstrumented class");
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Exception " + e.getClass() + ": " + e.getMessage() + " transforming class " + className + "; returning uninstrumented class");
		}
		
		return null;
		
	}

}
