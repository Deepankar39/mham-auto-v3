package com.mhamauto.servlet;

import com.mhamauto.admin.*;
import com.mhamauto.admin.AdminDao.SaveProduct;
import com.mhamauto.auth.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/** Admin routes. ProtectedPagesFilter rechecks the ADMIN role from DB on every request. */
@WebServlet(name="AdminServlet",urlPatterns={"/admin","/admin/products","/admin/product","/admin/categories","/admin/orders","/admin/order","/admin/customers","/admin/product/archive"})
public final class AdminServlet extends HttpServlet {
    private final AdminDao dao=new AdminDao();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {
        noStore(res);
        AuthPrincipal actor=SessionAuth.principal(req);
        if(actor==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!actor.isAdmin()){res.sendError(403,"Admin access required");return;}
        req.setAttribute("csrfToken",Csrf.issue(req));
        req.setAttribute("adminPage",req.getServletPath());
        req.setAttribute("saved","1".equals(req.getParameter("saved")));
        try {
            String route=req.getServletPath();
            switch(route){
                case "/admin" -> {
                    req.setAttribute("summary",dao.dashboard());
                    req.setAttribute("recentOrders",dao.orders(1,null).items().stream().limit(5).toList());
                    req.setAttribute("customerCount",dao.customerCount());
                    render(req,res,"admin-dashboard.jsp");
                }
                case "/admin/products" -> {
                    req.setAttribute("results",dao.products(AdminRules.page(req.getParameter("page")),req.getParameter("q"),req.getParameter("status")));
                    req.setAttribute("search",req.getParameter("q")==null?"":req.getParameter("q"));
                    req.setAttribute("statusFilter",req.getParameter("status")==null?"":req.getParameter("status"));
                    render(req,res,"admin-products.jsp");
                }
                case "/admin/product" -> {
                    req.setAttribute("categories",dao.categories());
                    Long id=AdminRules.optionalId(req.getParameter("id"));
                    if(id!=null){var product=dao.product(id);if(product==null){res.sendError(404);return;}req.setAttribute("product",product);}
                    render(req,res,"admin-product-form.jsp");
                }
                case "/admin/customers" -> {
                    req.setAttribute("search",req.getParameter("q")==null?"":req.getParameter("q"));
                    req.setAttribute("results",dao.customers(AdminRules.page(req.getParameter("page")),req.getParameter("q")));
                    render(req,res,"admin-customers.jsp");
                }
                case "/admin/categories" -> {
                    req.setAttribute("categories",dao.categories());render(req,res,"admin-categories.jsp");
                }
                case "/admin/orders" -> {
                    req.setAttribute("statusFilter",req.getParameter("status")==null?"":req.getParameter("status"));
                    req.setAttribute("results",dao.orders(AdminRules.page(req.getParameter("page")),req.getParameter("status")));
                    render(req,res,"admin-orders.jsp");
                }
                case "/admin/order" -> {
                    var order=dao.order(AdminRules.id(req.getParameter("id")));
                    if(order==null){res.sendError(404);return;}
                    req.setAttribute("orderDetail",order);render(req,res,"admin-order.jsp");
                }
                default -> res.sendError(404);
            }
        }catch(IllegalArgumentException e){res.sendError(400,"Invalid admin request");}
        catch(SQLException|IllegalStateException e){getServletContext().log("Phase 8 admin read failed",e);res.sendError(503,"Admin service temporarily unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {
        noStore(res);
        AuthPrincipal actor=SessionAuth.principal(req);
        if(actor==null){res.sendError(401);return;}
        if(!actor.isAdmin()){res.sendError(403);return;}
        if(!Csrf.valid(req)){res.sendError(403,"Invalid CSRF token");return;}
        try {
            switch(req.getServletPath()){
                case "/admin/product" -> {
                    Long productId=AdminRules.optionalId(req.getParameter("id"));
                    String handle=productId==null?AdminRules.handle(req.getParameter("handle")):"unchanged";
                    String title=AdminRules.required(req.getParameter("title"),"Product name",512);
                    String vendor=AdminRules.required(req.getParameter("vendor"),"Vendor",190);
                    Long cat=AdminRules.optionalId(req.getParameter("categoryId"));
                    String status=AdminRules.status(req.getParameter("status"));
                    boolean published=AdminRules.published(req.getParameter("published"));
                    String sku=AdminRules.sku(req.getParameter("sku"));
                    Integer stock=AdminRules.stock(req.getParameter("stock"));
                    var demoPrice=AdminRules.demoPrice(req.getParameter("demoPrice"));
                    if(demoPrice!=null && !"yes".equals(req.getParameter("confirmFictional")))throw new IllegalArgumentException("Confirm price is fictional Academic V1 demo data");
                    String image=AdminRules.optional(req.getParameter("imageUrl"),2048);
                    if(productId!=null && image!=null)throw new IllegalArgumentException("Editing existing image galleries is reserved for a later phase");
                    Long saved=dao.saveProduct(actor.id(),new SaveProduct(productId,handle,title,req.getParameter("description"),vendor,cat,status,published,sku,stock,demoPrice,image));
                    redirect(req,res,"/admin/product?id="+saved+"&saved=1");
                }
                case "/admin/product/archive" -> {
                    dao.archiveProduct(actor.id(),AdminRules.id(req.getParameter("id")));
                    redirect(req,res,"/admin/products?saved=1");
                }
                case "/admin/categories" -> {
                    if("rename".equals(req.getParameter("action"))||"delete".equals(req.getParameter("action"))){
                        dao.changeCategory(actor.id(),AdminRules.id(req.getParameter("id")),req.getParameter("action"),req.getParameter("name"));
                    }else if("create".equals(req.getParameter("action"))){dao.createCategory(actor.id(),req.getParameter("name"));}
                    else throw new IllegalArgumentException("Invalid category action");
                    redirect(req,res,"/admin/categories?saved=1");
                }
                case "/admin/order" -> {
                    long id=AdminRules.id(req.getParameter("id"));
                    dao.changeOrderStatus(actor.id(),id,req.getParameter("status"));
                    redirect(req,res,"/admin/order?id="+id+"&saved=1");
                }
                default -> res.sendError(405);
            }
        }catch(IllegalArgumentException e){
            // Do not reflect arbitrary input into HTML or reveal internal data.
            res.sendError(400,e.getMessage());
        }catch(SQLException e){
            if("23000".equals(e.getSQLState()))res.sendError(409,"Duplicate SKU, handle or category");
            else {getServletContext().log("Phase 8 admin write failed",e);res.sendError(503,"Admin write temporarily unavailable");}
        }catch(Exception e){getServletContext().log("Phase 8 unexpected admin write failure",e);res.sendError(503,"Admin write temporarily unavailable");}
    }
    private static void noStore(HttpServletResponse r){r.setHeader("Cache-Control","no-store, no-cache, must-revalidate");r.setHeader("Pragma","no-cache");}
    private static void render(HttpServletRequest req,HttpServletResponse res,String jsp)throws ServletException,IOException{
        req.getRequestDispatcher("/WEB-INF/views/"+jsp).forward(req,res);
    }
    private static void redirect(HttpServletRequest req,HttpServletResponse res,String path){
        res.setStatus(303);res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+path));
    }
}
