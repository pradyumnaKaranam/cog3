package com.ibm.research.cogassist.qprocessor.util;

public class ConsoleReader implements InputReader {

	@Override
	public String readLine() {
		return System.console().readLine();
	}

	@Override
	public String readLine(String prompt) {
		return System.console().readLine(prompt + ": ");
	}

	@Override
	public String readLine(String prompt, String defaultValue) {
		String value = System.console().readLine(prompt + " (%s): ", defaultValue);
		if (value != null && value.length() == 0) {
			return defaultValue;
		} else {
			return value;
		}
	}

	@Override
	public String readPassword() {
		return new String(System.console().readPassword());
	}

	@Override
	public String readPassword(String prompt) {
		return new String(System.console().readPassword(prompt + ": "));
	}

}
