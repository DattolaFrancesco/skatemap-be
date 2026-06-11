package fra.skatemap.controllers;

import fra.skatemap.entities.Media;
import fra.skatemap.entities.Spot;
import fra.skatemap.enums.Status_spot;
import fra.skatemap.payloads.SpotResponseDTO;
import fra.skatemap.services.MediaService;
import fra.skatemap.services.SpotService;
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
    private final SpotService spotService;

    public MediaController(MediaService mediaService, SpotService spotService) {
        this.mediaService = mediaService;
        this.spotService = spotService;
    }

        @GetMapping("/ffmpeg")
        public String testFfmpeg() throws Exception {

            System.out.println("FFMPEG TEST START");

            Process p = new ProcessBuilder("ffmpeg", "-version").start();
            int exit = p.waitFor();

            System.out.println("FFMPEG TEST DONE");

            return "exit code = " + exit;
        }

    @PostMapping(value="/image/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void  saveImage(@PathVariable UUID id,@RequestParam("file") List<MultipartFile> files ){
        Spot spot = this.spotService.findSpotById(id);
        this.mediaService.saveImage(spot,files);
    }
   @PostMapping(value="/video/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void  saveVideo(@PathVariable UUID id,@RequestParam("file") List<MultipartFile> files ){
        Spot spot = this.spotService.findSpotById(id);
        this.mediaService.saveVideo(spot,files);
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
