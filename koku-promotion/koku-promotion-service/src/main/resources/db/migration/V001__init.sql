create table koku.promotion
(
    id                             SERIAL PRIMARY KEY,
    recorded                       timestamp,
    updated                        timestamp,
    deleted                        boolean not null default FALSE,
    name                           varchar not null default '',
    activity_absolute_item_savings numeric(19, 2) CHECK (activity_absolute_item_savings IS NULL OR
                                                         activity_absolute_item_savings >= 0),
    activity_absolute_savings      numeric(19, 2) CHECK (activity_absolute_savings IS NULL OR activity_absolute_savings >= 0),
    activity_relative_item_savings numeric(5, 2) CHECK (activity_relative_item_savings IS NULL OR
                                                        activity_relative_item_savings >= 0),
    activity_relative_savings      numeric(5, 2) CHECK (activity_relative_savings IS NULL OR activity_relative_savings >= 0),
    product_absolute_item_savings  numeric(19, 2) CHECK (product_absolute_item_savings IS NULL OR
                                                         product_absolute_item_savings >= 0),
    product_absolute_savings       numeric(19, 2) CHECK (product_absolute_savings IS NULL OR product_absolute_savings >= 0),
    product_relative_item_savings  numeric(5, 2) CHECK (product_relative_item_savings IS NULL OR
                                                        product_relative_item_savings >= 0),
    product_relative_savings       numeric(5, 2) CHECK (product_relative_savings IS NULL OR product_relative_savings >= 0),
    version                        int8    not null default 1 CHECK (version >= 0)
);