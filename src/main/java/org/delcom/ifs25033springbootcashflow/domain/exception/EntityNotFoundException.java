package org.delcom.ifs25033springbootcashflow.domain.exception;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(int id) { super("Data dengan ID " + id + " tidak ditemukan"); }
}
