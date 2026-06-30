package com.dvein.banking_backend.common.constant;

public class AppConstants {

    // API Versions
    public static final String API_VERSION_V1 = "/api/v1";

    // Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_DEVICE_ID = "X-Device-ID";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_IP_ADDRESS = "X-Forwarded-For";

    // Token Types
    public static final String TOKEN_TYPE_BEARER = "Bearer ";

    // Roles
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMEZONE_UTC = "UTC";

    // Validation Messages
    public static final String VALIDATION_EMAIL_INVALID = "Invalid email format";
    public static final String VALIDATION_PHONE_INVALID = "Invalid phone number format";
    public static final String VALIDATION_PASSWORD_WEAK = "Password does not meet security requirements";

    // Account Prefix
    public static final String ACCOUNT_NUMBER_PREFIX = "ACC";
    public static final String CARD_NUMBER_PREFIX = "4532"; // Visa prefix

    // Default Values
    public static final String DEFAULT_BRANCH_CODE = "001234";
    public static final String DEFAULT_IFSC_CODE = "BANK0001234";
    public static final String DEFAULT_BANK_NAME = "DVein Bank";
}