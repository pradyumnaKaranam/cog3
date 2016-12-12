#!/usr/bin/python
import MySQLdb
import sys
import csv
import re
import sys  
import codecs
import json

def Ingestion(config_data, project_info):

	input_file = config_data["project_name"] + "_ErrorMsg.csv"
	input_data = open(input_file, 'r')
	all_lines = csv.DictReader(input_data , delimiter=',')
	
	# Open database connection
	db = MySQLdb.connect(host = config_data['icurate_db_host'], port = config_data['icurate_db_port'], \
		user = config_data['icurate_db_user'], passwd = config_data['icurate_db_password'], db = config_data['icurate_db_dbname'], charset='utf8' )
	# prepare a cursor object using cursor() method
	cursor = db.cursor()
	author_count = 0 
	no_question_count = 0 
	author_name = ""

	while True:

		author_count = author_count + 1

		try:
			line = all_lines.next()
			curation_id = -1
			print str(author_count) +  " Ingesting error code with id -  " + line['id'] 

			if(len(line['title']) == 0):
				no_question_count = no_question_count + 1
				print "No question skipping id " + line['id'] 
				continue
		
			question_state = 'APPROVED'
			question = "[ErrorCode: " + line['id'] +  "] - " +  line['title']
			
			result_str = "<html>"
			result_str = result_str +  "<p><b>URL - </b><br /><a href='%s' target='_blank'>%s</a><br /></p>" %(line['link'],line['link'])
			result_str = result_str +  "<br /><p><b>Explanation -</b><br />" + line['explanation'] + "<br /></p>"	
			result_str = result_str +  "<p><b>User Action -</b><br />" + line['user_action'] + "<br /></p>"	
			result_str = result_str +  "<p><b>System Response -</b><br />" + line['system_response'] + "<br /></p>"	
			result_str = result_str +  "</html>"

			author_name = config_data['authors'][author_count % (len(config_data['authors']))]

			### Prepare SQL query to INSERT a curated record into the database.
			sql = "INSERT INTO curatedcontent (scenario, content,project_id, state, author, approver, question_asker, semanticQuestion) VALUES \
					('%s','%s','%d','%s','%s','%s', '%s', '%s')" \
					% (unicode(MySQLdb.escape_string(line['id']), 'utf-8'), unicode(MySQLdb.escape_string(result_str), 'utf-8'),\
						project_info['project_id'],question_state,\
						author_name, config_data["admins"][0], author_name, unicode(MySQLdb.escape_string(question), 'utf-8') )
			try:
			   # Execute the SQL command
			   cursor.execute(sql)
			   curation_id = cursor.lastrowid
			   # Commit your changes in the database
			   db.commit()
			except:
			   # Rollback in case there is any error
			   print "Error in INSERT curatedcontent: " + line['id']
			   db.rollback()
			if (curation_id==-1):
				continue
			
			### Adding entry to tag activity 
			sql = "INSERT INTO tagactivity (tag_id, content_type, content_id, tagged_by) VALUES ('%d','%s', '%d', '%s')" \
					% (project_info['project_tag_id'], 'content', curation_id, author_name )
			try:
			   # Execute the SQL command
			   cursor.execute(sql)
			   # Commit your changes in the database
			   db.commit()
			except:
			   # Rollback in case there is any error
			   print "Error: in tagactivity " + line['id']
			   db.rollback()

			### Adding entry to activity 
			sql = "INSERT INTO activity (id, activity_type, user, projectid) VALUES ('%d','%s', '%s', '%d')" \
					% (curation_id, 'curation_create', author_name , project_info['project_id'])
			try:
			   # Execute the SQL command
			   cursor.execute(sql)
			   # Commit your changes in the database
			   db.commit()
			except:
			   # Rollback in case there is any error
			   print "Error: in activity " + line['id']
			   db.rollback()

			### Adding Actual Error Code to error_msg_code 
			sql = "INSERT IGNORE INTO error_code_msg (error_code, error_message, source, project_id) VALUES ('%s', '%s', '%s', '%d')" \
					% (unicode(MySQLdb.escape_string(line['id']), 'utf-8'), unicode(MySQLdb.escape_string(line['title']), 'utf-8') , 'orig', project_info['project_id'])
			try:
			   # Execute the SQL command
			   cursor.execute(sql)
			   # Commit your changes in the database
			   db.commit()
			except:
			   # Rollback in case there is any error
			   print "Error: in Ingestion actual error_code and massage" + line['id']
			   db.rollback()

			### Adding Curated QA to error_msg_code 
			sql = "INSERT IGNORE INTO error_code_msg (error_code, error_message, source, project_id) VALUES ('%s', '%s', '%s', '%d')" \
					% ( "curatedcontent_" + str(curation_id), unicode(MySQLdb.escape_string(question),'utf-8') , 'prod', project_info['project_id'])
			try:
			   # Execute the SQL command
			   cursor.execute(sql)
			   # Commit your changes in the database
			   db.commit()
			except:
			   # Rollback in case there is any error
			   print "Error: in QA entry in error_code_msg " + line['id']
			   db.rollback()

		except csv.Error:
			print "Line Number - " + str(author_count) +  " Problem while Ingesting id "
		except StopIteration:
			print "Dumping File end"
			break
		except:
			print "Insertion errro with the while loop"

	print "Question with no title " + str(no_question_count)
	
	db.close()
