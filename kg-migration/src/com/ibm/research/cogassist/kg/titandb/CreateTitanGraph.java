package com.ibm.research.cogassist.kg.titandb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;





public class CreateTitanGraph {
	public void createSchema() {

		System.out.println("Creating fresh schema");

		TitanGraph graph = TitanDatabaseManager.getDataSource();

		TitanManagement mgmt = graph.getManagementSystem();
		
		
		PropertyKey name = mgmt.makePropertyKey(TitanDBProperties.VERTEX_NAME).dataType(String.class).make();		
		PropertyKey type = mgmt.makePropertyKey(TitanDBProperties.VERTEX_TYPE).dataType(String.class).make();
		PropertyKey freq = mgmt.makePropertyKey(TitanDBProperties.VERTEX_WEIGHT).dataType(Integer.class).make();
		PropertyKey projectid = mgmt.makePropertyKey(TitanDBProperties.VERTEX_PROJECT_ID).dataType(Integer.class).make();
		PropertyKey language = mgmt.makePropertyKey(TitanDBProperties.VERTEX_LANGUAGE).dataType(String.class).make();
		mgmt.makePropertyKey(TitanDBProperties.VERTEX_SOURCE).dataType(String.class).make();
		mgmt.makePropertyKey(TitanDBProperties.VERTEX_TIMESTAMP).dataType(String.class).make();
		mgmt.makePropertyKey(TitanDBProperties.VERTEX_TAGS).dataType(String.class).make();
		mgmt.makePropertyKey(TitanDBProperties.VERTEX_MODULE).dataType(String.class).make();
		mgmt.makePropertyKey(TitanDBProperties.VERTEX_VISIT_COUNT).dataType(Integer.class).make();
		
		//mgmt.makeEdgeLabel("label").multiplicity(Multiplicity.MULTI).make();

		
		mgmt.buildIndex("vertexprops", Vertex.class).addKey(name,com.thinkaurelius.titan.core.schema.Parameter.of("mappedname",TitanDBProperties.VERTEX_NAME)).addKey(type).addKey(freq).addKey(projectid).addKey(language).buildMixedIndex("search");
		mgmt.buildIndex("edgeprops", Vertex.class).addKey(name).addKey(type).addKey(freq).buildMixedIndex("search");

		mgmt.commit();
		
		graph.commit();
		graph.shutdown();

	}
	
	
	
	public void addAnEdge(Map<String, String> source, Map<String, String> target, Map<String, String> relation, TitanGraph graph){
		
		Vertex sourceNode = GraphUtils.getVertex(source);
		Vertex targetNode = GraphUtils.getVertex(target);
		
	    if(sourceNode == null)
	    	sourceNode = createNode(graph, source);
	    else
	    	updateWeight(sourceNode, source.get("freq"));
	    if(targetNode == null)
	    	targetNode = createNode(graph, target);
	    else 
	    	updateWeight(targetNode, target.get("freq"));
	    
	    if(GraphUtils.isNeighbor(sourceNode,  targetNode, relation.get("label"), source.get("projectid"), relation.get("language")))
	    		return;
		Edge e1 = graph.addEdge(null, sourceNode, targetNode, relation.get("label"));
		Edge e = setEdgeProperties(e1, source, target, relation, graph );
		
		System.out.println(e.getVertex(Direction.OUT).getProperty(TitanDBProperties.VERTEX_NAME) + "--" + e.getLabel() + "-->" + e.getVertex(Direction.IN).getProperty(TitanDBProperties.VERTEX_NAME));
	}
	
	

	private void updateWeight(Vertex node, String currentWeight) {
		int prevWeight = node.getProperty(TitanDBProperties.VERTEX_WEIGHT);
		node.setProperty(TitanDBProperties.VERTEX_WEIGHT, prevWeight+Integer.valueOf(currentWeight)); 
		
	}

