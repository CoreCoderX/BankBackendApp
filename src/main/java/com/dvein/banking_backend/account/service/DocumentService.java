package com.dvein.banking_backend.account.service;

import com.dvein.banking_backend.account.dto.request.UploadDocumentRequest;
import com.dvein.banking_backend.account.dto.response.DocumentResponse;
import com.dvein.banking_backend.account.model.Customer;
import com.dvein.banking_backend.account.model.Document;
import com.dvein.banking_backend.account.repository.CustomerRepository;
import com.dvein.banking_backend.account.repository.DocumentRepository;
import com.dvein.banking_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public DocumentResponse uploadDocument(Long userId, UploadDocumentRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userid", userId));

        // Generate document URL (simulated)
        String documentUrl = "uploads/docs/" + UUID.randomUUID() + ".pdf";

        Document document = Document.builder()
                .customer(customer)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .documentUrl(documentUrl)
                .expiryDate(request.getExpiryDate() != null ?
                        LocalDate.parse(request.getExpiryDate(), DATE_FORMATTER) : null)
                .issueDate(request.getIssueDate() != null ?
                        LocalDate.parse(request.getIssueDate(), DATE_FORMATTER) : null)
                .issuingAuthority(request.getIssuingAuthority())
                .build();

        document = documentRepository.save(document);

        log.info("Document uploaded for customer: {} - Type: {}", userId, request.getDocumentType());

        return mapToDocumentResponse(document);
    }

    public List<DocumentResponse> getCustomerDocuments(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userid", userId));

        List<Document> documents = documentRepository.findByCustomer(customer);

        return documents.stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void verifyDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        document.setVerified(true);
        documentRepository.save(document);

        log.info("Document verified: {}", documentId);
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getId())
                .documentType(document.getDocumentType())
                .documentNumber(document.getDocumentNumber())
                .expiryDate(document.getExpiryDate())
                .issueDate(document.getIssueDate())
                .issuingAuthority(document.getIssuingAuthority())
                .verified(document.isVerified())
                .expired(document.isExpired())
                .createdAt(document.getCreatedAt())
                .build();
    }
}