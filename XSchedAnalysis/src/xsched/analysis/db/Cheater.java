package xsched.analysis.db;

import java.io.File;
import java.io.IOException;

import com.ibm.wala.util.io.FileProvider;

public abstract class Cheater {
	protected FillExtensionalDatabase context;
	
	void setContext(FillExtensionalDatabase parent) {
		this.context = parent;
	}
	
	public String exclusionsFile() { return null; }
	
	public File openExclusionsFile() {
		String exclusionsFile = exclusionsFile();
		if(exclusionsFile == null)
			return null;
		
		File exclude;
		try {
			exclude = FileProvider.getFile(exclusionsFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return exclude;
	}
	
	public void cheatBeforeDomainComputation() {}
}
