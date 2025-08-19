package notify;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zj.domain.mapper.*")
@SpringBootApplication(scanBasePackages = {"com.zj.notify.*","com.zj.domain"})
public class MessageNotifyTestApplication {
}