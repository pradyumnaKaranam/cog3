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
	print "\nGetting Error Message Data\n"
	try:
		errorMsgnoHTML.errorMsgData(config_data, sys.argv[1])
		print "XML Parsing finished. Error code/messages csv file has been generated"
	except:
		print "Problem with dumping answers from linked solution"
	### End of linking code 