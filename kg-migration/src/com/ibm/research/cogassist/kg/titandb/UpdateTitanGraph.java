package com.ibm.research.cogassist.kg.titandb;

import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class UpdateTitanGraph {
	public static boolean updateNodeVisitCount(String concept, String projectid, String language ){
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline vertices = new GremlinPipeline(graph.query().has("name",concept).has("projectid", projectid).has("language", language).vertices());
		for (Object result : vertices) {
			Vertex node = (Vertex) result;
			int visitCount = (Integer) node.getProperty("visitcount");
			node.setProperty("visitcount", visitCount+1);
			TitanDatabaseManager.shutdownGraph();
			return true;
		}
		return false;
	}
	
	public static boolean updateEdgeVisitCount(String concept, String action, String projectid, String language ){
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		
		GremlinPipeline results = new GremlinPipeline(graph.query().has("name",concept).has("projectid", projectid).has("language", language).vertices());
		for (Object result : results) {
			Vertex node = (Vertex) result;
			Iterable<Edge> edges = node.query().edges();
			for (Edge edge : edges) {
				if (edge.getLabel().equalsIgnoreCase(action)) {
					int visitCount = (Integer) edge.getProperty("visitcount");
					edge.setProperty("visitcount", visitCount+1);
					
				}
			}
			TitanDatabaseManager.shutdownGraph();
			return true;
		}
		return false;
	}
	
	
	
	private static void deleteEdgeWithLabel(Iterable<Vertex> neighbors, String label){
		for (int i=0; i<Iterables.size(neighbors); i++) {
			for(int j=i+1; j<Iterables.size(neighbors); j++){
				Vertex node1= Iterables.get(neighbors,i);
				Vertex node2 = Iterables.get(neighbors,j);
				Edge edge = GraphUtils.getEdge(node1, node2, label);
				if(edge != null)
					edge.remove();
			}
		}
	}
	
	private static void deleteDirectEdges(Vertex node, Iterable<Vertex> neighbors, String label){
		for(Vertex neighbor: neighbors){
			Edge edge = GraphUtils.getEdge(node, neighbor, label);
			if(edge != null)
				edge.remove();
			if (Iterables.size(neighbor.getEdges(Direction.BOTH)) == 0){
				neighbor.remove();
			} 
		}
	}

	/*Deleting a node we assume only question id only. Logic:
	 * First delete the edges of the neighbor with the same label
	 * The delete the edges to direct neighbors.
	 * Now if the degree of the neighbor is 0 delete the neighbor itself.
	 * Finally delete the node.*/
	public static boolean deleteRelations(String questionid, String projectid) {
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline vertices = new GremlinPipeline(graph.query().has("name",questionid).has("projectid", projectid).vertices());
		if(vertices.size() > 0){
			Vertex node = (Vertex) vertices.get(0);
			Iterable<Edge> edges = node.query().edges();
			String label = Iterables.get(edges, 0).getLabel();
			
			Iterable<Vertex> neighbors = node.getVertices(Direction.BOTH);
			deleteEdgeWithLabel(neighbors, label);
			deleteDirectEdges(node, neighbors, label);
			
			node.remove();
			TitanDatabaseManager.shutdownGraph();
		}
		return false;
	}
	
	public static boolean deleteSubGraph(String projectid) {
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		GremlinPipeline edges = new GremlinPipeline(graph.query().has("projectid", projectid).edges());
		for (Object obj : edges){
			Edge edge = (Edge) obj;
			edge.remove();
		}
		GremlinPipeline vertices = new GremlinPipeline(graph.query().has("projectid", projectid).vertices());
		for (Object obj : vertices){
			Vertex vertex = (Vertex) obj;
			vertex.remove();
		}
		TitanDatabaseManager.shutdownGraph();
		return true;
	}
}
