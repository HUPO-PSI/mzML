package psidev.psi.ms.rulefilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import psidev.psi.ms.ExtendedValidatorReport;
import psidev.psi.ms.rulefilter.jaxb.CvMappingRuleCondition;
import psidev.psi.ms.rulefilter.jaxb.CvMappingRuleToSkip;
import psidev.psi.ms.rulefilter.jaxb.MandatoryMzIdentMLElement;
import psidev.psi.ms.rulefilter.jaxb.MandatoryMzMLElement;
import psidev.psi.ms.rulefilter.jaxb.ObjectRuleCondition;
import psidev.psi.ms.rulefilter.jaxb.ObjectRuleToSkip;
import psidev.psi.ms.rulefilter.jaxb.ReferencedRules;
import psidev.psi.ms.rulefilter.jaxb.RuleFilter;
import psidev.psi.ms.rulefilter.jaxb.RulesToSkipRef;
import psidev.psi.ms.rulefilter.jaxb.UserCondition;
import psidev.psi.ms.rulefilter.jaxb.UserOption;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRule;

/**
 * This class provides the methods to maintain a list of object rules and a list
 * of cv mapping rules to skip. Its behaviour is configured by a configuration
 * xml file (following the schema:
 * http://proteo.cnb.csic.es/miape-api/schemas/ruleFilter_v1.2.xsd)
 * 
 * @author Salva
 * 
 */
public class RuleFilterManager {
	private JAXBContext jc;
	private RuleFilter filter = null;
	private List<String> objectRulesToSkip = new ArrayList<String>();
	private List<String> cvMappingRulesToSkip = new ArrayList<String>();

	public RuleFilterManager(File xmlFile) throws JAXBException {
		// check if null
		if (xmlFile == null)
			throw new IllegalArgumentException("Provide a no null file!");

		// check if exists
		if (!xmlFile.exists())
			throw new IllegalArgumentException(xmlFile.getAbsolutePath() + " doesn't exist!");
		jc = JAXBContext.newInstance("psidev.psi.ms.rulefilter.jaxb");
		this.filter = (RuleFilter) jc.createUnmarshaller().unmarshal(xmlFile);

	}

	public void setFilter(RuleFilter filter) {
		this.filter = filter;
	}

	public RuleFilter getFilter() {
		return filter;
	}

	/**
	 * Gets a list of mandatory elements
	 * 
	 * @return
	 */
	public List<String> getMandatoryElements() {
		List<String> ret = new ArrayList<String>();
		if (filter.getMandatoryElements() != null) {
			for (MandatoryMzIdentMLElement mandatoryElement : filter.getMandatoryElements()
					.getMandatoryMzIdentMLElement()) {
				final String mzIdentMLElement = mandatoryElement.getMzIdentMLElement();
				if (mzIdentMLElement != null && !"".equals(mzIdentMLElement))
					ret.add(mzIdentMLElement);
			}
			for (MandatoryMzMLElement mandatoryElement : filter.getMandatoryElements()
					.getMandatoryMzMLElement()) {
				final String mzMLElement = mandatoryElement.getMzMLElement();
				if (mzMLElement != null && !"".equals(mzMLElement))
					ret.add(mzMLElement);
			}
		}
		return ret;
	}

	/**
	 * Look for the chosen options in the rule filter file and returns a list
	 * with the identifiers of the rules to exclude
	 * 
	 * @param selectedOptions
	 *            : the key is the identifier of the condition and the value is
	 *            the chosen option
	 * @return the list of identifiers of rules to exclude
	 */
	public Set<String> getCVMappingRulesToSkipByUserOptions(HashMap<String, String> selectedOptions) {
		Set<String> cvMappingRulesIds = new HashSet<String>();

		for (String conditionId : selectedOptions.keySet()) {
			UserCondition userCondition = getCondition(conditionId);
			if (userCondition != null) {
				List<CvMappingRuleToSkip> rules = getMappingRulesToSkip(userCondition,
						selectedOptions.get(conditionId));
				if (rules != null) {
					for (CvMappingRuleToSkip cvMappingRule : rules) {
						cvMappingRulesIds.add(cvMappingRule.getId());
					}
				}
			}
		}
		return cvMappingRulesIds;
	}

