#!/usr/bin/python
import MySQLdb
import sys
import csv
import re
import sys  
import codecs

def Ingestion(config_data, project_info):

#if __name__ == '__main__':
	
	input_data = open("GCSS.csv", 'r')
	all_lines = csv.reader(input_data , delimiter=',')
	next(all_lines, None)  # skip the headers # if header is there 

	# Open database connection
	db = MySQLdb.connect(host = config_data['icurate_db_host'], port = config_data['icurate_db_port'], \
		user = config_data['icurate_db_user'], passwd = config_data['icurate_db_password'], db = config_data['icurate_db_dbname'], charset='utf8' )
	# prepare a cursor object using cursor() method
	cursor = db.cursor()
	
	author_count = 0 
	no_question_count = 0

	while True:
		author_count = author_count + 1
		try:
			line = all_lines.next()
			print str(author_count) +  " Ingesting id "

			# for i in xrange(0,len(line)):
			# 	if(len(line[1])==0):
			# 		continue
			# 	print "Column # ", str(i) 
			# 	print line[i]
			# 	print "end line\n" 

			if(len(line[1]) == 0):
				no_question_count = no_question_count + 1
				print "No question skipping id " + str(author_count) 
				continue

			if(line[1] != 'null'):

				question_state = 'APPROVED'
				question = MySQLdb.escape_string(line[2].replace('\t', ' ')) # Question Statement 

				result_str = "<html>"
				result_str = result_str +  "<p>"
				result_str = result_str + line[3] 
				result_str = result_str +  "</p>"
				result_str = result_str +  "</html>" # Answer Statement 
				
				author_name =  line[4] #config_data['authors'][author_count % (len(config_data['authors']))]
				approver_name = line[5] #config_data['admins'][0]
				tag_name = line[1]

				### Get the tag by name 
				sql = "SELECT id FROM tags WHERE name='%s'" % (tag_name)
				try:
				   # Execute the SQL command
				   cursor.execute(sql)
				   tag_id = cursor.fetchone()[0]
				except:
				   # Rollback in case there is any error
				   tag_id = -1
				   db.rollback()	
				if(tag_id == -1):
					### Adding tag for the porject 
					sql = "INSERT INTO tags (tag_type, name, created_by, functional_area) VALUES ('%s', '%s', '%s', '%s')" \
							% (config_data["tag_type"], tag_name, config_data["admins"][0], config_data["functional_area"])
					try:
					   # Execute the SQL command
					   cursor.execute(sql)
					   tag_id = cursor.lastrowid
					   # Commit your changes in the database
					   db.commit()
					except:
					   # Rollback in case there is any error
					   print "Error in creating tag "
					   db.rollback()
					   continue
				else:
					print "Tag already exists in DB with Tag ID - " + str(tag_id)

				# Prepare SQL query to INSERT a curated record into the database.
				sql = "INSERT INTO curatedcontent (scenario, content,project_id, state, author, approver, question_asker, semanticQuestion) VALUES \
						('%s','%s','%d','%s','%s', '%s','%s', '%s')" \
						% (unicode(MySQLdb.escape_string(question), 'utf-8'), unicode(MySQLdb.escape_string(result_str), 'utf-8'),\
							project_info['project_id'],question_state,\
							author_name, approver_name, author_name, unicode(MySQLdb.escape_string(question), 'utf-8') )
				curation_id = -1
				try:
				   # Execute the SQL command
				   cursor.execute(sql)
				   curation_id = cursor.lastrowid
				   # Commit your changes in the database
				   db.commit()
				except:
				   # Rollback in case there is any error
				   print "Error in INSERT curatedcontent(primary): " + str(author_count)
				   db.rollback()
				if (curation_id==-1):
					continue

				### Adding entry to tag activity 
				sql = "INSERT INTO tagactivity (tag_id, content_type, content_id, tagged_by) VALUES ('%d','%s', '%d', '%s')" \
						% (tag_id, 'content', curation_id, author_name )
				try:
				   # Execute the SQL command
				   cursor.execute(sql)
				   # Commit your changes in the database
				   db.commit()
				except:
				   # Rollback in case there is any error
				   print "Error: in tagactivity " + str(author_count)
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
				   print "Error: in activity " + str(author_count)
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
				   print "Error: in QA entry in error_code_msg " + str(author_count)
				   db.rollback()

				# ### Alternative question adding to the altquestion table 
				# alt_question_id = -1
				# if (line[3] == 'Unanswered' or line[4]=='null' ):
				# 	continue
				# sql = "INSERT INTO altquestions (curation_id, question, type) VALUES ('%d', '%s', '%s')" % (curation_id, \
				# 	 unicode(MySQLdb.escape_string(line[4].replace('\t', ' ')), 'utf-8') , 'content')
				# try:
				#    # Execute the SQL command
				#    cursor.execute(sql)
				#    alt_question_id = cursor.lastrowid
				#    # Commit your changes in the database
				#    db.commit()
				# except:
				#    # Rollback in case there is any error
				#    print "Error: in altquestion(primary) " + line[0]
				#    db.rollback()
				# if(alt_question_id != -1):
				# 	### Adding activity of alternative question
				# 	sql = "INSERT INTO activity (id, activity_type, user, projectid) VALUES ('%d', '%s', '%s', '%d')" \
				# 		% (alt_question_id, 'altquestion', author_name, project_info['project_id'])
				# 	try:
				# 	   # Execute the SQL command
				# 	   cursor.execute(sql)
				# 	   # Commit your changes in the database
				# 	   db.commit()
				# 	except:
				# 	   # Rollback in case there is any error
				# 	   print "Error: in altquestion activity " + line[0]
				# 	   db.rollback()
		except csv.Error:
			print "Line Number - " + str(author_count) +  " Problem while Ingesting id "
		except StopIteration:
			print "Dumping File end"
			break
		except:
			print "Insertion errro with the while loop"

	print "Question with no title " + str(no_question_count)
	
	# db.close()