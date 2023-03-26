/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author omar
 */
public class mygui extends javax.swing.JFrame {

    DefaultTableModel dtmForEmp;
    DefaultTableModel dtmForArticle;
    DefaultTableModel dtmForCmd;
    DefaultTableModel dtmForSelectedCmd;
    DefaultTableModel dtmForStock;

    Connection cnx;
    Statement stmt;
    ResultSet rsFroEmp;
    ResultSet rsForArti;
    ResultSet rsFroCmd;
    ResultSet rsFroSelectedCmd;
    ResultSet rsForStock;
    ResultSet rsForStockToDelete;

    static String cinToUpdate;
    static String nomToUpdate;
    static String prenomToUpdate;

    static String articleIdToUpdate;
    static String articleNomToUpdate;
    static String articlePrixToUpdate;
    static String articleIdStockToUpdate;

    String selectedNumInCmdTable;

    static String selectedNumCmd;

    static ArrayList<String> idsOfDamendedArticles;

    public mygui() {
        initComponents();

        setLocationRelativeTo(null);

        //-----setting up table model for employees table
        custemTable(empTable);
        dtmForEmp = (DefaultTableModel) empTable.getModel();
        //-----setting up table model for articeles table
        custemTable(articleTable);
        dtmForArticle = (DefaultTableModel) articleTable.getModel();
        //-----setting up table model for cmd table
        custemTable(cmdTable);
        dtmForCmd = (DefaultTableModel) cmdTable.getModel();
        //-----setting up table model for Selected cmd table
        custemTable(cmdOfEmpTable);
        dtmForSelectedCmd = (DefaultTableModel) cmdOfEmpTable.getModel();
        //-----setting up table model for Selected cmd table
        custemTable(stockTable);
        dtmForStock = (DefaultTableModel) stockTable.getModel();

        // Filling JTable with data from DB
        cnx = new mysql().getCnx();
        try {
            stmt = cnx.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateEmployesTable();
        updateArticlesTable();
        updateCmdTable();
        updateStockTable();

        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.foreground", Color.white);

    }

    // my methods-----------------------
    public void custemTable(JTable table) {
        // Custimizing the table
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(48, 71, 94));
        table.getTableHeader().setForeground(new Color(255, 255, 255));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setRowHeight(40);
    }

