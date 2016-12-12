package com.ibm.research.cogassist.kg.titandb;

import java.io.File;

import com.ibm.research.cogassist.common.CogAssist;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;

public class TitanDatabaseManager {
	private static TitanGraph graph;

	public static TitanGraph getDataSource() {
		if(graph == null || graph.isClosed())
			return getTitanGraph();
		else 
			return graph;
	}
	
	private static TitanGraph getTitanGraph() {
		try {
			/*ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			String path = classLoader.getResource("titan-cassandra.properties").getPath();
			System.out.println(path);*/
			String path = CogAssist.getHomeDirectoryName()+File.separator+"titan-cassandra.properties";
			//String path = "/Users/neelamadhav/IBM/ca/home/titan-cassandra.properties";
			if(new File(path).exists())
				graph = TitanFactory.open(path);
			/*graph = TitanFactory.build().set("storage.backend", "cassandra")
					.set("storage.hostname", "irlbxvm098.irl.in.ibm.com")
					.set("index.search.backend","lucene")
					.set("index.search.directory","/home/neelamadhav/titan/index-data").open();*/
			else
				System.out.println("File doesnot exist...."+path);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	@SuppressWarnings("unused")
	private void commitGraph() {
		graph.commit();
	}
	
	public static void shutdownGraph() {
		graph.commit();
		graph.shutdown();
	}
}
