package com.example.demo.service.pdf;

import com.example.demo.model.entity.Book;

import java.util.List;

/**
 * @description PDF文件生成服务
 * @author ZhouYouMing
 * @date 2026/6/12 14:10
 */
public interface PdfGenerateService {

    /**
     * @description 根据图书列表生成图书清单PDF字节数组
     * @param books 图书数据列表
     * @return PDF文件字节数组
     */
    byte[] generateBookListPdf(List<Book> books);

    /**
     * @description 生成运维商账单确认书PDF字节数组
     * @param
     * @return PDF文件字节数组
     */
    byte[] generateOperationBillConfirmationPdf();
}
