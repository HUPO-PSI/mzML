package psidev.psi.ms.object_rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import psidev.psi.ms.object_rules.util.ObjectRuleUtil;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import uk.ac.ebi.jmzml.MzMLElement;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.ParamGroup;
import uk.ac.ebi.jmzml.model.mzml.Precursor;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;

/**
 * Check if the collision gas CV Term in activation has an empty value. Check if
 * binary data array is empty.
 * 
 * @author Salva
 * 
 */
public class SpectrumObjectRule extends ObjectRule<Spectrum> {

	private static final String COLLISION_GAS_ACC = "MS:1000419";
	private static final String MS_LEVEL_ACC = "MS:1000511";
	private static final String CHARGE_STATE_ACC = "MS:1000041";
	private static final String POSSIBLE_CHARGE_STATE_ACC = "MS:1000633";

	// Contexts
	private static final Context precursorActivationContext = new Context(
			MzMLElement.Precursor.getXpath() + "/activation/cvParam");
	private static final Context binaryDataArrayContext = new Context(
			MzMLElement.Spectrum.getXpath() + "/binaryDataArrayList/binary");
	private static final Context spectrumContext = new Context(MzMLElement.Spectrum.getXpath()
			+ "/cvParam");
	private static final Context selectedIonContext = new Context(
			MzMLElement.SelectedIonList.getXpath() + "/selectedIon/cvParam");
	private static final Context precursorContext = new Context(MzMLElement.Precursor.getXpath());

