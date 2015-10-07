/**
 * Created by IntelliJ IDEA. User: martlenn Date: 14-Oct-2008 Time: 16:06:58
 */
package psidev.psi.ms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import psidev.psi.tools.validator.ValidatorMessage;

/**
 * This class
 * 
 * @author martlenn
 * @version $Id$
 */
public class MzMLSchemaValidator {

	/**
	 * This static object is used to create the Schema object used for
	 * validation.
	 */
	private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory
			.newInstance("http://www.w3.org/2001/XMLSchema");

	/**
	 * The schema to validate against.
	 */
	private Schema schema = null;

	/**
	 * This method carries out the work of validating the XML file passed in
	 * through 'inputStream' against the compiled XML schema 'schema'. This
	 * method is a helper method called by the implementation of this abstract
	 * class.
	 * 
	 * @param reader
	 *            being a java.io.Reader from the complete XML file being
	 *            validated.
	 * @param schema
	 *            being a compiled schema object built from the appropriate xsd
	 *            ( performed by the implementing sub-class of this abstract
	 *            class.)
	 * @return an XMLValidationErrorHandler that can be queried for details of
	 *         any parsing errors to retrieve plain text or HTML
	 * @throws org.xml.sax.SAXException
	 */
	protected MzMLValidationErrorHandler validate(Reader reader, Schema schema) throws SAXException {

		final MzMLValidationErrorHandler mzMLValidationErrorHandler = new MzMLValidationErrorHandler();
		Validator validator = schema.newValidator();
		validator.setErrorHandler(mzMLValidationErrorHandler);
		try {
			validator.validate(new SAXSource(new InputSource(reader)));
		} catch (IOException ioe) {
			mzMLValidationErrorHandler.fatalError(ioe);
		} catch (SAXParseException spe) {
			// commented: the fatal error is internally called before to catch
			// the exception
			// mzMLValidationErrorHandler.fatalError(spe);
		}
		return mzMLValidationErrorHandler;
	}

	public void setSchema(URI aSchemaUri) throws SAXException, MalformedURLException {
		schema = SCHEMA_FACTORY.newSchema(aSchemaUri.toURL());
	}

	public Schema getSchema() {
		return schema;
	}

	/**
	 * This method must be implemented to create a suitable Schema object for
	 * the xsd file in question.
	 * 
	 * @param reader
	 *            the XML file being validated as a Stream (Reader)
	 * @return an XMLValidationErrorHandler that can be queried to return all of
	 *         the error in the XML file as plain text or HTML.
	 */
	public MzMLValidationErrorHandler validate(Reader reader) throws SAXException {
		if (schema == null) {
			throw new IllegalStateException(
					"You need to set a schema to validate against first! use the 'setSchema(File aSchemaFile)' method for this!");
		}
		return validate(reader, schema);
	}

	public static void main(String[] args) {

		MzMLSchemaValidator validator = new MzMLSchemaValidator();

		if (args == null || args.length != 2) {
			printUsage();
			System.exit(1);
		}
		// Check schema file.
		File schemaFile = new File(args[0]);
		if (!schemaFile.exists()) {
			System.err.println("\nUnable to find the schema file you specified: '" + args[0]
					+ "'!\n");
			System.exit(1);
		}
		if (schemaFile.isDirectory()) {
			System.err.println("\nThe schema file you specified ('" + args[0]
					+ "') was a folder, not a file!\n");
			System.exit(1);
		}
		if (!schemaFile.getName().toLowerCase().endsWith(".xsd")) {
			System.err.println("Warning: your schema file does not carry the extension '.xsd'!");
		}

		// Check input folder.
		File inputFolder = new File(args[1]);
		if (!inputFolder.exists()) {
			System.out.println("\nUnable to find the input folder you specified: '" + args[1]
					+ "'!\n");
			System.exit(1);
		}
		if (!inputFolder.isDirectory()) {
			System.out.println("\nThe input folder you specified ('" + args[1]
					+ "') was a file, not a folder!\n");
			System.exit(1);
		}

		BufferedReader br = null;
		try {
			// Set the schema.
			validator.setSchema(schemaFile.toURI());
			System.out
					.println("\nRetrieving files from '" + inputFolder.getAbsolutePath() + "'...");
			File[] inputFiles = inputFolder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					boolean result = false;
					if (name.toLowerCase().endsWith("mzml") || name.toLowerCase().endsWith("xml")) {
						result = true;
					}
					return result;
				}
			});
			System.out.println("Found " + inputFiles.length + " input files.\n");
			System.out.println("Validating files...");
			for (File inputFile : inputFiles) {
				System.out.println("\n\n\n  - Validating file '" + inputFile.getAbsolutePath()
						+ "'...");
				br = new BufferedReader(new FileReader(inputFile));
				MzMLValidationErrorHandler xveh = validator.validate(br);
				if (xveh.noErrors()) {
					System.out.println("    File is valid!");
				} else {
					System.out.println("    * Errors detected: ");
					for (ValidatorMessage vMsg : xveh.getErrorsAsValidatorMessages()) {
						System.out.println(vMsg.getMessage());
					}
				}
				br.close();
			}
			System.out.println("\nAll done!\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ioe) {
				// Do nothing.
			}
		}
	}

	private static void printUsage() {
		StringBuffer out = new StringBuffer();
		out.append("\n\nUsage: java ").append(MzMLSchemaValidator.class.getName());
		out.append(" <schema_file> <inputfolder> ");
		System.out.println(out.toString());
	}

}
