import com.mhamauto.admin.AdminRules;
import java.math.BigDecimal;
public final class Phase8OfflineCheck {
    public static void main(String[] ignored) {
        if(!"wheel-hub-front".equals(AdminRules.handle("WHEEL-HUB-FRONT")))throw new AssertionError("handle");
        if(AdminRules.stock("0")!=0 || AdminRules.stock("")!=null)throw new AssertionError("stock");
        if(!new BigDecimal("10.000").equals(AdminRules.demoPrice("10")))throw new AssertionError("price");
        if(AdminRules.escapedDescription("<script>").contains("<script>"))throw new AssertionError("HTML encoding");
        if(!"DEMO_CANCELLED".equals(AdminRules.nextOrderStatus("DEMO_PLACED","DEMO_CANCELLED")))throw new AssertionError("status");
        boolean blocked=false;
        try{AdminRules.nextOrderStatus("DEMO_CANCELLED","DEMO_REVIEWED");}catch(IllegalArgumentException ok){blocked=true;}
        if(!blocked)throw new AssertionError("transition gate");
        System.out.println("Phase 8 plain Java rules PASS");
    }
}
