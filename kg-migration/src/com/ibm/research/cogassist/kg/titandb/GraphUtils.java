package com.ibm.research.cogassist.kg.titandb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;

public class GraphUtils {
	
	public static Vertex getVertex(Map<String, String> source){
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		System.out.println(source.toString());
		System.out.println("Graph..."+graph.toString());
		GremlinPipeline vertices = new GremlinPipeline(graph.query().has("name",source.get("name")).has("type", source.get("type")).has("projectid", source.get("projectid")).has("language", source.get("language")).vertices());
		for(Object vertex : vertices){
			Vertex node = (Vertex) vertex;
		    return node;
		}
		return null;
	}
	
	/**
	 * Code is not working need to check
	 * @param source
	 * @param target
	 * @param label
	 * @return
	 */
	/*public static boolean isNeighbor(Vertex source, Vertex target){
		return new GremlinPipeline<Vertex, ArrayList<Vertex>>(source).both().has("id", target.getId()).hasNext();
	}
	
	public static boolean isNeighbor(Vertex source, Vertex target, String label){
		return new GremlinPipeline<Vertex, ArrayList<Vertex>>(source).bothE(label).has("id", target.getId()).hasNext();	
	}*/
	
	public static boolean isNeighbor(Vertex source, Vertex target, String projectid, String language){
		try{
		 return new GremlinPipeline(source).bothE().has("name", target.getProperty("name")).has("projectid", projectid).has("language", language).hasNext();
		}catch(Exception e){
			System.out.println(e.getMessage()+" Source: "+source.getProperty("name")+" Target: "+target.getProperty("name"));
			return false;
		}
	}
	
	public static boolean isNeighbor(Vertex source, Vertex target, String label, String projectid, String language){
		try{
			return new GremlinPipeline(source).bothE(label).has("name", target.getProperty("name")).has("projectid", projectid).has("language", language).hasNext();
		}catch(Exception e){
			System.out.println(e.getMessage()+" Source: "+source.getProperty("name")+"; Target: "+target.getProperty("name")+"; Label: "+label);
			return false;
		}
	}

	/*From source take its neighbor and from neighbor count the paths to target
	 * Excluding the source.*/
	public static long countPaths(Vertex source, Vertex target, Vertex excludeNode){
		TitanGraph graph = TitanDatabaseManager.getDataSource();
		List<List> names = new ArrayList<>();
		long count = new GremlinPipeline<Vertex, ArrayList<Vertex>>(source).as("x").both().loop("x",
		        new PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean>() {
		            @Override
		            public Boolean compute(LoopPipe.LoopBundle<Vertex> loopBundle) {
		                return loopBundle.getLoops() < 3;
		            }
		        }, new PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean>() {
		            @Override
		            public Boolean compute(LoopPipe.LoopBundle<Vertex> loopBundle) {
		                return target.equals(loopBundle.getObject().getProperty("name")) || excludeNode.equals(loopBundle.getObject().getProperty("name"));
		            }
		        }
		).has("id", target.getId()).count();
		return count;
	}
	
	public static List<Edge> getEdge(Vertex source, Vertex target) {
		 GremlinPipeline Edges = new GremlinPipeline(source).bothE().as("x").bothV().retain(Arrays.asList(target)).back("x");
		 List<Edge> edges = new ArrayList<>();
		 for (Object obj : Edges) {
			Edge edge = (Edge) obj;
			edges.add(edge);
		 }
		 return edges;
	}
	
	public static Edge getFirstEdge(Vertex source, Vertex target) {
		return (Edge) new GremlinPipeline(source).bothE().as("x").bothV().retain(Arrays.asList(target)).back("x").next();
	}
	
	public static Edge getEdge(Vertex source, Vertex target, String label){
		return (Edge)new GremlinPipeline<Vertex, ArrayList<Vertex>>(source).bothE(label).has("id", target.getId()).next();	
	}
	
	
}
