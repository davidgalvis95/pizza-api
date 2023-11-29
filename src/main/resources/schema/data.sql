CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS product (
    product_id uuid NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    type character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT product_pkey PRIMARY KEY (product_id),
    CONSTRAINT product_type_check CHECK (type::text = ANY (ARRAY['ADDITION'::character varying, 'BASE'::character varying, 'CHEESE'::character varying]::text[]))
    );

CREATE TABLE IF NOT EXISTS price (
    id uuid NOT NULL,
    product_id uuid,
    value integer,
    pizza_size character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT price_id_pkey PRIMARY KEY (id),
    CONSTRAINT product_id_fkey FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT pizza_size_check CHECK (type::text = ANY (ARRAY['SMALL'::character varying, 'MEDIUM'::character varying, 'BIG'::character varying, 'NOT_APPLICABLE'::character varying]::text[]))
    );

CREATE TABLE IF NOT EXISTS inventory (
    id uuid NOT NULL,
    product_id uuid,
    available_quantity integer,
    CONSTRAINT inventory_id_pkey PRIMARY KEY (id),
    CONSTRAINT product_id_fkey FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE IF NOT EXISTS promotion (
    code UUID NOT NULL DEFAULT uuid_generate_v4(),
    active boolean NOT NULL,
    descriptive_code character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT code_pkey PRIMARY KEY (code),
    CONSTRAINT descriptive_code_check CHECK (descriptive_code::text = ANY (ARRAY['C_50_OFF'::character varying, 'C_30_OFF'::character varying, 'C_2_X_1'::character varying, 'C_10_USD_OFF_PURCHASE_GRATER_THAN_30'::character varying]::text[]))
);

INSERT INTO product (product_id, type, name)
SELECT uuid_generate_v4(), 'BASE', 'Sicilian' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'BASE', 'Neapolitan' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'BASE', 'New York' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'BASE', 'Chicago' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Mozzarella Sauce' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Peperoni' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Bacon' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Beef' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Chilli' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Chorizo' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Chicken' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Tomato' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'ADDITION', 'Onion' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'CHEESE', 'Mozzarella' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'CHEESE', 'Provolone' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'CHEESE', 'Parmesan' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), 'CHEESE', 'Gouda' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1);

WITH inserted_products AS (SELECT p.product_id, p.type, p.name FROM product p)
INSERT INTO price (id, product_id, value, pizza_size)
SELECT
    uuid_generate_v4(),
    ip.product_id,
    CASE
        WHEN ip.name = 'Mozzarella Sauce' THEN 2
        WHEN ip.name = 'Peperoni' THEN 3
        WHEN ip.name = 'Bacon' THEN 3
        WHEN ip.name = 'Beef' THEN 3
        WHEN ip.name = 'Chilli' THEN 1
        WHEN ip.name = 'Chorizo' THEN 2
        WHEN ip.name = 'Chicken' THEN 3
        WHEN ip.name = 'Tomato' THEN 2
        WHEN ip.name = 'Onion' THEN 2
        WHEN ip.name = 'Mozzarella' THEN 7
        WHEN ip.name = 'Provolone' THEN 7
        WHEN ip.name = 'Parmesan' THEN 8
        WHEN ip.name = 'Gouda' THEN 9
        ELSE 10
        END AS value,
    'MEDIUM'
FROM inserted_products ip
WHERE ip.type <> 'BASE' AND NOT EXISTS (SELECT 1 FROM price LIMIT 1)
UNION ALL
SELECT
    uuid_generate_v4(),
    ip.product_id,
    CASE
        WHEN ip.name = 'Sicilian' THEN 12
        WHEN ip.name = 'Neapolitan' THEN 10
        WHEN ip.name = 'New York' THEN 13
        WHEN ip.name = 'Chicago' THEN 15
        ELSE 10
        END AS value,
    'NOT_APPLICABLE'
FROM inserted_products ip
WHERE ip.type = 'BASE' AND NOT EXISTS (SELECT 1 FROM price LIMIT 1)
UNION ALL
SELECT
    uuid_generate_v4(),
    ip.product_id,
    CASE
        WHEN ip.name = 'Mozzarella Sauce' THEN 3
        WHEN ip.name = 'Peperoni' THEN 4
        WHEN ip.name = 'Bacon' THEN 4
        WHEN ip.name = 'Beef' THEN 4
        WHEN ip.name = 'Chilli' THEN 2
        WHEN ip.name = 'Chorizo' THEN 3
        WHEN ip.name = 'Chicken' THEN 4
        WHEN ip.name = 'Tomato' THEN 3
        WHEN ip.name = 'Onion' THEN 3
        WHEN ip.name = 'Mozzarella' THEN 11
        WHEN ip.name = 'Provolone' THEN 11
        WHEN ip.name = 'Parmesan' THEN 12
        WHEN ip.name = 'Gouda' THEN 13
        ELSE 10
        END AS value,
    'BIG'
FROM inserted_products ip
WHERE ip.type <> 'BASE' AND NOT EXISTS (SELECT 1 FROM price LIMIT 1)
UNION ALL
SELECT
    uuid_generate_v4(),
    ip.product_id,
    CASE
        WHEN ip.name = 'Mozzarella Sauce' THEN 1
        WHEN ip.name = 'Peperoni' THEN 2
        WHEN ip.name = 'Bacon' THEN 2
        WHEN ip.name = 'Beef' THEN 2
        WHEN ip.name = 'Chilli' THEN 1
        WHEN ip.name = 'Chorizo' THEN 1
        WHEN ip.name = 'Chicken' THEN 2
        WHEN ip.name = 'Tomato' THEN 1
        WHEN ip.name = 'Onion' THEN 1
        WHEN ip.name = 'Mozzarella' THEN 4
        WHEN ip.name = 'Provolone' THEN 5
        WHEN ip.name = 'Parmesan' THEN 5
        WHEN ip.name = 'Gouda' THEN 6
        ELSE 10
        END AS value,
    'SMALL'
FROM inserted_products ip
WHERE ip.type <> 'BASE' AND NOT EXISTS (SELECT 1 FROM price LIMIT 1);

WITH inserted_products AS (SELECT p.product_id, p.type, p.name FROM product p)
INSERT INTO inventory (id, product_id, available_quantity)
SELECT
    uuid_generate_v4(),
    ip.product_id,
    100
FROM inserted_products ip
WHERE NOT EXISTS (SELECT 1 FROM inventory LIMIT 1);

INSERT INTO promotion (code, active, descriptive_code, description)
SELECT uuid_generate_v4(), FALSE, 'C_50_OFF', '50 percent off in the total purchase price' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), TRUE, 'C_30_OFF', '30 percent off in the total purchase price' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), FALSE, 'C_2_X_1', '2 pizzas by the price of 1' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1)
UNION ALL
SELECT uuid_generate_v4(), FALSE, 'C_10_USD_OFF_PURCHASE_GRATER_THAN_30', '$10 off if the purchase is greater than $30' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1);