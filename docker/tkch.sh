#!/bin/bash
set -e

PGPASSWORD=$POSTGRES_PASSWORD

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE tkch_test;
    CREATE DATABASE tkch;

    CREATE TABLE public.category (
	    id SERIAL PRIMARY KEY,
	    name VARCHAR(255) NOT NULL,
	    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
	);

	CREATE TABLE public.product (
	    id SERIAL PRIMARY KEY,
	    category_id INTEGER REFERENCES public.category(id) ON DELETE CASCADE,
	    name VARCHAR(255) NOT NULL,
	    description VARCHAR(1024) NOT NULL,
	    price NUMERIC (5, 2) NOT NULL,
	    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
	);
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "tkch_test" <<-EOSQL
    CREATE TABLE public.category (
	    id SERIAL PRIMARY KEY,
	    name VARCHAR(255) NOT NULL,
	    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
	);

	CREATE TABLE public.product (
	    id SERIAL PRIMARY KEY,
	    category_id INTEGER REFERENCES public.category(id) ON DELETE CASCADE,
	    name VARCHAR(255) NOT NULL,
	    description VARCHAR(1024) NOT NULL,
	    price NUMERIC (5, 2) NOT NULL,
	    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
	);
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "tkch" <<-EOSQL
    CREATE TABLE public.category (
	    id SERIAL PRIMARY KEY,
	    name VARCHAR(255) NOT NULL,
	    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
	);

	CREATE TABLE public.product (
	    id SERIAL PRIMARY KEY,
	    category_id INTEGER REFERENCES public.category(id) ON DELETE CASCADE,
	    name VARCHAR(255) NOT NULL,
	    description VARCHAR(1024) NOT NULL,
	    price NUMERIC (5, 2) NOT NULL,
	    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
	);

	INSERT INTO category (name, created_at, modified_at) VALUES ('category-1', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'), ('category-2', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039');

	INSERT INTO product (category_id, name, description, price, created_at, modified_at)
	VALUES (1, 'product-1', 'product-1 of category-1', 1.01, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
	       (1, 'product-2', 'product-2 of category-1', 1.02, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
	       (1, 'product-3', 'product-3 of category-1', 1.03, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
	       (2, 'product-4', 'product-4 of category-2', 2.01, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039');
EOSQL