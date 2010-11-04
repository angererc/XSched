package xsched.analysis.db;

import java.io.File;
import java.io.IOException;

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.ref.ReferenceCleanser;

public class FillExtensionalDatabase {
	final ExtensionalDatabase database;
	final AnalysisScope scope;
	final ClassHierarchy classHierarchy;
	final AnalysisOptions options = new AnalysisOptions();
	final AnalysisCache cache = new AnalysisCache();
	final Cheater cheater;
		
	public FillExtensionalDatabase(ExtensionalDatabase database, String scopeString, Cheater cheater) throws IOException, ClassHierarchyException {
		this.database = database;
		this.cheater = cheater;
		this.cheater.setContext(this);
		
		File exclude = cheater.openExclusionsFile();
		scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(scopeString, exclude);
		
		 // build a type hierarchy
	    System.out.print("building class hierarchy...");
	    classHierarchy = ClassHierarchy.make(scope);
	    System.out.println("done. Got " + classHierarchy.getNumberOfClasses() + " classes");
		    
	    ReferenceCleanser.registerClassHierarchy(classHierarchy);
	    ReferenceCleanser.registerCache(cache);
	    	    
	    //first, start the domain computation
	    new ComputeDomains(this);
	    
	    System.out.println("***************************************************");
	    System.out.println("finished computing domains, computing relations now");
	    System.out.println("***************************************************");
	    database.domainsAreComplete();
	    
	    //next compute the relations
	    new ComputeRelations(this);
	    	    
	    System.out.println("done");
	}
	
	public ExtensionalDatabase database() {
		return database;
	}
	
	public AnalysisScope analysisScope() {
		return scope;
	}
	
	public ClassHierarchy classHierarchy() {
		return classHierarchy;
	}
	
	public AnalysisOptions analysisOptions() {
		return options;
	}
	
	public AnalysisCache analysisCache() {
		return cache;
	}
	
}
