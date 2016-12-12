package com.ibm.research.proc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class IOUtils 
{
	public static List<String> readLines(InputStream in) throws IOException {
		final List<String> lines = new ArrayList<String>();
		processLines(in, new LineProcessor() {
			public void process(String line) {
				lines.add(line);
			}			
		});
		return lines;
	}
	
	public static List<String> readLinesIgnoreCarriageReturn(InputStream in) throws IOException {
		final List<String> lines = new ArrayList<String>();
		processLinesIgnoreCarriageReturn(in, new LineProcessor() {
			public void process(String line) {
				lines.add(line);
			}			
		});
		return lines;
	}
	
	public static void processLines(InputStream in, LineProcessor processor) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null) {
			processor.process(line);
		}
		br.close();
	}
	
	public static void processLinesIgnoreCarriageReturn(InputStream in, LineProcessor processor) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer line = new StringBuffer();
		int i;
		 while ((i = br.read()) != -1) {
			char c = (char) i;
			if (c == '\r') {
				// Ignore
			} else if (c == '\n') {
				// New line
				processor.process(line.toString());
				line = new StringBuffer();
			} else {
				line.append(c);
			}
		} 
		// Flush last line
		processor.process(line.toString());
		br.close();
	}
}
