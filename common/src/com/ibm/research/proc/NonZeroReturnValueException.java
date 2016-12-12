package com.ibm.research.proc;

import java.util.List;

public class NonZeroReturnValueException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public NonZeroReturnValueException(String errorOutput) {
		super(errorOutput);
	}

	public NonZeroReturnValueException(List<String> command, int exitValue) {
		super("Exit value: " + exitValue + " " + command.toString());
	}
	
}
