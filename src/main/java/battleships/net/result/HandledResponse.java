package battleships.net.result;

import java.io.Serializable;

/**
 * Use when last Reponse was handled, this will be ignored
 */
public class HandledResponse extends Response implements Serializable {

	private static final long serialVersionUID = 1L;

	public HandledResponse() {
		super(null);

	}

	@Override
	public String toString() {
		return "HandledResponse";
	}

}