    public void updateEmployesTable() {
        dtmForEmp.setRowCount(0);
        try {
            rsFroEmp = stmt.executeQuery("select * from employe;");
            while (rsFroEmp.next()) {
                Object[] obj = {rsFroEmp.getString(1), rsFroEmp.getString(2), rsFroEmp.getString(3)};
                dtmForEmp.addRow(obj);
            }
        } catch (SQLException ex) {
            Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateArticlesTable() {
        dtmForArticle.setRowCount(0);
        try {
            rsForArti = stmt.executeQuery("select * from article;");
            while (rsForArti.next()) {
                Object[] obj = {rsForArti.getString(1), rsForArti.getString(2), rsForArti.getString(4), rsForArti.getString(3)};
                dtmForArticle.addRow(obj);
            }
        } catch (SQLException ex) {
            Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateCmdTable() {
        dtmForCmd.setRowCount(0);
        try {
            rsFroCmd = stmt.executeQuery("select c.num, e.nom, e.prenom, c.date, c.etat from employe as e, cmd as c "
                    + "where c.cin_emp = e.cin;");

            String isDelevred;
            while (rsFroCmd.next()) {
                isDelevred = (rsFroCmd.getInt(5) == 1) ? "Livré" : "Non livré";//"🗹" : "🗙";

                Object[] obj = {rsFroCmd.getString(1), rsFroCmd.getString(2), rsFroCmd.getString(3), rsFroCmd.getString(4), isDelevred};
                dtmForCmd.addRow(obj);
            }
        } catch (SQLException ex) {
            Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateCmdSelectedTable() {
        dtmForSelectedCmd.setRowCount(0);
        try {
            rsFroSelectedCmd = stmt.executeQuery("select a.id, a.nom, a.prix, l.qte from lien_cmd_article as l, article as a where l.num_cmd ='"
                    + cmdTable.getValueAt(cmdTable.getSelectedRow(), 0) + "' and l.id_article = a.id;");

            idsOfDamendedArticles = new ArrayList<>();

            int prixTotal = 0;
            while (rsFroSelectedCmd.next()) {
                idsOfDamendedArticles.add(rsFroSelectedCmd.getString(1));
                prixTotal += rsFroSelectedCmd.getInt(3) * rsFroSelectedCmd.getInt(4);
                Object[] obj = {rsFroSelectedCmd.getString(1), rsFroSelectedCmd.getString(2), rsFroSelectedCmd.getString(3),
                    rsFroSelectedCmd.getString(4), rsFroSelectedCmd.getInt(3) * rsFroSelectedCmd.getInt(4)};
                dtmForSelectedCmd.addRow(obj);
            }
            if (dtmForSelectedCmd.getRowCount() > 0) {
                dtmForSelectedCmd.addRow(new Object[]{"", "", "", "", prixTotal});
            }

        } catch (SQLException ex) {
            Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateStockTable() {
        dtmForStock.setRowCount(0);
        try {
            rsForStock = stmt.executeQuery("select s.id, a.nom, s.qte, a.prix, a.id from stock as s, article as a where s.id=a.id_stock;");

            double prixTotal = 0.00;
            while (rsForStock.next()) {
                prixTotal += rsForStock.getInt(3) * rsForStock.getDouble(4);
                Object[] obj = {rsForStock.getString(1), rsForStock.getString(2), rsForStock.getString(4), rsForStock.getString(3), rsForStock.getInt(3) * rsForStock.getDouble(4)};
                dtmForStock.addRow(obj);
            }
            if (dtmForStock.getRowCount() > 0) {
                dtmForStock.addRow(new Object[]{"", "", "", "", prixTotal});
            }

        } catch (SQLException ex) {
            Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sideBar = new javax.swing.JPanel();
        employes = new javax.swing.JLabel();
        articles = new javax.swing.JLabel();
        commandes = new javax.swing.JLabel();
        stocks = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        empPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        empTable = new javax.swing.JTable();
        addEmp = new javax.swing.JLabel();
        modifyEmp = new javax.swing.JLabel();
        deleteEmp = new javax.swing.JLabel();
        artiPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        articleTable = new javax.swing.JTable();
        addArticle = new javax.swing.JLabel();
        modifyArticle = new javax.swing.JLabel();
        deleteArticle = new javax.swing.JLabel();
        cmdPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        cmdTable = new javax.swing.JTable();
        addCmd = new javax.swing.JLabel();
        deleteCmd = new javax.swing.JLabel();
        addCmd1 = new javax.swing.JLabel();
        cmdOfEmploye = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        cmdOfEmpTable = new javax.swing.JTable();
        addArticleToCmd = new javax.swing.JLabel();
        deleteArticleToCmd = new javax.swing.JLabel();
        cmdsTitel = new javax.swing.JLabel();
        goBack = new javax.swing.JLabel();
        stockPanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        stockTable = new javax.swing.JTable();
        deleteStock = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1200, 700));
        setPreferredSize(new java.awt.Dimension(1000, 700));

        sideBar.setBackground(new java.awt.Color(51, 51, 51));

        employes.setBackground(new java.awt.Color(255, 255, 255));
        employes.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        employes.setForeground(new java.awt.Color(255, 255, 255));
        employes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        employes.setText("Employés");
        employes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                employesMouseClicked(evt);
            }
        });

        articles.setBackground(new java.awt.Color(255, 255, 255));
        articles.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        articles.setForeground(new java.awt.Color(153, 153, 153));
        articles.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        articles.setText("Articles");
        articles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                articlesMouseClicked(evt);
            }
        });

        commandes.setBackground(new java.awt.Color(255, 255, 255));
        commandes.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        commandes.setForeground(new java.awt.Color(153, 153, 153));
        commandes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        commandes.setText("Commandes");
        commandes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                commandesMouseClicked(evt);
            }
        });

        stocks.setBackground(new java.awt.Color(255, 255, 255));
        stocks.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        stocks.setForeground(new java.awt.Color(153, 153, 153));
        stocks.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stocks.setText("Stocks");
        stocks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stocksMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout sideBarLayout = new javax.swing.GroupLayout(sideBar);
        sideBar.setLayout(sideBarLayout);
        sideBarLayout.setHorizontalGroup(
            sideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(employes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(articles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(commandes, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
            .addComponent(stocks, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        sideBarLayout.setVerticalGroup(
            sideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideBarLayout.createSequentialGroup()
                .addGap(165, 165, 165)
                .addComponent(employes, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(articles, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(commandes, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(stocks, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(323, Short.MAX_VALUE))
        );

        getContentPane().add(sideBar, java.awt.BorderLayout.LINE_START);

        mainPanel.setLayout(new java.awt.CardLayout());

        empPanel.setBackground(new java.awt.Color(204, 204, 204));

        empTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        empTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CIN", "Nom", "Prenom"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        empTable.setFocusable(false);
        empTable.setOpaque(false);
        empTable.setRowHeight(25);
        empTable.setSelectionBackground(new java.awt.Color(240, 84, 84));
        empTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        empTable.setShowHorizontalLines(true);
        empTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(empTable);

        addEmp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        addEmp.setFocusable(false);
        addEmp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addEmpMouseClicked(evt);
            }
        });

        modifyEmp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/modify.png"))); // NOI18N
        modifyEmp.setFocusable(false);
        modifyEmp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modifyEmpMouseClicked(evt);
            }
        });

        deleteEmp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/delete.png"))); // NOI18N
        deleteEmp.setFocusable(false);
        deleteEmp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteEmpMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout empPanelLayout = new javax.swing.GroupLayout(empPanel);
        empPanel.setLayout(empPanelLayout);
        empPanelLayout.setHorizontalGroup(
            empPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, empPanelLayout.createSequentialGroup()
                .addGroup(empPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(empPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addEmp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modifyEmp, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteEmp))
                    .addGroup(empPanelLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1002, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        empPanelLayout.setVerticalGroup(
            empPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, empPanelLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(empPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(modifyEmp, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteEmp)
                    .addComponent(addEmp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        mainPanel.add(empPanel, "card2");

        artiPanel.setBackground(new java.awt.Color(204, 204, 204));

        articleTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        articleTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nom", "Prix", "ID_Stock"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        articleTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        articleTable.setFocusable(false);
        articleTable.setOpaque(false);
        articleTable.setRowHeight(25);
        articleTable.setSelectionBackground(new java.awt.Color(240, 84, 84));
        articleTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        articleTable.setShowHorizontalLines(true);
        articleTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(articleTable);

        addArticle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        addArticle.setFocusable(false);
        addArticle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addArticleMouseClicked(evt);
            }
        });

        modifyArticle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/modify.png"))); // NOI18N
        modifyArticle.setFocusable(false);
        modifyArticle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modifyArticleMouseClicked(evt);
            }
        });

        deleteArticle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/delete.png"))); // NOI18N
        deleteArticle.setFocusable(false);
        deleteArticle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteArticleMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout artiPanelLayout = new javax.swing.GroupLayout(artiPanel);
        artiPanel.setLayout(artiPanelLayout);
        artiPanelLayout.setHorizontalGroup(
            artiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, artiPanelLayout.createSequentialGroup()
                .addGroup(artiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(artiPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addArticle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modifyArticle, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteArticle))
                    .addGroup(artiPanelLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1002, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        artiPanelLayout.setVerticalGroup(
            artiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, artiPanelLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(artiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(modifyArticle, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteArticle)
                    .addComponent(addArticle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        mainPanel.add(artiPanel, "card2");

        cmdPanel.setBackground(new java.awt.Color(204, 204, 204));

        cmdTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cmdTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "num cmd", "Nom d'employé", "Prenom d'employé", "Date", "État"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        cmdTable.setFocusable(false);
        cmdTable.setOpaque(false);
        cmdTable.setRowHeight(25);
        cmdTable.setSelectionBackground(new java.awt.Color(240, 84, 84));
        cmdTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        cmdTable.setShowHorizontalLines(true);
        cmdTable.getTableHeader().setReorderingAllowed(false);
        cmdTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cmdTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(cmdTable);

        addCmd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        addCmd.setFocusable(false);
        addCmd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addCmdMouseClicked(evt);
            }
        });

        deleteCmd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/delete.png"))); // NOI18N
        deleteCmd.setFocusable(false);
        deleteCmd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteCmdMouseClicked(evt);
            }
        });

        addCmd1.setBackground(new java.awt.Color(0, 0, 0));
        addCmd1.setForeground(new java.awt.Color(0, 0, 0));
        addCmd1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/print.png"))); // NOI18N
        addCmd1.setFocusable(false);
        addCmd1.setPreferredSize(new java.awt.Dimension(64, 64));
        addCmd1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addCmd1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cmdPanelLayout = new javax.swing.GroupLayout(cmdPanel);
        cmdPanel.setLayout(cmdPanelLayout);
        cmdPanelLayout.setHorizontalGroup(
            cmdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cmdPanelLayout.createSequentialGroup()
                .addGroup(cmdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(cmdPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addCmd1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addCmd)
                        .addGap(18, 18, 18)
                        .addComponent(deleteCmd))
                    .addGroup(cmdPanelLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1002, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        cmdPanelLayout.setVerticalGroup(
            cmdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cmdPanelLayout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addGroup(cmdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(deleteCmd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addCmd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addCmd1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        mainPanel.add(cmdPanel, "card2");

        cmdOfEmploye.setBackground(new java.awt.Color(204, 204, 204));

        cmdOfEmpTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        cmdOfEmpTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id d'article", "Nom d'article", "Prix", "Quantité", "Prix total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        cmdOfEmpTable.setFocusable(false);
        cmdOfEmpTable.setOpaque(false);
        cmdOfEmpTable.setRowHeight(25);
        cmdOfEmpTable.setSelectionBackground(new java.awt.Color(240, 84, 84));
        cmdOfEmpTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        cmdOfEmpTable.setShowHorizontalLines(true);
        cmdOfEmpTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(cmdOfEmpTable);

        addArticleToCmd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        addArticleToCmd.setFocusable(false);
        addArticleToCmd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addArticleToCmdMouseClicked(evt);
            }
        });

        deleteArticleToCmd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/delete.png"))); // NOI18N
        deleteArticleToCmd.setFocusable(false);
        deleteArticleToCmd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteArticleToCmdMouseClicked(evt);
            }
        });

        cmdsTitel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cmdsTitel.setForeground(new java.awt.Color(0, 0, 0));
        cmdsTitel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cmdsTitel.setText("La commande de");

        goBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/arrow-left-line.png"))); // NOI18N
        goBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goBackMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout cmdOfEmployeLayout = new javax.swing.GroupLayout(cmdOfEmploye);
        cmdOfEmploye.setLayout(cmdOfEmployeLayout);
        cmdOfEmployeLayout.setHorizontalGroup(
            cmdOfEmployeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cmdOfEmployeLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(cmdOfEmployeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(cmdOfEmployeLayout.createSequentialGroup()
                        .addComponent(goBack, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(173, 173, 173)
                        .addComponent(cmdsTitel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(131, 131, 131)
                        .addComponent(addArticleToCmd)
                        .addGap(18, 18, 18)
                        .addComponent(deleteArticleToCmd))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1002, Short.MAX_VALUE))
                .addGap(33, 33, 33))
        );
        cmdOfEmployeLayout.setVerticalGroup(
            cmdOfEmployeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cmdOfEmployeLayout.createSequentialGroup()
                .addGroup(cmdOfEmployeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cmdOfEmployeLayout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addGroup(cmdOfEmployeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(deleteArticleToCmd)
                            .addComponent(addArticleToCmd)))
                    .addGroup(cmdOfEmployeLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(cmdOfEmployeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(goBack, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmdsTitel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        mainPanel.add(cmdOfEmploye, "card2");

        stockPanel.setBackground(new java.awt.Color(204, 204, 204));

        stockTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        stockTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id de stock", "Nom d'article", "Prix d'article", "Quantité total", "Prix total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        stockTable.setFocusable(false);
        stockTable.setOpaque(false);
        stockTable.setRowHeight(25);
        stockTable.setSelectionBackground(new java.awt.Color(240, 84, 84));
        stockTable.setSelectionForeground(new java.awt.Color(255, 255, 255));
        stockTable.setShowHorizontalLines(true);
        stockTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(stockTable);

        deleteStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/delete.png"))); // NOI18N
        deleteStock.setFocusable(false);
        deleteStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteStockMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout stockPanelLayout = new javax.swing.GroupLayout(stockPanel);
        stockPanel.setLayout(stockPanelLayout);
        stockPanelLayout.setHorizontalGroup(
            stockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stockPanelLayout.createSequentialGroup()
                .addGroup(stockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(stockPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(deleteStock))
                    .addGroup(stockPanelLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1002, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        stockPanelLayout.setVerticalGroup(
            stockPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stockPanelLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(deleteStock)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        mainPanel.add(stockPanel, "card2");

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addEmpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addEmpMouseClicked
        addEmpDialog addDialog = new addEmpDialog(mygui.this, true);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
        updateEmployesTable();
    }//GEN-LAST:event_addEmpMouseClicked

    private void deleteEmpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteEmpMouseClicked
        String cinToDelete = null;
        if (empTable.getSelectedRowCount() > 0) {
            cinToDelete = (String) empTable.getValueAt(empTable.getSelectedRow(), 0);

        } else {
            JOptionPane.showMessageDialog(this, "Selectioner un Employè");
        }
        if (cinToDelete != null) {
            JOptionPane deleteJOptionPane = new JOptionPane();
            int choise = deleteJOptionPane.showConfirmDialog(this, "Etes-vous sur de SUPPRIMER cet employé", "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            deleteJOptionPane.setFocusable(false);
            if (choise == 0) {
                try {
                    stmt.executeUpdate("delete from employe where cin='" + cinToDelete + "';");
                } catch (SQLException ex) {
                    Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        updateEmployesTable();

    }//GEN-LAST:event_deleteEmpMouseClicked

    private void modifyEmpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyEmpMouseClicked
        if (empTable.getSelectedRowCount() > 0) {
            cinToUpdate = (String) empTable.getValueAt(empTable.getSelectedRow(), 0);
            nomToUpdate = (String) empTable.getValueAt(empTable.getSelectedRow(), 1);
            prenomToUpdate = (String) empTable.getValueAt(empTable.getSelectedRow(), 2);
            if (cinToUpdate != null) {
                modifyEmpDialog modifyDialog = new modifyEmpDialog(this, true);
                modifyDialog.setLocationRelativeTo(this);
                modifyDialog.setVisible(true);
                updateEmployesTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selectioner un Employè");
        }


    }//GEN-LAST:event_modifyEmpMouseClicked

    private void articlesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_articlesMouseClicked
        employes.setForeground(new Color(153, 153, 153));
        articles.setForeground(Color.WHITE);
        commandes.setForeground(new Color(153, 153, 153));
        stocks.setForeground(new Color(153, 153, 153));

        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();

        mainPanel.add(artiPanel);
        mainPanel.repaint();
        mainPanel.revalidate();
        articleTable.clearSelection();
    }//GEN-LAST:event_articlesMouseClicked

    private void employesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_employesMouseClicked
        employes.setForeground(Color.WHITE);
        articles.setForeground(new Color(153, 153, 153));
        commandes.setForeground(new Color(153, 153, 153));
        stocks.setForeground(new Color(153, 153, 153));

        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();

        mainPanel.add(empPanel);
        mainPanel.repaint();
        mainPanel.revalidate();
        empTable.clearSelection();
    }//GEN-LAST:event_employesMouseClicked

    private void addArticleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addArticleMouseClicked
        addArticleDialog addArticleDialog = new addArticleDialog(this, true);
        addArticleDialog.setLocationRelativeTo(this);
        addArticleDialog.setVisible(true);
        updateArticlesTable();
        updateStockTable();
    }//GEN-LAST:event_addArticleMouseClicked

    private void modifyArticleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyArticleMouseClicked
        if (articleTable.getSelectedRowCount() > 0) {
            articleIdToUpdate = (String) articleTable.getValueAt(articleTable.getSelectedRow(), 0);
            articleNomToUpdate = (String) articleTable.getValueAt(articleTable.getSelectedRow(), 1);
            articlePrixToUpdate = (String) articleTable.getValueAt(articleTable.getSelectedRow(), 2);
            articleIdStockToUpdate = (String) articleTable.getValueAt(articleTable.getSelectedRow(), 3);

            if (articleIdToUpdate != null) {
                modifyArticleDialog modifyArticleDialog = new modifyArticleDialog(this, true);
                modifyArticleDialog.setLocationRelativeTo(this);
                modifyArticleDialog.setVisible(true);
                updateArticlesTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selectioner un Article");
        }
    }//GEN-LAST:event_modifyArticleMouseClicked

    private void deleteArticleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteArticleMouseClicked
        String idToDelete = null;
        if (articleTable.getSelectedRowCount() > 0) {
            idToDelete = (String) articleTable.getValueAt(articleTable.getSelectedRow(), 0);

        } else {
            JOptionPane.showMessageDialog(this, "Selectioner un Article");
        }
        if (idToDelete != null) {
            JOptionPane deleteJOptionPane = new JOptionPane();
            int choise = deleteJOptionPane.showConfirmDialog(this, "Etes-vous sur de SUPPRIMER cet article", "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            deleteJOptionPane.setFocusable(false);
            if (choise == 0) {
                try {
                    stmt.executeUpdate("delete article, stock from article inner join stock on article.id_stock=stock.id where article.id='" + idToDelete + "';");
                } catch (SQLException ex) {
                    Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        updateArticlesTable();

    }//GEN-LAST:event_deleteArticleMouseClicked

    private void addCmdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addCmdMouseClicked
        addCmdDialog addCmdDialog = new addCmdDialog(this, true);
        addCmdDialog.setLocationRelativeTo(this);
        addCmdDialog.setVisible(true);
        updateCmdTable();
    }//GEN-LAST:event_addCmdMouseClicked

    private void deleteCmdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteCmdMouseClicked
        String cmdToDelete = null;
        if (cmdTable.getSelectedRowCount() > 0) {
            cmdToDelete = (String) cmdTable.getValueAt(cmdTable.getSelectedRow(), 0);

        } else {
            JOptionPane.showMessageDialog(this, "Selectioner une commande");
        }
        if (cmdToDelete != null) {
            JOptionPane deleteJOptionPane = new JOptionPane();
            int choise = deleteJOptionPane.showConfirmDialog(this, "Etes-vous sur de SUPPRIMER cet commande", "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//            deleteJOptionPane.setFocusable(false);
            if (choise == 0) {
                try {
                    stmt.executeUpdate("delete from lien_cmd_article where num_cmd='" + cmdToDelete + "';");
                    if (cmdOfEmpTable.getRowCount() == 0) {
                        stmt.executeUpdate("delete from cmd where num='" + cmdToDelete + "';");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            updateCmdTable();
        }
    }//GEN-LAST:event_deleteCmdMouseClicked

    private void commandesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_commandesMouseClicked
        employes.setForeground(new Color(153, 153, 153));
        articles.setForeground(new Color(153, 153, 153));
        commandes.setForeground(Color.WHITE);
        stocks.setForeground(new Color(153, 153, 153));

        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();

        mainPanel.add(cmdPanel);
        mainPanel.repaint();
        mainPanel.revalidate();
        cmdTable.clearSelection();
    }//GEN-LAST:event_commandesMouseClicked

    private void addArticleToCmdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addArticleToCmdMouseClicked
        selectedNumCmd = (String) dtmForCmd.getValueAt(cmdTable.getSelectedRow(), 0);

        choseArticleDialogAfterCmdCreated choseArticleDialogAfterCmdCreated = new choseArticleDialogAfterCmdCreated(new javax.swing.JDialog(), true);
        choseArticleDialogAfterCmdCreated.setLocationRelativeTo(this);
        choseArticleDialogAfterCmdCreated.setVisible(true);
        selectedNumCmd = null;
        updateCmdSelectedTable();
    }//GEN-LAST:event_addArticleToCmdMouseClicked

    private void deleteArticleToCmdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteArticleToCmdMouseClicked
        String ArticleIdIncmdToDelete = null;

        if (cmdTable.getSelectedRowCount() > 0) {
            ArticleIdIncmdToDelete = (String) dtmForSelectedCmd.getValueAt(cmdOfEmpTable.getSelectedRow(), 0);

        } else {
            JOptionPane.showMessageDialog(this, "Selectioner un Article");
        }
        if (ArticleIdIncmdToDelete != null) {
            JOptionPane deleteJOptionPane = new JOptionPane();
            int choise = deleteJOptionPane.showConfirmDialog(this, "Etes-vous sur de SUPPRIMER cet Article?", "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choise == 0) {
                try {
                    stmt.executeUpdate("delete from lien_cmd_article where id_article='" + ArticleIdIncmdToDelete + "';");
                } catch (SQLException ex) {
                    Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            updateCmdSelectedTable();
        }
    }//GEN-LAST:event_deleteArticleToCmdMouseClicked

    private void cmdTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmdTableMouseClicked
        if (evt.getClickCount() == 2) {
            String cmdInfo = (String) cmdTable.getValueAt(cmdTable.getSelectedRow(), 1) + " " + cmdTable.getValueAt(cmdTable.getSelectedRow(), 2)
                    + " à " + cmdTable.getValueAt(cmdTable.getSelectedRow(), 3);
            cmdsTitel.setText("<html>La commande de " + cmdInfo + "</html>");
            updateCmdSelectedTable();

            mainPanel.removeAll();
            mainPanel.repaint();
            mainPanel.revalidate();

            mainPanel.add(cmdOfEmploye);
            mainPanel.repaint();
            mainPanel.revalidate();

        }
    }//GEN-LAST:event_cmdTableMouseClicked

    private void deleteStockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteStockMouseClicked
        String idStockToDelete = null;

        if (stockTable.getSelectedRowCount() > 0) {
            idStockToDelete = (String) dtmForStock.getValueAt(stockTable.getSelectedRow(), 0);

        } else {
            JOptionPane.showMessageDialog(this, "Selectioner un Stock");
        }

        if (idStockToDelete != null) {
            JOptionPane deleteJOptionPane = new JOptionPane();
            int choise = deleteJOptionPane.showConfirmDialog(this, "La suppression de ce stock entraînera la perte des produits qui lui appartiennent\nEtes-vous sur de SUPPRIMER cet Stock?", "Attention", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choise == 0) {
                try {
                    stmt.executeUpdate("delete from stock where id='" + idStockToDelete + "';");
                } catch (SQLException ex) {

                    Logger.getLogger(mygui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            updateArticlesTable();
            updateStockTable();
        }

    }//GEN-LAST:event_deleteStockMouseClicked

    private void stocksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stocksMouseClicked
        employes.setForeground(new Color(153, 153, 153));
        articles.setForeground(new Color(153, 153, 153));
        commandes.setForeground(new Color(153, 153, 153));
        stocks.setForeground(Color.WHITE);

        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();

        mainPanel.add(stockPanel);
        mainPanel.repaint();
        mainPanel.revalidate();
        stockTable.clearSelection();
        updateStockTable();
    }//GEN-LAST:event_stocksMouseClicked

    private void goBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goBackMouseClicked
        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();

        mainPanel.add(cmdPanel);
        mainPanel.repaint();
        mainPanel.revalidate();
        cmdTable.clearSelection();
    }//GEN-LAST:event_goBackMouseClicked

    private void addCmd1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addCmd1MouseClicked
        if (cmdTable.getSelectedRowCount() > 0) {
            String fullName = (String) cmdTable.getValueAt(cmdTable.getSelectedRow(), 1) + " " + cmdTable.getValueAt(cmdTable.getSelectedRow(), 2);
            HashMap hm = new HashMap();
            hm.put("cmdId", cmdTable.getValueAt(cmdTable.getSelectedRow(), 0));
            hm.put("nomEmp", fullName);

            try {
                JasperReport j = JasperCompileManager.compileReport("/home/omar/NetBeansProjects/gestion_stock/src/src/stock_app_report.jrxml");
                JasperPrint jp = JasperFillManager.fillReport(j, hm, (new mysql()).getCnx());
                JasperViewer jv = new JasperViewer(jp);
                jv.viewReport(jp, false);
                jv.setVisible(true);
                jv.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
            } catch (JRException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selectioner une Commande");
        }


    }//GEN-LAST:event_addCmd1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            //</editor-fold>
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(mygui.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(mygui.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(mygui.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(mygui.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new mygui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addArticle;
    private javax.swing.JLabel addArticleToCmd;
    private javax.swing.JLabel addCmd;
    private javax.swing.JLabel addCmd1;
    private javax.swing.JLabel addEmp;
    private javax.swing.JPanel artiPanel;
    private javax.swing.JTable articleTable;
    private javax.swing.JLabel articles;
    private javax.swing.JTable cmdOfEmpTable;
    private javax.swing.JPanel cmdOfEmploye;
    private javax.swing.JPanel cmdPanel;
    private javax.swing.JTable cmdTable;
    private javax.swing.JLabel cmdsTitel;
    private javax.swing.JLabel commandes;
    private javax.swing.JLabel deleteArticle;
    private javax.swing.JLabel deleteArticleToCmd;
    private javax.swing.JLabel deleteCmd;
    private javax.swing.JLabel deleteEmp;
    private javax.swing.JLabel deleteStock;
    private javax.swing.JPanel empPanel;
    private javax.swing.JTable empTable;
    private javax.swing.JLabel employes;
    private javax.swing.JLabel goBack;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel modifyArticle;
    private javax.swing.JLabel modifyEmp;
    private javax.swing.JPanel sideBar;
    private javax.swing.JPanel stockPanel;
    private javax.swing.JTable stockTable;
    private javax.swing.JLabel stocks;
    // End of variables declaration//GEN-END:variables
}

//class MyMouseListener extends MouseAdapter {
//  public void mouseClicked(MouseEvent evt) {
//    if (evt.getClickCount() == 3) {
//      System.out.println("triple-click");
//    } else if (evt.getClickCount() == 2) {
//      System.out.println("double-click");
//    }
//  }
//}
// class LabelRendar implements TableCellRenderer{
//
//    @Override
//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//      
//        return (Component)value;
//    }
//
//}
//             JLabel lebl = new JLabel();
//             ImageIcon checked = new javax.swing.ImageIcon(getClass().getResource("/src/checked.png"));
//             ImageIcon notChecked = new javax.swing.ImageIcon(getClass().getResource("/src/not.png"));
//                if (rsFroCmd.getInt(8) == 1) {
//                    lebl.setIcon(checked);
//                }else {
//                    lebl.setIcon(notChecked);
//                }
