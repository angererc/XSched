package xsched;

class AnalysisScaffoldingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4132025783385362055L;

	AnalysisScaffoldingException() {
		super("This class is used during analysis and is not meant to be called at runtime!");
	}
}
