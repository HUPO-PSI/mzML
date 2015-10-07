package psidev.psi.ms;

import java.util.ArrayList;
import java.util.List;

import psidev.psi.tools.validator.Context;

/**
 * A context which cluster the different contexts for a same error message
 * 
 */
public class ClusteredContext extends Context {

	List<Context> contexts = new ArrayList<Context>();

	public ClusteredContext(String context) {
		super(context);
	}

	public ClusteredContext() {
		super(null);
	}

	public List<Context> getContexts() {
		return contexts;
	}

	public int getNumberOfContexts() {
		return contexts.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		if (!contexts.isEmpty()) {
			sb.append(contexts.iterator().next());
			if (contexts.size() > 1)
				sb.append(" in " + getNumberOfContexts() + " locations");
		}

		return sb.toString();
	}
}
