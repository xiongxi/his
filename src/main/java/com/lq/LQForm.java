package com.lq;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiongxi on 2018/1/7.
 */
public class LQForm {

    private static Logger logger = Logger.getLogger(LQForm.class);

    private AnyviewJTextField hospitalNameTextField;
    private JComboBox<String> hospitalBox;
    private static boolean isaddhospitalNameComplete = true;
    public volatile static JFrame frame;

    private StringBuilder updateRecordFile; //已经升级过的文件包
    private File updateRecordFileParent; //已经升级过的文件包地址
    private ArrayList<File> updateMoudelFileList = new ArrayList<>(); //需要升级的文件包
    private ArrayList<File> individuationProcessFileList = new ArrayList<>(); //医院个性过程
    private ArrayList<File> updateProcessFileList = new ArrayList<>(); //待升级过程
//    private ArrayList<File> allSubFile = new ArrayList<>();
    private ArrayList<String> moudelNameList = new ArrayList<>(); //带升级模块名称

    public LQForm() {

        initComponents();

        CANCELButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        SEARCH_PACKAGEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPackage();
            }
        });
        OPEN_DIRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(updateRecordFileParent);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        UPDATEButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        HOS_INFOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //显示升级记录
                showUpdateRecord();
            }
        });
    }

    public static void main(String[] args) {

        frame = new JFrame();

        LQForm lqForm = new LQForm();

        frame.setContentPane(lqForm.jPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        frame.setResizable(false);
        frame.setVisible(true);

    }

    private JPanel jPanel;
    private JLabel updateHospitalLabel;
    private JTextArea textArea_hos_info;
    private JTextArea textArea_moudel;
    private JTextArea textArea_log;
    private JButton SEARCH_PACKAGEButton;
    private JButton CANCELButton;
    private JButton UPDATEButton1;
    private JTextArea textArea_search_package;
    private JButton OPEN_DIRButton;
    private JTextField textField_package_name;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane_update_moudel;
    private JScrollPane scrollPane_search_package;
    private JScrollPane scrollPane_log;
    private JButton HOS_INFOButton;

    @SuppressWarnings("unchecked")
    private void initComponents() {

        final DefaultComboBoxModel hospitalBoxModel = new DefaultComboBoxModel();
        hospitalBox = new JComboBox(hospitalBoxModel) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };
        inithospitalBox();

        hospitalNameTextField.setJBox(hospitalBox);
        hospitalNameTextField.setLayout(new BorderLayout());
        hospitalNameTextField.add(hospitalBox, BorderLayout.SOUTH);

        setupAutoComplete(hospitalNameTextField, hospitalBox);

    }

    private void inithospitalBox() {

        hospitalBox.setSelectedItem(null);

        hospitalBox.setEditable(true);
        hospitalBox.setEnabled(true);

    }

    public void setupAutoComplete(final JTextField txtInput, final JComboBox<String> jBox) {

        setAdjusting(jBox, false);

        jBox.setSelectedItem(null);

        jBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                // 在JComboBox的监听事件时总是执行两次,原因如下:
                // ItemListener类中的方法itemStateChanged()事件的itemState有关，itemState在这里的状态有两个，Selected
                // 和 deSelected（即选中和未被选中）
                // 所以，当改变下拉列表中被选中的项的时候，其实是触发了两次事件：
                // 第一次是上次被选中的项的 State 由 Selected 变为 deSelected ，即取消选择
                // 第二次是本次被选中的项的 State 由 deSelected 变为 Selected ，即新选中，所以，必然的
                // ItemStateChanged 事件中的代码要被执行两次了。
                // 加上最外面的if语句，就可以解决这个问题。

                if (jBox.equals(hospitalBox)) {
                    if (!isaddhospitalNameComplete) {
                        return;
                    }
                }

                /*if (jBox.equals(classNameBox)) {
                    if (!isaddClassNameComplete)
                        return;
                }*/
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jBox.getSelectedItem() == null /*|| jBox.getSelectedItem().toString().equals("-请选择学校-")*/) {
                        return;
                    }
                    // 获取学校id和name
                    if (!isAdjusting(jBox)) {
                        /*if (jBox.equals(hospitalBox)) {
                            System.out.println(jBox.getSelectedItem() + "hospitalJBox");
                        }*/
                        if (jBox.getSelectedItem() != null) {
                            txtInput.setText(jBox.getSelectedItem().toString());
                        } else {
                            jBox.setPopupVisible(true);
                        }
                    }
                }
            }

        });

        txtInput.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                setAdjusting(jBox, true);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (jBox.isPopupVisible()) {
                        e.setKeyCode(KeyEvent.VK_ENTER);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.setSource(jBox);
                    jBox.dispatchEvent(e);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (jBox.getSelectedItem() != null)
                            txtInput.setText(jBox.getSelectedItem().toString());
                        jBox.setPopupVisible(false);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    jBox.setPopupVisible(false);
                }
                setAdjusting(jBox, false);
            }
        });
        txtInput.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                updateList();
            }

            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            private void updateList() {
                setAdjusting(jBox, true);
                JComboBox<String> jBox = (JComboBox<String>) txtInput.getComponent(0);
                /*if (jBox.equals(hospitalBox)) {
                }*/
                /*if (jBox.equals(classNameBox)) {
                }*/

                jBox.removeAllItems();
                String containStr = txtInput.getText().trim();
                addNameToBox(jBox, containStr);

                jBox.setPopupVisible(jBox.getItemCount() > 0);
                System.out.println("count" + jBox.getItemCount());
                setAdjusting(jBox, false);
            }
        });

    }

    //TODO 显示升级记录
    public void showUpdateRecord(){

        updateRecordFile = new StringBuilder();

        File file = new File(Constant.updateRecordDir);
        File[] array = file.listFiles();

        for(File f : array){
            String fileName = f.getName();
            if(f.isDirectory() && fileName.contains(hospitalNameTextField.getText())){ //找到长沙市八医院
                File[] array1 = f.listFiles();
                for(File f1 : array1) {
                    if(f1.isDirectory() && f1.getName().contains("医院升级记录")){
                        updateRecordFileParent = f1;
                        showUpdateRecordFile(f1);
                    }
                }
            }

        }

        if(null==updateRecordFile || "".equals(updateRecordFile.toString())){
            JOptionPane.showMessageDialog(frame, "search nothing,check your hospital");
        }
        textArea_hos_info.setText(updateRecordFile.toString());
    }

    private void showUpdateRecordFile(File f){
        File[] array = f.listFiles();
        for(File f1 : array){
            String fileName = f1.getName();
            if(f1.isFile() && (fileName.endsWith("zip")||fileName.endsWith("ZIP")||fileName.endsWith("rar")||fileName.endsWith("RAR"))){
                updateRecordFile.append(f1.getName() + "\n");
            }else if(f1.isDirectory()){
                showUpdateRecordFile(f1);
            }
        }
    }

    //TODO searchPackage
    public void searchPackage(){

        updateMoudelFileList = new ArrayList<>();
        moudelNameList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        String text = textArea_moudel.getText(); //获取到的升级模块文本
        String moudelArray[] = text.split("\\n");
        String moudelName;
        for(String moudel1 : moudelArray){
            logger.info("moudel1 = " + moudel1);
            getUpdateMoudelFileList(moudel1);
        }

        for(File f : updateMoudelFileList){
            sb.append(f.getName()).append("\n");
        }

        if("".equals(sb.toString())){
            JOptionPane.showMessageDialog(frame, "search nothing,check your files");
        }

        textArea_search_package.setText(sb.toString());

    }

    private void getUpdateMoudelFileList(String moudel1){

//        String moudelName;
        ArrayList<String> updateMoudelFileListName = new ArrayList<>();
//        StringBuilder sb = new StringBuilder();

//        moudel1 = "cxyhV2.2.9 to cxyhV2.3.3";

        String pattern = "([\\u4e00-\\u9fa5]+)V(\\d)\\.(\\d)\\.(\\d) to ([\\u4e00-\\u9fa5]+)V(\\d)\\.(\\d)\\.(\\d)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(moudel1);
        if (m.find()) {
//            logger.info("found");
            String moudelName = m.group(1);
            moudelNameList.add(moudelName);
            String moudelStart1 = m.group(2);
            String moudelStart2 = m.group(3);
            String moudelStart3 = m.group(4);
            String moudelEnd1 = m.group(6);
            String moudelEnd2 = m.group(7);
            String moudelEnd3 = m.group(8);

            Integer startVersion = Integer.valueOf(moudelStart1)*100+Integer.valueOf(moudelStart2)*10+Integer.valueOf(moudelStart3);
            Integer endVersion = Integer.valueOf(moudelEnd1)*100+Integer.valueOf(moudelEnd2)*10+Integer.valueOf(moudelEnd3);

            for(int i = startVersion+1; i<=endVersion ; i++){
                updateMoudelFileListName.add(moudelName+"V"+i/10/10%10+"."+i/10%10+"."+i%10);
            }

        } else {
            logger.info("not found");
        }

        File file = new File(Constant.updatePackageDir);
        getUpdateMoudelFileListFromName(file, updateMoudelFileListName);

    }

    private void getUpdateMoudelFileListFromName(File f, ArrayList<String> updateMoudelFileListName){

        if(null!=updateMoudelFileListName && updateMoudelFileListName.size()>0){
            Iterator<String> iterator = updateMoudelFileListName.iterator();
            while(iterator.hasNext()){
                String tmpStr = iterator.next();
                if(f.getName().contains(tmpStr)){
                    updateMoudelFileList.add(f);
                    iterator.remove();
                }
            }

            File[] arrayTemp = f.listFiles();
            for(File f1 : arrayTemp){
                if(f1.isDirectory()){
                    getUpdateMoudelFileListFromName(f1, updateMoudelFileListName);
                }

            }
        }
    }

    /*@Override
    public void actionPerformed(ActionEvent actionEvent) {
        System.exit(0);
    }*/

    private static void setAdjusting(JComboBox jBox, boolean adjusting) {
        jBox.putClientProperty("is_adjusting", adjusting);
    }

    private void addNameToBox(JComboBox<String> jBox, String containStr) {

        if (jBox.equals(hospitalBox)) {
            isaddhospitalNameComplete = false;
            if (containStr != null && containStr.trim() != "") {
                for (Map.Entry<Integer, String> entry : Constant.hospitalMap.entrySet()) {
                    if (entry.getValue().toLowerCase().contains(containStr.toLowerCase())) {
                        jBox.addItem(entry.getValue());
                    }
                    if (entry.getValue().toLowerCase().equals(containStr.trim())) {

                        // 发送网络请求 sendRequest(AnyProtocolKind.pkGetClass);
                    }
                } // for

            } else {
                for (Map.Entry<Integer, String> entry : Constant.hospitalMap.entrySet()) {

                    jBox.addItem(entry.getValue());

                } // for
            } // selse

            isaddhospitalNameComplete = true;
            hospitalBox.setSelectedItem(null);

        } // if hospitalBox


    }

    private static boolean isAdjusting(JComboBox cbInput) {
        if (cbInput.getClientProperty("is_adjusting") instanceof Boolean) {
            return (Boolean) cbInput.getClientProperty("is_adjusting");
        }
        return false;
    }

    private void update(){
//        merageFiles();
        //合并文件夹
        //数字开始和SetConfig.ini保留，其他提取出来合并

        String packageFileDir = updateRecordFileParent.getPath() + File.separator + textField_package_name.getText();

        File packageFile = new File(packageFileDir);
//        FileUtils.deleteQuietly(file);
        try {
            //删除原文件
            FileUtils.deleteDirectory(packageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!packageFile.exists()){
            packageFile.mkdirs();
        }

        try {
            //升级包移动
            for(File updateMoudelFile : updateMoudelFileList){
                FileUtils.copyDirectoryToDirectory(updateMoudelFile,packageFile);
            }
            //提取文件合并
            for(File updateMoudelFile : packageFile.listFiles()) {
                File[] array = updateMoudelFile.listFiles();
                for(File tempFile : array){
                    String pattern = "^\\d";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(tempFile.getName());
                    if (!(m.find() || tempFile.getName().equalsIgnoreCase("SetConfig.ini"))) {
                        if(tempFile.isDirectory()){
                            try{
                                FileUtils.copyDirectoryToDirectory(tempFile, packageFile);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            try{
                                FileUtils.copyFileToDirectory(tempFile, packageFile);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        FileUtils.forceDelete(tempFile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //个性过程提取
        File file = new File(Constant.individuationProcessDir);
        File[] array = file.listFiles();

        for(File f : array){
            String fileName = f.getName();
            if(f.isDirectory() && fileName.contains(hospitalNameTextField.getText())){ //找到长沙市八医院
                //找到所有个性过程文件
                final Collection<File> expectedFiles1 = FileUtils.listFilesAndDirs(f, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
                //找到升级包当中的所有文件
                final Collection<File> expectedFiles2 = FileUtils.listFilesAndDirs(packageFile, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
                for(File file1 : expectedFiles1){
                    for(File file2 : expectedFiles2) {
//                        String extensionName = FilenameUtils.getExtension(FILE_NAME);
                        if(file1.getName().equalsIgnoreCase(file2.getName()) && FilenameUtils.getExtension(file1.getName()).equalsIgnoreCase("sql")){
                            individuationProcessFileList.add(file1);
                        }
                    }
                }
                break;
            }

        }

        if(null!=individuationProcessFileList && individuationProcessFileList.size()>0){
            File individuationProcessFilePackage = new File(packageFileDir+File.separator+hospitalNameTextField.getText()+"个性过程");
            if(!individuationProcessFilePackage.exists()){
                individuationProcessFilePackage.mkdir();
            }
            for(File individuationProcessFile :individuationProcessFileList){
                try {
                    FileUtils.copyFileToDirectory(individuationProcessFile , individuationProcessFilePackage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //说明.txt合并
        File readme = new File(packageFile.getPath()+File.separator+"说明.txt");
        try {
            FileUtils.writeStringToFile(readme, "", "UTF-8", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(File tempFile : updateMoudelFileList){
            for(File tempFile1 : tempFile.listFiles()){
                if(tempFile1.getName().matches("说明\\.[T|t][X|x][T|t]")){
                    try {
                        StringBuilder sb = new StringBuilder();

                        sb.append(FileUtils.readFileToString(tempFile1, "GBK"));
                        sb.append("\n\n==========================================\n\n");
                        logger.info(sb.toString());
                        FileUtils.writeStringToFile(readme, sb.toString(), "UTF-8", true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        //发包必放文件

        //排序命名
        for(File fileRename : packageFile.listFiles()){
            if(fileRename.getName().equalsIgnoreCase("DLL")){
                fileRename.renameTo(new File(fileRename.getParent()+File.separator+"00"+fileRename.getName()));
            }
        }

        for(File fileRename : packageFile.listFiles()){
            for(File tempFile : updateMoudelFileList){
                if(fileRename.getName().equalsIgnoreCase(tempFile.getName())){
                    int flag = 0;
                    for(int i=0 ;i <moudelNameList.size();i++){
                        if (fileRename.getName().contains(moudelNameList.get(i))){
                            flag = i+1;
                        }
                    }

                    fileRename.renameTo(new File(fileRename.getParent()+File.separator+(flag<10?"0"+flag:flag)+fileRename.getName()));
                }

            }
        }

        JOptionPane.showMessageDialog(frame, "done");
    }

    private void getIndividuationProcessFile(File f){
        File[] array = f.listFiles();
        for(File f1 : array){
            if(f1.isDirectory()){
                getIndividuationProcessFile(f1);
            }else if(f1.getName().endsWith("sql") || f1.getName().endsWith("SQL")){

            }
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