	private Edge setEdgeProperties(Edge e,Map<String, String> source, Map<String, String> target, Map<String, String> relation, TitanGraph graph){
		Date date = new Date();
	    SimpleDateFormat format = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
	    
		e.setProperty(TitanDBProperties.EDGE_DISPLAY_NAME, relation.get("displayName"));
		e.setProperty(TitanDBProperties.EDGE_TYPE, relation.get("type"));
		e.setProperty(TitanDBProperties.EDGE_PROJECT_ID, source.get("projectid"));
		e.setProperty(TitanDBProperties.EDGE_TYPE, getEdgeType(source.get("type"), target.get("type")));
		e.setProperty(TitanDBProperties.EDGE_TIMESTAMP, format.format(date));
		if(relation.containsKey("tags"))
			e.setProperty(TitanDBProperties.EDGE_TAGS, source.get("tags"));
		if(relation.containsKey("module"))
			e.setProperty(TitanDBProperties.EDGE_MODULE, source.get("module"));
		e.setProperty(TitanDBProperties.EDGE_LANGUAGE, relation.get("language"));
		e.setProperty(TitanDBProperties.EDGE_VISIT_COUNT, 0);
		e.setProperty(TitanDBProperties.EDGE_SOURCE, "icurate");
		e.setProperty(TitanDBProperties.EDGE_UP_VOTES, 0);
		e.setProperty(TitanDBProperties.EDGE_DOWN_VOTES, 0);
		if(relation.containsKey("sourceRole"))
			e.setProperty(TitanDBProperties.EDGE_SOURCE_ROLE, source.get("sourceRole"));
		if(relation.containsKey("targetRole"))
			e.setProperty(TitanDBProperties.EDGE_TARGET_ROLE, source.get("targetRole"));
		return e;
	}


	private Vertex createNode(TitanGraph graph, Map<String, String> propertyMap) {
		Vertex node = graph.addVertex(null);
		Date date = new Date();
	    SimpleDateFormat format = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
	    
		node.setProperty(TitanDBProperties.VERTEX_NAME, propertyMap.get("name"));
		node.setProperty(TitanDBProperties.VERTEX_TYPE, propertyMap.get("type"));
		node.setProperty(TitanDBProperties.VERTEX_WEIGHT, Integer.parseInt(propertyMap.get("freq")));
		node.setProperty(TitanDBProperties.VERTEX_PROJECT_ID, propertyMap.get("projectid"));
		node.setProperty(TitanDBProperties.VERTEX_SOURCE, "icurate");
		node.setProperty(TitanDBProperties.VERTEX_TIMESTAMP, format.format(date));
		node.setProperty(TitanDBProperties.VERTEX_VISIT_COUNT, 0);
		if(propertyMap.containsKey("tags"))
			node.setProperty(TitanDBProperties.VERTEX_TAGS, propertyMap.get("tags"));
		if(propertyMap.containsKey("module"))
			node.setProperty(TitanDBProperties.VERTEX_MODULE, propertyMap.get("module"));
		node.setProperty(TitanDBProperties.VERTEX_LANGUAGE, propertyMap.get("language"));
		return node;
	}



	private Object getEdgeType(String sourceType, String targetType) {
		List<String> nodeType = Arrays.asList("NOUN", "SAP_CONCEPT");
		if(nodeType.contains(sourceType) && nodeType.contains(sourceType))
			return EdgeType.CONCEPT_CONCEPT.toString();
		
		return EdgeType.CONCEPT_ERRORCODE.toString();
	}

	public void deleteGraph(){
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		graph.shutdown();
		TitanCleanup.clear(graph);
		graph.shutdown();
		/*Iterable<Edge> edges = graph.getEdges();
		for(Edge edge : edges)
			edge.remove();
		Iterable<Vertex> vertices = graph.getVertices();
		for(Vertex vertex : vertices)
			vertex.remove();
		graph.commit();*/
	}
	
