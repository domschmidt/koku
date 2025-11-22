create table koku.file
(
    id        UUID             DEFAULT gen_random_uuid() PRIMARY KEY,
    deleted   boolean not null default FALSE,
    filename  varchar not null default '',
    content   bytea   not null,
    mime_type varchar not null,
    size      int8    not null,

    ref       varchar,
    ref_id    varchar,

    recorded  timestamp,
    updated   timestamp

);