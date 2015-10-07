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
import uk.ac.ebi.jmzml.model.mzml.ScanSettings;
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.model.mzml.SourceFileList;

/**
 * Check if one source file in the source file list is a parameter file or not
 * 
 * @author Salva
 * 
 */
public class ParameterFileInSourceFileListObjectRule extends ObjectRule<SourceFileList> {

	// Contexts
	private static final Context sourceFileListContext = new Context(
			MzMLElement.SourceFileList.getXpath());

	private static final String PARAMETER_FILE_ACC = "MS:1000740";

	private boolean acquisitionParameterFileError = false;

	// We had a problem with the default constructor. It was necessary to build
	// a new one this way to call the ObjectRule
	// constructor (below):
	public ParameterFileInSourceFileListObjectRule() {
		this(null);
	}

	// Anothe constructor that calls to ObjectRule
	public ParameterFileInSourceFileListObjectRule(OntologyManager ontologyManager) {
		super(ontologyManager);
	}

	// We have to implement the abstract methods of the abstract class
	// ObjectRule.
	public boolean canCheck(Object o) {
		return (o instanceof ScanSettings);
	}

	public Collection<ValidatorMessage> check(SourceFileList sourceFileList)
			throws ValidatorException {
		List<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();

		boolean acquisitionParameterFile = false;

		if (sourceFileList.getSourceFile() != null) {
			for (SourceFile sourceFile : sourceFileList.getSourceFile()) {
				if (!acquisitionParameterFile) {
					if (ObjectRuleUtil.checkAccessionsInCVParams(sourceFile.getCvParam(),
							this.PARAMETER_FILE_ACC) != null)
						acquisitionParameterFile = true;
				}
			}
			if (!acquisitionParameterFile) {
				messages.add(new ValidatorMessage(
						"There is not a parameter file in the source file list in "
								+ this.sourceFileListContext.getContext(), MessageLevel.ERROR,
						sourceFileListContext, this));
				acquisitionParameterFileError = true;
			}

		}

		return messages;
	}

	@Override
	public Collection<String> getHowToFixTips() {
		List<String> ret = new ArrayList<String>();
		if (acquisitionParameterFileError)
			ret.add("Add sourceFile as a acquisition 'parameter file' (" + this.PARAMETER_FILE_ACC
					+ ") in " + this.sourceFileListContext.getContext());
		return ret;
	}

	public String getId() {
		return this.getClass().getSimpleName();
	}

}
