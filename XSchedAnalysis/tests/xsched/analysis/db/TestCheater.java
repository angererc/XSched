package xsched.analysis.db;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public class TestCheater extends Cheater {
	@Override
	public String exclusionsFile() {
		return  "xsched/analysis/db/Exclusions.txt";
	}
	
	@Override
	public void cheatBeforeDomainComputation() {
		ExtensionalDatabase database = context.database();
		System.err.println("!!!! Cheating while computing domains!!!");
		database.types.add(TypeReference.JavaLangStringBuilder.getName());
		database.types.add(TypeReference.JavaLangNullPointerException.getName());
		database.types.add(TypeReference.JavaLangString.getName());
		database.types.add(TypeReference.JavaLangClass.getName());
		database.types.add(TypeReference.JavaLangError.getName());
		database.types.add(TypeReference.JavaUtilIterator.getName());
		database.types.add(TypeReference.JavaUtilVector.getName());
		database.types.add(TypeName.findOrCreate("V"));
		database.types.add(TypeName.findOrCreate("Z"));
		database.types.add(TypeName.findOrCreate("I"));
		database.types.add(TypeName.findOrCreate("J"));
		database.types.add(TypeReference.findOrCreateArrayOf(TypeReference.JavaLangString).getName());
		database.types.add(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/IllegalArgumentException").getName());
		//not sure if that's cheating or if it's correct to keep the Activation class out of this
		database.types.add(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Lxsched/Activation").getName());
		
		//make sure we have at least that many params
		for(int i = 0; i < 5; i++) {
			database.paramPositions.add(i);
		}
	}
}
