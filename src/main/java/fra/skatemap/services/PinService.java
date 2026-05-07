package fra.skatemap.services;

import fra.skatemap.entities.Pin;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.PinDTO;
import fra.skatemap.repositories.PinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PinService {
    private final PinRepository pinRepository;

    public PinService(PinRepository pinRepository) {
        this.pinRepository = pinRepository;
    }
    public Pin save(PinDTO pin){
        if(this.pinRepository.existsByLatitudeAndLongitude(pin.latitudine(), pin.longitude())){
            throw new BadRequestException("location already exist in the db");
        }
        Pin newPin = new Pin(pin.latitudine(),pin.longitude());
        return this.pinRepository.save(newPin);
    }
    public Page<Pin> findAll(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return this.pinRepository.findAll(pageable);
    }
}
