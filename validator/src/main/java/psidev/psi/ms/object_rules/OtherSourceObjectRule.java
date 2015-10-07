package psidev.psi.ms.object_rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import psidev.psi.ms.object_rules.util.ObjectRuleUtil;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.OntologyUtils;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import uk.ac.ebi.jmzml.MzMLElement;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.SourceComponent;

/**
 * Check if the source component is any other ionization source different from
 * MALDI or ESI
 * 
 * @author Salva
 * 
 */
public class OtherSourceObjectRule extends ObjectRule<SourceComponent> {

	private static final String ESI_ACC = "MS:1000073";
	private static final String MALDI_ACC = "MS:1000075";
	private Context sourceComponentCvParamContext = new Context(
			MzMLElement.SourceComponent.getXpath() + "/cvParam");
	private Collection<String> esiRelatedTerms;
	final OntologyAccess msOntology = ontologyManager.getOntologyAccess("MS");
	private int error = -1;
	private int ESI_ERROR = 1;
	private int MALDI_ERROR = 2;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public OtherSourceObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public OtherSourceObjectRule(OntologyManager ontologyManager) {
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

		// Check if some ESI related term is in source
		final List<CVParam> esiCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
				sourceComponent.getCvParam(), this.esiRelatedTerms);
		if (esiCVParam != null && !esiCVParam.isEmpty()) {
			messages.add(new ValidatorMessage(esiCVParam.iterator().next().getName() + " ("
					+ esiCVParam.iterator().next().getAccession()
					+ ") term found in source element. Select ESI source instead of OTHER source",
					MessageLevel.ERROR, this.sourceComponentCvParamContext, this));
			this.error = ESI_ERROR;
		}
		// Check if MALDI term is in source
		final CVParam maldiCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
				sourceComponent.getCvParam(), this.MALDI_ACC);
		if (maldiCVParam != null) {
			messages.add(new ValidatorMessage(
					maldiCVParam.getName()
							+ " ("
							+ maldiCVParam.getAccession()
							+ ") term found in source element. Select MALDI source instead of OTHER source",
					MessageLevel.ERROR, this.sourceComponentCvParamContext, this));
			this.error = MALDI_ERROR;
		}

		// return messages"ObjectRules.xml"
		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		Collection<String> ret = new ArrayList<String>();
		if (this.error == this.ESI_ERROR)
			ret.add("Select ESI source and start the validation again");
		else if (this.error == this.MALDI_ERROR)
			ret.add("Select MALDI source and start the validation again");
		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

	private void getRequiredAccessionsFromOntology() {

		this.esiRelatedTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(this.ESI_ACC)));
		this.esiRelatedTerms.add(ESI_ACC);
	}
}
