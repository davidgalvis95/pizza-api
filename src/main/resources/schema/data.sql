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
    CONSTRAINT pizza_size_check CHECK (pizza_size::text = ANY (ARRAY['SMALL'::character varying, 'MEDIUM'::character varying, 'BIG'::character varying, 'NOT_APPLICABLE'::character varying]::text[]))
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

CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    email character varying(255) COLLATE pg_catalog."default",
    password character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT user_id_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_role (
    id uuid NOT NULL,
    user_id uuid,
    role character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT user_role_id_pkey PRIMARY KEY (id),
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT role_check CHECK (role::text = ANY (ARRAY['USER'::character varying, 'MANAGER'::character varying, 'ADMIN'::character varying]::text[]))
);

INSERT INTO product (product_id, type, name)
SELECT '47f85d00-98c4-4a08-b4c6-eeb8bc301a91'::UUID, 'BASE', 'Sicilian' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '0104adaf-10da-472c-8661-a84991be9caf'::UUID, 'BASE', 'Neapolitan' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT 'ca604565-3c07-47c4-b7b7-c64a7592dc96'::UUID, 'BASE', 'New York' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '99b91e74-fb2a-46af-8073-87b19bab5fbf'::UUID, 'BASE', 'Chicago' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT 'a4f88125-8b31-471e-b2e6-dc22eeda4bac'::UUID, 'ADDITION', 'Mozzarella Sauce' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '845f4e12-1471-4087-8f63-181730df1de6'::UUID, 'ADDITION', 'Peperoni' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT 'aa6688d1-95c9-4d1a-8000-263ff3df3a69'::UUID, 'ADDITION', 'Bacon' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '46166f8c-8d3f-46b7-858b-5ffdea957669'::UUID, 'ADDITION', 'Beef' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '9076e375-1c6f-49fd-b019-e0f7ce348be3'::UUID, 'ADDITION', 'Chilli' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT 'ba8d54f6-4226-40d0-948f-42dcaf282384'::UUID, 'ADDITION', 'Chorizo' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '772c6f00-c49a-479b-80cf-1c7aa300c895'::UUID, 'ADDITION', 'Chicken' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '2ebf2247-8cac-4fbd-9cef-3af65e7e126e'::UUID, 'ADDITION', 'Tomato' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '63802484-24bf-42e6-8cee-3e7d28c794e1'::UUID, 'ADDITION', 'Onion' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT 'be89e36d-5701-477c-9158-91e9ce0d8768'::UUID, 'CHEESE', 'Mozzarella' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '60181fe8-0025-4455-a3fb-6027883cedea'::UUID, 'CHEESE', 'Provolone' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '41227ebf-e387-471d-a5c1-a6a74d335312'::UUID, 'CHEESE', 'Parmesan' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1)
UNION ALL
SELECT '1725f081-5415-4904-a1c9-d3e0b95e6eef'::UUID, 'CHEESE', 'Gouda' WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1);

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
SELECT 'b7611773-ae6b-482a-9f03-427712793d32'::UUID, FALSE, 'C_50_OFF', '50 percent off in the total purchase price' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1)
UNION ALL
SELECT '11872936-8d27-4ec8-9c6e-229223eeb7ea'::UUID, TRUE, 'C_30_OFF', '30 percent off in the total purchase price' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1)
UNION ALL
SELECT '7e2c6876-2d5d-4733-a527-b397047821b6'::UUID, FALSE, 'C_2_X_1', '2 pizzas by the price of 1' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1)
UNION ALL
SELECT 'cac097db-527e-489d-8a37-3e20b7371c5b'::UUID, FALSE, 'C_10_USD_OFF_PURCHASE_GRATER_THAN_30', '$10 off if the purchase is greater than $30' WHERE NOT EXISTS (SELECT 1 FROM promotion LIMIT 1);

INSERT INTO users (id, email, password)
--Admin user password = 'password'
SELECT uuid_generate_v4(), 'admin@yopmail.com', '{bcrypt}$2a$10$9G2gzSM3BPQqnf0cmZLGWef17BGO8eLR7bc07MPOxr4dJ5aucK/HC'
WHERE NOT EXISTS (SELECT 1 FROM users);

WITH
    admin_user AS (
        SELECT id
        FROM users LIMIT 1
)
INSERT INTO user_role(id, user_id, role)
SELECT uuid_generate_v4(), au.id, 'ADMIN'
FROM admin_user au WHERE NOT EXISTS (SELECT 1 FROM user_role)