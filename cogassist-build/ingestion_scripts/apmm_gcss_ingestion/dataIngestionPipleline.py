#!/usr/bin/python
import json
import os
import re
import io
import codecs
import sys
import csv
import urllib
import urllib2
import requests
import MySQLdb
import sys
import csv
import re
from sets import Set
import ingestDataToDB
import ProjectInitialization

if __name__ == '__main__':

	## Check for the config file
	try:
		with codecs.open('configure.json', 'r') as data_file:
			config_data = json.load(data_file)
	except IOError:
		print "Error: No file name as configure.json"

	### Initialize the Project (Creating project and Tag for it)
	# print "Initializing the Project for component"
	# project_info = -1
	# try:
	# 	project_info = ProjectInitialization.createProject(config_data)
	# except:
	# 	print "Problem in Project Initialization"
	### End of project initialization 

	### If we are ingesting the data to existing project
	project_info = {"project_id":64, "project_tag_id":200882 }

	### Ingesting Data to DB
	if(project_info == -1):
		print "Error while creating project. No data ingested. No role created."
	else:
		print "project creation is successful"
		print project_info, "\n"	
		### ingest data to db call take the data from same file 
		print "Ingesting the data to icurate"
		try:
			ingestDataToDB.Ingestion(config_data, project_info)
			print "Data successfully ingested"
		except:
			print "Problem with Ingesting data into db "
	### Data Ingeston End


	### Refeshing the solr the index for Error Code
	# print "Refeshing the solr"
	# solr_result = requests.get('http://' + config_data['icurate_db_host'] + ':8080/solr/SAPErrors/dataimport?command=full-import')
	# print "Done! Sole Refreshed. Project is ready to use"
