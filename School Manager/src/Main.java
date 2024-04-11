import java.sql.*;

public class Main {
    public static void main (String[] args)
    {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/school_manager?useSSL=false","root","password");
            Statement statement = connection.createStatement();

            statement.execute("CREATE TABLE IF NOT EXISTS teacher(teacher_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name TEXT, last_name TEXT);");
            statement.execute("CREATE TABLE IF NOT EXISTS course(course_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, title TEXT NOT NULL, course_type INTEGER NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS section(section_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                    "course_id INTEGER NOT NULL, teacher_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.execute("CREATE TABLE IF NOT EXISTS student(student_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name TEXT, last_name TEXT);");

            statement.execute("CREATE TABLE IF NOT EXISTS enrollment(section_id INTEGER NOT NULL, student_id INTEGER NOT NULL, PRIMARY KEY(section_id, student_id), " +
                    "FOREIGN KEY(section_id) REFERENCES section(section_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY(student_id) REFERENCES student(student_id) ON DELETE CASCADE ON UPDATE CASCADE);");
            //statement.executeUpdate("INSERT INTO teacher(id, first_name, last_name) VALUES (-1, 'No Teacher Assigned', 'No Teacher Assigned');");
            /*statement.execute("DESCRIBE student;");
            for (int i = 0; i < 25; i++) {
                String fn = "'Varain" + i + "' ";
                statement.executeUpdate("INSERT INTO student(first_name, last_name) VALUES (" + fn + ", 'Gandikota');");
            }

            statement.executeUpdate("INSERT INTO course(title, course_type) VALUES ('Unity Game Development', 1);");
            statement.executeUpdate("INSERT INTO section(course_id, teacher_id) VALUES (1, 1)");
            statement.executeUpdate("INSERT INTO section(course_id, teacher_id) VALUES (1, 1)");
            statement.executeUpdate("INSERT INTO enrollment(section_id, student_id) VALUES (1, 1)");
            statement.executeUpdate("INSERT INTO enrollment(section_id, student_id) VALUES (1, 3)");*/
            SchoolManagerFrame schoolFrame = new SchoolManagerFrame(connection);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
