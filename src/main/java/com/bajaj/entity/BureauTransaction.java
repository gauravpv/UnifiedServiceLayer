package com.bajaj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bureau_bre_details")
@Getter
@Setter
public class BureauTransaction extends BaseTransaction {

    @Column(name = "HASH_JSON", columnDefinition = "bytea")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] hashJson;
}
