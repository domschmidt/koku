create table koku.product_manufacturer
(
    id       SERIAL PRIMARY KEY,
    recorded timestamp,
    updated  timestamp,
    deleted  boolean not null default FALSE,
    name     varchar not null default '',
    version  int8    not null default 1 CHECK (version >= 0)
);

create table koku.product
(
    id              SERIAL PRIMARY KEY,
    recorded        timestamp,
    updated         timestamp,
    deleted         boolean not null default FALSE,
    name            varchar not null default '',
    manufacturer_id int8    not null,
    version         int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_manufacturer
        FOREIGN KEY (manufacturer_id)
            REFERENCES koku.product_manufacturer (id)

);
create table koku.product_price_history
(
    id         SERIAL PRIMARY KEY,
    recorded   timestamp,
    updated    timestamp,
    price      numeric(19, 2) CHECK (price >= 0),
    version    int8 not null default 1 CHECK (version >= 0),
    product_id int8 not null,
    CONSTRAINT fk_product
        FOREIGN KEY (product_id)
            REFERENCES koku.product (id)
);