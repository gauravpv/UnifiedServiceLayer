package com.bajaj.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "dedupe_bre_details")
public class DedupeTransaction extends BaseTransaction {
}
