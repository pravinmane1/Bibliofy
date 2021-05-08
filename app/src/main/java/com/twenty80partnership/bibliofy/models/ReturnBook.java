package com.twenty80partnership.bibliofy.models;

public class ReturnBook {
    String templateId,publication;

    public ReturnBook() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }
}
