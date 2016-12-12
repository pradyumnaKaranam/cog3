package com.ibm.research.cogassist.kg;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GetConfidence {

	public static Integer confidence(int freq, int maxFreq) {
		return freq+maxFreq;
	}

	/**
	 * Confidence in case of errormessage based on the path already traversered and error message
	 * based on the number of words matched.
	 * @param target
	 * @param path
	 * @return
	 */
	public static int confidence(String target, List<String> path) {
		List<String> paths= new LinkedList<String>();
		for (String pat : path){
			paths.addAll(Arrays.asList(pat.split(" ")));
		}
			
		List<String> errorMessage = new LinkedList<String>(Arrays.asList(target.split(" ")));
		int totalSize = errorMessage.size();
		errorMessage.retainAll(paths);
		if (errorMessage.size() == 0){
			return -1;
		}
		return errorMessage.size()*100/totalSize;
	}

}
