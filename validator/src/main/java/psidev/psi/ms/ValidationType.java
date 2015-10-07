package psidev.psi.ms;

public enum ValidationType {
	MIAPE_VALIDATION("MIAPE-compliant validation"), SEMANTIC_VALIDATION("Semantic validation");
	private final String name;

	ValidationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
