create table koku.user_region
(
    id           serial primary key,
    country_name varchar not null,
    country_iso  char(2) not null,
    state_name   varchar,
    state_iso    varchar(3),

    constraint user_region_unique unique (country_iso, state_iso)
);

create table koku.user
(
    id            varchar primary key,
    recorded      timestamp,
    updated       timestamp,
    deleted       boolean not null default FALSE,
    avatar_base64 text,
    firstname     varchar not null default '',
    lastname      varchar not null default '',
    fullname      varchar not null default '',
    region_id     int8 REFERENCES koku.user_region,

    version       int8    not null default 1 CHECK (version >= 0)
);

create table koku.user_appointment
(
    id              SERIAL PRIMARY KEY,
    recorded        timestamp,
    updated         timestamp,
    deleted         boolean not null default FALSE,
    description     varchar not null default '',
    start_timestamp timestamp,
    end_timestamp   timestamp,
    user_id         varchar    not null,
    version         int8    not null default 1 CHECK (version >= 0),

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES koku.user (id)

);