package productcompositeservice.composite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.magnus.util.event.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TypeSafeMatcher 을 상속받은 클래스를 만들면 MatcherAssert.assertThat 을 사용해서
 * 인자로 값을 넣고 자동으로 비교할 수 있다.
 */
public class IsSameEvent extends TypeSafeMatcher<String> {

    private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

    private ObjectMapper mapper = new ObjectMapper();

    private Event expectedEvent;

    private IsSameEvent(Event expectedEvent){
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String eventAsJson) {
        if(expectedEvent == null){
            return false;
        }

        LOG.trace("Convert the following json string to a map: {}", eventAsJson);

        Map mapEvent = convertJsonStringToMap(eventAsJson);
        mapEvent.remove("eventCreatedAt");

        Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);

        LOG.trace("Got the map: {}", mapEvent);
        LOG.trace("Compare to the expected map: {}", mapExpectedEvent);
        return mapEvent.equals(mapExpectedEvent);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like" + expectedJson);
    }


    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent){
        return new IsSameEvent(expectedEvent);
    }
    private Map getMapWithoutCreatedAt(Event event){
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }


    private Map convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class); // 객체를 제이슨으로 교환
        return mapper.convertValue(node, Map.class); // 제이슨을 맵 클래스로 교환
    }

    private String convertObjectToJsonString(Object object){
        try{
            return mapper.writeValueAsString(object);
        }catch(JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
    private Map convertJsonStringToMap(String eventAsJson){
        try{
            return mapper.readValue(eventAsJson, new TypeReference<HashMap>() {});
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
