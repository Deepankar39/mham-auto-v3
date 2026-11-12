import com.mhamauto.trade.TradeRules;
public final class Phase9OfflineCheck {
  static void rejects(Runnable action) {
    try{action.run();}catch(IllegalArgumentException expected){return;}
    throw new AssertionError("Expected validation failure");
  }
  public static void main(String[] args) {
    var form=TradeRules.application(" Kuwait Garage "," M. Ahmed ","+965 51234567","GARAGE","HAWALLI","Salmiya","demo only");
    if(!"Kuwait Garage".equals(form.companyName()))throw new AssertionError("trim");
    if(!"+96551234567".equals(form.phone()))throw new AssertionError("phone");
    if(!"PENDING".equals(TradeRules.listState("PENDING")))throw new AssertionError("filter");
    if(!"ALL".equals(TradeRules.listState(null)))throw new AssertionError("all");
    if(TradeRules.page(null)!=1||TradeRules.page("2")!=2)throw new AssertionError("page");
    if(TradeRules.id("15")!=15L)throw new AssertionError("id");
    if(!"APPROVED".equals(TradeRules.reviewState("APPROVED")))throw new AssertionError("review");
    rejects(()->TradeRules.reviewState("PENDING"));
    rejects(()->TradeRules.reviewState("ADMIN"));
    rejects(()->TradeRules.application("Business","Person","1234","GARAGE","CAPITAL","Area",""));
    rejects(()->TradeRules.application("Business","Person","51234567","TRADE_PRICE","CAPITAL","Area",""));
    rejects(()->TradeRules.application("<script>","Person","51234567","GARAGE","CAPITAL","Area",""));
    rejects(()->TradeRules.application("Business","Person","51234567","GARAGE","INVALID","Area",""));
    rejects(()->TradeRules.listState("APPROVED' OR 1=1"));
    rejects(()->TradeRules.page("0"));
    rejects(()->TradeRules.page("100001"));
    rejects(()->TradeRules.id("-1"));
    rejects(()->TradeRules.field("x".repeat(501),"Note",0,500,true));
    System.out.println("PASS Phase 9 pure Java validation checks");
  }
}
