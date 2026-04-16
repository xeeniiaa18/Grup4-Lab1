package epaw.lab1;

import epaw.lab1.util.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/hello")
public class HelloWorld extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String editID = request.getParameter("editID");
        out.println("<h1>Hola des del Servlet!</h1>");
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Users List</title></head>");
        
        
        out.println("<body>");
        
        out.println("<h1>Users from Database</h1>");
        out.println("<table border='1'>");
        out.println("<tr><th>ID</th><th>Name</th><th>Description</th><th>Action</th></tr>");

        try (DBManager db = new DBManager()) {
            PreparedStatement stmt = db.prepareStatement("SELECT id, name, description FROM users");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id =rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + name + "</td>");
                out.println("<td>" + description + "</td>");

                out.println("<td>");
                
                //edit
                out.println("<form method='GET' action='/hello' style='display:inline;'>");
                out.println("<input type='hidden' name='editID' value='" + id + "'>");
                out.println("<button type='submit'>✏️</button>");
                out.println("</form>");
                
                //delete
                out.println("<form method='POST' action='/hello' style='display:inline;'>");
                out.println("<input type='hidden' name='action' value='delete'>");
                out.println("<input type='hidden' name='id' value='" + id + "'>");
                out.println("<button type='submit'>🗑️</button>");
                out.println("</form>");
                
                out.println("</td>");
                out.println("</tr>");
            }
        } catch (Exception e) {
            out.println("<tr><td colspan='3'>Error: " + e.getMessage() + "</td></tr>");
            e.printStackTrace();
        }

        out.println("</table>");

        String editName = "";
        String editDescription = "";
        String idValue="";
        boolean isEdit = false;

        if (editID!=null){
            isEdit = true;
            try (DBManager db = new DBManager()) {
                PreparedStatement stmt = db.prepareStatement("SELECT name, description FROM users WHERE id = ?");
                stmt.setInt(1, Integer.parseInt(editID));
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    editName = rs.getString("name");
                    editDescription = rs.getString("description");
                    idValue=editID;
                }
            } catch (Exception e) {
                out.println("<p>Error fetching user for edit: " + e.getMessage() + "</p>");
                e.printStackTrace();
            }
        }

        out.println("<h2>" + (isEdit ? "Edit user" : "Add new user") + "</h2>");
        out.println("<form method='POST' action='/hello'>");

        if(isEdit){
            out.println("<input type='hidden' name='action' value='edit'>");
            out.println("<input type='hidden' name='id' value='" + idValue + "'>");
        }
        
        out.println("Name: <input type='text' name='name' value='" + editName + "' required><br><br>");
        out.println("Description: <input type='text' name='description' value='" + editDescription + "' required><br><br>");
        out.println("<button type='submit'>" + (isEdit ? "Update" : "Add") + "</button>");
        out.println("</form>");

        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

           
            String action = request.getParameter("action");
           
            try(DBManager db = new DBManager()) {

                if("delete".equals(action)){
                    int id = Integer.parseInt(request.getParameter("id"));
                    PreparedStatement deleteStmt = db.prepareStatement("DELETE FROM users WHERE id = ?");
                    deleteStmt.setInt(1, id);
                    deleteStmt.executeUpdate();
                }
                else if("edit".equals(action)){
                    int id = Integer.parseInt(request.getParameter("id"));
                    String name = request.getParameter("name");
                    String description = request.getParameter("description");
                    
                    
                    PreparedStatement updateStmt = db.prepareStatement("UPDATE users SET name = ?, description = ? WHERE id = ?");
                    updateStmt.setString(1, name);
                    updateStmt.setString(2, description);
                    updateStmt.setInt(3, id);
                    
                    updateStmt.executeUpdate();
                }
            
                else{
                    String name = request.getParameter("name");
                    String description = request.getParameter("description");
                    PreparedStatement stmt = db.prepareStatement("INSERT INTO users (name, description) VALUES (?, ?)");
                
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    
                    stmt.executeUpdate();
        
                }
                

            } catch (Exception e) {
                e.printStackTrace();
            }

       // doGet(request, response); with this we get dupicates everytime we refresh
         response.sendRedirect("/hello");
    }
}