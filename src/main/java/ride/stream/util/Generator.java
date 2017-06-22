package ride.stream.util;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

public class Generator {

    private static final PodamFactory FACTORY = new PodamFactoryImpl();

    public static <T> T aNew(Class<T> clazz){
        return FACTORY.manufacturePojo(clazz);
    }

}
