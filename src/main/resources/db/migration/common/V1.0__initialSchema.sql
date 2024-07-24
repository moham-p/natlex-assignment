create table geological_class (
    id number not null,
    code varchar(255),
    name varchar(255),
    section_id number,

    primary key (id)
);

create sequence geological_class_seq increment by 1 start with 1 nocycle;

create table job (
    id varchar(255) not null,
    created_at TIMESTAMP,
    file_path varchar(255),
    job_state varchar(255),
    job_type varchar(255),
    updated_at TIMESTAMP,
    primary key (id)
);

create table section (
    id number not null,
    job_id varchar(255),
    name varchar(255),

    primary key (id),

    foreign key (job_id)
        references job(id)
            on delete set null
);

create sequence section_seq increment by 1 start with 1 nocycle;

alter table if exists geological_class
    add constraint fk_geological_class_section
        foreign key (section_id)
            references section(id) on delete set null;
