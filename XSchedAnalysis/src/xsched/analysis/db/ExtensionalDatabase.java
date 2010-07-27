package xsched.analysis.db;

import xsched.analysis.bddbddb.Domain;
import xsched.analysis.bddbddb.QuaternaryRelation;
import xsched.analysis.bddbddb.TernaryRelation;

import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.FieldReference;

public class ExtensionalDatabase {

	//TODO: pass some estimations (based on nr. methods etc) so that we can give a rough size for the domains to make it more efficient.
	
	/* **************
	 * Domains
	 */
	public final Domain<SSAInstruction> bytecode = new Domain<SSAInstruction>("BC");
	public final Domain<NewSiteReference> heap = new Domain<NewSiteReference>("H");
	public final Domain<Variable> variable = new Domain<Variable>("V");
	public final Domain<FieldReference> field = new Domain<FieldReference>("F");
	
	/* **************
	 * Relations
	 */
	public TernaryRelation<SSAInstruction, Variable, NewSiteReference> newStatement = 
					new TernaryRelation<SSAInstruction, Variable, NewSiteReference>("newStatement", bytecode, variable, heap, "BC0_V0_H0");
	
	public QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable> load =
					new QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable>("load", bytecode, variable, field, variable, "BC0_V0_F0_V1");
	
	public QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable> store =
		new QuaternaryRelation<SSAInstruction, Variable, FieldReference, Variable>("store", bytecode, variable, field, variable, "BC0_V0_F0_V1");
	
	public void domainsAreComplete() {
		newStatement.zero();
		load.zero();
		store.zero();
	}
}
