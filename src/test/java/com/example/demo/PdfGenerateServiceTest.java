package com.example.demo;

import com.example.demo.model.entity.Book;
import com.example.demo.service.pdf.PdfGenerateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @description PDF文件生成服务测试类
 * @author ZhouYouMing
 * @date 2026/6/12 14:10
 */
@SpringBootTest
public class PdfGenerateServiceTest {

    /**
     * PDF文件生成服务
     */
    @Autowired
    private PdfGenerateService pdfGenerateService;

    /**
     * @description 测试生成图书清单PDF并保存到本地桌面
     * @param
     * @return void
     * @throws Exception 文件写入异常
     */
    @Test
    public void testGenerateBookListPdf_saveToDesktop() throws Exception {
        List<Book> books = buildMockBooks();
        byte[] pdfBytes = pdfGenerateService.generateBookListPdf(books);
        File outputFile = new File("C:\\Users\\zhouy\\Desktop\\book-list.pdf");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            outputStream.write(pdfBytes);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * @description 测试生成运维商账单确认书PDF并保存到本地桌面
     * @param
     * @return void
     * @throws Exception 文件写入异常
     */
    @Test
    public void testGenerateOperationBillConfirmationPdf_saveToDesktop() throws Exception {
        byte[] pdfBytes = pdfGenerateService.generateOperationBillConfirmationPdf();
        File outputFile = new File("C:\\Users\\zhouy\\Desktop\\operation-bill-confirmation.pdf");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            outputStream.write(pdfBytes);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    /**
     * @description 构建PDF测试图书数据
     * @param
     * @return 图书数据列表
     */
    private List<Book> buildMockBooks() {
        List<Book> books = new ArrayList<Book>();
        books.add(buildBook("978-7-544-25", "Java编程思想", "Bruce Eckel", "机械工业出版社", 1, "经典编程图书"));
        books.add(buildBook("978-7-111-21", "Spring Boot实战", "Craig Walls", "人民邮电出版社", 2, "测试中文PDF展示"));
        books.add(buildBook("978-7-302-01", "数据库系统概论", "王珊", "高等教育出版社", 0, "已下架"));
        return books;
    }

    /**
     * @description 构建单条图书测试数据
     * @param isbn 图书ISBN编码
     * @param name 图书名称
     * @param author 作者
     * @param publisher 出版社
     * @param status 图书状态
     * @param remark 备注
     * @return 图书实体
     */
    private Book buildBook(String isbn, String name, String author, String publisher, Integer status, String remark) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setName(name);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setStatus(status);
        book.setRemark(remark);
        return book;
    }
}
