package com.bajaj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "REQUEST_JSON", columnDefinition = "mediumblob")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] requestJson;

    @Column(name = "REQUEST_HASH", length = 255)
    private String requestHash;

    @Column(name = "RESPONSE_JSON", columnDefinition = "mediumblob")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] responseJson;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "ERROR_CODE", length = 100)
    private String errorCode;

    @Column(name = "ERROR_MESSAGE", length = 255)
    private String errorMessage;

    @Column(name = "REQUEST_TIMESTAMP", columnDefinition = "TIMESTAMP(3)")
    private Instant requestTimestamp;

    @Column(name = "RESPONSE_TIMESTAMP", columnDefinition = "TIMESTAMP(3)")
    private Instant responseTimestamp;

    @Column(name = "BT_ID", length = 10)
    private String btId;
}
