package com.ibm.research.cogassist.kg;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.ibm.research.cogassist.common.CogAssist;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.Tense;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.realiser.english.Realiser;

public class ChangeTense {
	StanfordCoreNLP pipeline;
	XMLLexicon lexicon;
	private static volatile ChangeTense instance;

	private ChangeTense() {
		System.out.println("******Loading SimpleNLG related jars.");
		String xmlLocation = CogAssist.getHomeDirectoryName()+File.separator+"default-lexicon.xml";
		lexicon = new XMLLexicon(xmlLocation);
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props, false);
		System.out.println("******Loaded SimpleNLG related jars.");
	}

	public static ChangeTense getInstance() {
		if (instance == null) {
			synchronized (SentenceParsing.class) {
				if (instance == null) {
					instance = new ChangeTense();
				}
			}
		}
		return instance;
	}

	@SuppressWarnings("unused")
	private String getPresentParticiple(String verb) {
		WordElement word = lexicon.getWord(getRootForm(verb), LexicalCategory.VERB);
		InflectedWordElement infl = new InflectedWordElement(word);

		infl.setFeature(Feature.FORM, Form.PRESENT_PARTICIPLE);
		Realiser realiser = new Realiser(lexicon);
		String present = realiser.realise(infl).getRealisation();
		return present;
	}

	private String getPresent(String verb) {

		WordElement word = lexicon.getWord(getRootForm(verb), LexicalCategory.VERB);
		InflectedWordElement infl = new InflectedWordElement(word);

		infl.setFeature(Feature.TENSE, Tense.FUTURE);
		Realiser realiser = new Realiser(lexicon);
		String present = realiser.realise(infl).getRealisation();
		return present;
	}

	private String getRootForm(String verb) {
		String root = "";
		Annotation document = pipeline.process(verb);
		for (CoreMap sentence : document.get(SentencesAnnotation.class))
			for (CoreLabel token : sentence.get(TokensAnnotation.class))
				root = token.get(LemmaAnnotation.class);
		return root;
	}

	private String getTag(String verb) {
		String tag = "";
		Annotation document = pipeline.process(verb);
		for (CoreMap sentence : document.get(SentencesAnnotation.class))
			for (CoreLabel token : sentence.get(TokensAnnotation.class))
				tag = token.tag();
		return tag;
	}

	private String getCurrentTense(String verb) {
		List<String> presentTenseList = Arrays.asList("VB", "VBG", "VBP", "VBZ");
		List<String> pastTenseList = Arrays.asList("VBD", "VBN");
		String tag = getTag(verb);
		if (pastTenseList.contains(tag))
			return "past";
		else if (presentTenseList.contains(tag))
			return "present";
		return "";

	}

	public String changeTense(String verb) {
		if (!verb.equals("-"))
			if (verb.split(" ").length == 1) {
				if (!getCurrentTense(verb).equals("present"))
					return getPresent(verb);
			} else if (verb.split(" ")[0].equalsIgnoreCase("no") || verb.split(" ")[0].equalsIgnoreCase("not")) {
				if (getCurrentTense(verb).equals("present"))
					return "does not " + verb.split(" ")[1];
				return "not " + verb.split(" ")[1];
			}
		return verb;
	}

	public String getMultiRootForm(String action) {
		if (action.split(" ").length <= 1)
			return getRootForm(action.trim()).trim();
		else {
			int length = action.split(" ").length;
			String out = getRootForm(action.split(" ")[0].trim());
			for (int i = 1; i < length; i++) {
				out = out + " " + getRootForm(action.split(" ")[i].trim());
			}
			return out.trim();
		}
	}

	public static void main(String[] args) {
		ChangeTense ct = ChangeTense.getInstance();
		System.out.println(ct.changeTense("created"));
		System.out.println(ct.getRootForm("created"));
		System.out.println(ct.getPresent("created"));
	}
}
