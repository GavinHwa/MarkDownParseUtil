import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class MarkDownParseUtil {

    // 存储去除后缀名的 markdown 文件名
    private String fileName = null;

    // 按行存储 markdown 文件
    private List<String> mdList = new ArrayList<>();

    // 存储 markdown 文件的每一行对应类型
    private List<String> mdTypeList = new ArrayList<>();

    private final String OTHER = "OTHER";

    /**
     * 提供 2 种构造方法，如新建类时提供了 markdown 文件名，则无需调用 readMarkdownFile 方法读入文件
     * 否则需主动调用 readMarkdownFile 方法读入文件
     */
    public MarkDownParseUtil() {
    }

    public MarkDownParseUtil(String fileName) {
        readMarkdownFile(fileName);
    }


    /**
     * 通过文件名读取一个 markdown 文件
     *
     * @param fileName 文件名
     */
    private void readMarkdownFile(String fileName) {

        try {
            mdList.clear();
            mdTypeList.clear();

            // 创建输入流
            fileName = fileName.trim();
            FileInputStream fis = new FileInputStream(fileName);
            InputStreamReader dis = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader mdFile = new BufferedReader(dis);
            this.fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));

            // 读取 markdown 文件
            String mdLine;
            mdList.add(" ");
            while ((mdLine = mdFile.readLine()) != null) {
                if (mdLine.isEmpty()) {
                    mdList.add(" ");
                } else {
                    mdList.add(mdLine);
                }
            }
            mdList.add(" ");
            mdFile.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    /**
     * 创建一个 html 文件，用于输出转换后的 html 语句
     *
     */
    private void createHtmlFile() {
        createHtmlFile(fileName + ".html");
    }


    /**
     * 重载 写入文件
     */
    private void createHtmlFile(String fileName) {
        defineAreaType();
        defineLineType();
        translateToHtml();

        try {
            // 创建输出流
            fileName = fileName.trim();
            FileOutputStream fis = new FileOutputStream(fileName.substring(0,fileName.lastIndexOf("."))+".html");
            OutputStreamWriter dis = new OutputStreamWriter(fis, StandardCharsets.UTF_8);
            BufferedWriter htmlFile = new BufferedWriter(dis);

            // 写入html头部
            htmlFile.write("<!DOCTYPE html>");
            htmlFile.newLine();
            htmlFile.write("<html lang=\"en\">");
            htmlFile.newLine();
            htmlFile.write("<meta charset=\"utf-8\">");
            htmlFile.newLine();
            htmlFile.write("<head>");
            htmlFile.newLine();
            htmlFile.write("<title>" + fileName.substring(fileName.lastIndexOf("\\")+1,fileName.indexOf(".")) + "</title>");
            htmlFile.newLine();
            //引入BootStrap和jQuery
            htmlFile.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            htmlFile.newLine();
            htmlFile.write("<link rel=\"stylesheet\" href=\"https://cdn.staticfile.org/twitter-bootstrap/4.3.1/css/bootstrap.min.css\">");
            htmlFile.newLine();
            htmlFile.write("<script src=\"https://cdn.staticfile.org/jquery/3.2.1/jquery.min.js\"></script>");
            htmlFile.newLine();
            htmlFile.write("<script src=\"https://cdn.staticfile.org/popper.js/1.15.0/umd/popper.min.js\"></script>");
            htmlFile.newLine();
            htmlFile.write("<script src=\"https://cdn.staticfile.org/twitter-bootstrap/4.3.1/js/bootstrap.min.js\"></script>");
            htmlFile.newLine();
            // 引入Typora给定的markdown转为html的CSS文件
            htmlFile.write("<link type=\"text/css\" rel=\"stylesheet\" href=\"css/markdown.css\"/>");
            htmlFile.newLine();
            htmlFile.write("</head>");
            htmlFile.newLine();
            htmlFile.write("<body>");
            htmlFile.newLine();

            // 写入 html 主体
            if (mdTypeList.size() == mdList.size()) {
                for (String s : mdList) {
                    htmlFile.write(s);
                    htmlFile.newLine();
                }
            }

            // 写入 html 尾部
            htmlFile.write("</body>");
            htmlFile.newLine();
            htmlFile.write("</html>");

            htmlFile.flush();
            htmlFile.close();

        } catch (IOException e) {
            System.out.println(e);
        }
    }


    /**
     * 判断每一段 markdown 语法对应的 html 类型
     */
    private void defineAreaType() {

        // 定位代码区
        ArrayList<String> tempList = new ArrayList<>();
        ArrayList<String> tempType = new ArrayList<>();

        tempType.add(OTHER);
        tempList.add(" ");

        boolean codeBegin = false, codeEnd = false;

        for (int i = 1; i < mdList.size() - 1; i++) {
            String line = mdList.get(i);
            if (line.length() > 2 && line.charAt(0) == '`' && line.charAt(1) == '`' && line.charAt(2) == '`') {
                // 进入代码区
                if (!codeBegin && !codeEnd) {
                    tempType.add("CODE_BEGIN");
                    tempList.add(" ");

                    codeBegin = true;
                }

                // 离开代码区
                else if (codeBegin && !codeEnd) {
                    tempType.add("CODE_END");
                    tempList.add(" ");

                    codeBegin = false;
                    codeEnd = false;
                } else {
                    tempType.add(OTHER);
                    tempList.add(line);
                }
            } else {
                tempType.add(OTHER);
                tempList.add(line);
            }
        }

        tempType.add(OTHER);
        tempList.add(" ");

        mdList = (ArrayList<String>) tempList.clone();
        mdTypeList = (ArrayList<String>) tempType.clone();

        tempList.clear();
        tempType.clear();

        // 定位其他区，注意代码区内无其他格式

        boolean isCodeArea = false;

        tempList.add(" ");
        tempType.add(OTHER);

        for (int i = 1; i < mdList.size() - 1; i++) {

            String line = mdList.get(i);
            String lastLine = mdList.get(i - 1);
            String nextLine = mdList.get(i + 1);

            if (mdTypeList.get(i).equals("CODE_BEGIN")) {
                isCodeArea = true;

                tempList.add(line);
                tempType.add("CODE_BEGIN");
                continue;
            }

            if (mdTypeList.get(i).equals("CODE_END")) {
                isCodeArea = false;

                tempList.add(line);
                tempType.add("CODE_END");

                continue;
            }

            // 代码区不含其他格式
            if (!isCodeArea) {
                // 进入引用区
                if (line.length() > 2 && line.charAt(0) == '>' && lastLine.charAt(0) != '>' && nextLine.charAt(0) == '>') {
                    tempList.add(" ");
                    tempList.add(line);

                    tempType.add("QUOTE_BEGIN");
                    tempType.add(OTHER);
                }

                // 离开引用区
                else if (line.length() > 2 && line.charAt(0) == '>' && lastLine.charAt(0) == '>' && nextLine.charAt(0) != '>') {
                    tempList.add(line);
                    tempList.add(" ");

                    tempType.add(OTHER);
                    tempType.add("QUOTE_END");
                }

                // 单行引用区
                else if (line.length() > 2 && line.charAt(0) == '>' && lastLine.charAt(0) != '>' && nextLine.charAt(0) != '>') {
                    tempList.add(" ");
                    tempList.add(line);
                    tempList.add(" ");
                    tempType.add("QUOTE_BEGIN");
                    tempType.add(OTHER);
                    tempType.add("QUOTE_END");
                }

                // 进入无序列表区
                else if ((line.charAt(0) == '-' && lastLine.charAt(0) != '-' && nextLine.charAt(0) == '-') ||
                        (line.charAt(0) == '+' && lastLine.charAt(0) != '+' && nextLine.charAt(0) == '+') ||
                        (line.charAt(0) == '*' && lastLine.charAt(0) != '*' && nextLine.charAt(0) == '*')) {

                    tempList.add(" ");
                    tempList.add(line);
                    tempType.add("UNORDER_BEGIN");
                    tempType.add(OTHER);
                }

                // 离开无序列表区
                else if ((line.charAt(0) == '-' && lastLine.charAt(0) == '-' && nextLine.charAt(0) != '-') ||
                        (line.charAt(0) == '+' && lastLine.charAt(0) == '+' && nextLine.charAt(0) != '+') ||
                        (line.charAt(0) == '*' && lastLine.charAt(0) == '*' && nextLine.charAt(0) != '*')) {

                    tempList.add(line);
                    tempList.add(" ");
                    tempType.add(OTHER);
                    tempType.add("UNORDER_END");
                }

                // 单行无序列表区
                else if ((line.charAt(0) == '-' && lastLine.charAt(0) != '-' && nextLine.charAt(0) != '-') ||
                        (line.charAt(0) == '+' && lastLine.charAt(0) != '+' && nextLine.charAt(0) != '+') ||
                        (line.charAt(0) == '*' && lastLine.charAt(0) != '*' && nextLine.charAt(0) != '*')) {
                    tempList.add(" ");
                    tempList.add(line);
                    tempList.add(" ");
                    tempType.add("UNORDER_BEGIN");
                    tempType.add(OTHER);
                    tempType.add("UNORDER_END");

                }

                // 进入有序列表区
                else if ((line.length() > 1 && (line.charAt(0) >= '1' || line.charAt(0) <= '9') && (line.charAt(1) == '.')) &&
                        !(lastLine.length() > 1 && (lastLine.charAt(0) >= '1' || line.charAt(0) <= '9') && (lastLine.charAt(1) == '.')) &&
                        (nextLine.length() > 1 && (nextLine.charAt(0) >= '1' || line.charAt(0) <= '9') && (nextLine.charAt(1) == '.'))) {

                    tempList.add(" ");
                    tempList.add(line);
                    tempType.add("ORDER_BEGIN");
                    tempType.add(OTHER);
                }

                // 离开有序列表区

                else if ((line.length() > 1 && (line.charAt(0) >= '1' || line.charAt(0) <= '9') && (line.charAt(1) == '.')) &&
                        (lastLine.length() > 1 && (lastLine.charAt(0) >= '1' || line.charAt(0) <= '9') && (lastLine.charAt(1) == '.')) &&
                        !(nextLine.length() > 1 && (nextLine.charAt(0) >= '1' || line.charAt(0) <= '9') && (nextLine.charAt(1) == '.'))) {

                    tempList.add(line);
                    tempList.add(" ");
                    tempType.add(OTHER);
                    tempType.add("ORDER_END");
                }

                // 单行有序列表区
                else if ((line.length() > 1 && (line.charAt(0) >= '1' || line.charAt(0) <= '9') && (line.charAt(1) == '.')) &&
                        !(lastLine.length() > 1 && (lastLine.charAt(0) >= '1' || line.charAt(0) <= '9') && (lastLine.charAt(1) == '.')) &&
                        !(nextLine.length() > 1 && (nextLine.charAt(0) >= '1' || line.charAt(0) <= '9') && (nextLine.charAt(1) == '.'))) {

                    tempList.add(" ");
                    tempList.add(line);
                    tempList.add(" ");
                    tempType.add("ORDER_BEGIN");
                    tempType.add(OTHER);
                    tempType.add("ORDER_END");
                }

                // 其他
                else {
                    tempList.add(line);
                    tempType.add(OTHER);
                }
            } else {
                tempList.add(line);
                tempType.add(OTHER);
            }
        }
        tempList.add(" ");
        tempType.add(OTHER);

        mdList = (ArrayList<String>) tempList.clone();
        mdTypeList = (ArrayList<String>) tempType.clone();

        tempList.clear();
        tempType.clear();
    }


    /**
     * 判断每一行 markdown 语法对应的 html 类型
     */
    private void defineLineType() {

        Stack<String> st = new Stack();

        for (int i = 0; i < mdList.size(); i++) {
            String line = mdList.get(i);
            String typeLine = mdTypeList.get(i);

            switch (typeLine) {
                case "QUOTE_BEGIN":
                case "UNORDER_BEGIN":
                case "ORDER_BEGIN":
                case "CODE_BEGIN": {
                    st.push(typeLine);
                    break;
                }
                case "QUOTE_END":
                case "UNORDER_END":
                case "ORDER_END":
                case "CODE_END": {
                    st.pop();
                }
                break;
                case OTHER:{
                    if (!st.isEmpty()) {
                        switch (st.peek()) {
                            // 引用行
                            case "QUOTE_BEGIN": {
                                mdList.set(i, line.trim().substring(1).trim());
                                break;
                            }

                            // 无序列表行
                            case "UNORDER_BEGIN": {
                                mdList.set(i, line.trim().substring(1).trim());
                                mdTypeList.set(i, "UNORDER_LINE");
                                break;
                            }

                            // 有序列表行
                            case "ORDER_BEGIN": {
                                mdList.set(i, line.trim().substring(2).trim());
                                mdTypeList.set(i, "ORDER_LINE");
                                break;
                            }

                            // 代码行
                            default: {
                                mdTypeList.set(i, "CODE_LINE");
                                break;
                            }
                        }
                    }

                    line = mdList.get(i);
                    typeLine = mdTypeList.get(i);

                    // 空行
                    if (line.trim().isEmpty()) {
                        mdTypeList.set(i, "BLANK_LINE");
                        mdList.set(i, "");
                    }

                    // 标题行
                    else if (line.trim().charAt(0) == '#') {
                        mdTypeList.set(i, "TITLE");
                        mdList.set(i, line.trim());
                    }
                    break;
                }
            }
        }
    }


    /**
     * 根据每一行的类型，将 markdown 语句 转化成 html 语句
     */
    private void translateToHtml() {
        for (int i = 0; i < mdList.size(); i++) {
            String line = mdList.get(i);
            String typeLine = mdTypeList.get(i);

            switch (typeLine) {
                // 是空行
                case "BLANK_LINE": {
                    mdList.set(i, "");
                    break;
                }

                // 是普通文本行
                case OTHER: {
                    mdList.set(i, "<p>" + translateToHtmlInline(line.trim()) + "</p>");
                    break;
                }

                // 是标题行
                case "TITLE": {
                    int titleClass = 1;
                    for (int j = 1; j < line.length(); j++) {
                        if (line.charAt(j) == '#') {
                            titleClass++;
                        } else {
                            break;
                        }
                    }
                    mdList.set(i, "<h" + titleClass + ">" + translateToHtmlInline(line.substring(titleClass).trim()) + "</h" + titleClass + ">");
                    break;
                }

                // 是无序列表行
                case "UNORDER_BEGIN":{
                    mdList.set(i, "<ul>");
                    break;
                }
                case "UNORDER_END":{
                    mdList.set(i, "</ul>");
                    break;
                }
                case "UNORDER_LINE":{
                    mdList.set(i, "<li>" + translateToHtmlInline(line.trim()) + "</li>");
                    break;
                }

                // 是有序列表行
                case "ORDER_BEGIN":{
                    mdList.set(i, "<ol>");
                    break;
                }
                case "ORDER_END":{
                    mdList.set(i, "</ol>");
                    break;
                }
                case "ORDER_LINE":{
                    mdList.set(i, "<li>" + translateToHtmlInline(line.trim()) + "</li>");
                    break;
                }

                // 是代码行
                case "CODE_BEGIN":{
                    mdList.set(i, "<pre>");
                    break;
                }
                case "CODE_END": {
                    mdList.set(i, "</pre>");
                    break;
                }
                case "CODE_LINE": {
                    mdList.set(i, "<code>" + line + "</code>");
                    break;
                }

                // 是引用行
                case "QUOTE_BEGIN":{
                    mdList.set(i, "<blockquote>");
                    break;
                }
                case "QUOTE_END":{
                    mdList.set(i, "</blockquote>");
                    break;
                }
            }
        }
    }


    /**
     * 将行内的 markdown 语句转换成对应的 html
     * @param line markdown语句
     * @return html 语句
     */
    private String translateToHtmlInline(String line) {

        String html = "";

        for (int i = 0; i < line.length(); i++) {
            // 图片
            if (i < line.length() - 4 && line.charAt(i) == '!' && line.charAt(i + 1) == '[') {
                int index1 = line.indexOf(']', i + 1);
                if (index1 != -1 && line.charAt(index1 + 1) == '(' && line.indexOf(')', index1 + 2) != -1) {
                    int index2 = line.indexOf(')', index1 + 2);
                    String picName = line.substring(line.lastIndexOf('/')+1,line.lastIndexOf(")"));
                    String picPath = "image/" + picName;
                    line = line.replace(line.substring(i, index2 + 1), "<img alt='" + picName + "' src='" + picPath + "' />");
                }
            }

            // 链接
            if (i < line.length() - 3 && ((i > 0 && line.charAt(i) == '[' && line.charAt(i - 1) != '!') || (line.charAt(0) == '['))) {
                int index1 = line.indexOf(']', i + 1);
                if (index1 != -1 && line.charAt(index1 + 1) == '(' && line.indexOf(')', index1 + 2) != -1) {
                    int index2 = line.indexOf(')', index1 + 2);
                    String linkName = line.substring(i + 1, index1);
                    String linkPath = line.substring(index1 + 2, index2);
                    line = line.replace(line.substring(i, index2 + 1), "<a href='" + linkPath + "' target="_blank"> " + linkName + "</a>");
                }
            }

            // 行内引用
            if (i < line.length() - 1 && line.charAt(i) == '`' && line.charAt(i + 1) != '`') {
                int index = line.indexOf('`', i + 1);
                if (index != -1) {
                    String quoteName = line.substring(i + 1, index);
                    line = line.replace(line.substring(i, index + 1), "<code>" + quoteName + "</code>");
                }
            }

            // 粗体
            if (i < line.length() - 2 && line.charAt(i) == '*' && line.charAt(i + 1) == '*') {
                int index = line.indexOf("**", i + 1);
                if (index != -1) {
                    String quoteName = line.substring(i + 2, index);
                    line = line.replace(line.substring(i, index + 2), "<strong>" + quoteName + "</strong>");
                }
            }

            // 斜体
            if (i < line.length() - 2 && line.charAt(i) == '*' && line.charAt(i + 1) != '*') {
                int index = line.indexOf('*', i + 1);
                if (index != -1 && line.charAt(index + 1) != '*') {
                    String quoteName = line.substring(i + 1, index);
                    line = line.replace(line.substring(i, index + 1), "<i>" + quoteName + "</i>");
                }
            }
        }
        return line;
    }


    public static void main(String[] args) {
        MarkDownParseUtil md = new MarkDownParseUtil("src\\@SpringBootApplication.md");
        md.createHtmlFile();
    }
}
