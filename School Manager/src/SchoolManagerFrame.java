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

    private ArrayList<Component> teacherGUIitems;
    private ArrayList<Component> studentGUIitems;
    private ArrayList<Component> courseGUIitems;
    private ArrayList<Component> sectionGUIitems;
    private ArrayList<ArrayList<Component>> allGUIitems;

    private Table teacherTable;
    private Table studentTable;
    private Table courseTable;
    private Table sectionTable;
    private Statement statement;
    public SchoolManagerFrame(Statement statement)
    {
        super("School Manager");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        this.statement = statement;
        teacherGUIitems = new ArrayList<>();
        studentGUIitems = new ArrayList<>();
        courseGUIitems = new ArrayList<>();
        sectionGUIitems = new ArrayList<>();
        JMenuBar menuBar = new JMenuBar();
        JScrollPane scrollPane = new JScrollPane();
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
            scrollPane.setViewportView(teacherTable);
            teacherMenuItem.setSelected(false);
            viewMenu.setSelected(false);
        });
        studentMenuItem.addActionListener(e -> {
            scrollPane.setViewportView(studentTable);
            studentMenuItem.setSelected(false);
            viewMenu.setSelected(false);
        });
        courseMenuItem.addActionListener(e -> {
            //scrollPane.setViewportView(teacherTable);
            studentMenuItem.setSelected(false);
            viewMenu.setSelected(false);
        });
        sectionMenuItem.addActionListener(e -> {
            //scrollPane.setViewportView(teacherTable);
            studentMenuItem.setSelected(false);
            viewMenu.setSelected(false);
        });
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        String[] teacherColumnNames = {"id", "first_name", "last_name"};

        ArrayList<ArrayList<Object>> data = new ArrayList<>();


        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(10, 10, 500, 550);

        add(scrollPane);
        Object[][] teacherTableData = new Object[0][0];
        try{
            ResultSet rs = statement.executeQuery("SELECT * FROM teacher WHERE id >= 1;");
            while (rs != null && rs.next())
            {
                ArrayList<Object> a1 = new ArrayList<>();
                a1.add(rs.getInt("id"));
                a1.add(rs.getString("first_name"));
                a1.add(rs.getString("last_name"));
                data.add(a1);
            }
            ArrayList<Integer> nonEditableColumns = new ArrayList<>(); nonEditableColumns.add(0); nonEditableColumns.add(3);
            teacherTable = constructTable(data, teacherColumnNames, nonEditableColumns);
            data.clear();
            rs = statement.executeQuery("SELECT * FROM student WHERE id >= 1;");
            while (rs != null && rs.next())
            {
                ArrayList<Object> a1 = new ArrayList<>();
                a1.add(rs.getInt("id"));
                a1.add(rs.getString("first_name"));
                a1.add(rs.getString("last_name"));
                a1.add(rs.getString("section"));
                data.add(a1);
            }

            studentTable = constructTable(data, teacherColumnNames, nonEditableColumns);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        allGUIitems = new ArrayList<>(); allGUIitems.add(teacherGUIitems);allGUIitems.add(studentGUIitems);allGUIitems.add(courseGUIitems);allGUIitems.add(sectionGUIitems);


        setVisible(true);

    }
    public void enableView(int indexNum)
    {
        int i = 0;
        for (ArrayList<Component> guiItems : allGUIitems)
        {
            if (i == indexNum)
            {
                for (Component guiItem : guiItems)
                {
                    guiItem.setEnabled(true);
                    guiItem.setVisible(true);

                }
            }
            else{
                for (Component guiItem : guiItems)
                {
                    guiItem.setEnabled(false);
                    guiItem.setVisible(false);

                }
            }

            i++;
        }
    }
    public Table constructTable(ArrayList<ArrayList<Object>> data, String[] columnNames, ArrayList<Integer> nonEditableColumns)
    {
        Object[][] tableData = new Object[0][0];
        if (data.size()!= 0)
            tableData = new Object[data.size()][data.get(0).size()];
        for (int i = 0; i < data.size(); i++)
        {
            tableData[i] = data.get(i).toArray();
        }
        return new Table(columnNames, tableData, nonEditableColumns);
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