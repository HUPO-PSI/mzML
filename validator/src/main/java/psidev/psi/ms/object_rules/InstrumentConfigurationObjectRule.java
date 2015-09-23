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
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration;

/**
 * Checks if the manufacturer and model are in the cv terms of the instrument
 * configuration as well as the customization term value is not empty
 * 
 * @author Salva
 * 
 */
public class InstrumentConfigurationObjectRule extends ObjectRule<InstrumentConfiguration> {

	private static final String INSTRUMENT_MODEL_ACC = "MS:1000031";
	private static final String CUSTOMIZATION_ACC = "MS:1000032";
	private Collection<String> manufacturerAccessions;
	private ArrayList<String> instrumentModelAccessions;
	private int error = -1;
	private Context instrumentConfigurationCvParamContext = new Context(
			MzMLElement.InstrumentConfiguration.getXpath() + "/cvParam");
	private final int MANUFACTURER_ERROR = 2;
	private final int MODEL_ERROR = 1;
	private final int CUSTOMIZATION_ERROR = 3;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public InstrumentConfigurationObjectRule() {
		this(null);

	}

	// Anothe constructor that calls to ObjectRule
	public InstrumentConfigurationObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
		// Get necessary terms from ontology
		getRequiredAccessionsFromOntology();
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof InstrumentConfiguration);
	}

	public Collection<ValidatorMessage> check(InstrumentConfiguration instrumentConfiguration)
			throws ValidatorException {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		// Check instrument manufacturer
		messages.addAll(checkManufacturer(instrumentConfiguration));

		// Check instrument model
		messages.addAll(checkInstrumentModel(instrumentConfiguration));

		// Check customizations
		messages.addAll(checkCustomizations(instrumentConfiguration));

		// return messages
		return messages;
	}

	private Collection<? extends ValidatorMessage> checkCustomizations(
			InstrumentConfiguration instrumentConfiguration) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		final List<CVParam> cvParams = instrumentConfiguration.getCvParam();
		final CVParam customizations = ObjectRuleUtil.checkAccessionsInCVParams(cvParams,
				this.CUSTOMIZATION_ACC);
		if (customizations != null) {
			if (customizations.getValue() == null || "".equals(customizations.getValue()))
				messages.add(new ValidatorMessage("No value in 'customizations' term ("
						+ this.CUSTOMIZATION_ACC + ") for the instrumentConfiguration id="
						+ instrumentConfiguration.getId(), MessageLevel.ERROR,
						instrumentConfigurationCvParamContext, this));
			error = CUSTOMIZATION_ERROR;
		}
		return messages;
	}

	private Collection<? extends ValidatorMessage> checkInstrumentModel(
			InstrumentConfiguration instrumentConfiguration) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		final List<CVParam> cvParams = instrumentConfiguration.getCvParam();
		if (ObjectRuleUtil.checkAccessionsInCVParams(cvParams, instrumentModelAccessions).isEmpty()) {
			messages.add(new ValidatorMessage("No instrument model in instrumentConfiguration id="
					+ instrumentConfiguration.getId(), MessageLevel.ERROR,
					instrumentConfigurationCvParamContext, this));
			error = MODEL_ERROR;
		}
		return messages;
	}

	private Collection<? extends ValidatorMessage> checkManufacturer(
			InstrumentConfiguration instrumentConfiguration) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		final List<CVParam> cvParams = instrumentConfiguration.getCvParam();
		if (ObjectRuleUtil.checkAccessionsInCVParams(cvParams, manufacturerAccessions).isEmpty()) {
			messages.add(new ValidatorMessage(
					"No instrument manufacturer in instrumentConfiguration id="
							+ instrumentConfiguration.getId(), MessageLevel.ERROR,
					instrumentConfigurationCvParamContext, this));
			error = MANUFACTURER_ERROR;
		}
		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		List<String> ret = new ArrayList<String>();
		if (error == MODEL_ERROR) {
			ret.add("Add a grandson of 'instrument model' (" + INSTRUMENT_MODEL_ACC + ") in "
					+ instrumentConfigurationCvParamContext.getContext());
		} else if (error == MANUFACTURER_ERROR) {
			ret.add("Add a direct children of 'instrument model' (" + INSTRUMENT_MODEL_ACC
					+ ") in " + instrumentConfigurationCvParamContext.getContext());
		} else if (error == CUSTOMIZATION_ERROR) {
			ret.add("Add a non empty value in the term 'customizations' (" + CUSTOMIZATION_ACC
					+ ") in " + instrumentConfigurationCvParamContext.getContext());
		}
		return ret;
	}

	private void getRequiredAccessionsFromOntology() {
		final OntologyAccess msOntology = ontologyManager.getOntologyAccess("MS");
		this.manufacturerAccessions = OntologyUtils.getAccessions(msOntology
				.getDirectChildren(new OntologyTermImpl(INSTRUMENT_MODEL_ACC)));
		if (manufacturerAccessions != null) {
			this.instrumentModelAccessions = new ArrayList<String>();
			for (String manufacturerAccession : manufacturerAccessions) {
				instrumentModelAccessions.addAll(OntologyUtils.getAccessions(msOntology
						.getAllChildren(new OntologyTermImpl(manufacturerAccession))));
			}
		}
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

}
