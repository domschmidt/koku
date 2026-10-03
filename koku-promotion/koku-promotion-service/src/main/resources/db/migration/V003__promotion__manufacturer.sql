create table koku.promotion_manufacturer
(
    promotion_id    int8 not null,
    manufacturer_id int8 not null,

    CONSTRAINT fk_promotion
        FOREIGN KEY (promotion_id)
            REFERENCES koku.promotion (id),

    CONSTRAINT uk_promotion_manufacturer
        UNIQUE (promotion_id, manufacturer_id)
);
