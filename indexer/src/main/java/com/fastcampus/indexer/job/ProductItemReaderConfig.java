import javax.sql.DataSource;

import com.fastcampus.indexer.model.Product;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

@Configuration
public class ProductItemReaderConfig {

    private final DataSource dataSource;

    @Autowired
    public ProductItemReaderConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Product> productItemReader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("productItemReader")
                .dataSource(dataSource)
                .sql("SELECT id, asin, title, img_url, product_url, stars, reviews, price, list_price, category_id, is_best_seller, bought_in_last_month, created_at, updated_at FROM products")
                .rowMapper(new BeanPropertyRowMapper<>(Product.class))
                .build();
    }
}



