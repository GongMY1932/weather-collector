package com.base.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @BelongsProject: sdsat
 * @BelongsPackage: com.sdsat.common.util
 * @Class: DocUtils
 * @Author: ZH HOME
 * @CreateTime: 2023-06-27  20:46
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
public class DocUtils {

    /**
     * 保存到模板
     *
     * @param filePath
     * @param dataMap
     * @throws IOException
     */
    public static void saveWord(String templatePath, String filePath, Map<String, String> dataMap) throws IOException, InvalidFormatException {
        XWPFDocument document = new XWPFDocument(OPCPackage.open(templatePath));
        // 处理文本
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            processParagraph(paragraph, dataMap);
        }
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        processParagraph(paragraph, dataMap);
                    }
                }
            }
        }
        FileOutputStream outStream = new FileOutputStream(filePath);
        document.write(outStream);
        outStream.close();
    }

    private static void processParagraph(XWPFParagraph paragraph, Map<String, String> dataMap) {
        List<XWPFRun> runs = paragraph.getRuns();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
                run.setText(text, 0);
            }
        }
    }


    /**
     * 插入模板
     *
     * @param templatePath
     * @param filePath
     * @param dataMap0
     * @throws IOException
     * @throws InvalidFormatException
     */
    public static void insertWord(String templatePath, String filePath, Map<String, String> dataMap0) throws IOException, InvalidFormatException {
        //对hashMap做一个排序
        List<Map.Entry<String, String>> sortedEntries = dataMap0.entrySet().stream()
                .sorted(Comparator.comparing(DocUtils::getKeySuffix)
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());
        Map<String, String> dataMap = new LinkedHashMap<>();
        sortedEntries.forEach(entry -> dataMap.put(entry.getKey(), entry.getValue()));
        saveWord(templatePath, filePath, dataMap);
        try {
            // 打开Word文档
            XWPFDocument document = new XWPFDocument(new FileInputStream(filePath));
            int rowIndex = 2; // 要插入记录的起始行索引（从0开始）
            // 计算记录数量（假设每条记录都以数字结尾，从1开始递增）
            int recordCount = calculateRecordCount(dataMap);
            // 构建新记录数组
            String[][] newRecords = new String[recordCount][4];
            int count = 0;
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                XWPFParagraph paragraph = document.createParagraph();
                if (key.startsWith("${imageTime}")) {
                    newRecords[count][0] = ".";
                    newRecords[count][1] = "数据块" + getIndexFromKey(entry.getKey());
                    newRecords[count][2] = "程控成像" + value;
                    newRecords[count][3] = "自行发送间隔10s";
                    // 添加数据块1
                    XWPFRun run1 = paragraph.createRun();
                    run1.setText("数据块" + getIndexFromKey(entry.getKey()) + "  145  程控成像（" + value + "）");
                    run1.addBreak();
                    run1.setText(dataMap.get("${imageByte}" + getIndexFromKey(entry.getKey())));
                    count++;
                } else if (key.startsWith("${playbackTime}")) {
                    newRecords[count][0] = ".";
                    newRecords[count][1] = "数据块" + getIndexFromKey(entry.getKey());
                    newRecords[count][2] = "数传回放" + value;
                    newRecords[count][3] = "自行发送间隔10s";
                    // 添加数据块2
                    XWPFRun run2 = paragraph.createRun();
                    run2.setText("数据块" + getIndexFromKey(entry.getKey()) + "  145  数传回放（" + value + "）");
                    run2.addBreak();
                    run2.setText(dataMap.get("${playbackByte}" + getIndexFromKey(entry.getKey())));
                    count++;
                }
//                // 添加段落间的空行
//                document.createParagraph().createRun().addBreak();
                if (count == recordCount) {
                    break;
                }
            }
            // 获取第一个表格（如果有多个表格，可以根据需要修改）
            XWPFTable table = document.getTables().get(0);
            // 逐条插入新记录
            for (int i = 0; i < newRecords.length; i++) {
                int currentRowIndex = rowIndex + i;
                String[] newRecord = newRecords[i];
                // 在指定行后插入新行
                XWPFTableRow newRow = table.insertNewTableRow(currentRowIndex + 1);
                // 复制指定行的格式到新行
                XWPFTableRow sourceRow = table.getRow(currentRowIndex);
                newRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
                // 在新行中添加单元格并设置内容
                for (String s : newRecord) {
                    XWPFTableCell newCell = newRow.createCell();
                    newCell.setText(s);
                }
                // 更新表格中的序号（从当前行开始）
                for (int k = 1; k < table.getNumberOfRows(); k++) {
                    XWPFTableRow row = table.getRow(k);
                    XWPFTableCell cell = row.getCell(0);
                    String originalNumber = cell.getText();
                    String updatedNumber = (k) + ".";
                    cell.removeParagraph(0); // 移除原有段落
                    XWPFParagraph paragraph = cell.addParagraph(); // 添加新段落
                    paragraph.setAlignment(ParagraphAlignment.CENTER); // 设置对齐方式
                    XWPFRun run = paragraph.createRun(); // 创建新的运行
                    run.setText(updatedNumber); // 设置新的序号
                    run.setFontSize(11); // 设置字体大小
                    run.setFontFamily("Arial"); // 设置字体样式
                    run.setBold(false); // 设置是否粗体
                }
            }
            // 保存修改后的Word文档
            FileOutputStream outStream = new FileOutputStream(filePath);
            document.write(outStream);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取key后缀
    private static String getKeySuffix(Map.Entry<String, String> entry) {
        String key = entry.getKey();
        return key.matches(".*\\d+$") ? key.replaceAll("^.*?(\\d+)$", "$1") : "";
    }

    // 根据key获取记录的索引（假设key的结尾是数字）
    private static int getIndexFromKey(String key) {
        String indexStr = key.substring(key.length() - 1);
        return Integer.parseInt(indexStr);
    }

    // 计算记录数量
    private static int calculateRecordCount(Map<String, String> dataMap) {
        int count = 0;
        for (String key : dataMap.keySet()) {
            if (key.startsWith("${imageTime}") || key.startsWith("${playbackTime}")) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("${circle}", "4326");
            dataMap.put("${station}", "4326");
            dataMap.put("${imageByte}1", "eb90");
            dataMap.put("${playbackByte}1", "eb907557");
            dataMap.put("${imageTime}1", "2020/10/12 45:78:23");
            dataMap.put("${playbackTime1}", "2020/10/12 45:78:23");
            dataMap.put("${imageByte}2", "eb90");
            dataMap.put("${playbackByte}2", "eb907557");
            dataMap.put("${imageTime}2", "2020/10/12 45:78:23");
            dataMap.put("${playbackTime2}", "2020/10/12 45:78:23");
            dataMap.put("${imageByte}3", "eb90");
            dataMap.put("${playbackByte}3", "eb907557");
            dataMap.put("${imageTime}3", "2020/10/12 45:78:23");
            dataMap.put("${playbackTime3}", "2020/10/12 45:78:23");


            dataMap.put("${date}", "2021年9月9日");
            dataMap.put("${author}", "张老三");
            DocUtils.saveWord("C:\\Users\\zhang\\Desktop\\JZJ6-instruct-temp.docx", "C:\\Users\\zhang\\Desktop\\金紫荆6号指令.docx", dataMap);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}

