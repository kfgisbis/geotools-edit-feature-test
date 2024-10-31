set client_encoding to 'UTF8';

create schema if not exists bis;

create extension if not exists postgis;

CREATE TABLE bis.test (
	id uuid NOT NULL,
	geometry public.geometry(geometry, 4326) NULL,
	CONSTRAINT test_pkey PRIMARY KEY (id)
);

-- insert into bis.test
--     (id, geometry)
-- values
--     (1, ST_GeomFromText('POLYGON ((30.47081103896594 59.68653786187065, 30.577346878776893 59.68391610100218, 30.548060574264543 59.640431917476235, 30.476999842938366 59.63987334565408, 30.434672844341275 59.66578128802311, 30.50849643458372 59.66818122656704, 30.47081103896594 59.68653786187065))', 4326))