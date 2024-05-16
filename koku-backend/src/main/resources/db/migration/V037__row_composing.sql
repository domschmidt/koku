create table koku.document_row_composing
(
    id                     int8    not null,
    recorded               timestamp,
    updated                timestamp,
    document_id            int8    not null,
    row_id                 int8    not null,
    position_index         int4,
    primary key (id)
);

alter table koku.document_row_composing
    add constraint fk_document_row_composing_document foreign key (document_id) references koku.document;
alter table koku.document_row_composing
    add constraint fk_document_row_composing_row foreign key (row_id) references koku.document_row;

insert into koku.document_row_composing(id, recorded, updated, document_id, row_id, position_index)
select (nextval ('koku.koku_seq')), recorded, updated, document_id, id, position_index
from koku.document_row;

alter table koku.document_row drop column position_index;
alter table koku.document_row drop column document_id;

create table koku.field_definition_activity_pricelist
(
    id int8 not null,
    primary key (id)
);

create table koku.field_definition_activity_pricelist_item_row_composing
(
    id                     int8    not null,
    recorded               timestamp,
    updated                timestamp,
    field_definition_id    int8    not null,
    row_id                 int8    not null,
    position_index         int4,
    primary key (id)
);

alter table koku.field_definition_activity_pricelist_item_row_composing
    add constraint fk_field_definition_activity_pricelist_item_row_composing_document foreign key (field_definition_id) references koku.field_definition_activity_pricelist;
alter table koku.field_definition_activity_pricelist_item_row_composing
    add constraint fk_field_definition_activity_pricelist_item_row_composing_row foreign key (row_id) references koku.document_row;

create table koku.field_definition_activity_pricelist_group_row_composing
(
    id                     int8    not null,
    recorded               timestamp,
    updated                timestamp,
    field_definition_id    int8    not null,
    row_id                 int8    not null,
    position_index         int4,
    primary key (id)
);

alter table koku.field_definition_activity_pricelist_group_row_composing
    add constraint fk_field_definition_activity_pricelist_group_row_composing_document foreign key (field_definition_id) references koku.field_definition_activity_pricelist;
alter table koku.field_definition_activity_pricelist_group_row_composing
    add constraint fk_field_definition_activity_pricelist_group_row_composing_row foreign key (row_id) references koku.document_row;
