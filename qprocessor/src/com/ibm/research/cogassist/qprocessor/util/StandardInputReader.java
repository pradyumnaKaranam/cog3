package com.ibm.research.cogassist.qprocessor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StandardInputReader implements InputReader {

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	@Override
	public String readLine() throws IOException {
		return br.readLine();
	}

	@Override
	public String readLine(String prompt) throws IOException {
		System.out.print(prompt + ": ");
		return br.readLine();
	}
	
	@Override
	public String readLine(String prompt, String defaultValue) throws IOException {
		System.out.print(prompt + " (" + defaultValue + "): ");
		String value = br.readLine();
		if (value != null && value.length() == 0) {
			return defaultValue;
		} else {
			return value;
		}
	}

	@Override
	public String readPassword() throws IOException {
		return readLine();
	}

	@Override
	public String readPassword(String prompt) throws IOException {
		return readLine(prompt);
	}

}
