package fra.skatemap.controllers;

import fra.skatemap.entities.Pin;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.PinDTO;
import fra.skatemap.services.PinService;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Pins")
public class PinController {
    private final PinService pinService;

    public PinController(PinService pinService) {
        this.pinService = pinService;
    }
    @PostMapping
    public PinDTO save(@RequestBody @Validated PinDTO body, BindingResult validation){
        if (validation.hasErrors()) throw new BadRequestException("pin is not valid");
        Pin found = this.pinService.save(body);
        return new PinDTO(found.getLatitude(),found.getLongitude());
    }
    @GetMapping
    public Page<Pin> findAll(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size){
        return this.pinService.findAll(page, size);
    }
}
