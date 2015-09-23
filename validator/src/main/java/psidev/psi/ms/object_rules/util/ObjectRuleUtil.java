package psidev.psi.ms.object_rules.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ebi.jmzml.model.mzml.CVParam;

public class ObjectRuleUtil {
	/**
	 * Check if any of the cvParams is one of the accessions
	 * 
	 * @param cvParams
	 * @param accessions
	 * @return The list of {@link CVParam} found, or an empty list if not found
	 **/
	public static List<CVParam> checkAccessionsInCVParams(List<CVParam> cvParams,
			Collection<String> accessions) {
		List<CVParam> ret = new ArrayList<CVParam>();
		if (cvParams != null && accessions != null)

			for (String accession : accessions) {
				CVParam cvParam = checkAccessionsInCVParams(cvParams, accession);
				if (cvParam != null)
					ret.add(cvParam);
			}
		return ret;
	}

	/**
	 * Check if any of the cvParams is the accession
	 * 
	 * @param cvParams
	 * @param accession
	 * @return The CVParam found or null if not found
	 */
	public static CVParam checkAccessionsInCVParams(List<CVParam> cvParams, String accession) {
		if (cvParams != null && accession != null)
			for (CVParam cvParam : cvParams) {
				if (accession.equals(cvParam.getAccession()))
					return cvParam;
			}
		return null;
	}
}
