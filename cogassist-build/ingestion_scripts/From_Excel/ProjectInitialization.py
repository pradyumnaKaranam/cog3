#!/usr/bin/python
import MySQLdb
import sys
import csv
import re
import sys  
import codecs

def createProject(config_data):
	
	project_name = config_data["project_name"] 
	project_description= config_data["project_description"]
	project_type = config_data["project_type"]
	project_id = -1
	project_tag_id = -1
	project_existing_flag = 0
	
	# Open database connection
	db = MySQLdb.connect(host = config_data['icurate_db_host'], port = config_data['icurate_db_port'], \
		user = config_data['icurate_db_user'], passwd = config_data['icurate_db_password'], db = config_data['icurate_db_dbname'], charset='utf8' )
	# prepare a cursor object using cursor() method
	cursor = db.cursor()
	
	### Get the project ID by name 
	sql = "SELECT id FROM project WHERE name='%s'" % (config_data['project_name'])
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   project_id = cursor.fetchone()[0]
	except:
		project_id = -1
	   	# Rollback in case there is any error
	   	db.rollback()		
	if(project_id == -1):
		### Prepare SQL query to INSERT a project into the database.
		sql = "INSERT INTO project (name, description, type) VALUES \
				('%s', '%s', '%s')" \
				% (unicode(MySQLdb.escape_string(project_name), 'utf-8'), \
					unicode(MySQLdb.escape_string(project_description), 'utf-8'), unicode(MySQLdb.escape_string(project_type), 'utf-8'))
		try:
		   # Execute the SQL command
		   cursor.execute(sql)
		   project_id = cursor.lastrowid
		   # Commit your changes in the database
		   db.commit()
		except:
		   # Rollback in case there is any error
		   print "Error while creating Project entry" 
		   db.rollback()
	else:
		project_existing_flag = 1
		print "Project already exists in DB with ID - " + str(project_id)

	### Get the project tag by name 
	sql = "SELECT id FROM tags WHERE name='%s'" % (config_data["tag_name"])
	try:
	   # Execute the SQL command
	   cursor.execute(sql)
	   project_tag_id = cursor.fetchone()[0]
	except:
	   # Rollback in case there is any error
	   project_tag_id = -1
	   db.rollback()	
	if(project_tag_id == -1):
		### Adding tag for the porject 
		sql = "INSERT INTO tags (tag_type, name, created_by, functional_area) VALUES ('%s', '%s', '%s', '%s')" \
				% (config_data["tag_type"], config_data["tag_name"], config_data["admins"][0], config_data["functional_area"])
		try:
		   # Execute the SQL command
		   cursor.execute(sql)
		   project_tag_id = cursor.lastrowid
		   # Commit your changes in the database
		   db.commit()
		except:
		   # Rollback in case there is any error
		   print "Error in creating tag "
		   db.rollback()
	else:
		print "Tag for project already exists in DB with Tag ID - " + str(project_tag_id)


	if (project_id==-1 or project_tag_id==-1):
		return -1

	if(project_existing_flag==0):
		###Adding admins role
		print "Adding admins role" 
		for admin in config_data['admins']:
			sql = "INSERT INTO project_userrole (project_id, user_id, role_id) VALUES ('%d', '%s', '%d')" \
			% (project_id, admin, 3)
			try: 
				cursor.execute(sql)
				db.commit()
			except:
				print "Error in creating admin role " + admin
				db.rollback()

		###Adding approvers role 
		print "Adding approvers role"
		for approver in config_data['approvers']:
			sql = "INSERT INTO project_userrole (project_id, user_id, role_id) VALUES ('%d', '%s', '%d')" \
			% (project_id, approver, 2)
			try: 
				cursor.execute(sql)
				db.commit()
			except:
				print "Error in creating admin role " + approver
				db.rollback()

		###Adding authors role 
		print "Adding authors role"
		for author in config_data['authors']:
			sql = "INSERT INTO project_userrole (project_id, user_id, role_id) VALUES ('%d', '%s', '%d')" \
			% (project_id, author, 1)
			try: 
				cursor.execute(sql)
				db.commit()
			except:
				print "Error in creating admin role " + author
				db.rollback()

	db.close()
	return {"project_id":project_id, "project_tag_id":project_tag_id }