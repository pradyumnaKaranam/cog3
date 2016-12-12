#!/usr/bin/python

import json
import os
import re
import io
import codecs
import sys
import csv
import unicodecsv
from sets import Set

if __name__ == '__main__':
	quality_file = open("Test_withQuesAns.csv", 'r')
	resolved = 0
	contributed = 0
	quality_type = Set()
	quality_dict = {}
	all_lines = csv.reader(quality_file)
	all_lines.next()

	apar_dict = {}
	
	count = 0 
	unans_count = 0
	multi_apar_count = 0 
	while True:
		try:
			line = all_lines.next()
			count = count + 1
			link = line[5]
			# print link + " ==> " + line[5] 
			
			#for link in links:
			if(link=='null'):
				unans_count = unans_count + 1
				print link, line[3]
			else:
				if(link in apar_dict.keys()):
					#print link, line[0], line[5], line[3]
					apar_dict[link].append(line[0]) 
				else:
					multi_apar_count = multi_apar_count + 1
					apar_dict[link] = [line[0]]
		except csv.Error:
			print "Line Number - " + str(count) +  " Problem while Ingesting id "
		except StopIteration:
			print "File end"
			break
		except:
			print "Errro with the while loop"
	
	#multi_apar_count = 0 
	associate_pmr_count = 0
	for ii in apar_dict.keys():
		#multi_apar_count = multi_apar_count + 1 
		if(ii != 'null' and len(apar_dict[ii])>1):
			associate_pmr_count = associate_pmr_count + len(apar_dict[ii])
			print ii + " lenght = " +str(len(apar_dict[ii]))
	print "Total pmrs " + str(count)
	print "Apar count " + str(multi_apar_count)
	print "Associated pmr count " + str(associate_pmr_count)
	print "Unanswered count " + str(unans_count)	
