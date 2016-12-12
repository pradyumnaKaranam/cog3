#!/usr/bin/env python

import os
import sys
import subprocess
import time
from bs4 import BeautifulSoup
import csv
import json
from os import listdir
import io
import unicodecsv
import requests


def techNotesData(config_data, technotes_filename):

    inputfile = technotes_filename
    inFile = open(inputfile,"rb")
    reader=csv.reader(inFile)
    
    header = reader.next()
    header = [x.lower() for x in header]
    
    try:
        titleInd = header.index('title')
    except:
        print "'title' header missing in input file"
        sys.exit()
    try:
        resolInd = header.index('doc url')
    except:
        print "'doc url' header missing/misspelt in input file"
        sys.exit()

    outputfile =  config_data["project_name"] + "_TechNotesData.csv"
    outFile = open(outputfile,"wb")
    writer = unicodecsv.writer(outFile)
    
    header = ["id","question","links","resolution","alt_ques", "primary_link"]
    writer.writerow(header)
    attribute = {}

    url="http://www-01.ibm.com/support/docview.wss?uid=swg2"
    

    for row in reader:
        resolIndex = row[resolInd]
        attribute[resolIndex] = {}

    inFile.seek(0)
    reader.next()
    
    for row in reader:
        resolIndex = row[resolInd]
        attribute[resolIndex]['id'] = resolIndex
        attribute[resolIndex]['question'] = row[titleInd]
        resol = url+row[resolInd]
        attribute[resolIndex]['resolution'] = resol
        attribute[resolIndex]['alt_ques'] = 'null'
        attribute[resolIndex]['links'] = resol
        attribute[resolIndex]['primary_link'] = resol

    for attrib in attribute:
        finalattrib = []
        for i in range(0,len(header)):
            finalattrib.append(attribute[attrib][header[i]])
        writer.writerow(finalattrib)
     
    outFile.close()
    inFile.close()
