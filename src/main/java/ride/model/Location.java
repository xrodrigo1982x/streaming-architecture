package ride.model;

import lombok.Data;
import uk.co.jemos.podam.common.PodamDoubleValue;

/**
 {-22.8531286, -43.3882947};
 {-22.8941379, -43.3126357};
 */

@Data
public class Location {

    @PodamDoubleValue(minValue = -22.8941379, maxValue = -22.8531286)
    private double latitude;
    @PodamDoubleValue(minValue = -43.3882947, maxValue = -43.3126357)
    private double longitude;

}
