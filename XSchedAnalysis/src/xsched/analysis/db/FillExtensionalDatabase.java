package xsched.analysis.db;

import java.io.File;
import java.io.IOException;

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.ref.ReferenceCleanser;

public class FillExtensionalDatabase {
	final ExtensionalDatabase database;
	final AnalysisScope scope;
	final ClassHierarchy classHierarchy;
	final AnalysisOptions options = new AnalysisOptions();
	final AnalysisCache cache = new AnalysisCache();
		
	public FillExtensionalDatabase(ExtensionalDatabase database, String scopeString) throws IOException, ClassHierarchyException {
		this.database = database;
		
		File exclude = FileProvider.getFile("xsched/analysis/db/J2SEExclusions.txt");
		scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(scopeString, exclude);
		
		 // build a type hierarchy
	    System.out.print("building class hierarchy...");
	    classHierarchy = ClassHierarchy.make(scope);
	    System.out.println("done");
		    
	    ReferenceCleanser.registerClassHierarchy(classHierarchy);
	    ReferenceCleanser.registerCache(cache);
	    
	    //first, start the domain computation
	    new ComputeDomains(this);
	    
	    database.domainsAreComplete();
	    
	    //next compute the relations
	    new ComputeRelations(this);
	    	    
	    System.out.println("done");
	}
	
}
