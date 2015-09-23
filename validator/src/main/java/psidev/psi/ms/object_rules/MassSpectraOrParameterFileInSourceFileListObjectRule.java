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
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.model.mzml.SourceFileList;

/**
 * Check if there is an spectra file or a parameter file in the source file list
 * 
 * @author Salva
 * 
 */
public class MassSpectraOrParameterFileInSourceFileListObjectRule extends
		ObjectRule<SourceFileList> {

	// Contexts
	private static final Context sourceFileContext = new Context(MzMLElement.SourceFile.getXpath());
	private static final String PARAMETER_FILE_ACC = "MS:1000740";
	private static final String MASS_SPECTROMETER_FILE_FORMAT_ACC = "MS:1000560";
	private static final String DATA_FILE_CHECKSUM_TYPE_ACC = "MS:1000561";
	private static final String NATIVE_SPECTRUM_IDENTIFIER_FORMAT = "MS:1000767";
	private Collection<String> dataFileChecksumTypeTerms;
	private Collection<String> massSpectrometryFileFormatTerms;
	private Collection<String> nativeSpectrumIdentifierFormatTerms;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public MassSpectraOrParameterFileInSourceFileListObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public MassSpectraOrParameterFileInSourceFileListObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
		getRequiredAccessionsFromOntology();
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof SourceFileList);
	}

	public Collection<ValidatorMessage> check(SourceFileList sourceFileList)
			throws ValidatorException {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		if (sourceFileList.getSourceFile() != null) {

			for (SourceFile sourceFile : sourceFileList.getSourceFile()) {
				boolean parameterFileError = false;
				boolean massSpectrometerFileTypeFormatError = false;
				boolean nativeSpectrumIdentifierFormatError = false;
				boolean dataFileChecksumTypeError = false;
				// PARAMETER FILE
				if (ObjectRuleUtil.checkAccessionsInCVParams(sourceFile.getCvParam(),
						this.PARAMETER_FILE_ACC) == null)
					parameterFileError = true;
				// MASS SPECTRA FILE FORMAT
				if (ObjectRuleUtil.checkAccessionsInCVParams(sourceFile.getCvParam(),
						this.massSpectrometryFileFormatTerms).isEmpty())
					massSpectrometerFileTypeFormatError = true;
				// DATA FILE CHECKSUM TYPE
				if (ObjectRuleUtil.checkAccessionsInCVParams(sourceFile.getCvParam(),
						this.dataFileChecksumTypeTerms).isEmpty())
					dataFileChecksumTypeError = true;
				// NATIVE SPECTRUM IDENTIFIER FORMAT
				if (ObjectRuleUtil.checkAccessionsInCVParams(sourceFile.getCvParam(),
						this.nativeSpectrumIdentifierFormatTerms).isEmpty())
					nativeSpectrumIdentifierFormatError = true;

				// CREATE MESSAGE ERRORS
				if (parameterFileError
						&& (massSpectrometerFileTypeFormatError || dataFileChecksumTypeError || nativeSpectrumIdentifierFormatError)) {
					// No Paramter file neither any of the mass spectrometer
					// file CV terms
					if (massSpectrometerFileTypeFormatError && dataFileChecksumTypeError
							&& nativeSpectrumIdentifierFormatError) {
						messages.add(new ValidatorMessage("The source file '" + sourceFile.getId()
								+ "' is neither a mass spectrometer file nor a parameter file",
								MessageLevel.ERROR, sourceFileContext, this));
					} else {
						messages.add(getMessage(sourceFile.getId(),
								massSpectrometerFileTypeFormatError, dataFileChecksumTypeError,
								nativeSpectrumIdentifierFormatError));
					}
				}
			}
		}

		return messages;
	}

	private ValidatorMessage getMessage(String sourceFileId,
			boolean massSpectrometerFileTypeFormatError, boolean dataFileChecksumTypeError,
			boolean nativeSpectrumIdentifierFormatError) {
		StringBuilder sb = new StringBuilder();
		if (massSpectrometerFileTypeFormatError)
			sb.append("The term 'mass spectrometer file format' ("
					+ this.MASS_SPECTROMETER_FILE_FORMAT_ACC + ") is not present");
		if (dataFileChecksumTypeError) {
			if (!"".equals(sb.toString()))
				sb.append("\n");
			sb.append("The term 'data file checksum type' (" + this.DATA_FILE_CHECKSUM_TYPE_ACC
					+ ") is not present");
		}
		if (nativeSpectrumIdentifierFormatError) {
			if (!"".equals(sb.toString()))
				sb.append("\n");
			sb.append("The term 'native spectrum identifier format' ("
					+ this.NATIVE_SPECTRUM_IDENTIFIER_FORMAT + ") is not present");
		}
		sb.append("\n in source file id='" + sourceFileId + "'");
		return new ValidatorMessage(sb.toString(), MessageLevel.ERROR, this.sourceFileContext, this);
	}

	@Override
	public Collection<String> getHowToFixTips() {
		return new ArrayList<String>();
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

	private void getRequiredAccessionsFromOntology() {
		final OntologyAccess msOntology = ontologyManager.getOntologyAccess("MS");

		this.dataFileChecksumTypeTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(this.DATA_FILE_CHECKSUM_TYPE_ACC)));

		this.massSpectrometryFileFormatTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(this.MASS_SPECTROMETER_FILE_FORMAT_ACC)));

		this.nativeSpectrumIdentifierFormatTerms = OntologyUtils.getAccessions(msOntology
				.getAllChildren(new OntologyTermImpl(this.NATIVE_SPECTRUM_IDENTIFIER_FORMAT)));
	}
}
