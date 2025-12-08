create table koku.activity
(
    id                      SERIAL PRIMARY KEY,
    recorded                timestamp,
    updated                 timestamp,
    deleted                 boolean not null default FALSE,
    approximately_duration  int8    not null default 0 CHECK (approximately_duration >= 0),
    name                    varchar not null default '',
    version                 int8    not null default 1 CHECK (version >= 0)

);
create table koku.activity_price_history
(
    id          SERIAL PRIMARY KEY,
    recorded    timestamp,
    updated     timestamp,
    price       numeric(19, 2) not null CHECK (price >= 0),
    version     int8           not null default 1 CHECK (version >= 0),
    activity_id int8,
    CONSTRAINT fk_activity
        FOREIGN KEY (activity_id)
            REFERENCES koku.activity (id)
);
create table koku.activity_step
(
    id       SERIAL PRIMARY KEY,
    recorded timestamp,
    updated  timestamp,
    deleted  boolean not null default FALSE,
    name     varchar not null default '',
    version  int8    not null default 1 CHECK (version >= 0)
);