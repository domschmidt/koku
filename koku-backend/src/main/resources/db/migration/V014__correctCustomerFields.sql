alter table koku.customer add column plaster_allergy boolean;
update koku.customer set plaster_allergy = false;
alter table koku.customer alter column plaster_allergy SET NOT NULL;

alter table koku.customer add column neurodermatitis boolean;
update koku.customer set neurodermatitis = false;
alter table koku.customer alter column neurodermatitis SET NOT NULL;

alter table koku.customer add column eye_disease varchar(255);
alter table koku.customer add column allergy varchar(255);