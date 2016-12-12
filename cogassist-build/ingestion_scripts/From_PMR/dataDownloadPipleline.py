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
import getDataFromCloudant
import ingestDataToDB
import getAnswerData
import ProjectInitialization

if __name__ == '__main__':

	## Check for the config file
	try:
		with codecs.open('configure.json', 'r') as data_file:
			config_data = json.load(data_file)
	except IOError:
		print "Error: No file name as configure.json"

	### Will create cloudant_dump.csv file 
	print "Getting PMRs from cloudant"
	try:
		getDataFromCloudant.fetchData(config_data)
	except:
		print "Problem with dumping data from cloudant"
	### finish dumping


	### Call to code for getting resolution and alternative question
	print "\nGetting answers/resolution and alternative question\n"
	try:
		getAnswerData.answerData(config_data)
		print "Data download finished"
	except:
		print "Problem with dumping answers from linked solution"
	### End of linking code 
