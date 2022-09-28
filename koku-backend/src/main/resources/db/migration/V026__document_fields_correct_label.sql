
ALTER TABLE field_definition_date
    RENAME COLUMN label TO capture_hint;

ALTER TABLE field_definition_signature
    RENAME COLUMN label TO capture_hint;

ALTER TABLE field_definition_text
    RENAME COLUMN label TO capture_hint;

ALTER TABLE field_definition_qrcode
    DROP COLUMN label;
