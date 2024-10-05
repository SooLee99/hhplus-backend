CREATE TABLE lecture (
                         lecture_id BIGINT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         instructor VARCHAR(255) NOT NULL,
                         description TEXT,
                         max_capacity INT DEFAULT 30,
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP
);

CREATE TABLE lecture_slot (
                              slot_id BIGINT PRIMARY KEY,
                              lecture_id BIGINT,
                              date DATE NOT NULL,
                              capacity INT DEFAULT 30,
                              created_at TIMESTAMP,
                              updated_at TIMESTAMP,
                              FOREIGN KEY (lecture_id) REFERENCES lecture(lecture_id)
);

CREATE TABLE lecture_slot_status (
                                     slot_id BIGINT PRIMARY KEY,
                                     status VARCHAR(20),
                                     current_applicants INT DEFAULT 0,
                                     waiting_list INT DEFAULT 0,
                                     FOREIGN KEY (slot_id) REFERENCES lecture_slot(slot_id)
);

CREATE TABLE application (
                             application_id BIGINT PRIMARY KEY,
                             slot_id BIGINT,
                             user_id BIGINT,
                             current_status VARCHAR(20),
                             created_at TIMESTAMP,
                             FOREIGN KEY (slot_id) REFERENCES lecture_slot(slot_id)
);
