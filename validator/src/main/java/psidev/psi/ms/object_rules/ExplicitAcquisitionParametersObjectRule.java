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
import uk.ac.ebi.jmzml.model.mzml.Software;
import uk.ac.ebi.jmzml.model.mzml.SoftwareList;

/**
 * Check if the acquisition software has an explicit description of the
 * parameters
 * 
 * @author Salva
 * 
 */
public class ExplicitAcquisitionParametersObjectRule extends ObjectRule<SoftwareList> {

	private static final String ACQUISITON_SOFTWARE_ACC = "MS:1001455";
	// TODO change the DATA_PROCESSING_PARAMETER_ACC by the new term for
	// "acquisition parameters"
	private static final String DATA_PROCESSING_PARAMETER_ACC = "MS:1000630";

	// Contexts
	private static final Context softwareContext = new Context(MzMLElement.Software.getXpath());

	private boolean acquisitionSoftwareError = false;
	private Collection<String> acquisitionSoftwareTerms;
	private Collection<String> acquisitionParametersTerms;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public ExplicitAcquisitionParametersObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public ExplicitAcquisitionParametersObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
		// Get necessary terms from ontology
		getRequiredAccessionsFromOntology();
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof SoftwareList);
	}

	public Collection<ValidatorMessage> check(SoftwareList softwareList) throws ValidatorException {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		boolean acquisitionSoftwareWithParameters = false;

		if (softwareList.getSoftware() != null) {
			for (Software software : softwareList.getSoftware()) {
				if (!acquisitionSoftwareWithParameters) {
					if (!ObjectRuleUtil.checkAccessionsInCVParams(software.getCvParam(),
							acquisitionSoftwareTerms).isEmpty())
						if (!ObjectRuleUtil.checkAccessionsInCVParams(software.getCvParam(),
								acquisitionParametersTerms).isEmpty())
							acquisitionSoftwareWithParameters = true;
				}

			}
			if (!acquisitionSoftwareWithParameters) {
				messages.add(new ValidatorMessage(
						"No explicit description of acquisition parameters have been provided in the acquisition software",
						MessageLevel.ERROR, softwareContext, this));
				acquisitionSoftwareError = true;
			}

		}

		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		List<String> ret = new ArrayList<String>();
		if (acquisitionSoftwareError)
			ret.add("Add a children of 'data processing parameter' ("
					+ DATA_PROCESSING_PARAMETER_ACC + ") in the acquisition software provided in "
					+ this.softwareContext.getContext());
		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

	private void getRequiredAccessionsFromOntology() {
		final OntologyAccess msOntology = ontologyManager.getOntologyAccess("MS");

		this.acquisitionSoftwareTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(ACQUISITON_SOFTWARE_ACC)));

		this.acquisitionParametersTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(DATA_PROCESSING_PARAMETER_ACC)));
		this.acquisitionParametersTerms.add(DATA_PROCESSING_PARAMETER_ACC);
	}
}
