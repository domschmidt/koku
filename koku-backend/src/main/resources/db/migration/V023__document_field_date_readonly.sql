alter table koku.field_definition_date
    add column font_size varchar(255);

alter table koku.field_definition_date
    add column read_only boolean not null;
