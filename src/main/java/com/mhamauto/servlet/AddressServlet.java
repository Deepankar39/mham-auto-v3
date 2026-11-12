package com.mhamauto.servlet;

import com.mhamauto.auth.*;
import com.mhamauto.checkout.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/** Owner-scoped Kuwait address book; no public write endpoints. */
@WebServlet(name="AddressServlet",urlPatterns="/addresses")
public final class AddressServlet extends HttpServlet {
    private final AddressService service=new AddressService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        noStore(res); AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        try{
            req.setAttribute("addressList",service.list(user.id()));
            req.setAttribute("editAddress",req.getParameter("edit")==null?null:service.find(user.id(),CheckoutRules.positiveId(req.getParameter("edit"))));
            req.setAttribute("csrfToken",Csrf.issue(req));
            req.setAttribute("governorateOptions",AddressRules.codes());
            req.setAttribute("addressNotice",switch(req.getParameter("notice")==null?"":req.getParameter("notice")){
                case "saved" -> "Address saved."; case "deleted" -> "Address removed."; default -> "";});
            req.getRequestDispatcher("/WEB-INF/views/addresses.jsp").forward(req,res);
        }catch(IllegalArgumentException e){res.sendError(400,e.getMessage());}
        catch(SQLException|IllegalStateException e){getServletContext().log("Address list failure",e);res.sendError(503,"Address service unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {
        noStore(res);AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!Csrf.valid(req)){res.sendError(403,"Expired security token");return;}
        try{
            String action=req.getParameter("action");
            if("delete".equals(action)) service.delete(user.id(),CheckoutRules.positiveId(req.getParameter("addressId")));
            else if("save".equals(action)) {
                String raw=req.getParameter("addressId");Long id=raw==null||raw.isBlank()?null:CheckoutRules.positiveId(raw);
                AddressRules.AddressData address=AddressRules.parse(req.getParameter("recipientName"),req.getParameter("phone"),
                    req.getParameter("governorate"),req.getParameter("area"),req.getParameter("block"),req.getParameter("street"),
                    req.getParameter("building"),req.getParameter("floorDetail"),req.getParameter("directions"));
                service.save(user.id(),id,address);
            } else throw new IllegalArgumentException("Unknown address action");
            res.setStatus(303);
            res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+"/addresses?notice="+("delete".equals(action)?"deleted":"saved")));
        }catch(CheckoutConflict e){res.sendError(409,e.getMessage());}
        catch(IllegalArgumentException e){res.sendError(400,e.getMessage());}
        catch(SQLException|IllegalStateException e){getServletContext().log("Address mutation failure",e);res.sendError(503,"Address service unavailable");}
    }
    private static void noStore(HttpServletResponse res){res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");}
}
