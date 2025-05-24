package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.emums.BookStatusEnum;
import com.example.demo.model.entity.Book;
import com.example.demo.model.input.BookParamIn;
import com.example.demo.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void testGetBookPage() {
        BookParamIn paramIn = new BookParamIn();
        paramIn.setAuthor("jim");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookParamIn> requestEntity = new HttpEntity<>(paramIn, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                "/api/v1/book", HttpMethod.GET, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        assertEquals("200",jsonObject.getString("code"));
    }

    @Test
    void testCreateBook() {
        Book book = new Book();
        book.setAuthor("jim");
        //随机生成
        long randomNum = (long) (Math.random() * (9999999999999L - 1000000000000L + 1)) + 1000000000000L;
        book.setIsbn(randomNum+"");
        book.setStatus(BookStatusEnum.ON_LINE.getStatus());
        book.setName("jack");
        book.setPublisher("浙江工业");
        book.setCreateTime(new Date());
        book.setUpdateTime(new Date());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Book> requestEntity = new HttpEntity<>(book, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                "/api/v1/book", HttpMethod.POST, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        assertEquals("200",jsonObject.getString("code"));
    }

    @Test
    void testUpdateBook() {
        Book book = new Book();
        //输入已存在的值
        book.setIsbn("978-7-544-25");
        book.setAuthor("jim");
        book.setStatus(BookStatusEnum.ON_LINE.getStatus());
        book.setName("jack");
        book.setPublisher("浙江工业");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Book> requestEntity = new HttpEntity<>(book, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                "/api/v1/book", HttpMethod.PUT, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        assertEquals("200",jsonObject.getString("code"));
    }

    @Test
    void testDelBook() {
        //输入已存在的数据
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("isbn", "978-7-544-26");
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                "/api/v1/book/{isbn}", HttpMethod.DELETE, null, String.class,uriVariables);
        String body = responseEntity.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        assertEquals("200",jsonObject.getString("code"));
    }


}
