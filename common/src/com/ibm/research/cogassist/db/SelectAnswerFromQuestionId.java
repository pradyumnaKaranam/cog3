package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.json.JSONObject;

public class SelectAnswerFromQuestionId extends Query<JSONObject> {
	public SelectAnswerFromQuestionId(DataSource ds, String projectid, String errorCode) throws SQLException {
super(ds, "select  q.Question as question, a.answer as answer, qa.id as qaid from  from questions q,question_answer qa,answers a  where q.id=qa.question_id and a.id=qa.answer_id and q.id  = ? and q.project_id = ?") ;
		
		super.bind(errorCode);
		super.bind(projectid);
	}
	
	@Override
	public JSONObject getRow(ResultSet resultSet) throws SQLException {
		JSONObject obj = new JSONObject();
		obj.put("question", resultSet.getString("question"));
		obj.put("answer", resultSet.getString("answer"));
		obj.put("qa_id", resultSet.getString("answer"));
		return obj;
	}
}
