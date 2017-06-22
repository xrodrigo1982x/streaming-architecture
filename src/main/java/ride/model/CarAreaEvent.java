package ride.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarAreaEvent {

    private String userId;
    private String carId;
    private Location location;
    private Date timestamp;

}
