package com.ibm.research.cogams.tikaingestion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.cogams.docingestion.AppLevelGlobals;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keyword;


public class AlchemyIntfc {
	
	private static final Logger logger = LoggerFactory.getLogger(AlchemyIntfc.class);
	JSONParser parser = new JSONParser();
	AlchemyLanguage service = new AlchemyLanguage();

	public AlchemyIntfc(){
		String apiKey = AppLevelGlobals.getInstance().getAlchemyAPIKey();
		service.setApiKey(apiKey);
	}




	public String addHighLights(String doc){
		try {
			Object obj = parser.parse(doc);
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray ansUnits = (JSONArray) jsonObject.get("answer_units");
			Iterator<JSONObject> iteratorAU = ansUnits.iterator();
			while (iteratorAU.hasNext()) {
				JSONObject currObj = iteratorAU.next();
				String title = (String) currObj.get("title");
				logger.trace("processing title for keywords: {}",title);
				JSONArray contents = (JSONArray) currObj.get("content");
				Iterator<JSONObject> iteratorCNTNT = contents.iterator();
				while(iteratorCNTNT.hasNext()){
					JSONObject currCObj = iteratorCNTNT.next();
					JSONArray ent = new JSONArray(); // entities that are to be added
					JSONArray keyw = new JSONArray(); // keywords that are to be added
					String contentType = (String) currCObj.get("media_type");
					if(contentType.equals("text/plain")){
						String body = (String) currCObj.get("text");
						if(body.trim().length() > 0){
							String text = title + " " + body;
							Map<String, Object> params = new HashMap<>();
							params.put("text",text);
							List<Entity> entities = service.getEntities(params).execute().getEntities();
							List<Keyword> keywords = service.getKeywords(params).execute().getKeywords();
							int el = entities.size();
							int kl = keywords.size();
							for(int i= 0; i<10;i++){
								if(el > i)
									ent.add(entities.get(i).getText());
								if(kl > i)
									keyw.add(keywords.get(i).getText());
							}
							currCObj.put("entities", ent);
							currCObj.put("keywords", keyw);
						}
					}
				}
			}
			return jsonObject.toString();
		} catch (ParseException e1) {
			System.out.println(" Could not parse !!");
			e1.printStackTrace();
		}
		return  doc;
	}


	String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public static void main(String[] args) throws IOException {

		AlchemyIntfc alchem = new AlchemyIntfc();
		String input = alchem.readFile("/home/pallavi/Desktop/docIngestion/pallavi/sample.txt");
		//alchem.service.setApiKey("982793953aa54ab33cfcf2dc767b8916aac9a5f8");
		String newDoc = alchem.addHighLights(input);
		System.out.println(" new doc :");
		System.out.println(newDoc);
	}
}

