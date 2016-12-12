package com.ibm.research.cogassist.db;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

/**
 * A query which is expected to return at most one result. This class
 * provides a utility method for quickly retrieving the singleton item
 * after construction of the query.
 * @author Rohan Padhye
 *
 */
public abstract class SingletonQuery<T> extends Query<T> {

	
	/** Calls the super-class constructor with the exact same arguments. */
	public SingletonQuery(DataSource ds, String sql, Object... params) {
		super(ds, sql, params);
	}
	
	/** Returns the single returned item or null if the query did not return any rows.  */
	public T fetch() throws SQLException {
		List<T> items = this.execute();
		if (items.isEmpty()) {
			return null;
		} else {
			return items.get(0);
		}
	}

}
