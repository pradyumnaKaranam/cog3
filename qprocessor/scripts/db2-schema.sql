create table catasks ( id int not null generated always as identity (increment by 1), project_id int not null, domain varchar(255) not null, user varchar(255), status varchar(255) not null, time_submitted timestamp not null, time_started timestamp, time_completed timestamp, last_update_time timestamp, info clob, processor_id integer, language varchar(255) not null, primary key (id)) 

