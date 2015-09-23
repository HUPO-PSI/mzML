package psidev.psi.ms.rulefilter;

import java.util.HashMap;

import psidev.psi.ms.rulefilter.jaxb.UserCondition;
import psidev.psi.ms.rulefilter.jaxb.UserOption;

/**
 * Interface that provides the methods to implement that parse rule filter xml
 * files. That files follow the xsd schema located at:
 * http://proteo.cnb.csic.es/miape-api/schemas/ruleFilter_v1.2.xsd
 * 
 * @author Salva
 * 
 */
public interface RuleFilterAgent {
	/**
	 * gets the selected options chosen by the user regarding the exclusion of
	 * CV mapping rules and object rules
	 * 
	 * @return a hashMap in which the key is the identifier of the
	 *         {@link UserCondition} and the value is the identifier of the
	 *         chosen {@link UserOption}
	 */
	public HashMap<String, String> getSelectedOptions();

}
