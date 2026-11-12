package com.mhamauto.trade;

import java.util.List;

/** JSP-friendly immutable view models. Applicant-facing view excludes private admin notes. */
public final class TradeModels {
    private TradeModels() { }
    public record Application(long id,long userId,String companyName,String contactName,String phone,String businessType,
                              String governorate,String area,String applicantNote,String status,String createdAt,String updatedAt,
                              String applicantEmail,String reviewedAt,String adminNote) {
        public long getId(){return id;} public long getUserId(){return userId;}
        public String getCompanyName(){return companyName;} public String getContactName(){return contactName;}
        public String getPhone(){return phone;} public String getBusinessType(){return businessType;}
        public String getGovernorate(){return governorate;} public String getArea(){return area;}
        public String getApplicantNote(){return applicantNote;} public String getStatus(){return status;}
        public String getCreatedAt(){return createdAt;} public String getUpdatedAt(){return updatedAt;}
        public String getApplicantEmail(){return applicantEmail;} public String getReviewedAt(){return reviewedAt;}
        public String getAdminNote(){return adminNote;}
        public boolean getPending(){return "PENDING".equals(status);}
    }
    public record Counts(long pending,long approved,long rejected) {
        public long getPending(){return pending;} public long getApproved(){return approved;}
        public long getRejected(){return rejected;}
    }
    public record Page(List<Application> items,long total,int page,int size) {
        public List<Application> getItems(){return items;}public long getTotal(){return total;}
        public int getPage(){return page;}public int getSize(){return size;}
        public boolean getHasPrevious(){return page>1;}
        public boolean getHasNext(){return (long)page*size<total;}
    }
}
