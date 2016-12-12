package com.ibm.research.cogassist.kgmigration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.DatabaseManager;
import com.ibm.research.cogassist.kg.titandb.CreateTitanGraph;
import com.ibm.research.cogassist.kg.titandb.TitanDatabaseManager;
import com.ibm.research.cogassist.kg.utils.ChangeTense;
import com.ibm.research.cogassist.kg.utils.Constants;
import com.ibm.research.cogassist.kg.utils.EdgeType;
import com.ibm.research.cogassist.kg.utils.NodeType;
import com.ibm.research.cogassist.kg.utils.PorterStemmer;
import com.ibm.research.cogassist.kg.utils.SentenceParsing;
import com.thinkaurelius.titan.core.TitanGraph;

public class KnowledgeGraphIngestor {

	private Map<String, Integer> nounFreqMap = new HashMap<String, Integer>();
	private Map<String, List<Object>> nodesMap = new LinkedHashMap<String, List<Object>>();
	private List<List<Object>> edgesList = new ArrayList<List<Object>>();
	private int nodeId = 0;
	private int errorId = 0;
	//private String errorCode = "";
	
	

	
	public static void main(String[] args) throws IOException, SQLException {

		String projectid=CogAssist.getProperty("projectid");
		KnowledgeGraphIngestor kgi = new KnowledgeGraphIngestor();
		System.out.println("Running kg for "+projectid);
		kgi.createKg(projectid, "en");
	}
	
	
	

	

	public String createKg(String projectid, String language) throws SQLException, IOException{
		int count = 0;
		List<String> errorCodeMap = CogAssist.getErrorCodesMap(DatabaseManager.getDataSource(), projectid);
		String path = new File(System.getProperty("java.io.tmpdir")).getCanonicalPath()+File.separator;
		System.out.println("Started creatingKg  " + path);
		PrintWriter writer = new PrintWriter(path+"code_out.txt", "UTF-8");
		for(String error : errorCodeMap) {
            System.out.println(error);
            String[] errorArray=error.split(":");
            String errorcode="";
            String errorMessage="";
            if(errorArray.length==2)
            {
			 errorcode = CogAssist.removeSpecialCharacters(errorArray[0]);
			 errorMessage = CogAssist.removeSpecialCharacters(errorArray[1]);
            }
            else
            	errorcode=CogAssist.removeSpecialCharacters(errorArray[0]);
            
			count++;
			if(!nodesMap.containsKey(errorcode)){
				nodeId++;
				nodesMap.put(errorcode, Arrays.asList(nodeId, errorcode, NodeType.ERROR_CODE.toString()));
			} else {
				nodeId = (int) nodesMap.get(errorcode).get(0);
			}
			errorId = nodeId;
			//errorCode = errorcode;
			SentenceParsing sp = SentenceParsing.getInstance();
			// Map<String, List<Map<String, List<String>>>> map =
			// sp.getAllParses(sample.split(",")[1]);
			//writer.println(errorcode+","+errorMessage);
			List<Map<String, List<String>>> list = sp.getDependencyParse(errorMessage);
			//writer.print(sample.split(",")[0]);
			//writer.println(list);
			pushNodesAndEdges(list, writer);
			if(count%25000 == 0)
				System.out.println("Createing Kg... "+count +"  "+(new Date()));
		}
		System.out.println("Completed creatingKg");
		//populateDBTable(projectid, user);
		//printNodesEdges(path, projectid);
		//loadNodesEdgesToDB(projectid, user, path);
		loadNodesEdgesToTitan(writer, projectid, language);
		return "true";
	}
	
	private Map<String, String> getNodeMap() {
		Map<String, String> nodeMap = new HashMap<>();
		for (String key: nodesMap.keySet())
			nodeMap.put(nodesMap.get(key).get(0).toString(), nodesMap.get(key).get(1).toString());
		return nodeMap;
	}
	
