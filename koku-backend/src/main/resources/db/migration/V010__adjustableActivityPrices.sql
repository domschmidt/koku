alter table koku.customer_appointment_activities_composing add column id int8;
alter table koku.customer_appointment_activities_composing add column recorded timestamp;
alter table koku.customer_appointment_activities_composing add column updated timestamp;
alter table koku.customer_appointment_activities_composing add column position int4;
alter table koku.customer_appointment_activities_composing add column sell_price numeric(19, 2);

update koku.customer_appointment_activities_composing set id=(nextval('koku.koku_seq')) where id is null;

ALTER TABLE koku.customer_appointment_activities_composing ALTER COLUMN id SET NOT NULL;

ALTER TABLE koku.customer_appointment_activities_composing DROP CONSTRAINT appointment_activities_composing_pkey;

alter table koku.customer_appointment_activities_composing add primary key (id);
