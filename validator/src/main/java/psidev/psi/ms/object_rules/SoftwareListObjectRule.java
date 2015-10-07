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
 * Check if there is an acquisition software and a data processing software in
 * the software list
 * 
 * @author Salva
 * 
 */
public class SoftwareListObjectRule extends ObjectRule<SoftwareList> {

	private static final String ACQUISITON_SOFTWARE_ACC = "MS:1001455";
	private static final String DATA_PROCESSING_SOFTWARE_ACC = "MS:1001457";

	// Contexts
	private static final Context softwareContext = new Context(MzMLElement.Software.getXpath());

	private boolean acquisitionSoftwareError = false;
	private boolean dataProcessingSoftwareError = false;
	private Collection<String> acquisitionSoftwareTerms;
	private Collection<String> dataProcessingSoftwareTerms;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public SoftwareListObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public SoftwareListObjectRule(OntologyManager ontologyManager) {
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
		boolean dataAnalysisSoftware = false;
		boolean acquisitionSoftware = false;

		if (softwareList.getSoftware() != null) {
			for (Software software : softwareList.getSoftware()) {
				if (!acquisitionSoftware)
					if (!ObjectRuleUtil.checkAccessionsInCVParams(software.getCvParam(),
							acquisitionSoftwareTerms).isEmpty())
						acquisitionSoftware = true;
				if (!dataAnalysisSoftware)
					if (!ObjectRuleUtil.checkAccessionsInCVParams(software.getCvParam(),
							dataProcessingSoftwareTerms).isEmpty())
						dataAnalysisSoftware = true;
			}
			if (!acquisitionSoftware) {
				messages.add(new ValidatorMessage(
						"No acquisition software provided in the software list",
						MessageLevel.ERROR, softwareContext, this));
				acquisitionSoftwareError = true;
			}
			if (!dataAnalysisSoftware) {
				messages.add(new ValidatorMessage(
						"No data processing software provided in the software list",
						MessageLevel.ERROR, softwareContext, this));
				dataProcessingSoftwareError = true;
			}
		}

		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		List<String> ret = new ArrayList<String>();
		if (dataProcessingSoftwareError)
			ret.add("Add a children of 'data processing software' (" + DATA_PROCESSING_SOFTWARE_ACC
					+ ") in " + this.softwareContext.getContext());
		if (acquisitionSoftwareError)
			ret.add("Add a children of 'acquisition software' (" + ACQUISITON_SOFTWARE_ACC
					+ ") in " + this.softwareContext.getContext());
		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

	private void getRequiredAccessionsFromOntology() {
		final OntologyAccess msOntology = ontologyManager.getOntologyAccess("MS");
		this.acquisitionSoftwareTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(ACQUISITON_SOFTWARE_ACC)));

		this.dataProcessingSoftwareTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(DATA_PROCESSING_SOFTWARE_ACC)));

	}
}
