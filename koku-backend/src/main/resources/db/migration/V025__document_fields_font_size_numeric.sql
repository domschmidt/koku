
ALTER TABLE koku.field_definition_date ADD COLUMN font_size_temp INTEGER;
ALTER TABLE koku.field_definition_text ADD COLUMN font_size_temp INTEGER;
ALTER TABLE koku.field_definition_checkbox ADD COLUMN font_size_temp INTEGER;

UPDATE koku.field_definition_date
SET font_size_temp=(
    CASE WHEN font_size = 'MEDIUM' then 12
         WHEN font_size = 'LARGE' then 20
         WHEN font_size = 'SMALL' then 8
    end
    );
UPDATE koku.field_definition_text
SET font_size_temp=(
    CASE WHEN font_size = 'MEDIUM' then 12
         WHEN font_size = 'LARGE' then 20
         WHEN font_size = 'SMALL' then 8
    end
    );
UPDATE koku.field_definition_checkbox
SET font_size_temp=(
    CASE WHEN font_size = 'MEDIUM' then 12
         WHEN font_size = 'LARGE' then 20
         WHEN font_size = 'SMALL' then 8
    end
    );

ALTER TABLE koku.field_definition_date DROP COLUMN font_size;
ALTER TABLE koku.field_definition_text DROP COLUMN font_size;
ALTER TABLE koku.field_definition_checkbox DROP COLUMN font_size;

ALTER TABLE field_definition_date
    RENAME COLUMN font_size_temp TO font_size;

ALTER TABLE field_definition_text
    RENAME COLUMN font_size_temp TO font_size;

ALTER TABLE field_definition_checkbox
    RENAME COLUMN font_size_temp TO font_size;
