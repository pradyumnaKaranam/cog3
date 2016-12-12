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

def answerData(config_data, excel_filename):

    inputfile = excel_filename
    inFile = open(inputfile,"rb")
    reader=csv.reader(inFile)

    outputfile = config_data["project_name"] + "_Excel_withQuesAns.csv"
    outFile = open(outputfile,"wb")
    writer = unicodecsv.writer(outFile)
    
    header = ["pmrid","question","links","question_tag","cluster_tag","resolution","alt_ques"]
    writer.writerow(header)
    attribute = {}
    
    ct = 0
    for row in reader:
        try:
            ct += 1
            print ct
            resolution = 'F'
            question = 'F'
            attribute = {}  #This dictionary will contain the value for each header field
            attribute['pmrid'] = row[0]
            attribute['question'] = row[1]
            attribute['resolution'] = 'Unanswered'
            attribute['alt_ques'] = 'null'
            attribute['links'] = 'null'

            if (row[3] == "Y"): 
                if row[2].strip().lower() == 'unanswered':
                    resolution = 'T'
                else:
                    save_row2 = row[2]
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
                                    resolution = 'T'
                                if h[0] == "Question" or h[0].startswith("Problem(Abstract)"):    #Key headers for alternate question
                                    ques = get_ques(soup,section)
                                    ques = unicode(ques).encode("utf-8")
                                    ques = ques.replace('\n', ' ')
                                    attribute['alt_ques'] = ques
                                    print attribute['alt_ques']
                                    question = 'T'
                    row[2] = save_row2
                res_data = ""
                if(len(attribute['resolution'])==0):
                    res_data = "Unanswered"
                    alt_ques_data = "null"
                else:
                    res_data = attribute['resolution']
                    alt_ques_data = attribute['alt_ques']
                finalattrib = row + [res_data,alt_ques_data]
                writer.writerow(finalattrib)
            else:
                writer.writerow(row)
        except:
            print "Error while getting answer for " + row[0]
        
    outFile.close()
    inFile.close()

