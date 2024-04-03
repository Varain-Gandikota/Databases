import javax.management.ObjectName;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Files;
import java.util.*;
import java.sql.*;
import java.io.*;

public class SchoolManagerFrame extends JFrame{

    private ArrayList<JPanel> allGUIitems;

    private Table teacherTable;
    private Table studentTable;
    private Table studentScheduleTable;
    private Table courseTable;
    private Table sectionTable;
    private Connection connection;
    private Button studentSaveChanges;
    private JPanel teacherPanel;
    private JPanel studentPanel;
    private JPanel coursePanel;
    private JPanel sectionPanel;
    private JPanel helpPanel;
    private TextFieldWithLabel studentFirstName;
    private TextFieldWithLabel studentLastName;
    private TextFieldWithLabel teacherFirstName;
    private TextFieldWithLabel teacherLastName;
    private Button addTeacherButton;
    private Button removeTeacherButton;
    private Button teacherSaveChanges;
    private JScrollPane sectionsTaughtScrollPane;
    private Table sectionsTaughtTable;
    private Button addStudentButton;
    private JScrollPane scheduleScrollPane;
    private JScrollPane teacherScrollPane;
    private JScrollPane studentScrollPane;
    private JScrollPane courseScrollPane;
    private Button removeStudentButton;
    private ButtonGroup courseTypeButtonGroup;
    private RadioButton courseACAbutton;
    private RadioButton courseKAPbutton;
    private RadioButton courseAPbutton;
    private TextFieldWithLabel courseName;
    private Button addCourseButton;
    private Button removeCourseButton;
    private Button saveCourseChangesButton;
    private JScrollPane sectionScrollPane;
    private ArrayList<Integer> coursesAvailableArrayList;
    private ArrayList<Integer> studentsAvailableArrayList;
    private ArrayList<Integer> teachersAvailableArrayList;
    private ComboBox<Object> coursesAvailable;
    private ComboBox<Object> teachersAvailable;
    private ComboBox<Object> studentsAvailable;
    private JScrollPane rosterScrollPane;
    private Table rosterTable;

