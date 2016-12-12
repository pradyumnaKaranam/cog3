package com.ibm.research.cogassist.kg.titandb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.DatabaseManager;
import com.ibm.research.cogassist.json.JSONArray;
import com.ibm.research.cogassist.json.JSONObject;
import com.ibm.research.cogassist.kg.ChangeTense;
import com.ibm.research.cogassist.kg.EdgeType;
import com.ibm.research.cogassist.kg.NodeType;
import com.thinkaurelius.titan.core.Order;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class SearchTitanDB {

	public static JSONArray getAllVertices() {
		JSONArray arr = new JSONArray();
		TitanGraph graph = TitanDatabaseManager.getDataSource();

		for (Vertex vertex : graph.getVertices()) {
			JSONObject obj = new JSONObject();
			obj.put("name", vertex.getProperty(TitanDBProperties.VERTEX_NAME));
			obj.put("type", vertex.getProperty(TitanDBProperties.VERTEX_TYPE));
			obj.put("freq", vertex.getProperty(TitanDBProperties.VERTEX_WEIGHT));
			arr.add(obj);
		}
		TitanDatabaseManager.shutdownGraph();
		return arr;
	}

	public static void test() {
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		// DatabaseManager.createGraph();
		/*
		 * Iterable<Result<Vertex>> results = graph.indexQuery("vertexprops",
		 * "v.name:transfer order").vertices();
		 * System.out.println(results.toString()); for(Result<Vertex> result :
		 * results) {
		 * System.out.println(result.getElement().getProperty("name").
		 * toString()); }
		 */
		// DatabaseManager.shutdownGraph();
		// graph.commit();

		/*
		 * Iterable<CacheVertex> results =
		 * graph.query().has("name",Text.CONTAINS,"transfer order").vertices();
		 * for(CacheVertex result : results) {
		 * System.out.println(result.getProperty("name").toString()); }
		 */

		GremlinPipeline results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, "transfer order").vertices());
		for (Object e : results) {
			Vertex v = (Vertex) e;
			Iterable<Edge> edges = v.query().edges();
			for (Edge edge : edges)
				System.out.println(edge.getLabel());
		}

	}
	
	
	public static JSONObject getJSONObject(Edge edge, Vertex node, List<String> targets, String displayLabel) throws IOException, SQLException{
		JSONObject obj = new JSONObject();
		ChangeTense ct = ChangeTense.getInstance();
		Vertex targetNode = edge.getVertex(Direction.IN);
		if(!node.equals(edge.getVertex(Direction.OUT)))
			targetNode = edge.getVertex(Direction.OUT);
		String target = targetNode.getProperty(TitanDBProperties.VERTEX_NAME);
		JSONArray targetArr = new JSONArray();
		targetArr.addAll(targets);
		targetArr.add(edge.getLabel());
		targetArr.add(target);
		
		displayLabel = displayLabel+" "+ct.changeTense(edge.getProperty(TitanDBProperties.EDGE_DISPLAY_NAME))+" "+target;
		
		obj.put("confidence", targetNode.getProperty(TitanDBProperties.VERTEX_WEIGHT));
		String type = targetNode.getProperty(TitanDBProperties.VERTEX_TYPE);

		if (type.equalsIgnoreCase(NodeType.ERROR_CODE.toString())){
			DataSource ds = DatabaseManager.getDataSource();
			displayLabel = CogAssist.getErrorMessage(ds, target, true);
		}else if(type.equalsIgnoreCase(NodeType.CURATED_CONTENT.toString())) {
			DataSource ds = DatabaseManager.getDataSource();
			displayLabel = CogAssist.getErrorMessage(ds, target, false);
		}
		obj.put("displaylabel", displayLabel);
		obj.put("path", targetArr);
		obj.put("type", type);
		return obj;
	}
	
	public static JSONObject getJSONObject(Vertex node, String action, List<String> targets, String displayLabel) throws IOException, SQLException{
		ChangeTense ct = ChangeTense.getInstance();
		String rootLabel = ct.getMultiRootForm(action);
		JSONObject obj = new JSONObject();
		String target = node.getProperty(TitanDBProperties.VERTEX_NAME);
		JSONArray targetArr = new JSONArray();
		targetArr.addAll(targets);
		targetArr.add(rootLabel);
		targetArr.add(target);
		
		displayLabel = displayLabel+" "+ct.changeTense(action)+" "+target;
		
		obj.put("confidence", node.getProperty(TitanDBProperties.VERTEX_WEIGHT));

		String type = node.getProperty(TitanDBProperties.VERTEX_TYPE);

		if (type.equalsIgnoreCase(NodeType.ERROR_CODE.toString())){
			DataSource ds = DatabaseManager.getDataSource();
			displayLabel = CogAssist.getErrorMessage(ds, target, true);
		}else if(type.equalsIgnoreCase(NodeType.CURATED_CONTENT.toString())) {
			
			DataSource ds = DatabaseManager.getDataSource();
			displayLabel = CogAssist.getErrorMessage(ds, target, false);
		}
		
		obj.put("displaylabel", displayLabel);
		obj.put("path", targetArr);
		obj.put("type", type);
		return obj;
	}
	
	public static JSONObject getFinalObject(){
		JSONObject obj = new JSONObject();
		obj.put(NodeType.CONCEPT.toString(), new JSONArray());
		obj.put(NodeType.CURATED_CONTENT.toString(), new JSONArray());
		obj.put(NodeType.ERROR_CODE.toString(), new JSONArray());
		obj.put(NodeType.NOUN.toString(), new JSONArray());
		return obj;
	}
	
	public static JSONObject getRelatedSuggestions(String concept, String action, String projectid, String language) throws SQLException, IOException {
		JSONObject finalObject = getFinalObject();
		ChangeTense ct = ChangeTense.getInstance();
		String rootLabel = ct.getMultiRootForm(action);
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline results;
		try{
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices()).both(rootLabel);
		}catch(Exception e){
			TitanDatabaseManager.shutdownGraph();
			graph = TitanDatabaseManager.getDataSource();
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices()).both(rootLabel);
		}
		for (Object result : results) {
			Vertex node = (Vertex) result;
			JSONObject obj = getJSONObject(node, action, Arrays.asList(concept, action), concept+" "+action);
			
			JSONArray arr = (JSONArray) finalObject.get(obj.getString("type"));
			arr.add(obj);
		}
		
		//graph.shutdown();
		return finalObject;
	}
	
	public static JSONObject getRelatedSuggestions(String concept, String action, List<String> conceptList, String projectid, String language, String displayLabel) throws SQLException, IOException {
		ChangeTense ct = ChangeTense.getInstance();
		String rootLabel = ct.getMultiRootForm(action);
		JSONObject finalObject = getFinalObject();
		TitanGraph graph = TitanDatabaseManager.getDataSource();GremlinPipeline results;
		try{ 
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices()).both(rootLabel);
		}catch(Exception e){
			TitanDatabaseManager.shutdownGraph();
			graph = TitanDatabaseManager.getDataSource();
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices()).both(rootLabel);
		}

		for (Object result : results) {
			Vertex node = (Vertex) result;
			if (!conceptList.contains(node.getProperty(TitanDBProperties.VERTEX_NAME))) {
				JSONObject obj = getJSONObject(node, action, conceptList, displayLabel);
				JSONArray arr = (JSONArray) finalObject.get(obj.getString("type"));
				arr.add(obj);
			}
		}
		//graph.shutdown();
		return finalObject;
	}
	
	public static JSONObject getRelatedErrorCodes(String concept, String action, List<String> conceptList, String projectid, String language,String displayLabel) throws SQLException, IOException {
		ChangeTense ct = ChangeTense.getInstance();
		String rootLabel = ct.getMultiRootForm(action);
		JSONObject finalObject = getFinalObject();
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline results;
		try{ 
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices()).both(rootLabel).has(TitanDBProperties.EDGE_TYPE,EdgeType.CONCEPT_ERRORCODE);
		}catch(Exception e){
			TitanDatabaseManager.shutdownGraph();
			graph = TitanDatabaseManager.getDataSource();
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices()).both(rootLabel).has(TitanDBProperties.EDGE_TYPE,EdgeType.CONCEPT_ERRORCODE);
		}

		for (Object result : results) {
			Vertex node = (Vertex) result;
			if (!conceptList.contains(node.getProperty(TitanDBProperties.VERTEX_NAME))) {
				JSONObject obj = getJSONObject(node, action, conceptList, displayLabel);
				JSONArray arr = (JSONArray) finalObject.get(obj.getString("type"));
				arr.add(obj);
			}
		}
		//graph.shutdown();
		return finalObject;
	}
	
	
	public static JSONObject getRelatedSuggestions(String concept, String projectid, String language) throws SQLException, IOException {
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline pipe = new GremlinPipeline();
		JSONObject finalObject = getFinalObject();
		GremlinPipeline results;
		try{
			results = pipe.start(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices());
		}catch(Exception e){
			TitanDatabaseManager.shutdownGraph();
			graph = TitanDatabaseManager.getDataSource();
			results = pipe.start(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices());
		}
		for (Object result : results) {
			Vertex node = (Vertex) result;
			System.out.println(node.getProperty(TitanDBProperties.VERTEX_NAME).toString());
			Iterable<Edge> edges = node.getEdges(Direction.BOTH);
			for (Edge edge : edges) {
				if (!edge.getLabel().equals("-")) {
					JSONObject obj = getJSONObject(edge, node, Arrays.asList(concept), concept);
					JSONArray arr = (JSONArray) finalObject.get(obj.getString("type"));
					arr.add(obj);
				}
			}
		}
		//graph.shutdown();
		return finalObject;
	}

	public static JSONObject getRelatedSuggestions(String concept, List<String> conceptList, String projectid, String language, String displayLabel) throws SQLException, IOException {
		JSONObject finalObject = getFinalObject();
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline results;
		try{
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices());
		}catch(Exception e){
			TitanDatabaseManager.shutdownGraph();
			graph = TitanDatabaseManager.getDataSource();
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices());
		}
		for (Object result : results) {
			Vertex node = (Vertex) result;
			Iterable<Edge> edges = node.query().edges();
			if (!conceptList.contains(node.getProperty(TitanDBProperties.VERTEX_NAME))) {
				for (Edge edge : edges) {
					if (!edge.getLabel().equals("-") && !conceptList.contains(edge.getLabel())) {
						JSONObject obj = getJSONObject(edge, node, conceptList, displayLabel);
						JSONArray arr = (JSONArray) finalObject.get(obj.getString("type"));
						arr.add(obj);
					}
				}
			}

		}
		//graph.shutdown();
		return finalObject;
	}
	
	
	
	public static JSONObject getRelatedErrorCodes(String concept, List<String> conceptList, String projectid, String language, String displayLabel) throws SQLException, IOException {
		JSONObject finalObject = getFinalObject();
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline results;
		try{
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices());
		}catch(Exception e){
			TitanDatabaseManager.shutdownGraph();
			graph = TitanDatabaseManager.getDataSource();
			results = new GremlinPipeline(graph.query().has(TitanDBProperties.VERTEX_NAME, Text.CONTAINS, concept).has(TitanDBProperties.VERTEX_PROJECT_ID, projectid).has(TitanDBProperties.VERTEX_LANGUAGE, language).orderBy(TitanDBProperties.VERTEX_WEIGHT, Order.DESC).vertices());
		}
		for (Object result : results) {
			Vertex node = (Vertex) result;
			Iterable<Edge> edges = node.query().edges();
			if (!conceptList.contains(node.getProperty(TitanDBProperties.VERTEX_NAME))) {
				for (Edge edge : edges) {
					if (!edge.getLabel().equals("-") && edge.getProperty(TitanDBProperties.EDGE_TYPE).equals(EdgeType.CONCEPT_ERRORCODE) && !conceptList.contains(edge.getLabel())) {
						JSONObject obj = getJSONObject(edge, node, conceptList, displayLabel);
						JSONArray arr = (JSONArray) finalObject.get(obj.getString("type"));
						arr.add(obj);
					}
				}
			}

		}
		//graph.shutdown();
		return finalObject;
	}
	
	public static void reCreateGraph(){
		CreateTitanGraph ctg = new CreateTitanGraph(); 
		ctg.deleteGraph();
		ctg.createSchema();
	}

	public static void changeProperties(){
		CreateTitanGraph ctg = new CreateTitanGraph(); 
		ctg.changeProperties();
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		// System.out.println(getAllVertices());
		// test();
		//System.out.println(getRelatedSuggestions("transfer order", "creating", "1").toString());
		System.out.println(getRelatedSuggestions("transfer order", "1", "en").toString());
		//reCreateGraph();
	}
}
