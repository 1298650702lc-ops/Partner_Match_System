package com.jsoft.once;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//有个很重要的点 TableListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
public class TableListener implements ReadListener <XingQiuTableUserInfo> {
    /**
     * 这个每一条数据解析都会来调用
     * @param data
     * @param Context
     */
    @Override
    public void invoke(XingQiuTableUserInfo data, AnalysisContext Context) {
        log.info("解析到一条数据:{}", data);

    }

    /**
     * 所有数据解析完成之后会调用
     * @param Context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext Context) {
        log.info("所有数据解析完成！");
    }
}
