package xsched.analysis.utils;

import java.util.Collection;

import com.ibm.wala.classLoader.FieldImpl;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.shrikeCT.AnnotationsReader.UnimplementedException;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.debug.Assertions;

public class AnnotationsUtil {

	public static Collection<Annotation> getAnnotations(IMethod m) {
		if (m instanceof ShrikeCTMethod) {			
			try {
				return ((ShrikeCTMethod) m).getRuntimeVisibleAnnotations();	 //.getRuntimeInvisibleAnnotations();
			} catch (InvalidClassFileException e) {
				e.printStackTrace();
				Assertions.UNREACHABLE();
			} catch (Exception e) {
				e.printStackTrace();
				Assertions.UNREACHABLE();
			}

		}
		return null;
	}
	public static Annotation getAnnotation(IMethod m, TypeName type) {
		Collection<Annotation> annotations = getAnnotations(m);
		if(annotations != null) {
			for (Annotation a : annotations) {
				if (a.getType().getName().equals(type)) {
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Does a particular class have a particular annotation?
	 */
	public static Collection<Annotation> getAnnotations(IClass c) {
		if (c instanceof ShrikeClass) {			 
			try {
				return ((ShrikeClass) c).getRuntimeInvisibleAnnotations();
			} catch (InvalidClassFileException e) {
				e.printStackTrace();
				Assertions.UNREACHABLE();
			} catch (UnimplementedException e) {
				e.printStackTrace();
				Assertions.UNREACHABLE();
			}			 
		}
		return null;
	}

	public static Annotation getAnnotation(IClass c, TypeName type) {
		Collection<Annotation> annotations = getAnnotations(c);
		if(annotations != null) {
			for (Annotation a : annotations) {
				if (a.getType().getName().equals(type)) {
					return a;
				}
			}
		}
		return null;
	}

	public static Collection<Annotation> getAnnotations(IField field) {
		if (field instanceof FieldImpl) {
			return ((FieldImpl)field).getAnnotations();			
		}
		return null;
	}
	
	public static Annotation getAnnotation(IField field, TypeName type) {
		Collection<Annotation> annotations = getAnnotations(field);
		if(annotations != null) {
			for (Annotation a : annotations) {
				if (a.getType().getName().equals(type)) {
					return a;
				}
			}
		}
		return null;
	}
}