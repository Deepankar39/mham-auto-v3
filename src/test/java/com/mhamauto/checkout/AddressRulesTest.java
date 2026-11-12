package com.mhamauto.checkout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressRulesTest {
    @Test void normalizesKuwaitNumber(){assertEquals("+96551234567",AddressRules.phone("+965 51234567"));}
    @Test void validatesGovernorate(){assertThrows(IllegalArgumentException.class,()->AddressRules.parse("Test","51234567","UNKNOWN","Salmiya","5","Road","10","",""));}
    @Test void rejectsUnsafeText(){assertThrows(IllegalArgumentException.class,()->AddressRules.field("<script>","name",2,20,false));}
    @Test void acceptsOptionalDetails(){assertEquals("",AddressRules.field(null,"directions",0,250,true));}
}
