create sequence koku.koku_seq start 1 increment 50;
create table koku.activity
(
    id                     int8    not null,
    recorded               timestamp,
    updated                timestamp,
    approximately_duration int8,
    deleted                boolean not null,
    description            varchar(255),
    primary key (id)
);
create table koku.activity_price_history
(
    id          int8 not null,
    recorded    timestamp,
    updated     timestamp,
    price       numeric(19, 2),
    activity_id int8,
    primary key (id)
);
create table koku.activity_sequence_item
(
    id                        int8 not null,
    recorded                  timestamp,
    updated                   timestamp,
    appointment_id            int8,
    optional_activity_step_id int8,
    optional_product_id       int8,
    position                  int4,
    primary key (id)
);
create table koku.activity_step
(
    id          int8    not null,
    recorded    timestamp,
    updated     timestamp,
    deleted     boolean not null,
    description varchar(255),
    primary key (id)
);
create table koku.appointment
(
    id              int8 not null,
    recorded        timestamp,
    updated         timestamp,
    additional_info varchar(255),
    description     varchar(255),
    revenue         numeric(19, 2),
    start           timestamp,
    customer_id     int8,
    primary key (id)
);
create table koku.appointment_activities_composing
(
    appointment_id   int8 not null,
    activity_id      int8 not null,
    activities_order int4 not null,
    primary key (appointment_id, activities_order)
);
create table koku.appointment_sales_composing
(
    appointment_id int8 not null,
    sale_id        int8 not null
);
create table koku.appointment_soldproducts_composing
(
    appointment_id int8 not null,
    product_id     int8 not null
);
create table koku.customer
(
    id                    int8 not null,
    recorded              timestamp,
    updated               timestamp,
    additional_info       varchar(255),
    address               varchar(255),
    business_telephone_no varchar(255),
    city                  varchar(255),
    email                 varchar(255),
    first_name            varchar(255),
    last_name             varchar(255),
    medical_tolerance     varchar(255),
    mobile_telephone_no   varchar(255),
    postal_code           varchar(255),
    private_telephone_no  varchar(255),
    primary key (id)
);
create table koku.customer_document
(
    id          int8 not null,
    recorded    timestamp,
    updated     timestamp,
    upload_id   int8,
    customer_id int8,
    primary key (id)
);
create table koku.document
(
    id          int8 not null,
    recorded    timestamp,
    updated     timestamp,
    description varchar(255),
    primary key (id)
);
create table koku.document_field
(
    id                       int8 not null,
    recorded                 timestamp,
    updated                  timestamp,
    alignment                varchar(255),
    lg                       int4,
    md                       int4,
    position_index           int4,
    read_only                boolean,
    sm                       int4,
    xl                       int4,
    xs                       int4,
    field_definition_type_id int8,
    row_id                   int8,
    primary key (id)
);
create table koku.document_row
(
    id          int8 not null,
    recorded    timestamp,
    updated     timestamp,
    document_id int8,
    primary key (id)
);
create table koku.field_definition_signature
(
    id int8 not null,
    primary key (id)
);
create table koku.field_definition_svg
(
    max_width_in_px           int4,
    svg_content_base64encoded varchar(130000),
    width_percentage          int4,
    id                        int8 not null,
    primary key (id)
);
create table koku.field_definition_text
(
    font_size varchar(255),
    text      varchar(255),
    id        int8 not null,
    primary key (id)
);
create table koku.field_definition_type
(
    type     varchar(31) not null,
    id       int8        not null,
    recorded timestamp,
    updated  timestamp,
    primary key (id)
);
create table koku.file
(
    uuid          uuid    not null,
    creation_date timestamp,
    deleted       boolean not null,
    file_name     varchar(255),
    customer_id   int8,
    primary key (uuid)
);
create table koku.order
(
    id          int8 not null,
    recorded    timestamp,
    updated     timestamp,
    date        date,
    description varchar(255),
    name        varchar(255),
    price       numeric(19, 2),
    customer_id int8,
    primary key (id)
);
create table koku.product
(
    id              int8    not null,
    recorded        timestamp,
    updated         timestamp,
    deleted         boolean not null,
    description     varchar(255),
    manufacturer_id int8,
    primary key (id)
);
create table koku.product_category
(
    id          int8 not null,
    recorded    timestamp,
    updated     timestamp,
    description varchar(255),
    primary key (id)
);
create table koku.product_category_composing
(
    productcategory_id int8 not null,
    product_id         int8 not null
);
create table koku.product_manufacturer
(
    id       int8    not null,
    recorded timestamp,
    updated  timestamp,
    deleted  boolean not null,
    name     varchar(255),
    primary key (id)
);
create table koku.product_price_history
(
    id         int8 not null,
    recorded   timestamp,
    updated    timestamp,
    price      numeric(19, 2),
    product_id int8,
    primary key (id)
);
create table koku.user
(
    id              int8    not null,
    recorded        timestamp,
    updated         timestamp,
    deleted         boolean not null,
    password        varchar(255),
    username        varchar(255),
    user_details_id int8,
    primary key (id)
);
create table koku.user_details
(
    id            int8 not null,
    recorded      timestamp,
    updated       timestamp,
    avatar_base64 text,
    firstname     varchar(255),
    lastname      varchar(255),
    user_id       int8,
    primary key (id)
);
create table koku.user_refresh_token
(
    id       int8 not null,
    recorded timestamp,
    updated  timestamp,
    expires  timestamp,
    token_id varchar(255),
    user_id  int8,
    primary key (id)
);
alter table koku.user
    add constraint UKsb8bbouer5wak8vyiiy4pf2bx unique (username);
