#!/usr/bin/python
import MySQLdb
import sys
import csv
import re
import sys  
import codecs

def Ingestion(config_data, project_info):
	
	input_data = open(config_data["project_name"] + "_withQuesAns.csv", 'r')
	all_lines = csv.reader(input_data , delimiter=',')
	next(all_lines, None)  # skip the headers # if header is there 

	# Open database connection
	db = MySQLdb.connect(host = config_data['icurate_db_host'], port = config_data['icurate_db_port'], \
		user = config_data['icurate_db_user'], passwd = config_data['icurate_db_password'], db = config_data['icurate_db_dbname'], charset='utf8' )
	# prepare a cursor object using cursor() method
	cursor = db.cursor()
	
	author_count = 0 
	no_question_count = 0
	apar_dict = {} 
	
	### Ingesting content to DB in iterative manner. 
	while True:
		author_count = author_count + 1
		try:
			line = all_lines.next()
			print str(author_count) +  " Ingesting id " + line[0] 
			if(len(line[1]) == 0):
				no_question_count = no_question_count + 1
				print "No question skipping id " + line[0] 
				continue
		
			if(line[5] != 'null'):

				if(line[5] not in apar_dict.keys()):
					question_state = 'WAITING_FOR_APPROVAL'
					question = MySQLdb.escape_string("[PMR - " + line[0] + " ] " + line[1].replace('\t', ' '))
					result_str = "<html>"

					if(line[2]=='null'):
						question_state = 'UNANSWERED'
					else:
						result_str = result_str +  "<p><b>Related URLs - &nbsp;</b><ul>" 
						hyperlike_lists = line[2].split(',') 
						for hl in hyperlike_lists:
							result_str = result_str + "<li><a href=\"%s\" target=\"_blank\">%s</a></li>&nbsp;&nbsp;" %(hl,hl)
						result_str = result_str +  "</ul></p>"

					if(line[3]=='null'):
						line[3] = ""

					if(line[3]=='Unanswered'):
						question_state = 'UNANSWERED'
					else:		
						result_str = result_str +  "<p><b>Solution - &nbsp;</b>" + line[3] + "</p>"
					
					result_str = result_str +  "</html>"
					
					author_name = config_data['authors'][author_count % (len(config_data['authors']))]

					# Prepare SQL query to INSERT a curated record into the database.
					sql = "INSERT INTO curatedcontent (scenario, content,project_id, state, author, question_asker, semanticQuestion) VALUES \
							('%s','%s','%d','%s','%s', '%s', '%s')" \
							% (unicode(MySQLdb.escape_string(question), 'utf-8'), unicode(MySQLdb.escape_string(result_str), 'utf-8'),\
								project_info['project_id'],question_state,\
								author_name ,author_name , unicode(MySQLdb.escape_string(question), 'utf-8') )
					curation_id = -1
					try:
					   # Execute the SQL command
					   cursor.execute(sql)
					   curation_id = cursor.lastrowid
					   # Commit your changes in the database
					   db.commit()
					except:
					   # Rollback in case there is any error
					   print "Error in INSERT curatedcontent(primary): " + line[0]
					   db.rollback()
					if (curation_id==-1):
						continue

					## Adding curation id to dictionary 
					apar_dict[line[5]] = curation_id

					## Adding entry to tag activity 
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

					## Adding entry to activity 
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
					if (line[3] == 'Unanswered' or line[4]=='null' ):
						continue
					sql = "INSERT INTO altquestions (curation_id, question, type) VALUES ('%d', '%s', '%s')" % (curation_id, \
						 unicode(MySQLdb.escape_string(line[4].replace('\t', ' ')), 'utf-8') , 'content')
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
				else:
					curation_id = apar_dict[line[5]]

					## Alternative question adding to the altquestion table 
					alt_question_id = -1
					if (line[1]=='null' or len(line[1])==0):
						continue
					sql = "INSERT INTO altquestions (curation_id, question, type) VALUES ('%d', '%s', '%s')" % (curation_id, \
						 unicode(MySQLdb.escape_string(line[1].replace('\t', ' ')), 'utf-8') , 'content')
					try:
					   # Execute the SQL command
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
			else:
				question_state = 'WAITING_FOR_APPROVAL'
				question = MySQLdb.escape_string("[PMR - " + line[0] + " ] " + line[1].replace('\t', ' '))
				result_str = "<html>"

				if(line[2]=='null'):
					question_state = 'UNANSWERED'
				else:
					result_str = result_str +  "<p><b>Related URLs - &nbsp;</b><ul>" 
					hyperlike_lists = line[2].split(',') 
					for hl in hyperlike_lists:
						result_str = result_str + "<li><a href=\"%s\" target=\"_blank\">%s</a></li>&nbsp;&nbsp;" %(hl,hl)
					result_str = result_str +  "</ul></p>"

				if(line[3]=='null'):
					line[3] = ""

				if(line[3]=='Unanswered' or line[5]=='null'):
					question_state = 'UNANSWERED'
				else:		
					result_str = result_str +  "<p><b>Solution - &nbsp;</b>" + line[3] + "</p>"
				
				result_str = result_str +  "</html>"
				
				author_name = config_data['approvers'][author_count % (len(config_data['approvers']))]

				# Prepare SQL query to INSERT a curated record into the database.
				sql = "INSERT INTO curatedcontent (scenario, content,project_id, state, author, question_asker, semanticQuestion) VALUES \
						('%s','%s','%d','%s','%s', '%s', '%s')" \
						% (unicode(MySQLdb.escape_string(question), 'utf-8'), unicode(MySQLdb.escape_string(result_str), 'utf-8'),\
							project_info['project_id'],question_state,\
							author_name ,author_name , unicode(MySQLdb.escape_string(question), 'utf-8') )
				curation_id = -1
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

				## Adding entry to tag activity 
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

				## Adding entry to activity 
				sql = "INSERT INTO activity (id, activity_type, user, projectid) VALUES ('%d', '%s', '%s', '%d')" \
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


				### Alternative question adding to the altquestion table 
				alt_question_id = -1
				if (line[3] == 'Unanswered' or line[4]=='null' ):
					continue
				sql = "INSERT INTO altquestions (curation_id, question, type) VALUES ('%d', '%s', '%s')" % (curation_id, \
					 unicode(MySQLdb.escape_string(line[4]), 'utf-8') , 'content')
				try:
				   # Execute the SQL command
				   cursor.execute(sql)
				   alt_question_id = cursor.lastrowid
				   # Commit your changes in the database
				   db.commit()
				except:
				   # Rollback in case there is any error
				   print "Error: in altquestion(null link) " + line[0]
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
					   print "Error: in altquestion activity" + line[0]
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
