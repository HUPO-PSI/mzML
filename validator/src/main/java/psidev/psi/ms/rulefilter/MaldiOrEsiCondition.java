package psidev.psi.ms.rulefilter;

public enum MaldiOrEsiCondition {
	MALDI("MALDI"), ESI("ESI"), OTHER("OTHER");
	private final String option;

	public static String getID() {
		return "MALDI_OR_ESI";
	}

	MaldiOrEsiCondition(String option) {
		this.option = option;
	}

	public String getOption() {
		return this.option;
	}
}
