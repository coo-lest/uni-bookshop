import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import org.jdesktop.swingx.JXTreeTable;

public class OBS {
    DBConn dbConn = new DBConn("e8252125", "e8252125");

//    DBConn dbConn = new DBConn("e8250009", "e8250009", "faith.comp.hkbu.edu.hk", 22,
//            "e825xxxx", "********");

    public OBS() {
        List<String> sids = dbConn.selectSid();

        JFrame homePage = new JFrame("Online University Bookshop");
        homePage.setSize(600, 400);
        homePage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = homePage.getContentPane();
        c.setLayout(new GridLayout(4, 1, 20, 20));
        c.add(new JLabel("Welcome to Online University Bookshop! Please choose one function!", SwingConstants.CENTER));

        Button b1 = new Button("Order Search");
        Button b2 = new Button("Order Making");
        b1.setBounds(50, 300, 200, 50);
        b1.setBounds(200, 300, 100, 50);
        c.add(b1);
        c.add(b2);

        JLabel jl = new JLabel("You have outstanding orders! Can't make a new order!", SwingConstants.CENTER);
        c.add(jl);
        jl.setVisible(false);

        b1.addActionListener(e -> {
            String sid = login(sids);
            orderSearching(sid);

        });

        b2.addActionListener(e -> {
            jl.setVisible(false);
            String sid = login(sids);
            jl.setVisible(!orderMaking(sid));
        });

        homePage.setVisible(true);
    }

    public String login(List<String> sids) {
        boolean ifexist = true;
        String sid;
        do {
            JPanel panel = new JPanel();
            final TextField sidField = new TextField();
            panel.setLayout(new GridLayout(3, 1));
            panel.add(new JLabel("Please login with your student ID:"));
            panel.add(sidField);
            JLabel jl = new JLabel("Students does not exist!");
            jl.setVisible(!ifexist);
            panel.add(jl);

            JOptionPane pane = new JOptionPane(panel) {
                public void selectInitialValue() {
                    sidField.requestFocusInWindow();
                }
            };

            JDialog dialog = pane.createDialog("Login");
            dialog.setVisible(true);
            dialog.dispose();

            sid = sidField.getText();

            ifexist = false;
            for (String i : sids) {
                if (i.equals(sid)) {
                    ifexist = true;
                    break;
                }
            }

        } while (!ifexist);

        return sid;
    }

    public boolean orderMaking(String sid) {
        List<Order> orders = dbConn.searchOrder(sid);
        for (Order i : orders) {
            if (i.status == 0 || i.status == 1) {
                return false;
            }
        }

        String oid = "" + sid.charAt(6) + sid.charAt(7) + String.format("%02d", orders.size() % 100) + new Date().getTime() % 1000000;
        String[] payInfo = payMethod();
        dbConn.orderMaking(oid, sid, new Date(), payInfo[0], payInfo[1]);

        JFrame omPage = new JFrame("Order Making");
        omPage.setSize(1000, 800);
        omPage.setLayout(new GridLayout(1, 2));
        omPage.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container omc = omPage.getContentPane();
        JTextArea orderInfo = new JTextArea();
        orderInfo.setSize(500, 800);

        String books = "Your books in this order (Order No.:" + oid + "):\n" +
                "-----------------------------------------------------------------------------------------------\n";
        //将books的信息转换为string --TODO

        orderInfo.setText(books);
        omc.add(orderInfo);
        Panel addBooks = new Panel();
        addBooks.setLayout(null);
        omc.add(addBooks);

        JLabel bookL = new JLabel("Input book No. here:");
        JTextField bookT = new JTextField(100);
        JLabel qtyL = new JLabel("Input quantity here:");
        JTextField qtyT = new JTextField(100);
        Button b1 = new Button("Add");
        Button b2 = new Button("Confirm");
        bookL.setBounds(50, 50, 200, 20);
        bookT.setBounds(50, 80, 100, 20);
        qtyL.setBounds(50, 120, 200, 20);
        qtyT.setBounds(50, 150, 100, 20);
        b1.setBounds(50, 200, 100, 40);
        b2.setBounds(50, 600, 100, 40);
        addBooks.add(bookL);
        addBooks.add(bookT);
        addBooks.add(qtyL);
        addBooks.add(qtyT);
        addBooks.add(b1);
        addBooks.add(b2);

        JLabel ifAdd = new JLabel();
        ifAdd.setBounds(50, 260, 500, 20);
        addBooks.add(ifAdd);
        ifAdd.setVisible(false);


        omPage.setVisible(true);

        bookT.addActionListener(e -> bookT.requestFocusInWindow());

        qtyT.addActionListener(e -> bookT.requestFocusInWindow());

        b1.addActionListener(e -> {
            String book_no = bookT.getText();
            String qty = qtyT.getText();
            bookT.setText("");
            qtyT.setText("");

            int conditions = addBook(oid, book_no, qty);
            if (conditions == -1)
                ifAdd.setText("Add books successfully!");
            else if (conditions == -2)
                ifAdd.setText("Fail to add books: Please input book No. and quantity!");
            else if (conditions == -3)
                ifAdd.setText("Fail to add books: Book does not exists!");
            else
                ifAdd.setText("Fail to add books: This book(book No:" + book_no + ") is out of stock! Remaining quantity: " + conditions);

            ifAdd.setVisible(true);
        });

        b2.addActionListener(e -> omPage.dispose());

        return true;
    }

