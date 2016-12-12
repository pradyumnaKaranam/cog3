package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.json.JSONObject;

public class SelectAnswerFromErrorCode extends Query<JSONObject> {
	public SelectAnswerFromErrorCode(DataSource ds, String projectid, String id) throws SQLException {
		super(ds, "select  q.Question as question, a.answer as answer from  from questions q,question_answer qa,answers a  where q.id=qa.question_id and a.id=qa.answer_id and q.error_code  = ? and q.project_id = ?") ;
		super.bind(id);
		super.bind(projectid);
	}
	
	@Override
	public JSONObject getRow(ResultSet resultSet) throws SQLException {
		JSONObject obj = new JSONObject();
		obj.put("question", resultSet.getString("question"));
		obj.put("answer", resultSet.getString("answer"));
		return obj;
	}
}