	public void createGraphFromFile(String filePath) throws IOException{
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(filePath));

		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		br.close();
		
	}



	public void changeProperties() {
		System.out.println("Creating fresh schema");
		
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		TitanManagement mgmt = graph.getManagementSystem();
		
		PropertyKey name = mgmt.getPropertyKey("name");
		mgmt.changeName(name, TitanDBProperties.VERTEX_NAME);
		
		PropertyKey type = mgmt.getPropertyKey("type");
		mgmt.changeName(type, TitanDBProperties.VERTEX_TYPE);
		
		PropertyKey weight = mgmt.getPropertyKey("weight");
		mgmt.changeName(weight, TitanDBProperties.VERTEX_WEIGHT);
		
		PropertyKey projectid = mgmt.getPropertyKey("projectid");
		mgmt.changeName(projectid, TitanDBProperties.VERTEX_PROJECT_ID);
		
		PropertyKey language = mgmt.getPropertyKey("language");
		mgmt.changeName(language, TitanDBProperties.VERTEX_LANGUAGE);
		
		PropertyKey source = mgmt.getPropertyKey("source");
		mgmt.changeName(source, TitanDBProperties.VERTEX_SOURCE);
		
		PropertyKey timestamp = mgmt.getPropertyKey("timestamp");
		mgmt.changeName(timestamp, TitanDBProperties.VERTEX_TIMESTAMP);
		
		PropertyKey tags = mgmt.getPropertyKey("tags");
		mgmt.changeName(tags, TitanDBProperties.VERTEX_TAGS);
		
		PropertyKey module = mgmt.getPropertyKey("module");
		mgmt.changeName(module, TitanDBProperties.VERTEX_MODULE);
		
		PropertyKey visitcount = mgmt.getPropertyKey("visitcount");
		mgmt.changeName(visitcount, TitanDBProperties.VERTEX_VISIT_COUNT);
		
		for( Edge edge : graph.getEdges()){
			renameEdgeProperties(edge);
		}
		
		for( Vertex vertex : graph.getVertices()){
			renameVertexProperties(vertex);
		}
		
		
		
		mgmt.commit();
		
		graph.commit();
		graph.shutdown();
		
	}

	private void renameProperty(String oldProp, String newProp, Edge edge){
		Object value = edge.getProperty(oldProp);
		edge.removeProperty(oldProp);
		edge.setProperty(newProp, value);
	}
	
	private void renameProperty(String oldProp, String newProp, Vertex vertex){
		Object value = vertex.getProperty(oldProp);
		vertex.removeProperty(oldProp);
		vertex.setProperty(newProp, value);
	}

	private void renameEdgeProperties(Edge edge) {
		renameProperty("displayName", TitanDBProperties.EDGE_DISPLAY_NAME, edge);
		
		renameProperty("down_vote", TitanDBProperties.EDGE_DOWN_VOTES, edge);
		
		renameProperty("up_vote", TitanDBProperties.EDGE_UP_VOTES, edge);
		
		renameProperty("language", TitanDBProperties.EDGE_LANGUAGE, edge);
		
		renameProperty("module", TitanDBProperties.EDGE_MODULE, edge);
		
		renameProperty("projectid", TitanDBProperties.EDGE_PROJECT_ID, edge);
		
		renameProperty("source", TitanDBProperties.EDGE_SOURCE, edge);
		
		renameProperty("sourceRole", TitanDBProperties.EDGE_SOURCE_ROLE, edge);
		
		renameProperty("targetRole", TitanDBProperties.EDGE_TARGET_ROLE, edge);
		
		renameProperty("tags", TitanDBProperties.EDGE_TAGS, edge);
		
		
		renameProperty("timestamp", TitanDBProperties.EDGE_TIMESTAMP, edge);
		
		renameProperty("type", TitanDBProperties.EDGE_TYPE, edge);
		
		renameProperty("visitcount", TitanDBProperties.EDGE_VISIT_COUNT, edge);
		
	}
	
	private void renameVertexProperties(Vertex vertex) {
		
		renameProperty("name", TitanDBProperties.VERTEX_NAME, vertex);
		
		renameProperty("type", TitanDBProperties.VERTEX_TYPE, vertex);
		
		renameProperty("weight", TitanDBProperties.VERTEX_WEIGHT, vertex);
		
		renameProperty("projectid", TitanDBProperties.VERTEX_PROJECT_ID, vertex);
		
		renameProperty("language", TitanDBProperties.VERTEX_LANGUAGE, vertex);
		
		renameProperty("source", TitanDBProperties.VERTEX_SOURCE, vertex);
		
		renameProperty("timestamp", TitanDBProperties.VERTEX_TIMESTAMP, vertex);
		
		renameProperty("tags", TitanDBProperties.VERTEX_TAGS, vertex);
		
		renameProperty("module", TitanDBProperties.VERTEX_MODULE, vertex);
		
		renameProperty("visitcount", TitanDBProperties.VERTEX_VISIT_COUNT, vertex);
		
	}
}