	private List<CvMappingRuleToSkip> getMappingRulesToSkip(UserCondition userCondition,
			String optionId) {
		List<CvMappingRuleToSkip> cvMappingRulesToSkip = new ArrayList<CvMappingRuleToSkip>();
		if (optionId != null)
			for (UserOption option : userCondition.getUserOption()) {
				if (option.getId().equals(optionId)) {
					if (option.getCvMappingRuleToSkip() != null) {
						cvMappingRulesToSkip.addAll(option.getCvMappingRuleToSkip());
					}
					if (option.getRulesToSkipRef() != null) {
						for (RulesToSkipRef rulesToSkipRef : option.getRulesToSkipRef()) {
							if (filter.getReferences() != null
									&& filter.getReferences().getReferencedRules() != null) {
								for (ReferencedRules referencedRuleSet : filter.getReferences()
										.getReferencedRules()) {
									if (referencedRuleSet.getId().equals(rulesToSkipRef.getRef())) {
										if (referencedRuleSet.getCvMappingRuleToSkip() != null) {
											cvMappingRulesToSkip.addAll(referencedRuleSet
													.getCvMappingRuleToSkip());
										}
									}
								}
							}
						}
					}
				}
			}
		return cvMappingRulesToSkip;
	}

	private UserCondition getCondition(String conditionId) {
		if (conditionId == null || "".equals(conditionId) || filter == null)
			return null;
		for (UserCondition userCondition : filter.getUserConditions().getUserCondition()) {
			if (userCondition.getId().equals(conditionId))
				return userCondition;
		}
		return null;
	}

	/**
	 * Look for the the chosen options in the rule filter file and returns a
	 * list with the identifiers of the rules to exclude
	 * 
	 * @param selectedOptions
	 *            : the key is the identifier of the condition and the value is
	 *            the chosen option
	 * @return the list of identifiers of rules to exclude
	 */
	public Set<String> getObjectRulesToSkipByUserOptions(HashMap<String, String> selectedOptions) {
		Set<String> ret = new HashSet<String>();

		for (String conditionId : selectedOptions.keySet()) {
			UserCondition condition = getCondition(conditionId);
			if (condition != null) {
				List<ObjectRuleToSkip> rules = getObjectRulesToSkip(condition,
						selectedOptions.get(conditionId));
				if (rules != null) {
					for (ObjectRuleToSkip objectRule : rules) {
						ret.add(objectRule.getId());
					}
				}
			}
		}

		return ret;
	}

	private List<ObjectRuleToSkip> getObjectRulesToSkip(UserCondition condition, String optionId) {
		List<ObjectRuleToSkip> ret = new ArrayList<ObjectRuleToSkip>();
		if (optionId != null)
			for (UserOption option : condition.getUserOption()) {
				if (option.getId().equals(optionId)) {
					if (option.getObjectRuleToSkip() != null) {
						ret.addAll(option.getObjectRuleToSkip());
					}
					if (option.getRulesToSkipRef() != null) {
						for (RulesToSkipRef ruleToSkipRef : option.getRulesToSkipRef()) {
							if (filter.getReferences() != null
									&& filter.getReferences().getReferencedRules() != null) {
								for (ReferencedRules referencedRuleSet : filter.getReferences()
										.getReferencedRules()) {
									if (referencedRuleSet.equals(ruleToSkipRef.getRef())) {
										ret.addAll(referencedRuleSet.getObjectRuleToSkip());
									}
								}
							}
						}
					}
				}
			}
		return ret;
	}

	private List<String> getObjectRulesToSkipByObjectRule(String ruleId, boolean valid) {
		List<String> ret = new ArrayList<String>();

		if (this.filter.getObjectRuleConditions() != null) {
			for (ObjectRuleCondition objectRuleCondition : this.filter.getObjectRuleConditions()
					.getObjectRuleCondition()) {
				if (objectRuleCondition.getId().equals(ruleId)) {
					if ((valid && objectRuleCondition.isValid())
							|| (!valid && !objectRuleCondition.isValid())) {
						if (objectRuleCondition.getObjectRuleToSkip() != null) {
							for (ObjectRuleToSkip objectRule : objectRuleCondition
									.getObjectRuleToSkip()) {
								ret.add(objectRule.getId());
							}
						}
					}
				}
			}
		}
		return ret;
	}

