package fra.skatemap.runners;

import fra.skatemap.entities.Type;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.TypeDTO;
import fra.skatemap.services.TypeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class SpotTypeRunner implements CommandLineRunner {
    private final TypeService typeService;

    public SpotTypeRunner(TypeService typeService) {
        this.typeService = typeService;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> types = List.of("rail", "ladge", "stair", "skatepark", "street");
       for(String type : types ){
           try{
               this.typeService.save(new TypeDTO(type));
           }catch(BadRequestException ex){
               System.out.println("type already exist: "+type);
           }
       }
    }
}
