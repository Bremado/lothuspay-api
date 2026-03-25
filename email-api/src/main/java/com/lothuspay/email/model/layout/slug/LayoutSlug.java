package com.lothuspay.email.model.layout.slug;

public enum LayoutSlug {

    WELCOME_EMAIL,
    NEW_LOGIN_ALERT,
    PASSWORD_RESET,
    NEWSLETTER,
    PROMOTIONAL_OFFER,
    ACCOUNT_VERIFICATION,
    TRANSACTIONAL_RECEIPT,
    TRANSACTIONAL_SEND,
    FEEDBACK_REQUEST,
    BILLING_REMINDER,
    ACCOUNT_CLOSURE_CONFIRMATION,
    ACCOUNT_DOCUMENTATION_REQUEST,
    ACCOUNT_DOCUMENTATION_SUCCESS,

    ;

    public static LayoutSlug fromString(String slug) {
        for (LayoutSlug layoutSlug : LayoutSlug.values()) {
            if (layoutSlug.name().equalsIgnoreCase(slug)) {
                return layoutSlug;
            }
        }
        return null;
    }

}
