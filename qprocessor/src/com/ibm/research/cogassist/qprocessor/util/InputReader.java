package com.ibm.research.cogassist.qprocessor.util;

import java.io.IOException;

public interface InputReader {
	public String readLine() throws IOException;
	public String readLine(String prompt) throws IOException;
	public String readLine(String prompt, String defaultValue) throws IOException;
	public String readPassword() throws IOException;
	public String readPassword(String prompt) throws IOException;
}
