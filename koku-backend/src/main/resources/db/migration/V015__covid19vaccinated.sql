alter table koku.customer add column covid19vaccinated boolean;
update koku.customer set covid19vaccinated = false;
alter table koku.customer alter column covid19vaccinated SET NOT NULL;