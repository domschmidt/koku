create table koku.file_tag
(
    id          int8    not null,
    name        varchar,
    value       varchar,
    position    integer not null,
    file_upload_uuid uuid    not null,

    primary key (id),
    constraint fk_file_upload foreign key (file_upload_uuid) references koku.file
);
