package com.ibm.research.cogassist.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
public class UpdateDocumentStatusQuery extends Update {

	public UpdateDocumentStatusQuery(DataSource ds, String projname, String status, String docname) throws SQLException {
		super(ds, "update documents " +
				" set status = ? " +
				" where filename = ? and " +
				" project_id = ?"
				);
		super.bind(status);
		super.bind(docname);
		super.bind(CogAssist.getProjectID(ds,projname));		
	}
	public UpdateDocumentStatusQuery(DataSource ds, String projname, String status, String docname, String reason) throws SQLException {
                super(ds, "update documents " +
                                " set status = ?, reason = ? " +
                                " where filename = ? and " +
                                " project_id = ?"
                                );
                super.bind(status);
		super.bind(reason);
                super.bind(docname);
                super.bind(CogAssist.getProjectID(ds,projname));
        }

}
