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

public class ScheduleSiteRewriter implements ClassFileTransformer {
	
	final ClassPool classPool;
	public ScheduleSiteRewriter() {
		classPool = ClassPool.getDefault();	
	}
	
	private void instrumentMethod(final CtMethod method) throws CannotCompileException {
		//there are GeneratedMethodAccessor2.invoke() methods out there (from the use of reflection in Task)
		//that call xschedTask methods
		//and if we rewrite them we get weird behavior...
		if(method.getLongName().startsWith("sun.reflect."))
			return;
		
		method.instrument(new ExprEditor() {

			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				if(m.getMethodName().startsWith(Task.MainTaskMethodPrefix)) {
					String statement = "{ xsched.Runtime.scheduleMainTask($0, \"" + m.getMethodName() + "\", $args); }";							
					System.out.println("found schedule site: " + m.getMethodName() + " in " + method.getLongName() + "; replacing it with " + statement);

					m.replace(statement);
				} else if (m.getMethodName().startsWith(Task.NormalTaskMethodPrefix)) {
					String statement = "{ xsched.Runtime.scheduleNormalTask($0, \"" + m.getMethodName() + "\", $args); }";							
					System.out.println("found schedule site: " + m.getMethodName() + " in  " + method.getLongName() + "; replacing it with " + statement);
					m.replace(statement);
				}
			}
			
		});
		
		
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		//System.out.println("about to really instrument class " + className);
		CtClass cc = null;
		
		String javaClassName = className.replace('/', '.');
		
		try {
			cc = classPool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
			//System.out.println("did insert class in class pool");
			//cc = classPool.get(className);
			//System.out.println("got class from class pool ");
			//Thread.sleep(1000);
			CtMethod[] methods = cc.getMethods();
			for (int k=0; k<methods.length; k++) {				
				if (methods[k].getLongName().startsWith(javaClassName)) {
					//System.out.println("instrumenting method " + methods[k]);
					instrumentMethod(methods[k]);					
				} else {
					//System.out.println("no body; cannot instrument method " + methods[k].getLongName() + " (class name = " + javaClassName + ")");
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
