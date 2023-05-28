package productcompositeservice.composite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static productcompositeservice.composite.IsSameEvent.sameEventExceptCreatedAt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.magnus.util.core.product.Product;
import se.magnus.util.event.Event;


public class IsSameEventTests {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {

        // 이벤트 1 과 이벤트 2 는 같은 이벤트이지만 다른 시간에 생성되었다.
        // 이벤트 3 과 이벤트 4 는 다른 이벤트이다
        Event<Integer, Product> event1 = new Event<>(Event.Type.CREATE, 1, new Product(1, "name", 1, null));
        Event<Integer, Product> event2 = new Event<>(Event.Type.CREATE, 1, new Product(1, "name", 1, null));
        Event<Integer, Product> event3 = new Event<>(Event.Type.DELETE, 1, null);
        Event<Integer, Product> event4 = new Event<>(Event.Type.CREATE, 1, new Product(2, "name", 1, null));

        String event1Json = mapper.writeValueAsString(event1);

        /**
         * MatcherAssert.assertThat 을 사용하면 서로 다른 두 객체를 매칭할 수 있다.
         *
         * 첫 번째 인자로 실제 값을 넣고, 두 번째 인자로 Matcher<> 를 상속한 객체를 만들고 메서드를 오버라이딩해서
         * 실제 값과 비교하는 논리를 적용할 수 있다.
         */
        MatcherAssert.assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
    }
}
