package fra.skatemap.controllers;

import fra.skatemap.config.SpotSpecification;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.User;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.SpotRequestDTO;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.services.SpotService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/spots")
public class SpotController {
    private final SpotService spotService;

    public SpotController(SpotService spotService) {
        this.spotService = spotService;
    }

    @PostMapping
    public Spot save(@RequestBody @Validated SpotRequestDTO body, BindingResult validation,@AuthenticationPrincipal User user){
        if (validation.hasErrors()) {
            String errors = validation.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new BadRequestException("invalid data: " + errors);
        }
        return this.spotService.save(body,user);
    }
   /* @GetMapping("/single/{id}")
    public SpotResponseDTO findById(@PathVariable UUID id){
        return this.spotService.findById(id);
    }*/

    @GetMapping("/all")
    public Page<SpotResponseDTO> findAllSpotByStatus(@RequestParam(required = false)Status_spot status,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "50") int size,
                                          @RequestParam(defaultValue = "name") String sortBy) {
        return this.spotService.findAllSpotByStatus(status,page,size,sortBy);
    }
    @GetMapping
    public Page<SpotResponseDTO> filterSpots(
            @RequestParam(required = false) List<String> continent,
            @RequestParam(required = false) List<String> risk,
            @RequestParam(required = false) List<String> type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Specification<Spot> spec = Specification
                .where(SpotSpecification.hasContinent(continent))
                .and(SpotSpecification.hasRisk(risk))
                .and(SpotSpecification.hasType(type));
        return this.spotService.filterSpots(spec,page,size,sortBy);
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable UUID id){
        return this.spotService.deleteById(id);
    }

    @PutMapping("/{id}")
    public SpotResponseDTO modifyById(@RequestBody @Validated SpotRequestDTO body, BindingResult validation, @PathVariable UUID id){
        if (validation.hasErrors()) {
            String errors = validation.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            throw new BadRequestException("invalid data: " + errors);
        }
        return this.spotService.modifyById(id,body);
    }

}
