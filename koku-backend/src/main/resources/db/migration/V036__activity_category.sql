create table koku.activity_category
(
    id                     int8    not null,
    recorded               timestamp,
    updated                timestamp,
    description            varchar(255),
    primary key (id)
);

ALTER TABLE koku.activity
    ADD COLUMN category_id int8;
ALTER TABLE koku.activity
    ADD COLUMN relevant_for_price_list boolean;

update koku.activity set relevant_for_price_list = false;

alter table koku.activity
    add constraint fk_activity_category foreign key (category_id) references koku.activity_category on delete set null;
