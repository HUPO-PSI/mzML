/**
 * Created by IntelliJ IDEA. User: martlenn Date: 15-Aug-2007 Time: 15:29:31
 */
package psidev.psi.ms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.SAXException;

import psidev.psi.ms.object_rules.MandatoryElementsObjectRule;
import psidev.psi.ms.rulefilter.RuleFilterManager;
import psidev.psi.tools.cvrReader.CvRuleReaderException;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.Validator;
import psidev.psi.tools.validator.ValidatorCvContext;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.Rule;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRule;
import uk.ac.ebi.jmzml.MzMLElement;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.DataProcessingList;
import uk.ac.ebi.jmzml.model.mzml.FileDescription;
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration;
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfigurationList;
import uk.ac.ebi.jmzml.model.mzml.MzMLObject;
import uk.ac.ebi.jmzml.model.mzml.SampleList;
import uk.ac.ebi.jmzml.model.mzml.ScanSettings;
import uk.ac.ebi.jmzml.model.mzml.Software;
import uk.ac.ebi.jmzml.model.mzml.SoftwareList;
import uk.ac.ebi.jmzml.model.mzml.SourceComponent;
import uk.ac.ebi.jmzml.model.mzml.SourceFileList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

/**
 * This class represents the PSI mzML semantic validator.
 * 
 * @author Lennart Martens, modified by Juan Antonio Vizcaino and Salvador
 *         Martínez
 * @version $Id$
 */
public class MzMLValidator extends Validator {
	// 10 steps: schema validation, reading cv-rules, fileDescription,
	// sampleList, instrumentConfigurationList, scanSettingsList,
	// software,
	// dataProcessingList, chromatogram, spectrum, complete
	private static final int PROGRESS_STEPS = 16;
	// private MzMLValidatorGUI gui = null;
	private MzMLValidatorGUI gui = null;
	private int progress = 0;
	private MessageLevel msgL = MessageLevel.WARN;
	private MzMLUnmarshaller unmarshaller = null;
	private HashMap<String, List<ValidatorMessage>> msgs;
	private RuleFilterManager ruleFilterManager;
	private URI schemaUri = null;
	private boolean skipValidation = false;
	private ExtendedValidatorReport extendedReport;
	/**
	 * counter used for creating unique IDs.
	 * <p/>
	 * These are used to create temp files.
	 */
	private long uniqId = 0;

	/**
	 * Constructor to initialise the validator with the custom ontology and
	 * cv-mapping without object rule settings. Note: this constructor will try
	 * to load a local mzIdentML XML schema for syntactic schema validation.
	 * 
	 * @param aOntologyConfig
	 *            the ontology configuration file.
	 * @param aCvMappingFile
	 *            the cv-mapping rule configuration file.
	 * @param aCodedRuleFile
	 *            the object rule configuration file
	 * @param validatorGUI
	 *            the GUI
	 * @throws ValidatorException
	 *             in case the validator encounters unexpected errors.
	 * @throws OntologyLoaderException
	 *             in case of problems while loading the needed ontologies.
	 * @throws FileNotFoundException
	 *             in case of any configuration file doesn't exist.
	 * @throws CvRuleReaderException
	 *             in case of problems while reading cv mapping rules.
	 * 
	 */
	public MzMLValidator(InputStream aOntologyConfig, String aCvMappingFile, String aCodedRuleFile,
			MzMLValidatorGUI validatorGUI) throws ValidatorException, OntologyLoaderException,
			FileNotFoundException, CvRuleReaderException {
		super(aOntologyConfig);

		InputStream cvMappingFile = null;
		if(new File(aCvMappingFile).exists()) {
			cvMappingFile = new FileInputStream(aCvMappingFile);
		}else{
			cvMappingFile = MzMLValidator.class.getResourceAsStream("/"+aCvMappingFile);
		}
		InputStream objectRuleFile = null;
		if(new File(aCodedRuleFile).exists()) {
			objectRuleFile = new FileInputStream(aCodedRuleFile);
		}else{
			objectRuleFile = MzMLValidator.class.getResourceAsStream("/"+aCodedRuleFile);
		}
		this.setObjectRules(objectRuleFile);
		this.setCvMappingRules(cvMappingFile);
		try {
			cvMappingFile.close();
			objectRuleFile.close();
		} catch (IOException e1) {
			// do nothing
			e1.printStackTrace();
		}

		// set the gui
		this.setValidatorGUI(validatorGUI);

		this.msgs = new HashMap<String, List<ValidatorMessage>>();
		try {
			// ToDo: find better default value: e.g. official address or local
			// file
			this.schemaUri = new URI(
					"http://psidev.cvs.sourceforge.net/*checkout*/psidev/psi/psi-ms/mzML/schema/mzML1.1.0_idx.xsd");
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Could not create URI for schema location!", e);
		}

	}

	/**
	 * 
	 * @param validatroGUI
	 *            MzMLValidatorGUI that acts as the GUI parent of this
	 *            validator. Can be 'null' if ran from the command-line.
	 */
	public void setValidatorGUI(MzMLValidatorGUI validatroGUI) {
		this.gui = validatroGUI;
		this.progress = 0;
	}