	private List<String> getCvMappingRulesToSkipByObjectRule(String ruleId, boolean valid) {
		List<String> ret = new ArrayList<String>();

		if (this.filter.getObjectRuleConditions() != null) {
			for (ObjectRuleCondition objectRuleCondition : this.filter.getObjectRuleConditions()
					.getObjectRuleCondition()) {
				if (objectRuleCondition.getId().equals(ruleId)) {
					if ((valid && objectRuleCondition.isValid())
							|| (!valid && !objectRuleCondition.isValid())) {
						if (objectRuleCondition.getCvMappingRuleToSkip() != null) {
							for (CvMappingRuleToSkip cvMappingRule : objectRuleCondition
									.getCvMappingRuleToSkip()) {
								ret.add(cvMappingRule.getId());
							}
						}
					}
				}
			}
		}
		return ret;
	}

	public List<String> getCvMappingRulesToSkipByCvMappingRule(String ruleId, boolean valid) {
		List<String> ret = new ArrayList<String>();

		if (this.filter.getCvMappingRuleConditions() != null) {
			for (CvMappingRuleCondition cvMappingRule : this.filter.getCvMappingRuleConditions()
					.getCvMappingRuleCondition()) {
				if (cvMappingRule.getId().equals(ruleId)) {
					if ((valid && cvMappingRule.isValid()) || (!valid && !cvMappingRule.isValid())) {
						if (cvMappingRule.getCvMappingRuleToSkip() != null) {
							for (CvMappingRuleToSkip cvMappingRule2 : cvMappingRule
									.getCvMappingRuleToSkip()) {
								ret.add(cvMappingRule2.getId());
							}
						}
					}
				}
			}
		}
		return ret;
	}

