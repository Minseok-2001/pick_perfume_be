CREATE TABLE report
    (
        report_id         BIGINT AUTO_INCREMENT NOT NULL,
        reporter_id       BIGINT       NOT NULL,
        report_type       VARCHAR(255) NOT NULL,
        target_type       VARCHAR(255) NOT NULL,
        target_id         BIGINT       NOT NULL,
        content           TEXT         NULL,
        status            VARCHAR(255) NOT NULL,
        processor_comment VARCHAR(255) NULL,
        processor_id      BIGINT       NULL,
        created_at        datetime     NOT NULL,
        updated_at        datetime     NOT NULL,
        CONSTRAINT pk_report PRIMARY KEY (report_id)
    );