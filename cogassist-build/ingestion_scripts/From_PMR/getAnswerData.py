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

def make_soup(url):
    response = requests.get(url)
    html = response.content
    return BeautifulSoup(html, "html.parser")

def get_ques(soup,section):
    siblings = section.fetchNextSiblings()
    text = ''
    for sibling in siblings:
        if sibling.name == 'h2':
            break
        else:
            text += sibling.text
    return text

def get_resolution(soup,section):
    siblings = section.fetchNextSiblings()
    text = ''
    for sibling in siblings:
        if sibling.name == 'h2':
            break
        else:
            text += str(sibling)
    return text

def answerData(config_data):

    inputfile = "cloudant_dump_" + config_data["project_name"] + ".csv"  
    inFile = open(inputfile,"rb")
    reader=csv.reader(inFile)

    outputfile = config_data["project_name"] + "_withQuesAns.csv"
    outFile = open(outputfile,"wb")
    writer = unicodecsv.writer(outFile)
    
    header = ["pmrid","question","links","resolution","alt_ques", "primary_link"]
    writer.writerow(header)
    attribute = {}
    
    ct = 0
    for row in reader:
        try:
            ct += 1
            print "fetching answer-text",ct, row[0]
            resolution = 'F'
            question = 'F'
            attribute = {}  #This dictionary will contain the value for each header field
            attribute['pmrid'] = row[0]
            attribute['question'] = row[1]
            attribute['resolution'] = 'Unanswered'
            attribute['alt_ques'] = 'null'
            attribute['links'] = 'null'
            attribute['primary_link'] = 'null'

            if row[2].strip().lower() == 'unanswered':
                resolution = 'T'
            else:
                row[2] = row[2].split()
                attribute['links'] = ','.join(row[2])

                #get the appropriate html content
                for url in row[2]:
                    if resolution == 'T':
                        break
                    if 'uid=swg' in url:   #Only curate from those links which has this substring
                        soup = make_soup(url)
                        
                        for section in soup.find_all('h2'):
                            h = section.find_all(text=True)[0].split('.')
                            if h[0] == "Resolving the problem" or h[0] == "Answer":    #Key headers for resolution
                                resol = get_resolution(soup,section)
                                #resol = unicode(resol).encode("utf-8")
                                resol = resol.replace('\n', ' ')
                                attribute['resolution'] = resol
                                attribute['primary_link'] = url
                                resolution = 'T'
                            if h[0] == "Question" or h[0].startswith("Problem(Abstract)"):    #Key headers for alternate question
                                ques = get_ques(soup,section)
                                ques = unicode(ques).encode("utf-8")
                                ques = ques.replace('\n', ' ')
                                attribute['alt_ques'] = ques
                                question = 'T'
            finalattrib = []
            for i in range(0,len(header)):
                finalattrib.append(attribute[header[i]])
            writer.writerow(finalattrib)
        except:
            print "Error while getting answer for " + row[0]
        
    outFile.close()
    inFile.close()

