package xsched.analysis;

import xsched.analysis.db.ExtensionalDatabase;
import xsched.analysis.db.FillExtensionalDatabase;

public class ScheduleAnalysis {

	public static void main(String[] args) throws Exception {
		ExtensionalDatabase database = new ExtensionalDatabase();
		
		new FillExtensionalDatabase(database, "/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class");
		
		database.save("/Users/angererc/Desktop/test");
	}
}
