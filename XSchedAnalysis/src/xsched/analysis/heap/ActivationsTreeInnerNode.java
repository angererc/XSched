package xsched.analysis.heap;

import java.util.ArrayList;
import java.util.List;

public abstract class ActivationsTreeInnerNode<Context> extends ActivationsTree<Context> {
	protected final List<ActivationsTree<Context>> children = new ArrayList<ActivationsTree<Context>>();
	
	protected ActivationsTreeInnerNode(Context context) {
		super(context);
	}
}
