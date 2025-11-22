create table koku.document
(
    id       SERIAL PRIMARY KEY,
    deleted  boolean not null,
    recorded timestamp,
    updated  timestamp,
    name     varchar not null default '',
    template jsonb,
    version  int8    not null default 1 CHECK (version >= 0)
);