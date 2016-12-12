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
from sets import Set
import unicodecsv


def fetchData( config_data ):
	username = config_data['cloudant_username']
	passwd = config_data['cloudant_password']
	url = config_data['cloudant_component_view_url']

	### Prepare urllib to query to the Cloudant
	passman = urllib2.HTTPPasswordMgrWithDefaultRealm()
	passman.add_password(None, url, username, passwd)
	authhandler = urllib2.HTTPBasicAuthHandler(passman)
	opener = urllib2.build_opener(authhandler)
	urllib2.install_opener(opener)
	pagehandle = urllib2.urlopen(url)

	json_obj = json.load(pagehandle)
	### Getting all pmrs for project
	list_data = json_obj['rows']

	try:
		cloudant_dump_fp = open('cloudant_dump_'+ config_data['project_name'] + '.csv' , 'w')
		cloudant_dump = unicodecsv.writer(cloudant_dump_fp, delimiter=',', quoting=csv.QUOTE_NONNUMERIC )
	except IOError:
		print "Error: Problem with dumping file from cloudant"

	UnAnswered_count = 0 
	duplicate_count = 0 
	pmr_count = 1
	for each_data in list_data:
		try:
			print pmr_count,  each_data['id']	
			pmr_count = pmr_count + 1
			url = config_data['cloudant_pmr_answer_view_url'] + '?key=\"' + each_data['id'] + '\"&include_docs=true'
			passman.add_password(None, url, username, passwd)
			authhandler = urllib2.HTTPBasicAuthHandler(passman)
			opener = urllib2.build_opener(authhandler)
			urllib2.install_opener(opener)
			pagehandle = urllib2.urlopen(url)
			junction_json_obj = json.load(pagehandle)

			list_mapping = junction_json_obj['rows']
			### Here we are getting pmrid and pmr titl. 
			pmr_row = [each_data['id'], each_data['key'][1].replace("'", "").replace("\"", "") ]
			answer_link = ""
			
			resolved_link = Set()
			contributed_link = Set()
			mapping_set = Set()

			if(len(list_mapping) > 0):
				for  ii in list_mapping:
					if('quality' in ii['doc'].keys() ):
						if(ii['doc']['quality']=='resolved'):
							resolved_link.add(ii['doc']['answer'])
							break
						elif(ii['doc']['quality']=='contributed'):
							contributed_link.add(ii['doc']['answer'])
					if('answer' in ii['doc'].keys()):
						mapping_set.add(ii['doc']['answer'])

				### resolved or contributed is there rewrite the mapping_set-which has all the links 	
				if(len(resolved_link)>0):
					print "Resolved", each_data['id']
					mapping_set = resolved_link
				elif(len(contributed_link)>0):	
					print "Contributed", each_data['id']
					mapping_set = contributed_link	

				if(len(mapping_set)==0):
					UnAnswered_count = UnAnswered_count + 1
					answer_link = "UnAnswered"
				else:
					if(len(mapping_set)>1):
						print "multiple urls ", each_data['id']
						duplicate_count = duplicate_count + 1
					for  ii in mapping_set:	
						answer_link = answer_link + ii + " "
			else:
				UnAnswered_count = UnAnswered_count + 1
				answer_link = "UnAnswered"
			pmr_row.append(answer_link)
			cloudant_dump.writerow(pmr_row)
		except:
			print "Error occur while processing the pmr " + each_data['id']
	cloudant_dump_fp.close()


