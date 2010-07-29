package xsched.analysis;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

import xsched.analysis.db.Cheater;
import xsched.analysis.db.ExtensionalDatabase;
import xsched.analysis.db.FillExtensionalDatabase;

public class ScheduleAnalysis {

	public void runScheduleAnalysis(String outputDirectory, Cheater cheater) {
		ExtensionalDatabase database = new ExtensionalDatabase();
		
		try {
			new FillExtensionalDatabase(database, "/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class", cheater);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static class DefaultCheater extends Cheater {
		@Override
		public String exclusionsFile() {
			return  "xsched/analysis/db/J2SEExclusions.txt";
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
	public static void main(String[] args) throws Exception {
		ScheduleAnalysis analysis = new ScheduleAnalysis();
		analysis.runScheduleAnalysis("/Users/angererc/Desktop/test", new DefaultCheater());
	}
}