	/**
	 * 
	 * @param level
	 *            MessageLevel with the minimal messagelevel to report.
	 */
	public void setMessageReportLevel(MessageLevel level) {
		this.msgL = level;
	}

	/**
	 * Get the currently specified schema URI instance files are validated
	 * against.
	 * 
	 * @return the URI pointing to the mzML schema.
	 */
	public URI getSchemaUri() {
		return schemaUri;
	}

	/**
	 * Use this to overwrite default schema location and specify your own schema
	 * to validate against. Note: to be able to validate both indexed and
	 * non-indexed mzML files, the schema for the indexed mzML should be given.
	 * 
	 * @param schemaUri
	 *            the URI that points to the schema.
	 */
	public void setSchemaUri(URI schemaUri) {
		this.schemaUri = schemaUri;
	}

	/**
	 * Flag to skip the schema validation step.
	 * 
	 * @return true if the schema validaton skep will be skipped with the
	 *         current settings, false if a schema validation will be performed.
	 */
	public boolean isSkipValidation() {
		return skipValidation;
	}

	/**
	 * Flag to specify if a schema validation is to be performed.
	 * 
	 * @param skipValidation
	 *            set to true if the schema validation step should be skipped.
	 */
	public void setSkipValidation(boolean skipValidation) {
		this.skipValidation = skipValidation;
	}

	/**
	 * Validate an input stream
	 * 
	 * @param aInputStream
	 * @return
	 * @throws ValidatorException
	 */
	public Collection<ValidatorMessage> validate(InputStream aInputStream)
			throws ValidatorException {

		File tempFile;
		try {
			tempFile = storeAsTemporaryFile(aInputStream);
			return this.startValidation(tempFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ValidatorException("Unable to process input stream", e);
		}

	}

	/**
	 * Store the content of the given input stream into a temporary file and
	 * return its descriptor.
	 * 
	 * @param is
	 *            the input stream to store.
	 * @return a File descriptor describing a temporary file storing the content
	 *         of the given input stream.
	 * @throws IOException
	 *             if an IO error occur.
	 */
	private File storeAsTemporaryFile(InputStream is) throws IOException {

		if (is == null) {
			throw new IllegalArgumentException("You must give a non null InputStream");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(is));

		// Create a temp file and write URL content in it.
		File tempDirectory = new File(System.getProperty("java.io.tmpdir", "tmp"));
		if (!tempDirectory.exists()) {
			if (!tempDirectory.mkdirs()) {
				throw new IOException("Cannot create temp directory: "
						+ tempDirectory.getAbsolutePath());
			}
		}

		long id = getUniqueId();

		File tempFile = File.createTempFile("validator." + id, ".xml", tempDirectory);

		log.info("The file is temporary store as: " + tempFile.getAbsolutePath());

		BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));

		String line;
		while ((line = in.readLine()) != null) {
			out.write(line);
		}

		in.close();

		out.flush();
		out.close();

