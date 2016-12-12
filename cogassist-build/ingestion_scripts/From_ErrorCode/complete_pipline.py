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
import errorMsgnoHTML
import ProjectInitialization

if __name__ == '__main__':

	## Check for the config file
	try:
		with codecs.open('configure.json', 'r') as data_file:
			config_data = json.load(data_file)
	except IOError:
		print "Error: No file name as configure.json"


	### Call to code for getting resolution and alternative question
	# print "\nGetting Error Message Data\n"
	# try:
	# 	errorMsgnoHTML.errorMsgData(config_data, sys.argv[1])
	# except:
	# 	print "Problem with dumping answers from linked solution"
	# ### End of linking code 

	# ### Initialize the Project (Creating project and Tag for it)
	# print "Initializing the Project for component"
	# project_info = -1
	# try:
	# 	project_info = ProjectInitialization.createProject(config_data)
	# except:
	# 	print "Problem in Project Initialization"
	# ### End of project initialization 

	# if(project_info == -1):
	# 	print "Error while creating project. No data ingested. No role created."
	# else:
	# 	print "project creation is successful"
	# 	print project_info, "\n"	
	# 	### ingest data to db call take the data from same file 
	# 	print "Ingesting the data to icurate"
	# 	try:
	# 		ingestDataToDB.Ingestion(config_data, project_info)
	# 	except:
	# 		print "Problem with dumping data from cloudant"
	# 	print "Data successfully ingested"
	# 	### Data Ingeston End