	private boolean collisionGasEmpty = false;
	private boolean binaryDataArrayEmpty = false;
	private boolean msLevelEmpty = false;
	private boolean possibleChargeStateEmpty = false;
	private boolean chargeStateEmpty = false;
	private boolean precursorEmpty = false;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public SpectrumObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public SpectrumObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof Spectrum);
	}

	public Collection<ValidatorMessage> check(Spectrum spectrum) throws ValidatorException {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		if (spectrum.getPrecursorList() != null) {
			for (Precursor precursor : spectrum.getPrecursorList().getPrecursor()) {
				if (precursor.getActivation() != null) {
					// COLLISION GAS EMPTY
					messages.addAll(checkActivation(precursor.getActivation()));
				}
				// CHARGE STATE or POSSIBLE CHARGE STATE EMPTY
				if (precursor.getSelectedIonList() != null) {
					for (ParamGroup selectedIon : precursor.getSelectedIonList().getSelectedIon()) {
						messages.addAll(checkSelectedIon(selectedIon));
					}
				}
			}
		}
		// BINARY DATA ARRAY EMPTY
		messages.addAll(checkBinaryDataArray(spectrum));

		// MS LEVEL EMPTY
		messages.addAll(checkMSLevel(spectrum));

		// IF MS LEVEL > 2, then check if a precursor is provided
		int msLevel = getMSLevel(spectrum);
		if (msLevel >= 2) {
			messages.addAll(checkPrecursor(spectrum));
		}
		return messages;
	}

	private List<ValidatorMessage> checkPrecursor(Spectrum spectrum) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		boolean spectrumRef = true;
		boolean externalSpectrum = true;
		this.precursorEmpty = false;
		if (spectrum.getPrecursorList() != null) {
			for (Precursor precursor : spectrum.getPrecursorList().getPrecursor()) {
				if (precursor.getSpectrumRef() == null || "".equals(precursor.getSpectrumRef()))
					spectrumRef = false;
				if (precursor.getSourceFileRef() == null || "".equals(precursor.getSourceFileRef())
						|| precursor.getExternalSpectrumID() == null
						|| "".equals(precursor.getExternalSpectrumID()))
					externalSpectrum = false;
				if (!spectrumRef && !externalSpectrum) {
					messages.add(getPrecursorEmptyError(spectrum));
					this.precursorEmpty = true;
				}
			}
		} else {
			messages.add(getPrecursorEmptyError(spectrum));
		}

		return messages;
	}

	private ValidatorMessage getPrecursorEmptyError(Spectrum spectrum) {
		return new ValidatorMessage(
				"There is not a spectrum reference for spectrum (required for MS level >= 2 spectra)",
				MessageLevel.ERROR, this.precursorContext, this);
	}

	private List<ValidatorMessage> checkMSLevel(Spectrum spectrum) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		this.msLevelEmpty = false;
		if (spectrum.getCvParam() != null) {
			final CVParam msLevelCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
					spectrum.getCvParam(), this.MS_LEVEL_ACC);
			if (msLevelCVParam != null) {
				if (msLevelCVParam.getValue() == null || "".equals(msLevelCVParam.getValue())) {
					messages.add(new ValidatorMessage("MS level value is empty",
							MessageLevel.ERROR, this.spectrumContext, this));
					this.msLevelEmpty = true;
				}
			}
		}
		return messages;
	}

	private int getMSLevel(Spectrum spectrum) {
		if (spectrum.getCvParam() != null) {
			final CVParam msLevelCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
					spectrum.getCvParam(), this.MS_LEVEL_ACC);
			if (msLevelCVParam != null) {
				if (msLevelCVParam.getValue() != null && !"".equals(msLevelCVParam.getValue())) {
					try {
						return Integer.valueOf(msLevelCVParam.getValue());
					} catch (Exception e) {
						// do nothing
					}
				}
			}
		}
		return -1;
	}

	private List<ValidatorMessage> checkBinaryDataArray(Spectrum spectrum) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		this.binaryDataArrayEmpty = false;
		if (spectrum.getBinaryDataArrayList() != null)
			for (BinaryDataArray binaryDataArray : spectrum.getBinaryDataArrayList()
					.getBinaryDataArray()) {
				if (binaryDataArray.getBinary().length == 0) {
					messages.add(new ValidatorMessage("Binary data array is empty",
							MessageLevel.ERROR, this.binaryDataArrayContext, this));
					this.binaryDataArrayEmpty = true;
				}
			}
		return messages;
	}

	private List<ValidatorMessage> checkActivation(ParamGroup activation) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		this.collisionGasEmpty = false;
		final CVParam collisionGasCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
				activation.getCvParam(), this.COLLISION_GAS_ACC);
		if (collisionGasCVParam != null) {
			if (collisionGasCVParam.getValue() == null || "".equals(collisionGasCVParam.getValue())) {
				messages.add(new ValidatorMessage("Collision gas value is empty",
						MessageLevel.ERROR, this.precursorActivationContext, this));
				this.collisionGasEmpty = true;
			}
		}
		return messages;
	}

	private List<ValidatorMessage> checkSelectedIon(ParamGroup selectedIon) {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		this.chargeStateEmpty = false;
		this.possibleChargeStateEmpty = false;
		final CVParam chargeStateCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
				selectedIon.getCvParam(), this.CHARGE_STATE_ACC);
		if (chargeStateCVParam != null) {
			if (chargeStateCVParam.getValue() == null || "".equals(chargeStateCVParam.getValue())) {
				messages.add(new ValidatorMessage("Charge state value is empty",
						MessageLevel.ERROR, this.selectedIonContext, this));
				this.chargeStateEmpty = true;
			}
		}
		final CVParam possibleChargeStateCVParam = ObjectRuleUtil.checkAccessionsInCVParams(
				selectedIon.getCvParam(), this.POSSIBLE_CHARGE_STATE_ACC);
		if (possibleChargeStateCVParam != null) {
			if (possibleChargeStateCVParam.getValue() == null
					|| "".equals(possibleChargeStateCVParam.getValue())) {
				messages.add(new ValidatorMessage("Possible charge state value is empty",
						MessageLevel.ERROR, this.selectedIonContext, this));
				this.possibleChargeStateEmpty = true;
			}
		}
		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		List<String> ret = new ArrayList<String>();
		if (collisionGasEmpty)
			ret.add("Add a non empty value in the cvParam 'collision gas' (" + COLLISION_GAS_ACC
					+ ") in " + this.precursorActivationContext.getContext());
		if (binaryDataArrayEmpty)
			ret.add("Add a non empty value in the binaryDataArray in "
					+ this.binaryDataArrayContext.getContext());
		if (msLevelEmpty)
			ret.add("Add a non empty value in the cvParam 'ms level' (" + MS_LEVEL_ACC + ") in "
					+ this.spectrumContext.getContext());
		if (chargeStateEmpty)
			ret.add("Add a non empty value in the cvParam 'charge state' (" + CHARGE_STATE_ACC
					+ ") in " + this.selectedIonContext.getContext());
		if (possibleChargeStateEmpty)
			ret.add("Add a non empty value in the cvParam 'possible charge state' ("
					+ POSSIBLE_CHARGE_STATE_ACC + ") in " + this.selectedIonContext.getContext());
		if (precursorEmpty)
			ret.add("Add the attribute 'spectrumRef' to refer to the precursor spectra in the document."
					+ " It can also be provided by the attribute 'externalSpectrumID' together with 'sourceFileRef' that refer to an external spectrum ID. "
					+ precursorContext.getContext());
		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

}
