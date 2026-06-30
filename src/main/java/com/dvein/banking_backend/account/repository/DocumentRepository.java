package com.dvein.banking_backend.account.repository;

import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Document;
import com.dvein.banking_backend.common.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCustomer(Customer customer);

    Optional<Document> findByCustomerAndDocumentType(Customer customer, DocumentType documentType);

    List<Document> findByDocumentType(DocumentType documentType);

    boolean existsByCustomerAndDocumentNumber(Customer customer, String documentNumber);
}