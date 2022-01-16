/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package systems.proto.nsflash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author peepshow-21
 *
 */
public class MakeChunksFrame extends JFrame {
    
    private static final String CHUNK_FILE  = "nextion";
    private static final String PROP_FILE   = "tft_file";
    private static final String PROP_DIR    = "chunk_dir";
    
    private File propsFile;
    private File tftFile;
    private File chunkDir;
    private Properties props;

    /**
     * Creates new form MakeChunksFrame
     */
    public MakeChunksFrame() {
        try {
            initComponents();        
            setLocationRelativeTo(null);
            propsFile = new File(System.getProperty("home.dir"),"nextion.properties");
            props = new Properties();
            if (propsFile.exists()) {
                props.load(new FileReader(propsFile));                
                String tft = props.getProperty(PROP_FILE);
                tftFile = new File(tft);
                fileTF.setText(tft);
                String dir = props.getProperty(PROP_DIR);
                chunkDir = new File(dir);
                fileTF.setText(tft);
                dirTF.setText(dir);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void build() {
        
        byte[] b = new byte[4096];
        try {
            FileInputStream is = new FileInputStream(tftFile);
            FileWriter outFile = new FileWriter(new File(chunkDir,
                    String.format("%s.txt", CHUNK_FILE)));
            outFile.write(String.valueOf(tftFile.length()));
            outFile.close();                   
            int r = 1;
            int chunk = 0;
            while (r>0) {
                r = is.read(b, 0, 4096);
                if (r>0) {
                    StringBuilder s = new StringBuilder();
                    for (int i=0; i<r; i++) {
                        s.append(String.format("%02X", b[i]));
                    }
                    String outName = String.format("%s-%04d.hex", CHUNK_FILE, chunk);
                    outFile = new FileWriter(new File(chunkDir,outName));
                    outFile.write(s.toString());
                    outFile.close();
                    chunk++;
                }
            }
            props.setProperty(PROP_FILE, tftFile.getAbsolutePath());
            props.setProperty(PROP_DIR, chunkDir.getAbsolutePath());
            props.store(new FileWriter(propsFile), null);
        } catch (Exception ex) {
            Logger.getLogger(MakeChunksFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        findFileBN = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        dirTF = new javax.swing.JTextField();
        findDirBN = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tasmota NSPanel Display Firmware Upload");

        fileTF.setColumns(50);
        fileTF.setFocusable(false);

        jLabel1.setText("TFT File");

        findFileBN.setText("Find");
        findFileBN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findFileBNActionPerformed(evt);
            }
        });

        jButton2.setText("Build");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel2.setText("Chunk Directory");

        dirTF.setColumns(50);
        dirTF.setFocusable(false);

        findDirBN.setText("Find");
        findDirBN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findDirBNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fileTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(findFileBN))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(dirTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(findDirBN)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findFileBN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dirTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findDirBN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void findFileBNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findFileBNActionPerformed
        JFileChooser chooser = new JFileChooser();
        int stat = chooser.showOpenDialog(this);
        tftFile = chooser.getSelectedFile();
        fileTF.setText(tftFile.getAbsolutePath());                
    }//GEN-LAST:event_findFileBNActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        build();
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void findDirBNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findDirBNActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int stat = chooser.showOpenDialog(this);
        chunkDir = chooser.getSelectedFile();
        dirTF.setText(chunkDir.getAbsolutePath());                
    }//GEN-LAST:event_findDirBNActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MakeChunksFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MakeChunksFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MakeChunksFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MakeChunksFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MakeChunksFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dirTF;
    private javax.swing.JTextField fileTF;
    private javax.swing.JButton findDirBN;
    private javax.swing.JButton findFileBN;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
