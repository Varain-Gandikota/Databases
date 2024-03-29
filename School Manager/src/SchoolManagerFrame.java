import javax.print.attribute.standard.JobMediaSheetsSupported;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.*;
import java.util.Objects;

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
    private Button addStudentButton;
    private JScrollPane scheduleScrollPane;
    private JScrollPane teacherScrollPane;
    private JScrollPane studentScrollPane;
    private JScrollPane courseScrollPane;
    private Button removeStudentButton;
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
        constructJTables();
        teacherScrollPane.setBounds(10, 10, 500, 550);
        studentScrollPane.setBounds(10, 10, 500, 550);
        courseScrollPane.setBounds(10, 10, 500, 550);
        scheduleScrollPane.setBounds(520, 10, 350, 100);

        teacherPanel = new JPanel(null);
        studentPanel = new JPanel(null);
        coursePanel = new JPanel(null);
        sectionPanel = new JPanel(null);

        teacherPanel.add(teacherScrollPane);
        teacherPanel.setSize(getWidth(), getHeight());

        studentPanel.add(studentScrollPane);
        studentPanel.add(scheduleScrollPane);
        studentPanel.setSize(getWidth(), getHeight());


        studentSaveChanges = new Button("Save Alterations to Database", 625, 500, 300, 50, studentPanel);
        addStudentButton = new Button("Add Student", 625, 300, 300, 50, studentPanel);
        removeStudentButton = new Button("Remove Selected Student", 625, 400, 300, 50, studentPanel);
        studentSaveChanges.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel)studentTable.getModel();
            try{
                Statement s = connection.createStatement();
                for (int row = 0; row < studentTable.getRowCount(); row++)
                {
                    String sqlCommand = String.format("UPDATE student SET first_name= '%s', last_name = '%s' WHERE id = %s",
                            studentTable.getValueAt(row, 1), studentTable.getValueAt(row, 2), studentTable.getValueAt(row, 0));
                    s.executeUpdate(sqlCommand);
                }
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
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        studentFirstName = new TextFieldWithLabel("First Name: ", 625, 150, 300, 50, studentPanel);
        studentLastName = new TextFieldWithLabel("Last Name: ", 625, 225, 300, 50, studentPanel);

        coursePanel.add(courseScrollPane);
        coursePanel.setSize(getWidth(), getHeight());

        allGUIitems = new ArrayList<>(); allGUIitems.add(teacherPanel);allGUIitems.add(studentPanel);allGUIitems.add(coursePanel);allGUIitems.add(sectionPanel);
        add(teacherPanel);
        add(studentPanel);
        add(coursePanel);
        add(sectionPanel);
        enableView(teacherPanel);
        setResizable(false);
        setVisible(true);

    }
    public void constructJTables()
    {
        ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(3);
        teacherTable = constructTable("SELECT * FROM teacher WHERE id >= 1;", new String[]{"id", "first_name", "last_name"}, nonEditableColumns);
        studentTable = constructTable("SELECT * FROM student WHERE id >= 1;", new String[]{"id", "first_name", "last_name"}, nonEditableColumns);
        courseTable = constructTable("SELECT * FROM course WHERE id >= 1;", new String[]{"id", "title", "type"}, nonEditableColumns);

        teacherScrollPane.setViewportView(teacherTable);
        studentScrollPane.setViewportView(studentTable);
        courseScrollPane.setViewportView(courseTable);

        nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(1);

        studentScheduleTable = null;
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (studentTable.getSelectedRow() == -1)
                return;
            DefaultTableModel model = (DefaultTableModel)studentTable.getModel();


            studentScheduleTable = constructScheduleTable((Integer)studentTable.getValueAt(studentTable.getSelectedRow(), 0));
            scheduleScrollPane.setViewportView(studentScheduleTable);
            for (int i = 0; i < studentTable.getColumnCount(); i++)
            {
                System.out.println(studentTable.getValueAt(studentTable.getSelectedRow(), i));
            }
            System.out.println(studentTable.getColumnModel().getColumn(studentTable.getSelectedColumn()));
        });
    }
    public void enableView(JPanel panel)
    {
        for (JPanel j : allGUIitems)
        {
            if (j == panel)
            {
               j.setVisible(true);
               j.setEnabled(true);
            }
            else{
                j.setVisible(false);
                j.setEnabled(false);
            }
        }
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
