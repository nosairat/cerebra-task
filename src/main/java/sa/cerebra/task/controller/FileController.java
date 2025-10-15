package sa.cerebra.task.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sa.cerebra.task.dto.request.LoginRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    @PostMapping("/upload")
    public ResponseEntity<?> login( @RequestBody LoginRequest request){
        log.info(request.getPhone());
        return ResponseEntity.ok().build();
    }

}
