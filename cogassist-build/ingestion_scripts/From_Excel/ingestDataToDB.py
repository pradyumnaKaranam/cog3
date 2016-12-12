#!/usr/bin/python
import MySQLdb
import sys
import csv
import re
import sys  
import codecs
import json

def Ingestion(config_data, project_info):

	input_data = open( config_data["project_name"] + "_Excel_withQuesAns.csv", 'r')
	all_lines = csv.reader(input_data , delimiter=',')
	next(all_lines, None)  # skip the headers # if header is there 

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

			if (line[3] == "Y"): 
				project_info['project_tag_id'] = -1
				curation_id = -1

				print str(author_count) +  " Ingesting lead question id " + line[0] 

				if(len(line[1]) == 0):
					no_question_count = no_question_count + 1
					print "No question skipping id " + line[0] 
					continue
			
				question_state = 'APPROVED'
				question = MySQLdb.escape_string("[Excel] - " +  line[1])
				
				result_str = "<html>"
				result_str = result_str +  "<p><b>Related URLs - &nbsp;</b><ul>" 
				hyperlike_lists = line[2].split(',') 
				for hl in hyperlike_lists:
					result_str = result_str + "<li><a href=\"%s\" target=\"_blank\">%s</a></li>&nbsp;&nbsp;" %(hl,hl)
				result_str = result_str +  "</ul></p>"


				if(len(line[5])==0  or line[5]=='Unanswered' or line[5]=='null'):
					print "No Answer"
					line[5] = ""

				result_str = result_str +  "<p><b>Solution - &nbsp;</b>" + line[5] + "</p>"				
				result_str = result_str +  "</html>"
				
				author_name = config_data['authors'][author_count % (len(config_data['authors']))]

				### Adding the Tag itself to the DB
				sql = "INSERT INTO tags (tag_type, name, created_by, functional_area) VALUES ('%s', '%s', '%s', '%s')" \
						% (config_data["tag_type"], line[4], config_data["admins"][0], config_data["functional_area"])
				try:
				   # Execute the SQL command
				   cursor.execute(sql)
				   project_info['project_tag_id'] = cursor.lastrowid
				   # Commit your changes in the database
				   db.commit()
				except:
				   # Rollback in case there is any error
				   project_info['project_tag_id'] = -1
				   print "Error in creating tag " + line[4]
				   db.rollback()		
				if(project_info['project_tag_id'] == -1):
					continue

				### Prepare SQL query to INSERT a curated record into the database.
				sql = "INSERT INTO curatedcontent (scenario, content,project_id, state, author, approver, question_asker, semanticQuestion) VALUES \
						('%s','%s','%d','%s','%s','%s', '%s', '%s')" \
						% (unicode(MySQLdb.escape_string(question), 'utf-8'), unicode(MySQLdb.escape_string(result_str), 'utf-8'),\
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
				   print "Error in INSERT curatedcontent: " + line[0]
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
				   print "Error: in tagactivity " + line[0]
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
				   print "Error: in activity " + line[0]
				   db.rollback()

				## Alternative question adding to the altquestion table 
				alt_question_id = -1
				if (line[5] == 'Unanswered' or line[6]=='null' ):
					continue
				sql = "INSERT INTO altquestions (curation_id, question, type) VALUES ('%d', '%s', '%s')" % (curation_id, \
					 unicode(MySQLdb.escape_string(line[6].replace('\t', ' ')), 'utf-8') , 'content')
				try:
				   # Execute the SQL command
				   cursor.execute(sql)
				   alt_question_id = cursor.lastrowid
				   # Commit your changes in the database
				   db.commit()
				except:
				   # Rollback in case there is any error
				   print "Error: in altquestion(primary) " + line[0]
				   db.rollback()
				if(alt_question_id != -1):
					### Adding activity of alternative question
					sql = "INSERT INTO activity (id, activity_type, user, projectid) VALUES ('%d', '%s', '%s', '%d')" \
						% (alt_question_id, 'altquestion', author_name, project_info['project_id'])
					try:
					   # Execute the SQL command
					   cursor.execute(sql)
					   # Commit your changes in the database
					   db.commit()
					except:
					   # Rollback in case there is any error
					   print "Error: in altquestion activity " + line[0]
					   db.rollback()

			elif (line[3]=="N"):
				
				if(curation_id==-1 or project_info['project_tag_id']==-1):
					print "No lead question. Skipping altquestion " + str(line[0])
					continue
				### Alternative question adding to the altquestion table 
				alt_question_id = -1
				sql = "INSERT INTO altquestions (curation_id, question, type) VALUES ('%d', '%s', '%s')" % (curation_id, \
					 unicode(MySQLdb.escape_string(line[1]), 'utf-8') , 'content')
				try:
				   # Execute the SQL command
				   print "Adding alt question " + str(line[0])
				   cursor.execute(sql)
				   alt_question_id = cursor.lastrowid
				   # Commit your changes in the database
				   db.commit()
				except:
				   # Rollback in case there is any error
				   print "Error: in altquestion " + line[0]
				   db.rollback()
				if(alt_question_id != -1):
					### Adding activity of alternative question
					sql = "INSERT INTO activity (id, activity_type, user, projectid) VALUES ('%d', '%s', '%s', '%d')" \
						% (alt_question_id, 'altquestion', author_name, project_info['project_id'])
					try:
					   # Execute the SQL command
					   cursor.execute(sql)
					   # Commit your changes in the database
					   db.commit()
					except:
					   # Rollback in case there is any error
					   print "Error: in altquestion activity " + line[0]
					   db.rollback()

		except csv.Error:
			print "Line Number - " + str(author_count) +  " Problem while Ingesting id "
		except StopIteration:
			print "Dumping File end"
			break
		except:
			print "Insertion errro with the while loop"

	print "Question with no title " + str(no_question_count)
	
	# sql = "insert ignore into error_code_msg (error_code, error_message, source, project_id) \
	# select MD5(concat(semanticQuestion,project_id)), semanticQuestion, 'prod',  project_id from curatedcontent\
	# where semanticQuestion is not null and project_id=%d group by semanticQuestion, project_id;" % (project_info['project_id'])
	
	# insert ignore into error_code_msg (error_code, error_message, source, project_id)\
	# select MD5(concat(curation_id,question, type)), question, 'prod',  project_id from \
	# altquestions where question is not null group by question, curation_id;   
	# #sql = ""

	# try:
	#    # Execute the SQL command
	#    cursor.execute(sql)
	#    # Commit your changes in the database
	#    db.commit()
	#    print "Query on error_code_msg successful"
	# except:
	#    # Rollback in case there is any error
	#    print "Error: Ingestion into error_code_msg table "
	#    db.rollback()
	db.close()
