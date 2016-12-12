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
from lxml import etree

def make_soup(url):
    response = requests.get(url)
    html = response.content
    return BeautifulSoup(html, "html.parser")

def get_resolution(soup,section):
    siblings = section.fetchNextSiblings()
    text = ''
    for sibling in siblings:
        if sibling.name == 'h4':
            break
        else:
            text += str(sibling)
    return text

def errorMsgData(config_data, xml_filename):

    url = "http://www.ibm.com/support/knowledgecenter/api/content/nl/en-us/SSZJPZ_8.7.0"

    outputfile =  config_data["project_name"] + "_ErrorMsg.csv"
    outFile = open(outputfile,"wb")
    writer = unicodecsv.writer(outFile)
    
    header = ["id","title","explanation","user_action","system_response","link"]
    writer.writerow(header)

    parser = etree.XMLParser(recover=True)
    doc = etree.parse(xml_filename ,parser)
    root = doc.getroot()
    error_list = root.xpath("//topic[@label='Messages for the parallel engine']")
    
    for topics in error_list[0]:
        if ":" in topics.get('label'):
            for topic in topics:
                link = topic.get("href")
                link = url+link
               
                attr = {}
                ret = requests.head(link)
                   
                if ret.status_code == 200:
                    soup=make_soup(link)
                    errorid = soup.find('h1')
                    attr['link'] = link
                    attr['id'] = errorid.text
                    results = soup.find("p", {"class" : "shortdesc shortdesc msgText"})
                    attr['title']=results.contents[0].replace('\n', ' ')
                    for section in soup.find_all('h4'):
                        if section.text.lower() == 'explanation':
                            text = get_resolution(soup,section)
                            text = text.replace('\n', ' ')
                            attr['explanation'] = text
                        if section.text.lower() == 'system action':
                            text = get_resolution(soup,section)
                            text = text.replace('\n', ' ')
                            attr['system_response'] = text
                        if section.text.lower() == 'user response':
                            text = get_resolution(soup,section)
                            text = text.replace('\n', ' ')
                            attr['user_action'] = text
                    finalattrib = []
                    for i in range(0,len(header)):
                        finalattrib.append(attr[header[i]])
                    writer.writerow(finalattrib)
    outFile.close()
