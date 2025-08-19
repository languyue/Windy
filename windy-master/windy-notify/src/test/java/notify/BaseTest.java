package notify;


import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) // 关键：启用JUnit4的Spring支持
@SpringBootTest(classes = MessageNotifyTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BaseTest {

}
