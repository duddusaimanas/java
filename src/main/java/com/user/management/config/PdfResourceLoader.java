package com.user.management.config;

import java.io.IOException;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.lang.Arrays;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PdfResourceLoader {

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    @PostConstruct
    public void init() throws IOException {
        Integer count = jdbcClient.sql("select count(*) from vector_store")
                .query(Integer.class)
                .single();

        if (count == 0) {
            log.info("Loading resources into vector store");

            Arrays.asList(new PathMatchingResourcePatternResolver().getResources("classpath:docs/*"))
                    .forEach(urlString -> {
                        var pdfDocumentReader = new PagePdfDocumentReader(urlString);
                        var documents = new TokenTextSplitter().apply(pdfDocumentReader.get());
                        vectorStore.accept(documents);
                    });

            log.info("Context is ready");
        }
    }
}
