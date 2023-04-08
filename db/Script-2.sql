create database gizlo_test;

create table customers (
	customer_id int identity,
	dni varchar(10),
	name varchar(255),
	surname varchar(255),
	primary key (customer_id)
);

create table customers (
	customer_id serial primary key,
	dni varchar(10),
	name varchar(255),
	surname varchar(255)
);

drop table customers;

insert
	into
	customers (dni,
	name,
	surname)
values ('0956257497',
'LUIS JEANPIER',
'MENDOZA NAVARRO');

insert
	into
	customers (dni,
	name,
	surname)
values ('0947663545',
'KALEB DAVID',
'CHARA TOALA');

select * from customers;