	public List<String> getObjectRulesToSkipByCvMappingRule(String ruleId, boolean valid) {
		List<String> ret = new ArrayList<String>();

		if (this.filter.getCvMappingRuleConditions() != null) {
			for (CvMappingRuleCondition cvMappingRule : this.filter.getCvMappingRuleConditions()
					.getCvMappingRuleCondition()) {
				if (cvMappingRule.getId().equals(ruleId)) {
					if ((valid && cvMappingRule.isValid()) || (!valid && !cvMappingRule.isValid())) {
						if (cvMappingRule.getObjectRuleToSkip() != null) {
							for (ObjectRuleToSkip objectRule : cvMappingRule.getObjectRuleToSkip()) {
								ret.add(objectRule.getId());
							}
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Add a collection of cv mapping rules identifiers to the list of cv
	 * mapping rules to skip
	 * 
	 * @param cvMappingRulesIdentifiers
	 */
	public void addCvMappingRulesToSkip(Collection<String> cvMappingRulesIdentifiers) {
		for (String cvRuleIdentifier : cvMappingRulesIdentifiers) {
			this.cvMappingRulesToSkip.add(cvRuleIdentifier);
		}
	}

	/**
	 * Add a collection of object rules identifiers to the list of object rules
	 * to skip
	 * 
	 * @param objectRulesIdentifiers
	 */
	public void addObjectRulesToSkip(Collection<String> objectRulesIdentifiers) {
		for (String objectRuleIdentifier : objectRulesIdentifiers) {
			this.objectRulesToSkip.add(objectRuleIdentifier);
		}
	}

	/**
	 * Add a CvRule to the collection of cv mapping rules to skip
	 * 
	 * @param cvMappingRule
	 */
	public void addCvMappingRuleToSkip(CvRule cvMappingRule) {
		this.cvMappingRulesToSkip.add(cvMappingRule.getId());
	}

	/**
	 * Add an object rule to the collection of object rules to skip
	 * 
	 * @param objectRule
	 */
	public void addObjectRuleToSkip(ObjectRule objectRule) {
		this.objectRulesToSkip.add(objectRule.getId());
	}

	/**
	 * Gets the list of identifiers of object rules to skip
	 * 
	 * @return
	 */
	public List<String> getObjectRulesToSkip() {
		return objectRulesToSkip;
	}

	/**
	 * Gets the list of identifiers of cv mapping rules to skip
	 * 
	 * @return
	 */
	public List<String> getCvMappingRulesToSkip() {
		return cvMappingRulesToSkip;
	}

	/**
	 * Check if it is necessary to add some rules to skip since the result of
	 * the execution of a cvRule
	 * 
	 * @param rule
	 * @param valid
	 *            if the rule has been passed or not
	 */
	public void updateRulesToSkipByCvMappingRuleResult(CvRule rule, boolean valid) {
		// get cvMappingRules that should be skipped
		final List<String> cvMappingRulesToSkipByCvMappingRule = this
				.getCvMappingRulesToSkipByCvMappingRule(rule.getId(), valid);
		if (cvMappingRulesToSkipByCvMappingRule != null
				&& !cvMappingRulesToSkipByCvMappingRule.isEmpty())
			this.addCvMappingRulesToSkip(cvMappingRulesToSkipByCvMappingRule);

		// get objectRules that should be skipped
		final List<String> objectRulesToSkipByCvMappingRule = this
				.getObjectRulesToSkipByCvMappingRule(rule.getId(), valid);
		if (objectRulesToSkipByCvMappingRule != null && !objectRulesToSkipByCvMappingRule.isEmpty())
			this.addObjectRulesToSkip(objectRulesToSkipByCvMappingRule);
	}

	/**
	 * Check if it is necessary to add some rules to skip since the result of
	 * the execution of a object rule
	 * 
	 * @param rule
	 * @param valid
	 *            if the rule has been passed or not
	 */
	public void updateRulesToSkipByObjectRuleResult(ObjectRule rule, boolean valid) {
		// get objectRules that should be skipped
		List<String> objectRulesToSkip = this.getObjectRulesToSkipByObjectRule(rule.getId(), valid);
		if (objectRulesToSkip != null && !objectRulesToSkip.isEmpty()) {
			this.objectRulesToSkip.addAll(objectRulesToSkip);
		}
		// get cvMappingRules that should be skipped
		List<String> cvMappingRulesToSkip = this.getCvMappingRulesToSkipByObjectRule(rule.getId(),
				valid);
		if (cvMappingRulesToSkip != null && !cvMappingRulesToSkip.isEmpty()) {
			this.cvMappingRulesToSkip.addAll(objectRulesToSkip);
		}

	}

	/**
	 * Restarts the list of rules to skip
	 */
	public void restartRulesToSkip() {
		// restart the rules to skip
		this.objectRulesToSkip = new ArrayList<String>();
		this.cvMappingRulesToSkip = new ArrayList<String>();
	}

	/**
	 * Depending of the user selections some object and cvMapping rules will be
	 * tagged to be skipped in the validator
	 * 
	 * @param selectedOptions
	 *            a set of pairs key, value, where the key is the identifier of
	 *            the condition and the value is the name of the chosen option
	 */
	public void filterRulesByUserOptions(HashMap<String, String> selectedOptions) {

		// get cv mapping identifiers to exclude
		this.addCvMappingRulesToSkip(getCVMappingRulesToSkipByUserOptions(selectedOptions));

		// get object rules identifiers to exclude
		this.addObjectRulesToSkip(getObjectRulesToSkipByUserOptions(selectedOptions));
	}

	/**
	 * Filters the list of ValidatorMessages checking each message with the
	 * lists of rules to skip and return the final list of messages
	 * 
	 * @return the collection of validation messages after the filter
	 */
	public Collection<ValidatorMessage> filterValidatorMessages(
			HashMap<String, List<ValidatorMessage>> msgs, ExtendedValidatorReport extendedReport) {
		ArrayList<ValidatorMessage> finalMessages = new ArrayList<ValidatorMessage>();

		// for each message, check if the rule that generated it is in the list
		// of rules to skip
		if (msgs != null && !msgs.isEmpty()) {
			
			for (String ruleIdentifier : msgs.keySet()) {
				boolean report = true;
				// if the rule that generated the messages is in the list of
				// rules to skip, not add to the final message list
				if (this.getObjectRulesToSkip() != null
						&& this.getObjectRulesToSkip().contains(ruleIdentifier))
					report = false;

				// if the rule that generated the messages is in the list of
				// rules to skip, not add to the final message list
				if (this.getCvMappingRulesToSkip() != null
						&& this.getCvMappingRulesToSkip().contains(ruleIdentifier))
					report = false;

				if (report) {
					finalMessages.addAll(msgs.get(ruleIdentifier));
				} else {
					// move the rule to the list of non checked rules
					extendedReport.setObjectRuleAsSkipped(ruleIdentifier);
				}
			}
		}
		return finalMessages;
	}

	public void printRuleFilter() {
		for (UserCondition condition : filter.getUserConditions().getUserCondition()) {
			System.out.println("\nCondition: " + condition.getId());
			for (UserOption option : condition.getUserOption()) {
				System.out.println("\tOption " + option.getId());
				for (CvMappingRuleToSkip cvMappingRule : option.getCvMappingRuleToSkip()) {
					System.out.println("\t\tmapping rule id: " + cvMappingRule.getId());
				}
				for (ObjectRuleToSkip objectRule : option.getObjectRuleToSkip()) {
					System.out.println("\t\tobject rule id: " + objectRule.getId());
				}
				if (option.getRulesToSkipRef() != null) {
					for (RulesToSkipRef ruleSetReference : option.getRulesToSkipRef()) {
						if (filter.getReferences() != null) {
							for (ReferencedRules referencedRuleSet : filter.getReferences()
									.getReferencedRules()) {
								if (referencedRuleSet.getId().equals(ruleSetReference.getRef())) {
									for (CvMappingRuleToSkip cvMappingRule : referencedRuleSet
											.getCvMappingRuleToSkip()) {
										System.out.println("\t\tmapping rule id: "
												+ cvMappingRule.getId());
									}
									for (ObjectRuleToSkip objectRule : referencedRuleSet
											.getObjectRuleToSkip()) {
										System.out.println("\t\tobject rule id: "
												+ objectRule.getId());
									}
								}
							}
						}
					}
				}
			}
		}
		if (filter.getObjectRuleConditions() != null) {
			System.out.println("\nObjectRuleConditions:");
			for (ObjectRuleCondition objectRuleCondition : filter.getObjectRuleConditions()
					.getObjectRuleCondition()) {
				System.out.println("\tObject rule condition: " + objectRuleCondition.getId()
						+ " isValid: " + objectRuleCondition.isValid());
				for (ObjectRuleToSkip objectRule : objectRuleCondition.getObjectRuleToSkip()) {
					System.out.println("\t\tobject rule to skip: " + objectRule.getId());
				}
				for (CvMappingRuleToSkip cvMappingRule : objectRuleCondition
						.getCvMappingRuleToSkip()) {
					System.out.println("\t\tcvMapping rule to skip: " + cvMappingRule.getId());
				}
			}
		}
		if (filter.getCvMappingRuleConditions() != null) {
			System.out.println("\ncvMappingRuleConditions:");
			for (CvMappingRuleCondition cvMappingCondition : filter.getCvMappingRuleConditions()
					.getCvMappingRuleCondition()) {
				System.out.println("\tcvMapping rule condition: " + cvMappingCondition.getId()
						+ " isValid: " + cvMappingCondition.isValid());
				for (ObjectRuleToSkip objectRule : cvMappingCondition.getObjectRuleToSkip()) {
					System.out.println("\t\tobject rule to skip: " + objectRule.getId());
				}
				for (CvMappingRuleToSkip cvMappingRule : cvMappingCondition
						.getCvMappingRuleToSkip()) {
					System.out.println("\t\tcvMapping rule to skip: " + cvMappingRule.getId());
				}
			}
		}
		if (filter.getMandatoryElements() != null) {
			System.out.println("\nMandatory elements:");
			for (MandatoryMzMLElement mandatorymzMLElement : filter.getMandatoryElements()
					.getMandatoryMzMLElement()) {
				System.out.println("\tMandatory mzML element: "
						+ mandatorymzMLElement.getMzMLElement());
			}
			for (MandatoryMzIdentMLElement mandatorymzIdentMLElement : filter
					.getMandatoryElements().getMandatoryMzIdentMLElement()) {
				System.out.println("\tMandatory mzIdentML element: "
						+ mandatorymzIdentMLElement.getMzIdentMLElement());
			}
		}
	}
}
