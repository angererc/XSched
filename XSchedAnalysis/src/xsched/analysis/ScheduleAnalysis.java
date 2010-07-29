package xsched.analysis;

import xsched.analysis.db.Cheater;
import xsched.analysis.db.ExtensionalDatabase;
import xsched.analysis.db.FillExtensionalDatabase;

public class ScheduleAnalysis {

	public void runScheduleAnalysis(String scopePath, String outputDirectory, Cheater cheater) {
		
		ExtensionalDatabase database = new ExtensionalDatabase();
		
		try {
			new FillExtensionalDatabase(database, scopePath, cheater);
			database.save(outputDirectory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		//e.g.: "/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class /Users/angererc/Desktop/test"
		ScheduleAnalysis analysis = new ScheduleAnalysis();
		analysis.runScheduleAnalysis(args[0], args[1], new Cheater(){});
	}
}
