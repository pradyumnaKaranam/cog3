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
		print "Project name " + config_data['project_name'] + " found with ID: " + str(projectid) + "\nRetriving data for NLC Traning"

	### Getting Data from curatedcontent and altquestion 
	sql = "select semanticQuestion AS text, CONCAT('CUR' , CAST(cc.id AS CHAR)) AS classes \
			from curatedcontent cc \
			where cc.project_id=%d \
 			union \
			select aq.question as text, CONCAT('CUR' , CAST(aq.curation_id AS CHAR)) AS classes \
			from curatedcontent cc, altquestions aq \
			where cc.id=aq.curation_id  and cc.project_id=%d and aq.type='content';" % (projectid,projectid)
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   nlc_data = cursor.fetchall()
	except:
	   # Rollback in case there is any error
	   print "NLC Data Retriving Query Failed."
	   db.rollback()		

	nlc_writer = unicodecsv.writer(open(config_data['project_name'] + "_NLCData.csv", "wb"), lineterminator="\n" , delimiter=',' )
	nlc_header = ["text", "classes"]
	nlc_writer.writerow(nlc_header)  
	for data in nlc_data:
		refine_data = []
		### Removing Newline character from title
		refine_data.append(data[0].replace('\n', ' ').replace('\t', ' '))
		refine_data.append(data[1])
		nlc_writer.writerow(refine_data)
	
	print "Data successfully extracted."
	db.close()
