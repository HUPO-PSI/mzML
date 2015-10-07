package psidev.psi.ms.object_rules;

import java.util.Collection;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;

/**
 * Created by IntelliJ IDEA. User: javizca Date: 16-jul-2008 Time: 9:37:28 To
 * change this template use File | Settings | File Templates.
 */
public class TemplateObjectRule extends ObjectRule {

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public TemplateObjectRule() {
		this(null);

	}

	// Anothe constructor that calls to ObjectRule
	public TemplateObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return false; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	public Collection<ValidatorMessage> check(Object o) throws ValidatorException {
		return null; // To change body of implemented methods use File |
						// Settings | File Templates.
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}
}
