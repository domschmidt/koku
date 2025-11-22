create table koku.customer
(
    id                    SERIAL PRIMARY KEY,
    recorded              timestamp,
    updated               timestamp,
    deleted               boolean not null default FALSE,
    firstname             varchar not null default '',
    lastname              varchar not null default '',
    email                 varchar not null default '',
    address               varchar not null default '',
    postal_code           varchar not null default '',
    city                  varchar not null default '',
    private_telephone_no  varchar not null default '',
    business_telephone_no varchar not null default '',
    mobile_telephone_no   varchar not null default '',
    medical_tolerance     varchar not null default '',
    additional_info       varchar not null default '',
    on_firstname_basis    boolean not null default FALSE,
    hay_fever             boolean not null default FALSE,
    plaster_allergy       boolean not null default FALSE,
    cyanoacrylate_allergy boolean not null default FALSE,
    asthma                boolean not null default FALSE,
    dry_eyes              boolean not null default FALSE,
    circulation_problems  boolean not null default FALSE,
    epilepsy              boolean not null default FALSE,
    diabetes              boolean not null default FALSE,
    claustrophobia        boolean not null default FALSE,
    neurodermatitis       boolean not null default FALSE,
    contacts              boolean not null default FALSE,
    glasses               boolean not null default FALSE,
    covid19vaccinated     boolean not null default FALSE,
    covid19boostered      boolean not null default FALSE,
    eye_disease           varchar not null default '',
    allergy               varchar not null default '',
    birthday              date,
    version               int8    not null default 1 CHECK (version >= 0)
);

create table koku.customer_appointment
(
    id                             SERIAL PRIMARY KEY,
    recorded                       timestamp,
    updated                        timestamp,
    deleted                        boolean not null default FALSE,
    additional_info                varchar not null default '',
    description                    varchar not null default '',
    start                          timestamp,
    customer_id                    int8    not null,
    user_id                        varchar not null,
    activities_revenue_snapshot    numeric(19, 2),
    sold_products_revenue_snapshot numeric(19, 2),
    version                        int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_customer
        FOREIGN KEY (customer_id)
            REFERENCES koku.customer (id)

);

create table koku.customer_appointment_promotion
(
    id             SERIAL PRIMARY KEY,
    recorded       timestamp,
    updated        timestamp,
    appointment_id int8    not null,
    promotion_id   int8    not null,
    position       integer not null default 0 CHECK (position >= 0),
    version        int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES koku.customer_appointment (id)

);

create table koku.customer_appointment_activity
(
    id                   SERIAL PRIMARY KEY,
    recorded             timestamp,
    updated              timestamp,
    appointment_id       int8    not null,
    activity_id          int8    not null,
    sell_price           numeric(19, 2),
    final_price_snapshot numeric(19, 2),
    position             integer not null default 0 CHECK (position >= 0),
    version              int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES koku.customer_appointment (id)

);

create table koku.customer_appointment_sold_product
(
    id                   SERIAL PRIMARY KEY,
    recorded             timestamp,
    updated              timestamp,
    appointment_id       int8    not null,
    product_id           int8    not null,
    sell_price           numeric(19, 2),
    final_price_snapshot numeric(19, 2),
    position             integer not null default 0 CHECK (position >= 0),
    version              int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES koku.customer_appointment (id)

);

create table koku.customer_appointment_activity_sequence
(
    id               SERIAL PRIMARY KEY,
    recorded         timestamp,
    updated          timestamp,
    appointment_id   int8    not null,
    activity_step_id int8,
    product_id       int8,
    position         integer not null default 0 CHECK (position >= 0),
    version          int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES koku.customer_appointment (id)

);