    public String[] payMethod() {
        String[] payInfo = {null, null};

        do {
            JComboBox<String> pay = new JComboBox<>();
            JLabel jl = new JLabel("Please choose one method to pay");
            pay.addItem("Credit card");
            pay.addItem("Bank transfer");
            pay.addItem("Cash");
            pay.addItem("Octopus");
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1));
            panel.add(jl);
            panel.add(pay);

            JOptionPane pane = new JOptionPane(panel) {
                public void selectInitialValue() {
                    pay.requestFocusInWindow();
                }
            };

            JDialog dialog = pane.createDialog("Pay method");
            dialog.setVisible(true);
            dialog.dispose();

            payInfo[0] = (String) pay.getSelectedItem();

            if (payInfo[0].equals("Credit card")) {

                JPanel panel2 = new JPanel();
                final TextField cardNo = new TextField();
                panel2.setLayout(new GridLayout(2, 1));
                panel2.add(new JLabel("Please input your credit card No.:"));
                panel2.add(cardNo);

                JOptionPane pane2 = new JOptionPane(panel2) {
                    public void selectInitialValue() {
                        cardNo.requestFocusInWindow();
                    }
                };

                JDialog dialog2 = pane2.createDialog("Credit Card No");
                dialog2.setVisible(true);
                dialog2.dispose();

                payInfo[1] = cardNo.getText();
            }

        } while (payInfo[0].equals("Credit card") && payInfo[1].equals(""));

        return payInfo;
    }

    public int addBook(String order_no, String book_no, String qty) {
        if (book_no.equals("") || qty.equals(""))
            return -2;
        else {
            int stock = dbConn.selectStock(book_no);
            if (stock == -1)
                return -3;
            else if (stock < Integer.parseInt(qty))
                return stock;

        }

        dbConn.addBook(order_no, book_no, Integer.parseInt(qty));
        return -1;
    }

    public void orderSearching(String sid) {
        // Retrieve data from DB
        List<Order> orders = dbConn.searchOrder(sid);   // A List of Order
        List<BookInOrder>[] bookInOrders = new List[orders.size()];  // An array of Lists of bookInOrder
        // Get bookInOrder for every order
        for (int i = 0; i < orders.size(); i++) {
            bookInOrders[i] = dbConn.searchBookInOrder(orders.get(i).orderNo);
        }

        // Display settings
        JFrame osPage = new JFrame("All of your orders");
        osPage.setLayout(null);
        osPage.setSize(800, 1000);
        Container osc = osPage.getContentPane();

        OrderInfoTable table = new OrderInfoTable(orders, bookInOrders);
        JXTreeTable jxTable = table.getTreeTable();

        JScrollPane pane = new JScrollPane(jxTable);
        pane.setBounds(0, 0, 600, 1000);
        osc.add(pane);

        Button b1 = new Button("Order Update");
        Button b2 = new Button("Order Cancelling");
        b1.setEnabled(false);
        b2.setEnabled(false);
        b1.setBounds(650, 50, 100, 40);
        b2.setBounds(650, 150, 100, 40);
        osc.add(b1);
        osc.add(b2);

        // Row selection
        int selectedType;   // 0: invalid; 1: Order; 2: Book
        final String[] orderNo = new String[1];
        final String[] bookNo = new String[1];
        jxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    // Single selection allowed only

        jxTable.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                int selectedRow = jxTable.getSelectedRow();
                TreePath path = jxTable.getPathForRow(selectedRow);
                Node selectedNode = table.getNode(path);

                orderNo[0] = jxTable.getStringAt(selectedRow, 0);
                bookNo[0] = jxTable.getStringAt(selectedRow, 1);

                System.out.println(orderNo[0]);
                System.out.println(bookNo[0]);
                if (selectedNode.isLeaf()) {    // Book selected
                    b1.setEnabled(true);
                    b2.setEnabled(false);
                } else {    // Order selected
                    b1.setEnabled(false);
                    b2.setEnabled(true);
                }
            }
        });


        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateOrder(orderNo[0], bookNo[0]);
                // TODO: REFRESH AFTER UPDATE!
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                orderCancelling(sid);
            }
        });

        osPage.setVisible(true);

    }

    public void updateOrder(String orderNo, String bookNo) {
        dbConn.orderUpdate(orderNo, bookNo, new Date());
        // TODO Handle exceptions
    }

    public void orderCancelling(String sid) {

    }

    public static void main(String[] args) {
        OBS obs = new OBS();
    }

}