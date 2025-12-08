create table koku.file
(
    id          UUID             DEFAULT gen_random_uuid() PRIMARY KEY,
    deleted     boolean not null default FALSE,
    filename    varchar not null default '',
    content     bytea   not null,
    mime_type   varchar not null,
    size        int8    not null,

    customer_id int8,

    recorded    timestamp,
    updated     timestamp,
    version     int8    not null default 1 CHECK (version >= 0)

);