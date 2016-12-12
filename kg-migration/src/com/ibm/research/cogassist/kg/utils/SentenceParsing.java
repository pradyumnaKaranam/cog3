package com.ibm.research.cogassist.kg.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.DatabaseManager;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class SentenceParsing {
	private StanfordCoreNLP pipeline;
	private MaxentTagger tagger;
	private DependencyParser parser;
	Map<String, List<String>> nounVerbMap = new HashMap<String, List<String>>();
	private static volatile SentenceParsing instance;
    private SentenceParsing() throws IOException {
    	System.out.println("*******Loading stanford nlp jars");
    	String modelPath = DependencyParser.DEFAULT_MODEL;
		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		tagger = new MaxentTagger(taggerPath);
		parser = DependencyParser.loadFromModelFile(modelPath);
    	//pipeline = new StanfordCoreNLP();
		Properties dbCredentials = CogAssist.getDBProperties();
		DataSource dataSource = DatabaseManager.createJdbcDataSource(dbCredentials);
		DatabaseManager.setDataSource(dataSource, dbCredentials.getProperty("DBTYPE"));
		System.out.println("*******Loaded stanford nlp jars");
    }
 
    public static SentenceParsing getInstance() throws IOException {
        if (instance == null ) {
            synchronized (SentenceParsing.class) {
                if (instance == null) {
                    instance = new SentenceParsing();
                }
            }
        }
 
        return instance;
    }
    
    private List<String> parseTree(Tree tree) {
		List<String> nounList = new ArrayList<String>();
		if ((tree.value()).equalsIgnoreCase("np")){
			nounList.addAll(getNoun(tree));
		}else {
			for (Tree t : tree.children())
				nounList.addAll(parseTree(t));
		}
		return nounList;
	} 
	
	private List<String> getNoun(Tree tree){
		String noun = "";
		List<String> nounList = new ArrayList<String>(); 
		for (Tree child: tree.children()){
			if ((child.value()).equalsIgnoreCase("nn") || (child.value()).equalsIgnoreCase("nnp"))
				noun = noun + " " + child.children()[0].value();
			if ((child.value()).equalsIgnoreCase("np")){
				if(!"".equals(noun))
					nounList.add(noun.trim());
				nounList.addAll(getNoun(child));
			} else {
				nounList.addAll(parseTree(child));
			}
				
		}
		if(!"".equals(noun))
			nounList.add(noun.trim());
		return nounList;
	}
	
	private Map<String, List<String>> parseTree (Collection<TypedDependency> deps ) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		String noun = "";
		List<String> nounList = new ArrayList<String>();
		List<String> actionList = new ArrayList<String>();
		String last = "";
		String action ="";
		for (TypedDependency td : deps){
			//System.out.println(td.reln()+" : "+td.dep().backingLabel().index()+" : "+td.gov().backingLabel().value());
			String reln = td.reln().getShortName();
			String dep = td.dep().backingLabel().value();
			String gov = td.gov().backingLabel().value();
			switch (reln) {
			case "nn":
			case "amod":
				if ( td.dep().backingLabel().index() < td.gov().backingLabel().index()){
					noun = noun + " " + dep;
					last =  td.gov().backingLabel().value();
				} else {
					noun = noun + " " + gov;
					last = td.dep().backingLabel().value();
				}
				break;
			case "conj":
			case "appos":
				if ( td.dep().backingLabel().index() < td.gov().backingLabel().index()){
					noun = noun + " "+ last + " " + dep;
					nounList.add(noun.trim());
					noun = gov;
				} else {
					noun = noun + " "+ last + " " + gov;
					nounList.add(noun.trim());
					noun = dep;
				}
				break;
			case "dobj":
			case "pobj":
			case "iobj":
			case "prep_in":
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				else
					nounList.add(dep.trim());
				noun = "";
				last = "";
				break;
			case "nsubj":
			case "nsubjpass":
				action = action.trim()+" "+gov;
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				else
					nounList.add(dep.trim());
				noun = "";
				last = "";
				break;
				
			case "npadvmod":
			case "vmod":
				action = dep;
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				noun = "";
				last = "";
				break;
			case "aux":
				action = gov;
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				noun = "";
				last = "";
				break;
			case "prep":
				if ("".equals(action))
					action = dep;
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				noun = "";
				last = "";
				break;
			case "neg":
			case "advmod":
				action = dep + " " + action.trim();
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				noun = "";
				last = "";
				break;
			default:
				if (!"".equals(noun))
					nounList.add(noun.trim() + " "+ last);
				noun = "";
				last = "";
				
		}
		}
		//action = getRootForm(action);
		action = action.trim();
		if (Constants.stopWords.contains(action) || "name".equalsIgnoreCase(action)
				|| "type".equalsIgnoreCase(action))
			action = "-";
		if (!actionList.contains(action) )
			actionList.add(action);
		if(!"".equals(noun)){
			noun = (noun.trim() + " "+ last.trim()).trim();
			if (!"".equals(noun) && !Constants.stopWords.contains(noun))
				nounList.add(noun);
		}
		map.put("nouns", nounList);
		map.put("actions", actionList);
		return map;
	}
    
	
	private List<String> corefAnalysis(Annotation annotation, List<CoreMap> sentences){
		List<String> nounList = new ArrayList<String>();
		Map<Integer, CorefChain> corefChains = annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class);
		if (corefChains != null) 			
		for (Map.Entry<Integer, CorefChain> entry : corefChains.entrySet()) {
			for (CorefChain.CorefMention m : entry.getValue().getMentionsInTextualOrder()) {
				nounList.add(m.toString().split("\"")[1]);
				
			}
		}
		return nounList;
	}
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, List<Map<String, List<String>>>> getAllParses(String errorCode) throws IOException {
		// set up optional output files
		Map<String, List<Map<String, List<String>>>> map = new HashMap<String, List<Map<String,List<String>>>>();
		
		Annotation annotation = new Annotation(errorCode);

		pipeline.annotate(annotation);

		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			for (CoreMap sentence : sentences) {
				Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
				Map<String, List<String>> m = new HashMap<String, List<String>>();
				m.put("nouns", parseTree(tree));
				//m.put("tree", Arrays.asList(tree.toString()));
				if (!map.containsKey("consistuency"))
					map.put("consistuency", new ArrayList());
				map.get("consistuency").add(m);
				//SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
				SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
				m =parseTree(graph.typedDependencies());
				//m.put("tree", Arrays.asList(graph.toString(SemanticGraph.OutputFormat.LIST).replaceAll("[\\t\\n\\r]"," ")));
				if (!map.containsKey("dependency"))
					map.put("dependency", new ArrayList());
				map.get("dependency").add(m);
			}
			Map<String, List<String>> m = new HashMap<String, List<String>>();
			m.put("nouns",corefAnalysis(annotation, sentences));
			if (!map.containsKey("coreference"))
				map.put("coreference", new ArrayList());
			map.get("coreference").add(m);
		}
		return map;
	}
	
	public List<Map<String, List<String>>> getDependencyParse(String errorCode) throws IOException {
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(errorCode));
		List<Map<String, List<String>>> completeList = new ArrayList<Map<String,List<String>>>();
		for (List<HasWord> sentence : tokenizer) {
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			GrammaticalStructure gs = parser.predict(tagged);
			Map<String, List<String>> parseTree = parseTree(gs.typedDependencies());
			parseTree.put("tree", Arrays.asList(gs.typedDependencies().toString()));
			completeList.add(parseTree);
		}

		return completeList;
	}
}
