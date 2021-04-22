alter table koku.customer_appointment_soldproducts_composing add column id int8;
alter table koku.customer_appointment_soldproducts_composing add column recorded timestamp;
alter table koku.customer_appointment_soldproducts_composing add column updated timestamp;
alter table koku.customer_appointment_soldproducts_composing add column position int4;
alter table koku.customer_appointment_soldproducts_composing add column sell_price numeric(19, 2);

update koku.customer_appointment_soldproducts_composing set id=(nextval('koku.koku_seq')) where id is null;

ALTER TABLE koku.customer_appointment_soldproducts_composing ALTER COLUMN id SET NOT NULL;
alter table koku.customer_appointment_soldproducts_composing add primary key (id);

create table koku.promotion
(
    id                             int8    not null,
    recorded                       timestamp,
    updated                        timestamp,
    deleted                        boolean not null,
    end_date                       date,
    name                           varchar(255),
    start_date                     date,
    promotion_activity_settings_id int8,
    promotion_product_settings_id  int8,
    primary key (id)
);
create table koku.promotion_activity_settings
(
    id                    int8 not null,
    recorded              timestamp,
    updated               timestamp,
    absolute_item_savings numeric(19, 2),
    absolute_savings      numeric(19, 2),
    relative_item_savings numeric(19, 2),
    relative_savings      numeric(19, 2),
    promotion_id          int8,
    primary key (id)
);
create table koku.promotion_product_settings
(
    id                    int8 not null,
    recorded              timestamp,
    updated               timestamp,
    absolute_item_savings numeric(19, 2),
    absolute_savings      numeric(19, 2),
    relative_item_savings numeric(19, 2),
    relative_savings      numeric(19, 2),
    promotion_id          int8,
    primary key (id)
);

create table koku.customer_appointment_promotions_composing
(
    customer_appointment_id int8 not null,
    promotion_id            int8 not null,
    promotions_order        int4 not null,
    primary key (customer_appointment_id, promotions_order)
);
alter table koku.customer_appointment_promotions_composing
    add constraint FKfojqbqyud6hiudxkxowl16uqx foreign key (promotion_id) references koku.promotion;
alter table koku.customer_appointment_promotions_composing
    add constraint FKnqu9yscws5yhw77gr9pcjpr2d foreign key (customer_appointment_id) references koku.customer_appointment;

alter table koku.promotion
    add constraint FK72wgdwbw5d16fpu36kcrwwtox foreign key (promotion_activity_settings_id) references koku.promotion_activity_settings;
alter table koku.promotion
    add constraint FKm7fd9yica5l32r7mgko6ucymi foreign key (promotion_product_settings_id) references koku.promotion_product_settings;
alter table koku.promotion_activity_settings
    add constraint FK26p7jlgke07dlwgl4slors1a0 foreign key (promotion_id) references koku.promotion;
alter table koku.promotion_product_settings
    add constraint FK1ph13vhsu5sj1jv1adv2y7vim foreign key (promotion_id) references koku.promotion;