import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.auth.IdentityRules;
import com.mhamauto.auth.LoginThrottle;
import com.mhamauto.auth.PasswordHasher;
import java.util.Set;

public class Phase4AuthCheck {
  private static void require(boolean ok,String message){if(!ok) throw new AssertionError(message);}
  public static void main(String[] args) {
    require("person@example.com".equals(IdentityRules.email(" Person@Example.COM ")),"email normalization");
    require("Customer One".equals(IdentityRules.displayName(" Customer One ")),"display name");
    try {IdentityRules.password("short");throw new AssertionError("weak password accepted");}
    catch (IllegalArgumentException expected) { }
    String password="academic-V1-test-password-123";
    String encoded=PasswordHasher.hash(password);
    require(encoded.startsWith("pbkdf2-sha256$600000$"),"hash format");
    require(PasswordHasher.verify(password,encoded),"password check");
    require(!PasswordHasher.verify("wrong",encoded),"wrong password check");
    require(!PasswordHasher.verify(password,"plaintext"),"plaintext accepted");
    AuthPrincipal customer=new AuthPrincipal(3,"Sample","sample@example.com",Set.of("CUSTOMER"));
    require(!customer.isAdmin(),"customer has admin permission");
    AuthPrincipal administrator=new AuthPrincipal(1,"Admin","admin@example.com",Set.of("CUSTOMER","ADMIN"));
    require(administrator.isAdmin(),"admin permission check");
    LoginThrottle limiter=new LoginThrottle();
    for(int i=0;i<6;i++) limiter.failed("127.0.0.1|test",1000L);
    require(!limiter.allowed("127.0.0.1|test",1001L),"throttle limit");
    require(limiter.allowed("127.0.0.1|test",1000L+15*60*1000),"throttle expiry");
    System.out.println("PASS: Phase 4 identity normalization, password hashing, roles and throttling");
  }
}
