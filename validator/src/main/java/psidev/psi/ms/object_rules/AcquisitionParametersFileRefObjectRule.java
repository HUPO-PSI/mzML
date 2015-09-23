package psidev.psi.ms.object_rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import uk.ac.ebi.jmzml.MzMLElement;
import uk.ac.ebi.jmzml.model.mzml.ScanSettings;
import uk.ac.ebi.jmzml.model.mzml.SourceFileRef;

/**
 * Check if there is a reference to a source file in the scan settings
 * parameters
 * 
 * @author Salva
 * 
 */
public class AcquisitionParametersFileRefObjectRule extends ObjectRule<ScanSettings> {

	// Contexts
	private static final Context sourceFileRefContext = new Context(
			MzMLElement.SourceFileRef.getXpath());

	private boolean acquisitionParameterFileRefError = false;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public AcquisitionParametersFileRefObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public AcquisitionParametersFileRefObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof ScanSettings);
	}

	public Collection<ValidatorMessage> check(ScanSettings scanSettings) throws ValidatorException {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		boolean acquisitionParameterFileRef = false;

		if (scanSettings.getSourceFileRefList() != null) {
			for (SourceFileRef sourceFileRef : scanSettings.getSourceFileRefList()
					.getSourceFileRef()) {
				if (!acquisitionParameterFileRef) {
					if (!"".equals(sourceFileRef.getRef()))
						acquisitionParameterFileRef = true;
				}

			}
			if (!acquisitionParameterFileRef) {
				messages.add(new ValidatorMessage(
						"There is not a reference to an acquisition parameter file in "
								+ this.sourceFileRefContext.getContext(), MessageLevel.ERROR,
						sourceFileRefContext, this));
				acquisitionParameterFileRefError = true;
			}

		}

		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		List<String> ret = new ArrayList<String>();
		if (acquisitionParameterFileRefError)
			ret.add("Add reference to a source file in the attribute 'ref' in "
					+ this.sourceFileRefContext.getContext());
		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

}
