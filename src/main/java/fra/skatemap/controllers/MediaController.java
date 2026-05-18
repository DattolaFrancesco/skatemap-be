package fra.skatemap.controllers;

import fra.skatemap.entities.Media;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.services.MediaService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    @PostMapping(value="/image/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void  saveImage(@PathVariable UUID id,@RequestParam("file") List<MultipartFile> files ){
        this.mediaService.saveImage(id,files);
    }
    @PostMapping(value="/video/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void  saveVideo(@PathVariable UUID id,@RequestParam("file") List<MultipartFile> files ){
        this.mediaService.saveVideo(id,files);
    }
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id){
        this.mediaService.deleteById(id);
    }
    @GetMapping("/{id}")
    public Page<Media> findAllMediaByIdAndType(@PathVariable UUID id,@RequestParam(required = false) String type,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size){
        return this.mediaService.findAllMediaByIdAndType(id,type,page,size);
    }
}
