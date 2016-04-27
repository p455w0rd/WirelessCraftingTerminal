package net.p455w0rd.wirelesscraftingterminal.api.exceptions;

public class MissingIngredientError extends Exception {

	private static final long serialVersionUID = -998858343831371697L;

	public MissingIngredientError(final String n) {
		super(n);
	}
}
