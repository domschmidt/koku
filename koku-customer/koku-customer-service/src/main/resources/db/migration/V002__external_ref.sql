alter table koku.customer ADD COLUMN external_ref varchar UNIQUE;
alter table koku.customer_appointment ADD COLUMN external_ref varchar UNIQUE;
alter table koku.customer_appointment_promotion ADD COLUMN external_ref varchar UNIQUE;
alter table koku.customer_appointment_activity ADD COLUMN external_ref varchar UNIQUE;
alter table koku.customer_appointment_sold_product ADD COLUMN external_ref varchar UNIQUE;
alter table koku.customer_appointment_activity_sequence ADD COLUMN external_ref varchar UNIQUE;