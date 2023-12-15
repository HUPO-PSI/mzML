package psidev.psi.ms.rulefilter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import psidev.psi.ms.Resources;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.fail;

public class RuleFilterTest {

	private static final String FILE_RULE_FILTER = "/ruleFilterMIAPE.xml";
	@Rule
	public TemporaryFolder tf = new TemporaryFolder();

	@Test
	public void ruleFilterTest() throws IOException {
		File outputFolder = tf.newFolder("ruleFilterTest");
		try {
			File file = Resources.extractResource(FILE_RULE_FILTER, outputFolder);
			final RuleFilterManager filterManager = new RuleFilterManager(file);
			filterManager.printRuleFilter();
			HashMap<String, String> selectedOptions = new HashMap<String, String>();
			selectedOptions.put(MaldiOrEsiCondition.getID(), MaldiOrEsiCondition.ESI.getOption());
			filterManager.getCVMappingRulesToSkipByUserOptions(selectedOptions);
		} catch (JAXBException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void ruleFilterTest2() throws IOException {
		File outputFolder = tf.newFolder("ruleFilterTest2");
		try {
			System.out.println("\n\n---------------------");
			File file = Resources.extractResource(FILE_RULE_FILTER, outputFolder);
			final RuleFilterManager filterManager = new RuleFilterManager(file);
			HashMap<String, String> selectedOptions = new HashMap<String, String>();
			selectedOptions.put(MaldiOrEsiCondition.getID(), MaldiOrEsiCondition.ESI.getOption());
			final Set<String> cvMappingRulesToExclude = filterManager
					.getCVMappingRulesToSkipByUserOptions(selectedOptions);
			for (String ruleId : cvMappingRulesToExclude) {
				System.out.println(ruleId);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
			fail();
		}
	}
}
