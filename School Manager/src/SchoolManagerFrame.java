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
    private Table courseTable;
    private Table sectionTable;
    private Statement statement;

    private JPanel teacherPanel;
    private JPanel studentPanel;
    private JPanel coursePanel;
    private JPanel sectionPanel;
    public SchoolManagerFrame(Statement statement)
    {
        super("School Manager");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        this.statement = statement;


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
            teacherMenuItem.setSelected(false);
            viewMenu.setSelected(false);
        });
        studentMenuItem.addActionListener(e -> {
            enableView(studentPanel);
            teacherMenuItem.setSelected(false);
            viewMenu.setSelected(false);

        });
        courseMenuItem.addActionListener(e -> {

        });
        sectionMenuItem.addActionListener(e -> {

        });
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);


        try{

            ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(3);

            ResultSet rs = statement.executeQuery("SELECT * FROM teacher WHERE id >= 1;");
            teacherTable = constructTable(rs, new String[]{"id", "first_name", "last_name"}, nonEditableColumns);

            rs = statement.executeQuery("SELECT * FROM student WHERE id >= 1;");
            studentTable = constructTable(rs, new String[]{"id", "first_name", "last_name", "section"}, nonEditableColumns);
            studentTable.getSelectionModel().addListSelectionListener(e -> {
                System.out.println(studentTable.getSelectedRow());
                System.out.println(studentTable.getColumnModel().getColumn(studentTable.getSelectedColumn()));
            });

            rs = statement.executeQuery("SELECT * FROM course WHERE id >= 1;");
            courseTable = constructTable(rs, new String[]{"id", "title", "type"}, nonEditableColumns);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        JScrollPane teacherScrollPane = new JScrollPane(teacherTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane studentScrollPane = new JScrollPane(studentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        teacherScrollPane.setBounds(10, 10, 500, 550);
        studentScrollPane.setBounds(10, 10, 500, 550);
        teacherPanel = new JPanel(null);
        studentPanel = new JPanel(null);
        coursePanel = new JPanel(null);
        sectionPanel = new JPanel(null);

        teacherPanel.add(teacherScrollPane);
        teacherPanel.setSize(getWidth(), getHeight());

        studentPanel.add(studentScrollPane);
        studentPanel.setSize(getWidth(), getHeight());

        allGUIitems = new ArrayList<>(); allGUIitems.add(teacherPanel);allGUIitems.add(studentPanel);allGUIitems.add(coursePanel);allGUIitems.add(sectionPanel);
        add(teacherPanel);
        add(studentPanel);
        enableView(teacherPanel);
        setVisible(true);

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
    public Table constructTable(ResultSet rs, String[] columnNames, ArrayList<Integer> nonEditableColumns)
    {
        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        try {
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
class Panel extends JPanel{

    public Panel(Frame f, LayoutManager l)
    {
        super(l);
        setBounds(10, 10, getWidth(), getHeight());
        f.add(this);
    }
}