package ride.routing;

import feign.Body;
import feign.Param;
import feign.RequestLine;

public interface Elasticsearch {

    String QUERY = "%7B%22query%22%3A%7B%22percolate%22%3A%7B%22field%22%3A%22query%22%2C%22document_type%22%3A%22carLocation%22%2C%22document%22%3A%7B%22carId%22%3A%22{carId}%22%2C%22location%22%3A%7B%22lat%22%3A{lat}%2C%22lon%22%3A{lon}%7D%7D%7D%7D%7D";

    @RequestLine("POST /carlocation/_search")
    @Body(QUERY)
    String search(@Param("carId") String id, @Param("lat") double lat, @Param("lon") double lon);
}