		return tempFile;
	}

	/**
	 * Return a unique ID.
	 * 
	 * @return a unique id.
	 */
	synchronized private long getUniqueId() {
		return ++uniqId;
	}

	@Override
	public Collection<ValidatorMessage> validate(Object objectToCheck) throws ValidatorException {
		Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		for (ObjectRule rule : this.getObjectRules()) {
			if (rule.canCheck(objectToCheck)) {

				final Collection<ValidatorMessage> resultCheck = rule.check(objectToCheck);
				// update the object rule report
				extendedReport.objectRuleExecuted(rule, resultCheck);

				if (ruleFilterManager != null) {
					// state if the rule is valid in the file or not
					boolean valid = true;
					if (resultCheck != null && !resultCheck.isEmpty()) {
						valid = false;
					}
					ruleFilterManager.updateRulesToSkipByObjectRuleResult(rule, valid);
				}
				messages.addAll(resultCheck);
			}
		}
		return messages;
	}

	/**
	 * The entry point of the application.
	 * 
	 * @param args
	 *            Start-up arguments; five here: the ontology configuration
	 *            file, the CV mapping file, the coded rules file, the rule
	 *            filter file, the mzML file to validate, and the level of the
	 *            error messages.
	 */
	public static void main(String[] args) {
		if (args == null || args.length != 6) {
			printUsage();
		}
		// Validate existence of input files.
		File ontology = new File(args[0]);
		if (!ontology.exists()) {
			printError("The ontology config file you specified '" + args[0] + "' does not exist!");
		} else if (ontology.isDirectory()) {
			printError("The ontology config file you specified '" + args[0]
					+ "' is a folder, not a file!");
		}

		File cvMapping = new File(args[1]);
		if (!cvMapping.exists()) {
			printError("The CV mapping config file you specified '" + args[1] + "' does not exist!");
		} else if (cvMapping.isDirectory()) {
			printError("The CV mapping config file you specified '" + args[1]
					+ "' is a folder, not a file!");
		}

		File objectRules = new File(args[2]);
		if (!objectRules.exists()) {
			printError("The object rules config file you specified '" + args[2]
					+ "' does not exist!");
		} else if (objectRules.isDirectory()) {
			printError("The object rules config file you specified '" + args[2]
					+ "' is a folder, not a file!");
		}

		// Validate rule filter file to test
		File ruleFilterXMLFile = new File(args[3]);
		if (!ruleFilterXMLFile.exists()) {
			printError("The rule filter file you specified '" + args[3] + "' does not exist!");
		} else if (ruleFilterXMLFile.isDirectory()) {
			printError("The rule filter file you specified '" + args[3]
					+ "' is a folder, not a file!");
		}

		// Validate file to test
		File mzML = new File(args[4]);
		if (!mzML.exists()) {
			printError("The mzML file you specified '" + args[4] + "' does not exist!");
		} else if (mzML.isDirectory()) {
			printError("The mzML file you specified '" + args[4] + "' is a folder, not a file!");
		}

		// Validate messagelevel.
		MessageLevel msgLevel = getMessageLevel(args[5]);
		if (msgLevel == null) {
			System.err.println("\n\n *** Unknown message level '" + args[5] + "' ***\n");
			System.err.println("\tTry one of the following:");
			System.err.println("\t\t - DEBUG");
			System.err.println("\t\t - INFO");
			System.err.println("\t\t - WARN");
			System.err.println("\t\t - ERROR");
			System.err.println("\t\t - FATAL");
			System.err.println(" !!! Defaulting to 'INFO' !!!\n\n");
			msgLevel = MessageLevel.INFO;

		}

		// OK, all validated. Let's get going!
		Collection<ValidatorMessage> messages = new ArrayList<ValidatorMessage>();
		// We create the validator here:
		MzMLValidator validator = null;

		try {
			InputStream ontInput = new FileInputStream(ontology);
			// rule filter manager
			RuleFilterManager ruleFilterManager = null;
			if (ruleFilterXMLFile != null)
				ruleFilterManager = new RuleFilterManager(ruleFilterXMLFile);
			validator = new MzMLValidator(ontInput, cvMapping.getAbsolutePath(),
					objectRules.getAbsolutePath(), null);
			validator.setMessageReportLevel(msgLevel);
			validator.setRuleFilterManager(ruleFilterManager);

			// Add the messages to the ArrayList messages and use the
			// startValidation() method (below).
			messages.addAll(validator.startValidation(mzML));
		} catch (Exception e) {
			System.err.println("\n\nException occurred: " + e.getMessage());
			e.printStackTrace();
		}

		// ---------------- Print messages ---------------- //
		System.out.println(validator.printMessages(messages));
		System.out.println("\n");
		System.out.println(validator.printValidatorReport());
		System.out.println("\n");
		System.out.println(validator.printCvContextReport());
		System.out.println("\n\nAll done. Goodbye.");
	}

	/**
	 * Sets the ruleFilterManager
	 * 
	 * @param ruleFilterManager
	 */
	public void setRuleFilterManager(RuleFilterManager ruleFilterManager) {
		this.ruleFilterManager = ruleFilterManager;
	}

	public String printValidatorReport() {
		ExtendedValidatorReport report = this.getExtendedReport();
		if (report != null) {
			if (report.getTotalCvRules() == 0)
				report.setCvRules(getCvRuleManager().getCvRules(), this.ruleFilterManager);

			int cvRulesTotal = report.getTotalCvRules();
			int objectRulesTotal = report.getTotalObjectRules();

			StringBuilder sb = new StringBuilder();
			sb.append("\n\n\n---------- ---------- Rule statistics ---------- ----------\n\n");

			sb.append("\tCvMappingRule total count: ").append(cvRulesTotal).append("\n");
			sb.append("\tCvMappingRules not run: ").append(report.getCvRulesNotChecked().size())
					.append("\n");
			for (CvRule rule : report.getCvRulesNotChecked()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("\tCvMappingRules with invalid Xpath: ")
					.append(report.getCvRulesInvalidXpath().size()).append("\n");
			for (CvRule rule : report.getCvRulesInvalidXpath()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("\tCvMappingRules valid Xpath, but no hit: ")
					.append(report.getCvRulesValidXpath().size()).append("\n");
			for (CvRule rule : report.getCvRulesValidXpath()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("\tCvMappingRules run & valid: ").append(report.getCvRulesValid().size())
					.append("\n");
			for (CvRule rule : report.getCvRulesValid()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("\tObjectRule total count: ").append(objectRulesTotal).append("\n");
			sb.append("\tObjectRules not run: ").append(report.getObjectRulesNotChecked().size())
					.append("\n");
			for (ObjectRule rule : report.getObjectRulesNotChecked()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("\tObjectRules run & invalid: ")
					.append(report.getObjectRulesInvalid().size()).append("\n");
			for (ObjectRule rule : report.getObjectRulesInvalid()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("\tObjectRules run & valid: ").append(report.getObjectRulesValid().size())
					.append("\n");
			for (ObjectRule rule : report.getObjectRulesValid()) {
				sb.append("\t\trule: ").append(rule.getId()).append("\n");
			}
			sb.append("---------- ---------- ---------- ---------- ----------\n");

			return sb.toString();
		}
		return "";
	}

	public String printCvContextReport() {
		StringBuilder sb = new StringBuilder();

		if (ValidatorCvContext.getInstance() != null
				&& !ValidatorCvContext.getInstance().getNotRecognisedXpath().isEmpty()) {
			sb.append("\n\n\n---------- ---------- CvContext statistics ---------- ----------\n\n");
			// check for terms that were not anticipated with the rules in the
			// CV mapping file.
			for (String xpath : ValidatorCvContext.getInstance().getNotRecognisedXpath()) {
				sb.append("\t").append(xpath).append("\n");
				sb.append("\tunrecognized terms:\n");
				for (String term : ValidatorCvContext.getInstance().getNotRecognisedTerms(xpath)) {
					sb.append(term).append("; ");
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Starts the validation of a mzML file
	 * 
	 * @param mzMLFile
	 *            File with the mzML file to validate.
	 * @return Collection with validator messages.
	 */
	public Collection<ValidatorMessage> startValidation(File mzMLFile) {
		// reset old validation results
		// this will currently reset the status of all CvRules to a "not run"
		// status
		super.resetCvRuleStatus();
		// set the extended validator
		this.extendedReport = new ExtendedValidatorReport(getObjectRules());

		// init gui
		if (this.gui != null) {
			progress = 0;
			this.gui.initProgress(progress, PROGRESS_STEPS, progress);
		}

		// first up we check if the file is actually valid against the schema
		// (if not disabled)
		String guiProgressNote;
		if (skipValidation) {
			guiProgressNote = "Skipping schema validation!";
			if (this.gui != null) {
				this.gui.setProgress(++progress, guiProgressNote);
				try {
					// sleep for a second to give the user time to see this
					// important message
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// if we are interrupted (which should not happen) we just
					// go
					// on.
				}
			} else {
				System.out.println(guiProgressNote);
			}

		} else { // validating
			guiProgressNote = "Validating against schema (depending on the file size, this might take a while)...";
			if (this.gui != null) {
				this.gui.setProgress(++progress, guiProgressNote);
			} else {
				System.out.println(guiProgressNote);
			}

			boolean valid;
			try {
				valid = isValidMzML(mzMLFile, schemaUri);
			} catch (SAXException e) {
				log.error("ERROR during schema validation.", e);
				final ValidatorMessage message = new ValidatorMessage(
						"ERROR during schema validation." + e.getMessage(), MessageLevel.ERROR);
				addValidatorMessage("schema validation", message, this.msgL);
				valid = false;
			}

			// handle schema validation failure
			if (!valid) {
				if (this.gui != null) {
					return clusterByMessagesAndRules(getMessageCollection());

				} else {
					System.err.println("The provided file is not valid against the mzML schema!");
					System.err.println("Input file     : " + mzMLFile.getAbsolutePath());
					System.err.println("Schema location: " + schemaUri);
					for (ValidatorMessage msg : getMessageCollection()) {
						System.err.println(msg.getMessage());
					}
					System.exit(-1);
				}
			}

		}
		try {
			// We create an MzMLUnmarshaller (this specialised Unmarshaller will
			// internally use a XML index
			// to marshal the mzML file in snippets given by their Xpath)
			this.unmarshaller = new MzMLUnmarshaller(mzMLFile.toURI().toURL(), false);

			// ---------------- Internal consistency check of the CvMappingRules
			// ---------------- //

			// Validate CV Mapping Rules
			if (this.gui != null) { // report progress to the GUI if present
				this.gui.setProgress(++progress, "Checking internal consistency of CV rules......");
			}

			addMessages(this.checkCvMappingRules(), this.msgL);
			// See if the mapping makes sense.
			if (this.msgs.size() != 0) {
				if (this.gui == null) {
					System.err
							.println("\n\nThere were errors processing the CV mapping configuration file:\n");
					for (ValidatorMessage lMessage : getMessageCollection()) {
						System.err.println("\t - " + lMessage);
					}
					System.err.println("\n\n");
					printError("Unable to start validation due to configuration errors.\nSee above.");
				} else {
					return getMessageCollection();
				}
			}

			// ---------------- Validation work proper ---------------- //

			// Object rules that check internal Xrefs (e.g.:
			// referenceableParamGroups and refs).
			// Failure = auto-exit.
			// @TODO Check internal references with object rules!
			// ****************************
			// CHECK MANDATORY ELEMENTS
			// ****************************
			checkMandatoryElements();

			// ****************************
			// OBJECT RULES
			// ****************************
			applyObjectRules();

			// If we get here, the document is validatable. So proceed with the
			// CV mapping rules.
			applyCVMappingRules();

			if (this.gui != null) {
				this.gui.setProgress(++progress, "Validation complete, compiling output...");
			}
		} catch (Exception e) {
			if (this.gui == null) {
				System.err.println("\n\nException occurred: " + e.getMessage());
				e.printStackTrace();
			} else {
				this.gui.notifyOfError(e);
				return new ArrayList<ValidatorMessage>();
			}
		}

		// If ruleFilterManager is enabled, filter the messages.
		// Anyway, cluster the messages
		if (ruleFilterManager != null) {
			final Collection<ValidatorMessage> filteredValidatorMessages = ruleFilterManager
					.filterValidatorMessages(this.msgs, this.extendedReport);
			final Collection<ValidatorMessage> clusteredMessages = this
					.clusterByMessagesAndRules(filteredValidatorMessages);
			return clusteredMessages;
		} else {
			// or return all messages for semantic validation
			final Collection<ValidatorMessage> clusteredMessages = this
					.clusterByMessagesAndRules(this.getMessageCollection());
			return clusteredMessages;
		}
	}

	/**
	 * Clusters the ValidatorMessages
	 * 
	 * @param messages
	 * @return
	 */
	private Collection<ValidatorMessage> clusterByMessagesAndRules(
			Collection<ValidatorMessage> messages) {
		Collection<ValidatorMessage> clusteredMessages = new ArrayList<ValidatorMessage>(
				messages.size());

		// build a first clustering by message and rule
		Map<String, Map<Rule, Set<ValidatorMessage>>> clustering = new HashMap<String, Map<Rule, Set<ValidatorMessage>>>();
		for (ValidatorMessage message : messages) {
			// if the message doen't have an associated rule, store it directly
			// (comes from schema validation)
			if (message.getRule() == null)
				clusteredMessages.add(message);
			else {
				// if contains the same message, from the same rule, cluster it
				if (clustering.containsKey(message.getMessage())) {
					Map<Rule, Set<ValidatorMessage>> messagesCluster = clustering.get(message
							.getMessage());

					if (messagesCluster.containsKey(message.getRule())) {
						messagesCluster.get(message.getRule()).add(message);
					} else {
						Set<ValidatorMessage> validatorMessages = new HashSet<ValidatorMessage>();
						validatorMessages.add(message);
						messagesCluster.put(message.getRule(), validatorMessages);
					}
				} else {
					Map<Rule, Set<ValidatorMessage>> messagesCluster = new HashMap<Rule, Set<ValidatorMessage>>();

					Set<ValidatorMessage> validatorMessages = new HashSet<ValidatorMessage>();
					validatorMessages.add(message);
					messagesCluster.put(message.getRule(), validatorMessages);

					clustering.put(message.getMessage(), messagesCluster);
				}
			}
		}

		// build a second cluster by message level
		Map<MessageLevel, ClusteredContext> clusteringByMessageLevel = new HashMap<MessageLevel, ClusteredContext>();

		for (Map.Entry<String, Map<Rule, Set<ValidatorMessage>>> entry : clustering.entrySet()) {

			String message = entry.getKey();
			Map<Rule, Set<ValidatorMessage>> ruleCluster = entry.getValue();

			// cluster by message level and create proper validatorMessage
			for (Map.Entry<Rule, Set<ValidatorMessage>> ruleEntry : ruleCluster.entrySet()) {
				clusteringByMessageLevel.clear();

				Rule rule = ruleEntry.getKey();
				Set<ValidatorMessage> validatorMessages = ruleEntry.getValue();

				for (ValidatorMessage validatorMessage : validatorMessages) {

					if (clusteringByMessageLevel.containsKey(validatorMessage.getLevel())) {
						ClusteredContext clusteredContext = clusteringByMessageLevel
								.get(validatorMessage.getLevel());

						clusteredContext.getContexts().add(validatorMessage.getContext());
					} else {
						ClusteredContext clusteredContext = new ClusteredContext();

						clusteredContext.getContexts().add(validatorMessage.getContext());

						clusteringByMessageLevel.put(validatorMessage.getLevel(), clusteredContext);
					}
				}

				for (Map.Entry<MessageLevel, ClusteredContext> levelEntry : clusteringByMessageLevel
						.entrySet()) {

					ValidatorMessage validatorMessage = new ValidatorMessage(message,
							levelEntry.getKey(), levelEntry.getValue(), rule);
					clusteredMessages.add(validatorMessage);

				}
			}
		}

		return clusteredMessages;
	}

	/**
	 * Check for the presence of all mandatory elements required at this
	 * validation type
	 */
	private void checkMandatoryElements() {
		List<ValidatorMessage> ret = new ArrayList<ValidatorMessage>();
		if (ruleFilterManager != null) {
			final List<String> mandatoryElements = ruleFilterManager.getMandatoryElements();
			for (String elementName : mandatoryElements) {
				MzMLElement mzMLElement = getMzMLElement(elementName);
				// check if that element is present on the file
				final MzMLObject mzIdentMLObject = unmarshaller.unmarshalFromXpath(
						mzMLElement.getXpath(), mzMLElement.getClazz());
				if (mzIdentMLObject == null) {
					final MandatoryElementsObjectRule mandatoryObjectRule = new MandatoryElementsObjectRule(
							ontologyMngr);
					final ValidatorMessage validatorMessage = new ValidatorMessage(
							"The element on xPath:'" + mzMLElement.getXpath()
									+ "' is required for the current type of validation.",
							MessageLevel.ERROR, new Context(mzMLElement.getXpath()),
							mandatoryObjectRule);
					// extendedReport.objectRuleExecuted(mandatoryObjectRule,
					// validatorMessage);
					// this.addObjectRule(mandatoryObjectRule);
					addValidatorMessage(validatorMessage.getRule().getId(), validatorMessage,
							this.msgL);
				}
			}
		}
	}

	private MzMLElement getMzMLElement(String elementName) {
		for (MzMLElement element : MzMLElement.values()) {
			if (element.name().equals(elementName))
				return element;
		}
		return null;
	}


	private Collection<ValidatorMessage> getMessageCollection() {
		Collection<ValidatorMessage> ret = new HashSet<ValidatorMessage>();
		for (String key : this.msgs.keySet()) {
			final List<ValidatorMessage> list = this.msgs.get(key);
			if (list != null) {
				ret.addAll(list);
			}
		}
		return ret;
	}

	private void applyCVMappingRules() throws ValidatorException {
		// --------------------
		// Validate the file description.
		// -------------------- //
		// ToDo: check: if there is only one such element in the XML, we can
		// use the unmarshalFromXpath() method!
		// This is the new way to do it (not considering the unmarshaller)
		checkElementCvMapping(MzMLElement.FileDescription.getXpath(), FileDescription.class);

		// --------------------
		// Validate the sample list.
		// -------------------- //
		checkElementCvMapping(MzMLElement.SampleList.getXpath(), SampleList.class);

		// --------------------
		// Validate the software list.
		// -------------------- //
		checkElementCvMapping(MzMLElement.Software.getXpath(), Software.class);

		// --------------------
		// Validate the instrument configuration list
		// (substituting the former instrument list)
		// -------------------- //
		checkElementCvMapping(MzMLElement.InstrumentConfigurationList.getXpath(),
				InstrumentConfigurationList.class);

		// --------------------
		// Validate the data processing list.
		// -------------------- //
		checkElementCvMapping(MzMLElement.DataProcessingList.getXpath(), DataProcessingList.class);

		// --------------------
		// Validate the chromatogram binary data array.
		// -------------------- //
		checkElementCvMapping(MzMLElement.Chromatogram.getXpath(), Chromatogram.class);

		// --------------------
		// Validate each spectrum (in parallel depending of CPU cores)
		// -------------------- //

		if (this.gui != null) {
			this.gui.setProgress(++progress, "Validating " + MzMLElement.Spectrum.getXpath()
					+ " (this might take a while)...");
		}
		// iterator to provide MzMLObjects
		MzMLObjectIterator spectrumIterator = this.unmarshaller.unmarshalCollectionFromXpath(
				MzMLElement.Spectrum.getXpath(), Spectrum.class);

		// create synchronized List to which all threads can write their
		// Validator messages
		Map<String, List<ValidatorMessage>> sync_msgs = Collections
				.synchronizedMap(new HashMap<String, List<ValidatorMessage>>());

		// Create lock.
		InnerLock lock = new InnerLock();
		InnerIteratorSync<Spectrum> iteratorSync = new InnerIteratorSync(spectrumIterator);
		Collection<InnerSpecValidator> runners = new ArrayList<InnerSpecValidator>();
		int processorCount = Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < processorCount; i++) {
			InnerSpecValidator runner = new InnerSpecValidator(iteratorSync, lock, i);
			runners.add(runner);
			new Thread(runner).start();
		}

		// Wait for it.
		lock.isDone(runners.size());
		// now we add all the collected messages from the spectra validators
		// to the general message list
		addSyncMessages(sync_msgs, this.msgL);
	}

	/**
	 * Apply object rules
	 * 
	 * @return a collection of {@link ValidatorMessage}
	 * @throws ValidatorException
	 */
	private void applyObjectRules() throws ValidatorException {

		// instrumentConfiguration
		checkElementObjectRule(MzMLElement.InstrumentConfiguration.getXpath(),
				InstrumentConfiguration.class);

		// source file list
		checkElementObjectRule(MzMLElement.SourceFileList.getXpath(), SourceFileList.class);

		// source
		checkElementObjectRule(MzMLElement.SourceComponent.getXpath(), SourceComponent.class);

		// softwareList
		checkElementObjectRule(MzMLElement.SoftwareList.getXpath(), SoftwareList.class);

		// scan settings
		checkElementObjectRule(MzMLElement.ScanSettings.getXpath(), ScanSettings.class);

	}

	private void checkElementCvMapping(String xpath, Class clazz) throws ValidatorException {
		MzMLObjectIterator mzMLIter;
		if (this.gui != null) {
			this.gui.setProgress(++progress, "Validating " + xpath + "...");
		}
		mzMLIter = this.unmarshaller.unmarshalCollectionFromXpath(xpath, clazz);

		Collection toValidate = new ArrayList();
		while (mzMLIter.hasNext()) {
			final Object next = mzMLIter.next();
			toValidate.add(next);
		}

		final Collection<ValidatorMessage> cvMappingResult = this.checkCvMapping(toValidate, xpath);

		addMessages(cvMappingResult, this.msgL);
	}

	private void checkElementObjectRule(String xpath, Class clazz) throws ValidatorException {
		MzMLObjectIterator<MzMLObject> mzMLIter;
		if (this.gui != null) {
			this.gui.setProgress(++progress, "Validating " + xpath + "...");
		}
		mzMLIter = this.unmarshaller.unmarshalCollectionFromXpath(xpath, clazz);

		Collection<ValidatorMessage> objectRuleResult = new ArrayList<ValidatorMessage>();
		Collection toValidate = new ArrayList();
		while (mzMLIter.hasNext()) {
			final MzMLObject next = mzMLIter.next();
			final Collection<ValidatorMessage> validationResult = this.validate(next);
			if (validationResult != null && !validationResult.isEmpty())
				objectRuleResult.addAll(validationResult);
		}

		addMessages(objectRuleResult, this.msgL);
	}

	@Override
	public Collection<ValidatorMessage> checkCvMapping(Collection<?> collection, String xPath)
			throws ValidatorException {
		Collection messages = new ArrayList();

		if (this.getCvRuleManager() != null) {
			for (CvRule rule : this.getCvRuleManager().getCvRules()) {
				for (Object o : collection) {
					if (rule.canCheck(xPath)) {
						final Collection<ValidatorMessage> resultCheck = rule.check(o, xPath);
						if (this.ruleFilterManager != null) {
							// state if the rule is valid in the file or not
							boolean valid = true;
							if (resultCheck != null && !resultCheck.isEmpty()) {
								valid = false;
							}
							this.ruleFilterManager.updateRulesToSkipByCvMappingRuleResult(rule,
									valid);
						}
						messages.addAll(resultCheck);
					}
				}
			}
		} else {
			log.error("The CvRuleManager has not been set up yet.");
		}
		return messages;
	}

	private void addValidatorMessage(String ruleId, ValidatorMessage validatorMessage,
			MessageLevel msgLevel) {

		if (validatorMessage.getLevel().isHigher(msgLevel)
				|| validatorMessage.getLevel().isSame(msgLevel)) {
			if (this.msgs.containsKey(ruleId)) {
				this.msgs.get(ruleId).add(validatorMessage);
			} else {
				List<ValidatorMessage> list = new ArrayList<ValidatorMessage>();
				list.add(validatorMessage);
				this.msgs.put(ruleId, list);
			}
			this.extendedReport.setObjectRuleAsInvalid(ruleId);
		} else {
			this.extendedReport.setObjectRuleAsSkipped(ruleId);
		}
	}

	/**
	 * Add a new object rule to the list of object rules only if it is not
	 * already in the list
	 * 
	 * @param rule
	 * @return true if has been added or false if not
	 */
	private boolean addObjectRule(ObjectRule rule) {
		boolean isNew = true;
		for (ObjectRule objectRule : this.getObjectRules()) {
			if (objectRule.getId().equals(rule.getId()))
				isNew = false;
		}
		if (isNew)
			this.getObjectRules().add(rule);
		return isNew;
	}

	private boolean isValidMzML(File mzML, URI schemaUri) throws SAXException {
		boolean valid;

		MzMLSchemaValidator mzMLschemaValidator = new MzMLSchemaValidator();
		MzMLValidationErrorHandler errorHandler = null;
		try {
			mzMLschemaValidator.setSchema(schemaUri);
			errorHandler = mzMLschemaValidator.validate(new FileReader(mzML));
		} catch (FileNotFoundException e) {
			log.fatal("FATAL: Could not find the MzML instance file while trying to "
					+ "validate it! Its existence should have been checked before!", e);
			System.exit(-1);
		} catch (MalformedURLException e) {
			log.fatal("FATAL: The MzML schema URI is not ");
			// should not happen! since the MzMLValidator should check first if
			// the
			// schema URI is valid before trying to validate with it!
			e.printStackTrace();
			System.exit(-1);
		}

		// if we have no errors, the file is valid
		if (errorHandler.noErrors()) {
			valid = true;
		} else {
			for (ValidatorMessage validatorMessage : errorHandler.getErrorsAsValidatorMessages()) {
				addValidatorMessage("schema validation", validatorMessage, this.msgL);
			}
			valid = false;
		}

		return valid;
	}

	/**
	 * Simple wrapper class to allow synchronisation on the hasNext() and next()
	 * methods of the iterator.
	 */
	private class InnerIteratorSync<T> {
		private Iterator<T> iter = null;

		public InnerIteratorSync(Iterator<T> aIterator) {
			iter = aIterator;
		}

		public synchronized T next() {
			T result = null;
			if (iter.hasNext()) {
				result = iter.next();
			}
			return result;
		}
	}

	/**
	 * Simple lock class so the main thread can detect worker threads'
	 * completion.
	 */
	private class InnerLock {
		private int doneCount = 0;

		public synchronized void updateDoneCount() {
			doneCount++;
			notifyAll();
		}

		public synchronized boolean isDone(int totalCount) {
			while (doneCount < totalCount) {
				try {
					wait();
				} catch (InterruptedException ie) {
					System.err.println("I've been interrupted...");
				}
			}
			return true;
		}
	}

	/**
	 * Runnable that requests the next spectrum from the synchronised iterator
	 * wrapper, and validates it.
	 */
	private class InnerSpecValidator<T> implements Runnable {
		InnerIteratorSync<T> iter = null;
		Collection messages = new ArrayList();
		InnerLock lock = null;
		int count = 0;
		int iNumber = -1;

		public InnerSpecValidator(InnerIteratorSync<T> aIterator, InnerLock aLock, int aNumber) {
			iter = aIterator;
			lock = aLock;
			iNumber = aNumber;
		}

		public void run() {
			T s = null;
			while ((s = iter.next()) != null) {
				// I changed SpectrumType for Spectrum (according to the new
				// PRIDE object model)
				ArrayList<T> tovalidate = new ArrayList(1);
				tovalidate.add(s);
				try {
					// check cvMapping rules
					addMessages(checkCvMapping(tovalidate, MzMLElement.Spectrum.getXpath()), msgL);
					// check object rules
					addMessages(validate(s), msgL);

					count++;
				} catch (ValidatorException ve) {
					ve.printStackTrace();
				}
			}
			lock.updateDoneCount();
		}

		public Collection getMessages() {
			return messages;
		}

		public int getCount() {
			return count;
		}
	}

	private static void printUsage() {
		printError("Usage:\n\n\t"
				+ MzMLValidator.class.getName()
				+ " <ontology_config_file> <cv_mapping_config_file> <coded_rules_config_file> <xml_file_filter_file> <mzml_file_to_validate> <message_level>\n\n\t\tWhere message level can be:\n\t\t - DEBUG\n\t\t - INFO\n\t\t - WARN\n\t\t - ERROR\n\t\t - FATAL");
	}

	private static void printError(String aMessage) {
		System.err.println("\n\n" + aMessage + "\n\n");
		System.exit(1);
	}

	private String printMessages(Collection aMessages) {
		StringBuilder sb = new StringBuilder();
		if (aMessages.size() != 0) {
			sb.append("\n\nThe following messages were obtained during the validation of your XML file:\n");
			for (Object aMessage : aMessages) {
				ValidatorMessage lMessage = (ValidatorMessage) aMessage;
				sb.append(" * " + lMessage + "\n");
			}
		} else {
			sb.append("\n\nCongratulations! Your XML file passed the semantic validation!\n\n");
		}
		return sb.toString();
	}

	private void addMessages(Collection<ValidatorMessage> aNewMessages, MessageLevel aLevel) {
		for (ValidatorMessage aNewMessage : aNewMessages) {
			if (aNewMessage.getLevel().isHigher(aLevel) || aNewMessage.getLevel().isSame(aLevel)) {
				addValidatorMessage(aNewMessage.getRule().getId(), aNewMessage, this.msgL);
			}
		}
	}

	private void addSyncMessages(Map<String, List<ValidatorMessage>> sync_msgs, MessageLevel aLevel) {
		for (String key : sync_msgs.keySet()) {
			final List<ValidatorMessage> list = sync_msgs.get(key);
			for (ValidatorMessage aNewMessage : list) {
				if (aNewMessage.getLevel().isHigher(aLevel)
						|| aNewMessage.getLevel().isSame(aLevel)) {
					addValidatorMessage(key, aNewMessage, this.msgL);
				}
			}

		}

	}

	private static MessageLevel getMessageLevel(String aLevel) {
		aLevel = aLevel.trim();
		MessageLevel result = null;
		if (aLevel.equals("DEBUG")) {
			result = MessageLevel.DEBUG;
		} else if (aLevel.equals("INFO")) {
			result = MessageLevel.INFO;
		} else if (aLevel.equals("WARN")) {
			result = MessageLevel.WARN;
		} else if (aLevel.equals("ERROR")) {
			result = MessageLevel.ERROR;
		} else if (aLevel.equals("FATAL")) {
			result = MessageLevel.FATAL;
		}

		return result;
	}

	/**
	 * Method to reset all the fields in this validator.
	 * 
	 * @throws CvRuleReaderException
	 * @throws ValidatorException
	 * @throws IOException
	 */
	protected void reset(String cvMappingRuleFileName, String objectRuleFileName)
			throws CvRuleReaderException, ValidatorException, IOException {
		// reset the collection of ValidatorMessages
		if (this.msgs != null) {
			this.msgs.clear();
		}

		// reset the message reporting level to the default
		this.msgL = MessageLevel.WARN;
		// reset the unmarshaller
		this.unmarshaller = null;
		// reset the progress counter
		this.progress = 0;

		// restart the rules to skip
		if (ruleFilterManager != null)
			ruleFilterManager.restartRulesToSkip();

		// delete all objectRules
		this.getObjectRules().clear();
		// delete all cvMappingRules
		this.getCvRuleManager().getCvRules().clear();

		// set the new cvMapping rules
		final FileInputStream cvMappingRuleFile = new FileInputStream(cvMappingRuleFileName);
		this.setCvMappingRules(cvMappingRuleFile);
		cvMappingRuleFile.close();

		// set the new objectrules
		final FileInputStream objectRuleFile = new FileInputStream(objectRuleFileName);
		this.setObjectRules(objectRuleFile);
		objectRuleFile.close();

	}

	public ExtendedValidatorReport getExtendedReport() {
		if (this.extendedReport != null) {
			if (this.extendedReport.getTotalCvRules() == 0)
				this.extendedReport.setCvRules(this.getCvRuleManager().getCvRules(),
						this.ruleFilterManager);
		}
		return this.extendedReport;

	}
}
