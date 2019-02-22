package com.yahoo.platform.yui.compressor;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MyCompressor {

    private static String rootPath = "F:/minjs/root";

    public static void main(String[] args) throws IOException {
        Path rootPath = Paths.get(MyCompressor.rootPath);
//        try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)){
//            for(Path e : stream){
//                System.out.println(e.getFileName());
//            }
//        }catch(IOException e){
//            e.printStackTrace();
//        }
        List<Path> result = new ArrayList<>(2000);
        Files.walkFileTree(rootPath, new  MySimpleFileVisitor(result));
    }

    private static class MySimpleFileVisitor extends SimpleFileVisitor<Path>{

        private List<Path> result;
        MySimpleFileVisitor(List<Path> result){
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            String fullPath = path.getParent().toString() + '\\' + path.getFileName();
            Reader in = new FileReader(path.toFile());
            if(fullPath.endsWith(".js")){
                try{
                    JavaScriptCompressor scriptCompressor = new JavaScriptCompressor(in, new ErrorReporter() {
                        public void warning(String message, String sourceName,
                                            int line, String lineSource, int lineOffset) {
//                            System.err.println("\n[WARNING] in " + fullPath);
//                            if (line < 0) {
//                                System.err.println("  " + message);
//                            } else {
//                                System.err.println("  " + line + ':' + lineOffset + ':' + message);
//                            }
                        }

                        public void error(String message, String sourceName,
                                          int line, String lineSource, int lineOffset) {
                            System.err.println("[ERROR] in " + fullPath);
                            if (line < 0) {
                                System.err.println("  " + message);
                            } else {
                                System.err.println("  " + line + ':' + lineOffset + ':' + message);
                            }
                        }

                        public EvaluatorException runtimeError(String message, String sourceName,
                                                               int line, String lineSource, int lineOffset) {
                            error(message, sourceName, line, lineSource, lineOffset);
                            return new EvaluatorException(message);
                        }

                    });
                    in.close();
                    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(fullPath)), "UTF-8");
                    scriptCompressor.compress(
                            out,
//                        new FileWriter(path.toFile()),  //输出路径
                            8000,   //行长度限制
                            true, //压缩过程是否修改参数名
                            true, //Display informational messages and warnings
                            true, //Preserve all semicolons 保存所有分号
                            true  //Disable all micro optimizations 禁用所有微观优化
                    );
                    out.close();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
            return super.visitFile(path, attrs);
        }
    }
}