alter table koku.activity_price_history
    add constraint FKn0hi10yrpe5ll488ialw80utb foreign key (activity_id) references koku.activity;
alter table koku.activity_sequence_item
    add constraint FK3knwxjwqi1a4vqjn8o80p4tyr foreign key (appointment_id) references koku.appointment;
alter table koku.activity_sequence_item
    add constraint FKo2nlq62gpqtgjv0rxnbelakjq foreign key (optional_activity_step_id) references koku.activity_step;
alter table koku.activity_sequence_item
    add constraint FK2115ynm2hvxtobkf2p1age839 foreign key (optional_product_id) references koku.product;
alter table koku.appointment
    add constraint FKmyowslj1th8d9j6j3wlbwrtoe foreign key (customer_id) references koku.customer;
alter table koku.appointment_activities_composing
    add constraint FK2p9mjqsa8vljn4yfxm1dgsglj foreign key (activity_id) references koku.activity;
alter table koku.appointment_activities_composing
    add constraint FK6tkkt1qiheing1qcqenvedupm foreign key (appointment_id) references koku.appointment;
alter table koku.appointment_sales_composing
    add constraint FKp3nq8dk8e6ciujxaybh765an4 foreign key (sale_id) references koku.order;
alter table koku.appointment_sales_composing
    add constraint FKc2k2ojrgag02w7yo8h3se3iun foreign key (appointment_id) references koku.appointment;
alter table koku.appointment_soldproducts_composing
    add constraint FKjpc1mvldrr1ntv0q0vuje7lwp foreign key (product_id) references koku.product;
alter table koku.appointment_soldproducts_composing
    add constraint FKp1tqmw7p7dl6c6yhmcloowk4h foreign key (appointment_id) references koku.appointment;
alter table koku.customer_document
    add constraint FK5146mcr0wlbs9a4lr0kx7uusr foreign key (customer_id) references koku.customer;
alter table koku.document_field
    add constraint FKch6n022hdi6k7nenc0x002jq2 foreign key (field_definition_type_id) references koku.field_definition_type;
alter table koku.document_field
    add constraint FKbh7qvuad3obcmcrwesx1ei6nh foreign key (row_id) references koku.document_row;
alter table koku.document_row
    add constraint FKoo8e50oblg9a330yjyedlt461 foreign key (document_id) references koku.document;
alter table koku.field_definition_signature
    add constraint FKfdynpdf9r870gu9i0afiwdmk8 foreign key (id) references koku.field_definition_type;
alter table koku.field_definition_svg
    add constraint FKovu9ga1i2ua9pywpwsugupe1r foreign key (id) references koku.field_definition_type;
alter table koku.field_definition_text
    add constraint FKl1618mgkwj3daod3ba8wj1hk3 foreign key (id) references koku.field_definition_type;
alter table koku.file
    add constraint FKng8hdl3fy5ehqyih6r7p4ol0c foreign key (customer_id) references koku.customer;
alter table koku.order
    add constraint FKb8tboo4d95mh8gavvovwbb7vg foreign key (customer_id) references koku.customer;
alter table koku.product
    add constraint FKgj44t6t48u9vxho2yi6ijawjw foreign key (manufacturer_id) references koku.product_manufacturer;
alter table koku.product_category_composing
    add constraint FKdpxbqslami1stdn5fb2640qgy foreign key (product_id) references koku.product;
alter table koku.product_category_composing
    add constraint FK6xn81u3dhnu7u4y5wdclbxcdq foreign key (productcategory_id) references koku.product_category;
alter table koku.product_price_history
    add constraint FKf0bksln3xky2vmhp66s5md5jd foreign key (product_id) references koku.product;
alter table koku.user
    add constraint FK3wsl4duq3n5imh005r68f3uar foreign key (user_details_id) references koku.user_details;
alter table koku.user_details
    add constraint FKloyyp7tdlkchecrf8bjlqsftt foreign key (user_id) references koku.user;
alter table koku.user_refresh_token
    add constraint FKm2whm1xy7jcn6qmqxo2pispv1 foreign key (user_id) references koku.user;
