	package xsched.analysis.db;

import java.lang.reflect.Field;
import java.util.ArrayList;

import xsched.analysis.bddbddb.BinaryRelation;
import xsched.analysis.bddbddb.Domain;
import xsched.analysis.bddbddb.QuaternaryRelation;
import xsched.analysis.bddbddb.Relation;
import xsched.analysis.bddbddb.TernaryRelation;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;

public class ExtensionalDatabase {

	//TODO: pass some estimations (based on nr. methods etc) so that we can give a rough size for the domains to make it more efficient.
	
	/* **************
	 * Domains
	 */
	public final Domain<SSAInstruction> bytecodes = new Domain<SSAInstruction>("Bytecode");
	public final Domain<ObjectCreationSite> objects = new Domain<ObjectCreationSite>("Object");
	public final Domain<Variable> variables = new Domain<Variable>("Variable");
	public final Domain<FieldReference> fields = new Domain<FieldReference>("Field");
	//TODO we don't use TypeReference because that defines a type as a tuple of class loader and type
	//but class loaders are hierarchical which we cannot handle in this flat type domain.
	//one possibility (the more correct one) would be to flatten the types by adding a type for each class loader
	//(e.g., <Application, Ljava/lang/Object> and <Primordial, Ljava/lang/Object>)
	//or we just use the name and don't handle all this class loader craziness correctly.
	public final Domain<TypeName> types = new Domain<TypeName>("Type");  
	public final Domain<IMethod> methods = new Domain<IMethod>("Method");
	public final Domain<Selector> selectors = new Domain<Selector>("Selector");
	public final Domain<Integer> paramPositions = new Domain<Integer>("ParamPosition");
	
	/* **************
	 * special domain elements
	 */
	public FieldReference arrayElementField = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "gen_array", "element", "Ljava/lang/Object");
	
	public Variable theGlobalObjectRef = new Variable("TheGlobalObjectRef", 0);
	
	public ObjectCreationSite theImmutableStringObject = new ObjectCreationSite.SpecialCreationSite("The Immutable String Object");
	public ObjectCreationSite theGlobalObject = new ObjectCreationSite.SpecialCreationSite("The Global Object");
	
	/* **************
	 * Relations
	 */
	public BinaryRelation<Variable, ObjectCreationSite> assignObject = 
		new BinaryRelation<Variable, ObjectCreationSite>("assignObject", variables, objects, "Variable0_Object0");
	
	//(base, field, dest) => dest = base.field
	public QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable> load =
		new QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable>("load", bytecodes, variables, fields, variables, "Bytecode0_Variable0_Field0_Variable1");
	
	//load of a primitive field; we still want to know about the access but we don't want to know the value
	public TernaryRelation<SSAInstruction, Variable, FieldReference> primLoad =
		new TernaryRelation<SSAInstruction, Variable, FieldReference>("primLoad", bytecodes, variables, fields, "Bytecode0_Variable0_Field0");
	
	//(base, field, source) => base.field = source
	public QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable> store =
		new QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable>("store", bytecodes, variables, fields, variables, "Bytecode0_Variable0_Field0_Variable1");
	
	//(base, field, source) => base.field = source
	public TernaryRelation<SSAInstruction, Variable, FieldReference> primStore =
		new TernaryRelation<SSAInstruction, Variable, FieldReference>("primStore", bytecodes, variables, fields, "Bytecode0_Variable0_Field0");
	
	public BinaryRelation<Variable, Variable> assigns0 =
		new BinaryRelation<Variable, Variable>("assign0", variables, variables, "Variable0_Variable1");
	
	public BinaryRelation<Variable, TypeName> variableType =
		new BinaryRelation<Variable, TypeName>("variableType", variables, types, "Variable0_Type0");
	
	public BinaryRelation<ObjectCreationSite, TypeName> objectType =
		new BinaryRelation<ObjectCreationSite, TypeName>("objectType", objects, types, "Object0_Type0");
	
	public BinaryRelation<TypeName, TypeName> assignable =
		new BinaryRelation<TypeName, TypeName>("assignable", types, types, "Type0_Type1");
	
	public TernaryRelation<TypeName, Selector, IMethod> methodImplementation =
		new TernaryRelation<TypeName, Selector, IMethod>("methodImplementation", types, selectors, methods, "Type0_Selector0_Method0");
	
	public TernaryRelation<IMethod, Integer, Variable> formals =
		new TernaryRelation<IMethod, Integer, Variable>("formal", methods, paramPositions, variables, "Method0_ParamPosition0_Variable0");
	
	public BinaryRelation<IMethod, Variable> methodReturns =
		new BinaryRelation<IMethod, Variable>("methodReturn", methods, variables, "Method0_Variable0");
	
	public TernaryRelation<SSAInstruction, Integer, Variable> actuals =
		new TernaryRelation<SSAInstruction, Integer, Variable>("actual", bytecodes, paramPositions, variables, "Bytecode0_ParamPosition0_Variable0");
	
	public BinaryRelation<SSAInstruction, Variable> callSiteReturns =
		new BinaryRelation<SSAInstruction, Variable>("callSiteReturn", bytecodes, variables, "Bytecode0_Variable0");
	
	public BinaryRelation<SSAInstruction, IMethod> staticInvokes =
		new BinaryRelation<SSAInstruction, IMethod>("staticInvoke", bytecodes, methods, "Bytecode0_Method0");
	
	public BinaryRelation<SSAInstruction, Selector> methodInvokes =
		new BinaryRelation<SSAInstruction, Selector>("methodInvoke", bytecodes, selectors, "Bytecode0_Selector0");
	
	/* *******************
	 * Methods
	 */
	
	public void domainsAreComplete() {
		for(Relation<?> relation : relations()) {
			relation.zero();
		}	
	}
	
	public void save(String dirName) {
		for(Domain<?> domain : domains()) {
			domain.save(dirName, true);
		}
		
		for(Relation<?> relation : relations()) {
			relation.save(dirName);
		}
		
	}
	
	private ArrayList<Domain<?>> domains() {
		ArrayList<Domain<?>> result = new ArrayList<Domain<?>>();
		for(Field f : this.getClass().getFields()) {
			if(Domain.class.isAssignableFrom(f.getType())) {
				try {
					result.add((Domain<?>) f.get(this));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	private ArrayList<Relation<?>> relations() {
		ArrayList<Relation<?>> result = new ArrayList<Relation<?>>();
		for(Field f : this.getClass().getFields()) {
			if(Relation.class.isAssignableFrom(f.getType())) {
				try {
					result.add((Relation<?>) f.get(this));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
}
