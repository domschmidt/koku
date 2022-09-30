ALTER TABLE koku.file
    ADD column dynamic_document_id int8;

ALTER TABLE koku.file
    ADD CONSTRAINT fk_dynamic_document foreign key (dynamic_document_id) references koku.document;
