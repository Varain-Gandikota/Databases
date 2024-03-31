import javax.print.attribute.standard.JobMediaSheetsSupported;
import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import javax.swing.table.DefaultTableModel;
import javax.xml.transform.Result;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.ObjectOutputStream;
import java.util.*;
import java.sql.*;

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
    private ComboBox<Integer> coursesAvailable;
    private ComboBox<Integer> teachersAvailable;
    private ComboBox<Integer> studentsAvailable;
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
            sectionsTaughtTable = constructSectionTaughtTable(0);
            sectionsTaughtScrollPane.setViewportView(sectionsTaughtTable);
            studentTable.clearSelection();

            viewMenu.setSelected(false);
        });
        studentMenuItem.addActionListener(e -> {
            enableView(studentPanel);
            viewMenu.setSelected(false);

        });
        courseMenuItem.addActionListener(e -> {
            enableView(coursePanel);
            viewMenu.setSelected(false);

        });
        sectionMenuItem.addActionListener(e -> {
            enableView(sectionPanel);
            constructRosterTable(0);
            rosterTable.clearSelection();
            sectionTable.clearSelection();
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

        teacherPanel.add(teacherScrollPane);
        teacherPanel.add(sectionsTaughtScrollPane);
        teacherPanel.setSize(getWidth(), getHeight());

        studentPanel.add(studentScrollPane);
        studentPanel.add(scheduleScrollPane);
        studentPanel.setSize(getWidth(), getHeight());

        sectionPanel.add(sectionScrollPane);
        sectionPanel.add(rosterScrollPane);
        sectionPanel.setSize(getWidth(), getHeight());


        studentSaveChanges = new Button("Save Alterations to Database", 625, 500, 300, 50, studentPanel);
        addStudentButton = new Button("Add Student", 625, 300, 300, 50, studentPanel);
        removeStudentButton = new Button("Remove Selected Student", 625, 400, 300, 50, studentPanel);

        teacherSaveChanges = new Button("Save Alterations to Database", 625, 500, 300, 50, teacherPanel);
        addTeacherButton = new Button("Add Teacher", 625, 300, 300, 50, teacherPanel);
        removeTeacherButton = new Button("Remove Selected Teacher", 625, 400, 300, 50, teacherPanel);

        saveCourseChangesButton = new Button("Save Course Alterations to Database", 625, 500, 300, 50, coursePanel);
        addCourseButton = new Button("Add Course", 625, 300, 300, 50, coursePanel);
        removeCourseButton = new Button("Remove Selected Course", 625, 400, 300, 50, coursePanel);

        Button addStudentToRosterButton = new Button("Add student to roster", 690, 300, 170, 25, sectionPanel);
        Button removeStudentToRosterButton = new Button("Remove selected student from roster", 640, 350, 270, 25, sectionPanel);//32
        addStudentToRosterButton.addActionListener(e -> {
            try{
                if (studentsAvailable.getSelectedItem() == null || sectionTable.getRowCount() == 0)
                    return;
                Statement s = connection.createStatement();
                Integer selectedId = (Integer)studentsAvailable.getSelectedItem();
                Integer selectedSectionId = (Integer)sectionTable.getValueAt(sectionTable.getSelectedRow(), 0);
                String sql = String.format("INSERT INTO enrollment(section_id, student_id) VALUES (%d, %d);", selectedSectionId, selectedId);
                s.executeUpdate(sql);
                rosterTable = constructRosterTable(selectedSectionId);
                rosterScrollPane.setViewportView(rosterTable);
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
                s.executeUpdate("DELETE FROM teacher WHERE id = " + (Integer)teacherTable.getValueAt(teacherTable.getSelectedRow(), 0) + ";");
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
        teachersAvailable = new ComboBox<>(700, 150, 150, 25, sectionPanel, teachersAvailableArrayList);
        coursesAvailable = new ComboBox<>(700, 200, 150, 25, sectionPanel, coursesAvailableArrayList);
        studentsAvailable = new ComboBox<>(700, 250, 150, 25, sectionPanel, coursesAvailableArrayList);

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

        allGUIitems = new ArrayList<>(); allGUIitems.add(teacherPanel);allGUIitems.add(studentPanel);allGUIitems.add(coursePanel);allGUIitems.add(sectionPanel);
        updateActiveTeachersAndCourses();
        add(teacherPanel);
        add(studentPanel);
        add(coursePanel);
        add(sectionPanel);
        enableView(teacherPanel);
        setResizable(false);
        setVisible(true);

    }
    public void updateActiveTeachersAndCourses()
    {
        try{

            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT id FROM teacher WHERE id >= 1");
            teachersAvailableArrayList.clear();
            coursesAvailableArrayList.clear();
            while (rs != null && rs.next()){
                teachersAvailableArrayList.add(rs.getInt(1));
            }
            rs = s.executeQuery("SELECT id FROM course WHERE id >= 1");
            while (rs != null && rs.next()){
                coursesAvailableArrayList.add(rs.getInt(1));
            }
            coursesAvailable.setItems(coursesAvailableArrayList);
            teachersAvailable.setItems(teachersAvailableArrayList);
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
            String sql = String.format("SELECT id FROM student WHERE id NOT IN %s;", notIds);
            if (rosterTable.getRowCount() == 0){
                sql = "SELECT id FROM student WHERE id >= 1;";
            }
            ResultSet rs = s.executeQuery(sql);
            while (rs != null && rs.next()){
                studentsAvailableArrayList.add(rs.getInt("id"));
            }
            studentsAvailable.setItems(studentsAvailableArrayList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void constructJTables()
    {
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(3);
        teacherTable = constructTable("SELECT * FROM teacher WHERE id >= 1;", new String[]{"id", "first_name", "last_name"}, nonEditableColumns);
        studentTable = constructTable("SELECT * FROM student WHERE id >= 1;", new String[]{"id", "first_name", "last_name"}, nonEditableColumns);
        nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(2);
        courseTable = constructTable("SELECT * FROM course WHERE id >= 1;", new String[]{"id", "title", "type"}, nonEditableColumns);
        nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(1); nonEditableColumns.add(2);
        sectionTable = constructTable("SELECT * FROM section WHERE id >= 1;", new String[]{"id", "course_id", "teacher_id"}, nonEditableColumns);



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
            //sectionTable.clearSelection();
        });
    }
    public void enableView(JPanel panel)
    {
        for (JPanel j : allGUIitems)
        {
            j.setVisible(j == panel);
            j.setEnabled(j == panel);
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
    public ComboBox(int x, int y, int width, int height, JPanel panel, ArrayList<E> items){
        super();
        setBounds(x, y, width, height);
        for (E item : items)
            addItem(item);
        panel.add(this);

    }
    public void setItems(ArrayList<E> items){
        removeAllItems();
        for (E item : items)
            addItem(item);
    }
}
