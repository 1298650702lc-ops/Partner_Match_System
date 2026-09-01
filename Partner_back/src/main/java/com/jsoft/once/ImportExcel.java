package com.jsoft.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author F4EN
 * @description: 导入Excel 数据读取
 */
public class ImportExcel {
    public static void main(String[] args) {
        //定义路径
        String filePath = "D:\\tencent\\qqDownloads\\testExcel.xlsx";
        //监听器读取
        readByListener(filePath);
        //同步读取
        synchronousRead(filePath);
    }

    /**
     * 监听器读取取
     * @param filePath
     */
    public static void readByListener(String filePath) {
        // 读取excel
        //这里需要指定用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        //这里每次会读取100条数据，然后返回，直接调用invoke()方法，所以可以自定义缓存数量，或者使用doAfterAllAnalysed()方法读取所有数据
        EasyExcel.read(filePath, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }
    public static void synchronousRead(String filePath) {
        //同步读取 不推荐使用，数据量大会把数据放到内存里面
        //这里需要指定用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> list = EasyExcel.read(filePath).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo userInfo : list) {
            System.out.println(userInfo);
        }
    }
}
