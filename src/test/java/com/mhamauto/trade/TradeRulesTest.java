package com.mhamauto.trade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TradeRulesTest {
  @Test void acceptsKuwaitApplications(){
    var form=TradeRules.application("Kuwait Garage","Applicant","96551234567","WORKSHOP","AHMADI","Fahaheel","Sample");
    assertEquals("+96551234567",form.phone());assertEquals("WORKSHOP",form.businessType());
  }
  @Test void rejectsInvalidBusinessTypesAndData(){
    assertThrows(IllegalArgumentException.class,()->TradeRules.application("Shop","A","51234567","WH0LESALE","HAWALLI","Salmiya",""));
    assertThrows(IllegalArgumentException.class,()->TradeRules.application("Shop","Applicant","51234567","GARAGE","HAWALLI","<img>",""));
  }
  @Test void reviewCannotSetPendingOrOtherEntitlements(){
    assertEquals("REJECTED",TradeRules.reviewState("REJECTED"));
    assertThrows(IllegalArgumentException.class,()->TradeRules.reviewState("PENDING"));
    assertThrows(IllegalArgumentException.class,()->TradeRules.reviewState("CUSTOMER"));
  }
  @Test void filteringAndPaginationAreBounded(){
    assertEquals("ALL",TradeRules.listState(null));
    assertEquals(1,TradeRules.page(null));
    assertEquals(15L,TradeRules.id("15"));
    assertThrows(IllegalArgumentException.class,()->TradeRules.page("-1"));
    assertThrows(IllegalArgumentException.class,()->TradeRules.page("100001"));
    assertThrows(IllegalArgumentException.class,()->TradeRules.listState("APPROVED OR TRUE"));
  }
}
