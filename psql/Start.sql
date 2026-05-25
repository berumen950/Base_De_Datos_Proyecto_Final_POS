-- Porfavor usar \c ProjectDB o ejecutar en DB correcto.

--Pasos a seguir



--CREATE ROLE admin
--WITH LOGIN
--PASSWORD 'admin123';

--CREATE DATABASE ProjectDB;
--GRANT ALL PRIVILEGES ON DATABASE ProjectDB TO admin;
CREATE ROLE admin
WITH LOGIN
PASSWORD 'admin123';

CREATE DATABASE ProjectDB;

GRANT ALL PRIVILEGES ON DATABASE ProjectDB TO admin;

\c ProjectDB

GRANT ALL PRIVILEGES ON SCHEMA public TO admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON TABLES TO admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON SEQUENCES TO admin;


CREATE TABLE customers
(
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(125) NOT NULL,
    last_name VARCHAR(125) NOT NULL,
    address VARCHAR(125) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(125) UNIQUE NOT NULL
);

COMMENT ON COLUMN customers.phone IS 'FIXED:NONE|FORMAT:PHONE';
COMMENT ON COLUMN customers.email IS 'FIXED:NONE|FORMAT:EMAIL';



CREATE TABLE staff
(
    staff_id SERIAL PRIMARY KEY,
    first_name VARCHAR(125) NOT NULL,
    last_name VARCHAR(125) NOT NULL,
    role_staff VARCHAR(125) NOT NULL,
    birth_date DATE NOT NULL,
    address VARCHAR(125) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(125) UNIQUE NOT NULL
);

COMMENT ON COLUMN staff.role_staff IS 'FIXED:Manager,Sales Associate,Cashier,Inventory Manager,Assistant Manager|FORMAT:NONE';
COMMENT ON COLUMN staff.birth_date IS 'FIXED:NONE|FORMAT:DATE';
COMMENT ON COLUMN staff.phone IS 'FIXED:NONE|FORMAT:PHONE';
COMMENT ON COLUMN staff.email IS 'FIXED:NONE|FORMAT:EMAIL';

