create table catasks (
	id int auto_increment,
	project_id int not null,
	domain varchar(255) not null,
	user varchar(255),
	status varchar(255) not null,
	time_submitted datetime not null,
	time_started datetime,
	time_completed datetime,
	last_update_time datetime,
	info mediumtext,
	processor_id integer,
	language varchar(255) not null,
	primary key (id)
);
