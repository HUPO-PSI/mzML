package psidev.psi.ms.object_rules;

import java.util.ArrayList;
import java.util.Collection;

import psidev.psi.ms.object_rules.util.ObjectRuleUtil;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import uk.ac.ebi.jmzml.MzMLElement;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.SourceComponent;

/**
 * Check if there is a matrix solution cv term in the source component, and if
 * the value is empty or not.
 * 
 * @author Salva
 * 
 */
public class MatrixSolutionObjectRule extends ObjectRule<SourceComponent> {

	private static final String MATRIX_SOLUTION_ACC = "MS:1000834";
	private Context sourceComponentCvParamContext = new Context(
			MzMLElement.SourceComponent.getXpath() + "/cvParam");

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public MatrixSolutionObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public MatrixSolutionObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof SourceComponent);
	}

	public Collection<ValidatorMessage> check(SourceComponent sourceComponent)
			throws ValidatorException {
		Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		final CVParam matrixSolutionCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
				sourceComponent.getCvParam(), this.MATRIX_SOLUTION_ACC);
		if (matrixSolutionCVParam != null)
			if (matrixSolutionCVParam.getValue() == null
					|| "".equals(matrixSolutionCVParam.getValue()))
				messages.add(new ValidatorMessage("Matrix solution value is empty",
						MessageLevel.ERROR, this.sourceComponentCvParamContext, this));
		// return messages"ObjectRules.xml"
		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		Collection<String> ret = new ArrayList<String>();

		ret.add("Add a non empty value in the cvParam 'matrix solution' (" + MATRIX_SOLUTION_ACC
				+ ") in " + this.sourceComponentCvParamContext.getContext());

		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

}
