package ride.model;

import lombok.Data;

import java.util.Date;

@Data
public class CarLocationEvent {

    private String carId;
    private Date timestamp;
    private Location location;
    private boolean available;

}
