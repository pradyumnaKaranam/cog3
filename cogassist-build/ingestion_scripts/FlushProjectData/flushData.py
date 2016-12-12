#!/usr/bin/python
import MySQLdb
import sys
import csv
import re
import sys  
import codecs
import json
import unicodecsv


if __name__ == '__main__':
	
	## Check for the config file
	try:
		with codecs.open('configure.json', 'r') as data_file:
			config_data = json.load(data_file)
	except IOError:
		print "Error: No file name as configure.json"


	
	# Open database connection
	db = MySQLdb.connect(host = config_data['icurate_db_host'], port = config_data['icurate_db_port'], \
		user = config_data['icurate_db_user'], passwd = config_data['icurate_db_password'], db = config_data['icurate_db_dbname'], charset='utf8' )
	# prepare a cursor object using cursor() method
	cursor = db.cursor()


	### Get the project ID by name 
	projectid = -1
	sql = "SELECT id FROM project WHERE name='%s'" % (config_data['project_name'])
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   projectid = cursor.fetchone()[0]
	except:
	   # Rollback in case there is any error
	   db.rollback()		

	if (projectid == -1):
		print "Didn't find the project relate to this name " + config_data['project_name']
		sys.exit(0)
	else:
		print "Project name " + config_data['project_name'] + " found with ID: " + str(projectid) 

	### Get the project tag by name 
	project_tag_id = -1
	sql = "SELECT id FROM tags WHERE name='%s'" % (config_data["tag_name"])
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   project_tag_id = cursor.fetchone()[0]
	except:
	   # Rollback in case there is any error
	   project_tag_id = -1
	   db.rollback()

	if (project_tag_id == -1):
		print "Didn't find the tag relate to this name " + config_data['tag_name']
		sys.exit(0)
	else:
		print "Tag name " + config_data['tag_name'] + " found with ID: " + str(project_tag_id) 

	user_answer = raw_input("Are you sure, you really want to delete project " + config_data['project_name'] + "? Press Y for yes or N for No - ")

	if(user_answer!='Y'):
		print "No data deleted."
		sys.exit(0)
	else:
		print "\nProcessing with data deletion.\n"

	### Deleting data from error_code_msg 
	sql = "DELETE FROM error_code_msg where project_id=%d;" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from error_code_msg has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from error_code_msg."
	   db.rollback()		

	### Deleting data from activity 
	sql = "DELETE FROM activity where projectid=%d;" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from activity has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from activity."
	   db.rollback()

	### Deleting data from altquestions 
	sql = "DELETE FROM altquestions where curation_id in (select id from curatedcontent where project_id=%d);" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from altquestions has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from altquestions."
	   db.rollback()

	### Deleting data from tagactivity 
	sql = "DELETE FROM tagactivity where content_id in (select id from curatedcontent where project_id=%d);" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from tagactivity has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from tagactivity."
	   db.rollback()

	### Deleting data from curatedcontent 
	sql = "DELETE FROM curatedcontent where project_id=%d;" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from curatedcontent has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from curatedcontent."
	   db.rollback()

	### Deleting data from project_userrole 
	sql = "DELETE FROM project_userrole where project_id=%d;" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from project_userrole has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from project_userrole."
	   db.rollback()

	### Deleting data from project
	sql = "DELETE FROM project where id=%d;" % (projectid)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from project has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from project."
	   db.rollback()

	### Deleting data from tags
	sql = "DELETE FROM tags where id=%d;" % (project_tag_id)
	print "\n" + sql
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   print "Project data from tags has been deleted."
	   db.commit()
	except:
	   # Rollback in case there is any error
	   print "Problem with data deletion from tags."
	   db.rollback()

	print "All data for project has been removed successfully."
	db.close()
