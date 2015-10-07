package psidev.psi.ms.object_rules;

import java.util.ArrayList;
import java.util.Collection;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;

/**
 * This class is for displaying a validator FATAL message when an exception is
 * thrown by the psi xml parser. <br/>
 * This rule is not in the list of object rules to execute.
 * 
 */

public class MandatoryElementsObjectRule extends ObjectRule<Object> {
	public MandatoryElementsObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);

		setName("Mandatory element check");

		setDescription("Check that some elements in the xml file are present or not.");

		addTip("Add the element in the appropiate location.");
	}

	@Override
	public boolean canCheck(Object t) {
		return true;
	}

	@Override
	public Collection<ValidatorMessage> check(Object o) throws ValidatorException {
		return new ArrayList<ValidatorMessage>();
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}
}