	private void loadNodesEdgesToTitan(PrintWriter writer, String projectid, String language) throws IOException, SQLException {
		System.out.println("****************************Started pushing the graph to Titan DB");
		System.out.println("*****Loading Titan related jar Files.");
		CreateTitanGraph ctg = new CreateTitanGraph();
		//SearchTitanDB.reCreateGraph();  //Deleting the graph and recreating the graph everytime.
		ChangeTense ct = ChangeTense.getInstance();
		Map<String, String> nodeMap = getNodeMap();
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		System.out.println("*****Loaded Titan related jar Files.");
		for(List<?> l : edgesList) {
			Map<String, String> source = new HashMap<String, String>();
			Map<String, String> target = new HashMap<String, String>();
			Map<String, String> edge = new HashMap<String, String>();
			source.put("name", nodeMap.get(l.get(0).toString()));
			target.put("name", nodeMap.get(l.get(1).toString()));
			int sourceFreq = getNodeFreq(nodeMap.get(l.get(0).toString()));
			int targetFreq = getNodeFreq(nodeMap.get(l.get(1).toString()));
			
			source.put("freq",String.valueOf(sourceFreq));
			target.put("freq",String.valueOf(targetFreq));
			String sourceType = getNodeType(nodeMap.get(l.get(0).toString()));
			String targetType = getNodeType(nodeMap.get(l.get(1).toString()));
			source.put("type",sourceType);
			target.put("type", targetType);
			
			source.put("projectid",projectid);
			target.put("projectid", projectid);
			
			source.put("language", language);
			target.put("language", language);
			
			edge.put("displayName", l.get(2).toString());
			edge.put("label", ct.getMultiRootForm(l.get(2).toString().trim()));
			edge.put("type", l.get(3).toString());
			edge.put("language", language);
			writer.println(source.toString()+"  "+target.toString()+"  "+edge.toString());
			ctg.addAnEdge(source, target, edge, graph);
		}
		graph.commit();
		graph.shutdown();
		System.out.println("****************************Pushed the graph to Titan DB");
	}
	
	private int getNodeFreq(Object index) {
		if (nounFreqMap.containsKey(index))
			return nounFreqMap.get(index);
		return 0;
	}

	private String getNodeType(Object index) {
		String type = NodeType.ERROR_CODE.toString();
		if (nounFreqMap.containsKey(index))
			if( nounFreqMap.get(index) > 3)
				type = NodeType.CONCEPT.toString();
			else
				type = NodeType.NOUN.toString();
		return type;
	}
	
	private void pushNodesAndEdges(List<Map<String, List<String>>> list, PrintWriter writer) {
		for (Map<String, List<String>> m : list) {
			if(m.get("actions") != null && m.get("actions").size() != 0){
			String action = m.get("actions").get(0);
			List<String> nounList = m.get("nouns");
			//ChangeTense ct = ChangeTense.getInstance();
			
			if (!"".equals(action)) {
				for (String n : nounList) {
					String key = PorterStemmer.stem(n.toLowerCase());
					if (!(n.split(" ").length == 1 && n.toLowerCase().endsWith("ing")) && !Constants.stopWords.contains(n.toLowerCase()) && !Constants.stopWords.contains(key)) {
						/*if (!nounFreqMap.containsKey(key))
							nounFreqMap.put(key, 0);
						nounFreqMap.put(key, nounFreqMap.get(key) + 1);*/
						try{
							nounFreqMap.put(key, nounFreqMap.get(key) + 1);
						} catch(NullPointerException e){
							nounFreqMap.put(key, 1);
						}
						
						try{
							edgesList.add(Arrays.asList(errorId, nodesMap.get(key).get(0), action, EdgeType.CONCEPT_ERRORCODE.toString()));
						} catch(NullPointerException e){
							nodeId++;
							nodesMap.put(key, Arrays.asList(nodeId, n.toLowerCase(), NodeType.CONCEPT.toString()));
							edgesList.add(Arrays.asList(errorId, nodesMap.get(key).get(0), action, EdgeType.CONCEPT_ERRORCODE.toString()));
						}
						//writer.println(key+"  "+errorId+"  "+nodesMap.get(key).get(0));
					}
				}
				if (nounList.size() > 1)
					getRelationalEdges(nounList, action);
			}
		}
		}
	}

	private void getRelationalEdges(List<String> nounList, String action) {
		//ChangeTense ct = ChangeTense.getInstance();
		for (int i = 0; i < nounList.size(); i++)
			for (int j = i + 1; j < nounList.size(); j++) {
				String key1 = nounList.get(i).toLowerCase();
				String key2 = nounList.get(j).toLowerCase();
				if (!(key1.split(" ").length == 1 && key1.endsWith("ing")) && !Constants.stopWords.contains(key1) && !Constants.stopWords.contains(PorterStemmer.stem(key1))
						&& !(key2.split(" ").length == 1 && key2.endsWith("ing")) && !Constants.stopWords.contains(key2) && !Constants.stopWords.contains(PorterStemmer.stem(key2))) {
					edgesList.add(Arrays.asList(nodesMap.get(PorterStemmer.stem(key1)).get(0), nodesMap.get(PorterStemmer.stem(key2)).get(0), action, EdgeType.CONCEPT_CONCEPT.toString()));
				}
			}

	}

}
