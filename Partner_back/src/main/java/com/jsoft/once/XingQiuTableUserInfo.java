package com.jsoft.once;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
/**
 * @author F4EN
 * @description: 星球表格用户信息（数据清洗用）
 */
public class XingQiuTableUserInfo {
    @ExcelProperty("成员编号")
    private String plantCode;
    @ExcelProperty("成员昵称")
    private String username;
    @ExcelProperty("本月积分")
    private Float score;
}