CREATE TABLE sales_outlets 
(
    sales_outlet_id SERIAL PRIMARY KEY,
    name VARCHAR(125) NOT NULL,
    address VARCHAR(125) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

COMMENT ON COLUMN sales_outlets.phone IS 'FIXED:NONE|FORMAT:PHONE';

CREATE TABLE payment_methods
(
    payment_method_code VARCHAR(125) PRIMARY KEY,
    payment_method_name VARCHAR(125) NOT NULL,
    payment_method_description VARCHAR(200) NOT NULL
);

CREATE TABLE products
(
    product_id SERIAL PRIMARY KEY,

    name VARCHAR(125) NOT NULL,

    description VARCHAR(200) NOT NULL,

    product_code VARCHAR(150) UNIQUE NOT NULL,

    stock INT NOT NULL
        CHECK (stock >= 0),

    wholesale_price NUMERIC(10,2) NOT NULL
        CHECK (wholesale_price >= 0),

    retail_price NUMERIC(10,2) NOT NULL
        CHECK (retail_price >= 0)
);

DROP TABLE IF EXISTS sales_transactions CASCADE;

CREATE TABLE sales_transactions
(
    transaction_id SERIAL PRIMARY KEY,

    transaction_datetime TIMESTAMP NOT NULL,

    wholesale_price NUMERIC(10,2)
        DEFAULT 0
        CHECK (wholesale_price >= 0),

    retail_price NUMERIC(10,2)
        DEFAULT 0
        CHECK (retail_price >= 0),

    customer_id INT NOT NULL,
    staff_id INT NOT NULL,
    sales_outlet_id INT NOT NULL,

    FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    FOREIGN KEY (staff_id)
        REFERENCES staff(staff_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    FOREIGN KEY (sales_outlet_id)
        REFERENCES sales_outlets(sales_outlet_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

COMMENT ON COLUMN sales_transactions.transaction_datetime
IS 'FIXED:NONE|FORMAT:DATETIME';

COMMENT ON COLUMN sales_transactions.transaction_datetime IS 'FIXED:NONE|FORMAT:DATETIME';

CREATE TABLE transaction_products
(
    transaction_id INT NOT NULL,
    product_id INT NOT NULL,

    quantity INT NOT NULL
        CHECK (quantity > 0),

    PRIMARY KEY (transaction_id, product_id),

    FOREIGN KEY (transaction_id)
        REFERENCES sales_transactions(transaction_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE payments
(
    payment_id SERIAL PRIMARY KEY,

    amount NUMERIC(10,2) NOT NULL
        CHECK (amount >= 0),

    payment_date DATE NOT NULL,

    payment_reference VARCHAR(125),

    transaction_id INT NOT NULL,
    payment_method_code VARCHAR(125) NOT NULL,

    FOREIGN KEY (transaction_id)
        REFERENCES sales_transactions(transaction_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (payment_method_code)
        REFERENCES payment_methods(payment_method_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

COMMENT ON COLUMN payments.payment_date IS 'FIXED:NONE|FORMAT:DATE';


CREATE OR REPLACE FUNCTION dataSift()
RETURNS JSON
LANGUAGE sql
AS
$$

SELECT json_agg(

    json_build_object(

        'table', t.table_name,
        'columns',
        (
            SELECT json_agg(

                json_build_object(

                    'name', c.column_name,
                    'type_oid', a.atttypid,
                    'nullable', c.is_nullable = 'YES',
                    'comment', pgd.description,
                    'auto_increment',
                    CASE
                        WHEN c.column_default LIKE 'nextval(%'
                        THEN true
                        ELSE false
                    END,
                    'primary_key',
                    CASE
                        WHEN pk.column_name IS NOT NULL
                        THEN true
                        ELSE false
                    END

                )

                ORDER BY c.ordinal_position

            )

            FROM information_schema.columns c

            LEFT JOIN pg_catalog.pg_class pc
                ON pc.relname = c.table_name
                AND pc.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')

            LEFT JOIN pg_catalog.pg_attribute a
                ON a.attrelid = pc.oid
                AND a.attname = c.column_name

            LEFT JOIN pg_catalog.pg_description pgd
                ON pgd.objoid = pc.oid
                AND pgd.objsubid = a.attnum

            LEFT JOIN (

                SELECT
                    kcu.table_name,
                    kcu.column_name

                FROM information_schema.table_constraints tc

                JOIN information_schema.key_column_usage kcu
                    ON tc.constraint_name = kcu.constraint_name

                WHERE tc.constraint_type = 'PRIMARY KEY'
                  AND tc.table_schema = 'public'

            ) pk

                ON c.table_name = pk.table_name
                AND c.column_name = pk.column_name

            WHERE c.table_name = t.table_name
            AND c.table_schema = 'public'

        )

    )

)

FROM (

    SELECT DISTINCT table_name

    FROM information_schema.columns

    WHERE table_schema = 'public'

) t;

$$;




CREATE OR REPLACE FUNCTION fin_transaction(
    target_transaction_id INT
)
RETURNS VOID
LANGUAGE plpgsql
AS
$$
DECLARE
    total_wholesale NUMERIC(10,2):=0;
    total_retail NUMERIC(10,2):=0;
BEGIN

    
    IF NOT EXISTS(
        SELECT 1
        FROM sales_transactions
        WHERE transaction_id=target_transaction_id
    )
    THEN
        RAISE EXCEPTION
        'Transaction % does not exist',
        target_transaction_id;
    END IF;


    
    IF NOT EXISTS(
        SELECT 1
        FROM transaction_products
        WHERE transaction_id=target_transaction_id
    )
    THEN
        RAISE EXCEPTION
        'Transaction % has no products',
        target_transaction_id;
    END IF;


    
    PERFORM 1
    FROM products p
    JOIN transaction_products tp
        ON p.product_id=tp.product_id
    WHERE tp.transaction_id=target_transaction_id
    FOR UPDATE;


    
    IF EXISTS(
        SELECT 1
        FROM transaction_products tp
        JOIN products p
            ON p.product_id=tp.product_id
        WHERE tp.transaction_id=target_transaction_id
        AND p.stock < tp.quantity
    )
    THEN
        RAISE EXCEPTION
        'Insufficient stock for one or more products';
    END IF;


   
    SELECT
        COALESCE(
            SUM(tp.quantity*p.wholesale_price),
            0
        ),

        COALESCE(
            SUM(tp.quantity*p.retail_price),
            0
        )

    INTO
        total_wholesale,
        total_retail

    FROM transaction_products tp
    JOIN products p
        ON p.product_id=tp.product_id
    WHERE tp.transaction_id=target_transaction_id;


    
    UPDATE sales_transactions
    SET
        wholesale_price=total_wholesale,
        retail_price=total_retail
    WHERE transaction_id=target_transaction_id;


    
    UPDATE products p
    SET stock=p.stock-tp.quantity
    FROM transaction_products tp
    WHERE tp.product_id=p.product_id
    AND tp.transaction_id=target_transaction_id;

END;
$$;





CREATE OR REPLACE FUNCTION add_fixed_value(
    p_table_name TEXT,
    p_column_name TEXT,
    p_new_value TEXT
)
RETURNS VOID
LANGUAGE plpgsql
AS
$$
DECLARE
    current_comment TEXT;
    fixed_part TEXT;
    format_part TEXT;
BEGIN

    SELECT pgd.description
    INTO current_comment
    FROM pg_catalog.pg_statio_all_tables st
    JOIN pg_catalog.pg_description pgd
      ON pgd.objoid = st.relid
    JOIN information_schema.columns c
      ON c.ordinal_position = pgd.objsubid
     AND c.table_name = st.relname
    WHERE c.table_name = p_table_name
      AND c.column_name = p_column_name;

    
    fixed_part := substring(current_comment FROM 'FIXED:([^|]+)');
    
    
    format_part := substring(current_comment FROM 'FORMAT:([^|]+)');

    
    IF fixed_part = 'NONE' OR fixed_part IS NULL THEN
        fixed_part := p_new_value;
    ELSE
        fixed_part := fixed_part || ',' || p_new_value;
    END IF;

    
    IF format_part IS NOT NULL THEN
        current_comment := 'FIXED:' || fixed_part || '|FORMAT:' || format_part;
    ELSE
        current_comment := 'FIXED:' || fixed_part || '|FORMAT:NONE';
    END IF;

    EXECUTE format(
        'COMMENT ON COLUMN %I.%I IS %L',
        p_table_name,
        p_column_name,
        current_comment
    );

END;
$$;





CREATE OR REPLACE FUNCTION low_stock_trigger()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
	IF NEW.stock <=10 THEN
	
		 PERFORM pg_notify(
            'low_stock_channel',
            'Low stock for product: ' ||
            NEW.name ||
            ' Stock: ' ||
            NEW.stock
        );

	END IF;

	RETURN NEW;

END;
$$;

CREATE TRIGGER trg_lowStock
AFTER UPDATE OF stock
ON products FOR EACH ROW EXECUTE FUNCTION low_stock_trigger();




CREATE OR REPLACE FUNCTION validate_fixed_comment()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
DECLARE
    col_record RECORD;
    fixed_values TEXT;
    col_value TEXT;
    valid_values TEXT[];
    trimmed_value TEXT;
BEGIN
    FOR col_record IN 
        SELECT 
            c.column_name,
            pgd.description as comment
        FROM information_schema.columns c
        LEFT JOIN pg_catalog.pg_class pc
            ON pc.relname = c.table_name
            AND pc.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')
        LEFT JOIN pg_catalog.pg_attribute a
            ON a.attrelid = pc.oid
            AND a.attname = c.column_name
        LEFT JOIN pg_catalog.pg_description pgd
            ON pgd.objoid = pc.oid
            AND pgd.objsubid = a.attnum
        WHERE c.table_name = TG_TABLE_NAME
          AND pgd.description IS NOT NULL
    LOOP
        
        fixed_values := substring(col_record.comment FROM 'FIXED:([^|]+)');
        
       
        CONTINUE WHEN fixed_values IS NULL OR trim(fixed_values) = 'NONE';
        
        
        EXECUTE format('SELECT ($1).%I::TEXT', col_record.column_name) 
            USING NEW INTO col_value;
        
        
        CONTINUE WHEN col_value IS NULL;
        
        
        trimmed_value := trim(col_value);
        
        
        valid_values := string_to_array(fixed_values, ',');
        
        
        valid_values := ARRAY(
            SELECT trim(v) FROM unnest(valid_values) AS v
        );
        
        
        IF NOT (trimmed_value = ANY(valid_values)) THEN
            RAISE EXCEPTION 'Invalid value for %.%. Expected one of: %. Got: %',
                TG_TABLE_NAME, col_record.column_name, 
                array_to_string(valid_values, ', '), trimmed_value;
        END IF;
        
    END LOOP;
    
    RETURN NEW;
END;
$$;


CREATE TRIGGER validate_customers
BEFORE INSERT OR UPDATE ON customers
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_staff
BEFORE INSERT OR UPDATE ON staff
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_sales_outlets
BEFORE INSERT OR UPDATE ON sales_outlets
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_sales_transactions
BEFORE INSERT OR UPDATE ON sales_transactions
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_payments
BEFORE INSERT OR UPDATE ON payments
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_products
BEFORE INSERT OR UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_payment_methods
BEFORE INSERT OR UPDATE ON payment_methods
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();

CREATE TRIGGER validate_transaction_products
BEFORE INSERT OR UPDATE ON transaction_products
FOR EACH ROW EXECUTE FUNCTION validate_fixed_comment();


GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;

GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;

ALTER DEFAULT PRIVILEGES
GRANT ALL ON TABLES TO admin;

ALTER DEFAULT PRIVILEGES
GRANT ALL ON SEQUENCES TO admin;  