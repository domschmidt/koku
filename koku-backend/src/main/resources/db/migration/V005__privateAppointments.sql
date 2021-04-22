alter table koku.activity_sequence_item RENAME column appointment_id TO customer_appointment_id;

alter table koku.appointment RENAME TO customer_appointment;

alter table koku.appointment_activities_composing RENAME column appointment_id TO customer_appointment_id;
alter table koku.appointment_activities_composing RENAME TO customer_appointment_activities_composing;

alter table koku.appointment_sales_composing RENAME column appointment_id TO customer_appointment_id;
alter table koku.appointment_sales_composing RENAME TO customer_appointment_sales_composing;

alter table koku.appointment_soldproducts_composing RENAME column appointment_id TO customer_appointment_id;
alter table koku.appointment_soldproducts_composing RENAME TO customer_appointment_soldproducts_composing;

create table koku.private_appointment
(
    id          int8    not null,
    recorded    timestamp,
    updated     timestamp,
    deleted     boolean not null,
    description varchar(255),
    start       timestamp,
    user_id     int8,
    primary key (id)
);

alter table koku.private_appointment
    add constraint FK9p415945m20jyjt34416s9eep foreign key (user_id) references koku.user;