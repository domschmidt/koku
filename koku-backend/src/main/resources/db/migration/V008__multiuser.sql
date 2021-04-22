alter table koku.customer_appointment add column user_id bigint;

alter table koku.customer_appointment
    add constraint fkf4sfyblkrjw7t09cx2grxccnr foreign key (user_id) references koku.user;