    public SchoolManagerFrame(Connection connection)
    {
        super("School Manager");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        this.connection = connection;


        JMenuBar menuBar = new JMenuBar();

        menuBar.setBounds(50, 0, 900, 100);

        JMenuItem teacherMenuItem = new JMenuItem("Teachers");
        JMenuItem studentMenuItem = new JMenuItem("Students");
        JMenuItem courseMenuItem = new JMenuItem("Courses");
        JMenuItem sectionMenuItem = new JMenuItem("Sections");




        JMenu viewMenu = new JMenu("View");
        viewMenu.add(teacherMenuItem);
        viewMenu.add(studentMenuItem);
        viewMenu.add(courseMenuItem);
        viewMenu.add(sectionMenuItem);

        teacherMenuItem.addActionListener(e -> {
            enableView(teacherPanel);
            refreshInformation();
            viewMenu.setSelected(false);
        });
        studentMenuItem.addActionListener(e -> {
            enableView(studentPanel);
            refreshInformation();
            viewMenu.setSelected(false);

        });
        courseMenuItem.addActionListener(e -> {
            enableView(coursePanel);
            refreshInformation();
            viewMenu.setSelected(false);

        });
        sectionMenuItem.addActionListener(e -> {
            enableView(sectionPanel);
            refreshInformation();
            viewMenu.setSelected(false);
        });
        JMenu fileMenu = new JMenu("File");

        JMenuItem exportDataItem = new JMenuItem("Export Data");
        JMenuItem importDataItem = new JMenuItem("Import Data");
        JMenuItem purgeItem = new JMenuItem("Purge Data");
        JMenuItem exitItem = new JMenuItem("Exit");

        fileMenu.add(exportDataItem);
        fileMenu.add(importDataItem);
        fileMenu.add(purgeItem);
        fileMenu.add(exitItem);

        exportDataItem.addActionListener(e -> {
            try{
                File exportFile = new File("ExportedSchoolManagerInformation.txt");
                //adds a number to file name until path doesn't exist
                int i = 1;
                while (exportFile.exists())
                {
                    exportFile = new File("ExportedSchoolManagerInformation (" + i + ").txt");
                    i+=1;
                }
                //variable created to quell this annoying yellow highlight
                boolean a = exportFile.createNewFile();

                if (exportFile.exists()){
                    Statement s = connection.createStatement();
                    FileWriter fw = new FileWriter(exportFile, false);

                    ArrayList<ArrayList<Object>> teacherTableData = new ArrayList<>();
                    String sql = "SELECT * FROM teacher WHERE id >= 1;";
                    ResultSet rs = s.executeQuery(sql);
                    while (rs != null && rs.next())
                    {
                        ArrayList<Object> a1 = new ArrayList<>();
                        for (int f = 1; f <= rs.getMetaData().getColumnCount(); f++)
                        {
                            a1.add(rs.getObject(f));
                        }
                        teacherTableData.add(a1);
                    }
                    ArrayList<ArrayList<Object>> studentTableData = getAllDataFromSQLTable("student");
                    ArrayList<ArrayList<Object>> courseTableData = getAllDataFromSQLTable("course");
                    ArrayList<ArrayList<Object>> sectionTableData = getAllDataFromSQLTable("section");
                    ArrayList<ArrayList<Object>> enrollmentTableData = getAllDataFromSQLTable("enrollment");

                    transcribeInformationToFile(exportFile, teacherTableData, fw);
                    transcribeInformationToFile(exportFile, studentTableData, fw);
                    transcribeInformationToFile(exportFile, courseTableData, fw);
                    transcribeInformationToFile(exportFile, sectionTableData, fw);
                    transcribeInformationToFile(exportFile, enrollmentTableData, fw);

                    System.out.println(teacherTableData);
                    fw.close();
                }
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });

        importDataItem.addActionListener(e -> {
            try {
                Statement statement = connection.createStatement();
                statement.execute("DROP TABLE IF EXISTS enrollment;");
                statement.execute("DROP TABLE IF EXISTS section;");
                statement.execute("DROP TABLE IF EXISTS teacher;");
                statement.execute("DROP TABLE IF EXISTS student;");
                statement.execute("DROP TABLE IF EXISTS course;");

                statement.execute("CREATE TABLE IF NOT EXISTS teacher(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name TEXT, last_name TEXT);");
                statement.execute("CREATE TABLE IF NOT EXISTS course(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, title TEXT NOT NULL, course_type INTEGER NOT NULL);");
                statement.execute("CREATE TABLE IF NOT EXISTS section(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        "course_id INTEGER NOT NULL, teacher_id INTEGER NOT NULL, " +
                        "FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE ON UPDATE CASCADE);");
                statement.execute("CREATE TABLE IF NOT EXISTS student(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name TEXT, last_name TEXT);");
                statement.execute("CREATE TABLE IF NOT EXISTS enrollment(section_id INTEGER NOT NULL, student_id INTEGER NOT NULL, PRIMARY KEY(section_id, student_id), " +
                        "FOREIGN KEY(section_id) REFERENCES section(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE ON UPDATE CASCADE);");

                statement.executeUpdate("INSERT INTO teacher(id, first_name, last_name) VALUES (-1, 'No Teacher Assigned', 'No Teacher Assigned');");
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(null);
                if (result != JFileChooser.APPROVE_OPTION)
                    return;

                File chosenFile = fileChooser.getSelectedFile();
                if (chosenFile.exists()){

                    Scanner s = new Scanner(chosenFile);
                    for (int i = 0; i < 5; i++){
                        ArrayList<String[]> tableData = new ArrayList<>();
                        while (s.hasNext()){
                            String row = s.nextLine();
                            if (row.equals("END")){
                                break;
                            }
                            String[] rowData = row.split(",");
                            tableData.add(rowData);
                            System.out.println(Arrays.toString(rowData));
                        }
                        //inserts correct data depending on i
                        switch (i) {
                            case 0 -> {
                                for (String[] rowData : tableData) {
                                    String sql = String.format("INSERT INTO teacher(id, first_name, last_name) VALUES (%s, '%s', '%s');", rowData[0], rowData[1], rowData[2]);
                                    statement.executeUpdate(sql);
                                }
                            }
                            case 1 -> {
                                for (String[] rowData : tableData) {
                                    String sql = String.format("INSERT INTO student(id, first_name, last_name) VALUES (%s, '%s', '%s');", rowData[0], rowData[1], rowData[2]);
                                    statement.executeUpdate(sql);
                                }
                            }
                            case 2 -> {
                                for (String[] rowData : tableData) {
                                    String sql = String.format("INSERT INTO course(id, title, course_type) VALUES (%s, '%s', %s);", rowData[0], rowData[1], rowData[2]);
                                    statement.executeUpdate(sql);
                                }
                            }
                            case 3 -> {
                                for (String[] rowData : tableData) {
                                    String sql = String.format("INSERT INTO section(id, course_id, teacher_id) VALUES (%s, %s, %s);", rowData[0], rowData[1], rowData[2]);
                                    statement.executeUpdate(sql);
                                }
                            }
                            case 4 -> {
                                for (String[] rowData : tableData) {
                                    String sql = String.format("INSERT INTO enrollment(section_id, student_id) VALUES (%s, %s);", rowData[0], rowData[1]);
                                    statement.executeUpdate(sql);
                                }
                            }
                        }
                    }
                }
                refreshInformation();

            }catch (Exception e1){
                e1.printStackTrace();
            }

        });
        exitItem.addActionListener(e -> {
            dispose();
            System.out.println("Closed Program");
            try{
                connection.close();
            }catch (Exception e1){
                e1.printStackTrace();
            }
            System.exit(0);
        });
        purgeItem.addActionListener(e -> {
            try{
                Statement statement = connection.createStatement();
                statement.execute("DROP TABLE IF EXISTS enrollment;");
                statement.execute("DROP TABLE IF EXISTS section;");
                statement.execute("DROP TABLE IF EXISTS teacher;");
                statement.execute("DROP TABLE IF EXISTS student;");
                statement.execute("DROP TABLE IF EXISTS course;");

                statement.execute("CREATE TABLE IF NOT EXISTS teacher(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name TEXT, last_name TEXT);");
                statement.execute("CREATE TABLE IF NOT EXISTS course(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, title TEXT NOT NULL, course_type INTEGER NOT NULL);");
                statement.execute("CREATE TABLE IF NOT EXISTS section(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        "course_id INTEGER NOT NULL, teacher_id INTEGER NOT NULL, " +
                        "FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE ON UPDATE CASCADE);");
                statement.execute("CREATE TABLE IF NOT EXISTS student(id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name TEXT, last_name TEXT);");
                statement.execute("CREATE TABLE IF NOT EXISTS enrollment(section_id INTEGER NOT NULL, student_id INTEGER NOT NULL, PRIMARY KEY(section_id, student_id), " +
                        "FOREIGN KEY(section_id) REFERENCES section(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE ON UPDATE CASCADE);");

                statement.executeUpdate("INSERT INTO teacher(id, first_name, last_name) VALUES (-1, 'No Teacher Assigned', 'No Teacher Assigned');");
            }catch (Exception e1){
                e1.printStackTrace();
            }
            dispose();
            System.out.println("Closed Program");
            try{
                connection.close();
            }catch (Exception e1){
                e1.printStackTrace();
            }
            System.exit(0);
        });
        JMenu helpMenu = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> {
            enableView(helpPanel);
            refreshInformation();
            helpMenu.setSelected(false);
        });
        helpMenu.add(about);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        teacherScrollPane = new JScrollPane(teacherTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        studentScrollPane = new JScrollPane(studentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        courseScrollPane = new JScrollPane(courseTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scheduleScrollPane = new JScrollPane(studentScheduleTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sectionsTaughtScrollPane = new JScrollPane(sectionsTaughtTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sectionScrollPane = new JScrollPane(sectionTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rosterScrollPane = new JScrollPane(rosterTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);



        constructJTables();
        teacherScrollPane.setBounds(10, 10, 500, 550);
        studentScrollPane.setBounds(10, 10, 500, 550);
        courseScrollPane.setBounds(10, 10, 500, 550);
        sectionScrollPane.setBounds(10, 10, 500, 550);
        scheduleScrollPane.setBounds(520, 10, 350, 100);
        sectionsTaughtScrollPane.setBounds(520, 10, 350, 100);
        rosterScrollPane.setBounds(520, 10, 350, 100);


        teacherPanel = new JPanel(null);
        studentPanel = new JPanel(null);
        coursePanel = new JPanel(null);
        sectionPanel = new JPanel(null);
        helpPanel = new JPanel(null);

        teacherPanel.add(teacherScrollPane);
        teacherPanel.add(sectionsTaughtScrollPane);
        teacherPanel.setSize(getWidth(), getHeight());

        studentPanel.add(studentScrollPane);
        studentPanel.add(scheduleScrollPane);
        studentPanel.setSize(getWidth(), getHeight());

        sectionPanel.add(sectionScrollPane);
        sectionPanel.add(rosterScrollPane);
        sectionPanel.setSize(getWidth(), getHeight());

        helpPanel.setSize(getWidth(), getHeight());


        studentSaveChanges = new Button("Save Alterations to Database", 625, 500, 300, 50, studentPanel);
        addStudentButton = new Button("Add Student (Auto Saves)", 625, 300, 300, 50, studentPanel);
        removeStudentButton = new Button("Remove Selected Student (Auto Saves)", 625, 400, 300, 50, studentPanel);

        teacherSaveChanges = new Button("Save Alterations to Database", 625, 500, 300, 50, teacherPanel);
        addTeacherButton = new Button("Add Teacher (Auto Saves)", 625, 300, 300, 50, teacherPanel);
        removeTeacherButton = new Button("Remove Selected Teacher (Auto Saves)", 625, 400, 300, 50, teacherPanel);

        saveCourseChangesButton = new Button("Save Course Alterations to Database", 625, 500, 300, 50, coursePanel);
        addCourseButton = new Button("Add Course (Auto Saves)", 625, 300, 300, 50, coursePanel);
        removeCourseButton = new Button("Remove Selected Course (Auto Saves)", 625, 400, 300, 50, coursePanel);

        Button addStudentToRosterButton = new Button("Add student to roster (Auto Saves)", 690, 300, 170, 25, sectionPanel);
        Button removeStudentFromRosterButton = new Button("Remove selected student from roster (Auto Saves)", 640, 350, 270, 25, sectionPanel);//32
        Button addNewSection = new Button("Add new section (Auto Saves)", 625, 400, 300, 50, sectionPanel);
        Button removeSelectedSection = new Button("Remove selected section (Auto Saves)", 625, 475, 300, 50, sectionPanel);
        Button saveSectionChanges = new Button("Save section table changes", 625, 550, 300, 50, sectionPanel);
        saveSectionChanges.addActionListener(e -> {
            try{

                //since table has string but section takes course_id and teacher_id it causes error
                Statement s = connection.createStatement();
                for (int row = 0; row < sectionTable.getRowCount(); row++)
                {
                    int courseId = ((Course)sectionTable.getValueAt(row, 1)).getId();
                    int teacherId = ((Teacher)sectionTable.getValueAt(row, 2)).getId();
                    System.out.println(courseId + "\t" + teacherId);
                    String sqlCommand = String.format("UPDATE section SET course_id=%s, teacher_id=%s WHERE id=%s;", courseId, teacherId, sectionTable.getValueAt(row, 0));
                    s.executeUpdate(sqlCommand);
                }
                constructJTables();
                updateActiveTeachersAndCourses();
                updateAvailableStudentsToAddToRoster();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        addNewSection.addActionListener(e -> {
            if (teachersAvailable.getItemCount() == 0 || coursesAvailable.getItemCount() == 0 || teachersAvailable.getSelectedItem() == null || coursesAvailable.getSelectedItem() == null)
                return;
            try{
                Statement s = connection.createStatement();
                int teacherId = ((Teacher)teachersAvailable.getSelectedItem()).getId();
                int courseId = ((Course)coursesAvailable.getSelectedItem()).getId();

                String sql = String.format("INSERT INTO section(course_id, teacher_id) VALUES (%d, %d)", courseId, teacherId);
                s.executeUpdate(sql);
                refreshInformation();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        removeSelectedSection.addActionListener(e -> {
            if (sectionTable.getSelectedRow() == -1)
                return;
            try {
                Statement s = connection.createStatement();
                s.executeUpdate("DELETE FROM section WHERE id = " + (Integer)sectionTable.getValueAt(sectionTable.getSelectedRow(), 0) + ";");
                constructJTables();
                sectionTable.clearSelection();
                constructRosterTable(0);
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        removeStudentFromRosterButton.addActionListener(e -> {
            if (rosterTable == null || rosterTable.getSelectedRow() == -1)
                return;
            try{
                Statement s = connection.createStatement();

                int selectedStudentId = (Integer)rosterTable.getValueAt(rosterTable.getSelectedRow(), 2);
                int selectedSectionId = (Integer)sectionTable.getValueAt(sectionTable.getSelectedRow(), 0);
                String sql = String.format("DELETE FROM enrollment WHERE student_id = %d AND section_id = %d", selectedStudentId, selectedSectionId);
                s.executeUpdate(sql);
                constructRosterTable(selectedSectionId);
                updateAvailableStudentsToAddToRoster();
                updateActiveTeachersAndCourses();
            }catch (Exception e1){
                e1.printStackTrace();
            }

        });
        addStudentToRosterButton.addActionListener(e -> {
            try{
                if (studentsAvailable.getSelectedItem() == null || sectionTable.getRowCount() == 0 || sectionTable.getSelectedRow() == -1)
                    return;
                Statement s = connection.createStatement();
                Student selectedStudent = (Student)studentsAvailable.getSelectedItem();
                Integer selectedId = selectedStudent.getId();
                Integer selectedSectionId = (Integer)sectionTable.getValueAt(sectionTable.getSelectedRow(), 0);
                String sql = String.format("INSERT INTO enrollment(section_id, student_id) VALUES (%d, %d);", selectedSectionId, selectedId);
                s.executeUpdate(sql);
                constructRosterTable(selectedSectionId);
                updateAvailableStudentsToAddToRoster();
                updateActiveTeachersAndCourses();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });

        saveCourseChangesButton.addActionListener(e -> {
            try{
                Statement s = connection.createStatement();
                for (int row = 0; row < courseTable.getRowCount(); row++)
                {
                    String sqlCommand = String.format("UPDATE course SET title= '%s', course_type= %s WHERE id = %s",
                            courseTable.getValueAt(row, 1), courseTable.getValueAt(row, 2), courseTable.getValueAt(row, 0));
                    s.executeUpdate(sqlCommand);
                }
                updateActiveTeachersAndCourses();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        addCourseButton.addActionListener(e -> {
            if (courseName.getText().isEmpty())
                return;
            try{
                Statement s = connection.createStatement();
                ButtonModel buttonModel = courseTypeButtonGroup.getSelection();
                int type = 0;
                if (buttonModel == courseKAPbutton.getModel())
                    type = 1;
                else if (buttonModel == courseAPbutton.getModel())
                    type = 2;
                String command = String.format("INSERT INTO course (title, course_type) VALUES ('%s', %d);",
                        courseName.getText(),type);
                s.executeUpdate(command);
                courseName.setText("");
                constructJTables();
                updateActiveTeachersAndCourses();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        removeCourseButton.addActionListener(e -> {
            if (courseTable.getSelectedRow() == -1)
                return;
            try {
                Statement s = connection.createStatement();
                s.executeUpdate("DELETE FROM course WHERE id = " + (Integer)courseTable.getValueAt(courseTable.getSelectedRow(), 0) + ";");
                constructJTables();
                courseTable.clearSelection();
                courseName.setText("");
                updateActiveTeachersAndCourses();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        teacherSaveChanges.addActionListener(e -> {
            try{
                Statement s = connection.createStatement();
                for (int row = 0; row < teacherTable.getRowCount(); row++)
                {
                    String sqlCommand = String.format("UPDATE teacher SET first_name= '%s', last_name = '%s' WHERE id = %s",
                            teacherTable.getValueAt(row, 1), teacherTable.getValueAt(row, 2), teacherTable.getValueAt(row, 0));
                    s.executeUpdate(sqlCommand);
                }
                updateActiveTeachersAndCourses();
                updateAvailableStudentsToAddToRoster();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        addTeacherButton.addActionListener(e -> {
            if (teacherFirstName.getText().isEmpty() || teacherLastName.getText().isEmpty())
                return;
            try{
                Statement s = connection.createStatement();
                String command = String.format("INSERT INTO teacher (first_name, last_name) VALUES ('%s', '%s');",
                        teacherFirstName.getText(),teacherLastName.getText());
                s.executeUpdate(command);
                constructJTables();
                updateActiveTeachersAndCourses();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        removeTeacherButton.addActionListener(e -> {
            if (teacherTable.getSelectedRow() == -1)
                return;
            try {
                Statement s = connection.createStatement();
                int selectedId = (Integer)teacherTable.getValueAt(teacherTable.getSelectedRow(), 0);
                s.executeUpdate(String.format("UPDATE section SET teacher_id = -1 WHERE teacher_id = %d", selectedId));
                s.executeUpdate("DELETE FROM teacher WHERE id = " + selectedId + ";");
                constructJTables();
                teacherTable.clearSelection();
                sectionsTaughtScrollPane.setViewportView(null);
                updateActiveTeachersAndCourses();
                updateAvailableStudentsToAddToRoster();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        studentSaveChanges.addActionListener(e -> {
            try{
                Statement s = connection.createStatement();
                for (int row = 0; row < studentTable.getRowCount(); row++)
                {
                    String sqlCommand = String.format("UPDATE student SET first_name= '%s', last_name = '%s' WHERE id = %s",
                            studentTable.getValueAt(row, 1), studentTable.getValueAt(row, 2), studentTable.getValueAt(row, 0));
                    s.executeUpdate(sqlCommand);
                }
                rosterTable = constructRosterTable(0);
                updateAvailableStudentsToAddToRoster();
                rosterScrollPane.setViewportView(rosterTable);
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        addStudentButton.addActionListener(e -> {
            if (studentFirstName.getText().isEmpty() || studentLastName.getText().isEmpty())
                return;
            try{
                Statement s = connection.createStatement();
                String command = String.format("INSERT INTO student (first_name, last_name) VALUES ('%s', '%s');",
                        studentFirstName.getText(),studentLastName.getText());
                s.executeUpdate(command);
                updateAvailableStudentsToAddToRoster();
                constructJTables();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        removeStudentButton.addActionListener(e -> {
            if (studentTable.getSelectedRow() == -1)
                return;
            try {
                Statement s = connection.createStatement();
                s.executeUpdate("DELETE FROM student WHERE id = " + (Integer)studentTable.getValueAt(studentTable.getSelectedRow(), 0) + ";");
                constructJTables();
                studentTable.clearSelection();
                scheduleScrollPane.setViewportView(null);
                updateAvailableStudentsToAddToRoster();
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        studentFirstName = new TextFieldWithLabel("First Name: ", 625, 150, 300, 50, studentPanel);
        studentLastName = new TextFieldWithLabel("Last Name: ", 625, 225, 300, 50, studentPanel);

        teacherFirstName = new TextFieldWithLabel("First Name: ", 625, 150, 300, 50, teacherPanel);
        teacherLastName = new TextFieldWithLabel("Last Name: ", 625, 225, 300, 50, teacherPanel);

        courseName = new TextFieldWithLabel("Course Name: ", 625, 50, 300, 50, coursePanel);
        courseName.addActionListener(e -> {
            System.out.println("Finished Editing");
        });
        courseTypeButtonGroup = new ButtonGroup();
        courseACAbutton = new RadioButton("Academic (0)",625, 125, 300, 25, courseTypeButtonGroup, coursePanel);
        courseKAPbutton = new RadioButton("KAP (1)",625, 150, 300, 25, courseTypeButtonGroup, coursePanel);
        courseAPbutton = new RadioButton("AP (2)",625, 175, 300, 25, courseTypeButtonGroup, coursePanel);

        teachersAvailableArrayList = new ArrayList<>();
        studentsAvailableArrayList = new ArrayList<>();
        coursesAvailableArrayList = new ArrayList<>();

        JLabel teacherAvailableText = new JLabel("Available Teachers (ID):"); teacherAvailableText.setBounds(525, 150, 250, 25); sectionPanel.add(teacherAvailableText);
        JLabel coursesAvailableText = new JLabel("Available Courses  (ID):"); coursesAvailableText.setBounds(525, 200, 250, 25); sectionPanel.add(coursesAvailableText);
        JLabel studentsAvailableText = new JLabel("Available students to add (ID):"); studentsAvailableText.setBounds(525, 250, 250, 25); sectionPanel.add(studentsAvailableText);

        JLabel versionNumber = new JLabel("Version Number: 1.0.16.2024"); versionNumber.setBounds(10 ,10, 450, 100);
        JLabel applicationCreators = new JLabel("Application Creators: Varain \"the goat\" Gandikota"); applicationCreators.setBounds(10 ,125, 700, 100);
        versionNumber.setFont(new Font("Serif", Font.PLAIN, 30)); applicationCreators.setFont(new Font("Serif", Font.PLAIN, 30));
        helpPanel.add(versionNumber); helpPanel.add(applicationCreators);

        teachersAvailable = new ComboBox<>(700, 150, 150, 25, sectionPanel);
        teachersAvailable.addActionListener(e -> {
            if (sectionTable.getSelectedRow() == -1 || teachersAvailable.getSelectedItem() == null)
                return;
            sectionTable.setValueAt(teachersAvailable.getSelectedItem(), sectionTable.getSelectedRow(), 2);
        });
        coursesAvailable = new ComboBox<>(700, 200, 150, 25, sectionPanel);
        coursesAvailable.addActionListener(e -> {
            if (sectionTable.getSelectedRow() == -1 || coursesAvailable.getSelectedItem() == null)
                return;

            sectionTable.setValueAt(coursesAvailable.getSelectedItem(), sectionTable.getSelectedRow(), 1);
        });
        studentsAvailable = new ComboBox<>(700, 250, 150, 25, sectionPanel);
        updateActiveTeachersAndCourses();
        updateAvailableStudentsToAddToRoster();
        courseACAbutton.addActionListener(e -> {
            if (courseTable.getSelectedRow() == -1)
                return;
            courseTable.setValueAt(0, courseTable.getSelectedRow(), 2);
        });
        courseKAPbutton.addActionListener(e -> {
            if (courseTable.getSelectedRow() == -1)
                return;
            courseTable.setValueAt(1, courseTable.getSelectedRow(), 2);
        });
        courseAPbutton.addActionListener(e -> {
            if (courseTable.getSelectedRow() == -1)
                return;
            courseTable.setValueAt(2, courseTable.getSelectedRow(), 2);
        });
        courseTypeButtonGroup.setSelected(courseACAbutton.getModel(), true);
        coursePanel.add(courseScrollPane);
        coursePanel.setSize(getWidth(), getHeight());

        allGUIitems = new ArrayList<>(); allGUIitems.add(teacherPanel);allGUIitems.add(studentPanel);allGUIitems.add(coursePanel);allGUIitems.add(sectionPanel); allGUIitems.add(helpPanel);
        updateActiveTeachersAndCourses();
        add(teacherPanel);
        add(studentPanel);
        add(coursePanel);
        add(sectionPanel);
        add(helpPanel);
        enableView(teacherPanel);
        setResizable(false);
        setVisible(true);

    }
    public void transcribeInformationToFile(File f, ArrayList<ArrayList<Object>> data, FileWriter fw){
        try {
            String thingToWrite = "";
            for (ArrayList<Object> dataArrayList : data){
                for (Object ob : dataArrayList){
                    thingToWrite += ob + ",";
                }
                thingToWrite = thingToWrite.substring(0, thingToWrite.length()-1);
                thingToWrite+="\n";
            }
            thingToWrite += "END\n";
            fw.write(thingToWrite);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public ArrayList<ArrayList<Object>> getAllDataFromSQLTable(String tableName){
        ArrayList<ArrayList<Object>> tableData = new ArrayList<>();
        try {
            Statement s = connection.createStatement();
            String sql = String.format("SELECT * FROM %s;", tableName);
            ResultSet rs = s.executeQuery(sql);
            while (rs != null && rs.next())
            {
                ArrayList<Object> a = new ArrayList<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                {
                    a.add(rs.getObject(i));
                }
                tableData.add(a);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return tableData;
    }
    public void refreshInformation()
    {
        constructRosterTable(0);
        constructScheduleTable(0);
        constructSectionTaughtTable(0);

        updateActiveTeachersAndCourses();
        updateAvailableStudentsToAddToRoster();
        constructJTables();

    }
    public void updateActiveTeachersAndCourses()
    {
        try{

            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM teacher WHERE id >= 1");
            teachersAvailableArrayList.clear();
            coursesAvailableArrayList.clear();
            ArrayList<Object> teacherObjects = new ArrayList<>();
            ArrayList<Object> courseObjects = new ArrayList<>();


            while (rs != null && rs.next()){
                teachersAvailableArrayList.add(rs.getInt(1));
                teacherObjects.add(new Teacher(rs.getString("first_name"), rs.getString("last_name"), rs.getInt("id")));
            }
            rs = s.executeQuery("SELECT * FROM course WHERE id >= 1");
            while (rs != null && rs.next()){
                coursesAvailableArrayList.add(rs.getInt(1));
                courseObjects.add(new Course(rs.getString("title"), rs.getInt("id"), rs.getInt("course_type")));
            }
            coursesAvailable.setItems(courseObjects);
            teachersAvailable.setItems(teacherObjects);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void updateAvailableStudentsToAddToRoster()
    {
        try {
            studentsAvailableArrayList.clear();
            if (rosterTable == null)
            {
                studentsAvailable.removeAllItems();
                return;
            }
            Statement s = connection.createStatement();
            String notIds = "(";
            for (int row = 0; row < rosterTable.getRowCount(); row++)
            {
                notIds += rosterTable.getValueAt(row, 2) + ",";
            }
            notIds = notIds.substring(0, notIds.length()-1) + ")";
            String sql = String.format("SELECT * FROM student WHERE id NOT IN %s;", notIds);
            if (rosterTable.getRowCount() == 0){
                sql = "SELECT * FROM student WHERE id >= 1;";
            }
            ResultSet rs = s.executeQuery(sql);
            ArrayList<String> studentsNamesAvailableArrayList = new ArrayList<>();
            ArrayList<Object> studentsArrayList = new ArrayList<>();
            while (rs != null && rs.next()){
                studentsArrayList.add(new Student(rs.getString("first_name"),rs.getString("last_name"), rs.getInt("id") ));
                studentsAvailableArrayList.add(rs.getInt("id"));
                studentsNamesAvailableArrayList.add(rs.getString("last_name") + ", " + rs.getString("first_name"));
            }
            studentsAvailable.setItems(studentsArrayList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void constructSectionTable(){
        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM section WHERE id >= 1;");
            while (rs != null && rs.next()){
                ArrayList<Object> a1 = new ArrayList<>();
                a1.add(rs.getObject(1));
                a1.add(rs.getObject(2));
                a1.add(rs.getObject(3));
                data.add(a1);
            }
            for (ArrayList<Object> row : data){
                int course_id = (Integer)row.get(1);
                int teacher_id = (Integer)row.get(2);

                String sql = String.format("SELECT * FROM teacher WHERE id = %d", teacher_id);
                ResultSet rs1 = statement.executeQuery(sql);
                rs1.next();
                Teacher teacherObject = new Teacher(rs1.getString("first_name"), rs1.getString("last_name"), teacher_id);

                row.set(2, teacherObject);

                sql = String.format("SELECT * FROM course WHERE id = %d", course_id);
                rs1 = statement.executeQuery(sql);
                rs1.next();
                Course courseObject = new Course(rs1.getString("title"), rs1.getInt("id"), rs1.getInt("course_type"));
                row.set(1, courseObject);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        Object[][] tableData = new Object[0][0];
        if (data.size()!= 0)
            tableData = new Object[data.size()][data.get(0).size()];
        for (int i = 0; i < data.size(); i++)
        {
            tableData[i] = data.get(i).toArray();
        }
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(1); nonEditableColumns.add(2);
        Table t = new Table(new String[]{"section_id", "course_name", "teacher_name"}, tableData, nonEditableColumns);
        t.setVisible(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setResizingAllowed(false);
        sectionTable = t;
        sectionScrollPane.setViewportView(sectionTable);
    }
    public void constructJTables()
    {
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(3);
        teacherTable = constructTable("SELECT * FROM teacher WHERE id >= 1;", new String[]{"id", "first_name", "last_name"}, nonEditableColumns);
        studentTable = constructTable("SELECT * FROM student WHERE id >= 1;", new String[]{"id", "first_name", "last_name"}, nonEditableColumns);
        nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(2);
        courseTable = constructTable("SELECT * FROM course WHERE id >= 1;", new String[]{"id", "title", "type"}, nonEditableColumns);
        constructSectionTable();

        teacherScrollPane.setViewportView(teacherTable);
        studentScrollPane.setViewportView(studentTable);
        courseScrollPane.setViewportView(courseTable);
        sectionScrollPane.setViewportView(sectionTable);


        nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(1);

        studentScheduleTable = null;
        sectionsTaughtTable = null;
        rosterTable = null;
        teacherTable.getModel().addTableModelListener(e -> {
            System.out.println("Something");
        });
        sectionTable.getSelectionModel().addListSelectionListener(e -> {
            if (sectionTable.getSelectedRow() == -1)
                return;
            rosterTable = constructRosterTable((Integer)sectionTable.getValueAt(sectionTable.getSelectedRow(), 0));
            rosterScrollPane.setViewportView(rosterTable);
            updateAvailableStudentsToAddToRoster();

            Object selectedTeacherId = sectionTable.getValueAt(sectionTable.getSelectedRow(), 2);
            teachersAvailable.setSelectedItem(selectedTeacherId);
            teacherTable.clearSelection();
            courseTable.clearSelection();
            studentTable.clearSelection();

        });
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (studentTable.getSelectedRow() == -1)
                return;
            studentScheduleTable = constructScheduleTable((Integer)studentTable.getValueAt(studentTable.getSelectedRow(), 0));
            scheduleScrollPane.setViewportView(studentScheduleTable);

            teacherTable.clearSelection();
            courseTable.clearSelection();
            sectionTable.clearSelection();

        });
        teacherTable.getSelectionModel().addListSelectionListener(e -> {
            if (teacherTable.getSelectedRow() == -1)
                return;
            sectionsTaughtTable = constructSectionTaughtTable((Integer)teacherTable.getValueAt(teacherTable.getSelectedRow(), 0));
            sectionsTaughtScrollPane.setViewportView(sectionsTaughtTable);

            studentScheduleTable = constructScheduleTable(0);
            scheduleScrollPane.setViewportView(studentScheduleTable);

            studentTable.clearSelection();
            courseTable.clearSelection();
            sectionTable.clearSelection();
        });
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (courseTable.getSelectedRow() == -1)
                return;
            int type = (Integer)courseTable.getValueAt(courseTable.getSelectedRow(), 2);
            ButtonModel bm;
            if (type == 0)
                bm = courseACAbutton.getModel();
            else if (type == 1)
                bm = courseKAPbutton.getModel();
            else
                bm = courseAPbutton.getModel();
            courseTypeButtonGroup.setSelected(bm, true);
            courseName.setText((String)courseTable.getValueAt(courseTable.getSelectedRow(), 1));

            sectionsTaughtTable = constructSectionTaughtTable(0);
            sectionsTaughtScrollPane.setViewportView(sectionsTaughtTable);

            studentScheduleTable = constructScheduleTable(0);
            scheduleScrollPane.setViewportView(studentScheduleTable);

            teacherTable.clearSelection();
            studentTable.clearSelection();
        });
    }
    public void enableView(JPanel panel)
    {
        for (JPanel j : allGUIitems)
        {
            boolean isPanel = j == panel;
            j.setVisible(isPanel);
            j.setEnabled(isPanel);
        }
    }
    public Table constructRosterTable(int sectionId)
    {
        ArrayList<String> studentFirstNames = new ArrayList<>();
        ArrayList<String> studentLastNames = new ArrayList<>();
        ArrayList<Integer> studentIds = new ArrayList<>();
        try {

            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT student_id FROM enrollment WHERE section_id = " + sectionId + ";" );
            while (rs != null && rs.next()){
                studentIds.add(rs.getInt("student_id"));
            }
            System.out.println("student ids: " + studentIds);
            for (Integer id : studentIds)
            {
                String sqlCommand = String.format("SELECT first_name, last_name FROM student WHERE id = %d", id);
                rs = s.executeQuery(sqlCommand);
                rs.next();
                studentFirstNames.add(rs.getString("first_name"));
                studentLastNames.add(rs.getString("last_name"));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        for (int i = 0; i < studentIds.size(); i++){
            ArrayList<Object> a = new ArrayList<>();
            a.add(studentLastNames.get(i));
            a.add(studentFirstNames.get(i));
            a.add(studentIds.get(i));
            data.add(a);
        }
        data.sort((o1, o2) -> {
            String os1 = (String)o1.get(0);
            String os2 = (String)o2.get(0);
            return os1.compareTo(os2);
        });
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(1); nonEditableColumns.add(2);
        Object[][] dataArray = new Object[0][0];
        if (data.size()!= 0)
            dataArray = new Object[data.size()][data.get(0).size()];
        for (int i = 0; i < data.size(); i++)
        {
            dataArray[i] = data.get(i).toArray();
        }
        Table t = new Table(new String[]{"last_name", "first_name", "student_id"}, dataArray, nonEditableColumns);
        t.setVisible(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setResizingAllowed(false);
        rosterTable = t;
        rosterScrollPane.setViewportView(rosterTable);
        return t;
    }
    public Table constructTable(String sql, String[] columnNames, ArrayList<Integer> nonEditableColumns)
    {

        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs != null && rs.next()){
                ArrayList<Object> a1 = new ArrayList<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                {
                    a1.add(rs.getObject(i));
                }
                data.add(a1);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        Object[][] tableData = new Object[0][0];
        if (data.size()!= 0)
            tableData = new Object[data.size()][data.get(0).size()];
        for (int i = 0; i < data.size(); i++)
        {
            tableData[i] = data.get(i).toArray();
        }
        Table t = new Table(columnNames, tableData, nonEditableColumns);
        t.setVisible(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setResizingAllowed(false);
        return t;
    }
    public Table constructSectionTaughtTable(int id)
    {
        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0);nonEditableColumns.add(1);
        ArrayList<Object> courseNames = new ArrayList<>();
        ArrayList<Object> sections = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();

            //gets all sections of student
            ResultSet rs = statement.executeQuery("SELECT id FROM section WHERE teacher_id = " + id);

            ArrayList<Integer> courseIds = new ArrayList<>();
            while (rs != null && rs.next()){
                sections.add(rs.getObject(1));
            }
            //gets the courses of the sections
            System.out.println(sections);
            for (Object i : sections)
            {
                Integer section_id = (Integer)i;
                rs = statement.executeQuery("SELECT course_id FROM section WHERE id = " + section_id);
                rs.next();
                courseIds.add(rs.getInt("course_id"));
            }
            for (Integer i : courseIds)
            {
                rs = statement.executeQuery("SELECT title FROM course WHERE id = " + i);
                rs.next();
                courseNames.add(rs.getObject("title"));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        Object[][] tableData = new Object[0][0];
        if (!sections.isEmpty())
            tableData = new Object[sections.size()][courseNames.size()];
        for (int i = 0; i < sections.size(); i++)
        {
            tableData[i] = new Object[]{sections.get(i), courseNames.get(i)};
        }
        Table t = new Table(new String[]{"section_id", "course_name"}, tableData, nonEditableColumns);
        sectionsTaughtTable = t;
        sectionsTaughtScrollPane.setViewportView(sectionsTaughtTable);
        t.setVisible(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setResizingAllowed(false);
        return t;
    }
    public Table constructScheduleTable(int id)
    {
        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0);nonEditableColumns.add(1);
        ArrayList<Object> courseNames = new ArrayList<>();
        ArrayList<Object> sections = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();

            //gets all sections of student
            ResultSet rs = statement.executeQuery("SELECT section_id FROM enrollment WHERE student_id = " + id);

            ArrayList<Integer> courseIds = new ArrayList<>();
            while (rs != null && rs.next()){
                sections.add(rs.getObject(1));
            }
            //gets the courses of the sections
            System.out.println(sections);
            for (Object i : sections)
            {
                Integer section_id = (Integer)i;
                rs = statement.executeQuery("SELECT course_id FROM section WHERE id = " + section_id);
                rs.next();
                courseIds.add(rs.getInt("course_id"));
            }
            for (Integer i : courseIds)
            {
                rs = statement.executeQuery("SELECT title FROM course WHERE id = " + i);
                rs.next();
                courseNames.add(rs.getObject("title"));
            }



        }catch (Exception e) {
            e.printStackTrace();
        }
        Object[][] tableData = new Object[0][0];
        if (!sections.isEmpty())
            tableData = new Object[sections.size()][courseNames.size()];
        for (int i = 0; i < sections.size(); i++)
        {
            tableData[i] = new Object[]{sections.get(i), courseNames.get(i)};
        }
        Table t = new Table(new String[]{"section_id", "course_name"}, tableData, nonEditableColumns);
        studentScheduleTable = t;
        scheduleScrollPane.setViewportView(studentScheduleTable);
        t.setVisible(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setResizingAllowed(false);
        return t;
    }

}
class VisualList<E> extends JList{
    private ArrayList<E> data;
    public VisualList(ArrayList<E> data)
    {
        super(data.toArray());
        this.data = data;
    }
    public void add(E newInfo)
    {
        data.add(newInfo);
        setListData(data.toArray());
    }
    public void remove(E info)
    {
        data.remove(info);
        setListData(data.toArray());
    }
    public void clear()
    {
        data.clear();
        setListData(data.toArray());
    }
}
class Table extends JTable{

    private String[] columnNames;
    private Object[][] data;
    private ArrayList<Integer> nonEditableColumns;
    public Table(String[] columnNames, Object[][] data, ArrayList<Integer> nonEditableColumns) {
        super(data, columnNames);
        this.data = data;
        this.columnNames = columnNames;
        this.nonEditableColumns = nonEditableColumns;
        DefaultTableModel model = new DefaultTableModel(data, columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return !nonEditableColumns.contains(column);
            }
        };
        this.setModel(model);
    }

}
class Button extends JButton{
    public Button(String title, int x, int y, int width, int height, JPanel panel)
    {
        super(title);
        setBounds(x, y, width, height);
        setVisible(true);
        panel.add(this);
    }
}
class TextFieldWithLabel extends JTextField{
    private JLabel label;
    public TextFieldWithLabel(String title, int x, int y, int width, int height, JPanel p)
    {
        super();
        setBounds(x, y, width, height);
        label = new JLabel(title);
        label.setBounds(x-100, y, width, height);
        p.add(this);
        p.add(label);
    }
}
class RadioButton extends JRadioButton{
    public RadioButton(String title,int x, int y, int width, int height, ButtonGroup gp, JPanel panel){
        super(title);
        setBounds(x, y, width, height);
        gp.add(this);
        panel.add(this);

    }
}
class ComboBox<E> extends JComboBox<E>{
    private ArrayList<Integer> actualIds;
    public ComboBox(int x, int y, int width, int height, JPanel panel, ArrayList<E> items){
        super();
        setBounds(x, y, width, height);
        for (E item : items)
            addItem(item);
        panel.add(this);

    }
    public ComboBox(int x, int y, int width, int height, JPanel panel){
        super();
        setBounds(x, y, width, height);
        panel.add(this);
    }
    public void setItems(ArrayList<E> items){
        removeAllItems();
        for (E item : items)
            addItem(item);
    }

    public ArrayList<Integer> getActualIds() {
        return actualIds;
    }

    public void setActualIds(ArrayList<Integer> actualIds) {
        this.actualIds = actualIds;
    }
}
class Student{
    private String firstName;
    private String lastName;
    private int id;

    public Student(String firstName, String lastName, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    @Override
    public String toString() {
        return lastName + ", " + firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }
}
class Teacher{
    private String firstName;
    private String lastName;
    private int id;

    public Teacher(String firstName, String lastName, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    @Override
    public String toString() {
        return lastName + ", " + firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }
}
class Course{
    private String title;
    private int id;
    private int type;

    public Course(String title, int id, int type) {
        this.title = title;
        this.type = type;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return title;
    }
}
