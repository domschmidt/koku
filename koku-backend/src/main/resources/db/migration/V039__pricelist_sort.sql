create table koku.field_definition_activity_pricelist_sort
(
    id                  int8 not null,
    sort_by_id          int8 not null,
    recorded            timestamp,
    updated             timestamp,
    position_index      int4,
    field_definition_id int8 not null,
    primary key (id)
);

alter table koku.field_definition_activity_pricelist_sort
    add constraint fk_field_definition_activity_pricelist_sort_field_definition foreign key (field_definition_id) references koku.field_definition_activity_pricelist;
