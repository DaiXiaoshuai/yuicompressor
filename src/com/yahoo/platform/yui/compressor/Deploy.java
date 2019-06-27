package com.yahoo.platform.yui.compressor;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Deploy {

    //源码目录
    public static String sourcesRoot = "F:\\Web\\apache-tomcat-8.5.32\\webapps\\shxx_web";
    //目标目录
    public static String targetRoot = "F:\\minjs\\shxx_web";
//    //白名单 忽略的文件夹名 此验证方法仅判断路径中是否包含下列字符串
//    public static String[] ignores = {".idea", ".svn"};

    public static void main(String[] args) throws IOException {
        //删除老版本文件
        Path target = Paths.get(targetRoot);
        if(Files.exists(target)){
            Files.walkFileTree(target, new DeleteFilesVisitor());
        }
        //压缩并保存到目标目录
        Files.walkFileTree(Paths.get(sourcesRoot),new MoveAndSimplerVisitor() );
        Desktop.getDesktop().open(new File(targetRoot));
    }

    private static class DeleteFilesVisitor extends SimpleFileVisitor<Path>{
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.deleteIfExists(file);
            return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                throw exc;
            }
        }
    }

    private static class MoveAndSimplerVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Reader in = new FileReader(file.toFile());
            if(file.toString().endsWith(".js")&& !file.toString().endsWith("min.js")){
                try{
                    JavaScriptCompressor scriptCompressor = new JavaScriptCompressor(in, new ErrorReporter() {
                        public void warning(String message, String sourceName,
                                            int line, String lineSource, int lineOffset) {
                        }

                        public void error(String message, String sourceName,
                                          int line, String lineSource, int lineOffset) {
                            System.err.println("[ERROR] in " + file);
                            if (line < 0) {
                                System.err.println("  " + message);
                            } else {
                                System.err.println("  " + line + ':' + lineOffset + ':' + message);
                            }
                        }

                        public EvaluatorException runtimeError(String message, String sourceName,
                                                               int line, String lineSource, int lineOffset) {
                            error(message, sourceName, line, lineSource, lineOffset);
                            try {
                                Files.copy(file, Paths.get(file.toString().replace(sourcesRoot, targetRoot)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return new EvaluatorException(message);
                        }

                    });
                    in.close();
                    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(file.toString().replace(sourcesRoot, targetRoot))), StandardCharsets.UTF_8);
                    scriptCompressor.compress(
                            out,
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
            }else{
                 if(!file.toString().endsWith(".exe"))
                    Files.copy(file, Paths.get(file.toString().replace(sourcesRoot, targetRoot)));
            }
            return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if(dir.toString().contains(".idea")||dir.toString().contains(".svn")){
                return FileVisitResult.SKIP_SUBTREE;
            }
            Files.createDirectories(Paths.get(dir.toString().replace(sourcesRoot, targetRoot)));
            return super.preVisitDirectory(dir, attrs);
        }
    }

}
